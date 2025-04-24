package com.websarva.wings.android.zuboradiary.ui.fragment

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
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryPictureConfigurator
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialog
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryShowViewModel
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateTimeWithSecondsString
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
internal class DiaryShowFragment : BaseFragment() {

    internal companion object {
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
    override val mainViewModel: DiaryShowViewModel by viewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureUriPermissionManager =
            object : UriPermissionManager() {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean? {
                    return mainViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryShowBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryShowFragment.viewLifecycleOwner
            diaryShowViewModel = mainViewModel
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

    override fun receiveDialogResults() {
        receiveDiaryLoadingFailureDialogResult()
        receiveDiaryDeleteDialogResult()
    }

    override fun removeDialogResults() {
        removeResulFromFragment(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    // 日記読込失敗確認ダイアログフラグメントからデータ受取
    private fun receiveDiaryLoadingFailureDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingFailureDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        backFragment()
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = mainViewModel.deleteDiary()
            if (!isSuccessful) return@launch

            releasePictureUriPermission()
            withContext(Dispatchers.Main) {
                backFragment()
            }
        }
    }

    private fun releasePictureUriPermission() {
        val pictureUri = mainViewModel.picturePath.value ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            pictureUriPermissionManager.releasePersistablePermission(requireContext(), pictureUri)
        }
    }

    private fun setUpPendingDialogObserver() {
        pendingDialogNavigation = object : PendingDialogNavigation {
            override fun showPendingDialog(pendingDialog: PendingDialog): Boolean {
                if (pendingDialog !is DiaryShowPendingDialog) return false

                when (pendingDialog) {
                    is DiaryShowPendingDialog.DiaryLoadingFailure ->
                        showDiaryLoadingFailureDialog(pendingDialog.date)
                }
                return true
            }
        }
    }

    // 画面表示データ準備
    private fun setUpDiaryData() {
        mainViewModel.initialize()
        val diaryDate = DiaryShowFragmentArgs.fromBundle(requireArguments()).date

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = mainViewModel.loadSavedDiary(diaryDate, true)
            if (isSuccessful) return@launch

            withContext(Dispatchers.Main) {
                showDiaryLoadingFailureDialog(diaryDate)
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            setNavigationOnClickListener {
                backFragment()
            }
            setOnMenuItemClickListener { item: MenuItem ->
                // 日記編集フラグメント起動
                if (item.itemId == R.id.diaryShowToolbarOptionEditDiary) {
                    val editDiaryDate = mainViewModel.date.requireValue()
                    showDiaryEdit(editDiaryDate)
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryShowToolbarOptionDeleteDiary) {
                    val deleteDiaryDate = mainViewModel.date.requireValue()
                    showDiaryDeleteDialog(deleteDiaryDate)
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.date
                .collectLatest { value: LocalDate? ->
                    // MEMO:DiaryViewModelを初期化するとDiaryDateにnullが代入されるため、下記"return"を処理。
                    if (value == null) return@collectLatest

                    val dateString = value.toJapaneseDateString(requireContext())
                    binding.materialToolbarTopAppBar.title = dateString
                }
        }
    }

    // 天気表示欄設定
    private fun setUpWeatherLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1
                .collectLatest { value: Weather ->
                    Weather1Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeather1Selected
                    ).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: Weather ->
                    Weather2Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeatherSlush,
                        binding.includeDiaryShow.textWeather2Selected
                    ).onChanged(value)
                }
        }
    }

    internal class Weather1Observer(private val context: Context, private val textWeather: TextView) {

        fun onChanged(value: Weather) {
            textWeather.text = value.toString(context)
        }
    }

    internal class Weather2Observer(
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
            mainViewModel.condition
                .collectLatest { value: Condition ->
                    ConditionObserver(
                        requireContext(),
                        binding.includeDiaryShow.textConditionSelected
                    ).onChanged(value)
                }
        }
    }

    internal class ConditionObserver(private val context: Context, private val textCondition: TextView) {

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

            mainViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    NumVisibleItemsObserver(itemLayouts).onChanged(value)
                }
        }
    }

    internal class NumVisibleItemsObserver(private val itemLayouts: Array<LinearLayout>) {

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
            mainViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver(
                        themeColor,
                        binding.includeDiaryShow.textAttachedPicture,
                        binding.includeDiaryShow.imageAttachedPicture
                    ).onChanged(value)
                }
        }
    }

    internal class PicturePathObserver(
        private val themeColor: ThemeColor,
        private val textPictureTitle: TextView,
        private val imageView: ImageView
    ) {

        fun onChanged(value: Uri?) {
            if (value == null) {
                textPictureTitle.visibility = View.GONE
                imageView.visibility = View.GONE
                return
            }

            textPictureTitle.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            DiaryPictureConfigurator()
                .setUpPictureOnDiary(
                    imageView,
                    value,
                    themeColor
                )
        }
    }

    private fun setUpLogLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.log
                .collectLatest { value: LocalDateTime? ->
                    LogObserver(requireContext(), binding.includeDiaryShow.textLogValue)
                        .onChanged(value)
                }
        }
    }

    internal class LogObserver(private val context: Context ,private val textLog: TextView) {

        fun onChanged(value: LocalDateTime?) {
            // MEMO:DiaryViewModelを初期化するとDiaryLogにnullが代入されるため、下記"return"を処理。
            if (value == null) return

            val dateString = value.toJapaneseDateTimeWithSecondsString(context)
            textLog.text = dateString
        }
    }

    @MainThread
    private fun showDiaryEdit(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                false,
                true,
                date
            )
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryLoadingFailureDialog(date: LocalDate) {
        if (!canNavigateFragment) {
            mainViewModel.addPendingDialogList(DiaryShowPendingDialog.DiaryLoadingFailure(date))
            return
        }

        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryLoadingFailureDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryDeleteDialog(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    @MainThread
    private fun backFragment() {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_calendar_fragment) {
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val showedDiaryLocalDate = mainViewModel.date.value
            savedStateHandle[KEY_SHOWED_DIARY_DATE] = showedDiaryLocalDate
        }
        navController.navigateUp()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
