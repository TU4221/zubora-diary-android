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
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditDiaryFragment extends Fragment {

    // View関係
    private FragmentEditDiaryBinding binding;
    private boolean isDeletingItemTransition = false;
    private final String TOOL_BAR_TITLE_NEW = "新規作成";
    private final String TOOL_BAR_TITLE_EDIT = "編集中";
    private LocalDate lastSelectedDate;
    private final List<View> noKeyboardViews = new ArrayList<>();
    ArrayAdapter<String> weather2ArrayAdapter;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiarySavingErrorDialog;
    private boolean shouldShowDiaryLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;
    private boolean shouldShowWeatherLoadingErrorDialog;


    // ViewModel
    private EditDiaryViewModel editDiaryViewModel;
    private SettingsViewModel settingsViewModel;

    // 位置情報
    private boolean shouldPrepareWeatherSelection = false;

    // 上書保存方法
    public static final int UPDATE_TYPE_UPDATE_ONLY = 0;
    public static final int UPDATE_TYPE_DELETE_AND_UPDATE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        editDiaryViewModel = provider.get(EditDiaryViewModel.class);
        settingsViewModel = provider.get(SettingsViewModel.class);
        // DiaryViewModel初期化
        // TODO:下記検討する
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        if (isStartDiaryFragment) {
            editDiaryViewModel.initialize();
        }

        // Navigation設定
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // データバインディング設定
        binding = FragmentEditDiaryBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setEditDiaryViewModel(editDiaryViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpSelectDiaryItemTitleFragmentResultReceiver();
        setUpDialogResultReceiver();
        setUpDiaryData();
        setUpToolBar();
        setUpNoKeyBoardViews();
        setUpDateInputField();
        setUpWeatherInputField();
        setUpConditionInputField();
        setUpTitleInputField();
        setUpItemInputField();
        setUpPictureInputField();
        setUpErrorObserver();
    }

    // 項目タイトル入力フラグメントからデータ受取
    private void setUpSelectDiaryItemTitleFragmentResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry != null) {
            SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
            MutableLiveData<String> newItemTitle =
                    savedStateHandle.getLiveData(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
            newItemTitle.observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String string) {
                    Integer itemNumber =
                            savedStateHandle.get(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                    if (itemNumber != null) {
                        editDiaryViewModel.updateItemTitle(itemNumber, string);
                    }
                    savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                    savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
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
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private void receiveDatePickerDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_DATE);
        if (containsDialogResult) {
            LocalDate selectedDate =
                    savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_DATE);
            if (selectedDate == null) {
                return;
            }
            editDiaryViewModel.updateDate(selectedDate);
        }
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private void receiveLoadExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            LocalDate date = editDiaryViewModel.getDateLiveData().getValue();
            if (date == null) {
                return;
            }
            Integer selectedButton =
                    savedStateHandle.get(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton == null) {
                return;
            }
            if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                editDiaryViewModel.initialize();
                editDiaryViewModel.prepareDiary(date, true);
            } else {
                LocalDate loadedDate = editDiaryViewModel.getLoadedDateLiveData().getValue();
                if (loadedDate == null) {
                    return;
                }
                fetchWeatherInformation(date);
            }
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveUpdateExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON)
                        && savedStateHandle.contains(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
        if (containsDialogResult) {
            Integer updateType =
                    savedStateHandle.get(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
            if (updateType == null) {
                return;
            }
            boolean isSuccessful;
            switch (updateType) {
                case UPDATE_TYPE_DELETE_AND_UPDATE:
                    Log.d("保存形式確認", "日付変更上書保存");
                    isSuccessful = editDiaryViewModel.deleteExistingDiaryAndSaveDiary();
                    break;
                case UPDATE_TYPE_UPDATE_ONLY:
                default:
                    Log.d("保存形式確認", "上書保存");
                    isSuccessful = editDiaryViewModel.saveDiary();
                    break;
            }
            if (isSuccessful) {
                LocalDate date = editDiaryViewModel.getDateLiveData().getValue();
                if (date == null) {
                    return;
                }
                showShowDiaryFragment(date);
            }
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private void receiveDeleteConfirmDialogResult(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        if (containsDialogResult) {
            Integer deleteItemNumber = savedStateHandle
                    .get(DeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);

            // TODO:Observerへ移行
            Integer numVisibleItems = editDiaryViewModel.getNumVisibleItemsLiveData().getValue();
            if (deleteItemNumber == null) {
                return;
            }
            if (numVisibleItems == null) {
                return;
            }
            if (deleteItemNumber == 1 && numVisibleItems.equals(deleteItemNumber)) {
                editDiaryViewModel.deleteItem(deleteItemNumber);
            } else {
                isDeletingItemTransition = true;
                hideItem(deleteItemNumber, false);
            }
        }
    }

    private void receiveWeatherInformationDialogResult(SavedStateHandle savedStateHandle) {
        // 天気情報読込ダイアログフラグメントから結果受取
        boolean containsDialogResult =
                savedStateHandle.contains(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
        if (containsDialogResult) {
            Integer selectedButton =
                    savedStateHandle.get(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
            if (selectedButton == null) {
                return;
            }
            if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
                LocalDate loadDiaryDate = editDiaryViewModel.getDateLiveData().getValue();
                if (loadDiaryDate == null) {
                    return;
                }
                editDiaryViewModel.fetchWeatherInformation(
                        loadDiaryDate,
                        settingsViewModel.getLatitude(),
                        settingsViewModel.getLongitude()
                );
            }
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
        if (isStartDiaryFragment && !editDiaryViewModel.getHasPreparedDiary()) {
            editDiaryViewModel.prepareDiary(editDiaryDate, isLoadingExistingDiary);
        }

    }

    private void setUpToolBar() {
        // TODO:下記コメントアウトコードはobserverで管理(確認後削除)
        /*boolean isNewDiary = editDiaryViewModel.getLoadedDateLiveData().getValue().isEmpty();
        if (isNewDiary) {
            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
        } else {
            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
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
                            LocalDate loadedDate = editDiaryViewModel.getLoadedDateLiveData().getValue();
                            LocalDate savingDate = editDiaryViewModel.getDateLiveData().getValue();
                            if (savingDate == null) {
                                return false;
                            }
                            boolean isNewDiary = true;
                            boolean isMatchedDate = false;
                            if (loadedDate != null) {
                                isNewDiary = false;
                                isMatchedDate = loadedDate.equals(savingDate);
                            }

                            boolean isUpdateDiary = editDiaryViewModel.hasDiary(savingDate);
                            if (isUpdateDiary) {
                                int updateType;
                                if (isMatchedDate) {
                                    Log.d("保存形式確認", "上書保存");
                                    boolean isSuccessful = editDiaryViewModel.saveDiary();
                                    if (isSuccessful) {
                                        showShowDiaryFragment(savingDate);
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
                                    isSuccessful = editDiaryViewModel.saveDiary();
                                } else {
                                    Log.d("保存形式確認", "日付変更新規保存");
                                    isSuccessful = editDiaryViewModel.deleteExistingDiaryAndSaveDiary();
                                }
                                if (isSuccessful) {
                                    showShowDiaryFragment(savingDate);
                                }
                            }
                            return true;
                        } else if (item.getItemId() == R.id.showDiaryToolbarOptionDeleteDiary) {
                            editDiaryViewModel.deleteDiary();
                        }
                        return false;
                    }
                });

        editDiaryViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate date) {
                        if (date == null) {
                            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
                        } else {
                            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
                        }
                    }
                });
    }

    // キーボード入力不要View作成
    private void setUpNoKeyBoardViews() {
        noKeyboardViews.add(binding.editTextDate);
        noKeyboardViews.add(binding.spinnerWeather1);
        noKeyboardViews.add(binding.spinnerWeather2);
        noKeyboardViews.add(binding.spinnerCondition);
        noKeyboardViews.add(binding.includeItem1.editTextItemTitle);
        noKeyboardViews.add(binding.includeItem1.imageButtonItemDelete);
        noKeyboardViews.add(binding.includeItem2.editTextItemTitle);
        noKeyboardViews.add(binding.includeItem2.imageButtonItemDelete);
        noKeyboardViews.add(binding.includeItem3.editTextItemTitle);
        noKeyboardViews.add(binding.includeItem3.imageButtonItemDelete);
        noKeyboardViews.add(binding.includeItem4.editTextItemTitle);
        noKeyboardViews.add(binding.includeItem4.imageButtonItemDelete);
        noKeyboardViews.add(binding.includeItem5.editTextItemTitle);
        noKeyboardViews.add(binding.includeItem5.imageButtonItemDelete);
        noKeyboardViews.add(binding.imageButtonAddItem);
    }

    // 日付入力欄設定
    private void setUpDateInputField() {
        binding.editTextDate.setFocusable(true);
        binding.editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDate date = editDiaryViewModel.getDateLiveData().getValue();
                if (date == null) {
                    return;
                }
                showDatePickerDialog(date);
            }
        });

        editDiaryViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
            @Override
            public void onChanged(LocalDate date) {
                if (date == null) {
                    return;
                }
                binding.editTextDate.setText(DateConverter.toStringLocalDate(date));
                Log.d("EditDiaryInputDate", "SelectedDate:" + date);
                Log.d("EditDiaryInputDate", "lastSelectedDate:" + lastSelectedDate);
                boolean shouldShowDialog = shouldShowLoadingExistingDiaryDialogOnDateChanged(date);
                if (shouldShowDialog) {
                    showLoadingExistingDiaryDialog(date);
                } else {
                    // 読込確認Dialog表示時は、確認後下記処理を行う。
                    LocalDate loadedDate = editDiaryViewModel.getLoadedDateLiveData().getValue();
                    if (loadedDate != null && !date.equals(lastSelectedDate)) {
                        fetchWeatherInformation(date);
                    }
                }
                lastSelectedDate = date;
            }
        });
    }

    private boolean shouldShowLoadingExistingDiaryDialogOnDateChanged(@NonNull LocalDate changedDate) {
        if (changedDate.equals(lastSelectedDate)) {
            return false;
        }
        LocalDate loadedDate = editDiaryViewModel.getLoadedDateLiveData().getValue();
        if (changedDate.equals(loadedDate)) {
            return false;
        }
        return editDiaryViewModel.hasDiary(changedDate);
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
        weather2ArrayAdapter = createWeatherSpinnerAdapter();
        binding.spinnerWeather2.setAdapter(weather2ArrayAdapter);

        // TODO:天気情報取得が同期処理なら不要
        binding.spinnerWeather1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    editDiaryViewModel.cancelWeatherSelectionPreparation();
                }
                return false;
            }
        });
        binding.spinnerWeather1
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        WeatherConverter weatherConverter = new WeatherConverter();
                        Weathers weather = weatherConverter.toWeather(position);
                        editDiaryViewModel.updateWeather1(weather);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        editDiaryViewModel.getWeather1LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Weathers>() {
                    @Override
                    public void onChanged(Weathers weather) {
                        int weatherNumber;
                        if (weather == null) {
                            weatherNumber = 0;
                        } else {
                            weatherNumber = weather.toWeatherNumber();
                        }
                        if (weatherNumber != binding.spinnerWeather1.getSelectedItemPosition()) {
                            binding.spinnerWeather1.setSelection(weatherNumber);
                        }

                        // Weather2 Spinner有効無効切替
                        if (weather == Weathers.UNKNOWN) {
                            // TODO:下記アダプター作成コード確認
                            binding.spinnerWeather2.setEnabled(false);
                            binding.spinnerWeather2.setAdapter(weatherArrayAdapter);
                            binding.spinnerWeather2.setSelection(0);
                        } else {
                            weather2ArrayAdapter = createWeatherSpinnerAdapter(weather);
                            binding.spinnerWeather2.setAdapter(weather2ArrayAdapter);
                            binding.spinnerWeather2.setEnabled(true);
                            // HACK:AdapterをセットするとSpinnerの選択アイテムがデフォルト値になるため下記対応。
                            editDiaryViewModel.updateWeather2(editDiaryViewModel.getWeather2LiveData().getValue());
                        }
                    }
                });

        binding.spinnerWeather2
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        String strWeather = weather2ArrayAdapter.getItem(position);
                        WeatherConverter weatherConverter = new WeatherConverter();
                        Weathers weather = weatherConverter.toWeather(requireContext(), strWeather);
                        editDiaryViewModel.updateWeather2(weather);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });

        editDiaryViewModel.getWeather2LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Weathers>() {
                    @Override
                    public void onChanged(Weathers weather) {
                        Weathers _weather = weather;
                        if (weather == null) {
                            _weather = Weathers.UNKNOWN;
                        }
                        String strWeather = _weather.toString(requireContext());
                        int position = weather2ArrayAdapter.getPosition(strWeather);
                        binding.spinnerWeather2.setSelection(position);
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
                            LocalDate date = editDiaryViewModel.getDateLiveData().getValue();
                            if (date == null) {
                                return;
                            }
                            fetchWeatherInformation(date);
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
                        ConditionConverter conditionConverter = new ConditionConverter();
                        Conditions condition = conditionConverter.toCondition(position);
                        editDiaryViewModel.updateCondition(condition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        editDiaryViewModel.getConditionLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Conditions>() {
                    @Override
                    public void onChanged(Conditions condition) {
                        int conditionNumber;
                        if (condition == null) {
                            conditionNumber = 0;
                        } else {
                            conditionNumber = condition.toConditionNumber();
                        }
                        if (conditionNumber != binding.spinnerCondition.getSelectedItemPosition()) {
                            binding.spinnerCondition.setSelection(conditionNumber);
                        }
                    }
                });
    }

    private void setUpTitleInputField() {
        // タイトル入力欄設定
        setupEditText(
                binding.editTextTitle,
                binding.textTitleLength,
                15,
                binding.nestedScrollFullScreen, // 背景View
                noKeyboardViews
        );
    }

    private void setUpItemInputField() {
        // 項目入力欄関係Viewを配列に格納
        final int MAX_ITEMS = DiaryLiveData.MAX_ITEMS;
        TextView[] textItems = new TextView[MAX_ITEMS];
        EditText[] editTextItemsTitle = new EditText[MAX_ITEMS];
        NestedScrollView[] nestedScrollItemsComment = new NestedScrollView[MAX_ITEMS];
        EditText[] editTextItemsComment = new EditText[MAX_ITEMS];
        TextView[] textItemsCommentLength = new TextView[MAX_ITEMS];
        ImageButton[] imageButtonItemsDelete = new ImageButton[MAX_ITEMS];

        textItems[0] = binding.includeItem1.textItemNumber;
        editTextItemsTitle[0] = binding.includeItem1.editTextItemTitle;
        nestedScrollItemsComment[0] = binding.includeItem1.nestedScrollItemComment;
        editTextItemsComment[0] = binding.includeItem1.editTextItemComment;
        textItemsCommentLength[0] = binding.includeItem1.textItemCommentLength;
        imageButtonItemsDelete[0] = binding.includeItem1.imageButtonItemDelete;

        textItems[1] = binding.includeItem2.textItemNumber;
        editTextItemsTitle[1] = binding.includeItem2.editTextItemTitle;
        nestedScrollItemsComment[1] = binding.includeItem2.nestedScrollItemComment;
        editTextItemsComment[1] = binding.includeItem2.editTextItemComment;
        textItemsCommentLength[1] = binding.includeItem2.textItemCommentLength;
        imageButtonItemsDelete[1] = binding.includeItem2.imageButtonItemDelete;

        textItems[2] = binding.includeItem3.textItemNumber;
        editTextItemsTitle[2] = binding.includeItem3.editTextItemTitle;
        nestedScrollItemsComment[2] = binding.includeItem3.nestedScrollItemComment;
        editTextItemsComment[2] = binding.includeItem3.editTextItemComment;
        textItemsCommentLength[2] = binding.includeItem3.textItemCommentLength;
        imageButtonItemsDelete[2] = binding.includeItem3.imageButtonItemDelete;

        textItems[3] = binding.includeItem4.textItemNumber;
        editTextItemsTitle[3] = binding.includeItem4.editTextItemTitle;
        nestedScrollItemsComment[3] = binding.includeItem4.nestedScrollItemComment;
        editTextItemsComment[3] = binding.includeItem4.editTextItemComment;
        textItemsCommentLength[3] = binding.includeItem4.textItemCommentLength;
        imageButtonItemsDelete[3] = binding.includeItem4.imageButtonItemDelete;

        textItems[4] = binding.includeItem5.textItemNumber;
        editTextItemsTitle[4] = binding.includeItem5.editTextItemTitle;
        nestedScrollItemsComment[4] = binding.includeItem5.nestedScrollItemComment;
        editTextItemsComment[4] = binding.includeItem5.editTextItemComment;
        textItemsCommentLength[4] = binding.includeItem5.textItemCommentLength;
        imageButtonItemsDelete[4] = binding.includeItem5.imageButtonItemDelete;

        // 項目欄設定
        // 項目タイトル入力欄設定
        for (int i = 0; i < editTextItemsTitle.length; i++) {
            int inputItemNo =  i + 1;
            editTextItemsTitle[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 項目タイトル入力フラグメント起動
                    String inputItemTitle =
                            editDiaryViewModel.getItemTitleLiveData(inputItemNo).getValue();
                    NavDirections action =
                            EditDiaryFragmentDirections
                                    .actionEditDiaryFragmentToSelectItemTitleFragment(
                                            inputItemNo, inputItemTitle);
                    navController.navigate(action);
                }
            });
        }

        // 項目コメント入力欄設定。
        for (int i = 0; i < MAX_ITEMS; i++) {
            setupEditText(
                    editTextItemsComment[i],
                    textItemsCommentLength[i],
                    50,
                    binding.nestedScrollFullScreen, // 背景View,
                    noKeyboardViews
            );
        }

        // 項目追加ボタン設定
        binding.imageButtonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageButtonAddItem.setEnabled(false);

                // TODO:Observerへ移行
                Integer NumVisibleItems = editDiaryViewModel.getNumVisibleItemsLiveData().getValue();
                if (NumVisibleItems != null) {
                    int addItemNumber = NumVisibleItems + 1;
                    showItem(addItemNumber, false);
                    editDiaryViewModel.incrementVisibleItemsCount();
                }
            }
        });

        // 項目削除ボタン設定
        for (int i = 0; i < MAX_ITEMS; i++) {
            int deleteItemNumber = i + 1;
            imageButtonItemsDelete[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDiaryDialog(deleteItemNumber);
                }
            });
        }

        // 項目欄MotionLayout設定
        for (int i = 0; i < MAX_ITEMS; i++) {
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
                    Log.d("20240605", "ItemLiveData" + String.valueOf(itemNumber) + " onTransitionCompleted");
                    // 対象項目欄を閉じた後の処理
                    if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                        Log.d("20240605", "currentId:hided_state");
                        if (isDeletingItemTransition) {
                            editDiaryViewModel.deleteItem(itemNumber);
                            isDeletingItemTransition = false;
                            setUpItemsLayout(); // TODO:必要？
                        }
                        binding.imageButtonAddItem.setVisibility(View.VISIBLE);
                    } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                        Log.d("20240605", "currentId:showed_state");
                        binding.imageButtonAddItem.setEnabled(true);
                        // TODO:Observerへ移行
                        Integer numVisibleItems = editDiaryViewModel.getNumVisibleItemsLiveData().getValue();
                        if (numVisibleItems != null && numVisibleItems == MAX_ITEMS) {
                            binding.imageButtonAddItem.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
                    // 処理なし
                }
            });

        }

        editDiaryViewModel.getNumVisibleItemsLiveData()
                        .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer integer) {
                                // TODO:保留
                            }
                        });

        setUpItemsLayout(); //必要数の項目欄表示
    }

    private MotionLayout selectItemMotionLayout(int itemNumber) {
        MotionLayout itemMotionLayout = null;
        switch (itemNumber) {
            case 1:
                itemMotionLayout = binding.includeItem1.motionLayoutEditDiaryItem;
                break;
            case 2:
                itemMotionLayout = binding.includeItem2.motionLayoutEditDiaryItem;
                break;
            case 3:
                itemMotionLayout = binding.includeItem3.motionLayoutEditDiaryItem;
                break;
            case 4:
                itemMotionLayout = binding.includeItem4.motionLayoutEditDiaryItem;
                break;
            case 5:
                itemMotionLayout = binding.includeItem5.motionLayoutEditDiaryItem;
                break;
        }
        return itemMotionLayout;
    }

    private void setUpItemsLayout() {
        Integer numVisibleItems = editDiaryViewModel.getNumVisibleItemsLiveData().getValue();
        if (numVisibleItems == null) {
            return;
        }
        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            if (itemNumber <= numVisibleItems) {
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

    private void setUpPictureInputField() {
        // TODO
    }

    private void setUpErrorObserver() {
        // エラー表示
        editDiaryViewModel.getIsDiarySavingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showDiarySavingErrorDialog();
                    editDiaryViewModel.clearDiarySavingError();
                }
            }
        });
        editDiaryViewModel.getIsDiaryLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showDiaryLoadingErrorDialog();
                    editDiaryViewModel.clearDiaryLoadingError();
                }
            }
        });
        editDiaryViewModel.getIsDiaryDeleteErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showDiaryDeleteErrorDialog();
                    editDiaryViewModel.clearDiaryDeleteError();
                }
            }
        });
        editDiaryViewModel.getIsWeatherLoadingErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                if (aBoolean) {
                    showWeatherLoadingErrorDialog();
                    editDiaryViewModel.clearWeatherLoadingError();
                }
            }
        });
    }

    // TODO:マテリアルインプットフィールドに置換
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

    // TODO:マテリアルインプットフィールドに置換
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

    private boolean fetchWeatherInformation(@NonNull LocalDate date) {
        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        Boolean isChecked =
                settingsViewModel.getIsCheckedGettingWeatherInformationLiveData().getValue();
        if (isChecked == null) {
            shouldPrepareWeatherSelection = true;
            return false;
        } else if (!isChecked) {
            return false;
        }
        Boolean hasUpdatedLocation = settingsViewModel.getHasUpdatedLocationLiveData().getValue();
        if (hasUpdatedLocation == null) {
            shouldPrepareWeatherSelection = true;
            return false;
        }
        if (hasUpdatedLocation) {
            Log.d("20240719", "onTextChanged:prepareWeatherSelection");
            // 本フラグメント起動時のみダイアログなしで天気情報取得
            if (lastSelectedDate == null) {
                editDiaryViewModel.fetchWeatherInformation(
                        date,
                        settingsViewModel.getLatitude(),
                        settingsViewModel.getLongitude()
                );
            } else {
                showWeatherInformationDialog(date);
            }
        }
        return true;
    }

    public void showShowDiaryFragment(@NonNull LocalDate date) {
        boolean isStartDiaryFragment =
                EditDiaryFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        NavDirections action;
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるactionを切り替える。
        if (isStartDiaryFragment) {
            action = EditDiaryFragmentDirections
                    .actionEditDiaryFragmentToShowDiaryFragmentPattern2(date);
        } else {
            action = EditDiaryFragmentDirections
                    .actionEditDiaryFragmentToShowDiaryFragmentPattern1(date);
        }
        navController.navigate(action);
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
        navController.navigate(action);
    }



    private void showLoadingExistingDiaryDialog(LocalDate date) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToLoadExistingDiaryDialog(date);
        navController.navigate(action);
    }

    private void showDeleteConfirmationDiaryDialog(int itemNumber) {
        NavDirections action =
                EditDiaryFragmentDirections
                        .actionEditDiaryFragmentToDeleteConfirmationDialog(itemNumber);
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
        navController.navigate(action);
    }
}
