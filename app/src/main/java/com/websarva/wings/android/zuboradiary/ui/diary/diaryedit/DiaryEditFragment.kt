package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.annotation.MainThread
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager
import com.websarva.wings.android.zuboradiary.ui.TestDiariesSaver
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.requireValue
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryStateFlow
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Unmodifiable
import java.time.LocalDate
import java.util.Arrays

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment() {

    companion object {
        // Navigation関係
        private val fromClassName = "From" + DiaryEditFragment::class.java.name
        val KEY_EDITED_DIARY_DATE: String = "EditedDiaryDate$fromClassName"
    }

    private val logTag = createLogTag()

    // View関係
    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var isDeletingItemTransition = false
    private lateinit var weather2ArrayAdapter: ArrayAdapter<String>

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    private val diaryEditViewModel: DiaryEditViewModel by activityViewModels()

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureUriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean? {
                    return diaryEditViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryEditBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryEditFragment.viewLifecycleOwner
            diaryEditViewModel = this@DiaryEditFragment.diaryEditViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelInitialization()
        setUpPendingDialogObserver()
        setUpDiaryData()
        setUpToolBar()
        setUpDateInputField()
        setUpWeatherInputField()
        setUpConditionInputField()
        setUpTitleInputField()
        setUpItemInputField()
        setUpPictureInputField()
        setupEditText()

        // TODO:最終的に削除
        binding.fabTest.setOnClickListener {
            val testDiariesSaver = TestDiariesSaver(diaryEditViewModel)
            lifecycleScope.launch(Dispatchers.IO) {
                testDiariesSaver.save(28)
            }
        }
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // DiaryItemTitleEditFragmentから編集結果受取

        val newItemTitle =
            receiveResulFromPreviousFragment<String>(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)

        launchAndRepeatOnViewLifeCycleStarted {
            newItemTitle.collectLatest { value: String? ->
                // MEMO:結果がない場合もあるので"return"で返す。
                if (value == null) return@collectLatest

                val itemNumber =
                    checkNotNull(
                        receiveResulFromPreviousFragment<ItemNumber>(
                            DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER
                        ).value
                    )

                diaryEditViewModel.updateItemTitle(itemNumber, value)

                removeResulFromFragment(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)
                removeResulFromFragment(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER)
            }
        }
    }

    override fun handleOnReceivingDialogResult() {
        receiveDiaryLoadingDialogResult()
        receiveDiaryLoadingFailureDialogResult()
        receiveDiaryUpdateDialogResult()
        receiveDiaryDeleteDialogResult()
        receiveDatePickerDialogResult()
        receiveWeatherInfoFetchDialogResult()
        receiveDiaryItemDeleteDialogResult()
        receiveDiaryPictureDeleteDialogResult()
        clearFocusAllEditText()
    }

    override fun removeDialogResultOnDestroy() {
        removeResulFromFragment(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DatePickerDialogFragment.KEY_SELECTED_DATE)
        removeResulFromFragment(WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON)
        removeResulFromFragment(DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER)
        removeResulFromFragment(DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    override fun setUpOtherAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(diaryEditViewModel).onChanged(value)
                }
        }
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private fun receiveDiaryLoadingDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON) ?: return

        val date = diaryEditViewModel.date.requireValue()

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            lifecycleScope.launch(Dispatchers.IO) {
                diaryEditViewModel.prepareDiary(date, true)
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                DateObserver().fetchWeatherInfo(date)
            }
        }
    }

    // 日記読込失敗確認ダイアログフラグメントから結果受取
    private fun receiveDiaryLoadingFailureDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingFailureDialogFragment.KEY_SELECTED_BUTTON)
                ?: return
        if (selectedButton != Dialog.BUTTON_POSITIVE) return

        backFragment()
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryUpdateDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryEditViewModel.saveDiary()
            if (!isSuccessful) return@launch

            updatePictureUriPermission()
            val date = diaryEditViewModel.date.requireValue()
            withContext(Dispatchers.Main) {
                showDiaryShowFragment(date)
            }
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryEditViewModel.deleteDiary()
            if (!isSuccessful) return@launch

            releaseLoadedPictureUriPermission()
            withContext(Dispatchers.Main) {
                val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                val destinationId = navBackStackEntry.destination.id
                if (destinationId == R.id.navigation_diary_show_fragment) {
                    navController.navigateUp()
                }
                backFragment()
            }
        }

    }

    // 日付入力ダイアログフラグメントからデータ受取
    private fun receiveDatePickerDialogResult() {
        val selectedDate =
            receiveResulFromDialog<LocalDate>(DatePickerDialogFragment.KEY_SELECTED_DATE) ?: return

        diaryEditViewModel.updateDate(selectedDate)
    }

    private fun receiveWeatherInfoFetchDialogResult() {
        // 天気情報読込ダイアログフラグメントから結果受取
        val selectedButton =
            receiveResulFromDialog<Int>(
                WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON
            ) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        val loadDiaryDate = diaryEditViewModel.date.requireValue()
        val geoCoordinates = settingsViewModel.geoCoordinates.requireValue()
        lifecycleScope.launch(Dispatchers.IO) {
            diaryEditViewModel.fetchWeatherInformation(loadDiaryDate, geoCoordinates)
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private fun receiveDiaryItemDeleteDialogResult() {
        val deleteItemNumber =
            receiveResulFromDialog<ItemNumber>(
                DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER
            ) ?: return

        val numVisibleItems = diaryEditViewModel.numVisibleItems.value

        if (deleteItemNumber.value == 1 && numVisibleItems == deleteItemNumber.value) {
            diaryEditViewModel.deleteItem(deleteItemNumber)
        } else {
            isDeletingItemTransition = true
            hideItem(deleteItemNumber, false)
        }
    }

    private fun receiveDiaryPictureDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(
                DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON
            ) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        diaryEditViewModel.deletePicturePath()
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
                diaryEditViewModel.shouldInitializeOnFragmentDestroy = true
            }

            navController.removeOnDestinationChangedListener(this)
        }
    }

    private fun setUpPendingDialogObserver() {
        addNavBackStackEntryLifecycleObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                diaryEditViewModel.triggerPendingDialogListObserver()
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.pendingDialogList
                .collectLatest { value: PendingDialogList ->
                    val pendingDialog = value.findFirstItem() ?: return@collectLatest
                    val date = diaryEditViewModel.date.requireValue()
                    withContext(Dispatchers.Main) {
                        when (pendingDialog) {
                            PendingDialog.DIARY_LOADING -> showDiaryLoadingDialog(date)
                            PendingDialog.DIARY_LOADING_FAILURE -> showDiaryLoadingFailureDialog(date)
                            PendingDialog.WEATHER_INFO_FETCHING -> showWeatherInfoFetchingDialog(date)
                        }
                        diaryEditViewModel.removePendingDialogListFirstItem()
                    }
                }
        }
    }

    private fun setUpDiaryData() {
        // 画面表示データ準備
        if (diaryEditViewModel.hasPreparedDiary) return

        val diaryDate = DiaryEditFragmentArgs.fromBundle(requireArguments()).date
        val requiresDiaryLoading =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).requiresDiaryLoading
        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful =
                diaryEditViewModel.prepareDiary(diaryDate, requiresDiaryLoading, true)
            if (isSuccessful) return@launch

            withContext(Dispatchers.Main) {
                showDiaryLoadingFailureDialog(diaryDate)
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                backFragment()
            }

        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                val diaryDate = diaryEditViewModel.date.requireValue()

                //日記保存(日記表示フラグメント起動)。
                if (item.itemId == R.id.diaryEditToolbarOptionSaveDiary) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val shouldShowDialog =
                            diaryEditViewModel.shouldShowUpdateConfirmationDialog() ?: return@launch
                        if (shouldShowDialog) {
                            withContext(Dispatchers.Main) {
                                showDiaryUpdateDialog(diaryDate)
                            }
                        } else {
                            val isSuccessful = diaryEditViewModel.saveDiary()
                            if (isSuccessful) {
                                updatePictureUriPermission()
                                withContext(Dispatchers.Main) {
                                    showDiaryShowFragment(diaryDate)
                                }
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryEditToolbarOptionDeleteDiary) {
                    showDiaryDeleteDialog(diaryDate)
                    return@setOnMenuItemClickListener true
                }
                false
            }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.loadedDate
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
                }
        }
    }

    // 日付入力欄設定
    private fun setUpDateInputField() {
        binding.textInputEditTextDate.apply {
            inputType = EditorInfo.TYPE_NULL //キーボード非表示設定
            setOnClickListener {
                val date = diaryEditViewModel.date.requireValue()
                showDatePickerDialog(date)
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.date
                .collectLatest { value: LocalDate? ->
                    DateObserver().onChanged(value)
                }
        }
    }

    private inner class DateObserver {
        fun onChanged(value: LocalDate?) {
            if (value == null) return
            if (diaryEditViewModel.isShowingItemTitleEditFragment) return

            val dateTimeStringConverter = DateTimeStringConverter()
            val dateString = dateTimeStringConverter.toYearMonthDayWeek(value)
            binding.textInputEditTextDate.setText(dateString)

            lifecycleScope.launch(Dispatchers.IO) {
                val shouldShowDialog =
                    shouldShowDiaryLoadingDialog(value) ?: return@launch
                if (shouldShowDialog) {
                    withContext(Dispatchers.Main) {
                        showDiaryLoadingDialog(value)
                    }
                } else {
                    // 読込確認Dialog表示時は、確認後下記処理を行う。
                    fetchWeatherInfo(value)
                }
            }
        }

        private suspend fun shouldShowDiaryLoadingDialog(changedDate: LocalDate): Boolean? {
            if (diaryEditViewModel.isNewDiaryDefaultStatus) {
                return diaryEditViewModel.existsSavedDiary(changedDate)
            }

            val previousDate = diaryEditViewModel.previousDate.value
            val loadedDate = diaryEditViewModel.loadedDate.value

            if (changedDate == previousDate) return false
            if (changedDate == loadedDate) return false
            return diaryEditViewModel.existsSavedDiary(changedDate)
        }

        suspend fun fetchWeatherInfo(changedDate: LocalDate) {
            if (requiresWeatherInfoFetching(changedDate)) {
                fetchWeatherInfo(
                    changedDate,
                    requiresShowingWeatherInfoFetchingDialog())
            }
        }

        private fun requiresWeatherInfoFetching(date: LocalDate): Boolean {
            val previousDate = diaryEditViewModel.previousDate.value
            if (!diaryEditViewModel.isNewDiary && previousDate == null) return false
            return previousDate != date
        }

        private fun requiresShowingWeatherInfoFetchingDialog(): Boolean {
            val previousDate = diaryEditViewModel.previousDate.value
            return previousDate != null
        }
    }

    // 天気入力欄。
    private fun setUpWeatherInputField() {
        val weatherArrayAdapter = createWeatherSpinnerAdapter()
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter)
        weather2ArrayAdapter = createWeatherSpinnerAdapter()
        binding.autoCompleteTextWeather2.setAdapter(weather2ArrayAdapter)

        binding.autoCompleteTextWeather1.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextWeather1.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strWeather = checkNotNull(arrayAdapter.getItem(position)) as String
                val weather = Weather.of(requireContext(), strWeather)
                diaryEditViewModel.updateWeather1(weather)
                binding.autoCompleteTextWeather1.clearFocus()
            }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.weather1
                .collectLatest { value: Weather ->
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather1.setText(strWeather, false)

                    // Weather2 Spinner有効無効切替
                    val isEnabled = (value != Weather.UNKNOWN)
                    binding.textInputLayoutWeather2.isEnabled = isEnabled
                    binding.autoCompleteTextWeather2.isEnabled = isEnabled
                    if (value == Weather.UNKNOWN || diaryEditViewModel.isEqualWeathers) {
                        binding.autoCompleteTextWeather2.setAdapter(
                            weatherArrayAdapter
                        )
                        diaryEditViewModel.updateWeather2(Weather.UNKNOWN)
                    } else {
                        weather2ArrayAdapter = createWeatherSpinnerAdapter(value)
                        binding.autoCompleteTextWeather2.setAdapter(
                            weather2ArrayAdapter
                        )
                    }
                }
        }

        binding.autoCompleteTextWeather2.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextWeather2.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strWeather = checkNotNull(arrayAdapter.getItem(position)) as String
                val weather = Weather.of(requireContext(), strWeather)
                diaryEditViewModel.updateWeather2(weather)
                binding.autoCompleteTextWeather2.clearFocus()
            }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.weather2
                .collectLatest { value: Weather ->
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather2.setText(strWeather, false)
                }
        }
    }

    private fun createWeatherSpinnerAdapter(vararg excludedWeathers: Weather?): ArrayAdapter<String> {
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireContext(), themeResId)

        val weatherItemList: MutableList<String> = ArrayList()
        Arrays.stream(Weather.entries.toTypedArray()).forEach { x: Weather ->
            val isIncluded = !isExcludedWeather(x, *excludedWeathers)
            if (isIncluded) weatherItemList.add(x.toString(requireContext()))
        }

        return ArrayAdapter(contextWithTheme, R.layout.layout_drop_down_list_item, weatherItemList)
    }

    private fun isExcludedWeather(weather: Weather, vararg excludedWeathers: Weather?): Boolean {
        for (excludedWeather in excludedWeathers) {
            if (weather == excludedWeather) return true
        }
        return false
    }

    // 気分入力欄。
    private fun setUpConditionInputField() {
        // ドロップダウン設定
        val conditionArrayAdapter = createConditionSpinnerAdapter()
        binding.autoCompleteTextCondition.apply {
            setAdapter(conditionArrayAdapter)
            onItemClickListener =
                OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    val listAdapter = adapter
                    val arrayAdapter = listAdapter as ArrayAdapter<*>
                    val strCondition = arrayAdapter.getItem(position) as String
                    val condition = Condition.of(requireContext(), strCondition)
                    diaryEditViewModel.updateCondition(condition)
                    clearFocus()
                }
        }


        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.condition
                .collectLatest { value: Condition ->
                    val strCondition = value.toString(requireContext())
                    binding.autoCompleteTextCondition.setText(strCondition, false)
                }
        }
    }

    private fun createConditionSpinnerAdapter(): ArrayAdapter<String> {
        val themeResId = themeColor.themeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireContext(), themeResId)

        val conditionItemList: MutableList<String> = ArrayList()
        Arrays.stream(Condition.entries.toTypedArray())
            .forEach { x: Condition -> conditionItemList.add(x.toString(requireContext())) }

        return ArrayAdapter(contextWithTheme, R.layout.layout_drop_down_list_item, conditionItemList)
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
                // 項目タイトル入力フラグメント起動
                val inputItemTitle =
                    diaryEditViewModel.getItemTitle(inputItemNumber).value
                showDiaryItemTitleEditFragment(inputItemNumber, inputItemTitle)
            }
        }

        // 項目追加ボタン設定
        binding.imageButtonItemAddition.setOnClickListener {
            binding.imageButtonItemAddition.isEnabled = false
            diaryEditViewModel.incrementVisibleItemsCount()
        }

        // 項目削除ボタン設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val deleteItemNumber = ItemNumber(i)
            val itemArrayNumber = i - 1
            imageButtonItemsDelete[itemArrayNumber].setOnClickListener {
                showDiaryItemDeleteDialog(deleteItemNumber)
            }
        }

        // 項目欄MotionLayout設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val itemNumber = ItemNumber(i)
            val itemMotionLayout = selectItemMotionLayout(itemNumber)
            itemMotionLayout.setTransitionListener(ItemMotionLayoutListener(itemNumber))
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryEditViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    NumVisibleItemsObserver().onChanged(value)
                }
        }
    }

    private inner class ItemMotionLayoutListener(
        val itemNumber: ItemNumber
    ): MotionLayout.TransitionListener {

        val initializeValue = -1
        var startScrollPosition = initializeValue
        var goalStateId = initializeValue

        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            Log.d(logTag, "onTransitionStarted()_itemNumber = $itemNumber")

            setUpScroll(motionLayout)
        }

        // MEMO:startId,endIdはMotionSceneファイル(.xml)で記述したIdが代入される。
        //      また、motionLayout.currentStateは指定先のIdを取得する。
        private fun setUpScroll(motionLayout: MotionLayout?) {
            goalStateId = motionLayout?.currentState ?: return
            startScrollPosition = binding.nestedScrollFullScreen.scrollY
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

            if (diaryEditViewModel.shouldJumpItemMotionLayout) return

            scrollOnTransition(progress)
        }

        private fun scrollOnTransition(progress: Float) {
            val itemHeight = binding.includeItem1.linerLayoutDiaryEditItem.height
            val scrollY =
                when (goalStateId) {
                    R.id.motion_scene_edit_diary_item_hided_state -> {
                        // MEMO:アイテム削除時は
                        if (itemNumber.value == diaryEditViewModel.numVisibleItems.value) {
                            startScrollPosition -
                                    (itemHeight * progress).toInt()
                        } else {
                            startScrollPosition
                        }

                    }
                    R.id.motion_scene_edit_diary_item_showed_state -> {
                        startScrollPosition +
                                (itemHeight * (1 - progress)).toInt()
                    }
                    else -> startScrollPosition
                }
            binding.nestedScrollFullScreen.smoothScrollTo(0, scrollY)
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            Log.d(logTag, "onTransitionCompleted()_itemNumber = $itemNumber")
            // 対象項目欄削除後の処理
            var completedStateLogMsg = "UnknownState"
            if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                completedStateLogMsg = "HidedState"
                deleteItemContents()

            // 対象項目欄追加後の処理
            } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                completedStateLogMsg = "ShowedState"
            }
            Log.d(logTag, "onTransitionCompleted()_CompletedState = $completedStateLogMsg")

            initializeProperty()
            diaryEditViewModel.shouldJumpItemMotionLayout = false
        }

        private fun deleteItemContents() {
            if (isDeletingItemTransition) {
                diaryEditViewModel.deleteItem(itemNumber)
                isDeletingItemTransition = false
            }
        }

        private fun initializeProperty() {
            startScrollPosition = initializeValue
            goalStateId = initializeValue
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
            //
            if (!diaryEditViewModel.shouldJumpItemMotionLayout) {
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
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_hided_state)
        }
    }

    private fun showItem(itemNumber: ItemNumber, isJump: Boolean) {
        val itemMotionLayout = selectItemMotionLayout(itemNumber)
        if (isJump) {
            itemMotionLayout
                .jumpToState(R.id.motion_scene_edit_diary_item_showed_state)
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_showed_state)
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
            diaryEditViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver().onChanged(value)
                }
        }

        binding.apply {
            imageAttachedPicture.setOnClickListener {
                mainActivity.loadPicturePath()
            }
            imageButtonAttachedPictureDelete.setOnClickListener {
                showDiaryPictureDeleteDialog()
            }
        }

    }

    private inner class PicturePathObserver {
        fun onChanged(value: Uri?) {
            val diaryPictureManager =
                DiaryPictureManager(
                    requireContext(),
                    binding.imageAttachedPicture,
                    themeColor.getOnSurfaceVariantColor(requireContext().resources)
                )

            diaryPictureManager.setUpPictureOnDiary(value)
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

    private fun updatePictureUriPermission() {
        val latestPictureUri = diaryEditViewModel.picturePath.value
        val loadedPictureUri = diaryEditViewModel.loadedPicturePath.value

        lifecycleScope.launch(Dispatchers.IO) {
            pictureUriPermissionManager.apply {
                if (latestPictureUri == null && loadedPictureUri == null) return@launch
                if (latestPictureUri != null && loadedPictureUri == null) {
                    takePersistablePermission(latestPictureUri)
                    return@launch
                }
                if (latestPictureUri == null) {
                    releasePersistablePermission(checkNotNull(loadedPictureUri))
                    return@launch
                }
                if (latestPictureUri == loadedPictureUri) return@launch
                takePersistablePermission(latestPictureUri)
                releasePersistablePermission(checkNotNull(loadedPictureUri))
            }
        }
    }

    private fun releaseLoadedPictureUriPermission() {
        val loadedPictureUri = diaryEditViewModel.loadedPicturePath.value ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            pictureUriPermissionManager.releasePersistablePermission(loadedPictureUri)
        }
    }

    private fun setupEditText() {
        val textInputSetup = TextInputSetup(requireActivity())

        val allTextInputLayouts = createAllTextInputLayoutList().toTypedArray<TextInputLayout>()
        textInputSetup.setUpFocusClearOnClickBackground(
            binding.viewNestedScrollBackground,
            *allTextInputLayouts
        )

        textInputSetup.setUpKeyboardCloseOnEnter(binding.textInputLayoutTitle)

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

        textInputSetup.setUpScrollable(*scrollableTextInputLayouts)

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
            textInputSetup.createClearButtonSetupTransitionListener(*clearableTextInputLayouts)
        addTransitionListener(transitionListener)
    }

    private fun clearFocusAllEditText() {
        val textInputLayoutList = createAllTextInputLayoutList()
        textInputLayoutList.stream().forEach { x: TextInputLayout ->
            val editText = checkNotNull(x.editText)
            editText.clearFocus()
        }
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

    private suspend fun fetchWeatherInfo(date: LocalDate, requestsShowingDialog: Boolean) {
        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        val isChecked = settingsViewModel.isCheckedWeatherInfoAcquisition.requireValue()
        if (!isChecked) return

        if (!settingsViewModel.hasUpdatedGeoCoordinates) {
            diaryEditViewModel.addWeatherInfoFetchErrorMessage()
            return
        }

        // 本フラグメント起動時のみダイアログなしで天気情報取得
        if (requestsShowingDialog) {
            withContext(Dispatchers.Main) {
                showWeatherInfoFetchingDialog(date)
            }
        } else {
            val geoCoordinates = settingsViewModel.geoCoordinates.requireValue()
            diaryEditViewModel.fetchWeatherInformation(date, geoCoordinates)
        }
    }

    @MainThread
    private fun showDiaryShowFragment(date: LocalDate) {
        if (isDialogShowing) return

        val isStartDiaryFragment =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).isStartDiaryFragment
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるdirectionsを切り替える。
        val directions = if (isStartDiaryFragment) {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPattern2(date)
        } else {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPattern1(date)
        }
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryItemTitleEditFragment(
        inputItemNumber: ItemNumber,
        inputItemTitle: String
    ) {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToSelectItemTitleFragment(inputItemNumber, inputItemTitle)
        navController.navigate(directions)
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(true)
    }

    @MainThread
    private fun showDiaryLoadingDialog(date: LocalDate) {
        if (isDialogShowing) {
            diaryEditViewModel.addPendingDialogList(PendingDialog.DIARY_LOADING)
            return
        }

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryLoadingDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryLoadingFailureDialog(date: LocalDate) {
        if (isDialogShowing) {
            diaryEditViewModel.addPendingDialogList(PendingDialog.DIARY_LOADING_FAILURE)
            return
        }

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryLoadingFailureDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryUpdateDialog(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryUpdateDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryDeleteDialog(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryDeleteDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDatePickerDialog(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDatePickerDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showWeatherInfoFetchingDialog(date: LocalDate) {
        if (!diaryEditViewModel.canFetchWeatherInformation(date)) return
        if (isDialogShowing) {
            diaryEditViewModel.addPendingDialogList(PendingDialog.WEATHER_INFO_FETCHING)
            return
        }

        // 今日の日付以降は天気情報を取得できないためダイアログ表示不要
        diaryEditViewModel.canFetchWeatherInformation(date)

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchingDialog(date)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryItemDeleteDialog(itemNumber: ItemNumber) {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber)
        navController.navigate(directions)
    }

    @MainThread
    private fun showDiaryPictureDeleteDialog() {
        if (isDialogShowing) return

        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryPictureDeleteDialog()
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryEditViewModel.triggerAppMessageBufferListObserver()
    }

    @MainThread
    private fun backFragment() {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val destinationId = navBackStackEntry.destination.id
        if (destinationId == R.id.navigation_calendar_fragment) {
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val editedDiaryLocalDate = diaryEditViewModel.loadedDate.value
            savedStateHandle[KEY_EDITED_DIARY_DATE] = editedDiaryLocalDate
        }
        navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()

        // HACK:DiaryItemTitleEditFragmentから本Fragmentへ画面遷移(戻る)した時、
        //      スピナーのアダプターが選択中アイテムのみで構成されたアダプターに更新されてしまうので
        //      onResume()メソッドにて再度アダプターを設定して対策。
        //      (Weather2はWeather1のObserver内で設定している為不要)
        binding.apply {
            val weatherArrayAdapter = createWeatherSpinnerAdapter()
            autoCompleteTextWeather1.setAdapter(weatherArrayAdapter)
            val conditionArrayAdapter = createConditionSpinnerAdapter()
            autoCompleteTextCondition.setAdapter(conditionArrayAdapter)
        }

        // HACK:ItemTitleEditFragmentから戻ってきた時に処理させたく箇所を
        //      変数(DiaryEditViewModel.IsShowingItemTitleEditFragment)で分岐させる。
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(false)
    }

    override fun destroyBinding() {
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // MEMO:DiaryEditViewModelのスコープ範囲はActivityになるが、
        //      DiaryEditFragment、DiaryItemTitleEditFragment表示時のみViewModelのプロパティ値を保持できたらよいので、
        //      DiaryEditFragmentを破棄するタイミングでViewModelのプロパティ値を初期化する。
        diaryEditViewModel.apply {
            if (shouldInitializeOnFragmentDestroy) initialize()
        }
    }

    fun attachPicture(uri: Uri) {
        diaryEditViewModel.updatePicturePath(uri)
    }
}
