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
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.picker.DatePickerDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryEditViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryItemDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryLoadDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryLoadFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryImageDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.DiaryItemTitleEditDialog
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryUpdateDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.ExitWithoutDiarySaveDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.WeatherInfoFetchDialogFragment
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParametersForDiaryEdit
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.adapter.spinner.ConditionSpinnerAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.spinner.WeatherSpinnerAdapter
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import java.time.LocalDate

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment<FragmentDiaryEditBinding, DiaryEditEvent>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryEditFragment::class.java.name
    }

    val logTag = createLogTag()

    private val motionLayoutTransitionTime = 500 /*ms*/

    private val motionLayoutJumpTime = 1 /*ms*/

    private lateinit var motionLayoutDiaryEditItems: Array<MotionLayout>

    private var itemMotionLayoutListeners: Array<ItemMotionLayoutListener>? = null

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
        setUpToolBar()
        setUpDateInputField()
        setUpWeatherInputField()
        setUpConditionInputField()
        setUpTitleInputField()
        setUpItemInputField()
        setUpImageInputField()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryItemTitleEditFragmentResultReceiver()
        setUpDiaryLoadDialogResultReceiver()
        setUpDiaryLoadFailureDialogResultReceiver()
        setUpDiaryUpdateDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
        setUpDatePickerDialogResultReceiver()
        setUpWeatherInfoFetchDialogResultReceiver()
        setUpDiaryItemDeleteDialogResultReceiver()
        setUpDiaryImageDeleteDialogResultReceiver()
        setUpExitWithoutDiarySaveDialogResultReceiver()
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
    private fun setUpDiaryLoadDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadDialogResultReceived(result)
        }
    }

    // 日記読込失敗確認ダイアログフラグメントから結果受取
    private fun setUpDiaryLoadFailureDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadFailureDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadFailureDialogResultReceived(result)
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

    private fun setUpExitWithoutDiarySaveDialogResultReceiver() {
        setUpDialogResultReceiver(
            ExitWithoutDiarySaveDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onExitWithoutDiarySaveDialogResultReceived(result)
        }
    }

    override fun onMainUiEventReceived(event: DiaryEditEvent) {
        when (event) {
            is DiaryEditEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.date)
            }
            is DiaryEditEvent.NavigateDiaryItemTitleEditFragment -> {
                navigateDiaryItemTitleEditFragment(event.diaryItemTitle)
            }
            is DiaryEditEvent.NavigateDiaryLoadDialog -> {
                navigateDiaryLoadDialog(event.parameters)
            }
            is DiaryEditEvent.NavigateDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
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
            is DiaryEditEvent.NavigateExitWithoutDiarySaveConfirmationDialog -> {
                navigateExitWithoutDiarySaveConfirmationDialog(event.parameters)
            }
            is DiaryEditEvent.NavigatePreviousFragmentOnDiaryDelete -> {
                navigatePreviousFragmentOnDiaryDelete(event.result)
            }
            is DiaryEditEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed -> {
                navigatePreviousFragmentWithRetry(KEY_RESULT, event.result)
            }
            is DiaryEditEvent.TransitionDiaryItemToInvisibleState -> {
                transitionDiaryItemToInvisible(event.itemNumber, false)
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
            is DiaryEditEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        navigatePreviousFragmentOnce(KEY_RESULT, event.wrappedEvent.result)
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    private fun setUpFocusViewScroll() {
        KeyboardManager().registerKeyBoredStateListener(this) { isVisible ->
            if (!isVisible) return@registerKeyBoredStateListener
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

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // 日記保存、削除
                when (item.itemId) {
                    R.id.diaryEditToolbarOptionSaveDiary -> {
                        mainViewModel.onDiarySaveMenuClick()
                        return@setOnMenuItemClickListener true
                    }

                    R.id.diaryEditToolbarOptionDeleteDiary -> {
                        mainViewModel.onDiaryDeleteMenuClick()
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
            mainViewModel.isNewDiary
                .collectLatest { value: Boolean ->
                    // MEMO:日記新規作成時はnullとなり、新規作成状態と判断する。
                    val isDeleteEnabled = !value

                    val menu = binding.materialToolbarTopAppBar.menu
                    val deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary)
                    deleteMenuItem.setEnabled(isDeleteEnabled)

                    // TODO:テスト用の為、最終的に削除
                    val testMenuItem = menu.findItem(R.id.diaryEditToolbarOptionTest)
                    testMenuItem.setEnabled(!isDeleteEnabled)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.editingDiaryDate
                .collectLatest { value: LocalDate? ->
                    val dateString = value?.toJapaneseDateString(requireContext())
                    mainViewModel.onOriginalDiaryDateChanged(dateString)
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
        binding.autoCompleteTextWeather1
            .setAdapter(WeatherSpinnerAdapter(requireContext(), themeColor))

        binding.autoCompleteTextWeather1.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, _: View?, position: Int, _: Long ->
                requireNotNull(parent)
                val arrayAdapter = parent.adapter as ArrayAdapter<*>
                val weatherString = arrayAdapter.getItem(position) as String
                val weather = WeatherUi.of(requireContext(), weatherString)
                mainViewModel.onWeather1InputFieldItemClick(weather)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1
                .collectLatest { value: WeatherUi ->
                    Log.d("20250428", "Weather collectLatest()")
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather1.setText(strWeather, false)
                    binding.autoCompleteTextWeather2
                        .setAdapter(WeatherSpinnerAdapter(requireContext(), themeColor, value))
                }
        }

        binding.autoCompleteTextWeather2.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, _: View?, position: Int, _: Long ->
                requireNotNull(parent)
                val arrayAdapter = parent.adapter as ArrayAdapter<*>
                val weatherString = arrayAdapter.getItem(position) as String
                val weather = WeatherUi.of(requireContext(), weatherString)
                mainViewModel.onWeather2InputFieldItemClick(weather)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: WeatherUi ->
                    val strWeather = value.toString(requireContext())
                    binding.autoCompleteTextWeather2.setText(strWeather, false)
                }
        }
    }

    // 気分入力欄。
    private fun setUpConditionInputField() {
        binding.autoCompleteTextCondition
            .setAdapter(
                ConditionSpinnerAdapter(requireContext(), themeColor)
            )

        binding.autoCompleteTextCondition.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, _: View?, position: Int, _: Long ->
                requireNotNull(parent)
                val arrayAdapter = parent.adapter as ArrayAdapter<*>
                val conditionString = arrayAdapter.getItem(position) as String
                val condition = ConditionUi.of(requireContext(), conditionString)
                mainViewModel.onConditionInputFieldItemClick(condition)
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.condition
                .collectLatest { value: ConditionUi ->
                    val strCondition = value.toString(requireContext())
                    binding.autoCompleteTextCondition.setText(strCondition, false)
                }
        }
    }

    @Suppress("EmptyMethod")
    private fun setUpTitleInputField() {
        // 処理なし
    }

    private fun setUpItemInputField() {
        // 項目欄設定
        // 項目タイトル入力欄設定
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
        textInputEditTextItemsTitle.forEachIndexed { i, textInputEditText ->
            textInputEditText.setOnClickListener {
                val inputItemNumber = i + 1
                mainViewModel.onItemTitleInputFieldClick(inputItemNumber)
            }
        }

        // 項目削除ボタン設定
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
        imageButtonItemsDelete.forEachIndexed { i, imageButton ->
            imageButton.setOnClickListener {
                val inputItemNumber = i + 1
                mainViewModel.onItemDeleteButtonClick(inputItemNumber)
            }
        }

        // 項目欄MotionLayout設定
        motionLayoutDiaryEditItems =
            binding.run {
                arrayOf(
                    includeItem1.motionLayoutDiaryEditItem,
                    includeItem2.motionLayoutDiaryEditItem,
                    includeItem3.motionLayoutDiaryEditItem,
                    includeItem4.motionLayoutDiaryEditItem,
                    includeItem5.motionLayoutDiaryEditItem,
                )
            }
        val arraySize = motionLayoutDiaryEditItems.size
        itemMotionLayoutListeners =
            Array(arraySize) { init ->
                ItemMotionLayoutListener(
                    arraySize,
                    init + 1,
                    binding.includeItem1.linerLayoutDiaryEditItem,
                    binding.nestedScrollFullScreen,
                    { mainViewModel.onDiaryItemInvisibleStateTransitionCompleted(it) },
                    { mainViewModel.onDiaryItemVisibleStateTransitionCompleted() },
                    { selectItemMotionLayout(it) }
                ).also {
                    motionLayoutDiaryEditItems[init].setTransitionListener(it)
                }
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    setUpItemsLayout(value)
                }
        }
    }

    private class ItemMotionLayoutListener(
        private val maxItemNumber: Int,
        private val itemNumber: Int,
        private val itemLayout: LinearLayout,
        private val scrollView: NestedScrollView,
        private val onDiaryItemTransitionToInvisibleCompleted: (Int) -> Unit,
        private val onDiaryItemTransitionToVisibleCompleted: (Int) -> Unit,
        private val processItemMotionLayoutSelection: (Int) -> MotionLayout,
    ): MotionLayout.TransitionListener {

        private val logTag = createLogTag()

        private val scrollTimeMotionLayoutTransition = 1000 /*ms*/

        private var isTriggeredBySmooth = false

        fun markTransitionAsSmooth() {
            isTriggeredBySmooth = true
        }

        fun markTransitionAsJump() {
            isTriggeredBySmooth = false
        }

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
            if (currentId == R.id.motion_scene_edit_diary_item_invisible_state) {
                completedStateLogMsg = "InvisibleState"
                if (isTriggeredBySmooth) {
                    if (isNextItemInvisibleState()) scrollOnDiaryItemTransitionToInvisible()
                    onDiaryItemTransitionToInvisibleCompleted(itemNumber)
                }

            // 対象項目欄追加後の処理
            } else if (currentId == R.id.motion_scene_edit_diary_item_visible_state) {
                completedStateLogMsg = "VisibleState"
                if (isTriggeredBySmooth) {
                    scrollOnDiaryItemTransitionToVisible()
                    onDiaryItemTransitionToVisibleCompleted(itemNumber)
                }
            }
            Log.d(logTag, "onTransitionCompleted()_itemNumber = $itemNumber, CompletedState = $completedStateLogMsg")

            isTriggeredBySmooth = false
        }

        private fun isNextItemInvisibleState(): Boolean {
            if (itemNumber == maxItemNumber) return true
            val nextItemNumber = itemNumber.inc()
            val motionLayout = processItemMotionLayoutSelection(nextItemNumber)
            return motionLayout.currentState == R.id.motion_scene_edit_diary_item_invisible_state
        }

        // 対象項目追加後のスクロール処理
        private fun scrollOnDiaryItemTransitionToVisible() {
            scrollOnDiaryItemTransition(true)
        }

        // 対象項目削除後のスクロール処理
        private fun scrollOnDiaryItemTransitionToInvisible() {
            scrollOnDiaryItemTransition(false)
        }

        private fun scrollOnDiaryItemTransition(isUpDirection: Boolean) {
            val itemHeight = itemLayout.height
            val scrollY =
                if (isUpDirection) {
                    itemHeight
                } else {
                    -itemHeight
                }
            Log.d("20250801", "scrollOnDiaryItemTransition(${isUpDirection})_scrollView:${scrollView}_itemHeight:${itemHeight}")
            scrollView.smoothScrollBy(0, scrollY, scrollTimeMotionLayoutTransition)
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

    private fun selectItemMotionLayout(itemNumber: Int): MotionLayout {
        val arrayNumber = itemNumber - 1
        return motionLayoutDiaryEditItems[arrayNumber]
    }

    private fun selectItemMotionLayoutListener(itemNumber: Int): ItemMotionLayoutListener {
        val arrayNumber = itemNumber - 1
        return checkNotNull(itemMotionLayoutListeners)[arrayNumber]
    }

    private fun setUpItemsLayout(numItems: Int) {
        Log.d(logTag, "setUpItemsLayout()_numItems = $numItems")

        // MEMO:削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
        if (shouldTransitionItemMotionLayout) {
            shouldTransitionItemMotionLayout = false
            val numVisibleItems = countVisibleItems()
            val differenceValue = numItems - numVisibleItems
            if (numItems > numVisibleItems && differenceValue == 1) {
                transitionDiaryItemToVisible(numItems, false)
                return
            }
        }

        for (i in motionLayoutDiaryEditItems.indices) {
            val itemNumber = i + 1
            if (itemNumber <= numItems) {
                transitionDiaryItemToVisible(itemNumber, true)
            } else {
                transitionDiaryItemToInvisible(itemNumber, true)
            }
        }
    }

    private fun transitionDiaryItemToInvisible(itemNumber: Int, isJump: Boolean) {
        Log.d("logTag", "transitionDiaryItemToInvisible()_itemNumber = $itemNumber, isJump = $isJump")
        val itemMotionLayoutListener = selectItemMotionLayoutListener(itemNumber)
        val itemMotionLayout = selectItemMotionLayout(itemNumber)
        if (isJump) {
            itemMotionLayoutListener.markTransitionAsJump()
            // HACK: 画面回転後など、特定の条件下で jumpToState() を使用すると、
            //       意図した状態に遷移せず、異なる状態（例: EndStateの指示でStartState状態に遷移）になる場合があった。
            //       transitionToState() に変更し、短いduration（ほぼ0に近い値）を指定することで同等の表示を行う。
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_invisible_state,
                    motionLayoutJumpTime
                )
        } else {
            itemMotionLayoutListener.markTransitionAsSmooth()
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_invisible_state,
                    motionLayoutTransitionTime
                )
        }
    }

    private fun transitionDiaryItemToVisible(itemNumber: Int, isJump: Boolean) {
        Log.d("logTag", "transitionDiaryItemToVisible()_itemNumber = $itemNumber, isJump = $isJump")
        val itemMotionLayoutListener = selectItemMotionLayoutListener(itemNumber)
        val itemMotionLayout = selectItemMotionLayout(itemNumber)
        if (isJump) {
            itemMotionLayoutListener.markTransitionAsJump()
            // HACK: 画面回転後など、特定の条件下で jumpToState() を使用すると、
            //       意図した状態に遷移せず、異なる状態（例: EndStateの指示でStartState状態に遷移）になる場合があった。
            //       transitionToState() に変更し、短いduration（ほぼ0に近い値）を指定することで同等の表示を行う。
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_visible_state,
                    motionLayoutJumpTime
                )
        } else {
            itemMotionLayoutListener.markTransitionAsSmooth()
            itemMotionLayout
                .transitionToState(
                    R.id.motion_scene_edit_diary_item_visible_state,
                    motionLayoutTransitionTime
                )
        }
    }

    private fun countVisibleItems(): Int {
        return motionLayoutDiaryEditItems.count { motionLayout ->
            motionLayout.currentState == R.id.motion_scene_edit_diary_item_visible_state
        }
    }

    private fun setUpImageInputField() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.imageFileName
                .collectLatest { value: ImageFileNameUi? ->
                    mainViewModel.onDiaryImageFileNameChanged(value)
                }
        }
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.imageFilePath
                .collectLatest { value: ImageFilePathUi? ->
                    binding.imageProgressAttachedImage.loadImage(value?.path)
                }
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
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemTitleEditFragment(diaryItemTitle: DiaryItemTitle) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemTitleEditDialog(
                diaryItemTitle
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadDialog(parameters: DiaryLoadParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryUpdateDialog(parameters: DiaryUpdateParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryUpdateDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(parameters: DiaryDeleteParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryDeleteDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDatePickerDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateWeatherInfoFetchDialog(parameters: WeatherInfoFetchParameters) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemDeleteDialog(parameters: DiaryItemDeleteParameters) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemDeleteDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryImageDeleteDialog() {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryImageDeleteDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    private fun navigateExitWithoutDiarySaveConfirmationDialog(
        parameters: NavigatePreviousParametersForDiaryEdit
    ) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToExitWithoutDiarySaveConfirmationDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigatePreviousFragmentOnDiaryDelete(result: FragmentResult.Some<LocalDate>) {
        val destinationId =
            try {
                findNavController().getBackStackEntry(R.id.navigation_calendar_fragment)
                R.id.navigation_calendar_fragment
            } catch (e: IllegalArgumentException) {
                R.id.navigation_diary_list_fragment
            }
        navigateFragmentWithRetry(
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

    override fun clearViewBindings() {
        itemMotionLayoutListeners = null

        super.clearViewBindings()
    }
}
