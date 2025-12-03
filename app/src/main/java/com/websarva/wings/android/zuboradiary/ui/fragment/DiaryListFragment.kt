package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.StandardDiaryListAdapter
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryListViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.DiaryListSetupHelper
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.ConfirmationDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet.ListPickersDialogFragment
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.SwipeBackgroundButtonInteractionHelper
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ConfirmationDialogArgs
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ListPickerConfig
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ListPickersArgs
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ListPickersResult
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Month
import java.time.Year

/**
 * 日記を一覧表示をするフラグメント。
 *
 * 以下の責務を持つ:
 * - データベースから日記を検索し、一覧表示する
 * - スクロールに応じた追加の日記読み込み
 * - 日記リストアイテムのスワイプによる削除機能
 * - 日記リストアイテムをタップした際の日記表示画面への遷移
 * - 新しい日記を作成するための画面遷移
 * - ワード検索画面への遷移
 */
@AndroidEntryPoint
class DiaryListFragment :
    BaseFragment<FragmentDiaryListBinding, DiaryListUiEvent>(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryListViewModel by viewModels()
    
    override val destinationId = R.id.navigation_diary_list_fragment

    /** 日記リストを表示するためのRecyclerViewアダプター。 */
    private var diaryListAdapter: StandardDiaryListAdapter? = null

    /** 日記リスト(RecyclerView)のセットアップを補助するヘルパークラス。 */
    private var diaryListSetupHelper: DiaryListSetupHelper? = null

    /** RecyclerViewのスワイプ操作と背景ボタンのインタラクションを処理するヘルパークラス。 */
    private var swipeBackgroundButtonInteractionHelper: SwipeBackgroundButtonInteractionHelper? = null

    /** 開始年月選択ダイアログで使用する年ピッカーリスト。 */
    private var yearPickerList = listOf(Year.of(LocalDate.now().year))

    /** 開始年月選択ダイアログで使用する月ピッカーリスト。 */
    private val monthPickerList = Month.entries.toList()
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、ツールバー、日記リストの初期設定を行う。*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDiaryList()

        mainViewModel.onUiReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainViewModel.onUiGone()
    }
    //endregion
    
    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryListBinding {
        return FragmentDiaryListBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    /** 追加処理として、リスナ、アダプタ等の解放を行う。*/
    override fun clearViewBindings() {
        binding.materialToolbarTopAppBar.setOnMenuItemClickListener(null)

        binding.recyclerDiaryList.adapter = null
        diaryListAdapter = null

        diaryListSetupHelper?.cleanup()
        diaryListSetupHelper = null

        swipeBackgroundButtonInteractionHelper?.cleanup()
        swipeBackgroundButtonInteractionHelper = null

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        observeDatePickerDialogResult()
        observeDiaryDeleteDialogResult()
    }

    /** 開始年月の選択ダイアログからの結果を監視する。 */
    private fun observeDatePickerDialogResult() {
        observeDialogResult<ListPickersResult>(
            RESULT_KEY_START_YEAR_MONTH_PICKER
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    val selectedYearIndex = result.data.firstPickerValue
                    val selectedMonthIndex =
                        result.data.secondPickerValue
                            ?: throw IllegalStateException("月ピッカーの選択値が`null`")
                    val selectedYear = yearPickerList[selectedYearIndex]
                    val selectedMonth = monthPickerList[selectedMonthIndex]
                    val selectedYearMonth = Year.of(selectedYear.value).atMonth(selectedMonth)
                    mainViewModel.onDatePickerDialogPositiveResultReceived(selectedYearMonth)
                }
                DialogResult.Negative,
                DialogResult.Cancel -> { /*処理なし*/ }
            }
        }
    }

    /** 日記削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_DIARY_DELETE_CONFIRMATION
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
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryListUiEvent) {
        when (event) {
            is DiaryListUiEvent.NavigateDiaryShowScreen -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            is DiaryListUiEvent.NavigateDiaryEditScreen -> {
                navigateDiaryEditFragment(event.id, event.date)
            }
            is DiaryListUiEvent.NavigateWordSearchScreen -> {
                navigateWordSearchFragment()
            }
            is DiaryListUiEvent.ShowStartYearMonthPickerDialog -> {
                navigateStartYearMonthPickerDialog(event.maxYear, event.minYear)
            }
            is DiaryListUiEvent.ShowDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
        }
    }

    override fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent) {
        when (event) {
            ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                scrollDiaryListToFirstPosition()
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeDiaryListItem()
        observeDiaryListSwipeEnabled()
    }

    override fun setupUiEventObservers() {
        super.setupUiEventObservers()

        observeUiEventFromActivity()
    }

    /** 日記リストのデータの変更を監視し、UIを更新する。 */
    private fun observeDiaryListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.diaryList == new.diaryList
            }.map {
                it.diaryList
            }.collect {
                updateDiaryList(it)
            }
        }
    }

    /** 日記リストのスワイプ有効状態の変更を監視し、UIに反映する。 */
    private fun observeDiaryListSwipeEnabled() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.isLoadingOnScrolled.map { !it }.collect {
                updateDiaryListSwipeEnabled(it)
            }
        }
    }

    /** ActivityからのUIイベントを監視する。 */
    private fun observeUiEventFromActivity() {
        fragmentHelper.observeActivityUiEvent(
            this,
            mainActivityViewModel,
            this
        )
    }
    //endregion

    //region CommonUiEventHandler Overrides
    override fun navigatePreviousFragment() {
        navigatePreviousFragmentOnce(FragmentResult.None)
    }
    //endregion

    //region View Setup
    /** ツールバーのメニューアイテムクリックリスナーを設定する。 */
    private fun setupToolbar() {
        binding.materialToolbarTopAppBar
            .setOnMenuItemClickListener { item: MenuItem ->
                // ワード検索フラグメント起動
                if (item.itemId == R.id.diaryListToolbarOptionWordSearch) {
                    mainViewModel.onWordSearchMenuClick()
                    return@setOnMenuItemClickListener true
                }
                false
            }
    }

    /** 日記リストを表示するRecyclerViewの初期設定を行う。 */
    private fun setupDiaryList() {
        val diaryRecyclerView = binding.recyclerDiaryList
        diaryListAdapter = StandardDiaryListAdapter(
            themeColor,
            { mainViewModel.onDiaryListItemClick(it) },
            { mainViewModel.onDiaryListItemDeleteButtonClick(it) }
        ).also { adapter ->
            diaryListSetupHelper = 
                DiaryListSetupHelper(
                    diaryRecyclerView,
                    adapter
                ) { 
                    mainViewModel.onDiaryListEndScrolled()
                }.apply { setup() }

            swipeBackgroundButtonInteractionHelper = 
                SwipeBackgroundButtonInteractionHelper(
                    diaryRecyclerView,
                    adapter
                ).apply { setup() }
        }
    }
    //endregion

    //region View Control
    /** アダプターに新しい日記リストを送信し、UIを更新する。 */
    private fun updateDiaryList(diaryList: DiaryListUi<DiaryListItemContainerUi.Standard>) {
        diaryListAdapter?.submitList(diaryList.itemList) {
            mainViewModel.onDiaryListUpdateCompleted()
        }
    }

    /** 日記リストのスワイプ操作の有効/無効を切り替える。 */
    private fun updateDiaryListSwipeEnabled(isSwipeEnabled: Boolean) {
        swipeBackgroundButtonInteractionHelper?.updateItemSwipeEnabled(isSwipeEnabled)
    }

    /** 日記リストの先頭までスムーズにスクロールする。 */
    private fun scrollDiaryListToFirstPosition() {
        binding.recyclerDiaryList.smoothScrollToPosition(0)
    }
    //endregion

    //region Navigation Helpers
    /**
     * 日記編集画面([DiaryEditFragment])へ遷移する。
     * @param id 編集する日記のID（新規作成の場合はnull）
     * @param date 対象の日付
     *  */
    private fun navigateDiaryEditFragment(id: String?, date: LocalDate) {
        Log.d("20250714", "navigateDiaryEditFragment()")
        val directions = 
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryEditFragment(
                id,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日記表示画面([DiaryShowFragment])へ遷移する。
     * @param id 編集する日記のID（新規作成の場合はnull）
     * @param date 対象の日付
     * */
    private fun navigateDiaryShowFragment(id: String, date: LocalDate) {
        Log.d("20250714", "navigateDiaryShowFragment()")
        val directions = 
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToDiaryShowFragment(id, date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** ワード検索画面([WordSearchFragment])へ遷移する。 */
    private fun navigateWordSearchFragment() {
        Log.d("20250714", "navigateWordSearchFragment()")
        val directions = 
            DiaryListFragmentDirections.actionNavigationDiaryListFragmentToWordSearchFragment()
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 開始年月選択ダイアログ([ListPickersDialogFragment])へ遷移する。 */
    private fun navigateStartYearMonthPickerDialog(newestYear: Year, oldestYear: Year) {
        yearPickerList =
            (oldestYear.value..newestYear.value).map {
                Year.of(it)
            }

        val args = ListPickersArgs(
            resultKey = RESULT_KEY_START_YEAR_MONTH_PICKER,
            pickerConfigs = listOf(
                ListPickerConfig(
                    items = yearPickerList.map { it.toString() },
                    initialIndex = yearPickerList.size - 1
                ),
                ListPickerConfig(
                    items = monthPickerList.map { it.value.toString() },
                    initialIndex = monthPickerList.size - 1
                )
            )
        )
        val directions =
            MobileNavigationDirections.actionGlobalToListPickersDialog(args)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /** 日記削除確認ダイアログ([ConfirmationDialogFragment])へ遷移する。 */
    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val args = ConfirmationDialogArgs(
            resultKey = RESULT_KEY_DIARY_DELETE_CONFIRMATION,
            titleRes = R.string.dialog_diary_delete_title,
            messageText = getString(
                R.string.dialog_diary_delete_message,
                date.formatDateString(requireContext())
            )
        )
        val directions = MobileNavigationDirections.actionGlobalToConfirmationDialog(args)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }
    //endregion

    internal companion object {
        /** 開始年月選択ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_START_YEAR_MONTH_PICKER = "start_year_month_picker_result"

        /** 日記削除の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_DIARY_DELETE_CONFIRMATION = "diary_delete_confirmation_result"
    }
}
