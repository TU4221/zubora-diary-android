package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryItemTitleEditBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.notNullValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaryItemTitleEditFragment : BaseFragment() {

    companion object {
        private val fromClassName = "From" + DiaryItemTitleEditFragment::class.java.name
        val KEY_UPDATE_ITEM_NUMBER: String = "UpdateItemNumber$fromClassName"
        val KEY_NEW_ITEM_TITLE: String = "NewItemTitle$fromClassName"
    }

    // View関係
    private var _binding: FragmentDiaryItemTitleEditBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    private lateinit var diaryItemTitleEditViewModel: DiaryItemTitleEditViewModel

    override fun initializeViewModel() {
        val provider = ViewModelProvider(this)
        diaryItemTitleEditViewModel = provider[DiaryItemTitleEditViewModel::class.java]
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@DiaryItemTitleEditFragment
            diaryItemTitleEditViewModel = this@DiaryItemTitleEditFragment.diaryItemTitleEditViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
        setUpItemTitleInputField()
        setUpItemTitleSelectionHistory()
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        // EditDiaryFragmentからデータ受取
        val targetItemNumber =
            DiaryItemTitleEditFragmentArgs.fromBundle(requireArguments()).itemNumber
        val targetItemTitle =
            DiaryItemTitleEditFragmentArgs.fromBundle(requireArguments()).itemTitle
        diaryItemTitleEditViewModel.updateDiaryItemTitle(targetItemNumber, targetItemTitle)
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        receiveDiaryItemTitleDeleteDialogResult()
        retryOtherAppMessageDialogShow()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        savedStateHandle.apply {
            remove<Any>(DiaryItemTitleDeleteDialogFragment.KEY_SELECTED_BUTTON)
            remove<Any>(DiaryItemTitleDeleteDialogFragment.KEY_DELETE_LIST_ITEM_POSITION)
        }
    }

    override fun setUpOtherAppMessageDialog() {
        diaryItemTitleEditViewModel.appMessageBufferList
            .observe(
                viewLifecycleOwner,
                AppMessageBufferListObserver(diaryItemTitleEditViewModel)
            )
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private fun receiveDiaryItemTitleDeleteDialogResult() {
        val selectedButton =
            receiveResulFromDialog<Int>(DiaryItemTitleDeleteDialogFragment.KEY_SELECTED_BUTTON)
                ?: return

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            val deleteListItemPosition =
                checkNotNull(
                    receiveResulFromDialog<Int>(
                        DiaryItemTitleDeleteDialogFragment.KEY_DELETE_LIST_ITEM_POSITION
                    )
                )

            diaryItemTitleEditViewModel
                .deleteDiaryItemTitleSelectionHistoryItem(deleteListItemPosition)
        } else {
            val adapter =
                checkNotNull(
                    binding.recyclerItemTitleSelectionHistory.adapter
                ) as ItemTitleSelectionHistoryListAdapter
            adapter.closeSwipedItem()
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            val targetItemNumber = diaryItemTitleEditViewModel.itemNumber.checkNotNull()
            val toolBarTitle =
                getString(R.string.fragment_diary_item_title_edit_toolbar_first_title) + targetItemNumber + getString(
                    R.string.fragment_diary_item_title_edit_toolbar_second_title
                )
            title = toolBarTitle
            setNavigationOnClickListener { navController.navigateUp() }
        }
    }

    private fun setUpItemTitleInputField() {
        val textInputSetup = TextInputSetup(requireActivity())
        val textInputLayouts = arrayOf(
            binding.textInputLayoutNewItemTitle
        )
        textInputSetup.setUpKeyboardCloseOnEnter(*textInputLayouts)
        textInputSetup.setUpFocusClearOnClickBackground(
            binding.viewFullScreenBackground,
            *textInputLayouts
        )
        val transitionListener =
            textInputSetup.createClearButtonSetupTransitionListener(*textInputLayouts)
        addTransitionListener(transitionListener)

        val editText = checkNotNull(binding.textInputLayoutNewItemTitle.editText)
        editText.addTextChangedListener(InputItemTitleErrorWatcher())

        binding.buttonNewItemTitleSelection.apply {
            setOnClickListener {
                val isError = !binding.textInputLayoutNewItemTitle.error.isNullOrEmpty()
                if (isError) return@setOnClickListener

                val title = diaryItemTitleEditViewModel.itemTitle.notNullValue()
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
            this.showDiaryItemTitleDeleteDialog(
                listItemPosition,
                listItemTitle
            )
        }

        // 選択履歴読込・表示
        diaryItemTitleEditViewModel.apply {
            loadDiaryItemTitleSelectionHistory()
            itemTitleSelectionHistoryLiveData
                .observe(viewLifecycleOwner) { selectionHistoryList: SelectionHistoryList ->
                    val adapter =
                        checkNotNull(
                            binding.recyclerItemTitleSelectionHistory.adapter
                        ) as ItemTitleSelectionHistoryListAdapter
                    adapter.submitList(selectionHistoryList.selectionHistoryListItemList)
                }
        }
    }

    // DiaryItemTitleEditFragmentを閉じる
    private fun completeItemTitleEdit(newItemTitle: String) {
        val targetItemNumber = diaryItemTitleEditViewModel.itemNumber.checkNotNull()

        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[KEY_UPDATE_ITEM_NUMBER] = targetItemNumber
        savedStateHandle[KEY_NEW_ITEM_TITLE] = newItemTitle

        showDiaryEditFragment()
    }

    private fun showDiaryEditFragment() {
        if (isDialogShowing) return

        val directions =
            DiaryItemTitleEditFragmentDirections
                .actionDiaryItemTitleEditFragmentToDiaryEditFragment()
        navController.navigate(directions)
    }

    private fun showDiaryItemTitleDeleteDialog(listItemPosition: Int, listItemTitle: String) {
        require(listItemPosition >= 0)
        if (isDialogShowing) return

        val directions =
            DiaryItemTitleEditFragmentDirections
                .actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteDialog(
                    listItemPosition,
                    listItemTitle
                )
        navController.navigate(directions)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryItemTitleEditFragmentDirections
                .actionDiaryItemTitleEditFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun retryOtherAppMessageDialogShow() {
        diaryItemTitleEditViewModel.triggerAppMessageBufferListObserver()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
