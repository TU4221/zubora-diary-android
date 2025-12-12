package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.DialogDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryItemTitleEditViewModel
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.helper.SwipeSimpleInteractionHelper
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ConfirmationDialogArgs
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DiaryItemTitleEditNavDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
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
class DiaryItemTitleEditDialog : BaseFullScreenDialogFragment<
        DialogDiaryItemTitleEditBinding,
        DiaryItemTitleEditUiEvent,
        DiaryItemTitleEditNavDestination,
        DummyNavBackDestination
>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryItemTitleEditViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_item_title_edit_dialog

    override val resultKey: String get() = RESULT_KEY

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
        binding.includeHistory.recyclerItemTitleSelectionHistory.adapter = null
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
        observeDialogResult<Unit>(
            RESULT_KEY_TITLE_SELECTION_HISTORY_DELETE_CONFIRMATION
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel
                        .onDiaryItemTitleSelectionHistoryDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel
                        .onDiaryItemTitleSelectionHistoryDeleteDialogNegativeResultReceived()
                }
            }
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryItemTitleEditUiEvent) {
        when (event) {
            DiaryItemTitleEditUiEvent.CloseSwipedTitleSelectionHistory -> {
                swipeSimpleInteractionHelper?.closeSwipedItem()
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

    //region View Setup
    /** 項目タイトル選択履歴を表示するRecyclerViewの初期設定を行う。 */
    private fun setupItemTitleSelectionHistory() {
        val recyclerView = binding.includeHistory.recyclerItemTitleSelectionHistory
        selectionHistoryListAdapter =
            DiaryItemTitleSelectionHistoryListAdapter(
                themeColor
            ) {
                mainViewModel.onDiaryItemTitleSelectionHistoryListItemClick(it)
            }.also { listAdapter ->
                recyclerView.apply {
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
    override fun toNavDirections(destination: DiaryItemTitleEditNavDestination): NavDirections {
        return when (destination) {
            is DiaryItemTitleEditNavDestination.AppMessageDialog -> {
                navigationEventHelper.createAppMessageDialogNavDirections(destination.message)
            }
            is DiaryItemTitleEditNavDestination.SelectionHistoryDeleteDialog -> {
                createTitleSelectionHistoryDeleteDialogNavDirections(destination.itemTitle)
            }
        }
    }

    override fun toNavDestinationId(destination: DummyNavBackDestination): Int {
        // 処理なし
        throw IllegalStateException("NavDestinationIdへの変換は不要の為、未対応。")
    }

    /**
     * タイトル選択履歴削除確認ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。。
     * @param itemTitle 削除対象の項目タイトル
     */
    private fun createTitleSelectionHistoryDeleteDialogNavDirections(itemTitle: String): NavDirections {
        val args = ConfirmationDialogArgs(
            resultKey = RESULT_KEY_TITLE_SELECTION_HISTORY_DELETE_CONFIRMATION,
            titleRes = R.string.dialog_diary_item_title_history_delete_title,
            messageText = getString(
                R.string.dialog_diary_item_title_history_delete_message,
                itemTitle
            )
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(args)
    }
    //endregion

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryItemTitleEditDialog::class.java.name

        /** タイトル選択履歴削除の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_TITLE_SELECTION_HISTORY_DELETE_CONFIRMATION =
            "title_selection_history_delete_confirmation_result"
    }
}
