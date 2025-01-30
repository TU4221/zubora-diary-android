package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

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
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager
import com.websarva.wings.android.zuboradiary.ui.TestDiariesSaver
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment
import com.websarva.wings.android.zuboradiary.ui.notNullValue
import dagger.hilt.android.AndroidEntryPoint
import org.jetbrains.annotations.Unmodifiable
import java.time.LocalDate
import java.util.Arrays

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment() {
    // View関係
    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var isDeletingItemTransition = false
    private lateinit var weather2ArrayAdapter: ArrayAdapter<String>

    // ViewModel
    private lateinit var diaryEditViewModel: DiaryEditViewModel

    // Uri関係
    private lateinit var pictureUriPermissionManager: UriPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureUriPermissionManager =
            object : UriPermissionManager(requireContext()) {
                override fun checkUsedUriDoesNotExist(uri: Uri): Boolean {
                    return diaryEditViewModel.checkSavedPicturePathDoesNotExist(uri)
                }
            }
    }

    override fun initializeViewModel() {
        val provider = ViewModelProvider(requireActivity())
        diaryEditViewModel = provider[DiaryEditViewModel::class.java]
        diaryEditViewModel.initialize()
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryEditBinding.inflate(themeColorInflater, container, false)
        binding.lifecycleOwner = this
        binding.diaryEditViewModel = diaryEditViewModel
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            Log.d("20240823", "OnClick")
            val testDiariesSaver = TestDiariesSaver(diaryEditViewModel)
            testDiariesSaver.save(28)
        }
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        // DiaryItemTitleEditFragmentから編集結果受取

        val newItemTitleLiveData =
            savedStateHandle.getLiveData<String>(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)
        newItemTitleLiveData.observe(viewLifecycleOwner) { string: String? ->
            // MEMO:結果がない場合もあるので"return"で返す。
            if (string == null) return@observe

            val itemNumber =
                checkNotNull(
                    savedStateHandle.get<ItemNumber>(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER)
                )

            diaryEditViewModel.updateItemTitle(itemNumber, string)

            savedStateHandle.remove<Any>(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER)
            savedStateHandle.remove<Any>(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE)
        }
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        receiveDiaryLoadingDialogResult()
        receiveDiaryUpdateDialogResult()
        receiveDiaryDeleteDialogResult()
        receiveDatePickerDialogResult()
        receiveWeatherInfoFetchDialogResult()
        receiveDiaryItemDeleteDialogResult()
        receiveDiaryPictureDeleteDialogResult()
        retryOtherAppMessageDialogShow()
        clearFocusAllEditText()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        savedStateHandle.remove<Any>(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON)
        savedStateHandle.remove<Any>(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON)
        savedStateHandle.remove<Any>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON)
        savedStateHandle.remove<Any>(DatePickerDialogFragment.KEY_SELECTED_DATE)
        savedStateHandle.remove<Any>(WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON)
        savedStateHandle.remove<Any>(DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER)
        savedStateHandle.remove<Any>(DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON)
    }

    override fun setUpOtherAppMessageDialog() {
        diaryEditViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(diaryEditViewModel))
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private fun receiveDiaryLoadingDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON) ?: return

        val date = checkNotNull(diaryEditViewModel.date.value)

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            diaryEditViewModel.initialize()
            diaryEditViewModel.prepareDiary(date, true)
        } else {
            if (!diaryEditViewModel.isNewDiaryDefaultStatus) {
                fetchWeatherInfo(date, true)
            }
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryUpdateDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        val isSuccessful = diaryEditViewModel.saveDiary()
        if (!isSuccessful) return

        updatePictureUriPermission()
        val date = diaryEditViewModel.date.checkNotNull()
        showDiaryShowFragment(date)
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun receiveDiaryDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON) ?: return
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return

        val isSuccessful = diaryEditViewModel.deleteDiary()
        if (!isSuccessful) return

        releaseLoadedPictureUriPermission()
        navController.navigateUp()
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

        val loadDiaryDate = diaryEditViewModel.date.checkNotNull()
        val geoCoordinates = settingsViewModel.geoCoordinatesLiveData.checkNotNull()
        diaryEditViewModel.fetchWeatherInformation(loadDiaryDate, geoCoordinates)
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private fun receiveDiaryItemDeleteDialogResult() {
        val deleteItemNumber =
            receiveResulFromDialog<ItemNumber>(
                DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER
            ) ?: return

        val numVisibleItems = diaryEditViewModel.numVisibleItems.notNullValue()

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

    private fun setUpDiaryData() {
        // 画面表示データ準備
        if (diaryEditViewModel.hasPreparedDiary) return

        val diaryDate = DiaryEditFragmentArgs.fromBundle(requireArguments()).date
        val requiresDiaryLoading =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).requiresDiaryLoading
        diaryEditViewModel.prepareDiary(diaryDate, requiresDiaryLoading)
        if (!requiresDiaryLoading) fetchWeatherInfo(diaryDate, false)
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navController.navigateUp()
            }

        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                val diaryDate = diaryEditViewModel.date.checkNotNull()

                //日記保存(日記表示フラグメント起動)。
                if (item.itemId == R.id.diaryEditToolbarOptionSaveDiary) {
                    if (diaryEditViewModel.shouldShowUpdateConfirmationDialog) {
                        showDiaryUpdateDialog(diaryDate)
                    } else {
                        val isSuccessful = diaryEditViewModel.saveDiary()
                        if (isSuccessful) {
                            updatePictureUriPermission()
                            showDiaryShowFragment(diaryDate)
                        }
                    }
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryEditToolbarOptionDeleteDiary) {
                    showDiaryDeleteDialog(diaryDate)
                }
                false
            }

        diaryEditViewModel.loadedDate
            .observe(viewLifecycleOwner) { date: LocalDate? ->
                val title: String
                val enabledDelete: Boolean
                if (date == null) {
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

    // 日付入力欄設定
    private fun setUpDateInputField() {
        binding.textInputEditTextDate.inputType = EditorInfo.TYPE_NULL //キーボード非表示設定

        binding.textInputEditTextDate.setOnClickListener {
            val date = diaryEditViewModel.date.checkNotNull()
            showDatePickerDialog(date)
        }

        diaryEditViewModel.date.observe(viewLifecycleOwner, DateObserver())
    }

    private inner class DateObserver : Observer<LocalDate?> {
        override fun onChanged(value: LocalDate?) {
            if (value == null) return
            if (diaryEditViewModel.isShowingItemTitleEditFragment) return

            val dateTimeStringConverter = DateTimeStringConverter()
            binding.textInputEditTextDate.setText(dateTimeStringConverter.toYearMonthDayWeek(value))
            Log.d("DiaryEditInputDate", "currentDate:$value")
            val loadedDate = diaryEditViewModel.loadedDate.value
            Log.d("DiaryEditInputDate", "loadedDate:$loadedDate")
            val previousDate = diaryEditViewModel.previousDate.value
            Log.d("DiaryEditInputDate", "previousDate:$previousDate")
            if (requiresDiaryLoadingDialogShow(value)) {
                showDiaryLoadingDialog(value)
            } else {
                // 読込確認Dialog表示時は、確認後下記処理を行う。
                if (requiresWeatherInfoFetching(value)) {
                    fetchWeatherInfo(value, true)
                }
            }
        }

        fun requiresDiaryLoadingDialogShow(changedDate: LocalDate): Boolean {
            if (diaryEditViewModel.isNewDiaryDefaultStatus) return diaryEditViewModel.existsSavedDiary(
                changedDate
            )

            val previousDate = diaryEditViewModel.previousDate.value
            val loadedDate = diaryEditViewModel.loadedDate.value

            if (changedDate == previousDate) return false
            if (changedDate == loadedDate) return false
            return diaryEditViewModel.existsSavedDiary(changedDate)
        }

        fun requiresWeatherInfoFetching(date: LocalDate): Boolean {
            val previousDate = diaryEditViewModel.previousDate.value ?: return false
            return date != previousDate
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

        diaryEditViewModel.weather1
            .observe(viewLifecycleOwner) { weather: Weather ->
                val strWeather = weather.toString(requireContext())
                binding.autoCompleteTextWeather1.setText(strWeather, false)

                // Weather2 Spinner有効無効切替
                val isEnabled = (weather != Weather.UNKNOWN)
                binding.textInputLayoutWeather2.isEnabled = isEnabled
                binding.autoCompleteTextWeather2.isEnabled = isEnabled
                if (weather == Weather.UNKNOWN || diaryEditViewModel.isEqualWeathers) {
                    binding.autoCompleteTextWeather2.setAdapter(
                        weatherArrayAdapter
                    )
                    diaryEditViewModel.updateWeather2(Weather.UNKNOWN)
                } else {
                    weather2ArrayAdapter = createWeatherSpinnerAdapter(weather)
                    binding.autoCompleteTextWeather2.setAdapter(
                        weather2ArrayAdapter
                    )
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

        diaryEditViewModel.weather2
            .observe(viewLifecycleOwner) { weather: Weather ->
                val strWeather = weather.toString(requireContext())
                binding.autoCompleteTextWeather2.setText(strWeather, false)
            }
    }

    private fun createWeatherSpinnerAdapter(vararg excludedWeathers: Weather?): ArrayAdapter<String> {
        val themeResId = requireThemeColor().themeResId
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
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter)
        binding.autoCompleteTextCondition.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val listAdapter = binding.autoCompleteTextCondition.adapter
                val arrayAdapter = listAdapter as ArrayAdapter<*>
                val strCondition = arrayAdapter.getItem(position) as String?
                val condition = Condition.of(requireContext(), strCondition)
                diaryEditViewModel.updateCondition(condition)
                binding.autoCompleteTextCondition.clearFocus()
            }

        diaryEditViewModel.condition
            .observe(viewLifecycleOwner) { condition: Condition ->
                val strCondition = condition.toString(requireContext())
                binding.autoCompleteTextCondition.setText(strCondition, false)
            }
    }

    private fun createConditionSpinnerAdapter(): ArrayAdapter<String> {
        val themeResId = requireThemeColor().themeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireContext(), themeResId)

        val conditionItemList: MutableList<String> = ArrayList()
        Arrays.stream(Condition.entries.toTypedArray())
            .forEach { x: Condition -> conditionItemList.add(x.toString(requireContext())) }

        return ArrayAdapter(contextWithTheme, R.layout.layout_drop_down_list_item, conditionItemList)
    }

    private fun setUpTitleInputField() {
        // 処理なし
    }

    private fun setUpItemInputField() {
        // 項目入力欄関係Viewを配列に格納
        val textInputEditTextItemsTitle =
            arrayOf(
                binding.includeItem1.textInputEditTextTitle,
                binding.includeItem2.textInputEditTextTitle,
                binding.includeItem3.textInputEditTextTitle,
                binding.includeItem4.textInputEditTextTitle,
                binding.includeItem5.textInputEditTextTitle,
            )
        val imageButtonItemsDelete = arrayOf(
            binding.includeItem1.imageButtonItemDelete,
            binding.includeItem2.imageButtonItemDelete,
            binding.includeItem3.imageButtonItemDelete,
            binding.includeItem4.imageButtonItemDelete,
            binding.includeItem5.imageButtonItemDelete,
        )

        // 項目欄設定
        // 項目タイトル入力欄設定
        for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
            val inputItemNumber = ItemNumber(i)
            val itemArrayNumber = i - 1
            textInputEditTextItemsTitle[itemArrayNumber].inputType = EditorInfo.TYPE_NULL //キーボード非表示設定

            textInputEditTextItemsTitle[itemArrayNumber].setOnClickListener {
                // 項目タイトル入力フラグメント起動
                val inputItemTitle =
                    diaryEditViewModel.getItemTitleLiveData(inputItemNumber).notNullValue()
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
            itemMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout,
                    startId: Int,
                    endId: Int
                ) {
                    // 処理なし
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float
                ) {
                    // 処理なし
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                    Log.d(
                        "MotionLayout",
                        "ItemLiveData$itemNumber onTransitionCompleted"
                    )
                    // 対象項目欄削除後の処理
                    if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                        Log.d("MotionLayout", "currentId:hided_state")
                        if (isDeletingItemTransition) {
                            diaryEditViewModel.deleteItem(itemNumber)
                            isDeletingItemTransition = false
                        }

                        // 対象項目欄追加後の処理
                    } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                        Log.d("MotionLayout", "currentId:showed_state")
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                    // 処理なし
                }
            })
        }

        diaryEditViewModel.numVisibleItems
            .observe(viewLifecycleOwner, NumVisibleItemsObserver())
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

    private inner class NumVisibleItemsObserver : Observer<Int> {
        override fun onChanged(value: Int) {
            enableItemAdditionButton(value < DiaryLiveData.MAX_ITEMS)
            setUpItemsLayout(value)
        }

        fun enableItemAdditionButton(enabled: Boolean) {
            binding.imageButtonItemAddition.isEnabled = enabled
            val alphaResId = if (enabled) {
                R.dimen.view_enabled_alpha
            } else {
                R.dimen.view_disabled_alpha
            }
            val alpha = ResourcesCompat.getFloat(resources, alphaResId)
            binding.imageButtonItemAddition.alpha = alpha
        }

        fun setUpItemsLayout(numItems: Int) {
            require(!(numItems < ItemNumber.MIN_NUMBER || numItems > ItemNumber.MAX_NUMBER))

            // MEMO:LifeCycleがResumedの時のみ項目欄のモーション追加処理を行う。
            //      削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
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
            binding.nestedScrollFullScreen
                .smoothScrollBy(
                    0,
                    binding.includeItem1.linerLayoutDiaryEditItem.height,
                    1400
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
        binding.imageAttachedPicture.setOnClickListener {
            requireMainActivity().loadPicturePath()
        }

        diaryEditViewModel.picturePath
            .observe(viewLifecycleOwner, PicturePathObserver())

        binding.imageButtonAttachedPictureDelete.setOnClickListener { showDiaryPictureDeleteDialog() }
    }

    private inner class PicturePathObserver : Observer<Uri?> {
        override fun onChanged(value: Uri?) {
            val diaryPictureManager =
                DiaryPictureManager(
                    requireContext(),
                    binding.imageAttachedPicture,
                    requireThemeColor().getOnSurfaceVariantColor(requireContext().resources)
                )

            diaryPictureManager.setUpPictureOnDiary(value)
            enablePictureDeleteButton(value != null)
        }

        fun enablePictureDeleteButton(enabled: Boolean) {
            binding.imageButtonAttachedPictureDelete.isEnabled = enabled
            val alphaResId = if (enabled) {
                R.dimen.view_enabled_alpha
            } else {
                R.dimen.view_disabled_alpha
            }
            val alpha = ResourcesCompat.getFloat(resources, alphaResId)
            binding.imageButtonAttachedPictureDelete.alpha = alpha
        }
    }

    private fun updatePictureUriPermission() {
        val latestPictureUri = diaryEditViewModel.picturePath.value
        val loadedPictureUri = diaryEditViewModel.loadedPicturePath.value

        try {
            if (latestPictureUri == null && loadedPictureUri == null) return

            if (latestPictureUri != null && loadedPictureUri == null) {
                pictureUriPermissionManager.takePersistablePermission(latestPictureUri)
                return
            }

            if (latestPictureUri == null) {
                pictureUriPermissionManager
                    .releasePersistablePermission(checkNotNull(loadedPictureUri))
                return
            }

            if (latestPictureUri == loadedPictureUri) return

            pictureUriPermissionManager.takePersistablePermission(latestPictureUri)
            pictureUriPermissionManager.releasePersistablePermission(checkNotNull(loadedPictureUri))
        } catch (e: SecurityException) {
            // 対処できないがアプリを落としたくない為、catchのみ処理する。
        }
    }

    private fun releaseLoadedPictureUriPermission() {
        val loadedPictureUri = diaryEditViewModel.loadedPicturePath.value ?: return
        pictureUriPermissionManager.releasePersistablePermission(loadedPictureUri)
    }

    private fun setupEditText() {
        val textInputSetup = TextInputSetup(requireActivity())

        val allTextInputLayouts = createAllTextInputLayoutList().toTypedArray<TextInputLayout>()
        textInputSetup.setUpFocusClearOnClickBackground(
            binding.viewNestedScrollBackground,
            *allTextInputLayouts
        )

        textInputSetup.setUpKeyboardCloseOnEnter(binding.textInputLayoutTitle)

        val scrollableTextInputLayouts = arrayOf(
            binding.includeItem1.textInputLayoutComment,
            binding.includeItem2.textInputLayoutComment,
            binding.includeItem3.textInputLayoutComment,
            binding.includeItem4.textInputLayoutComment,
            binding.includeItem5.textInputLayoutComment,
        )
        textInputSetup.setUpScrollable(*scrollableTextInputLayouts)

        val clearableTextInputLayouts = arrayOf(
            binding.textInputLayoutTitle,
            binding.includeItem1.textInputLayoutTitle,
            binding.includeItem2.textInputLayoutTitle,
            binding.includeItem3.textInputLayoutTitle,
            binding.includeItem4.textInputLayoutTitle,
            binding.includeItem5.textInputLayoutTitle,
        )
        val transitionListener =
            textInputSetup.createClearButtonSetupTransitionListener(*clearableTextInputLayouts)
        addTransitionListener(transitionListener)

        // TODO:キーボード表示時の自動スクロールを無効化(自動スクロール時toolbarが隠れる為)している為、listenerで代用したいが上手くいかない。
        /*binding.includeItem1.textInputEditTextComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int scrollAmount = v.get
                    binding.nestedScrollFullScreen.smoothScrollBy(0, v.getHeight());
                    binding.nestedScrollFullScreen.scroll
                }
            }
        });*/
    }

    private fun clearFocusAllEditText() {
        val textInputLayoutList = createAllTextInputLayoutList()
        textInputLayoutList.stream().forEach { x: TextInputLayout ->
            val editText = checkNotNull(x.editText)
            editText.clearFocus()
        }
    }

    private fun createAllTextInputLayoutList(): @Unmodifiable List<TextInputLayout> {
        return listOf(
            binding.textInputLayoutDate,
            binding.textInputLayoutWeather1,
            binding.textInputLayoutWeather2,
            binding.textInputLayoutCondition,
            binding.textInputLayoutTitle,
            binding.includeItem1.textInputLayoutTitle,
            binding.includeItem1.textInputLayoutComment,
            binding.includeItem2.textInputLayoutTitle,
            binding.includeItem2.textInputLayoutComment,
            binding.includeItem3.textInputLayoutTitle,
            binding.includeItem3.textInputLayoutComment,
            binding.includeItem4.textInputLayoutTitle,
            binding.includeItem4.textInputLayoutComment,
            binding.includeItem5.textInputLayoutTitle,
            binding.includeItem5.textInputLayoutComment
        )
    }

    private fun fetchWeatherInfo(date: LocalDate, requestsShowingDialog: Boolean) {
        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        val isChecked = settingsViewModel.isCheckedWeatherInfoAcquisitionSetting
        if (!isChecked) return

        val hasUpdatedLocation = settingsViewModel.hasUpdatedGeoCoordinates()
        if (!hasUpdatedLocation) {
            diaryEditViewModel.addWeatherInfoFetchErrorMessage()
            return
        }

        // 本フラグメント起動時のみダイアログなしで天気情報取得
        if (requestsShowingDialog) {
            showWeatherInfoFetchingDialog(date)
        } else {
            val geoCoordinates = settingsViewModel.geoCoordinatesLiveData.checkNotNull()
            diaryEditViewModel.fetchWeatherInformation(date, geoCoordinates)
        }
    }

    private fun showDiaryShowFragment(date: LocalDate) {
        if (isDialogShowing()) return

        val isStartDiaryFragment =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).isStartDiaryFragment
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるactionを切り替える。
        val action = if (isStartDiaryFragment) {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPattern2(date)
        } else {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPattern1(date)
        }
        navController.navigate(action)
    }

    private fun showDiaryItemTitleEditFragment(
        inputItemNumber: ItemNumber,
        inputItemTitle: String
    ) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToSelectItemTitleFragment(inputItemNumber, inputItemTitle)
        navController.navigate(action)
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(true)
    }

    private fun showDiaryLoadingDialog(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryLoadingDialog(date)
        navController.navigate(action)
    }

    private fun showDiaryUpdateDialog(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryUpdateDialog(date)
        navController.navigate(action)
    }

    private fun showDiaryDeleteDialog(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryDeleteDialog(date)
        navController.navigate(action)
    }

    private fun showDatePickerDialog(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDatePickerDialog(date)
        navController.navigate(action)
    }

    private fun showWeatherInfoFetchingDialog(date: LocalDate) {
        if (isDialogShowing()) return
        if (!diaryEditViewModel.canFetchWeatherInformation(date)) return

        // 今日の日付以降は天気情報を取得できないためダイアログ表示不要
        diaryEditViewModel.canFetchWeatherInformation(date)

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchingDialog(date)
        navController.navigate(action)
    }

    private fun showDiaryItemDeleteDialog(itemNumber: ItemNumber) {
        if (isDialogShowing()) return

        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber)
        navController.navigate(action)
    }

    private fun showDiaryPictureDeleteDialog() {
        if (isDialogShowing()) return

        val action =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryPictureDeleteDialog()
        navController.navigate(action)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryEditViewModel.triggerAppMessageBufferListObserver()
    }

    override fun onResume() {
        super.onResume()

        // HACK:DiaryItemTitleEditFragmentから本Fragmentへ画面遷移(戻る)した時、
        //      スピナーのアダプターが選択中アイテムのみで構成されたアダプターに更新されてしまうので
        //      onResume()メソッドにて再度アダプターを設定して対策。
        //      (Weather2はWeather1のObserver内で設定している為不要)
        val weatherArrayAdapter = createWeatherSpinnerAdapter()
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter)
        val conditionArrayAdapter = createConditionSpinnerAdapter()
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter)

        // HACK:ItemTitleEditFragmentから戻ってきた時に処理させたく箇所を
        //      変数(DiaryEditViewModel.IsShowingItemTitleEditFragment)で分岐させる。
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(false)
    }

    override fun destroyBinding() {
        _binding = null
    }

    fun attachPicture(uri: Uri) {
        diaryEditViewModel.updatePicturePath(uri)
    }
}
