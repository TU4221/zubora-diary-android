package com.websarva.wings.android.zuboradiary.ui.diary.editdiary;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
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
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
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
    private LocalDate lastSelectedDate;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiarySavingErrorDialog;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;
    private boolean shouldShowWeatherLoadingErrorDialog;


    // ViewModel
    private DiaryViewModel diaryViewModel;
    private SettingsViewModel settingsViewModel;

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
        // DiaryViewModel初期化
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
        binding = FragmentEditDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setDiaryViewModel(diaryViewModel);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpSelectDiaryItemTitleFragmentResultReceiver();
        setUpDialogResultReceiver();
        setUpDiaryData();
        setUpObserver();
        setUpToolBar();
        setUpDateInputField();
        setUpWeatherInputField();
        setUpConditionInputField();
        setUpTitleInputField();
        setUpItemInputField();
        setUpPictureInputField();
        setUpErrorObserver();


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















    }

    // 項目タイトル入力フラグメントからデータ受取
    private void setUpSelectDiaryItemTitleFragmentResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry != null) {
            SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
            MutableLiveData<String> newItemTitle =
                    savedStateHandle.getLiveData(EditDiarySelectItemTitleFragment.KEY_NEW_ITEM_TITLE);
            newItemTitle.observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String string) {
                    Integer itemNumber =
                            savedStateHandle.get(EditDiarySelectItemTitleFragment.KEY_UPDATE_ITEM_NUMBER);
                    if (itemNumber != null) {
                        diaryViewModel.updateItemTitle(itemNumber, string);
                    }
                    savedStateHandle.remove(EditDiarySelectItemTitleFragment.KEY_UPDATE_ITEM_NUMBER);
                    savedStateHandle.remove(EditDiarySelectItemTitleFragment.KEY_NEW_ITEM_TITLE);
                }
            });
        }
    }

    // ダイアログフラグメントからの結果受取設定
    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry == null) {
            return;
        }
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
                //      Dialog非表示中:Lifecycle.Event.ON_RESUME
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    receiveDatePickerDialogResult(savedStateHandle);
                    receiveLoadExistingDiaryDialogResult(savedStateHandle);
                    receiveUpdateExistingDiaryDialogResult(savedStateHandle);
                    receiveDeleteConfirmDialogResult(savedStateHandle);
                    receiveWeatherInformationDialogResult(savedStateHandle);
                    if (canShowDialog()) {
                        retryErrorDialogShow();
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
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
                    savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
                    savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
                    savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private void receiveDatePickerDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_DATE);
        if (containsDialogResult) {
            LocalDate selectedDate =
                    savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_DATE);
            diaryViewModel.updateDate(selectedDate);
        }
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private void receiveLoadExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            String stringDate = diaryViewModel.getLiveDate().getValue();
            LocalDate date = DateConverter.toLocalDate(stringDate);
            Integer selectedButton =
                    savedStateHandle.get(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton != null) {
                if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                    diaryViewModel.initialize();
                    diaryViewModel.prepareDiary(date, true);
                } else {
                    String loadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
                    Boolean isChecked =
                            settingsViewModel.getIsCheckedGettingWeatherInformationLiveData().getValue();
                    if ((loadedDate == null || loadedDate.isEmpty()) && isChecked != null && isChecked) {
                        prepareWeatherSelection(date);
                    }
                }
            }
        }
        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveUpdateExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON)
                        && savedStateHandle.contains(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
        if (containsDialogResult) {
            Integer updateType =
                    savedStateHandle.get(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
            if (updateType != null) {
                boolean isSuccessful;
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
                    showShowDiaryFragment();
                }
            }
        }
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private void receiveDeleteConfirmDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        if (containsDialogResult) {
            Integer deleteItemNumber = savedStateHandle
                    .get(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);

            if (deleteItemNumber != null) {
                if (deleteItemNumber == 1
                        && diaryViewModel.getVisibleItemsCount() == deleteItemNumber) {
                    diaryViewModel.deleteItem(deleteItemNumber);
                } else {
                    isDeletingItemTransition = true;
                    hideItem(deleteItemNumber, false);
                }
            }
        }
        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
    }

    private void receiveWeatherInformationDialogResult(SavedStateHandle savedStateHandle) {
        // 天気情報読込ダイアログフラグメントから結果受取
        boolean containsDialogResult =
                savedStateHandle.contains(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            String date = diaryViewModel.getLiveDate().getValue();
            Integer selectedButton =
                    savedStateHandle.get(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton != null) {
                if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                    LocalDate loadDiaryDate = DateConverter.toLocalDate(date);
                    diaryViewModel.prepareWeatherSelection(
                            loadDiaryDate,
                            settingsViewModel.getLatitude(),
                            settingsViewModel.getLongitude()
                    );
                }
            }
            savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
        }
    }

    private void setUpDiaryData() {
        // 画面表示データ準備
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        boolean isLoadingExistingDiary =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsLoadingDiary();
        LocalDate editDiaryDate =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getEditDiaryDate();
        if (isStartDiaryFragment && !diaryViewModel.getHasPreparedDiary()) {
            this.diaryViewModel.prepareDiary(editDiaryDate, isLoadingExistingDiary);
        }

    }

    private void setUpObserver() {
        diaryViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s != null) {
                            if (s.isEmpty()) {
                                binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
                            } else {
                                binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
                            }
                        }
                    }
                });
    }

    private void setUpToolBar() {
        // TODO:下記コメントアウトコードはobserverで管理(確認後削除)
        /*boolean isNewDiary = diaryViewModel.getLoadedDateLiveData().getValue().isEmpty();
        if (isNewDiary) {
            this.binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
        } else {
            this.binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
        }*/
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navController.navigateUp();
                    }
                });
        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //日記保存(日記表示フラグメント起動)。
                        if (item.getItemId() == R.id.editDiaryToolbarOptionSaveDiary) {
                            String stringLoadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
                            String stringSavingDate = diaryViewModel.getLiveDate().getValue();
                            boolean isNewDiary = true;
                            boolean isMatchedDate = false;
                            if (stringLoadedDate != null) {
                                isNewDiary = stringLoadedDate.isEmpty();
                                isMatchedDate = stringLoadedDate.equals(stringSavingDate);
                            }

                            LocalDate savingDate = DateConverter.toLocalDate(stringSavingDate);
                            boolean isUpdateDiary = diaryViewModel.hasDiary(savingDate);

                            if (isUpdateDiary) {
                                int updateType;
                                if (isMatchedDate) {
                                    Log.d("保存形式確認", "上書保存");
                                    boolean isSuccessful = diaryViewModel.saveDiary();
                                    if (isSuccessful) {
                                        showShowDiaryFragment();
                                    }
                                } else {
                                    if (isNewDiary) {
                                        updateType = UPDATE_TYPE_UPDATE_ONLY;
                                    } else {
                                        updateType = UPDATE_TYPE_DELETE_AND_UPDATE;
                                    }
                                    showUpdateExistingDiaryDialog(savingDate, updateType);
                                }
                            } else {
                                boolean isSuccessful;
                                if (isNewDiary) {
                                    Log.d("保存形式確認", "新規保存");
                                    isSuccessful = diaryViewModel.saveDiary();
                                } else {
                                    Log.d("保存形式確認", "日付変更新規保存");
                                    isSuccessful = diaryViewModel.deleteExistingDiaryAndSaveDiary();
                                }
                                if (isSuccessful) {
                                    showShowDiaryFragment();
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                });
    }

    // 日付入力欄設定
    private void setUpDateInputField() {
        binding.editTextDate.setFocusable(true);
        binding.editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringDate = diaryViewModel.getLiveDate().getValue();
                LocalDate date = DateConverter.toLocalDate(stringDate);
                showDatePickerDialog(date);
            }
        });

        this.diaryViewModel.getLiveDate().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == null) {
                    return;
                }
                Log.d("EditDiaryInputDate", "SelectedDate:" + s);
                Log.d("EditDiaryInputDate", "lastSelectedDate:" + lastSelectedDate);
                LocalDate date = DateConverter.toLocalDate(s);
                if (!s.isEmpty()) {
                    boolean shouldShowDialog = shouldShowLoadingExistingDiaryDialogOnDateChanged(date);
                    if (shouldShowDialog) {
                        showLoadingExistingDiaryDialog(date);
                    } else {
                        Boolean isChecked =
                                settingsViewModel.getIsCheckedGettingWeatherInformationLiveData().getValue();
                        // 読込確認Dialog表示時は、確認後下記処理を行う。
                        if ( isChecked != null && isChecked) {
                            String loadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
                            if ((loadedDate == null || loadedDate.isEmpty()) && !date.equals(lastSelectedDate)) {
                                prepareWeatherSelection(date);
                            }
                        }
                    }
                    // HACK:日記読込時、読込前に一度リセットする為、空の文字列が代入される。
                    //      これにより不具合が生じる為、空でない時のみ記憶する。
                    lastSelectedDate = date;
                }
                /*lastTextDate = s;*/
            }
        });
    }

    private boolean shouldShowLoadingExistingDiaryDialogOnDateChanged(LocalDate changedDate) {
        if (changedDate == null) {
            return false;
        }
        if (changedDate.equals(lastSelectedDate)) {
            return false;
        }
        String stringLoadedDate = diaryViewModel.getLoadedDateLiveData().getValue();
        LocalDate loadedDate = DateConverter.toLocalDate(stringLoadedDate);
        if (changedDate.equals(loadedDate)) {
            return false;
        }
        return diaryViewModel.hasDiary(changedDate);
    }

    // 天気入力欄。
    private void setUpWeatherInputField() {
        // TODO:下記MEMOの意味が理解できないので後で確認ご文章を修正する
        // MEMO:下記 onItemSelected は DataBinding を使用して ViewModel 内にメソッドを用意していたが、
        //      画面作成処理時に onItemSelected が処理される為、初期値設定する為の setSelection メソッドの処理タイミングの兼合いで、
        //      DataBinding での使用を取りやめ、ここにまとめて記載することにした。
        //      他スピナーも同様。
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter();
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
                        if (integer == null) {
                            return;
                        }
                        if (integer != binding.spinnerWeather1.getSelectedItemPosition()) {
                            binding.spinnerWeather1.setSelection(integer);
                        }

                        // StringWeather1LiveDataへ反映
                        WeatherConverter converter = new WeatherConverter();
                        Weathers weather = converter.toWeather(integer);
                        String strWeather = diaryViewModel.getLiveStrWeather1().getValue();
                        if (strWeather == null || !weather.toString(requireContext()).equals(strWeather)) {
                            diaryViewModel.setStrWeather1(weather.toString(requireContext()));
                        }

                        // Weather2 Spinner有効無効切替
                        if (integer != 0) {
                            // TODO:下記アダプター作成コード確認
                            ArrayAdapter<String> customuWeatherArrayAdapter =
                                    createWeatherSpinnerAdapter(Weathers.values()[integer]);
                            binding.spinnerWeather2.setAdapter(customuWeatherArrayAdapter);
                            binding.spinnerWeather2.setEnabled(true);
                        } else {
                            binding.spinnerWeather2.setEnabled(false);
                            diaryViewModel.setIntWeather2(0);
                            binding.spinnerWeather2.setAdapter(weatherArrayAdapter);
                            binding.spinnerWeather2.setSelection(0);
                        }
                    }
                });
        diaryViewModel.getLiveStrWeather1()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s == null) {
                            return;
                        }
                        // IntegerWeather1LiveDataへ反映
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
                        if (integer == null) {
                            return;
                        }
                        if (integer != binding.spinnerWeather2.getSelectedItemPosition()) {
                            binding.spinnerWeather2.setSelection(integer);
                        }

                        // StringWeather2LiveDataへ反映
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
                        if (s == null) {
                            return;
                        }
                        // IntegerWeather2LiveDataへ反映
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
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean && shouldPrepareWeatherSelection) {
                            String stringDate = diaryViewModel.getLiveDate().getValue();
                            LocalDate date = DateConverter.toLocalDate(stringDate);
                            prepareWeatherSelection(date);
                        }
                    }
                });
    }

    private ArrayAdapter<String> createWeatherSpinnerAdapter(@Nullable Weathers... excludedWeathers) {
        List<String> weatherItemList = new ArrayList<>();
        for (Weathers weather: Weathers.values()) {
            if (!isExcludedWeather(weather, excludedWeathers)) {
                weatherItemList.add(weather.toString(requireContext()));
            }
        }
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1 , weatherItemList);
    }

    private boolean isExcludedWeather(Weathers weather, @Nullable Weathers... excludedWeathers) {
        if (excludedWeathers == null) {
            return false;
        }
        for(Weathers excludedWeather: excludedWeathers) {
            if (weather == excludedWeather) {
                return true;
            }
        }
        return false;
    }

    // 気分入力欄。
    private void setUpConditionInputField() {
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
                        if (integer == null) {
                            return;
                        }
                        // StringConditionLiveDataへ反映
                        ConditionConverter converter = new ConditionConverter();
                        Conditions condition = converter.toCondition(integer);
                        String strCondition = diaryViewModel.getLiveStrCondition().getValue();
                        if (strCondition == null || !condition.toString(requireContext()).equals(strCondition)) {
                            diaryViewModel.setStrCondition(condition.toString(requireContext()));
                        }
                    }
                });
        diaryViewModel.getLiveStrWeather2()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s == null) {
                            return;
                        }
                        // IntegerConditionLiveDataへ反映
                        ConditionConverter converter = new ConditionConverter();
                        Conditions condition = converter.toCondition(requireContext(), s);
                        Integer intCondition = diaryViewModel.getLiveIntCondition().getValue();
                        if (intCondition == null || condition.toConditionNumber() != intCondition) {
                            diaryViewModel.setIntCondition(condition.toConditionNumber());
                        }
                    }
                });
    }

    private void setUpTitleInputField() {
// タイトル入力欄設定
        setupEditText(
                this.binding.editTextTitle,
                this.binding.textTitleLength,
                15,
                this.binding.nestedScrollFullScreen, // 背景View
                noKeyboardViews
        );
    }

    private void setUpItemInputField() {
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
    }

    private void setUpPictureInputField() {
// エラー表示
        diaryViewModel.getIsDiarySavingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiarySavingErrorDialog();
                    diaryViewModel.setIsDiarySavingErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryLoadingErrorDialog();
                    diaryViewModel.setIsDiaryLoadingErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showDiaryDeleteErrorDialog();
                    diaryViewModel.setIsDiaryDeleteErrorLiveData(false);
                }
            }
        });
        diaryViewModel.getIsWeatherLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showWeatherLoadingErrorDialog();
                    diaryViewModel.setIsWeatherLoadingErrorLiveData(false);
                }
            }
        });
    }

    private void setUpErrorObserver() {

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

    private void prepareWeatherSelection(LocalDate date) {
        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        Boolean hasUpdatedLocation = settingsViewModel.getHasUpdatedLocationLiveData().getValue();
        if (hasUpdatedLocation != null && hasUpdatedLocation) {
            Log.d("20240719", "onTextChanged:prepareWeatherSelection");
            if (lastSelectedDate.isEmpty()) {
                diaryViewModel.prepareWeatherSelection(
                        date,
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

    public void showShowDiaryFragment() {
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

    private void showDatePickerDialog(LocalDate date) {
        NavDirections action =
                EditDiaryFragmentDirections.actionEditDiaryFragmentToDatePickerDialog(date);
        navController.navigate(action);
    }

    public void showUpdateExistingDiaryDialog(LocalDate date, int updateType) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToUpdateExistingDiaryDialog(date, updateType);
        this.navController.navigate(action);
    }



    private void showLoadingExistingDiaryDialog(LocalDate date) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToLoadExistingDiaryDialog(date);
        navController.navigate(action);
    }

    private void showWeatherInformationDialog(LocalDate date) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToWeatherInformationDialog(date);
        navController.navigate(action);
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
            return;
        }
        if (shouldShowDiaryLoadingErrorDialog) {
            showDiaryLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryDeleteErrorDialog) {
            showDiaryDeleteErrorDialog();
            return;
        }
        if (shouldShowWeatherLoadingErrorDialog) {
            showWeatherLoadingErrorDialog();
            return;
        }
    }

    private void showDiarySavingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記の保存に失敗しました。");
            shouldShowDiarySavingErrorDialog = false;
        } else {
            shouldShowDiarySavingErrorDialog = true;
        }
    }

    private void showDiaryLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記の読込に失敗しました。");
            shouldShowDiaryLoadingErrorDialog = false;
        } else {
            shouldShowDiaryLoadingErrorDialog = true;
        }
    }

    private void showDiaryDeleteErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記の削除に失敗しました。");
            shouldShowDiaryDeleteErrorDialog = false;
        } else {
            shouldShowDiaryDeleteErrorDialog = true;
        }
    }

    private void showWeatherLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "天気情報の読込に失敗しました。");
            shouldShowWeatherLoadingErrorDialog = false;
        } else {
            shouldShowWeatherLoadingErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToMessageDialog(
                                title, message);
        EditDiaryFragment.this.navController.navigate(action);
    }
}
