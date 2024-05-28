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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

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
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.databinding.FragmentEditDiaryBinding;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragmentDirections;
import com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle.EditDiarySelectItemTitleFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle.EditDiarySelectItemTitleViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditDiaryFragment extends Fragment {

    // View関係
    private FragmentEditDiaryBinding binding;
    private final int MAX_ITEM_NUM = 5; // 項目入力欄最大数

    // Navigation関係
    private NavController navController;

    // ViewModel
    private DiaryViewModel diaryViewModel;
    private EditDiarySelectItemTitleViewModel editDiarySelectItemTitleViewModel;

    // キーボード関係
    private InputMethodManager inputMethodManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.diaryViewModel = provider.get(DiaryViewModel.class);
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
        this.binding = FragmentEditDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(EditDiaryFragment.this);
        this.binding.setEditDiaryViewModel(diaryViewModel);

        // キーボード設定
        this.inputMethodManager =
                (InputMethodManager) requireActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 項目タイトル入力フラグメントからデータ受取
        SavedStateHandle savedStateHandle =
                this.navController.getCurrentBackStackEntry().getSavedStateHandle();
        MutableLiveData<String> liveDataNewItemTitle =
                savedStateHandle.getLiveData(EditDiarySelectItemTitleFragment.KEY_NEW_ITEM_TITLE);
        liveDataNewItemTitle.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                int itemNumber =
                        savedStateHandle.get(EditDiarySelectItemTitleFragment.KEY_UPDATE_ITEM_NUMBER);
                switch (itemNumber) {
                    case 1:
                        EditDiaryFragment.this.diaryViewModel.setLiveItem1Title(string);
                        break;
                    case 2:
                        EditDiaryFragment.this.diaryViewModel.setLiveItem2Title(string);
                        break;
                    case 3:
                        EditDiaryFragment.this.diaryViewModel.setLiveItem3Title(string);
                        break;
                    case 4:
                        EditDiaryFragment.this.diaryViewModel.setLiveItem4Title(string);
                        break;
                    case 5:
                        EditDiaryFragment.this.diaryViewModel.setLiveItem5Title(string);
                        break;
                    default:
                }
                savedStateHandle.remove(EditDiarySelectItemTitleFragment.KEY_UPDATE_ITEM_NUMBER);
                savedStateHandle.remove(EditDiarySelectItemTitleFragment.KEY_NEW_ITEM_TITLE);
            }
        });


        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry =
                this.navController.getBackStackEntry(R.id.navigation_edit_diary_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    // 日付入力ダイアログフラグメントからデータ受取
                    boolean containsDatePickerDialogFragmentResults =
                            savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_YEAR)
                            && savedStateHandle
                                    .contains(DatePickerDialogFragment.KEY_SELECTED_MONTH)
                            && savedStateHandle
                                    .contains(DatePickerDialogFragment.KEY_SELECTED_DAY_OF_MONTH);
                    if (containsDatePickerDialogFragmentResults) {
                        Integer selectedYear =
                                savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                        Integer selectedMonth =
                                savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                        Integer selectedDayOfMonth =
                                savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_DAY_OF_MONTH);
                        EditDiaryFragment.this.diaryViewModel
                                .updateDate(selectedYear, selectedMonth, selectedDayOfMonth);
                        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DAY_OF_MONTH);
                    }

                    // 既存日記読込ダイアログフラグメントから結果受取
                    boolean containsLoadExistingDiaryDialogFragmentResult =
                            savedStateHandle.contains(LoadExistingDiaryDialogFragment.KEY_LOAD_DIARY_DATE);
                    if (containsLoadExistingDiaryDialogFragmentResult) {
                        String loadDiaryDate =
                                savedStateHandle.get(LoadExistingDiaryDialogFragment.KEY_LOAD_DIARY_DATE);
                        DiaryViewModel _diaryViewModel = EditDiaryFragment.this.diaryViewModel;
                        _diaryViewModel.setLiveLoadingDate(loadDiaryDate);
                        _diaryViewModel.prepareEditDiary();
                        Integer weather1 = _diaryViewModel.getLiveIntWeather1().getValue();
                        EditDiaryFragment.this.binding.spinnerWeather1.setSelection(weather1);
                        Integer weather2 = _diaryViewModel.getLiveIntWeather2().getValue();
                        EditDiaryFragment.this.binding.spinnerWeather2.setSelection(weather2);
                        Integer condition = _diaryViewModel.getLiveIntCondition().getValue();
                        EditDiaryFragment.this.binding.spinnerCondition.setSelection(condition);
                        ActionBar actionBar =
                                ((AppCompatActivity) getActivity()).getSupportActionBar();
                        actionBar.setTitle("編集中");
                        _diaryViewModel.setWasNewEditDiary(true);
                        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_LOAD_DIARY_DATE);
                    }

                    // 既存日記上書きダイアログフラグメントから結果受取
                    boolean containsUpdateExistingDiaryDialogFragmentResult =
                            savedStateHandle
                                    .contains(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    if (containsUpdateExistingDiaryDialogFragmentResult) {
                        EditDiaryFragment.this.diaryViewModel
                                .deleteExistingDiaryAndUpdateExistingDiary();
                        changeToShowDiaryFragment();
                        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    }

                    // 項目削除確認ダイアログフラグメントから結果受取
                    boolean containsDeleteConfirmDialogFragmentDialogFragmentResult =
                            savedStateHandle
                                    .contains(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    if (containsDeleteConfirmDialogFragmentDialogFragmentResult) {
                        Integer deleteItemNumber = savedStateHandle
                                .get(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                        EditDiaryFragment.this.diaryViewModel.deleteItem(deleteItemNumber);
                        EditDiaryFragment.this.editDiarySelectItemTitleViewModel
                                .deleteSavingDiaryItemTitle(deleteItemNumber);
                        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
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
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DAY_OF_MONTH);
                    savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_LOAD_DIARY_DATE);
                    savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });


        // ツールバー設定
        if (diaryViewModel.getIsNewEditDiary()) {
            this.binding.materialToolbarTopAppBar.setTitle("新規作成");
        } else {
            this.binding.materialToolbarTopAppBar.setTitle("編集中");
        }
        this.binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //戻る。
                        EditDiaryFragment.this.navController.navigateUp();
                    }
                });
        this.binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //日記保存(日記表示フラグメント起動)。
                        if (item.getItemId() == R.id.editDiaryToolbarOptionSaveDiary) {
                            boolean isNewDiary =
                                    EditDiaryFragment.this.diaryViewModel.getIsNewEditDiary();
                            String loadingDate =
                                    EditDiaryFragment.this.diaryViewModel.getLiveLoadingDate().getValue();
                            String savingDate =
                                    EditDiaryFragment.this.diaryViewModel.getLiveDate().getValue();
                            if (isNewDiary) {
                                if (EditDiaryFragment.this.diaryViewModel.hasDiary(savingDate)) {
                                    Log.d("保存形式確認", "新規上書き保存");
                                    startUpdateExistingDiaryDialogFragment(savingDate);
                                } else {
                                    Log.d("保存形式確認", "新規保存");
                                    EditDiaryFragment.this.diaryViewModel.saveNewDiary();
                                    changeToShowDiaryFragment();
                                }
                            } else {
                                if (loadingDate.equals(savingDate)) {
                                    Log.d("保存形式確認", "上書き保存");
                                    EditDiaryFragment.this.diaryViewModel.updateExistingDiary();
                                    changeToShowDiaryFragment();
                                } else {
                                    if(EditDiaryFragment.this.diaryViewModel.hasDiary(savingDate)) {
                                        Log.d("保存形式確認", "日付変更上書き保存");
                                        startUpdateExistingDiaryDialogFragment(savingDate);
                                    } else {
                                        Log.d("保存形式確認", "日付変更新規保存");
                                        EditDiaryFragment.this.diaryViewModel
                                                .deleteExistingDiaryAndSaveNewDiary();
                                        changeToShowDiaryFragment();
                                    }
                                }
                            }
                            EditDiaryFragment.this.editDiarySelectItemTitleViewModel
                                    .updateSelectedItemTitleHistory();

                            return true;
                        }
                        return false;
                    }
                });


        // 画面表示データ準備
        if (this.diaryViewModel.getRequiresPreparationDiary()) {
            this.diaryViewModel.prepareEditDiary();
        } else {
            this.diaryViewModel.setRequiresPreparationDiary(true);
        }


        // 項目入力欄関係Viewを配列に格納
        TextView[] textItems = new TextView[MAX_ITEM_NUM];
        EditText[] editTextItemsTitle = new EditText[MAX_ITEM_NUM];
        NestedScrollView[] nestedScrollItemsComment = new NestedScrollView[MAX_ITEM_NUM];
        EditText[] editTextItemsComment = new EditText[MAX_ITEM_NUM];
        TextView[] textItemsCommentLength = new TextView[MAX_ITEM_NUM];
        ImageButton[] imageButtonItemsDelete = new ImageButton[MAX_ITEM_NUM];

        textItems[0] = this.binding.textItem1;
        editTextItemsTitle[0] = this.binding.editTextItem1Title;
        nestedScrollItemsComment[0] = this.binding.nestedScrollItem1Comment;
        editTextItemsComment[0] = this.binding.editTextItem1Comment;
        textItemsCommentLength[0] = this.binding.textItem1CommentLength;
        imageButtonItemsDelete[0] = this.binding.imageButtonItem1Delete;

        textItems[1] = this.binding.textItem2;
        editTextItemsTitle[1] = this.binding.editTextItem2Title;
        nestedScrollItemsComment[1] = this.binding.nestedScrollItem2Comment;
        editTextItemsComment[1] = this.binding.editTextItem2Comment;
        textItemsCommentLength[1] = this.binding.textItem2CommentLength;
        imageButtonItemsDelete[1] = this.binding.imageButtonItem2Delete;

        textItems[2] = this.binding.textItem3;
        editTextItemsTitle[2] = this.binding.editTextItem3Title;
        nestedScrollItemsComment[2] = this.binding.nestedScrollItem3Comment;
        editTextItemsComment[2] = this.binding.editTextItem3Comment;
        textItemsCommentLength[2] = this.binding.textItem3CommentLength;
        imageButtonItemsDelete[2] = this.binding.imageButtonItem3Delete;

        textItems[3] = this.binding.textItem4;
        editTextItemsTitle[3] = this.binding.editTextItem4Title;
        nestedScrollItemsComment[3] = this.binding.nestedScrollItem4Comment;
        editTextItemsComment[3] = this.binding.editTextItem4Comment;
        textItemsCommentLength[3] = this.binding.textItem4CommentLength;
        imageButtonItemsDelete[3] = this.binding.imageButtonItem4Delete;

        textItems[4] = this.binding.textItem5;
        editTextItemsTitle[4] = this.binding.editTextItem5Title;
        nestedScrollItemsComment[4] = this.binding.nestedScrollItem5Comment;
        editTextItemsComment[4] = this.binding.editTextItem5Comment;
        textItemsCommentLength[4] = this.binding.textItem5CommentLength;
        imageButtonItemsDelete[4] = this.binding.imageButtonItem5Delete;

        // キーボード入力不要View
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(this.binding.editTextDate);
        noKeyboardViews.add(this.binding.spinnerWeather1);
        noKeyboardViews.add(this.binding.spinnerWeather2);
        noKeyboardViews.add(this.binding.spinnerCondition);
        for (int i = 0; i < MAX_ITEM_NUM; i++) {
            noKeyboardViews.add(editTextItemsTitle[i]);
            noKeyboardViews.add(imageButtonItemsDelete[i]);
        }
        noKeyboardViews.add(this.binding.imageButtonAddItem);


        //日付入力欄。
        this.binding.editTextDate.setFocusable(true);
        this.binding.editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action =
                        EditDiaryFragmentDirections.actionEditDiaryFragmentToDatePickerDialog();
                EditDiaryFragment.this.navController.navigate(action);
            }
        });

        this.diaryViewModel.getLiveDate().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (EditDiaryFragment.this.diaryViewModel.hasDiary(s)) {
                    String loadingDate =
                            EditDiaryFragment.this.diaryViewModel.loadingDate.getValue();
                    String inputDate =
                            EditDiaryFragment.this.diaryViewModel.date.getValue();
                    if (!loadingDate.equals(inputDate)) {
                        NavDirections action =
                                EditDiaryFragmentDirections
                                        .actionEditDiaryFragmentToLoadExistingDiaryDialog(s);
                        EditDiaryFragment.this.navController.navigate(action);
                    }
                }
            }
        });


        // 天気入力欄。
        // その他 ViewModel にて処理
        // TODO:下記MEMOの意味が理解できないので後で確認ご文章を修正する
        // MEMO:下記 onItemSelected は DataBinding を使用して ViewModel 内にメソッドを用意していたが、
        //      画面作成処理時に onItemSelected が処理される為、初期値設定する為の setSelection メソッドの処理タイミングの兼合いで、
        //      DataBinding での使用を取りやめ、ここにまとめて記載することにした。
        //      他スピナーも同様。
        this.binding.spinnerWeather1
                .setSelection(this.diaryViewModel.getLiveIntWeather1().getValue());
        this.binding.spinnerWeather1
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        EditDiaryFragment.this.diaryViewModel.setLiveIntWeather1(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });


        this.binding.spinnerWeather2
                .setSelection(this.diaryViewModel.getLiveIntWeather2().getValue());
        this.binding.spinnerWeather2
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        EditDiaryFragment.this.diaryViewModel.setLiveIntWeather2(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });

        this.diaryViewModel.getLiveIntWeather1()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        EditDiaryFragment.this.diaryViewModel.updateStrWeather1();
                        if (integer != 0) {
                            EditDiaryFragment.this.binding.spinnerWeather2.setEnabled(true);
                        } else {
                            EditDiaryFragment.this.binding.spinnerWeather2.setEnabled(false);
                            EditDiaryFragment.this.diaryViewModel.setLiveIntWeather2(0);
                            EditDiaryFragment.this.binding.spinnerWeather2.setSelection(0);
                        }
                    }
                });

        this.diaryViewModel.getLiveIntWeather2()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        EditDiaryFragment.this.diaryViewModel.updateStrWeather2();
                    }
                });


        // 気分入力欄。
        // その他 ViewModel にて処理
        this.binding.spinnerCondition
                .setSelection(this.diaryViewModel.getLiveIntCondition().getValue());
        this.binding.spinnerCondition
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        EditDiaryFragment.this.diaryViewModel.setLiveIntCondition(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });

        this.diaryViewModel.getLiveIntCondition()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        EditDiaryFragment.this.diaryViewModel.updateStrCondition();
                    }
                });


        // タイトル入力欄設定
        setupEditText(
                this.binding.editTextTitle,
                this.binding.textTitleLength,
                15,
                this.inputMethodManager,
                this.binding.nestedScrollFullScreen, // 背景View
                noKeyboardViews
        );


        // 項目設定
        // 項目タイトル入力欄設定
        for (int i = 0; i < editTextItemsTitle.length; i++) {
            int inputItemNo =  i + 1;
            editTextItemsTitle[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditDiaryFragment.this.diaryViewModel.setRequiresPreparationDiary(false);

                    // 項目タイトル入力フラグメント起動
                    String inputItemTitle;
                    switch (inputItemNo) {
                        case 1:
                            inputItemTitle =
                                    EditDiaryFragment.this.diaryViewModel
                                            .getLiveItem1Title().getValue();
                            break;
                        case 2:
                            inputItemTitle =
                                    EditDiaryFragment.this.diaryViewModel
                                            .getLiveItem2Title().getValue();
                            break;
                        case 3:
                            inputItemTitle =
                                    EditDiaryFragment.this.diaryViewModel
                                            .getLiveItem3Title().getValue();
                            break;
                        case 4:
                            inputItemTitle =
                                    EditDiaryFragment.this.diaryViewModel
                                            .getLiveItem4Title().getValue();
                            break;
                        case 5:
                            inputItemTitle =
                                    EditDiaryFragment.this.diaryViewModel
                                            .getLiveItem5Title().getValue();
                            break;
                        default:
                            inputItemTitle = "";
                    }
                    NavDirections action =
                            EditDiaryFragmentDirections
                                    .actionEditDiaryFragmentToSelectItemTitleFragment(
                                            inputItemNo, inputItemTitle);
                    EditDiaryFragment.this.navController.navigate(action);
                }
            });
        }

        // 項目コメント入力欄設定。
        for (int i = 0; i < this.MAX_ITEM_NUM; i++) {
            setupEditText(
                    editTextItemsComment[i],
                    textItemsCommentLength[i],
                    50,
                    inputMethodManager,
                    binding.nestedScrollFullScreen, // 背景View,
                    noKeyboardViews
            );
        }

        // 項目追加ボタン設定
        // ViewModel にて処理

        // 項目削除ボタン設定
        for (int i = 0; i < this.MAX_ITEM_NUM; i++) {
            int deleteItemNumber = i + 1;
            imageButtonItemsDelete[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavDirections action =
                            EditDiaryFragmentDirections
                                    .actionEditDiaryFragmentToDeleteConfirmationDialog(deleteItemNumber);
                    EditDiaryFragment.this.navController.navigate(action);
                }
            });
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


    // キーボードを隠す。
    private void hideKeyboard(InputMethodManager inputMethodManager, View touchView) {
        inputMethodManager
                .hideSoftInputFromWindow(
                        touchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToUpdateExistingDiaryDialog(savingDate);
        this.navController.navigate(action);
    }

    public void changeToShowDiaryFragment() {
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        NavDirections action;
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるactionを切り替える。
        if (isStartDiaryFragment) {
            action = EditDiaryFragmentDirections.actionEditDiaryFragmentToShowDiaryFragmentPattern2();
        } else {
            action = EditDiaryFragmentDirections.actionEditDiaryFragmentToShowDiaryFragmentPattern1();
        }
        this.navController.navigate(action);
    }

}
