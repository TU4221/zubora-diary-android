package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryItemTitleDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryItemTitleEditViewModel
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.ItemTitleSelectionHistoryListAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.ItemTitleEditResult
import com.websarva.wings.android.zuboradiary.ui.view.edittext.TextInputConfigurator
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiaryItemTitleEditFragment : BaseFragment() {

    internal companion object {
        private val fromClassName = "From" + DiaryItemTitleEditFragment::class.java.name
        val KEY_RESULT: String = "Result$fromClassName"
    }

    // View関係
    private var _binding: FragmentDiaryItemTitleEditBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryItemTitleEditViewModel by viewModels()

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryItemTitleEditFragment.viewLifecycleOwner
            diaryItemTitleEditViewModel = mainViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
        setUpItemTitleInputField()
        setUpItemTitleSelectionHistory()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryItemTitleDeleteDialogResultReceiver()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // EditDiaryFragmentからデータ受取
        val targetItemNumber =
            DiaryItemTitleEditFragmentArgs.fromBundle(requireArguments()).itemNumber
        val targetItemTitle =
            DiaryItemTitleEditFragmentArgs.fromBundle(requireArguments()).itemTitle
        mainViewModel.updateDiaryItemTitle(targetItemNumber, targetItemTitle)
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private fun setUpDiaryItemTitleDeleteDialogResultReceiver() {
        setUpDialogResultReceiver<Int>(
            DiaryItemTitleDeleteDialogFragment.KEY_RESULT
        ) { result ->
            when (result) {
                is DialogResult.Positive<Int> -> {
                    val deleteListItemPosition = result.data
                    lifecycleScope.launch {
                        mainViewModel
                            .deleteDiaryItemTitleSelectionHistoryItem(deleteListItemPosition)
                    }
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    val adapter =
                        checkNotNull(
                            binding.recyclerItemTitleSelectionHistory.adapter
                        ) as ItemTitleSelectionHistoryListAdapter
                    adapter.closeSwipedItem()
                }
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            val targetItemNumber = mainViewModel.itemNumber.requireValue()
            val toolBarTitle =
                getString(R.string.fragment_diary_item_title_edit_toolbar_first_title) + targetItemNumber + getString(
                    R.string.fragment_diary_item_title_edit_toolbar_second_title
                )
            title = toolBarTitle
            setNavigationOnClickListener { navController.navigateUp() }
        }
    }

    private fun setUpItemTitleInputField() {
        val textInputConfigurator = TextInputConfigurator()
        val textInputLayouts = arrayOf(
            binding.textInputLayoutNewItemTitle
        )
        textInputConfigurator.setUpKeyboardCloseOnEnter(*textInputLayouts)
        textInputConfigurator.setUpFocusClearOnClickBackground(
            binding.viewFullScreenBackground,
            *textInputLayouts
        )
        val transitionListener =
            textInputConfigurator.createClearButtonSetupTransitionListener(*textInputLayouts)
        addTransitionListener(transitionListener)

        val editText = checkNotNull(binding.textInputLayoutNewItemTitle.editText)
        editText.addTextChangedListener(InputItemTitleErrorWatcher())

        binding.buttonNewItemTitleSelection.apply {
            setOnClickListener {
                val isError = !binding.textInputLayoutNewItemTitle.error.isNullOrEmpty()
                if (isError) return@setOnClickListener

                val title = mainViewModel.itemTitle.value
                completeItemTitleEdit(title)
            }

            val isEnabled = editText.text.toString().isNotEmpty()
            this.isEnabled = isEnabled
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
        itemTitleSelectionHistoryListAdapter.setOnClickItemListener { newItemTitle: String ->
            this.completeItemTitleEdit(
                newItemTitle
            )
        }
        itemTitleSelectionHistoryListAdapter.setOnClickDeleteButtonListener { listItemPosition: Int, listItemTitle: String ->
            this.navigateDiaryItemTitleDeleteDialog(
                listItemPosition,
                listItemTitle
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
    private fun completeItemTitleEdit(newItemTitle: String) {
        val targetItemNumber = mainViewModel.itemNumber.requireValue()

        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_RESULT] =
            ItemTitleEditResult(
                targetItemNumber,
                newItemTitle
            )

        navigateDiaryEditFragment()
    }

    private fun navigateDiaryEditFragment() {
        if (!canNavigateFragment) return

        val directions =
            DiaryItemTitleEditFragmentDirections.actionDiaryItemTitleEditFragmentToDiaryEditFragment()
        navController.navigate(directions)
    }

    private fun navigateDiaryItemTitleDeleteDialog(listItemPosition: Int, listItemTitle: String) {
        require(listItemPosition >= 0)
        if (!canNavigateFragment) return

        val directions =
            DiaryItemTitleEditFragmentDirections.actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteDialog(
                listItemPosition,
                listItemTitle
            )
        navController.navigate(directions)
    }

    override fun onNavigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditFragmentDirections.actionDiaryItemTitleEditFragmentToAppMessageDialog(
                appMessage
            )
        navController.navigate(directions)
    }

    override fun destroyBinding() {
        _binding = null
    }
}
