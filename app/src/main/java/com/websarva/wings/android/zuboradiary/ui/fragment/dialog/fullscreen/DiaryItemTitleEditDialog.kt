package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.DialogDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryItemTitleEditViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diaryitemtitle.DiaryItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryItemTitleDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@AndroidEntryPoint
class DiaryItemTitleEditDialog :
    BaseFullScreenDialogFragment<DialogDiaryItemTitleEditBinding, DiaryItemTitleEditUiEvent>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryItemTitleEditDialog::class.java.name
    }

    override val destinationId = R.id.navigation_diary_item_title_edit_dialog

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryItemTitleEditViewModel by viewModels()

    private lateinit var itemTitleSelectionHistoryListAdapter: DiaryItemTitleSelectionHistoryListAdapter

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup?
    ): DialogDiaryItemTitleEditBinding {
        return DialogDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()
        setUpItemTitleSelectionHistory()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryItemTitleDeleteDialogResultReceiver()
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private fun setUpDiaryItemTitleDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryItemTitleDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryItemTitleSelectionHistoryDeleteDialogResultReceived(result)
        }
    }

    override fun onMainUiEventReceived(event: DiaryItemTitleEditUiEvent) {
        when (event) {
            DiaryItemTitleEditUiEvent.CloseSwipedItem -> {
                val adapter =
                    checkNotNull(
                        binding.recyclerItemTitleSelectionHistory.adapter
                    ) as DiaryItemTitleSelectionHistoryListAdapter
                adapter.closeSwipedItem()
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

    override fun onNavigatePreviousFragmentEventReceived(result: FragmentResult<*>) {
        navigatePreviousFragment()
    }

    override fun onNavigateAppMessageEventReceived(appMessage: AppMessage) {
        navigateAppMessageDialog(appMessage)
    }

    private fun observeUiState() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.map {
                it.titleSelectionHistoriesLoadState
            }.mapNotNull {
                (it as? LoadState.Success)?.data
            }.distinctUntilChanged().collectLatest {
                itemTitleSelectionHistoryListAdapter.submitList(it.itemList)
            }
        }
    }

    private fun setUpItemTitleSelectionHistory() {
        itemTitleSelectionHistoryListAdapter =
            DiaryItemTitleSelectionHistoryListAdapter(
                binding.recyclerItemTitleSelectionHistory,
                themeColor
            ).apply {
                build()
                registerOnItemClickListener { item: DiaryItemTitleSelectionHistoryListItemUi ->
                    mainViewModel.onDiaryItemTitleSelectionHistoryListItemClick(item)
                }
                registerOnItemSwipeListener { item: DiaryItemTitleSelectionHistoryListItemUi ->
                    mainViewModel.onDiaryItemTitleSelectionHistoryListItemSwipe(item)
                }
            }
    }

    // DiaryItemTitleEditDialogを閉じる
    private fun completeItemTitleEdit(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        val navBackStackEntry = checkNotNull(findNavController().previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_RESULT] = FragmentResult.Some(diaryItemTitleSelection)
        navigatePreviousFragment()
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
}
