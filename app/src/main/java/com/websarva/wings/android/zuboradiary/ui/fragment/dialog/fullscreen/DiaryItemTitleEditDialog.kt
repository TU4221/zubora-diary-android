package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.DialogDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryItemTitleEditViewModel
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.SwipeSimpleInteractionHelper
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryItemTitleSelectionHistoryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

/**
 * 日記の項目タイトルを編集、または過去の履歴から選択するための全画面ダイアログ。
 *
 * 以下の責務を持つ:
 * - 項目タイトルの直接編集
 * - 過去に使用したタイトル履歴の表示と選択
 * - 履歴のスワイプによる削除機能
 * - 選択、または編集したタイトルを呼び出し元に返す
 */
@AndroidEntryPoint
class DiaryItemTitleEditDialog :
    BaseFullScreenDialogFragment<DialogDiaryItemTitleEditBinding, DiaryItemTitleEditUiEvent>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryItemTitleEditViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_item_title_edit_dialog

    /** 項目タイトル選択履歴を表示するためのRecyclerViewアダプター。 */
    private var selectionHistoryListAdapter: DiaryItemTitleSelectionHistoryListAdapter? = null

    /** 履歴リストのスワイプ操作を補助するヘルパークラス。 */
    private var swipeSimpleInteractionHelper: SwipeSimpleInteractionHelper? = null
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、項目タイトル選択履歴のデータの監視と初期設定を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupItemTitleSelectionHistory()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup?
    ): DialogDiaryItemTitleEditBinding {
        return DialogDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun clearViewBindings() {
        binding.recyclerItemTitleSelectionHistory.adapter = null
        selectionHistoryListAdapter = null

        swipeSimpleInteractionHelper?.cleanup()
        swipeSimpleInteractionHelper = null

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        observeDiaryItemTitleDeleteDialogResult()
    }

    /** 履歴項目削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryItemTitleDeleteDialogResult() {
        observeDialogResult(
            DiaryItemTitleSelectionHistoryDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryItemTitleSelectionHistoryDeleteDialogResultReceived(result)
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryItemTitleEditUiEvent) {
        when (event) {
            DiaryItemTitleEditUiEvent.CloseSwipedItem -> {
                swipeSimpleInteractionHelper?.closeSwipedItem()
            }

            is DiaryItemTitleEditUiEvent.CompleteEdit -> {
                completeItemTitleEdit(
                    event.diaryItemTitleSelection
                )
            }

            is DiaryItemTitleEditUiEvent.NavigateSelectionHistoryItemDeleteDialog -> {
                navigateDiaryItemTitleDeleteDialog(event.itemTitle)
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeItemTitleSelectionHistoryListItem()
    }

    /** 項目タイトル選択履歴リストのデータの変更を監視し、UIを更新する。 */
    private fun observeItemTitleSelectionHistoryListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.titleSelectionHistoriesLoadState == new.titleSelectionHistoriesLoadState
            }.mapNotNull { (it.titleSelectionHistoriesLoadState as? LoadState.Success)?.data }
                .collect { selectionHistoryListAdapter?.submitList(it.itemList) }
        }
    }
    //endregion

    //region CommonUiEventHandler Overrides
    override fun <T> navigatePreviousFragment(resultData: T?) {
        navigatePreviousFragment()
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditDialogDirections.actionDiaryItemTitleEditDialogToAppMessageDialog(
                appMessage
            )
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion

    //region View Setup
    /** 項目タイトル選択履歴を表示するRecyclerViewの初期設定を行う。 */
    private fun setupItemTitleSelectionHistory() {
        val recyclerView = binding.recyclerItemTitleSelectionHistory
        selectionHistoryListAdapter =
            DiaryItemTitleSelectionHistoryListAdapter(
                themeColor
            ) {
                mainViewModel.onDiaryItemTitleSelectionHistoryListItemClick(it)
            }.also { listAdapter ->
                with(recyclerView) {
                    adapter = listAdapter
                    layoutManager = LinearLayoutManager(context)
                    addItemDecoration(
                        DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    )
                }

                swipeSimpleInteractionHelper = SwipeSimpleInteractionHelper(
                    recyclerView,
                    listAdapter
                ) { position ->
                    val item = listAdapter.getItemAt(position)
                    mainViewModel.onDiaryItemTitleSelectionHistoryListItemSwipe(item)
                }.apply { setup() }
            }
    }
    //endregion

    //region Navigation Helpers
    /**
     * 編集/選択したタイトルを結果として設定し、ダイアログを閉じる。
     * @param diaryItemTitleSelection 呼び出し元に返すタイトル選択情報
     */
    private fun completeItemTitleEdit(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        navigatePreviousFragment(
            FragmentResult.Some(RESULT_KEY, diaryItemTitleSelection)
        )
    }

    /**
     * 履歴項目削除確認ダイアログ([DiaryItemTitleSelectionHistoryDeleteDialogFragment])へ遷移する。
     * @param itemTitle 削除対象の項目タイトル
     */
    private fun navigateDiaryItemTitleDeleteDialog(itemTitle: String) {
        val directions =
            DiaryItemTitleEditDialogDirections
                .actionDiaryItemTitleEditDialogToDiaryItemTitleSelectionHistoryDeleteDialog(itemTitle)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }
    //endregion

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryItemTitleEditDialog::class.java.name
    }
}
