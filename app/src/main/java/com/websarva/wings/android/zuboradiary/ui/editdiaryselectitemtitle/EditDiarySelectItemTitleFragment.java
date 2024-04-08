package com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Printer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentEditDiarySelectItemTitleBinding;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditDiarySelectItemTitleFragment extends Fragment {

    private FragmentEditDiarySelectItemTitleBinding binding;
    private InputMethodManager inputMethodManager;

    private EditDiarySelectItemTitleViewModel editDiarySelectItemTitleViewModel;
    private EditDiaryViewModel diaryViewModel;
    private int selectItemNo;
    private ConstraintLayout constraintLayoutFullScreen;
    private EditText editTextNewItemTitle;
    private TextView textNewItemTitleLength;
    private Button buttonSelectNewItemTitle;
    private TextView textItemTitleHistory;
    private RecyclerView recyclerSelectedItemTitleHistory;

    public EditDiarySelectItemTitleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        editDiarySelectItemTitleViewModel = provider.get(EditDiarySelectItemTitleViewModel.class);
        diaryViewModel = provider.get(EditDiaryViewModel.class);

        // 戻るボタンを推した時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment();

                    }
                }
        );

        // ConfirmDeleteDialogFragmentからのデータ受取、受取後の処理
        getChildFragmentManager().setFragmentResultListener(
                "ToEditDiarySelectItemTitleFragment_ConfirmDeleteDialogFragmentRequestKey",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(
                            @NonNull String requestKey,
                            @NonNull Bundle result
                    ) {
                        int selectedButtonResult = result.getInt("SelectedButtonResult");
                        int selectedItemTitlePos = result.getInt("SelectedItemTitlePos");

                        switch (selectedButtonResult) {
                            case DialogInterface.BUTTON_POSITIVE:
                                editDiarySelectItemTitleViewModel
                                        .deleteSelectedItemTitle(selectedItemTitlePos);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                            default:
                                SelectedItemTitleHistoryAdapter adapter =
                                        (SelectedItemTitleHistoryAdapter)
                                                EditDiarySelectItemTitleFragment
                                                        .this.recyclerSelectedItemTitleHistory
                                                                                    .getAdapter();
                                adapter.notifyItemChanged(selectedItemTitlePos);
                                break;

                        }
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // データバインディング設定
        this.binding =
                FragmentEditDiarySelectItemTitleBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(EditDiarySelectItemTitleFragment.this);
        this.binding.setEditDiaryViewModel(diaryViewModel);

        // クラスフィールド初期化
        this.constraintLayoutFullScreen = this.binding.constraintLayoutFullScreen;
        this.editTextNewItemTitle = this.binding.editTextNewItemTitle;
        this.textNewItemTitleLength = this.binding.textNewItemTitleLength;
        this.buttonSelectNewItemTitle = this.binding.buttonSelectNewItemTitle;
        this.textItemTitleHistory = this.binding.textItemTitleHistory;
        this.recyclerSelectedItemTitleHistory = this.binding.recyclerSelectedItemTitleHistory;

        this.inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //他フラグメントからの受取データ。
        Bundle extras = getArguments();
        this.selectItemNo = extras.getInt("SelectItemNo");

        //アクションバーオプションメニュー更新。
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                new MenuProvider() {
                    @Override
                    public void onCreateMenu(
                            @NonNull Menu menu, @NonNull MenuInflater menuInflater) {

                        ActionBar actionBar = ((AppCompatActivity) getActivity())
                                                                .getSupportActionBar();
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setHomeAsUpIndicator(null);
                    }

                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                        if (menuItem.getItemId() == android.R.id.home) {
                            backFragment();
                            return true;
                        }
                        return false;
                    }
                },
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );

        // 新規項目入力欄設定
        // キーボード不要Viewをまとめ、
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(this.buttonSelectNewItemTitle);
        noKeyboardViews.add(this.recyclerSelectedItemTitleHistory);
        setupEditText(
                editTextNewItemTitle,
                textNewItemTitleLength,
                15,
                inputMethodManager,
                constraintLayoutFullScreen,
                noKeyboardViews
        );

        switch (selectItemNo) {
            case 1:
                this.editTextNewItemTitle.setText(diaryViewModel.getLiveItem1Title().getValue());
                break;
            case 2:
                this.editTextNewItemTitle.setText(diaryViewModel.getLiveItem2Title().getValue());
                break;
            case 3:
                this.editTextNewItemTitle.setText(diaryViewModel.getLiveItem3Title().getValue());
                break;
            case 4:
                this.editTextNewItemTitle.setText(diaryViewModel.getLiveItem4Title().getValue());
                break;
            case 5:
                this.editTextNewItemTitle.setText(diaryViewModel.getLiveItem5Title().getValue());
                break;
            default:
                this.editTextNewItemTitle.setText("");
        }



        this.buttonSelectNewItemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextNewItemTitle.getText().toString();
                // 入力タイトルの先頭が空白文字以外(\\S)ならアイテムタイトル更新
                if (title.matches("\\S+.*")) {
                    updateItemTitle(title);
                    closeEditFragment();

                } else {
                    // 入力タイトルの先頭が空白文字(\\s)ならエラー表示
                    if (title.matches("\\s+.*")) {
                        editTextNewItemTitle.setError("先頭文字は空白文字以外を入力してください");
                    // それ以外(未入力)ならエラー表示
                    } else {
                        editTextNewItemTitle.setError("1文字以上の文字を入力してください");
                    }

                }
            }
        });

        // 選択履歴リストアイテム設定
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
        editDiarySelectItemTitleViewModel.loadSelectedItemTitleHistory();
        editDiarySelectItemTitleViewModel.getLiveSelectedItemTitleHistory()
                .observe(getViewLifecycleOwner(), new Observer<List<DiaryItemTitle>>() {
                    @Override
                    public void onChanged(List<DiaryItemTitle> diaryItemTitles) {
                        List<String> list = new ArrayList<>();
                        for (DiaryItemTitle diaryItemTitle: diaryItemTitles) {
                            list.add(diaryItemTitle.getTitle());
                        }

                        SelectedItemTitleHistoryAdapter adapter =
                                (SelectedItemTitleHistoryAdapter)
                                        EditDiarySelectItemTitleFragment.this
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
                // 未使用。
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
                    if (editText.getInputType() == (TYPE_TEXT_FLAG_MULTI_LINE + 1)) { // set と get でなぜか 1 ズレている。(公式のリファレンスでもズレあり。)
                        // 処理なし(改行)
                    } else {
                        // キーボードを隠す。
                        hideKeyboard(inputMethodManager, viewBackground);
                        // 自テキストフォーカスクリア。
                        editText.clearFocus();
                    }
                }

                // return true だとバックスペースが機能しなくなり入力文字を削除できなくなる。
                return false;
            }
        });

    }


    //// キーボードを隠す。
    private void hideKeyboard(InputMethodManager inputMethodManager, View touchView) {
        inputMethodManager.hideSoftInputFromWindow(touchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        public TextView textView;

        public SelectedItemTitleHistoryViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_selected_item_title);
        }
    }

    private class SelectedItemTitleHistoryAdapter
            extends RecyclerView.Adapter<SelectedItemTitleHistoryViewHolder> {
        private List<String> list;
        public SelectedItemTitleHistoryAdapter() {
        }

        @Override
        public SelectedItemTitleHistoryViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_selected_item_title_history,
                                         parent,
                              false);
            SelectedItemTitleHistoryViewHolder holder =
                    new SelectedItemTitleHistoryViewHolder(view);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedItemTitle = holder.textView.getText().toString();
                    updateItemTitle(selectedItemTitle);
                    closeEditFragment();

                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(SelectedItemTitleHistoryViewHolder holder, int position) {
            String selectedItemTitle = list.get(position);

            holder.textView.setText(selectedItemTitle);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void changeItem(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }

    // 参考：https://appdev-room.com/android-recyclerview-swipe-action
    private class SelectedItemTitleHistorySimpleCallBack extends ItemTouchHelper.SimpleCallback {
        public SelectedItemTitleHistorySimpleCallBack(int dragDirs,
                                                      int swipeDirs) {
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
            Bundle bundle = new Bundle();
            bundle.putString("SelectedItemTitle",
                             selectedItemTitleHistoryViewHolder.textView.getText().toString());
            bundle.putInt("SelectedItemTitlePos", viewHolder.getAdapterPosition());
            ConfirmDeleteDialogFragment dialogFragment = new ConfirmDeleteDialogFragment();
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getChildFragmentManager(), "ConfirmDeleteDialogFragment");


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

                backgroundColor.setBounds(itemView.getRight() - absDx, itemView.getTop(), iconRight, iconBottom);

                if (absDx >= switchingPoint) {
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                } else {
                    icon.setBounds(defaultIconLeft - absDx, iconTop, defaultIconRight - absDx, iconBottom);

                }

            } else {
                backgroundColor.setBounds(0, 0, 0, 0);
                icon.setBounds(0, 0, 0, 0);

            }

            backgroundColor.draw(c);
            icon.draw(c);

        }
    }

    private void updateItemTitle(String title) {
        this.editDiarySelectItemTitleViewModel
                .updateSavingDiaryItemTitle(this.selectItemNo, title);

        switch (this.selectItemNo) {
            case 1:
                this.diaryViewModel.setLiveItem1Title(title);
                break;
            case 2:
                this.diaryViewModel.setLiveItem2Title(title);
                break;
            case 3:
                this.diaryViewModel.setLiveItem3Title(title);
                break;
            case 4:
                this.diaryViewModel.setLiveItem4Title(title);
                break;
            case 5:
                this.diaryViewModel.setLiveItem5Title(title);
                break;
            default:
        }
    }

    // EditDiarySelectItemTitleFragmentを閉じる
    private void closeEditFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.remove(EditDiarySelectItemTitleFragment.this);
        fragmentTransaction.commit();

        Bundle result = new Bundle();
        fragmentManager.setFragmentResult(
                "ToEditDiaryFragment_EditDiarySelectItemTitleFragmentRequestKey", result);
    }

    // 一つ前のフラグメント(EDitDiaryFragment)を表示
    private void backFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack();

        // EDitDiaryのツールバーのメニュー作成
        Bundle result = new Bundle();
        fragmentManager.setFragmentResult(
                "ToEditDiaryFragment_EditDiarySelectItemTitleFragmentRequestKey", result);
    }

}
