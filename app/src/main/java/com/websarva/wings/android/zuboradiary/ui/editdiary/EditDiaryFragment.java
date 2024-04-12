package com.websarva.wings.android.zuboradiary.ui.editdiary;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.websarva.wings.android.zuboradiary.databinding.FragmentEditDiaryBinding;
import com.websarva.wings.android.zuboradiary.ui.ChangeFragment;
import com.websarva.wings.android.zuboradiary.ui.calendar.CalendarFragment;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragment;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle.EditDiarySelectItemTitleFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle.EditDiarySelectItemTitleViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.ListFragment;

import java.util.ArrayList;
import java.util.List;

public class EditDiaryFragment extends Fragment {

    private FragmentEditDiaryBinding binding;
    private InputMethodManager inputMethodManager;
    private String diaryDateConfirmedLoading = "";
    private final int MAX_ITEM_NUM = 5;
    private int showItemNum = 1;
    private EditText editTextDate;
    private Spinner spinnerWeather1;
    private Spinner spinnerWeather2;
    private Spinner spinnerCondition;
    private EditText editTextTitle;
    private TextView textTitleLength;
    private TextView[] textItems;
    private EditText[] editTextItemsTitle;
    private NestedScrollView[] nestedScrollItemsComment;
    private EditText[] editTextItemsComment;
    private TextView[] textItemsCommentLength;
    private ImageButton[] imageButtonItemsDelete;
    private ImageButton imageButtonAddItem;
    private EditDiaryViewModel diaryViewModel;
    private EditDiarySelectItemTitleViewModel editDiarySelectItemTitleViewModel;
    private MenuProvider editDiaryMenuProvider = new EditDiaryMenuProvider();


