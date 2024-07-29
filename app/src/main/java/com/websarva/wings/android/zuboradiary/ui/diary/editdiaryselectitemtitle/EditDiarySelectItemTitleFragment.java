package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.database.SelectedDiaryItemTitle;
import com.websarva.wings.android.zuboradiary.databinding.FragmentEditDiarySelectItemTitleBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditDiarySelectItemTitleFragment extends Fragment {

    // View関係
    private FragmentEditDiarySelectItemTitleBinding binding;

    // Navigation関係
    private NavController navController;

    private static final String fromClassName = "From" + EditDiarySelectItemTitleFragment.class.getName();
    public static final String KEY_UPDATE_ITEM_NUMBER = "UpdateItemNumber" + fromClassName;
    public static final String KEY_NEW_ITEM_TITLE = "NewItemTitle" + fromClassName;

    // ViewModel
    private EditDiarySelectItemTitleViewModel editDiarySelectItemTitleViewModel;
    private int targetItemNumber;
    private String targetItemTitle;

    // キーボード関係
    private InputMethodManager inputMethodManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.editDiarySelectItemTitleViewModel =
                provider.get(EditDiarySelectItemTitleViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        this.binding =
                FragmentEditDiarySelectItemTitleBinding
                        .inflate(inflater, container, false);

        // キーボード設定
        this.inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 遷移元からデータ受取
        this.targetItemNumber =
                EditDiarySelectItemTitleFragmentArgs.fromBundle(getArguments()).getTargetItemNumber();
        this.targetItemTitle =
                EditDiarySelectItemTitleFragmentArgs.fromBundle(getArguments()).getTargetItemTitle();

        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry =
                this.navController
                        .getBackStackEntry(R.id.navigation_edit_diary_select_item_title_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                // 履歴項目削除確認ダイアログからの結果受取
                boolean containsConfirmDeleteDialogFragmentResults =
                        savedStateHandle
                                .contains(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON)
                        && savedStateHandle
                                .contains(DeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
                if (event.equals(Lifecycle.Event.ON_RESUME)
                        && containsConfirmDeleteDialogFragmentResults) {
                    Integer selectedButton =
                            savedStateHandle
                                    .get(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
                    Integer deleteListItemPosition =
                            savedStateHandle
                                    .get(DeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
                    if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                        try {
                            EditDiarySelectItemTitleFragment.this.editDiarySelectItemTitleViewModel
                                    .deleteSelectedItemTitleHistoryItem(deleteListItemPosition);
                        } catch (Exception e) {
                            String title = "通信エラー";
                            String message = "削除に失敗しました。";
                            navigateMessageDialog(title, message);
                            SelectedItemTitleHistoryAdapter adapter =
                                    (SelectedItemTitleHistoryAdapter)
                                            EditDiarySelectItemTitleFragment.this.binding
                                                    .recyclerSelectedItemTitleHistory
                                                    .getAdapter();
                            adapter.notifyItemChanged(deleteListItemPosition);
                        }
                    } else {
                        SelectedItemTitleHistoryAdapter adapter =
                                (SelectedItemTitleHistoryAdapter)
                                        EditDiarySelectItemTitleFragment.this.binding
                                                .recyclerSelectedItemTitleHistory
                                                .getAdapter();
                        adapter.notifyItemChanged(deleteListItemPosition);
                    }
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
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_LIST_ITEM_POSITION);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });


        // ツールバー設定
        String toolBarTitle = "項目" + String.valueOf(targetItemNumber) + "タイトル編集中";
        this.binding.materialToolbarTopAppBar.setTitle(toolBarTitle);
        this.binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditDiarySelectItemTitleFragment.this.navController.navigateUp();
                    }
                });


        // 新規項目入力欄設定
        // キーボード入力不要View
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(this.binding.buttonSelectNewItemTitle);
        noKeyboardViews.add(this.binding.recyclerSelectedItemTitleHistory);
        setupEditText(
                this.binding.editTextNewItemTitle,
                this.binding.textNewItemTitleLength,
                15,
                this.inputMethodManager,
                this.binding.constraintLayoutFullScreen, // 背景View
                noKeyboardViews
        );

        this.binding.editTextNewItemTitle.setText(this.targetItemTitle);



        this.binding.buttonSelectNewItemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title =
                        EditDiarySelectItemTitleFragment
                                .this.binding.editTextNewItemTitle.getText().toString();
                // 入力タイトルの先頭が空白文字以外(\\S)ならアイテムタイトル更新
                if (title.matches("\\S+.*")) {
                    closeThisFragment(title);

                } else {
                    // 入力タイトルの先頭が空白文字(\\s)ならエラー表示
                    if (title.matches("\\s+.*")) {
                        EditDiarySelectItemTitleFragment.this.binding.editTextNewItemTitle
                                .setError("先頭文字は空白文字以外を入力してください");
                    // それ以外(未入力)ならエラー表示
                    } else {
                        EditDiarySelectItemTitleFragment.this.binding.editTextNewItemTitle
                                .setError("1文字以上の文字を入力してください");
                    }

                }
            }
        });

        // 選択履歴リストアイテム設定
        RecyclerView recyclerSelectedItemTitleHistory =
                this.binding.recyclerSelectedItemTitleHistory;
        recyclerSelectedItemTitleHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSelectedItemTitleHistory.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)
        );
        recyclerSelectedItemTitleHistory.setAdapter(new SelectedItemTitleHistoryAdapter());
        SelectedItemTitleHistorySimpleCallBack simpleCallBack =
                new SelectedItemTitleHistorySimpleCallBack(
                        ItemTouchHelper.ACTION_STATE_IDLE,
                        ItemTouchHelper.LEFT
                );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerSelectedItemTitleHistory);

        // 選択履歴読込・表示
        try {
            this.editDiarySelectItemTitleViewModel.loadSelectedItemTitleHistory();
        } catch (Exception e) {
            String messageTitle = "通信エラー";
            String message = "項目タイトル選択履歴の読込に失敗しました。";
            navigateMessageDialog(messageTitle, message);
        }

        this.editDiarySelectItemTitleViewModel.getLiveSelectedItemTitleHistory()
                .observe(getViewLifecycleOwner(), new Observer<List<SelectedDiaryItemTitle>>() {
                    @Override
                    public void onChanged(List<SelectedDiaryItemTitle> selectedDiaryItemTitles) {
                        List<String> list = new ArrayList<>();
                        for (SelectedDiaryItemTitle selectedDiaryItemTitle : selectedDiaryItemTitles) {
                            list.add(selectedDiaryItemTitle.getTitle());
                        }

                        SelectedItemTitleHistoryAdapter adapter =
                                (SelectedItemTitleHistoryAdapter)
                                        EditDiarySelectItemTitleFragment.this.binding
                                                .recyclerSelectedItemTitleHistory.getAdapter();

                        adapter.changeItem(list);
                    }
        });
    }

    //EditText 設定メソッド
    @SuppressLint("ClickableViewAccessibility")
    private void setupEditText(EditText editText,
                               TextView textShowLength,
                               int maxLength,
                               InputMethodManager inputMethodManager,
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
                        hideKeyboard(inputMethodManager, viewBackground);
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
                            hideKeyboard(inputMethodManager, viewBackground);
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
                        hideKeyboard(inputMethodManager, viewBackground);
                        // 自テキストフォーカスクリア。
                        editText.clearFocus();
                    }
                }

                // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
                return false;
            }
        });
    }


    //// キーボードを隠す。
    private void hideKeyboard(InputMethodManager inputMethodManager, View touchView) {
        inputMethodManager
                .hideSoftInputFromWindow(
                        touchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
    private class SelectedItemTitleHistoryViewHolder extends RecyclerView.ViewHolder {
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
                    closeThisFragment(selectedItemTitle);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(SelectedItemTitleHistoryViewHolder holder, int position) {
            String selectedItemTitle = this.selectedItemTitleList.get(position);
            holder.textSelectedItemTitle.setText(selectedItemTitle);
        }

        @Override
        public int getItemCount() {
            return this.selectedItemTitleList.size();
        }

        public void changeItem(List<String> list) {
            this.selectedItemTitleList = list;
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

            // TODO:getAdapterPositionの代替を検討する
            int itemPos = viewHolder.getAdapterPosition();
            String itemTitle =
                    selectedItemTitleHistoryViewHolder.textSelectedItemTitle.getText().toString();
            NavDirections action =
                    EditDiarySelectItemTitleFragmentDirections
                            .actionEditDiarySelectItemTitleFragmentToDeleteConfirmationDialog(
                                    itemPos, itemTitle);
            EditDiarySelectItemTitleFragment.this.navController.navigate(action);
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
            Drawable icon = getContext().getDrawable(R.drawable.ic_delete_forever_24);

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

    // EditDiarySelectItemTitleFragmentを閉じる
    private void closeThisFragment(String newItemTitle) {
        SavedStateHandle savedStateHandle =
                this.navController.getPreviousBackStackEntry().getSavedStateHandle();
        savedStateHandle.set(KEY_UPDATE_ITEM_NUMBER, this.targetItemNumber);
        savedStateHandle.set(KEY_NEW_ITEM_TITLE, newItemTitle);

        NavDirections action =
                EditDiarySelectItemTitleFragmentDirections
                        .actionSelectItemTitleFragmentToEditDiaryFragment();
        this.navController.navigate(action);
    }

    private void navigateMessageDialog(String title, String message) {
        NavDirections action =
                EditDiarySelectItemTitleFragmentDirections
                        .actionEditDiarySelectItemTitleFragmentToMessageDialog(
                                title, message);
        EditDiarySelectItemTitleFragment.this.navController.navigate(action);
    }
}
