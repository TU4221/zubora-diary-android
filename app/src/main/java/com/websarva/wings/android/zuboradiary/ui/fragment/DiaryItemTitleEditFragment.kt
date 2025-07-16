package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryItemTitleDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryItemTitleEditViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.ItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.view.edittext.TextInputConfigurator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class DiaryItemTitleEditFragment : BaseFragment<FragmentDiaryItemTitleEditBinding>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryItemTitleEditFragment::class.java.name
    }

    override val destinationId = R.id.navigation_diary_item_title_edit_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryItemTitleEditViewModel by viewModels()

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryItemTitleEditBinding {
        return FragmentDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false)
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

    override fun onMainViewModelEventReceived(event: ViewModelEvent) {
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

            ViewModelEvent.NavigatePreviousFragment -> {
                navigatePreviousFragment()
            }
            is ViewModelEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    // EditDiaryFragmentからデータ受取
    private fun receiveDiaryItemTitleEditData() {
        val diaryItemTitle =
            DiaryItemTitleEditFragmentArgs.fromBundle(requireArguments()).diaryItemTitle
        mainViewModel
            .onDiaryItemTitleDataReceivedFromPreviousFragment(diaryItemTitle)
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
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener { mainViewModel.onNavigationClicked() }
    }

    private fun setUpItemTitleInputField() {
        val textInputConfigurator = TextInputConfigurator()
        val textInputLayouts = arrayOf(
            binding.textInputLayoutNewItemTitle
        )
        textInputConfigurator.setUpKeyboardCloseOnEnter(*textInputLayouts)
        textInputConfigurator.setUpFocusClearOnClickBackground(
            binding.root,
            *textInputLayouts
        )
        val transitionListener =
            textInputConfigurator.createClearButtonSetupTransitionListener(*textInputLayouts)
        addTransitionListener(transitionListener)

        val editText = checkNotNull(binding.textInputLayoutNewItemTitle.editText)
        editText.addTextChangedListener(InputItemTitleErrorWatcher())

        binding.buttonNewItemTitleSelection
            .setOnClickListener {
                mainViewModel.onNewDiaryItemTitleSelectionButtonClicked()
            }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.itemTitle.collectLatest {
                binding.buttonNewItemTitleSelection.isEnabled = it.isNotEmpty()
            }
        }
    }

    private inner class InputItemTitleErrorWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // 処理なし
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            binding.apply {
                val title = s.toString()
                if (title.isEmpty()) {
                    textInputLayoutNewItemTitle.error =
                        getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_empty)
                    buttonNewItemTitleSelection.isEnabled = false
                    return
                }
                // 先頭が空白文字(\\s)
                if (title.matches("\\s+.*".toRegex())) {
                    textInputLayoutNewItemTitle.error =
                        getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched)
                    buttonNewItemTitleSelection.isEnabled = false
                    return
                }
                textInputLayoutNewItemTitle.error = null
                buttonNewItemTitleSelection.isEnabled = true
            }

        }

        override fun afterTextChanged(s: Editable) {
            // 処理なし
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
            mainViewModel.onDiaryItemTitleSelectionHistoryItemClicked(itemTitle)
        }
        itemTitleSelectionHistoryListAdapter
            .setOnClickDeleteButtonListener { itemTitle: String ->
                mainViewModel
                    .onDiaryItemTitleSelectionHistoryItemDeleteButtonClicked(
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

    // DiaryItemTitleEditFragmentを閉じる
    private fun completeItemTitleEdit(diaryItemTitle: DiaryItemTitle) {
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_RESULT] = FragmentResult.Some(diaryItemTitle)
        navigateDiaryEditFragment()
    }

    private fun navigateDiaryEditFragment() {
        val directions =
            DiaryItemTitleEditFragmentDirections.actionDiaryItemTitleEditFragmentToDiaryEditFragment()
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryItemTitleDeleteDialog(
        parameters: DiaryItemTitleSelectionHistoryItemDeleteParameters
    ) {
        val directions =
            DiaryItemTitleEditFragmentDirections
                .actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteDialog(
                    parameters
                )
        navigateFragment(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditFragmentDirections.actionDiaryItemTitleEditFragmentToAppMessageDialog(
                appMessage
            )
        navigateFragment(NavigationCommand.To(directions))
    }
}
