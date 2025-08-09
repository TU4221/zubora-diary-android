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
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.ItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryItemTitleDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class DiaryItemTitleEditDialog :
    BaseFullScreenDialogFragment<DialogDiaryItemTitleEditBinding, DiaryItemTitleEditEvent>() {

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

        receiveDiaryItemTitleEditData()
        setUpToolBar()
        setUpItemTitleInputField()
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

    override fun onMainUiEventReceived(event: DiaryItemTitleEditEvent) {
        when (event) {
            DiaryItemTitleEditEvent.CloseSwipedItem -> {
                val adapter =
                    checkNotNull(
                        binding.recyclerItemTitleSelectionHistory.adapter
                    ) as ItemTitleSelectionHistoryListAdapter
                adapter.closeSwipedItem()
            }
            is DiaryItemTitleEditEvent.CompleteEdit -> {
                completeItemTitleEdit(
                    event.diaryItemTitle
                )
            }
            is DiaryItemTitleEditEvent.NavigateSelectionHistoryItemDeleteDialog -> {
                navigateDiaryItemTitleDeleteDialog(
                    event.parameters
                )
            }
            is DiaryItemTitleEditEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        navigatePreviousFragment()
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    // EditDiaryFragmentからデータ受取
    private fun receiveDiaryItemTitleEditData() {
        val diaryItemTitle =
            DiaryItemTitleEditDialogArgs.fromBundle(requireArguments()).diaryItemTitle
        mainViewModel
            .onDiaryItemTitleDataReceived(diaryItemTitle)
    }

    private fun setUpToolBar() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.itemNumber.filterNotNull().collectLatest { itemNumber ->
                val toolbarTitle =
                    getString(
                        R.string.fragment_diary_item_title_edit_toolbar_title,
                        itemNumber
                    )
                binding.materialToolbarTopAppBar.title = toolbarTitle
            }
        }
    }

    private fun setUpItemTitleInputField() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.itemTitle.collectLatest {
                mainViewModel.onItemTitleChanged(it)
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.itemTitleInputTextValidationResult.collectLatest {
                binding.textInputLayoutNewItemTitle.error =
                    when (it) {
                        InputTextValidationResult.Valid -> {
                            null
                        }
                        InputTextValidationResult.InvalidEmpty -> {
                            getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_empty)
                        }
                        InputTextValidationResult.InvalidInitialCharUnmatched -> {
                            getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched)
                        }
                    }
            }
        }
    }

    private fun setUpItemTitleSelectionHistory() {
        val itemTitleSelectionHistoryListAdapter =
            ItemTitleSelectionHistoryListAdapter(
                requireContext(),
                binding.recyclerItemTitleSelectionHistory,
                themeColor
            )
        itemTitleSelectionHistoryListAdapter.build()
        itemTitleSelectionHistoryListAdapter.setOnClickItemListener { itemTitle: String ->
            mainViewModel.onDiaryItemTitleSelectionHistoryItemClick(itemTitle)
        }
        itemTitleSelectionHistoryListAdapter
            .setOnClickDeleteButtonListener { itemTitle: String ->
                mainViewModel
                    .onDiaryItemTitleSelectionHistoryItemDeleteButtonClick(
                        itemTitle
                    )
            }

        // 選択履歴読込・表示
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.itemTitleSelectionHistoryList
                .collectLatest { value: SelectionHistoryList ->
                    val adapter =
                        checkNotNull(
                            binding.recyclerItemTitleSelectionHistory.adapter
                        ) as ItemTitleSelectionHistoryListAdapter
                    adapter.submitList(value.itemList)
                }
        }
    }

    // DiaryItemTitleEditDialogを閉じる
    private fun completeItemTitleEdit(diaryItemTitle: DiaryItemTitle) {
        val navBackStackEntry = checkNotNull(findNavController().previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_RESULT] = FragmentResult.Some(diaryItemTitle)
        navigatePreviousFragment()
    }

    private fun navigateDiaryItemTitleDeleteDialog(
        parameters: DiaryItemTitleSelectionHistoryItemDeleteParameters
    ) {
        val directions =
            DiaryItemTitleEditDialogDirections.actionDiaryItemTitleEditDialogToDiaryItemTitleDeleteDialog(
                parameters
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditDialogDirections.actionDiaryItemTitleEditDialogToAppMessageDialog(
                appMessage
            )
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    override fun clearViewBindings() {
        val adapter =
            binding.recyclerItemTitleSelectionHistory.adapter as ItemTitleSelectionHistoryListAdapter
        adapter.clearViewBindings()

        super.clearViewBindings()
    }
}
