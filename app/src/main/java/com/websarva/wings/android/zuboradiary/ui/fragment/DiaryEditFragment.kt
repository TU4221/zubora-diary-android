package com.websarva.wings.android.zuboradiary.ui.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditPendingDialog
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryPictureConfigurator
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialog
import com.websarva.wings.android.zuboradiary.ui.view.edittext.TextInputConfigurator
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryStateFlow
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DatePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryEditViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryItemDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryPictureDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryUpdateDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.WeatherInfoFetchingDialogFragment
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.action.DiaryEditFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.annotations.Unmodifiable
import java.time.LocalDate

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment() {

    internal companion object {
        // Navigation関係
        private val fromClassName = "From" + DiaryEditFragment::class.java.name
        val KEY_EDITED_DIARY_DATE: String = "EditedDiaryDate$fromClassName"
    }

    private val logTag = createLogTag()

    // View関係
    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val motionLayoutTransitionTime = 500 /*ms*/
    private val scrollTimeMotionLayoutTransition = 1000 /*ms*/

    private var shouldTransitionItemMotionLayout = false

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryEditViewModel by activityViewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    private val screenHeight: Int
        get() {
            val windowManager =
                requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screenHeight: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bounds = windowManager.currentWindowMetrics.bounds
                screenHeight = bounds.height()
            } else {
                screenHeight = resources.displayMetrics.heightPixels
            }
            return screenHeight
        }

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
        _binding = FragmentDiaryEditBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryEditFragment.viewLifecycleOwner
            diaryEditViewModel = mainViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelInitialization()
        setUpOnBackPressedCallback()
        setUpFragmentAction()
        setUpPendingDialogObserver()
        setUpFocusViewScroll()
        setUpDiaryData()
        setUpToolBar()
        setUpDateInputField()
        setUpWeatherInputField()
        setUpConditionInputField()
        setUpTitleInputField()
        setUpItemInputField()
        setUpPictureInputField()
        setupEditText()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // DiaryItemTitleEditFragmentから編集結果受取

        val newItemTitle =
            receiveResulFromPreviousFragment<String>(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)

        launchAndRepeatOnViewLifeCycleStarted {
            newItemTitle.collectLatest { value: String? ->
                // MEMO:結果がない場合もあるので"return"で返す。
                if (value == null) return@collectLatest

                // TODO:newItemTitleとitemNumberをひとつにしたデータクラスを用意する
                val itemNumber =
                    checkNotNull(
                        receiveResulFromPreviousFragment<ItemNumber>(
                            DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER
                        ).value
                    )

                mainViewModel.onReceivedFromItemTitleEditFragment(itemNumber, value)

                val focusTargetView =
                    when (itemNumber.value) {
                        1 -> binding.includeItem1.textInputEditTextTitle
                        2 -> binding.includeItem2.textInputEditTextTitle
                        3 -> binding.includeItem3.textInputEditTextTitle
                        4 -> binding.includeItem4.textInputEditTextTitle
                        5 -> binding.includeItem5.textInputEditTextTitle
                        else -> throw IllegalStateException()
                    }
               focusTargetView.requestFocus()

                removeResulFromFragment(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)
                removeResulFromFragment(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER)
            }
        }
    }

    override fun receiveDialogResults() {
        receiveDiaryLoadingDialogResult()
        receiveDiaryLoadingFailureDialogResult()
        receiveDiaryUpdateDialogResult()
        receiveDiaryDeleteDialogResult()
        receiveDatePickerDialogResult()
        receiveWeatherInfoFetchDialogResult()
        receiveDiaryItemDeleteDialogResult()
        receiveDiaryPictureDeleteDialogResult()
    }

    override fun removeDialogResults() {
        removeResulFromFragment(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DatePickerDialogFragment.KEY_SELECTED_DATE)
        removeResulFromFragment(WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER)
        removeResulFromFragment(DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private fun receiveDiaryLoadingDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON) ?: return

        // TODO:ViewModelにStateFlow変数を持たせる。
        val isCheckedWeatherInfoAcquisition =
            settingsViewModel.isCheckedWeatherInfoAcquisition.requireValue()
        val geoCoordinates =
            settingsViewModel.geoCoordinates.value
        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            mainViewModel
                .onDiaryLoadingDialogPositiveButtonClicked(
                    isCheckedWeatherInfoAcquisition,
                    geoCoordinates
                )
        } else {
            mainViewModel
                .onDiaryLoadingDialogNegativeButtonClicked(
                    isCheckedWeatherInfoAcquisition,
                    geoCoordinates
                )
        }
    }

    // 日記読込失敗確認ダイアログフラグメントから結果受取
    private fun receiveDiaryLoadingFailureDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingFailureDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        mainViewModel.onDiaryLoadingFailureDialogPositiveButtonClicked()
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryUpdateDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        mainViewModel.onDiaryUpdateDialogPositiveButtonClicked()
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        mainViewModel.onDiaryDeleteDialogPositiveButtonClicked()
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private fun receiveDatePickerDialogResult() {
        val selectedDate =
            receiveResulFromDialog<LocalDate>(DatePickerDialogFragment.KEY_SELECTED_DATE) ?: return

        val isCheckedWeatherInfoAcquisition =
            settingsViewModel.isCheckedWeatherInfoAcquisition.requireValue()
        val geoCoordinates = settingsViewModel.geoCoordinates.value
        mainViewModel
            .onDatePickerDialogPositiveButtonClicked(
                selectedDate,
                isCheckedWeatherInfoAcquisition,
                geoCoordinates
            )
    }

    private fun receiveWeatherInfoFetchDialogResult() {
        // 天気情報読込ダイアログフラグメントから結果受取
        val selectedButton =
            receiveResulFromDialog<Int>(
                WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON
            ) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        val geoCoordinates = settingsViewModel.geoCoordinates.requireValue()
        mainViewModel.onWeatherInfoFetchDialogPositiveButtonClicked(geoCoordinates)
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private fun receiveDiaryItemDeleteDialogResult() {
        val deleteItemNumber =
            receiveResulFromDialog<ItemNumber>(
                DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER
            ) ?: return

        shouldTransitionItemMotionLayout = true
        mainViewModel.onDiaryItemDeleteDialogPositiveButtonClicked(deleteItemNumber)
    }

    private fun receiveDiaryPictureDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(
                DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON
            ) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        mainViewModel.onDiaryPictureDeleteDialogPositiveButtonClicked()
    }

    private fun setUpViewModelInitialization() {
        navController.addOnDestinationChangedListener(ViewModelInitializationSetupListener())
    }

    private inner class ViewModelInitializationSetupListener
        : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            // MEMO:本Fragment、Dialog、DiaryItemTitleEditFragment以外のFragmentへ切り替わった時のみViewModelを初期化する。
            if (destination.id == R.id.navigation_diary_edit_fragment
                || destination.id == R.id.navigation_date_picker_dialog
                || destination.id == R.id.navigation_diary_delete_dialog_for_diary_edit_fragment
                || destination.id == R.id.navigation_diary_item_delete_dialog
                || destination.id == R.id.navigation_diary_loading_dialog
                || destination.id == R.id.navigation_diary_picture_delete_dialog
                || destination.id == R.id.navigation_diary_update_dialog
                || destination.id == R.id.navigation_weather_info_fetching_dialog) return

            if (destination.id != R.id.navigation_diary_item_title_edit_fragment) {
                mainViewModel.shouldInitializeOnFragmentDestroy = true
            }

            navController.removeOnDestinationChangedListener(this)
        }
    }

    private fun setUpOnBackPressedCallback() {
        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainViewModel.onBackPressed()
            }

        })
    }

    private fun setUpFragmentAction() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.fragmentAction.collectLatest { value: FragmentAction ->
                when (value) {
                    is DiaryEditFragmentAction.NavigateDiaryShowFragment -> {
                        pictureUriPermissionManager
                            .handlePersistablePermission(requireContext(), value.uriPermissionAction)
                        navigateDiaryShowFragment(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryItemTitleEditFragment -> {
                        navigateDiaryItemTitleEditFragment(value.itemNumber, value.itemTitle)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryLoadingDialog -> {
                        navigateDiaryLoadingDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryLoadingFailureDialog -> {
                        navigateDiaryLoadingFailureDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryUpdateDialog -> {
                        navigateDiaryUpdateDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryDeleteDialog -> {
                        navigateDiaryDeleteDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDatePickerDialog -> {
                        navigateDatePickerDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateWeatherInfoFetchingDialog -> {
                        navigateWeatherInfoFetchingDialog(value.date)
                    }
                    is DiaryEditFragmentAction.NavigateDiaryItemDeleteDialog -> {
                        navigateDiaryItemDeleteDialog(value.itemNumber)
                    }
                    DiaryEditFragmentAction.NavigateDiaryPictureDeleteDialog -> {
                        navigateDiaryPictureDeleteDialog()
                    }
                    is DiaryEditFragmentAction.NavigatePreviousFragment -> {
                        navigatePreviousFragment(value.loadedDate)
                    }
                    is DiaryEditFragmentAction.NavigatePreviousFragmentOnDiaryDelete -> {
                        if (value.uri != null) {
                            pictureUriPermissionManager
                                .releasePersistablePermission(requireContext(), value.uri)
                        }
                        navigatePreviousFragmentOnDiaryDelete(value.loadedDate)
                    }
                    is DiaryEditFragmentAction.HideDiaryItem -> {
                        hideItem(value.itemNumber, value.isJump)
                    }
                    FragmentAction.None -> {
                        // 処理なし
                    }
                    else -> {
                        throw IllegalArgumentException()
                    }
                }
                mainViewModel.clearFragmentAction()
            }
        }
    }

    private fun setUpPendingDialogObserver() {
        pendingDialogNavigation = object : PendingDialogNavigation {
            override fun navigatePendingDialog(pendingDialog: PendingDialog): Boolean {
                if (pendingDialog !is DiaryEditPendingDialog) return false

                when (pendingDialog) {
                    is DiaryEditPendingDialog.DiaryLoading ->
                        navigateDiaryLoadingDialog(pendingDialog.date)
                    is DiaryEditPendingDialog.DiaryLoadingFailure ->
                        navigateDiaryLoadingFailureDialog(pendingDialog.date)
                    is DiaryEditPendingDialog.WeatherInfoFetching ->
                        navigateWeatherInfoFetchingDialog(pendingDialog.date)
                }
                return true
            }
        }
    }

    private fun setUpFocusViewScroll() {
        KeyboardManager().registerKeyBoredStateListener(this) { isShowed ->
            if (!isShowed) return@registerKeyBoredStateListener
            require(isSoftInputAdjustNothing())

            val focusView =
                this@DiaryEditFragment.view?.findFocus() ?: return@registerKeyBoredStateListener

            val offset = screenHeight / 3
            val location = IntArray(2)
            focusView.getLocationOnScreen(location)
            val positionY = location[1]
            val scrollAmount = positionY - offset

            binding.nestedScrollFullScreen.smoothScrollBy(0, scrollAmount)
        }
    }

    // MEMO:キーボード表示時、ActivityのLayoutが変更されない設定であるかを確認。
    private fun isSoftInputAdjustNothing(): Boolean {
        val softInputAdjust =
            requireActivity().window.attributes.softInputMode and
                    WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        return softInputAdjust == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
    }

    private fun setUpDiaryData() {
        // 画面表示データ準備
        // TODO:DiaryItemTitleEditをDialogに変更したら下記コード不要になる
        if (mainViewModel.hasPreparedDiary) return

        val diaryDate = DiaryEditFragmentArgs.fromBundle(requireArguments()).date
        val requiresDiaryLoading =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).shouldLoadDiary
        val isCheckedWeatherInfoAcquisition =
            settingsViewModel.isCheckedWeatherInfoAcquisition.requireValue()
        val geoCoordinates =
            settingsViewModel.geoCoordinates.value
        mainViewModel
            .prepareDiary(
                diaryDate,
                requiresDiaryLoading,
                isCheckedWeatherInfoAcquisition,
                geoCoordinates
            )
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                mainViewModel.onNavigationClicked()
            }

        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // 日記保存、削除
                when (item.itemId) {
                    R.id.diaryEditToolbarOptionSaveDiary -> {
                        mainViewModel.onDiarySaveMenuClicked()
                        return@setOnMenuItemClickListener true
                    }

                    R.id.diaryEditToolbarOptionDeleteDiary -> {
                        mainViewModel.onDiaryDeleteMenuClicked()
                        return@setOnMenuItemClickListener true
                    }

                    R.id.diaryEditToolbarOptionTest -> {
                        mainViewModel.test()
                        return@setOnMenuItemClickListener true
                    }
                }
                false
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.loadedDate
                .collectLatest { value: LocalDate? ->
                    val title: String
                    val enabledDelete: Boolean
                    if (value == null) {
                        title = getString(R.string.fragment_diary_edit_toolbar_title_create_new)
                        enabledDelete = false
                    } else {
                        title = getString(R.string.fragment_diary_edit_toolbar_title_edit)
                        enabledDelete = true
                    }
                    binding.materialToolbarTopAppBar.title = title

                    val menu = binding.materialToolbarTopAppBar.menu
                    val deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary)
                    deleteMenuItem.setEnabled(enabledDelete)

                    // TODO:テスト用の為、最終的に削除
                    val testMenuItem = menu.findItem(R.id.diaryEditToolbarOptionTest)
                    testMenuItem.setEnabled(!enabledDelete)
                }
        }
    }

    // 日付入力欄設定
    private fun setUpDateInputField() {
        binding.textInputEditTextDate.apply {
            inputType = EditorInfo.TYPE_NULL //キーボード非表示設定
            setOnClickListener {
                mainViewModel.onDateInputFieldClicked()
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.date
                .collectLatest { value: LocalDate? ->
                    if (value == null) return@collectLatest
                    if (mainViewModel.isTesting) return@collectLatest

                    val dateString = value.toJapaneseDateString(requireContext())
                    binding.textInputEditTextDate.setText(dateString)
                }
        }
    }

    // 天気入力欄。
    private fun setUpWeatherInputField() {
        binding.autoCompleteTextWeather1.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextWeather1.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strWeather = checkNotNull(arrayAdapter.getItem(position)) as String
                val weather = Weather.of(requireContext(), strWeather)
                mainViewModel.onWeather1InputFieldItemClicked(weather)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1
                .collectLatest { value: Weather ->
                    Log.d("20250428", "Weather collectLatest()")
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather1.setText(strWeather, false)

                    // Weather2 Spinner有効無効切替
                    val isEnabled = (value != Weather.UNKNOWN)
                    binding.textInputLayoutWeather2.isEnabled = isEnabled
                    binding.autoCompleteTextWeather2.isEnabled = isEnabled

                    mainViewModel.onWeather1Changed()
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1AdapterList.collectLatest { value: WeatherAdapterList ->
                val arrayAdapter = createWeatherSpinnerAdapter(value)
                binding.autoCompleteTextWeather1.setAdapter(arrayAdapter)
            }
        }

        binding.autoCompleteTextWeather2.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextWeather2.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strWeather = checkNotNull(arrayAdapter.getItem(position)) as String
                val weather = Weather.of(requireContext(), strWeather)
                mainViewModel.onWeather2InputFieldItemClicked(weather)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: Weather ->
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather2.setText(strWeather, false)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2AdapterList.collectLatest { value: WeatherAdapterList ->
                val arrayAdapter = createWeatherSpinnerAdapter(value)
                binding.autoCompleteTextWeather2.setAdapter(arrayAdapter)
            }
        }
    }

    private fun createWeatherSpinnerAdapter(list: WeatherAdapterList): ArrayAdapter<String> {
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireContext(), themeResId)
        val stringList = list.toStringList(requireContext())

        return ArrayAdapter(
            contextWithTheme,
            R.layout.layout_drop_down_list_item,
            stringList
        )
    }

    // 気分入力欄。
    private fun setUpConditionInputField() {
        binding.autoCompleteTextCondition.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextCondition.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strCondition = arrayAdapter.getItem(position) as String
                val condition = Condition.of(requireContext(), strCondition)
                mainViewModel.onConditionInputFieldItemClicked(condition)
            }


        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.condition
                .collectLatest { value: Condition ->
                    val strCondition = value.toString(requireContext())
                    binding.autoCompleteTextCondition.setText(strCondition, false)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.conditionAdapterList.collectLatest { value: ConditionAdapterList ->
                val arrayAdapter = createConditionSpinnerAdapter(value)
                binding.autoCompleteTextCondition.setAdapter(arrayAdapter)
            }
        }
    }

    private fun createConditionSpinnerAdapter(list: ConditionAdapterList): ArrayAdapter<String> {
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireContext(), themeResId)
        val stringList = list.toStringList(requireContext())

        return ArrayAdapter(
            contextWithTheme,
            R.layout.layout_drop_down_list_item,
            stringList)
    }

    @Suppress("EmptyMethod")
    private fun setUpTitleInputField() {
        // 処理なし
    }

    private fun setUpItemInputField() {
        // 項目入力欄関係Viewを配列に格納
        val textInputEditTextItemsTitle =
            binding.run {
                arrayOf(
                    includeItem1.textInputEditTextTitle,
                    includeItem2.textInputEditTextTitle,
                    includeItem3.textInputEditTextTitle,
                    includeItem4.textInputEditTextTitle,
                    includeItem5.textInputEditTextTitle,
                )
            }

        val imageButtonItemsDelete =
            binding.run {
                arrayOf(
                    includeItem1.imageButtonItemDelete,
                    includeItem2.imageButtonItemDelete,
                    includeItem3.imageButtonItemDelete,
                    includeItem4.imageButtonItemDelete,
                    includeItem5.imageButtonItemDelete,
                )
            }

        // 項目欄設定
        // 項目タイトル入力欄設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val inputItemNumber = ItemNumber(i)
            val itemArrayNumber = i - 1
            textInputEditTextItemsTitle[itemArrayNumber].inputType = EditorInfo.TYPE_NULL //キーボード非表示設定

            textInputEditTextItemsTitle[itemArrayNumber].setOnClickListener {
                mainViewModel.onItemTitleInputFieldClicked(inputItemNumber)
            }
        }

        // 項目追加ボタン設定
        binding.imageButtonItemAddition.setOnClickListener {
            binding.imageButtonItemAddition.isEnabled = false
            shouldTransitionItemMotionLayout = true
            mainViewModel.onItemAdditionButtonClicked()
        }

        // 項目削除ボタン設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val deleteItemNumber = ItemNumber(i)
            val itemArrayNumber = i - 1
            imageButtonItemsDelete[itemArrayNumber].setOnClickListener {
                mainViewModel.onItemDeleteButtonClicked(deleteItemNumber)
            }
        }

        // 項目欄MotionLayout設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val itemNumber = ItemNumber(i)
            val itemMotionLayout = selectItemMotionLayout(itemNumber)
            itemMotionLayout.setTransitionListener(ItemMotionLayoutListener(itemNumber))
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    NumVisibleItemsObserver().onChanged(value)
                }
        }
    }

    private inner class ItemMotionLayoutListener(
        val itemNumber: ItemNumber
    ): MotionLayout.TransitionListener {

        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            Log.d(logTag, "onTransitionStarted()_itemNumber = $itemNumber")
        }

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float
        ) {
            Log.d(
                logTag,
                "onTransitionChange()_itemNumber = $itemNumber, progress = $progress"
            )
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            Log.d(logTag, "onTransitionCompleted()_itemNumber = $itemNumber")

            // 対象項目欄削除後の処理
            var completedStateLogMsg = "UnknownState"
            if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                completedStateLogMsg = "HidedState"
                if (shouldTransitionItemMotionLayout) {
                    if (isNextItemHidedState()) scrollOnDiaryItemHided()
                    mainViewModel.onDiaryItemHidedStateTransitionCompleted(itemNumber)
                }

            // 対象項目欄追加後の処理
            } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                completedStateLogMsg = "ShowedState"
                if (shouldTransitionItemMotionLayout) {
                    scrollOnDiaryItemShowed()
                }
            }
            Log.d(logTag, "onTransitionCompleted()_CompletedState = $completedStateLogMsg")

            shouldTransitionItemMotionLayout = false
        }

        private fun isNextItemHidedState(): Boolean {
            val nextItemNumber = itemNumber.inc()
            val motionLayout = selectItemMotionLayout(nextItemNumber)
            return motionLayout.currentState == R.id.motion_scene_edit_diary_item_hided_state
        }

        // 対象項目追加後のスクロール処理
        private fun scrollOnDiaryItemShowed() {
            scrollOnDiaryItemTransition(true)
        }

        // 対象項目削除後のスクロール処理
        private fun scrollOnDiaryItemHided() {
            scrollOnDiaryItemTransition(false)
        }

        private fun scrollOnDiaryItemTransition(isUpDirection: Boolean) {
            val itemHeight = binding.includeItem1.linerLayoutDiaryEditItem.height
            val scrollY =
                if (isUpDirection) {
                    itemHeight
                } else {
                    -itemHeight
                }
            binding.nestedScrollFullScreen
                .smoothScrollBy(0, scrollY, scrollTimeMotionLayoutTransition)
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float
        ) {
            // 処理なし
        }

    }

    private fun selectItemMotionLayout(itemNumber: ItemNumber): MotionLayout {
        return when (itemNumber.value) {
            1 -> binding.includeItem1.motionLayoutDiaryEditItem
            2 -> binding.includeItem2.motionLayoutDiaryEditItem
            3 -> binding.includeItem3.motionLayoutDiaryEditItem
            4 -> binding.includeItem4.motionLayoutDiaryEditItem
            5 -> binding.includeItem5.motionLayoutDiaryEditItem
            else -> throw IllegalArgumentException()
        }
    }

    private inner class NumVisibleItemsObserver {
        fun onChanged(value: Int) {
            enableItemAdditionButton(value < DiaryStateFlow.MAX_ITEMS)
            setUpItemsLayout(value)
        }

        private fun enableItemAdditionButton(enabled: Boolean) {
            binding.imageButtonItemAddition.isEnabled = enabled
            val alphaResId = if (enabled) {
                R.dimen.view_enabled_alpha
            } else {
                R.dimen.view_disabled_alpha
            }
            val alpha = ResourcesCompat.getFloat(resources, alphaResId)
            binding.imageButtonItemAddition.alpha = alpha
        }

        private fun setUpItemsLayout(numItems: Int) {
            require(!(numItems < ItemNumber.MIN_NUMBER || numItems > ItemNumber.MAX_NUMBER))

            // MEMO:削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
            if (shouldTransitionItemMotionLayout) {
                val numShowedItems = countShowedItems()
                val differenceValue = numItems - numShowedItems
                if (numItems > numShowedItems && differenceValue == 1) {
                    showItem(ItemNumber(numItems), false)
                    return
                }
            }

            for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
                val itemNumber = ItemNumber(i)
                if (itemNumber.value <= numItems) {
                    showItem(itemNumber, true)
                } else {
                    hideItem(itemNumber, true)
                }
            }
        }
    }

    private fun hideItem(itemNumber: ItemNumber, isJump: Boolean) {
        val itemMotionLayout = selectItemMotionLayout(itemNumber)
        if (isJump) {
            itemMotionLayout
                .jumpToState(R.id.motion_scene_edit_diary_item_hided_state)
        } else {
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_hided_state,
                    motionLayoutTransitionTime
                )
        }
    }

    private fun showItem(itemNumber: ItemNumber, isJump: Boolean) {
        val itemMotionLayout = selectItemMotionLayout(itemNumber)
        if (isJump) {
            itemMotionLayout
                .jumpToState(R.id.motion_scene_edit_diary_item_showed_state)
        } else {
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_showed_state,
                    motionLayoutTransitionTime
                )
        }
    }

    private fun countShowedItems(): Int {
        var numShowedItems = 0
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val itemNumber = ItemNumber(i)
            val motionLayout = selectItemMotionLayout(itemNumber)
            if (motionLayout.currentState != R.id.motion_scene_edit_diary_item_showed_state) {
                continue
            }
            numShowedItems++
        }
        return numShowedItems
    }

    private fun setUpPictureInputField() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver().onChanged(value)
                }
        }

        binding.apply {
            imageAttachedPicture.setOnClickListener {
                mainActivity.loadPicturePath()
            }
            imageButtonAttachedPictureDelete.setOnClickListener {
                mainViewModel.onAttachedPictureDeleteButtonClicked()
            }
        }
    }

    private inner class PicturePathObserver {
        fun onChanged(value: Uri?) {
            DiaryPictureConfigurator()
                .setUpPictureOnDiary(
                    binding.imageAttachedPicture,
                    value,
                    themeColor
                )
            enablePictureDeleteButton(value != null)
        }

        private fun enablePictureDeleteButton(enabled: Boolean) {
            binding.imageButtonAttachedPictureDelete.apply {
                isEnabled = enabled
                val alphaResId = if (enabled) {
                    R.dimen.view_enabled_alpha
                } else {
                    R.dimen.view_disabled_alpha
                }
                val alphaValue = ResourcesCompat.getFloat(resources, alphaResId)
                alpha = alphaValue
            }
        }
    }

    private fun setupEditText() {
        val textInputConfigurator = TextInputConfigurator()

        val allTextInputLayouts = createAllTextInputLayoutList().toTypedArray<TextInputLayout>()
        textInputConfigurator.setUpFocusClearOnClickBackground(
            binding.viewNestedScrollBackground,
            *allTextInputLayouts
        )

        textInputConfigurator.setUpKeyboardCloseOnEnter(binding.textInputLayoutTitle)

        val scrollableTextInputLayouts =
            binding.run {
                arrayOf(
                    includeItem1.textInputLayoutComment,
                    includeItem2.textInputLayoutComment,
                    includeItem3.textInputLayoutComment,
                    includeItem4.textInputLayoutComment,
                    includeItem5.textInputLayoutComment,
                )
            }

        textInputConfigurator.setUpScrollable(*scrollableTextInputLayouts)

        val clearableTextInputLayouts =
            binding.run {
                arrayOf(
                    textInputLayoutTitle,
                    includeItem1.textInputLayoutTitle,
                    includeItem2.textInputLayoutTitle,
                    includeItem3.textInputLayoutTitle,
                    includeItem4.textInputLayoutTitle,
                    includeItem5.textInputLayoutTitle,
                )
            }
        val transitionListener =
            textInputConfigurator.createClearButtonSetupTransitionListener(*clearableTextInputLayouts)
        addTransitionListener(transitionListener)
    }

    private fun createAllTextInputLayoutList(): @Unmodifiable List<TextInputLayout> {
        return binding.run {
            listOf(
                textInputLayoutDate,
                textInputLayoutWeather1,
                textInputLayoutWeather2,
                textInputLayoutCondition,
                textInputLayoutTitle,
                includeItem1.textInputLayoutTitle,
                includeItem1.textInputLayoutComment,
                includeItem2.textInputLayoutTitle,
                includeItem2.textInputLayoutComment,
                includeItem3.textInputLayoutTitle,
                includeItem3.textInputLayoutComment,
                includeItem4.textInputLayoutTitle,
                includeItem4.textInputLayoutComment,
                includeItem5.textInputLayoutTitle,
                includeItem5.textInputLayoutComment
            )
        }
    }



    @MainThread
    private fun navigateDiaryShowFragment(date: LocalDate) {
        if (!canNavigateFragment) return

        val isStartDiaryFragment =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).isStartDiaryFragment
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるdirectionsを切り替える。
        val directions = if (isStartDiaryFragment) {
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryShowFragmentPattern2(date)
        } else {
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryShowFragmentPattern1(date)
        }
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryItemTitleEditFragment(
        inputItemNumber: ItemNumber,
        inputItemTitle: String
    ) {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToSelectItemTitleFragment(
                inputItemNumber,
                inputItemTitle
            )
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryLoadingDialog(date: LocalDate) {
        if (!canNavigateFragment) {
            mainViewModel.addPendingDialogList(DiaryEditPendingDialog.DiaryLoading(date))
            return
        }

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadingDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryLoadingFailureDialog(date: LocalDate) {
        if (!canNavigateFragment) {
            mainViewModel.addPendingDialogList(DiaryEditPendingDialog.DiaryLoadingFailure(date))
            return
        }

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadingFailureDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryUpdateDialog(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryUpdateDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryDeleteDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDatePickerDialog(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateWeatherInfoFetchingDialog(date: LocalDate) {
        if (!canNavigateFragment) {
            mainViewModel.addPendingDialogList(DiaryEditPendingDialog.WeatherInfoFetching(date))
            return
        }

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToWeatherInfoFetchingDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryItemDeleteDialog(itemNumber: ItemNumber) {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber)
        navController.navigate(directions)
    }

    @MainThread
    private fun navigateDiaryPictureDeleteDialog() {
        if (!canNavigateFragment) return

        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryPictureDeleteDialog()
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    @MainThread
    private fun navigatePreviousFragment(editedDiaryDate: LocalDate?) {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_calendar_fragment) {
            val savedStateHandle = navBackStackEntry.savedStateHandle
            savedStateHandle[KEY_EDITED_DIARY_DATE] = editedDiaryDate
        }
        navController.navigateUp()
    }

    @MainThread
    private fun navigatePreviousFragmentOnDiaryDelete(editedDiaryDate: LocalDate?) {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_diary_show_fragment) {
            navController.navigateUp()
        }
        navigatePreviousFragment(editedDiaryDate)
    }

    override fun destroyBinding() {
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // MEMO:DiaryEditViewModelのスコープ範囲はActivityになるが、
        //      DiaryEditFragment、DiaryItemTitleEditFragment表示時のみViewModelのプロパティ値を保持できたらよいので、
        //      DiaryEditFragmentを破棄するタイミングでViewModelのプロパティ値を初期化する。
        mainViewModel.apply {
            if (shouldInitializeOnFragmentDestroy) initialize()
        }
    }

    internal fun attachPicture(uri: Uri) {
        mainViewModel.updatePicturePath(uri)
    }
}
