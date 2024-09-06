package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryItemTitleEditBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;

import java.util.ArrayList;
import java.util.List;

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
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryItemTitleEditViewModel = provider.get(DiaryItemTitleEditViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentDiaryItemTitleEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
        int targetItemNumber =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getTargetItemNumber();
        String targetItemTitle =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getTargetItemTitle();
        diaryItemTitleEditViewModel.updateItemTitle(targetItemNumber, targetItemTitle);
    }

    @Override
    protected void handleOnReceivingResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        receiveDeleteConfirmationDialogResult(savedStateHandle);
        retryErrorDialogShow();
    }

    @Override
    protected void removeResultFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        diaryItemTitleEditViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryItemTitleEditViewModel));
    }

    // 履歴項目削除確認ダイアログからの結果受取
    private void receiveDeleteConfirmationDialogResult(SavedStateHandle savedStateHandle) {
        Integer selectedButton =
                receiveResulFromDialog(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) {
            return;
        }

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            Integer deleteListItemPosition =
                    receiveResulFromDialog(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
            if (deleteListItemPosition == null) {
                return;
            }

            diaryItemTitleEditViewModel
                    .deleteSelectedItemTitleHistoryItem(deleteListItemPosition);
        } else {
            ItemTitleSelectionHistoryListAdapter adapter =
                    (ItemTitleSelectionHistoryListAdapter)
                            binding.recyclerItemTitleSelectionHistory.getAdapter();
            if (adapter == null) {
                return;
            }

            adapter.closeSwipedItem();
        }
    }

    private void setUpToolBar() {
        Integer targetItemNumber = diaryItemTitleEditViewModel.getItemNumberLiveData().getValue();
        if (targetItemNumber == null) {
            // TODO:assert
            return;
        }
        String toolBarTitle = getString(R.string.fragment_diary_item_title_edit_toolbar_first_title) + targetItemNumber + getString(R.string.fragment_diary_item_title_edit_toolbar_second_title);
        binding.materialToolbarTopAppBar.setTitle(toolBarTitle);
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navController.navigateUp();
                    }
                });
    }

    private void setUpItemTitleInputField() {
        // 新規項目入力欄設定
        // キーボード入力不要View
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(binding.buttonSelectNewItemTitle);
        noKeyboardViews.add(binding.recyclerItemTitleSelectionHistory);
        setupEditText(
                binding.editTextNewItemTitle,
                binding.textNewItemTitleLength,
                15,
                binding.constraintLayoutFullScreen, // 背景View
                noKeyboardViews
        );

        diaryItemTitleEditViewModel.getItemTitleLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s == null) {
                            // TODO:assert
                            return;
                        }
                        binding.editTextNewItemTitle.setText(s);
                    }
                });

        binding.buttonSelectNewItemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.editTextNewItemTitle.getText().toString();
                // 入力タイトルの先頭が空白文字以外(\\S)ならアイテムタイトル更新
                if (title.matches("\\S+.*")) {
                    completeItemTitleEdit(title);
                } else {
                    // 入力タイトルの先頭が空白文字(\\s)ならエラー表示
                    if (title.matches("\\s+.*")) {
                        binding.editTextNewItemTitle.setError(getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched));
                        // それ以外(未入力)ならエラー表示
                    } else {
                        binding.editTextNewItemTitle.setError(getString(R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_empty));
                    }
                }
            }
        });
    }

    private void setUpItemTitleSelectionHistory() {
        // 選択履歴リストアイテム設定
        // TODO:下記必要か判断
        ItemTitleSelectionHistoryListAdapter itemTitleSelectionHistoryListAdapter =
                new ItemTitleSelectionHistoryListAdapter(
                        requireContext(),
                        binding.recyclerItemTitleSelectionHistory,
                        new ItemTitleSelectionHistoryListAdapter.OnClickItemListener() {
                            @Override
                            public void onClick(String title) {
                                completeItemTitleEdit(title);
                            }
                        },
                        new ItemTitleSelectionHistoryListAdapter.OnClickDeleteButtonListener() {
                            @Override
                            public void onClick(int position, String title) {
                                showDeleteConfirmationDialog(position, title);
                            }
                        });
        itemTitleSelectionHistoryListAdapter.build();

        // 選択履歴読込・表示
        diaryItemTitleEditViewModel.loadItemTitleSelectionHistory();
        diaryItemTitleEditViewModel.getItemTitleSelectionHistoryLiveData()
                .observe(getViewLifecycleOwner(), new Observer<List<DiaryItemTitleSelectionHistoryItem>>() {
                    @Override
                    public void onChanged(List<DiaryItemTitleSelectionHistoryItem> diaryItemTitleSelectionHistoryItems) {
                        if (diaryItemTitleSelectionHistoryItems == null) {
                            return;
                        }

                        ItemTitleSelectionHistoryListAdapter adapter =
                                (ItemTitleSelectionHistoryListAdapter)
                                        binding.recyclerItemTitleSelectionHistory.getAdapter();
                        if (adapter == null) {
                            return;
                        }
                        Log.d("20240826","ItemTitleSelectionHistoryLiveDataObserver");
                        adapter.submitList(diaryItemTitleSelectionHistoryItems);
                    }
                });
    }

    //EditText 設定メソッド
    @SuppressLint("ClickableViewAccessibility")
    private void setupEditText(EditText editText,
                               TextView textShowLength,
                               int maxLength,
                               View viewBackground,
                               List<View> otherViewList) {

        // 入力文字数制限
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(inputFilters);

        // 入力文字数表示
        showTextLength(editText, textShowLength);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 未使用
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showTextLength(editText, textShowLength);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 未使用
            }
        });

        // 入力欄フォーカス時の処理。
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 入力欄フォーカス外しリスナをスクロールビューに実装。
                // 参考：https://cpoint-lab.co.jp/article/202202/22053/
                viewBackground.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // キーボードを隠す。
                        KeyboardInitializer keyboardInitializer =
                                new KeyboardInitializer(requireActivity());
                        keyboardInitializer.hide(viewBackground);
                        // 自テキストフォーカスクリア。
                        editText.clearFocus();
                        // 他のタッチ、クリックリスナを継続させる為にfalseを戻す。
                        return false;
                    }
                });

                for (View otherView: otherViewList) {
                    otherView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // キーボードを隠す。
                            KeyboardInitializer keyboardInitializer =
                                    new KeyboardInitializer(requireActivity());
                            keyboardInitializer.hide(viewBackground);
                            // 自テキストフォーカスクリア。
                            editText.clearFocus();
                            // 他のタッチ、クリックリスナを継続させる為にfalseを戻す。
                            return false;
                        }
                    });
                }
            }
        });


        // 入力欄エンターキー押下時の処理。
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    // HACK:InputTypeの値が何故か1ズレている。(公式のリファレンスでもズレあり。)
                    //      (setとgetを駆使してLogで確認確認済み)
                    if (editText.getInputType() != (TYPE_TEXT_FLAG_MULTI_LINE + 1)) {
                        // キーボードを隠す。
                        KeyboardInitializer keyboardInitializer =
                                new KeyboardInitializer(requireActivity());
                        keyboardInitializer.hide(viewBackground);
                        // 自テキストフォーカスクリア。
                        editText.clearFocus();
                    }
                }

                // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
                return false;
            }
        });
    }

    // テキスト入力文字数表示メソッド
    private void showTextLength(TextView textView, TextView textLength) {
        int inputLength = textView.getText().length();
        InputFilter[] inputFilters = textView.getFilters();
        int inputMaxLength = -1;
        for (InputFilter inputFilter: inputFilters) {
            if (inputFilter instanceof InputFilter.LengthFilter) {
                InputFilter.LengthFilter lengthFilter = (InputFilter.LengthFilter) inputFilter;
                inputMaxLength = lengthFilter.getMax();
            }
        }
        String showLength = inputLength + "/" + inputMaxLength;
        textLength.setText(showLength);
    }

    // DiaryItemTitleEditFragmentを閉じる
    private void completeItemTitleEdit(String newItemTitle) {
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        if (navBackStackEntry == null) {
            // TODO:assert
            return;
        }
        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
        Integer targetItemNumber = diaryItemTitleEditViewModel.getItemNumberLiveData().getValue();
        if (targetItemNumber == null) {
            // TODO:assert
            return;
        }
        savedStateHandle.set(KEY_UPDATE_ITEM_NUMBER, targetItemNumber);
        savedStateHandle.set(KEY_NEW_ITEM_TITLE, newItemTitle);
        showDiaryEditFragment();
    }

    private void showDiaryEditFragment() {
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryEditFragment();
        navController.navigate(action);
    }

    private void showDeleteConfirmationDialog(int itemPosition, String itemTitle) {
        if (itemPosition < 0) {
            throw new IllegalArgumentException();
        }
        if (itemTitle == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteConfirmationDialog(
                                itemPosition, itemTitle);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        diaryItemTitleEditViewModel.triggerAppErrorBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
