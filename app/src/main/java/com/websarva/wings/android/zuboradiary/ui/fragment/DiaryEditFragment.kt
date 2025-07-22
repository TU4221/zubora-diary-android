package com.websarva.wings.android.zuboradiary.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator
import com.websarva.wings.android.zuboradiary.ui.view.edittext.TextInputConfigurator
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DatePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryEditViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryItemDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryImageDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryItemTitleEditDialog
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryUpdateDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.ExitWithoutDiarySavingDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.WeatherInfoFetchDialogFragment
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadingParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.annotations.Unmodifiable
import java.time.LocalDate

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment<FragmentDiaryEditBinding>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryEditFragment::class.java.name
    }

    private val logTag = createLogTag()

    private val motionLayoutTransitionTime = 500 /*ms*/
    private val scrollTimeMotionLayoutTransition = 1000 /*ms*/

    private var shouldTransitionItemMotionLayout = false

    override val destinationId = R.id.navigation_diary_edit_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryEditViewModel by viewModels()

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

    // MEMO:端末ギャラリーから画像Uri取得。画像未選択時、nullを受け取る。
    private val openDocumentResultLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        mainViewModel.onOpenDocumentResultImageUriReceived(uri)
    }

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryEditBinding {
        return FragmentDiaryEditBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFocusViewScroll()
        setUpDiaryData()
        setUpToolBar()
        setUpDateInputField()
        setUpWeatherInputField()
        setUpConditionInputField()
        setUpTitleInputField()
        setUpItemInputField()
        setUpImageInputField()
        setupEditText()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryItemTitleEditFragmentResultReceiver()
        setUpDiaryLoadingDialogResultReceiver()
        setUpDiaryLoadingFailureDialogResultReceiver()
        setUpDiaryUpdateDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
        setUpDatePickerDialogResultReceiver()
        setUpWeatherInfoFetchDialogResultReceiver()
        setUpDiaryItemDeleteDialogResultReceiver()
        setUpDiaryImageDeleteDialogResultReceiver()
        setUpExitWithoutDiarySavingDialogResultReceiver()
    }

    // DiaryItemTitleEditFragmentから編集結果受取
    private fun setUpDiaryItemTitleEditFragmentResultReceiver() {
        setUpFragmentResultReceiver(
            DiaryItemTitleEditDialog.KEY_RESULT
        ) { result: FragmentResult<DiaryItemTitle> ->
            mainViewModel.onItemTitleEditFragmentResultReceived(result)
        }
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private fun setUpDiaryLoadingDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadingDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadingDialogResultReceived(result)
        }
    }

    // 日記読込失敗確認ダイアログフラグメントから結果受取
    private fun setUpDiaryLoadingFailureDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadingFailureDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadingFailureDialogResultReceived(result)
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun setUpDiaryUpdateDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryUpdateDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryUpdateDialogResultReceived(result)
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun setUpDiaryDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private fun setUpDatePickerDialogResultReceiver() {
        setUpDialogResultReceiver(
            DatePickerDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDatePickerDialogResultReceived(result)
        }
    }

    // 天気情報読込ダイアログフラグメントから結果受取
    private fun setUpWeatherInfoFetchDialogResultReceiver() {
        setUpDialogResultReceiver(
            WeatherInfoFetchDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onWeatherInfoFetchDialogResultReceived(result)
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private fun setUpDiaryItemDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryItemDeleteDialogFragment.KEY_RESULT
        ) { result ->

            // TODO:シールドクラス Action -> Event に変更してから下記コードの処理方法を検討する。
            when (result) {
                is DialogResult.Positive -> {
                    shouldTransitionItemMotionLayout = true
                }

                DialogResult.Negative,
                DialogResult.Cancel -> {
                    // 処理なし
                }
            }

            mainViewModel.onDiaryItemDeleteDialogResultReceived(result)
        }
    }

    private fun setUpDiaryImageDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryImageDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryImageDeleteDialogResultReceived(result)
        }
    }

    private fun setUpExitWithoutDiarySavingDialogResultReceiver() {
        setUpDialogResultReceiver(
            ExitWithoutDiarySavingDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onExitWithoutDiarySavingDialogResultReceived(result)
        }
    }

    override fun onMainViewModelEventReceived(event: ViewModelEvent) {
        when (event) {
            is DiaryEditEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.date)
            }
            is DiaryEditEvent.NavigateDiaryItemTitleEditFragment -> {
                navigateDiaryItemTitleEditFragment(event.diaryItemTitle)
            }
            is DiaryEditEvent.NavigateDiaryLoadingDialog -> {
                navigateDiaryLoadingDialog(event.parameters)
            }
            is DiaryEditEvent.NavigateDiaryLoadingFailureDialog -> {
                navigateDiaryLoadingFailureDialog(event.date)
            }
            is DiaryEditEvent.NavigateDiaryUpdateDialog -> {
                navigateDiaryUpdateDialog(event.parameters)
            }
            is DiaryEditEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.parameters)
            }
            is DiaryEditEvent.NavigateDatePickerDialog -> {
                navigateDatePickerDialog(event.date)
            }
            is DiaryEditEvent.NavigateWeatherInfoFetchDialog -> {
                navigateWeatherInfoFetchDialog(event.parameters)
            }
            is DiaryEditEvent.NavigateDiaryItemDeleteDialog -> {
                navigateDiaryItemDeleteDialog(event.parameters)
            }
            DiaryEditEvent.NavigateDiaryImageDeleteDialog -> {
                navigateDiaryImageDeleteDialog()
            }
            is DiaryEditEvent.NavigateExitWithoutDiarySavingConfirmationDialog -> {
                navigateExitWithoutDiarySavingConfirmationDialog(event.parameters)
            }
            is DiaryEditEvent.NavigatePreviousFragment -> {
                navigatePreviousFragment(event.result)
            }
            is DiaryEditEvent.NavigatePreviousFragmentOnDiaryDelete -> {
                navigatePreviousFragmentOnDiaryDelete(event.result)
            }
            is DiaryEditEvent.TransitionDiaryItemHidedState -> {
                hideItem(event.itemNumber, false)
            }
            is DiaryEditEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch -> {
                checkAccessLocationPermissionBeforeWeatherInfoFetch(event.parameters)
            }
            is DiaryEditEvent.ItemAddition -> {
                shouldTransitionItemMotionLayout = true
            }
            is DiaryEditEvent.SelectImage -> {
                openDocumentResultLauncher.launch(arrayOf("image/*"))
            }
            is ViewModelEvent.NavigatePreviousFragment -> {
                // MEMO:"DiaryEditEvent.NavigatePreviousFragment"を使用する為、
                //      "ViewModelEvent.NavigatePreviousFragment"処理不要。
                throw IllegalArgumentException()
            }
            is ViewModelEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
            else -> {
                throw IllegalArgumentException()
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
        val diaryDate = DiaryEditFragmentArgs.fromBundle(requireArguments()).diaryDate
        val shouldLoadDiary =
            DiaryEditFragmentArgs.fromBundle(requireArguments()).shouldLoadDiary
        mainViewModel
            .onDiaryDataSetUp(
                diaryDate,
                shouldLoadDiary
            )
    }

    private fun setUpToolBar() {
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
            mainViewModel.loadedDiary
                .collectLatest { value: Diary? ->
                    // MEMO:日記新規作成時はnullとなり、新規作成状態と判断する。
                    val isDeleteEnabled = value != null

                    val menu = binding.materialToolbarTopAppBar.menu
                    val deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary)
                    deleteMenuItem.setEnabled(isDeleteEnabled)

                    val dateString = value?.date?.toJapaneseDateString(requireContext())
                    mainViewModel.onLoadedDiaryChangedUpdateEditingDiaryDateString(dateString)

                    // TODO:テスト用の為、最終的に削除
                    val testMenuItem = menu.findItem(R.id.diaryEditToolbarOptionTest)
                    testMenuItem.setEnabled(!isDeleteEnabled)
                }
        }
    }

    // 日付入力欄設定
    private fun setUpDateInputField() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.date.filterNotNull()
                .collectLatest { value: LocalDate ->
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
            textInputEditTextItemsTitle[itemArrayNumber].setOnClickListener {
                mainViewModel.onItemTitleInputFieldClicked(inputItemNumber)
            }
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
                    mainViewModel.onDiaryItemShowedStateTransitionCompleted()
                }
            }
            Log.d(logTag, "onTransitionCompleted()_CompletedState = $completedStateLogMsg")

            shouldTransitionItemMotionLayout = false
        }

        private fun isNextItemHidedState(): Boolean {
            if (itemNumber.value == ItemNumber.MAX_NUMBER) return true
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
            setUpItemsLayout(value)
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

    private fun setUpImageInputField() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.imageUri
                .collectLatest { value: Uri? ->
                    // MEMO:添付画像がないときはnullとなり、デフォルト画像をセットする。
                    DiaryImageConfigurator()
                        .setUpImageOnDiary(
                            binding.imageAttachedImage,
                            value,
                            themeColor
                        )
                }
        }
    }

    private fun setupEditText() {
        val textInputConfigurator = TextInputConfigurator()
        textInputConfigurator.setUpFocusClearOnClickBackground(binding.viewNestedScrollBackground)
        textInputConfigurator.setUpKeyboardCloseOnEnter(binding.textInputEditTextTitle)
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

    private fun navigateDiaryShowFragment(date: LocalDate) {
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるdirectionsを切り替える。
        val containsDiaryShowFragment =
            try {
                findNavController().getBackStackEntry(R.id.navigation_diary_show_fragment)
                true
            } catch (e: IllegalArgumentException) {
                false
            }

        val directions = if (containsDiaryShowFragment) {
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryShowFragmentPopUpToDiaryShow(date)
        } else {
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryShowFragmentPopUpToDiaryEdit(date)
        }
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemTitleEditFragment(diaryItemTitle: DiaryItemTitle) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemTitleEditDialog(
                diaryItemTitle
            )
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadingDialog(parameters: DiaryLoadingParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadingDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadingFailureDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadingFailureDialog(date)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryUpdateDialog(parameters: DiaryUpdateParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryUpdateDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(parameters: DiaryDeleteParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryDeleteDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDatePickerDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateWeatherInfoFetchDialog(parameters: WeatherInfoFetchParameters) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemDeleteDialog(parameters: DiaryItemDeleteParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemDeleteDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryImageDeleteDialog() {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryImageDeleteDialog()
        navigateFragment(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateExitWithoutDiarySavingConfirmationDialog(
        parameters: NavigatePreviousParameters
    ) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToExitWithoutDiarySavingConfirmationDialog(parameters)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigatePreviousFragment(result: FragmentResult<LocalDate>) {
        navigateFragment(NavigationCommand.Up(KEY_RESULT, result))
    }

    private fun navigatePreviousFragmentOnDiaryDelete(result: FragmentResult.Some<LocalDate>) {
        val destinationId =
            try {
                findNavController().getBackStackEntry(R.id.navigation_calendar_fragment)
                R.id.navigation_calendar_fragment
            } catch (e: IllegalArgumentException) {
                R.id.navigation_diary_list_fragment
            }
        navigateFragment(
            NavigationCommand.PopTo(
                destinationId,
                false,
                KEY_RESULT,
                result
            )
        )
    }

    private fun checkAccessLocationPermissionBeforeWeatherInfoFetch(
        parameters: WeatherInfoFetchParameters
    ) {
        mainViewModel.onAccessLocationPermissionChecked(
            requireContext().isAccessLocationGranted(),
            parameters
        )
    }
}
