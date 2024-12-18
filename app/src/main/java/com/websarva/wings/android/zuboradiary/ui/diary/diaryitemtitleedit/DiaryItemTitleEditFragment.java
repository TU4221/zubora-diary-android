package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDirections;

import com.google.android.material.textfield.TextInputLayout;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryItemTitleEditBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryItemTitleEditFragment extends BaseFragment {

    // View関係
    private FragmentDiaryItemTitleEditBinding binding;

    private static final String fromClassName = "From" + DiaryItemTitleEditFragment.class.getName();
    public static final String KEY_UPDATE_ITEM_NUMBER = "UpdateItemNumber" + fromClassName;
    public static final String KEY_NEW_ITEM_TITLE = "NewItemTitle" + fromClassName;

    // ViewModel
    private DiaryItemTitleEditViewModel diaryItemTitleEditViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(this);
        diaryItemTitleEditViewModel = provider.get(DiaryItemTitleEditViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
        binding = FragmentDiaryItemTitleEditBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryItemTitleEditViewModel(diaryItemTitleEditViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpToolBar();
        setUpItemTitleInputField();
        setUpItemTitleSelectionHistory();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // EditDiaryFragmentからデータ受取
        ItemNumber targetItemNumber =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getItemNumber();
        String targetItemTitle =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getItemTitle();
        diaryItemTitleEditViewModel.updateDiaryItemTitle(targetItemNumber, targetItemTitle);
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDiaryItemTitleDeleteDialogResult();
        retryOtherAppMessageDialogShow();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryItemTitleDeleteDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryItemTitleDeleteDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
    }

    @Override
    protected void setUpOtherAppMessageDialog() {
        diaryItemTitleEditViewModel.getAppMessageBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppMessageBufferListObserver(diaryItemTitleEditViewModel));
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private void receiveDiaryItemTitleDeleteDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(DiaryItemTitleDeleteDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            Integer deleteListItemPosition =
                    receiveResulFromDialog(DiaryItemTitleDeleteDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
            Objects.requireNonNull(deleteListItemPosition);

            diaryItemTitleEditViewModel
                    .deleteDiaryItemTitleSelectionHistoryItem(deleteListItemPosition);
        } else {
            ItemTitleSelectionHistoryListAdapter adapter =
                    (ItemTitleSelectionHistoryListAdapter)
                            binding.recyclerItemTitleSelectionHistory.getAdapter();
            Objects.requireNonNull(adapter);

            adapter.closeSwipedItem();
        }
    }

    private void setUpToolBar() {
        ItemNumber targetItemNumber = diaryItemTitleEditViewModel.getItemNumberLiveData().getValue();
        Objects.requireNonNull(targetItemNumber);

        String toolBarTitle = getString(R.string.fragment_diary_item_title_edit_toolbar_first_title) + targetItemNumber + getString(R.string.fragment_diary_item_title_edit_toolbar_second_title);
        binding.materialToolbarTopAppBar.setTitle(toolBarTitle);
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Objects.requireNonNull(v);

                        navController.navigateUp();
                    }
                });
    }

    private void setUpItemTitleInputField() {
        TextInputSetup textInputSetup = new TextInputSetup(requireActivity());
        TextInputLayout[] textInputLayouts = {binding.textInputLayoutNewItemTitle};
        textInputSetup.setUpKeyboardCloseOnEnter(textInputLayouts);
        textInputSetup.setUpFocusClearOnClickBackground(binding.viewFullScreenBackground, textInputLayouts);
        TextInputSetup.ClearButtonSetUpTransitionListener transitionListener =
                textInputSetup.createClearButtonSetupTransitionListener(textInputLayouts);
        addTransitionListener(transitionListener);

        EditText editText = binding.textInputLayoutNewItemTitle.getEditText();
        Objects.requireNonNull(editText);
        editText.addTextChangedListener(new InputItemTitleErrorWatcher());

        binding.buttonNewItemTitleSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);
                boolean isError = Objects.nonNull(binding.textInputLayoutNewItemTitle.getError());
                if (isError) return;

                String title = diaryItemTitleEditViewModel.getItemTitleLiveData().getValue();
                Objects.requireNonNull(title);
                completeItemTitleEdit(title);
            }
        });

        boolean isEnabled = !editText.getText().toString().isEmpty();
        binding.buttonNewItemTitleSelection.setEnabled(isEnabled);
    }

    private class InputItemTitleErrorWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 処理なし
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Objects.requireNonNull(s);

            String title = s.toString();
            if (title.isEmpty()) {
                binding.textInputLayoutNewItemTitle.setError(getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_empty));
                binding.buttonNewItemTitleSelection.setEnabled(false);
                return;
            }
            // 先頭が空白文字(\\s)
            if (title.matches("\\s+.*")) {
                binding.textInputLayoutNewItemTitle.setError(getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched));
                binding.buttonNewItemTitleSelection.setEnabled(false);
                return;
            }
            binding.textInputLayoutNewItemTitle.setError(null);
            binding.buttonNewItemTitleSelection.setEnabled(true);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // 処理なし
        }
    }

    private void setUpItemTitleSelectionHistory() {
        ItemTitleSelectionHistoryListAdapter itemTitleSelectionHistoryListAdapter =
                new ItemTitleSelectionHistoryListAdapter(
                        requireContext(),
                        binding.recyclerItemTitleSelectionHistory,
                        requireThemeColor()
                );
        itemTitleSelectionHistoryListAdapter.build();
        itemTitleSelectionHistoryListAdapter.setOnClickItemListener(new ItemTitleSelectionHistoryListAdapter.OnClickItemListener() {
            @Override
            public void onClick(@NonNull String title) {
                completeItemTitleEdit(title);
            }
        });
        itemTitleSelectionHistoryListAdapter.setOnClickDeleteButtonListener(new ItemTitleSelectionHistoryListAdapter.OnClickDeleteButtonListener() {
            @Override
            public void onClick(int position, @NonNull String title) {
                showDiaryItemTitleDeleteDialog(position, title);
            }
        });

        // 選択履歴読込・表示
        diaryItemTitleEditViewModel.loadDiaryItemTitleSelectionHistory();
        diaryItemTitleEditViewModel.getItemTitleSelectionHistoryLiveData()
                .observe(getViewLifecycleOwner(), new Observer<SelectionHistoryList>() {
                    @Override
                    public void onChanged(SelectionHistoryList SelectionHistoryItemList) {
                        Objects.requireNonNull(SelectionHistoryItemList);

                        ItemTitleSelectionHistoryListAdapter adapter =
                                (ItemTitleSelectionHistoryListAdapter)
                                        binding.recyclerItemTitleSelectionHistory.getAdapter();
                        Objects.requireNonNull(adapter);
                        adapter.submitList(SelectionHistoryItemList.getSelectionHistoryListItemList());
                    }
                });
    }

    // DiaryItemTitleEditFragmentを閉じる
    private void completeItemTitleEdit(String newItemTitle) {
        Objects.requireNonNull(newItemTitle);

        ItemNumber targetItemNumber = diaryItemTitleEditViewModel.getItemNumberLiveData().getValue();
        Objects.requireNonNull(targetItemNumber);

        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
        savedStateHandle.set(KEY_UPDATE_ITEM_NUMBER, targetItemNumber);
        savedStateHandle.set(KEY_NEW_ITEM_TITLE, newItemTitle);

        showDiaryEditFragment();
    }

    private void showDiaryEditFragment() {
        if (!canShowFragment()) return;

        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryEditFragment();
        navController.navigate(action);
    }

    private void showDiaryItemTitleDeleteDialog(int listItemPosition, String listItemTitle) {
        Objects.requireNonNull(listItemTitle);
        if (listItemPosition < 0) throw new IllegalArgumentException();
        if (!canShowFragment()) return;

        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteDialog(
                                listItemPosition,
                                listItemTitle
                        );
        navController.navigate(action);
    }

    @Override
    protected void navigateAppMessageDialog(@NonNull AppMessage appMessage) {
        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToAppMessageDialog(appMessage);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherAppMessageDialogShow() {
        diaryItemTitleEditViewModel.triggerAppMessageBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
