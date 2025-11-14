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
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryItemTitleDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

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

    private lateinit var selectionHistoryListAdapter: DiaryItemTitleSelectionHistoryListAdapter

    private var swipeSimpleInteractionHelper: SwipeSimpleInteractionHelper? = null
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeItemTitleSelectionHistoryListItem()
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
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        setupDiaryItemTitleDeleteDialogResultReceiver()
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private fun setupDiaryItemTitleDeleteDialogResultReceiver() {
        observeDialogResult(
            DiaryItemTitleDeleteDialogFragment.RESULT_KEY
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

    override fun onCommonUiEventReceived(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.NavigatePreviousFragment<*> -> {
                navigatePreviousFragment()
            }

            is CommonUiEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
        }
    }

    private fun observeItemTitleSelectionHistoryListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.titleSelectionHistoriesLoadState == new.titleSelectionHistoriesLoadState
            }.mapNotNull {
                (it.titleSelectionHistoriesLoadState as? LoadState.Success)?.data
            }.collect {
                selectionHistoryListAdapter.submitList(it.itemList)
            }
        }
    }
    //endregion

    //region View Setup
    private fun setupItemTitleSelectionHistory() {
        selectionHistoryListAdapter =
            DiaryItemTitleSelectionHistoryListAdapter(
                themeColor
            ) { mainViewModel.onDiaryItemTitleSelectionHistoryListItemClick(it) }

        val recyclerView = binding.recyclerItemTitleSelectionHistory.apply {
            adapter = selectionHistoryListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        swipeSimpleInteractionHelper = SwipeSimpleInteractionHelper(
            recyclerView,
            selectionHistoryListAdapter
        ) {
            val item = selectionHistoryListAdapter.getItemAt(it)
            mainViewModel.onDiaryItemTitleSelectionHistoryListItemSwipe(item)
        }.apply { setup() }
    }
    //endregion

    //region Navigation Helpers
    // DiaryItemTitleEditDialogを閉じる
    private fun completeItemTitleEdit(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        navigatePreviousFragment(
            RESULT_KEY,
            FragmentResult.Some(diaryItemTitleSelection)
        )
    }

    private fun navigateDiaryItemTitleDeleteDialog(itemTitle: String) {
        val directions =
            DiaryItemTitleEditDialogDirections
                .actionDiaryItemTitleEditDialogToDiaryItemTitleDeleteDialog(itemTitle)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditDialogDirections.actionDiaryItemTitleEditDialogToAppMessageDialog(
                appMessage
            )
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion

    internal companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryItemTitleEditDialog::class.java.name
    }
}
