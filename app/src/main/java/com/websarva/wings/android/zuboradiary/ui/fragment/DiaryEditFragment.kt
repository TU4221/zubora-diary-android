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
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.collections.map

/**
 * 日記の作成、編集を行うフラグメント。
 *
 * 以下の責務を持つ:
 * - 新規日記の作成、または既存日記の編集
 * - 日記の日付、天気、体調、タイトル、項目タイトル、項目コメント、画像の編集
 * - 日記項目の追加、削除、および表示順の制御
 * - 編集内容の保存、または保存せずに終了する際の確認処理
 */
@AndroidEntryPoint
class DiaryEditFragment : BaseFragment<FragmentDiaryEditBinding, DiaryEditUiEvent>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryEditViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_edit_fragment

    /** 日記項目レイアウトのトランジションアニメーション時間(ms)。 */
    private val motionLayoutTransitionTime = 500 /*ms*/

    /** 日記項目レイアウトのトランジションを即時完了させるための時間(ms)。 */
    private val motionLayoutJumpTime = 1 /*ms*/

    /** 各日記項目のMotionLayoutインスタンスを保持する配列。 */
    private var itemMotionLayouts: Array<MotionLayout>? = null

    /** 各日記項目のMotionLayoutリスナーを保持する配列。 */
    private var itemMotionLayoutListeners: Array<ItemMotionLayoutListener>? = null

    /** ソフトウェアキーボードを制御するマネージャークラス。 */
    private lateinit var keyboardManager: KeyboardManager

    /** 画面の高さを取得する。 */
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

    /** 端末のギャラリーから画像を選択した結果を処理するランチャー。 */
    // MEMO:端末ギャラリーから画像Uri取得。画像未選択時、nullを受け取る。
    private val openDocumentResultLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        mainViewModel.onOpenDocumentImageUriResultReceived(uri)
    }
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、キーボード、ツールバー、および項目レイアウトの初期設定を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboard()
        setupToolbar()
        setupItemMotionLayouts()
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

    /** 追加処理として、各種リスナーとアダプターの解放を行う。 */
    override fun clearViewBindings() {
        binding.materialToolbarTopAppBar.setOnMenuItemClickListener(null)

        binding.autoCompleteTextWeather1.setAdapter(null)
        binding.autoCompleteTextWeather2.setAdapter(null)
        binding.autoCompleteTextCondition.setAdapter(null)

        itemMotionLayouts?.forEach { it.setTransitionListener(null) }
        itemMotionLayouts = null
        itemMotionLayoutListeners = null

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        observeDiaryItemTitleEditDialogResult()
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

    /** 日記項目タイトル編集ダイアログからの結果を監視する。 */
    private fun observeDiaryItemTitleEditDialogResult() {
        observeFragmentResult(
            DiaryItemTitleEditDialog.RESULT_KEY
        ) { result ->
            when (result) {
                is FragmentResult.Some -> {
                    mainViewModel.onItemTitleEditDialogPositiveResultReceived(result.data)
                }
                FragmentResult.None -> { /*処理なし*/ }
            }
        }
    }

    /** 既存日記読込ダイアログからの結果を監視する。 */
    private fun observeDiaryLoadDialogResult() {
        observeDialogResult<Unit>(
            DiaryLoadDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDiaryLoadDialogPositiveResultReceived()
                }
                is DialogResult.Negative,
                is DialogResult.Cancel -> {
                    mainViewModel.onDiaryLoadDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 日記読込失敗確認ダイアログからの結果を監視する。 */
    private fun observeDiaryLoadFailureDialogResult() {
        observeDialogResult<Unit>(
            DiaryLoadFailureDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive<Unit>,
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryLoadFailureDialogResultReceived()
                }
            }
        }
    }

    /** 既存日記上書きダイアログからの結果を監視する。 */
    private fun observeDiaryUpdateDialogResult() {
        observeDialogResult<Unit>(
            DiaryUpdateDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDiaryUpdateDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryUpdateDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 日記削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult<Unit>(
            DiaryDeleteDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDiaryDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryDeleteDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 日付選択ダイアログからの結果を監視する。 */
    private fun observeDatePickerDialogResult() {
        observeDialogResult(
            DatePickerDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDatePickerDialogPositiveResultReceived(result.data)
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDatePickerDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 天気情報読込ダイアログからの結果を監視する。 */
    private fun observeUpWeatherInfoFetchDialogResult() {
        observeDialogResult<Unit>(
            WeatherInfoFetchDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onWeatherInfoFetchDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onWeatherInfoFetchDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 項目削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryItemDeleteDialogResult() {
        observeDialogResult<Unit>(
            DiaryItemDeleteDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDiaryItemDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryItemDeleteDialogNegativeResultReceived()
                }
            }
        }
    }

    /** 添付画像削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryImageDeleteDialogResult() {
        observeDialogResult<Unit>(
            DiaryImageDeleteDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive<Unit> -> {
                    mainViewModel.onDiaryImageDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /*処理なし*/ }
            }
        }
    }

    /** 未保存終了確認ダイアログからの結果を監視する。 */
    private fun observeExitWithoutDiarySaveDialogResult() {
        observeDialogResult<Unit>(
            ExitWithoutDiarySaveDialogFragment.RESULT_KEY
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onExitWithoutDiarySaveDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onExitWithoutDiarySaveDialogNegativeResultReceived()
                }
            }
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryEditUiEvent) {
        when (event) {
            is DiaryEditUiEvent.NavigateDiaryShowScreen -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            is DiaryEditUiEvent.NavigatePreviousScreenWithResult -> {
                navigatePreviousFragmentOnce(
                    FragmentResult.Some(RESULT_KEY, event.originalDiaryDate)
                )
            }
            is DiaryEditUiEvent.NavigatePreviousScreenOnDiaryDelete -> {
                navigatePreviousFragmentOnDiaryDelete(event.date)
            }
            is DiaryEditUiEvent.NavigatePreviousScreenOnInitialDiaryLoadFailed -> {
                navigatePreviousFragmentWithRetry(FragmentResult.None)
            }
            is DiaryEditUiEvent.ShowDiaryItemTitleEditDialog -> {
                navigateDiaryItemTitleEditDialog(event.diaryItemTitleSelection)
            }
            is DiaryEditUiEvent.ShowDiaryLoadDialog -> {
                navigateDiaryLoadDialog(event.date)
            }
            is DiaryEditUiEvent.ShowDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
            }
            is DiaryEditUiEvent.ShowDiaryUpdateDialog -> {
                navigateDiaryUpdateDialog(event.date)
            }
            is DiaryEditUiEvent.ShowDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
            is DiaryEditUiEvent.ShowDatePickerDialog -> {
                navigateDatePickerDialog(event.date)
            }
            is DiaryEditUiEvent.ShowWeatherInfoFetchDialog -> {
                navigateWeatherInfoFetchDialog(event.date)
            }
            is DiaryEditUiEvent.ShowDiaryItemDeleteDialog -> {
                navigateDiaryItemDeleteDialog(event.itemNumber)
            }
            DiaryEditUiEvent.ShowDiaryImageDeleteDialog -> {
                navigateDiaryImageDeleteDialog()
            }
            is DiaryEditUiEvent.ShowExitWithoutDiarySaveDialog -> {
                navigateExitWithoutDiarySaveDialog()
            }
            is DiaryEditUiEvent.ShowImageSelectionGallery -> {
                openDocumentResultLauncher.launch(arrayOf("image/*"))
            }
            is DiaryEditUiEvent.TransitionDiaryItemToVisible -> {
                transitionDiaryItemToVisible(event.itemNumber, false)
            }
            is DiaryEditUiEvent.TransitionDiaryItemToInvisible -> {
                transitionDiaryItemToInvisible(event.itemNumber, false)
            }
            is DiaryEditUiEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch -> {
                checkAccessLocationPermissionBeforeWeatherInfoFetch()
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeToolbarMenuState()
        observeWeather1DropdownOptions()
        observeWeather2DropdownOptions()
        observeConditionDropdownOptions()
        observeDiaryItem()
    }

    /** ツールバーメニューの表示状態を監視する。 */
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

    /** 天気1のドロップダウンメニューの選択肢を監視する。 */
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

    /** 天気2のドロップダウンメニューの選択肢を監視する。 */
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

    /** 体調のドロップダウンメニューの選択肢を監視する。 */
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

    /** 日記項目数を監視する。 */
    private fun observeDiaryItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.numVisibleDiaryItems == new.numVisibleDiaryItems
            }.map {
                it.numVisibleDiaryItems
            }.collect {
                if (it == countVisibleItems() && validateVisibleItemStatesContinuity()) return@collect
                renderItemLayouts(it)
            }
        }
    }
    //endregion

    //region CommonUiEventHandler Overrides
    override fun navigatePreviousFragment() {
        navigatePreviousFragmentOnce(FragmentResult.None)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion

    //region View Setup
    /** ツールバーのメニューアイテムクリックリスナーを設定する。 */
    private fun setupToolbar() {
        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // 日記保存、削除
                when (item.itemId) {
                    R.id.diaryEditToolbarOptionSaveDiary -> {
                        mainViewModel.onDiarySaveMenuClick()
                        true
                    }

                    R.id.diaryEditToolbarOptionDeleteDiary -> {
                        mainViewModel.onDiaryDeleteMenuClick()
                        true
                    }

                    R.id.diaryEditToolbarOptionTest -> {
                        mainViewModel.test()
                        true
                    }

                    else -> false
                }
            }
    }

    /** キーボード操作を管理するクラスを初期化する。 */
    private fun setupKeyboard() {
        keyboardManager = KeyboardManager(requireContext()).apply {
            registerKeyboardStateListener(this@DiaryEditFragment) { isVisible ->
                if (!isVisible) return@registerKeyboardStateListener

                ensureFocusedViewIsVisible()
            }
        }
    }

    /**
     * キーボードが表示された際に、フォーカスされたViewが隠れないように画面をスクロールさせる。
     *
     * このアプリは`AndroidManifest.xml`で`windowSoftInputMode="adjustNothing"`に設定されているため、
     * キーボード表示時にレイアウトが自動調整されない。
     * 本メソッドは、その副作用で入力欄がキーボードに隠れる問題を解決するために、
     * フォーカス位置まで手動でスクロールを実行する。
     */
    private fun ensureFocusedViewIsVisible() {
        val focusView = this@DiaryEditFragment.view?.findFocus() ?: return

        val offset = screenHeight / 3
        val location = IntArray(2)
        focusView.getLocationOnScreen(location)
        val positionY = location[1]
        val scrollAmount = positionY - offset

        binding.nestedScrollFullScreen.smoothScrollBy(0, scrollAmount)
    }
    //endregion

    //region View Manipulation
    /**
     * ツールバーのメニューの各項目の有効/無効を切り替える。
     * @param isDeleteEnabled 削除項目が有効場合はtrue
     */
    private fun updateToolbarMenuState(isDeleteEnabled: Boolean) {
        val menu = binding.materialToolbarTopAppBar.menu
        val deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary)
        deleteMenuItem.isEnabled = isDeleteEnabled

        // TODO:テスト用の為、最終的に削除
        val testMenuItem = menu.findItem(R.id.diaryEditToolbarOptionTest)
        testMenuItem.isEnabled = !isDeleteEnabled
    }

    /**
     * 天気1のドロップダウンアダプターを更新する。
     * @param options 表示する天気の選択肢
     */
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

    /**
     * 天気2のドロップダウンアダプターを更新する。
     * @param options 表示する天気の選択肢
     */
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

    /**
     * 体調のドロップダウンアダプターを更新する。
     * @param options 表示する体調の選択肢
     */
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
    /**
     * 日記項目に対応する[MotionLayout]の配列とそのリスナーを初期化し、関連付けを行う。
     */
    private fun setupItemMotionLayouts() {
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
                            { mainViewModel.onDiaryItemVisibleStateTransitionCompleted(it) },
                            { selectItemMotionLayout(it) },
                            { dx, dy, scrollDurationMs ->
                                binding.nestedScrollFullScreen.smoothScrollBy(dx, dy, scrollDurationMs)
                            },
                            {
                                binding.includeItem1.motionLayoutDiaryEditItem.height
                            }
                        ).also {
                            motionLayoutDiaryEditItems[init].setTransitionListener(it)
                        }
                    }
            }

    }

    /**
     * 各日記項目の[MotionLayout]における遷移イベントを監視するリスナー。
     * MotionLayoutのアニメーション完了を検知し、状態に応じて
     * スクロール処理の実行やViewModelへのイベント通知を行う責務を持つ。
     * また、アニメーションがスムーズな遷移か、ジャンプによる状態変化かを判別するための状態も管理する。
     *
     * @property maxItemNumber 全日記項目の最大数。
     * @property itemNumber このリスナーが担当する日記項目の番号。
     * @property onDiaryItemTransitionToInvisibleCompleted 項目が「非表示」状態への遷移を完了したときに呼び出されるコールバック。
     * @property onDiaryItemTransitionToVisibleCompleted 項目が「表示」状態への遷移を完了したときに呼び出されるコールバック。
     * @property selectItemMotionLayout 指定された項目番号のMotionLayoutを取得するためのコールバック。
     * @property smoothScroll 画面全体をスムーズスクロールさせるためのコールバック。
     * @property getItemHeight 1項目あたりの高さを取得するためのコールバック。
     */
    private class ItemMotionLayoutListener(
        private val maxItemNumber: Int,
        private val itemNumber: Int,
        private val onDiaryItemTransitionToInvisibleCompleted: (Int) -> Unit,
        private val onDiaryItemTransitionToVisibleCompleted: (Int) -> Unit,
        private val selectItemMotionLayout: (Int) -> MotionLayout?,
        private val smoothScroll: (dx: Int, dy: Int, scrollDurationMs: Int) -> Unit,
        private val getItemHeight: () -> Int,
    ): MotionLayout.TransitionListener {

        private val scrollTimeMotionLayoutTransition = 1000 /*ms*/

        /** アニメーションがスムーズな遷移（ユーザー操作によるもの）か、ジャンプ（初期化など）かを区別するためのフラグ。 */
        private var isTriggeredBySmooth = false

        /**
         * 現在の遷移をスムーズなアニメーションとしてマークする。
         * ユーザー操作による項目追加/削除アニメーションの開始前に呼び出す。
         */
        fun markTransitionAsSmooth() {
            isTriggeredBySmooth = true
        }

        /**
         * 現在の遷移をジャンプ（アニメーションなし）としてマークする。
         * 画面初期化時など、アニメーションを伴わない状態変化の際に呼び出す。
         */
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

        /**
         * この項目の次の項目が「非表示」状態であるかを確認する。
         *
         * 本リスナの対象が最後の項目の場合は常にtrueを返す。
         * これは、項目削除時に不要なスクロールが発生するのを防ぐために使用される。
         */
        private fun isNextItemInvisibleState(): Boolean {
            if (itemNumber == maxItemNumber) return true
            val nextItemNumber = itemNumber.inc()
            val motionLayout = selectItemMotionLayout(nextItemNumber) ?: return true
            return motionLayout.currentState == R.id.motion_scene_edit_diary_item_invisible_state
        }

        /**
         * 日記項目が「表示」状態に遷移した後のスクロール処理を実行する。
         */
        private fun scrollOnDiaryItemTransitionToVisible() {
            scrollOnDiaryItemTransition(true)
        }

        /**
         * 日記項目が「非表示」状態に遷移した後のスクロール処理を実行する。
         */
        private fun scrollOnDiaryItemTransitionToInvisible() {
            scrollOnDiaryItemTransition(false)
        }

        /**
         * 日記項目の高さに基づいて、画面全体をスクロールさせる。
         *
         * @param isUpDirection trueの場合は上方向（項目追加時）、falseの場合は下方向（項目削除時）にスクロールする。
         */
        private fun scrollOnDiaryItemTransition(isUpDirection: Boolean) {
            val itemHeight = getItemHeight()
            val scrollY =
                if (isUpDirection) {
                    itemHeight
                } else {
                    -itemHeight
                }
            smoothScroll(0, scrollY, scrollTimeMotionLayoutTransition)
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
    /**
     * 表示すべき日記項目数に応じて、各項目のレイアウト（表示/非表示）を更新・描画する。
     *
     * このメソッドは、画面初期化時や日記項目数が変更された際に呼び出される。
     * 現在表示されている項目数と引数で渡された項目数を比較し、
     * 差分に応じて各項目を[transitionDiaryItemToVisible]または[transitionDiaryItemToInvisible]へ遷移させる。
     *
     * @param numVisibleItems 表示すべき日記項目の総数。
     */
    private fun renderItemLayouts(numVisibleItems: Int) {
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

    /**
     * 指定された番号の日記項目を「非表示」状態へ遷移させる。
     *
     * @param itemNumber 遷移させる日記項目の番号。
     * @param isJump trueの場合はアニメーションなし（ジャンプ）、falseの場合はアニメーションあり（スムーズ）で遷移する。
     */
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

    /**
     * 指定された番号の日記項目を「表示」状態へ遷移させる。
     *
     * @param itemNumber 遷移させる日記項目の番号。
     * @param isJump trueの場合はアニメーションなし（ジャンプ）、falseの場合はアニメーションあり（スムーズ）で遷移する。
     */
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

    /**
     * 指定された項目番号に対応する[MotionLayout]を返す。
     *
     * @param itemNumber 取得したい日記項目の番号。
     * @return 対応するMotionLayout。見つからなければnull。
     */
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

    /**
     * 指定された項目番号に対応する[ItemMotionLayoutListener]を返す。
     *
     * @param itemNumber 取得したい日記項目の番号。
     * @return 対応するItemMotionLayoutListener。見つからなければnull。
     */
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

    /**
     * 現在「表示」状態にある日記項目の数を数える。
     *
     * @return 表示状態のMotionLayoutの数。
     */
    private fun countVisibleItems(): Int {
        return itemMotionLayouts?.let {
            it.count { motionLayout ->
                motionLayout.currentState == R.id.motion_scene_edit_diary_item_visible_state
            }
        } ?: 0
    }

    /**
     * 表示されている日記項目の連続性を検証する。
     * 最初の項目が表示状態であり、かつ表示されている項目間に非表示のものが挟まっていないことを確認する。
     * @return 検証を通過した場合は`true`、それ以外は`false`。
     */
    private fun validateVisibleItemStatesContinuity(): Boolean {
        val layouts = itemMotionLayouts ?: return false
        if (layouts.isEmpty()) return false

        // 最初の項目が「表示」状態でなければならない
        if (layouts.first().currentState != R.id.motion_scene_edit_diary_item_visible_state) {
            return false
        }

        // 「表示」状態の最後のインデックスを見つける
        val lastVisibleIndex = layouts.indexOfLast {
            it.currentState == R.id.motion_scene_edit_diary_item_visible_state
        }

        // 最初の項目から最後の表示項目まで、全てが表示状態でなければならない
        for (i in 0..lastVisibleIndex) {
            if (layouts[i].currentState != R.id.motion_scene_edit_diary_item_visible_state) {
                return false
            }
        }

        return true
    }
    //endregion

    //region Navigation Helpers
    /**
     * 日記表示画面([DiaryShowFragment])へ遷移する。
     * @param id 表示する日記のID
     * @param date 表示する日記の日付
     */
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

    /**
     * 日記項目タイトル編集ダイアログ([DiaryItemTitleEditDialog])へ遷移する。
     * @param diaryItemTitleSelection 編集対象の日記項目タイトル情報
     */
    private fun navigateDiaryItemTitleEditDialog(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemTitleEditDialog(
                diaryItemTitleSelection
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 既存日記読込ダイアログ([DiaryLoadDialogFragment])へ遷移する。
     * @param date 読み込む日記の日付
     */
    private fun navigateDiaryLoadDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日記読込失敗ダイアログ([DiaryLoadFailureDialogFragment])へ遷移する。
     * @param date 読み込みに失敗した日記の日付
     */
    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 既存日記上書きダイアログ([DiaryUpdateDialogFragment])へ遷移する。
     * @param date 上書きする日記の日付
     */
    private fun navigateDiaryUpdateDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryUpdateDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日記削除確認ダイアログ([DiaryDeleteDialogFragment])へ遷移する。
     * @param date 削除する日記の日付
     */
    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryDeleteDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日付選択ダイアログ([DatePickerDialogFragment])へ遷移する。
     * @param date 初期選択されている日付
     */
    private fun navigateDatePickerDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 天気情報読込ダイアログ([WeatherInfoFetchDialogFragment])へ遷移する。
     * @param date 天気情報を取得する日付
     */
    private fun navigateWeatherInfoFetchDialog(date: LocalDate) {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToWeatherInfoFetchDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 項目削除確認ダイアログ([DiaryItemDeleteDialogFragment])へ遷移する。
     * @param itemNumber 削除する項目の番号
     */
    private fun navigateDiaryItemDeleteDialog(itemNumber: Int) {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 添付画像削除確認ダイアログ([DiaryImageDeleteDialogFragment])へ遷移する。 */
    private fun navigateDiaryImageDeleteDialog() {
        val directions =
            DiaryEditFragmentDirections.actionDiaryEditFragmentToDiaryImageDeleteDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 日記を保存せずに終了することを確認するダイアログ([ExitWithoutDiarySaveDialogFragment])へ遷移する。 */
    private fun navigateExitWithoutDiarySaveDialog() {
        val directions =
            DiaryEditFragmentDirections
                .actionDiaryEditFragmentToExitWithoutDiarySaveDialog()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 前の画面へ戻る（日記削除時専用）。
     * @param date 削除された日記の日付
     */
    private fun navigatePreviousFragmentOnDiaryDelete(date: LocalDate) {
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
                FragmentResult.Some(RESULT_KEY, date)
            )
        )
    }
    //endregion

    //region Permission Handling
    /** 位置情報へのアクセス権限を確認し、結果をViewModelに通知する。 */
    private fun checkAccessLocationPermissionBeforeWeatherInfoFetch() {
        val isGranted = requireContext().isAccessLocationGranted()
        mainViewModel.onAccessLocationPermissionChecked(isGranted)
    }
    //endregion

    internal companion object {
        /** このフラグメントから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryEditFragment::class.java.name
    }
}
