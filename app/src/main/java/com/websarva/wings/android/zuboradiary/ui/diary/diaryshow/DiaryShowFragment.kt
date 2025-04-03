package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.requireValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
internal class DiaryShowFragment : BaseFragment() {

    companion object {
        // Navigation関係
        private val fromClassName = "From" + DiaryShowFragment::class.java.name
        val KEY_SHOWED_DIARY_DATE: String = "ShowedDiaryDate$fromClassName"
    }

    // View関係
    private var _binding: FragmentDiaryShowBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val diaryShowViewModel: DiaryShowViewModel by viewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backFragment(false)
            }
        })

        pictureUriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean? {
                    return diaryShowViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryShowBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryShowFragment.viewLifecycleOwner
            diaryShowViewModel = this@DiaryShowFragment.diaryShowViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPendingDialogObserver()
        setUpDiaryData()
        setUpToolBar()
        setUpWeatherLayout()
        setUpConditionLayout()
        setUpItemLayout()
        setUpPicture()
        setUpLogLayout()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun handleOnReceivingDialogResult() {
        receiveDiaryLoadingFailureDialogResult()
        receiveDiaryDeleteDialogResult()
    }

    override fun removeDialogResultOnDestroy() {
        removeResulFromFragment(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    override fun setUpOtherAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(diaryShowViewModel).onChanged(value)
                }
        }
    }

    // 日記読込失敗確認ダイアログフラグメントからデータ受取
    private fun receiveDiaryLoadingFailureDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingFailureDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        backFragment(true)
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryShowViewModel.deleteDiary()
            if (!isSuccessful) return@launch

            releasePictureUriPermission()
            withContext(Dispatchers.Main) {
                backFragment(true)
            }
        }
    }

    private fun releasePictureUriPermission() {
        val pictureUri = diaryShowViewModel.picturePath.value ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            pictureUriPermissionManager.releasePersistablePermission(pictureUri)
        }
    }

    private fun setUpPendingDialogObserver() {
        addNavBackStackEntryLifecycleObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                diaryShowViewModel.triggerPendingDialogListObserver()
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.pendingDialogList
                .collectLatest { value: PendingDialogList ->
                    val pendingDialog = value.findFirstItem() ?: return@collectLatest
                    val date = diaryShowViewModel.date.requireValue()
                    withContext(Dispatchers.Main) {
                        when (pendingDialog) {
                            PendingDialog.DIARY_LOADING_FAILURE -> showDiaryLoadingFailureDialog(date)
                        }
                        diaryShowViewModel.removePendingDialogListFirstItem()
                    }
                }
        }
    }

    // 画面表示データ準備
    private fun setUpDiaryData() {
        diaryShowViewModel.initialize()
        val diaryDate = DiaryShowFragmentArgs.fromBundle(requireArguments()).date

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryShowViewModel.loadSavedDiary(diaryDate, true)
            if (isSuccessful) return@launch

            withContext(Dispatchers.Main) {
                showDiaryLoadingFailureDialog(diaryDate)
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            setNavigationOnClickListener {
                backFragment(true)
            }
            setOnMenuItemClickListener { item: MenuItem ->
                // 日記編集フラグメント起動
                if (item.itemId == R.id.diaryShowToolbarOptionEditDiary) {
                    val editDiaryDate = diaryShowViewModel.date.requireValue()
                    showDiaryEdit(editDiaryDate)
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryShowToolbarOptionDeleteDiary) {
                    val deleteDiaryDate = diaryShowViewModel.date.requireValue()
                    showDiaryDeleteDialog(deleteDiaryDate)
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.date
                .collectLatest { value: LocalDate? ->
                    // MEMO:DiaryViewModelを初期化するとDiaryDateにnullが代入されるため、下記"return"を処理。
                    if (value == null) return@collectLatest

                    val converter = DateTimeStringConverter()
                    val stringDate = converter.toYearMonthDayWeek(value)
                    binding.materialToolbarTopAppBar.title = stringDate
                }
        }
    }

    // 天気表示欄設定
    private fun setUpWeatherLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.weather1
                .collectLatest { value: Weather ->
                    Weather1Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeather1Selected
                    ).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.weather2
                .collectLatest { value: Weather ->
                    Weather2Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeatherSlush,
                        binding.includeDiaryShow.textWeather2Selected
                    ).onChanged(value)
                }
        }
    }

    class Weather1Observer(private val context: Context, private val textWeather: TextView) {

        fun onChanged(value: Weather) {
            textWeather.text = value.toString(context)
        }
    }

    class Weather2Observer(
        private val context: Context,
        private val slush: TextView,
        private val textWeather: TextView
    ) {
        fun onChanged(value: Weather) {
            if (value == Weather.UNKNOWN) {
                slush.visibility = View.GONE
                textWeather.visibility = View.GONE
            } else {
                slush.visibility = View.VISIBLE
                textWeather.visibility = View.VISIBLE
            }
            textWeather.text = value.toString(context)
        }
    }

    private fun setUpConditionLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.condition
                .collectLatest { value: Condition ->
                    ConditionObserver(
                        requireContext(),
                        binding.includeDiaryShow.textConditionSelected
                    ).onChanged(value)
                }
        }
    }

    class ConditionObserver(private val context: Context, private val textCondition: TextView) {

        fun onChanged(value: Condition) {
            textCondition.text = value.toString(context)
        }
    }

    private fun setUpItemLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            val itemLayouts =
                binding.includeDiaryShow.run {
                    arrayOf(
                        includeItem1.linerLayoutDiaryShowItem,
                        includeItem2.linerLayoutDiaryShowItem,
                        includeItem3.linerLayoutDiaryShowItem,
                        includeItem4.linerLayoutDiaryShowItem,
                        includeItem5.linerLayoutDiaryShowItem
                    )
                }

            diaryShowViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    NumVisibleItemsObserver(itemLayouts).onChanged(value)
                }
        }
    }

    class NumVisibleItemsObserver(private val itemLayouts: Array<LinearLayout>) {

        fun onChanged(value: Int) {
            require(!(value < ItemNumber.MIN_NUMBER || value > ItemNumber.MAX_NUMBER))

            for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
                val itemArrayNumber = i - 1
                if (i <= value) {
                    itemLayouts[itemArrayNumber].visibility = View.VISIBLE
                } else {
                    itemLayouts[itemArrayNumber].visibility = View.GONE
                }
            }
        }
    }

    private fun setUpPicture() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver(
                        requireContext(),
                        themeColor,
                        binding.includeDiaryShow.textAttachedPicture,
                        binding.includeDiaryShow.imageAttachedPicture
                    ).onChanged(value)
                }
        }
    }

    class PicturePathObserver(
        context: Context,
        themeColor: ThemeColor,
        private val textPictureTitle: TextView,
        private val imageView: ImageView
    ) {

            private val diaryPictureManager: DiaryPictureManager =
                DiaryPictureManager(
                    context,
                    imageView,
                    themeColor.getOnSurfaceVariantColor(context.resources)
                )

        fun onChanged(value: Uri?) {
            if (value == null) {
                textPictureTitle.visibility = View.GONE
                imageView.visibility = View.GONE
                return
            }

            textPictureTitle.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            diaryPictureManager.setUpPictureOnDiary(value)
        }
    }

    private fun setUpLogLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.log
                .collectLatest { value: LocalDateTime? ->
                    LogObserver(binding.includeDiaryShow.textLogValue).onChanged(value)
                }
        }
    }

    class LogObserver(private val textLog: TextView) {

        fun onChanged(value: LocalDateTime?) {
            // MEMO:DiaryViewModelを初期化するとDiaryLogにnullが代入されるため、下記"return"を処理。
            if (value == null) return

            val dateTimeStringConverter = DateTimeStringConverter()
            val strDate = dateTimeStringConverter.toYearMonthDayWeekHourMinuteSeconds(value)
            textLog.text = strDate
        }
    }

    @MainThread
    private fun showDiaryEdit(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryShowFragmentDirections
                .actionNavigationDiaryShowFragmentToDiaryEditFragment(
                    false,
                    true,
                    date
                )
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryLoadingFailureDialog(date: LocalDate) {
        if (isDialogShowing) {
            diaryShowViewModel.addPendingDialogList(PendingDialog.DIARY_LOADING_FAILURE)
            return
        }

        val directions =
            DiaryShowFragmentDirections
                .actionDiaryShowFragmentToDiaryLoadingFailureDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryDeleteDialog(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryShowFragmentDirections
                .actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections
                .actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryShowViewModel.triggerAppMessageBufferListObserver()
    }

    // 一つ前のフラグメントを表示
    // MEMO:ツールバーの戻るボタンと端末の戻るボタンを区別している。
    //      ツールバーの戻るボタン:アプリ内でのみ戻る
    //      端末の戻るボタン:端末内で戻る(アプリ外から本アプリを起動した場合起動もとへ戻る)
    @MainThread
    private fun backFragment(requestsNavigateUp: Boolean) {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_calendar_fragment) {
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val showedDiaryLocalDate = diaryShowViewModel.date.value
            savedStateHandle[KEY_SHOWED_DIARY_DATE] = showedDiaryLocalDate
        }

        if (requestsNavigateUp) {
            navController.navigateUp()
        } else {
            navController.popBackStack()
        }
    }

    override fun destroyBinding() {
        _binding = null
    }
}
