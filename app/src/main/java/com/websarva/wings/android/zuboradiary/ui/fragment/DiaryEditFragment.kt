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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
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
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.utils.asString
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.adapter.spinner.AppDropdownAdapter
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.collections.map

@AndroidEntryPoint
class DiaryEditFragment : BaseFragment<FragmentDiaryEditBinding, DiaryEditUiEvent>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryEditViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_edit_fragment

    private val motionLayoutTransitionTime = 500 /*ms*/

    private val motionLayoutJumpTime = 1 /*ms*/

    private var itemMotionLayouts: Array<MotionLayout>? = null

    private var itemMotionLayoutListeners: Array<ItemMotionLayoutListener>? = null

    private var shouldTransitionItemMotionLayout = false

    private lateinit var keyboardManager: KeyboardManager

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
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpKeyboard()
        setUpKeyboard()
        setUpToolbar()
        setUpItemMotionLayouts()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryEditBinding {
        return FragmentDiaryEditBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun clearViewBindings() {
        binding.materialToolbarTopAppBar.setOnMenuItemClickListener(null)

        binding.autoCompleteTextWeather1.setAdapter(null)
        binding.autoCompleteTextWeather2.setAdapter(null)
        binding.autoCompleteTextCondition.setAdapter(null)

        itemMotionLayouts?.forEach {
            it.setTransitionListener(null)
        }
        itemMotionLayouts = null
        itemMotionLayoutListeners = null

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setUpFragmentResultObservers() {
        observeDiaryItemTitleEditFragmentResult()
        observeDiaryLoadDialogResult()
        observeDiaryLoadFailureDialogResult()
        observeDiaryUpdateDialogResult()
        observeDiaryDeleteDialogResult()
        observeDatePickerDialogResult()
        observeUpWeatherInfoFetchDialogResult()
        observeDiaryItemDeleteDialogResult()
        observeDiaryImageDeleteDialogResult()
        observeExitWithoutDiarySaveDialogResult()
    }

    // DiaryItemTitleEditFragmentから編集結果受取
    private fun observeDiaryItemTitleEditFragmentResult() {
        observeFragmentResult(
            DiaryItemTitleEditDialog.RESULT_KEY
        ) { result: FragmentResult<DiaryItemTitleSelectionUi> ->
            mainViewModel.onItemTitleEditFragmentResultReceived(result)
        }
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private fun observeDiaryLoadDialogResult() {
        observeDialogResult(
            DiaryLoadDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryLoadDialogResultReceived(result)
        }
    }

    // 日記読込失敗確認ダイアログフラグメントから結果受取
    private fun observeDiaryLoadFailureDialogResult() {
        observeDialogResult(
            DiaryLoadFailureDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryLoadFailureDialogResultReceived(result)
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun observeDiaryUpdateDialogResult() {
        observeDialogResult(
            DiaryUpdateDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryUpdateDialogResultReceived(result)
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult(
            DiaryDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private fun observeDatePickerDialogResult() {
        observeDialogResult(
            DatePickerDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDatePickerDialogResultReceived(result)
        }
    }

    // 天気情報読込ダイアログフラグメントから結果受取
    private fun observeUpWeatherInfoFetchDialogResult() {
        observeDialogResult(
            WeatherInfoFetchDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onWeatherInfoFetchDialogResultReceived(result)
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private fun observeDiaryItemDeleteDialogResult() {
        observeDialogResult(
            DiaryItemDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryItemDeleteDialogResultReceived(result)
        }
    }

    private fun observeDiaryImageDeleteDialogResult() {
        observeDialogResult(
            DiaryImageDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryImageDeleteDialogResultReceived(result)
        }
    }

    private fun observeExitWithoutDiarySaveDialogResult() {
        observeDialogResult(
            ExitWithoutDiarySaveDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onExitWithoutDiarySaveDialogResultReceived(result)
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryEditUiEvent) {
        when (event) {
            is DiaryEditUiEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            is DiaryEditUiEvent.NavigateDiaryItemTitleEditFragment -> {
                navigateDiaryItemTitleEditFragment(event.diaryItemTitleSelection)
            }
            is DiaryEditUiEvent.NavigateDiaryLoadDialog -> {
                navigateDiaryLoadDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateDiaryUpdateDialog -> {
                navigateDiaryUpdateDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateDatePickerDialog -> {
                navigateDatePickerDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateWeatherInfoFetchDialog -> {
                navigateWeatherInfoFetchDialog(event.date)
            }
            is DiaryEditUiEvent.NavigateDiaryItemDeleteDialog -> {
                navigateDiaryItemDeleteDialog(event.itemNumber)
            }
            DiaryEditUiEvent.NavigateDiaryImageDeleteDialog -> {
                navigateDiaryImageDeleteDialog()
            }
            is DiaryEditUiEvent.NavigateExitWithoutDiarySaveConfirmationDialog -> {
                navigateExitWithoutDiarySaveConfirmationDialog()
            }
            is DiaryEditUiEvent.NavigatePreviousFragmentOnDiaryDelete -> {
                navigatePreviousFragmentOnDiaryDelete(event.result)
            }
            is DiaryEditUiEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed -> {
                navigatePreviousFragmentWithRetry(RESULT_KEY, event.result)
            }
            is DiaryEditUiEvent.UpdateDiaryItemLayout -> {
                renderItemLayouts(event.numVisibleItems)
            }
            is DiaryEditUiEvent.TransitionDiaryItemToInvisibleState -> {
                transitionDiaryItemToInvisible(event.itemNumber, false)
            }
            is DiaryEditUiEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch -> {
                checkAccessLocationPermissionBeforeWeatherInfoFetch()
            }
            is DiaryEditUiEvent.ItemAddition -> {
                shouldTransitionItemMotionLayout = true
            }
            is DiaryEditUiEvent.SelectImage -> {
                openDocumentResultLauncher.launch(arrayOf("image/*"))
            }
        }
    }

    override fun onCommonUiEventReceived(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.NavigatePreviousFragment<*> -> {
                navigatePreviousFragmentOnce(RESULT_KEY, event.result)
            }
            is CommonUiEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
        }
    }

    override fun setUpUiStateObservers() {
        super.setUpUiStateObservers()

        observeToolbarMenuState()
        observeWeather1DropdownOptions()
        observeWeather2DropdownOptions()
        observeConditionDropdownOptions()
    }

    private fun observeToolbarMenuState() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.isNewDiary == new.isNewDiary
            }.map {
                it.isNewDiary
            }.collect {
                updateToolbarMenuState(!it) // isNewDiaryがfalseの時に有効
            }
        }
    }

    private fun observeWeather1DropdownOptions() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.weather1Options == new.weather1Options
            }.map {
                it.weather1Options
            }.collect {
                updateWeather1DropdownAdapter(it)
            }
        }
    }

    private fun observeWeather2DropdownOptions() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.weather2Options == new.weather2Options
            }.map {
                it.weather2Options
            }.collect {
                updateWeather2DropdownAdapter(it)
            }
        }
    }

    private fun observeConditionDropdownOptions() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.conditionOptions == new.conditionOptions
            }.map {
                it.conditionOptions
            }.collect {
                updateConditionDropdownAdapter(it)
            }
        }
    }
    //endregion

    //region View Setup
    private fun setUpToolbar() {
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
    }

    private fun setUpKeyboard() {
        keyboardManager = KeyboardManager(requireContext()).apply {
            registerKeyboardStateListener(this@DiaryEditFragment) { isVisible ->
                if (!isVisible) return@registerKeyboardStateListener
                if (!isSoftInputAdjustNothing()) return@registerKeyboardStateListener

                val focusView =
                    this@DiaryEditFragment.view?.findFocus() ?: return@registerKeyboardStateListener

                val offset = screenHeight / 3
                val location = IntArray(2)
                focusView.getLocationOnScreen(location)
                val positionY = location[1]
                val scrollAmount = positionY - offset

                binding.nestedScrollFullScreen.smoothScrollBy(0, scrollAmount)
            }
        }
    }

    // MEMO:キーボード表示時、ActivityのLayoutが変更されない設定であるかを確認。
    private fun isSoftInputAdjustNothing(): Boolean {
        val softInputAdjust =
            requireActivity().window.attributes.softInputMode and
                    WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        return softInputAdjust == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
    }
    //endregion

    //region View Manipulation
    private fun updateToolbarMenuState(isDeleteEnabled: Boolean) {
        val menu = binding.materialToolbarTopAppBar.menu
        val deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary)
        deleteMenuItem.isEnabled = isDeleteEnabled

        // TODO:テスト用の為、最終的に削除
        val testMenuItem = menu.findItem(R.id.diaryEditToolbarOptionTest)
        testMenuItem.isEnabled = !isDeleteEnabled
    }

    private fun updateWeather1DropdownAdapter(options: List<WeatherUi>) {
        val context = requireContext()
        val adapter =
            AppDropdownAdapter(
                context,
                themeColor,
                options.map { it.asString(context) }
            )
        binding.autoCompleteTextWeather1.setAdapter(adapter)
    }

    private fun updateWeather2DropdownAdapter(options: List<WeatherUi>) {
        val context = requireContext()
        val adapter =
            AppDropdownAdapter(
                context,
                themeColor,
                options.map { it.asString(context) }
            )
        binding.autoCompleteTextWeather2.setAdapter(adapter)
    }

    private fun updateConditionDropdownAdapter(options: List<ConditionUi>) {
        val context = requireContext()
        val adapter =
            AppDropdownAdapter(
                context,
                themeColor,
                options.map { it.asString(context) }
            )
        binding.autoCompleteTextCondition.setAdapter(adapter)
    }
    //endregion

    //region Motion Layout Setup
    private fun setUpItemMotionLayouts() {
        itemMotionLayouts =
            binding.run {
                arrayOf(
                    includeItem1.motionLayoutDiaryEditItem,
                    includeItem2.motionLayoutDiaryEditItem,
                    includeItem3.motionLayoutDiaryEditItem,
                    includeItem4.motionLayoutDiaryEditItem,
                    includeItem5.motionLayoutDiaryEditItem,
                )
            }.also { motionLayoutDiaryEditItems ->
                val arraySize = motionLayoutDiaryEditItems.size
                itemMotionLayoutListeners =
                    Array(arraySize) { init ->
                        ItemMotionLayoutListener(
                            arraySize,
                            init + 1,
                            { mainViewModel.onDiaryItemInvisibleStateTransitionCompleted(it) },
                            { mainViewModel.onDiaryItemVisibleStateTransitionCompleted() },
                            { selectItemMotionLayout(it) },
                            { dx, dy, scrollDurationMs ->
                                binding.nestedScrollFullScreen.smoothScrollBy(dx, dy, scrollDurationMs)
                            },
                            {
                                binding.includeItem1.linerLayoutDiaryEditItem.height
                            }
                        ).also {
                            motionLayoutDiaryEditItems[init].setTransitionListener(it)
                        }
                    }
            }

    }

    private class ItemMotionLayoutListener(
        private val maxItemNumber: Int,
        private val itemNumber: Int,
        private val onDiaryItemTransitionToInvisibleCompleted: (Int) -> Unit,
        private val onDiaryItemTransitionToVisibleCompleted: (Int) -> Unit,
        private val processSelectionItemMotionLayout: (Int) -> MotionLayout?,
        private val processSmoothScroll: (dx: Int, dy: Int, scrollDurationMs: Int) -> Unit,
        private val processItemHeightGetting: () -> Int,
    ): MotionLayout.TransitionListener {

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
            val motionLayout = processSelectionItemMotionLayout(nextItemNumber) ?: return true
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
            val itemHeight = processItemHeightGetting()
            val scrollY =
                if (isUpDirection) {
                    itemHeight
                } else {
                    -itemHeight
                }
            processSmoothScroll(0, scrollY, scrollTimeMotionLayoutTransition)
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
    //endregion

    //region Motion Layout Manipulation
    private fun renderItemLayouts(numVisibleItems: Int) {
        Log.d(logTag, "setUpItemsLayout()_numItems = $numVisibleItems")

        // MEMO:削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
        if (shouldTransitionItemMotionLayout) {
            shouldTransitionItemMotionLayout = false
            val currentNumVisibleItems = countVisibleItems()
            val differenceValue = numVisibleItems - currentNumVisibleItems
            if (numVisibleItems > currentNumVisibleItems && differenceValue == 1) {
                transitionDiaryItemToVisible(numVisibleItems, false)
                return
            }
        }

        itemMotionLayouts?.let {
            for (i in it.indices) {
                val itemNumber = i + 1
                if (itemNumber <= numVisibleItems) {
                    transitionDiaryItemToVisible(itemNumber, true)
                } else {
                    transitionDiaryItemToInvisible(itemNumber, true)
                }
            }
        }
    }

    private fun transitionDiaryItemToInvisible(itemNumber: Int, isJump: Boolean) {
        Log.d("logTag", "transitionDiaryItemToInvisible()_itemNumber = $itemNumber, isJump = $isJump")
        val itemMotionLayoutListener = selectItemMotionLayoutListener(itemNumber) ?: return
        val itemMotionLayout = selectItemMotionLayout(itemNumber) ?: return
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
        val itemMotionLayoutListener = selectItemMotionLayoutListener(itemNumber) ?: return
        val itemMotionLayout = selectItemMotionLayout(itemNumber) ?: return
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

    private fun selectItemMotionLayout(itemNumber: Int): MotionLayout? {
        val arrayNumber = itemNumber - 1
        return itemMotionLayouts?.let {
            if (arrayNumber in it.indices) {
                it[arrayNumber]
            } else {
                null
            }
        }
    }

    private fun selectItemMotionLayoutListener(itemNumber: Int): ItemMotionLayoutListener? {
        val arrayNumber = itemNumber - 1
        return itemMotionLayoutListeners?.let {
            if (arrayNumber in it.indices) {
                it[arrayNumber]
            } else {
                null
            }
        }
    }

    private fun countVisibleItems(): Int {
        return itemMotionLayouts?.let {
            it.count { motionLayout ->
                motionLayout.currentState == R.id.motion_scene_edit_diary_item_visible_state
            }
        } ?: 0
    }
    //endregion

    //region Navigation Helpers
    private fun navigateDiaryShowFragment(id: String, date: LocalDate) {
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるdirectionsを切り替える。
        val containsDiaryShowFragment =
            try {
                findNavController().getBackStackEntry(R.id.navigation_diary_show_fragment)
                true
            } catch (_: IllegalArgumentException) {
                false
            }

        val directions = if (containsDiaryShowFragment) {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPopUpToDiaryShow(id, date)
        } else {
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToDiaryShowFragmentPopUpToDiaryEdit(id, date)
        }
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemTitleEditFragment(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemTitleEditDialog(
                diaryItemTitleSelection
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryUpdateDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryUpdateDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryDeleteDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDatePickerDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateWeatherInfoFetchDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemDeleteDialog(itemNumber: Int) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber)
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

    private fun navigateExitWithoutDiarySaveConfirmationDialog() {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToExitWithoutDiarySaveConfirmationDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigatePreviousFragmentOnDiaryDelete(result: FragmentResult.Some<LocalDate>) {
        val destinationId =
            try {
                findNavController().getBackStackEntry(R.id.navigation_calendar_fragment)
                R.id.navigation_calendar_fragment
            } catch (_: IllegalArgumentException) {
                R.id.navigation_diary_list_fragment
            }
        navigateFragmentWithRetry(
            NavigationCommand.PopTo(
                destinationId,
                false,
                RESULT_KEY,
                result
            )
        )
    }
    //endregion

    //region Permission Handling
    private fun checkAccessLocationPermissionBeforeWeatherInfoFetch() {
        mainViewModel.onAccessLocationPermissionChecked(
            requireContext().isAccessLocationGranted()
        )
    }
    //endregion

    internal companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryEditFragment::class.java.name
    }
}