    public EditDiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryViewModel = provider.get(EditDiaryViewModel.class);
        editDiarySelectItemTitleViewModel = provider.get(EditDiarySelectItemTitleViewModel.class);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment();

                    }
                }
        );

        // 項目タイトル入力フラグメントからデータ受取
        // https://developer.android.com/guide/fragments/communicate#pass-between-fragments
        getChildFragmentManager().setFragmentResultListener(
                "ToEditDiaryFragment_EditDiarySelectItemTitleFragmentRequestKey",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        createMenu();
                    }
                }
        );

        // 日付入力ダイアログフラグメントからデータ受取
        // https://developer.android.com/guide/fragments/communicate#pass-parent-child
        getChildFragmentManager().setFragmentResultListener(
                "DatePickerDialogRequestKey",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        int selectYear = result.getInt("SelectYear");
                        int selectMonth = result.getInt("SelectMonth") + 1;
                        int selectDayOfMonth = result.getInt("SelectDayOfMonth");
                        diaryViewModel.updateDate(selectYear, selectMonth, selectDayOfMonth);
                    }
                }
        );

        // 既存日記読込ダイアログフラグメント結果処理
        getChildFragmentManager().setFragmentResultListener(
                "EditDiaryLoadExistingDiaryDialogRequestKey",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        diaryViewModel.setLiveLoadingDate(result.getString("Date"));
                        diaryViewModel.prepareEditDiary();
                        spinnerWeather1.setSelection(diaryViewModel.getLiveIntWeather1().getValue());
                        spinnerWeather2.setSelection(diaryViewModel.getLiveIntWeather2().getValue());
                        spinnerCondition.setSelection(diaryViewModel.getLiveIntCondition().getValue());
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        actionBar.setTitle("編集中");
                        diaryViewModel.setWasNewEditDiary(true);
                    }
                }
        );


        // 既存日記上書ダイアログフラグメント結果処理
        getChildFragmentManager().setFragmentResultListener(
                "EditDiaryUpdateExistingDiaryDialogRequestKey",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        diaryViewModel.deleteExistingDiaryAndUpdateExistingDiary();
                        changeToShowDiaryFragment();
                    }
                }
        );

        // 削除確認ダイアログフラグメントからデータ受取
        // https://developer.android.com/guide/fragments/communicate#pass-parent-child
        getChildFragmentManager().setFragmentResultListener(
                "DeleteConfirmDialogRequestKey",
                this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        int deleteItemNo = result.getInt("DeleteItemNo");
                        EditDiaryFragment.this.diaryViewModel.deleteItem(deleteItemNo);
                        EditDiaryFragment.this.editDiarySelectItemTitleViewModel
                                             .deleteSavingDiaryItemTitle(deleteItemNo);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_edit_diary, container, false);

        binding = FragmentEditDiaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //データバインディング設定(ビューモデルのライブデータ画面反映設定)
        binding.setLifecycleOwner(EditDiaryFragment.this);
        binding.setEditDiaryViewModel(diaryViewModel);

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //アクションバーオプションメニュー更新。
        createMenu();


        // 画面表示データ準備
        if (diaryViewModel.getRequiresPreparationDiary()) {
            Log.d("RequiresPreparation", "true");
            diaryViewModel.prepareEditDiary();
        } else {
            Log.d("RequiresPreparation", "false");
            diaryViewModel.setRequiresPreparationDiary(true);
        }


        //日付入力欄 View 取得。
        editTextDate = binding.editTextDate;
        //天気入力欄 View 取得。
        spinnerWeather1 = binding.spinnerWeather1;
        spinnerWeather2 = binding.spinnerWeather2;
        //気分入力欄 View 取得。
        spinnerCondition = binding.spinnerCondition;
        //タイトル入力欄 View 取得。
        editTextTitle = binding.editTextTitle;
        textTitleLength = binding.textTitleLength;

        //項目入力欄 View 取得。
        textItems = new TextView[MAX_ITEM_NUM];
        editTextItemsTitle = new EditText[MAX_ITEM_NUM];
        nestedScrollItemsComment = new NestedScrollView[MAX_ITEM_NUM];
        editTextItemsComment = new EditText[MAX_ITEM_NUM];
        textItemsCommentLength = new TextView[MAX_ITEM_NUM];
        imageButtonItemsDelete = new ImageButton[MAX_ITEM_NUM];

        textItems[0] = binding.textItem1;
        editTextItemsTitle[0] = binding.editTextItem1Title;
        nestedScrollItemsComment[0] = binding.nestedScrollItem1Comment;
        editTextItemsComment[0] = binding.editTextItem1Comment;
        textItemsCommentLength[0] = binding.textItem1CommentLength;
        imageButtonItemsDelete[0] = binding.imageButtonItem1Delete;

        textItems[1] = binding.textItem2;
        editTextItemsTitle[1] = binding.editTextItem2Title;
        nestedScrollItemsComment[1] = binding.nestedScrollItem2Comment;
        editTextItemsComment[1] = binding.editTextItem2Comment;
        textItemsCommentLength[1] = binding.textItem2CommentLength;
        imageButtonItemsDelete[1] = binding.imageButtonItem2Delete;

        textItems[2] = binding.textItem3;
        editTextItemsTitle[2] = binding.editTextItem3Title;
        nestedScrollItemsComment[2] = binding.nestedScrollItem3Comment;
        editTextItemsComment[2] = binding.editTextItem3Comment;
        textItemsCommentLength[2] = binding.textItem3CommentLength;
        imageButtonItemsDelete[2] = binding.imageButtonItem3Delete;

        textItems[3] = binding.textItem4;
        editTextItemsTitle[3] = binding.editTextItem4Title;
        nestedScrollItemsComment[3] = binding.nestedScrollItem4Comment;
        editTextItemsComment[3] = binding.editTextItem4Comment;
        textItemsCommentLength[3] = binding.textItem4CommentLength;
        imageButtonItemsDelete[3] = binding.imageButtonItem4Delete;

        textItems[4] = binding.textItem5;
        editTextItemsTitle[4] = binding.editTextItem5Title;
        nestedScrollItemsComment[4] = binding.nestedScrollItem5Comment;
        editTextItemsComment[4] = binding.editTextItem5Comment;
        textItemsCommentLength[4] = binding.textItem5CommentLength;
        imageButtonItemsDelete[4] = binding.imageButtonItem5Delete;

        imageButtonAddItem = binding.imageButtonAddItem;

        //背景 View 取得。
        NestedScrollView nestedScrollFullScreen = binding.nestedScrollFullScreen;

        //キーボード入力を必要としない入力 View 。
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(editTextDate);
        noKeyboardViews.add(spinnerWeather1);
        noKeyboardViews.add(spinnerWeather2);
        noKeyboardViews.add(spinnerCondition);
        for (int i = 0; i < MAX_ITEM_NUM; i++) {
            noKeyboardViews.add(editTextItemsTitle[i]);
            noKeyboardViews.add(imageButtonItemsDelete[i]);
        }
        noKeyboardViews.add(imageButtonAddItem);


        //日付入力欄。
        editTextDate.setFocusable(true);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialogFragment dialogFragment = new DatePickerDialogFragment();
                dialogFragment.show(getChildFragmentManager(), "DatePicker");
            }
        });

        diaryViewModel.getLiveDate().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (diaryViewModel.hasDiary(s)) {
                    if (!(diaryViewModel.loadingDate.getValue().equals(diaryViewModel.date.getValue()))) {
                        Bundle bundle = new Bundle();
                        bundle.putString("Date", s);
                        LoadExistingDiaryDialogFragment dialogFragment = new LoadExistingDiaryDialogFragment();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(getChildFragmentManager(), "ExistsDiaryDialogFragment");
                    }
                }
            }
        });


                // 天気入力欄。
                // その他 ViewModel にて処理
                // メモ
                // 下記 onItemSelected は DataBinding を使用して ViewModel 内にメソッドを用意していたが、
                // 画面作成処理時に onItemSelected が処理される為、初期値設定する為の setSelection メソッドの処理タイミングの兼合いで、
                // DataBinding での使用を取りやめ、ここにまとめて記載することにした。
                // 他スピナーも同様。
                spinnerWeather1.setSelection(diaryViewModel.getLiveIntWeather1().getValue());
        spinnerWeather1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                diaryViewModel.setLiveIntWeather1(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerWeather2.setSelection(diaryViewModel.getLiveIntWeather2().getValue());
        spinnerWeather2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                diaryViewModel.setLiveIntWeather2(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        diaryViewModel.getLiveIntWeather1().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if ((integer != 0) && (integer != null)) {
                    spinnerWeather2.setEnabled(true);
                } else {
                    spinnerWeather2.setEnabled(false);
                    diaryViewModel.setLiveIntWeather2(0);
                    spinnerWeather2.setSelection(0);
                }
            }
        });

        diaryViewModel.getLiveIntWeather1().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather1();
            }
        });

        diaryViewModel.getLiveIntWeather2().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather2();
            }
        });


        // 気分入力欄。
        // その他 ViewModel にて処理
        spinnerCondition.setSelection(diaryViewModel.getLiveIntCondition().getValue());
        spinnerCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                diaryViewModel.setLiveIntCondition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        diaryViewModel.getLiveIntCondition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrCondition();
            }
        });


        // タイトル入力欄。
        setupEditText(editTextTitle, textTitleLength, 15, inputMethodManager, nestedScrollFullScreen, noKeyboardViews);


        // 項目設定
        // 項目タイトル入力欄。
        for (int i = 0; i < editTextItemsTitle.length; i++) {
            int selectItemNo =  i + 1;
            editTextItemsTitle[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    diaryViewModel.setRequiresPreparationDiary(false);

                    // 項目タイトル入力フラグメント起動
                    Bundle bundle = new Bundle();
                    bundle.putInt("SelectItemNo", selectItemNo);
                    FragmentManager fragmentManager = getChildFragmentManager();

                    MenuHost menuHost = requireActivity();
                    menuHost.removeMenuProvider(editDiaryMenuProvider);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setReorderingAllowed(true);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.add(R.id.fragment_container_child, EditDiarySelectItemTitleFragment.class, bundle);
                    fragmentTransaction.commit();

                }
            });
        }

        // 項目コメント入力欄設定。
        for (int i = 0; i < MAX_ITEM_NUM; i++) {
            setupEditText(editTextItemsComment[i], textItemsCommentLength[i], 50, inputMethodManager, nestedScrollFullScreen, noKeyboardViews);
        }

        // 項目追加。
        // ViewModel にて処理

        //項目削除。
        for (int i = 0; i < MAX_ITEM_NUM; i++) {
            int deleteItemNo = i + 1;
            imageButtonItemsDelete[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("DeleteItemNo", deleteItemNo);
                    DeleteConfirmDialogFragment dialogFragment = new DeleteConfirmDialogFragment();
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(getChildFragmentManager(), "DeleteConfirmDialog");
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("20240322", "EditDiaryFragment_onResume()処理");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("20240325", "EditDiaryFragment_onPause()処理");
    }

    private void createMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                editDiaryMenuProvider,
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }

    private class EditDiaryMenuProvider implements MenuProvider {
        //アクションバーオプションメニュー設定。
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            menuInflater.inflate(R.menu.edit_diary_toolbar_menu, menu);

            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(null);

            if (diaryViewModel.getIsNewEditDiary().booleanValue()) {
                actionBar.setTitle("新規作成");
            } else {
                actionBar.setTitle("編集中");
            }

        }

        //アクションバーメニュー選択処理設定。
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            //日記保存(日記表示フラグメント起動)。
            if (menuItem.getItemId() == R.id.editDiaryToolbarOptionSaveDiary) {
                Toast.makeText(getView().getContext(), menuItem.toString(), Toast.LENGTH_SHORT).show();

                boolean isNewDiary = diaryViewModel.getIsNewEditDiary().booleanValue();
                String loadingDate = diaryViewModel.getLiveLoadingDate().getValue();
                String savingDate = diaryViewModel.getLiveDate().getValue();
                if (isNewDiary) {
                    if (diaryViewModel.hasDiary(savingDate)) {
                        Log.d("保存形式確認", "新規上書き保存");
                        startUpdateExistingDiaryDialogFragment(savingDate);
                    } else {
                        Log.d("保存形式確認", "新規保存");
                        diaryViewModel.saveNewDiary();
                        changeToShowDiaryFragment();
                    }
                } else {
                    if (loadingDate.equals(savingDate)) {
                        Log.d("保存形式確認", "上書き保存");
                        diaryViewModel.updateExistingDiary();
                        changeToShowDiaryFragment();
                    } else {
                        if(diaryViewModel.hasDiary(savingDate)) {
                            Log.d("保存形式確認", "日付変更上書き保存");
                            startUpdateExistingDiaryDialogFragment(savingDate);
                        } else {
                            Log.d("保存形式確認", "日付変更新規保存");
                            diaryViewModel.deleteExistingDiaryAndSaveNewDiary();
                            changeToShowDiaryFragment();
                        }
                    }
                }

                EditDiaryFragment.this.editDiarySelectItemTitleViewModel
                        .updateSelectedItemTitleHistory();

                return true;

                //戻る。
            } else if (menuItem.getItemId() == android.R.id.home) {
                backFragment();
                return true;

            } else {
                return false;
            }
        }
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
    public void showTextLength(TextView textView, TextView textLength) {
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


    public void startUpdateExistingDiaryDialogFragment(String savingDate) {
        Bundle bundle = new Bundle();
        bundle.putString("Date", savingDate);
        UpdateExistingDiaryDialogFragment dialogFragment = new UpdateExistingDiaryDialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "UpdateDiaryDialogFragment");
    }

    public void changeToShowDiaryFragment() {

        // ナビフラグメント取得
        FragmentManager activityFragmentManager = requireActivity().getSupportFragmentManager();
        Fragment navFragment = activityFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        Fragment navChildFragment = navFragment.getChildFragmentManager().getFragments().get(0);

        //ナビフラグメントがリストフラグメントの時、リスト更新。
        if (navChildFragment instanceof ListFragment) {
            ListFragment listFragment = (ListFragment) navChildFragment;
            listFragment.updateList();
        }

        FragmentManager parentFragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = parentFragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        //fragmentTransaction.addToBackStack(null); // ShowDiaryFragmentから戻る必要が無い為削除。
        fragmentTransaction.replace(
                R.id.front_fragmentContainerView_activity_main, ShowDiaryFragment.class, null);
        fragmentTransaction.commit();
    }

    // 一つ前のフラグメント(EDitDiaryFragment)を表示
    public void backFragment() {
        Toast.makeText(getView().getContext(), "戻る", Toast.LENGTH_SHORT).show();
        FragmentManager parentFragmentManager = getParentFragmentManager();


        // ナビフラグメント取得
        FragmentManager activityFragmentManager = requireActivity().getSupportFragmentManager();
        Fragment navFragment = activityFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        Fragment navChildFragment = navFragment.getChildFragmentManager().getFragments().get(0);

        // HACK:EditDiaryFragmentからListFragment(NavFragment)を表示した時、
        //      ListFragmentとEditDiaryFragmentのメニューバーが混在する。
        //      ListFragmentResultListenerでメニューバーの更新を設定しているが、
        //      ListFragmentがonResume状態で背面に存在するため、
        //      EditDiaryFragmentがonPause状態になる前に、
        //      ListFragmentResultListenerが起動して一時的に混在すると思われる。
        //      対策として下記コードを記述する。
        MenuHost menuHost = requireActivity();
        menuHost.removeMenuProvider(editDiaryMenuProvider);

        if (navChildFragment instanceof ListFragment) {
            // EditDiaryが新規作成中時の処理
            if (diaryViewModel.getIsNewEditDiary() || diaryViewModel.getWasNewEditDiary()) {

                Bundle result = new Bundle();
                parentFragmentManager.setFragmentResult(
                        "ToListFragment_EditDiaryFragmentRequestKey", result);

                // TODO:EditDiaryを起動す時に処理するか、閉じるときに処理するか保留。
                diaryViewModel.clear();

            }
        }

        if (navChildFragment instanceof CalendarFragment) {
            // EditDiaryが新規作成中の時の処理
            Bundle result = new Bundle();
            parentFragmentManager.setFragmentResult(
                    "ToCalendarFragment_EditDiaryFragmentRequestKey", result);

            // TODO:EditDiaryを起動す時に処理するか、閉じるときに処理するか保留。
            diaryViewModel.clear();

        }

        ChangeFragment.popBackStackOnFrontFragment(parentFragmentManager);

    }

}
