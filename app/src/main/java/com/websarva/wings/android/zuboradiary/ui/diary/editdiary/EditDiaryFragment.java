package com.websarva.wings.android.zuboradiary.ui.diary.editdiary;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
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
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.databinding.FragmentEditDiaryBinding;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.EditDiarySelectItemTitleFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditDiaryFragment extends Fragment {

    // View関係
    private FragmentEditDiaryBinding binding;
    private final int MAX_ITEMS_COUNT = DiaryViewModel.MAX_ITEMS; // 項目入力欄最大数
    private boolean isDeletingItemTransition = false;
    private final String TOOL_BAR_TITLE_NEW = "新規作成";
    private final String TOOL_BAR_TITLE_EDIT = "編集中";
    private String lastTextDate = "";

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiarySavingErrorDialog;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;
    private boolean shouldShowWeatherLoadingErrorDialog;


    // ViewModel
    private DiaryViewModel diaryViewModel;
    private SettingsViewModel settingsViewModel;

    // キーボード関係
    private InputMethodManager inputMethodManager;

    // 位置情報
    private double latitude = -1;
    private double longitude = -1;
    private boolean shouldPrepareWeatherSelection = false;

    // 上書保存方法
    public static final int UPDATE_TYPE_UPDATE_ONLY = 0;
    public static final int UPDATE_TYPE_DELETE_AND_UPDATE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryViewModel = provider.get(DiaryViewModel.class);
        settingsViewModel = provider.get(SettingsViewModel.class);
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        if (isStartDiaryFragment) {
            diaryViewModel.initialize();
        }

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // データバインディング設定
        this.binding = FragmentEditDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(EditDiaryFragment.this);
        this.binding.setDiaryViewModel(diaryViewModel);

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
                EditDiaryFragment.this.diaryViewModel.getItem(itemNumber).setTitle(string);
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
                            savedStateHandle.contains(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    if (containsLoadExistingDiaryDialogFragmentResult) {
                        String date = diaryViewModel.getLiveDate().getValue();
                        Integer selectedButton =
                                savedStateHandle.get(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                        if (selectedButton != null) {
                            if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                                diaryViewModel.initialize();
                                LocalDate loadDiaryDate = DateConverter.toLocalDate(date);
                                diaryViewModel
                                        .prepareDiary(
                                                loadDiaryDate.getYear(),
                                                loadDiaryDate.getMonthValue(),
                                                loadDiaryDate.getDayOfMonth(),
                                                true
                                        );
                            } else {
                                String loadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
                                Boolean isChecked =
                                        settingsViewModel.getIsCheckedGettingWeatherInformationLiveData().getValue();
                                if (loadedDate != null && loadedDate.isEmpty() && isChecked != null && isChecked) {
                                    prepareWeatherSelection(date);
                                }
                            }
                        }
                        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);


                    }

                    // 既存日記上書きダイアログフラグメントから結果受取
                    boolean containsUpdateExistingDiaryDialogFragmentResults =
                            savedStateHandle
                                    .contains(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON)
                                    && savedStateHandle
                                    .contains(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
                    if (containsUpdateExistingDiaryDialogFragmentResults) {
                        int updateType =
                                savedStateHandle.get(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
                        boolean isSuccessful = false;
                        switch (updateType) {
                            case UPDATE_TYPE_DELETE_AND_UPDATE:
                                Log.d("保存形式確認", "日付変更上書保存");
                                isSuccessful = diaryViewModel.deleteExistingDiaryAndSaveDiary();
                                break;
                            case UPDATE_TYPE_UPDATE_ONLY:
                            default:
                                Log.d("保存形式確認", "上書保存");
                                isSuccessful = diaryViewModel.saveDiary();
                                break;
                        }
                        if (isSuccessful) {
                            changeToShowDiaryFragment();
                        }
                        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
                    }

                    // 項目削除確認ダイアログフラグメントから結果受取
                    boolean containsDeleteConfirmDialogFragmentDialogFragmentResult =
                            savedStateHandle
                                    .contains(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    if (containsDeleteConfirmDialogFragmentDialogFragmentResult) {
                        Integer deleteItemNumber = savedStateHandle
                                .get(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);

                        if (deleteItemNumber == 1
                                && EditDiaryFragment.this.diaryViewModel.getVisibleItemsCount()
                                == deleteItemNumber) {
                            EditDiaryFragment.this.diaryViewModel.deleteItem(deleteItemNumber);
                        } else {
                            EditDiaryFragment.this.isDeletingItemTransition = true;
                            hideItem(deleteItemNumber, false);
                        }
                        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    }

                    if (canShowDialog()) {
                        retryErrorDialogShow();
                    }

                    // 天気情報読込ダイアログフラグメントから結果受取
                    boolean containsWeatherInformationDialogFragmentResult =
                            savedStateHandle.contains(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
                    if (containsWeatherInformationDialogFragmentResult) {
                        String date = diaryViewModel.getLiveDate().getValue();
                        Integer selectedButton =
                                savedStateHandle.get(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
                        if (selectedButton != null) {
                            if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                                LocalDate loadDiaryDate = DateConverter.toLocalDate(date);
                                diaryViewModel.prepareWeatherSelection(
                                        loadDiaryDate.getYear(),
                                        loadDiaryDate.getMonthValue(),
                                        loadDiaryDate.getDayOfMonth(),
                                        settingsViewModel.getLatitude(),
                                        settingsViewModel.getLongitude()
                                );
                            }
                        }
                        savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);


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
                    savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });


        // 画面表示データ準備
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        boolean isLoadingExistingDiary =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsLoadingDiary();
        int editDiaryDateYear =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getEditDiaryDateYear();
        int editDiaryDateMonth =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getEditDiaryDateMonth();
        int editDiaryDateDayOfMonth =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getEditDiaryDateDayOfMonth();
        if (isStartDiaryFragment && !this.diaryViewModel.getHasPreparedDiary()) {
            this.diaryViewModel.prepareDiary(
                    editDiaryDateYear,
                    editDiaryDateMonth,
                    editDiaryDateDayOfMonth,
                    isLoadingExistingDiary
            );
        }
        diaryViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (!s.isEmpty()) {
                            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
                            setupItemLayout();
                        }
                    }
                });


        // ツールバー設定
        boolean isNewDiary = diaryViewModel.getLoadedDateLiveData().getValue().isEmpty();
        if (isNewDiary) {
            this.binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
        } else {
            this.binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
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
                            boolean isNewDiary = diaryViewModel.getLoadedDateLiveData().getValue().isEmpty();
                            String stringLoadedDate =
                                    diaryViewModel.getLoadedDateLiveData().getValue();
                            String stringSavingDate =
                                    diaryViewModel.getLiveDate().getValue();
                            boolean isMatchedDate =
                                    stringLoadedDate.equals(stringSavingDate);
                            LocalDate savingDate = DateConverter.toLocalDate(stringSavingDate);



                            boolean isUpdateDiary =
                                    diaryViewModel.hasDiary(
                                            savingDate.getYear(),
                                            savingDate.getMonthValue(),
                                            savingDate.getDayOfMonth()
                                    );
                            if (isUpdateDiary) {
                                int updateType;
                                if (isMatchedDate) {
                                    Log.d("保存形式確認", "上書保存");
                                     boolean isSuccessful = diaryViewModel.saveDiary();
                                     if (isSuccessful) {
                                         changeToShowDiaryFragment();
                                     }
                                } else {
                                    if (isNewDiary) {
                                        updateType = UPDATE_TYPE_UPDATE_ONLY;
                                    } else {
                                        updateType = UPDATE_TYPE_DELETE_AND_UPDATE;
                                    }
                                    startUpdateExistingDiaryDialogFragment(
                                            stringSavingDate,
                                            updateType);
                                }
                            } else {
                                boolean isSuccessful = false;
                                if (isNewDiary) {
                                    Log.d("保存形式確認", "新規保存");
                                    isSuccessful = diaryViewModel.saveDiary();
                                } else {
                                    Log.d("保存形式確認", "日付変更新規保存");
                                    isSuccessful =
                                            diaryViewModel.deleteExistingDiaryAndSaveDiary();
                                }
                                if (isSuccessful) {
                                    changeToShowDiaryFragment();
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                });


        // 項目入力欄関係Viewを配列に格納
        TextView[] textItems = new TextView[MAX_ITEMS_COUNT];
        EditText[] editTextItemsTitle = new EditText[MAX_ITEMS_COUNT];
        NestedScrollView[] nestedScrollItemsComment = new NestedScrollView[MAX_ITEMS_COUNT];
        EditText[] editTextItemsComment = new EditText[MAX_ITEMS_COUNT];
        TextView[] textItemsCommentLength = new TextView[MAX_ITEMS_COUNT];
        ImageButton[] imageButtonItemsDelete = new ImageButton[MAX_ITEMS_COUNT];

        textItems[0] = this.binding.includeItem1.textItemNumber;
        editTextItemsTitle[0] = this.binding.includeItem1.editTextItemTitle;
        nestedScrollItemsComment[0] = this.binding.includeItem1.nestedScrollItemComment;
        editTextItemsComment[0] = this.binding.includeItem1.editTextItemComment;
        textItemsCommentLength[0] = this.binding.includeItem1.textItemCommentLength;
        imageButtonItemsDelete[0] = this.binding.includeItem1.imageButtonItemDelete;

        textItems[1] = this.binding.includeItem2.textItemNumber;
        editTextItemsTitle[1] = this.binding.includeItem2.editTextItemTitle;
        nestedScrollItemsComment[1] = this.binding.includeItem2.nestedScrollItemComment;
        editTextItemsComment[1] = this.binding.includeItem2.editTextItemComment;
        textItemsCommentLength[1] = this.binding.includeItem2.textItemCommentLength;
        imageButtonItemsDelete[1] = this.binding.includeItem2.imageButtonItemDelete;

        textItems[2] = this.binding.includeItem3.textItemNumber;
        editTextItemsTitle[2] = this.binding.includeItem3.editTextItemTitle;
        nestedScrollItemsComment[2] = this.binding.includeItem3.nestedScrollItemComment;
        editTextItemsComment[2] = this.binding.includeItem3.editTextItemComment;
        textItemsCommentLength[2] = this.binding.includeItem3.textItemCommentLength;
        imageButtonItemsDelete[2] = this.binding.includeItem3.imageButtonItemDelete;

        textItems[3] = this.binding.includeItem4.textItemNumber;
        editTextItemsTitle[3] = this.binding.includeItem4.editTextItemTitle;
        nestedScrollItemsComment[3] = this.binding.includeItem4.nestedScrollItemComment;
        editTextItemsComment[3] = this.binding.includeItem4.editTextItemComment;
        textItemsCommentLength[3] = this.binding.includeItem4.textItemCommentLength;
        imageButtonItemsDelete[3] = this.binding.includeItem4.imageButtonItemDelete;

        textItems[4] = this.binding.includeItem5.textItemNumber;
        editTextItemsTitle[4] = this.binding.includeItem5.editTextItemTitle;
        nestedScrollItemsComment[4] = this.binding.includeItem5.nestedScrollItemComment;
        editTextItemsComment[4] = this.binding.includeItem5.editTextItemComment;
        textItemsCommentLength[4] = this.binding.includeItem5.textItemCommentLength;
        imageButtonItemsDelete[4] = this.binding.includeItem5.imageButtonItemDelete;

        // キーボード入力不要View
        List<View> noKeyboardViews = new ArrayList<>();
        noKeyboardViews.add(this.binding.editTextDate);
        noKeyboardViews.add(this.binding.spinnerWeather1);
        noKeyboardViews.add(this.binding.spinnerWeather2);
        noKeyboardViews.add(this.binding.spinnerCondition);
        for (int i = 0; i < MAX_ITEMS_COUNT; i++) {
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
                Log.d("20240719", "TextDate:" + s);
                Log.d("20240719", "lastTextDate:" + lastTextDate);
                if (!s.isEmpty()) {
                    boolean isShowing = showLoadingExistingDiaryDialogFragmentOnDateChanged(s);
                    Boolean isChecked = settingsViewModel.getIsCheckedGettingWeatherInformationLiveData().getValue();
                    // 読込確認Dialog表示時は、確認後下記処理を行う。
                    if (!isShowing && isChecked != null && isChecked) {
                        String loadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
                        if (loadedDate.isEmpty() && !s.equals(lastTextDate)) {
                            prepareWeatherSelection(s);
                        }
                    }
                    // HACK:日記読込時、読込前に一度リセットする為、空の文字列が代入される。
                    //      これにより不具合が生じる為、空でない時のみ記憶する。
                    lastTextDate = s;
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
        List<String> weatherItemList = new ArrayList<>();
        for (Weathers weather: Weathers.values()) {
            weatherItemList.add(weather.toString(requireContext()));
        }
        ArrayAdapter<String> weatherArrayAdapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1 , weatherItemList);
        binding.spinnerWeather1.setAdapter(weatherArrayAdapter);
        binding.spinnerWeather2.setAdapter(weatherArrayAdapter);
        binding.spinnerWeather1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    diaryViewModel.cancelWeatherSelectionPreparation();
                }
                return false;
            }
        });
        binding.spinnerWeather1
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        diaryViewModel.setIntWeather1(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        diaryViewModel.getLiveIntWeather1()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer != binding.spinnerWeather1.getSelectedItemPosition()) {
                            binding.spinnerWeather1.setSelection(integer);
                        }

                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(integer);
                        String strWeather = diaryViewModel.getLiveStrWeather1().getValue();
                        if (strWeather == null || !weather.toString(requireContext()).equals(strWeather)) {
                            diaryViewModel.setStrWeather1(weather.toString(requireContext()));
                        }

                        if (integer != 0) {
                            binding.spinnerWeather2.setEnabled(true);
                        } else {
                            binding.spinnerWeather2.setEnabled(false);
                            diaryViewModel.setIntWeather2(0);
                            binding.spinnerWeather2.setSelection(0);
                        }
                    }
                });
        diaryViewModel.getLiveStrWeather1()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(requireContext(), s);
                        Integer intWeather = diaryViewModel.getLiveIntWeather1().getValue();
                        if (intWeather == null || weather.toWeatherNumber() != intWeather) {
                            diaryViewModel.setIntWeather1(weather.toWeatherNumber());
                        }
                    }
                });
        binding.spinnerWeather2
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        diaryViewModel.setIntWeather2(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        diaryViewModel.getLiveIntWeather2()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer != binding.spinnerWeather2.getSelectedItemPosition()) {
                            binding.spinnerWeather2.setSelection(integer);
                        }

                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(integer);
                        String strWeather = diaryViewModel.getLiveStrWeather2().getValue();
                        if (strWeather == null || !weather.toString(requireContext()).equals(strWeather)) {
                            diaryViewModel.setStrWeather2(weather.toString(requireContext()));
                        }
                    }
                });
        diaryViewModel.getLiveStrWeather2()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(requireContext(), s);
                        Integer intWeather = diaryViewModel.getLiveIntWeather2().getValue();
                        if (intWeather == null || weather.toWeatherNumber() != intWeather) {
                            diaryViewModel.setIntWeather2(weather.toWeatherNumber());
                        }
                    }
                });
        settingsViewModel.getHasUpdatedLocationLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean && shouldPrepareWeatherSelection) {
                            String date = diaryViewModel.getLiveDate().getValue();
                            prepareWeatherSelection(date);
                        }
                    }
                });


        // 気分入力欄。
        // その他 ViewModel にて処理

        List<String> conditonItemList = new ArrayList<>();
        for (Conditions weather: Conditions.values()) {
            conditonItemList.add(weather.toString(requireContext()));
        }
        ArrayAdapter<String> conditionArrayAdapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1 , conditonItemList);
        binding.spinnerCondition.setAdapter(conditionArrayAdapter);
        binding.spinnerCondition
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        diaryViewModel.setIntCondition(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        diaryViewModel.getLiveIntCondition()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {

                        ConditionConverter converter = new ConditionConverter();
                        Conditions condition = converter.toCondition(integer);
                        String strCondition = diaryViewModel.getLiveStrCondition().getValue();
                        if (strCondition == null) {
                            return;
                        }
                        if (!condition.toString(requireContext()).equals(strCondition)) {
                            diaryViewModel.setStrCondition(condition.toString(requireContext()));
                        }
                    }
                });
        diaryViewModel.getLiveStrWeather2()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        ConditionConverter converter = new ConditionConverter();
                        Conditions condition = converter.toCondition(requireContext(), s);
                        Integer intCondition = diaryViewModel.getLiveIntCondition().getValue();
                        if (intCondition == null) {
                            return;
                        }
                        if (condition.toConditionNumber() != intCondition) {
                            diaryViewModel.setIntCondition(condition.toConditionNumber());
                        }
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


        // 項目欄設定
        // 項目欄MotionLayout設定
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
            int itemNumber = i + 1;
            MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
            itemMotionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
                    // 処理なし
                }

                @Override
                public void onTransitionChange(
                        MotionLayout motionLayout, int startId, int endId, float progress) {
                    // 処理なし
                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                    Log.d("20240605", "Item" + String.valueOf(itemNumber) + " onTransitionCompleted");
                    // 対象項目欄を閉じた後の処理
                    if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                        Log.d("20240605", "currentId:Start");
                        if (EditDiaryFragment.this.isDeletingItemTransition) {
                            EditDiaryFragment.this.diaryViewModel.deleteItem(itemNumber);
                            EditDiaryFragment.this.isDeletingItemTransition = false;
                            setupItemLayout();
                        }
                        EditDiaryFragment.this.binding
                                .imageButtonAddItem.setVisibility(View.VISIBLE);
                    } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                        Log.d("20240605", "currentId:End");
                        EditDiaryFragment.this.binding.imageButtonAddItem.setEnabled(true);
                        if (EditDiaryFragment.this.diaryViewModel.getVisibleItemsCount()
                                == EditDiaryFragment.this.MAX_ITEMS_COUNT) {
                            EditDiaryFragment.this.binding
                                    .imageButtonAddItem.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
                    // 処理なし
                }
            });

        }
        setupItemLayout(); //必要数の項目欄表示

        // 項目タイトル入力欄設定
        for (int i = 0; i < editTextItemsTitle.length; i++) {
            int inputItemNo =  i + 1;
            editTextItemsTitle[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 項目タイトル入力フラグメント起動
                    String inputItemTitle =
                            EditDiaryFragment.this.diaryViewModel
                                    .getItem(inputItemNo).getLiveTitle().getValue();
                    NavDirections action =
                            EditDiaryFragmentDirections
                                    .actionEditDiaryFragmentToSelectItemTitleFragment(
                                            inputItemNo, inputItemTitle);
                    EditDiaryFragment.this.navController.navigate(action);
                }
            });
        }

        // 項目コメント入力欄設定。
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
            setupEditText(
                    editTextItemsComment[i],
                    textItemsCommentLength[i],
                    50,
                    inputMethodManager,
                    binding.nestedScrollFullScreen, // 背景View,
                    noKeyboardViews
            );
        }


        // 項目削除ボタン設定
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
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


        // 項目追加ボタン設定
        this.binding.imageButtonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDiaryFragment.this.binding.imageButtonAddItem.setEnabled(false);
                int visibleItemsCount = EditDiaryFragment.this.diaryViewModel.getVisibleItemsCount();
                int addItemNumber = visibleItemsCount + 1;
                showItem(addItemNumber, false);
                EditDiaryFragment.this.diaryViewModel.incrementVisibleItemsCount();
            }
        });

        // エラー表示
        diaryViewModel.getIsDiarySavingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (canShowDialog()) {
                        showDiarySavingErrorDialog();
                    } else {
                        shouldShowDiarySavingErrorDialog = true;
                    }
                    diaryViewModel.setIsDiarySavingErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (canShowDialog()) {
                        showDiaryLoadingErrorDialog();
                    } else {
                        shouldShowDiaryLoadingErrorDialog = true;
                    }
                    diaryViewModel.setIsDiaryLoadingErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (canShowDialog()) {
                        showDiaryDeleteErrorDialog();
                    } else {
                        shouldShowDiaryDeleteErrorDialog = true;
                    }
                    diaryViewModel.setIsDiaryDeleteErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsWeatherLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d("20240719", "onChangedError:" + String.valueOf(aBoolean));
                if (aBoolean) {
                    if (canShowDialog()) {
                        showWeatherLoadingErrorDialog();
                    } else {
                        shouldShowWeatherLoadingErrorDialog = true;
                    }
                    diaryViewModel.setIsWeatherLoadingErrorLiveData(false);
                }
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


    public void startUpdateExistingDiaryDialogFragment(String savingDate, int updateType) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToUpdateExistingDiaryDialog(savingDate, updateType);
        this.navController.navigate(action);
    }

    public void changeToShowDiaryFragment() {
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        NavDirections action;
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるactionを切り替える。
        String showDiaryDate = this.diaryViewModel.getLiveDate().getValue();
        LocalDate showDiaryLocalDate = DateConverter.toLocalDate(showDiaryDate);
        if (isStartDiaryFragment) {
            action = EditDiaryFragmentDirections
                    .actionEditDiaryFragmentToShowDiaryFragmentPattern2(
                            showDiaryLocalDate.getYear(),
                            showDiaryLocalDate.getMonthValue(),
                            showDiaryLocalDate.getDayOfMonth()
                    );
        } else {
            action = EditDiaryFragmentDirections
                    .actionEditDiaryFragmentToShowDiaryFragmentPattern1(
                            showDiaryLocalDate.getYear(),
                            showDiaryLocalDate.getMonthValue(),
                            showDiaryLocalDate.getDayOfMonth()
                    );
        }
        this.navController.navigate(action);
    }

    private boolean showLoadingExistingDiaryDialogFragmentOnDateChanged(String changedDate) {
        LocalDate localDate = DateConverter.toLocalDate(changedDate);
        if (changedDate.equals(lastTextDate)) {
            return false;
        }
        String loadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
        if (changedDate.equals(loadedDate)) {
            return false;
        }
        if (diaryViewModel.hasDiary(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth())) {
            NavDirections action =
                    EditDiaryFragmentDirections
                            .actionEditDiaryFragmentToLoadExistingDiaryDialog(changedDate);
            navController.navigate(action);
            return true;
        }
        return false;
    }

    private void prepareWeatherSelection(String date) {
        LocalDate localDate = DateConverter.toLocalDate(date);
        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        Boolean hasUpdatedLocation = settingsViewModel.getHasUpdatedLocationLiveData().getValue();
        if (hasUpdatedLocation != null && hasUpdatedLocation) {
            Log.d("20240719", "onTextChanged:prepareWeatherSelection");
            if (lastTextDate.isEmpty()) {
                diaryViewModel.prepareWeatherSelection(
                        localDate.getYear(),
                        localDate.getMonthValue(),
                        localDate.getDayOfMonth(),
                        settingsViewModel.getLatitude(),
                        settingsViewModel.getLongitude()
                );
            } else {
                showWeatherInformationDialog(date);
            }
        } else {
            shouldPrepareWeatherSelection = true;
        }
    }

    private MotionLayout selectItemMotionLayout(int itemNumber) {
        MotionLayout itemMotionLayout = null;
        switch (itemNumber) {
            case 1:
                itemMotionLayout = this.binding.includeItem1.motionLayoutEditDiaryItem;
                break;
            case 2:
                itemMotionLayout = this.binding.includeItem2.motionLayoutEditDiaryItem;
                break;
            case 3:
                itemMotionLayout = this.binding.includeItem3.motionLayoutEditDiaryItem;
                break;
            case 4:
                itemMotionLayout = this.binding.includeItem4.motionLayoutEditDiaryItem;
                break;
            case 5:
                itemMotionLayout = this.binding.includeItem5.motionLayoutEditDiaryItem;
                break;
        }
        return itemMotionLayout;
    }

    private void setupItemLayout() {
        int visibleItemsCount = this.diaryViewModel.getVisibleItemsCount();
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
            int itemNumber = i + 1;
            if (itemNumber <= visibleItemsCount) {
                showItem(itemNumber, true);
            } else {
                hideItem(itemNumber, true);
            }
        }
    }


    private void hideItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (itemMotionLayout != null) {
            if (isJump) {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_edit_diary_item_hided_state, 1);
            } else {
                itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_hided_state);
            }
        }
    }

    private void showItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (itemMotionLayout != null) {
            if (isJump) {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_edit_diary_item_showed_state, 1);
            } else {
                itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_showed_state);
            }
        }
    }

    private void showWeatherInformationDialog(String date) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToWeatherInformationDialog(date);
        EditDiaryFragment.this.navController.navigate(action);
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToMessageDialog(
                                title, message);
        EditDiaryFragment.this.navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_edit_diary_fragment;
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiarySavingErrorDialog) {
            showDiarySavingErrorDialog();
            shouldShowDiarySavingErrorDialog = false;
            return;
        }
        if (shouldShowDiaryLoadingErrorDialog) {
            showDiaryLoadingErrorDialog();
            shouldShowDiaryLoadingErrorDialog = false;
            return;
        }
        if (shouldShowDiaryDeleteErrorDialog) {
            showDiaryDeleteErrorDialog();
            shouldShowDiaryDeleteErrorDialog = false;
            return;
        }
        if (shouldShowWeatherLoadingErrorDialog) {
            showWeatherLoadingErrorDialog();
            shouldShowWeatherLoadingErrorDialog = false;
            return;
        }
    }

    private void showDiarySavingErrorDialog() {
        showMessageDialog("通信エラー", "日記の保存に失敗しました。");
    }

    private void showDiaryLoadingErrorDialog() {
        showMessageDialog("通信エラー", "日記の読込に失敗しました。");
    }

    private void showDiaryDeleteErrorDialog() {
        showMessageDialog("通信エラー", "日記の削除に失敗しました。");
    }

    private void showWeatherLoadingErrorDialog() {
        showMessageDialog("通信エラー", "天気情報の読込に失敗しました。");
    }

    private void showSettingLoadingErrorDialog() {
        showMessageDialog("通信エラー", "設定情報の読込に失敗しました。");
    }
}
