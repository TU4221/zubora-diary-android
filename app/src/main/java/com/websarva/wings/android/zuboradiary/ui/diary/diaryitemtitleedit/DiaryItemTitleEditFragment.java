package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryItemTitleEditFragment extends Fragment {

    // View関係
    private FragmentDiaryItemTitleEditBinding binding;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowItemTitleSelectionHistoryLoadingErrorDialog;
    private boolean shouldShowItemTitleSelectionHistoryItemDeleteErrorDialog;

    private static final String fromClassName = "From" + DiaryItemTitleEditFragment.class.getName();
    public static final String KEY_UPDATE_ITEM_NUMBER = "UpdateItemNumber" + fromClassName;
    public static final String KEY_NEW_ITEM_TITLE = "NewItemTitle" + fromClassName;

    // ViewModel
    private DiaryItemTitleEditViewModel diaryItemTitleEditViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryItemTitleEditViewModel =
                provider.get(DiaryItemTitleEditViewModel.class);

        // Navigation設定
        navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        binding = FragmentDiaryItemTitleEditBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpTargetItemInformation();
        setUpDialogResultReceiver();
        setUpToolBar();
        setUpItemTitleInputField();
        setUpItemTitleSelectionHistory();
        setUpErrorObserver();
    }

    private void setUpTargetItemInformation() {
        // EditDiaryFragmentからデータ受取
        int targetItemNumber =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getTargetItemNumber();
        String targetItemTitle =
                DiaryItemTitleEditFragmentArgs.fromBundle(getArguments()).getTargetItemTitle();
        diaryItemTitleEditViewModel.updateItemTitle(targetItemNumber, targetItemTitle);
    }

    private void setUpDialogResultReceiver() {
        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry == null) {
            return;
        }
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    receiveDeleteConfirmationDialogResult(savedStateHandle);
                    removeDialogResults(savedStateHandle);
                    retryErrorDialogShow();
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    removeDialogResults(savedStateHandle);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    private void removeDialogResults(SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
    }

    private void receiveDeleteConfirmationDialogResult(SavedStateHandle savedStateHandle) {
        // 履歴項目削除確認ダイアログからの結果受取
        boolean containsDialogResults =
                savedStateHandle
                        .contains(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON)
                        && savedStateHandle
                        .contains(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
        if (containsDialogResults) {
            Integer selectedButton =
                    savedStateHandle
                            .get(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
            Integer deleteListItemPosition =
                    savedStateHandle
                            .get(DiaryItemTitleDeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
            if (selectedButton == null) {
                return;
            }
            if (deleteListItemPosition == null) {
                return;
            }
            if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                diaryItemTitleEditViewModel
                        .deleteSelectedItemTitleHistoryItem(deleteListItemPosition);
            } else {
                SelectedItemTitleHistoryAdapter adapter =
                        (SelectedItemTitleHistoryAdapter)
                                binding.recyclerSelectedItemTitleHistory.getAdapter();
                if (adapter == null) {
                    // TODO:assert
                    return;
                }
                adapter.notifyItemChanged(deleteListItemPosition);
            }
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
        noKeyboardViews.add(binding.recyclerSelectedItemTitleHistory);
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
        RecyclerView recyclerItemTitleSelectionHistory = binding.recyclerSelectedItemTitleHistory;
        recyclerItemTitleSelectionHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerItemTitleSelectionHistory.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        );
        recyclerItemTitleSelectionHistory.setAdapter(new SelectedItemTitleHistoryAdapter());
        SelectedItemTitleHistorySimpleCallBack simpleCallBack =
                new SelectedItemTitleHistorySimpleCallBack(
                        ItemTouchHelper.ACTION_STATE_IDLE,
                        ItemTouchHelper.LEFT
                );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerItemTitleSelectionHistory);

        // 選択履歴読込・表示
        diaryItemTitleEditViewModel.loadSelectedItemTitleHistory();
        diaryItemTitleEditViewModel.getItemTitleSelectionHistoryLiveData()
                .observe(getViewLifecycleOwner(), new Observer<List<DiaryItemTitleSelectionHistoryItem>>() {
                    @Override
                    public void onChanged(List<DiaryItemTitleSelectionHistoryItem> diaryItemTitleSelectionHistoryItems) {
                        List<String> list = new ArrayList<>();
                        for (DiaryItemTitleSelectionHistoryItem diaryItemTitleSelectionHistoryItem : diaryItemTitleSelectionHistoryItems) {
                            list.add(diaryItemTitleSelectionHistoryItem.getTitle());
                        }

                        SelectedItemTitleHistoryAdapter adapter =
                                (SelectedItemTitleHistoryAdapter)
                                        binding.recyclerSelectedItemTitleHistory.getAdapter();
                        if (adapter == null) {
                            return;
                        }
                        adapter.changeItem(list);
                    }
                });
    }

    private void setUpErrorObserver() {
        // エラー表示
        diaryItemTitleEditViewModel.getIsItemTitleSelectionHistoryLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showItemTitleSelectionHistoryLoadingErrorDialog();
                            diaryItemTitleEditViewModel.clearSelectedItemTitleListLoadingError();
                        }
                    }
                });

        diaryItemTitleEditViewModel.getIsItemTitleSelectionHistoryItemDeleteErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showItemTitleSelectionHistoryItemDeleteErrorDialog();
                            diaryItemTitleEditViewModel.clearSelectedItemTitleDeleteError();
                        }
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

    // アイテムタイトル選択履歴ViewHolder
    // TODO:ListAdapterに変更&独立クラスへ
    private static class SelectedItemTitleHistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView textSelectedItemTitle;

        public SelectedItemTitleHistoryViewHolder(View itemView) {
            super(itemView);
            textSelectedItemTitle = itemView.findViewById(R.id.text_selected_item_title);
        }
    }

    private class SelectedItemTitleHistoryAdapter
            extends RecyclerView.Adapter<SelectedItemTitleHistoryViewHolder> {
        private List<String> selectedItemTitleList;

        @NonNull
        @Override
        public SelectedItemTitleHistoryViewHolder onCreateViewHolder(
                ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view =
                    inflater.inflate(
                            R.layout.row_selected_item_title_history, parent, false);
            SelectedItemTitleHistoryViewHolder holder =
                    new SelectedItemTitleHistoryViewHolder(view);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedItemTitle = holder.textSelectedItemTitle.getText().toString();
                    completeItemTitleEdit(selectedItemTitle);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(SelectedItemTitleHistoryViewHolder holder, int position) {
            String selectedItemTitle = selectedItemTitleList.get(position);
            holder.textSelectedItemTitle.setText(selectedItemTitle);
        }

        @Override
        public int getItemCount() {
            return selectedItemTitleList.size();
        }

        public void changeItem(List<String> list) {
            selectedItemTitleList = list;
            notifyDataSetChanged();
        }

    }

    // 参考：https://appdev-room.com/android-recyclerview-swipe-action
    private class SelectedItemTitleHistorySimpleCallBack extends ItemTouchHelper.SimpleCallback {
        public SelectedItemTitleHistorySimpleCallBack(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            SelectedItemTitleHistoryViewHolder selectedItemTitleHistoryViewHolder =
                                                    (SelectedItemTitleHistoryViewHolder) viewHolder;

            int itemPos = viewHolder.getBindingAdapterPosition();
            String itemTitle =
                    selectedItemTitleHistoryViewHolder.textSelectedItemTitle.getText().toString();
            showDeleteConfirmationDialog(itemPos, itemTitle);
        }

        @Override
        public void onChildDraw(
                @NonNull Canvas c,
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                float dX,
                float dY,
                int actionState,
                boolean isCurrentlyActive
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            ColorDrawable backgroundColor = new ColorDrawable(Color.RED);
            Drawable icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_delete_forever_24);
            if (icon == null) {
                // TODO:assert
                return;
            }
            View itemView = viewHolder.itemView;
            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconLeft = itemView.getRight() - icon.getIntrinsicWidth() - iconMargin * 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconRight = itemView.getRight() - iconMargin;
            int iconBottom = itemView.getBottom() - iconMargin;

            int defaultIconLeft = itemView.getRight();
            int defaultIconRight = itemView.getRight() + icon.getIntrinsicWidth();

            if (dX < 0 ) {
                int absDx = Math.abs((int) dX);
                int switchingPoint = itemView.getRight() - iconLeft;

                backgroundColor
                        .setBounds(
                                itemView.getRight() - absDx,
                                itemView.getTop(),
                                iconRight,
                                iconBottom
                        );

                if (absDx >= switchingPoint) {
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                } else {
                    icon.setBounds(
                            defaultIconLeft - absDx, iconTop,
                            defaultIconRight - absDx,
                            iconBottom
                    );
                }

            } else {
                backgroundColor.setBounds(0, 0, 0, 0);
                icon.setBounds(0, 0, 0, 0);

            }

            backgroundColor.draw(c);
            icon.draw(c);
        }
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
        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryEditFragment();
        navController.navigate(action);
    }

    private void showDeleteConfirmationDialog(int itemPosition, String itemTitle) {
        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToDiaryItemTitleDeleteConfirmationDialog(
                                itemPosition, itemTitle);
        navController.navigate(action);
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowItemTitleSelectionHistoryLoadingErrorDialog) {
            showItemTitleSelectionHistoryLoadingErrorDialog();
            return;
        }
        if (shouldShowItemTitleSelectionHistoryItemDeleteErrorDialog) {
            showItemTitleSelectionHistoryItemDeleteErrorDialog();
        }
    }

    private void showItemTitleSelectionHistoryLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "選択履歴の読込に失敗しました。");
            shouldShowItemTitleSelectionHistoryLoadingErrorDialog = false;
        } else {
            shouldShowItemTitleSelectionHistoryLoadingErrorDialog = true;
        }
    }

    private void showItemTitleSelectionHistoryItemDeleteErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "削除に失敗しました。");
            shouldShowItemTitleSelectionHistoryItemDeleteErrorDialog = false;
        } else {
            shouldShowItemTitleSelectionHistoryItemDeleteErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                DiaryItemTitleEditFragmentDirections
                        .actionDiaryItemTitleEditFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_diary_item_title_edit_fragment;
    }
}
