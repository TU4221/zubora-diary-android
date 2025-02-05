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
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
class DiaryShowFragment : BaseFragment() {

    companion object {
        // Navigation関係
        private val fromClassName = "From" + DiaryShowFragment::class.java.name
        val KEY_SHOWED_DIARY_DATE: String = "ShowedDiaryDate$fromClassName"
    }

    // View関係
    private var _binding: FragmentDiaryShowBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    private lateinit var diaryShowViewModel: DiaryShowViewModel

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
                override fun checkUsedUriDoesNotExist(uri: Uri): Boolean {
                    return diaryShowViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeViewModel() {
        val provider = ViewModelProvider(this)
        diaryShowViewModel = provider[DiaryShowViewModel::class.java]
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryShowBinding.inflate(themeColorInflater, container, false)
        binding.lifecycleOwner = this
        binding.diaryShowViewModel = diaryShowViewModel
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpDiaryData()
        setUpToolBar()
        setUpWeatherLayout()
        setUpConditionLayout()
        setUpItemLayout()
        setUpPicture()
        setUpLogLayout()
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        // 処理なし
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        receiveDiaryDeleteDialogResult()
        retryOtherAppMessageDialogShow()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        savedStateHandle.remove<Any>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    override fun setUpOtherAppMessageDialog() {
        diaryShowViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(diaryShowViewModel))
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        val isSuccessful = diaryShowViewModel.deleteDiary()
        if (!isSuccessful) return

        releasePictureUriPermission()
        backFragment(true)
    }

    private fun releasePictureUriPermission() {
        val pictureUri = diaryShowViewModel.picturePathLiveData.value ?: return

        pictureUriPermissionManager.releasePersistablePermission(pictureUri)
    }

    // 画面表示データ準備
    private fun setUpDiaryData() {
        diaryShowViewModel.initialize()
        val diaryDate = DiaryShowFragmentArgs.fromBundle(requireArguments()).date

        // 日記編集Fragmentで日記を削除して日記表示Fragmentに戻って来た時は更に一つ前のFragmentへ戻る。
        if (!diaryShowViewModel.existsSavedDiary(diaryDate)) {
            navController.navigateUp()
            return
        }

        diaryShowViewModel.loadSavedDiary(diaryDate)
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            setNavigationOnClickListener {
                backFragment(true)
            }
            setOnMenuItemClickListener { item: MenuItem ->
                // 日記編集フラグメント起動
                if (item.itemId == R.id.diaryShowToolbarOptionEditDiary) {
                    val editDiaryDate = diaryShowViewModel.dateLiveData.checkNotNull()
                    showDiaryEdit(editDiaryDate)
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryShowToolbarOptionDeleteDiary) {
                    val deleteDiaryDate = diaryShowViewModel.dateLiveData.checkNotNull()
                    showDiaryDeleteDialog(deleteDiaryDate)
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }

        diaryShowViewModel.dateLiveData
            .observe(viewLifecycleOwner) { date: LocalDate? ->
                // MEMO:DiaryViewModelを初期化するとDiaryDateにnullが代入されるため、下記"return"を処理。
                if (date == null) return@observe

                val converter = DateTimeStringConverter()
                val stringDate = converter.toYearMonthDayWeek(date)
                binding.materialToolbarTopAppBar.title = stringDate
            }
    }

    // 天気表示欄設定
    private fun setUpWeatherLayout() {
        diaryShowViewModel.apply {
            weather1LiveData.observe(
                viewLifecycleOwner,
                Weather1Observer(
                    requireContext(),
                    binding.includeDiaryShow.textWeather1Selected
                )
            )
            weather2LiveData.observe(
                viewLifecycleOwner,
                Weather2Observer(
                    requireContext(),
                    binding.includeDiaryShow.textWeatherSlush,
                    binding.includeDiaryShow.textWeather2Selected
                )
            )
        }
    }

    class Weather1Observer(private val context: Context, private val textWeather: TextView)
        : Observer<Weather> {

        override fun onChanged(value: Weather) {
            textWeather.text = value.toString(context)
        }
    }

    class Weather2Observer(
        private val context: Context,
        private val slush: TextView,
        private val textWeather: TextView
    ) : Observer<Weather> {
        override fun onChanged(value: Weather) {
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
        diaryShowViewModel.conditionLiveData
            .observe(
                viewLifecycleOwner,
                ConditionObserver(
                    requireContext(),
                    binding.includeDiaryShow.textConditionSelected
                )
            )
    }

    class ConditionObserver(private val context: Context, private val textCondition: TextView)
        : Observer<Condition> {

        override fun onChanged(value: Condition) {
            textCondition.text = value.toString(context)
        }
    }

    private fun setUpItemLayout() {
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
        diaryShowViewModel.numVisibleItemsLiveData
            .observe(viewLifecycleOwner, NumVisibleItemsObserver(itemLayouts))
    }

    class NumVisibleItemsObserver(private val itemLayouts: Array<LinearLayout>) :
        Observer<Int> {

        override fun onChanged(value: Int) {
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
        diaryShowViewModel.picturePathLiveData
            .observe(
                viewLifecycleOwner,
                PicturePathObserver(
                    requireContext(),
                    requireThemeColor(),
                    binding.includeDiaryShow.textAttachedPicture,
                    binding.includeDiaryShow.imageAttachedPicture
                )
            )
    }

    class PicturePathObserver(
        context: Context,
        themeColor: ThemeColor,
        private val textPictureTitle: TextView,
        private val imageView: ImageView
    ) : Observer<Uri?> {

            private val diaryPictureManager: DiaryPictureManager =
                DiaryPictureManager(
                    context,
                    imageView,
                    themeColor.getOnSurfaceVariantColor(context.resources)
                )

        override fun onChanged(value: Uri?) {
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
        diaryShowViewModel.logLiveData
            .observe(
                viewLifecycleOwner,
                LogObserver(binding.includeDiaryShow.textLogValue)
            )
    }

    class LogObserver(private val textLog: TextView) : Observer<LocalDateTime?> {

        override fun onChanged(value: LocalDateTime?) {
            // MEMO:DiaryViewModelを初期化するとDiaryLogにnullが代入されるため、下記"return"を処理。
            if (value == null) return

            val dateTimeStringConverter = DateTimeStringConverter()
            val strDate = dateTimeStringConverter.toYearMonthDayWeekHourMinuteSeconds(value)
            textLog.text = strDate
        }
    }

    private fun showDiaryEdit(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryShowFragmentDirections
                .actionNavigationDiaryShowFragmentToDiaryEditFragment(
                    false,
                    true,
                    date
                )
        navController.navigate(action)
    }

    private fun showDiaryDeleteDialog(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navController.navigate(action)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryShowViewModel.triggerAppMessageBufferListObserver()
    }

    // 一つ前のフラグメントを表示
    // MEMO:ツールバーの戻るボタンと端末の戻るボタンを区別している。
    //      ツールバーの戻るボタン:アプリ内でのみ戻る
    //      端末の戻るボタン:端末内で戻る(アプリ外から本アプリを起動した場合起動もとへ戻る)
    private fun backFragment(requestsNavigateUp: Boolean) {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_calendar_fragment) {
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val showedDiaryLocalDate = diaryShowViewModel.dateLiveData.value
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
