package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.ui.TestDiariesSaver;
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.internal.Preconditions;

@AndroidEntryPoint
public class DiaryEditFragment extends BaseFragment {

    // View関係
    private FragmentDiaryEditBinding binding;
    private boolean isDeletingItemTransition = false;
    private final String TOOL_BAR_TITLE_NEW = "新規作成";
    private final String TOOL_BAR_TITLE_EDIT = "編集中";
    private LocalDate lastSelectedDate;
    private ArrayAdapter<String> weather2ArrayAdapter;

    // ViewModel
    private DiaryEditViewModel diaryEditViewModel;
    private SettingsViewModel settingsViewModel;

    // 位置情報
    private boolean shouldPrepareWeatherSelection = false;

    // 上書保存方法
    public static final int UPDATE_TYPE_UPDATE_ONLY = 0;
    public static final int UPDATE_TYPE_DELETE_AND_UPDATE = 1;
    // TODO:上記を下記に変更
    private enum UpdateType {
        UPDATE_ONLY,
        DELETE_AND_UPDATE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryEditViewModel = provider.get(DiaryEditViewModel.class);
        diaryEditViewModel.initialize();
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentDiaryEditBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryEditViewModel(diaryEditViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDiaryData();
        setUpToolBar();
        setUpDateInputField();
        setUpWeatherInputField();
        setUpConditionInputField();
        setUpTitleInputField();
        setUpItemInputField();
        setUpPictureInputField();
        setupEditText();

        // TODO:最終的に削除
        binding.fabTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("20240823", "OnClick");
                TestDiariesSaver testDiariesSaver = new TestDiariesSaver(diaryEditViewModel);
                testDiariesSaver.save(28);
            }
        });
    }

    @Override
    protected void setUpThemeColor() {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        MutableLiveData<String> newItemTitleLiveData =
                savedStateHandle.getLiveData(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
        newItemTitleLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                // MEMO:結果がない場合もあるので"return"で返す。
                if (string == null) return;

                Integer itemNumber =
                        savedStateHandle.get(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                // MEMO:この時点で結果がない場合異常なので例外を発生させる。。
                Objects.requireNonNull(itemNumber);

                diaryEditViewModel.updateItemTitle(itemNumber, string);

                savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
            }
        });
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDatePickerDialogResult(savedStateHandle);
        receiveLoadExistingDiaryDialogResult(savedStateHandle);
        receiveUpdateExistingDiaryDialogResult(savedStateHandle);
        receiveDeleteConfirmDialogResult(savedStateHandle);
        receiveWeatherInformationDialogResult(savedStateHandle);
        retryErrorDialogShow();
        clearFocusAllEditText();
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
        savedStateHandle.remove(DiaryItemDeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        savedStateHandle.remove(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        diaryEditViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryEditViewModel));
        settingsViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(settingsViewModel));
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private void receiveDatePickerDialogResult(SavedStateHandle savedStateHandle) {
        LocalDate selectedDate = receiveResulFromDialog(DatePickerDialogFragment.KEY_SELECTED_DATE);
        if (selectedDate == null) {
            return;
        }

        diaryEditViewModel.updateDate(selectedDate);
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private void receiveLoadExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        Integer selectedButton = receiveResulFromDialog(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) {
            return;
        }
        LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
        if (date == null) {
            return;
        }

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            diaryEditViewModel.initialize();
            diaryEditViewModel.prepareDiary(date, true);
        } else {
            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();
            if (loadedDate == null) {
                return;
            }
            fetchWeatherInformation(date);
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveUpdateExistingDiaryDialogResult(SavedStateHandle savedStateHandle) {
        Integer updateType = receiveResulFromDialog(UpdateExistingDiaryDialogFragment.KEY_UPDATE_TYPE);
        if (updateType == null) {
            return;
        }

        boolean isSuccessful;
        switch (updateType) {
            case UPDATE_TYPE_DELETE_AND_UPDATE:
                Log.d("保存形式確認", "日付変更上書保存");
                isSuccessful = diaryEditViewModel.deleteExistingDiaryAndSaveDiary();
                break;
            case UPDATE_TYPE_UPDATE_ONLY:
            default:
                Log.d("保存形式確認", "上書保存");
                isSuccessful = diaryEditViewModel.saveDiary();
                break;
        }
        if (isSuccessful) {
            LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
            showDiaryShowFragment(date);
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private void receiveDeleteConfirmDialogResult(SavedStateHandle savedStateHandle) {
        Integer deleteItemNumber =
                receiveResulFromDialog(DiaryItemDeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        if (deleteItemNumber == null) {
            return;
        }
        Integer numVisibleItems = diaryEditViewModel.getNumVisibleItemsLiveData().getValue();
        if (numVisibleItems == null) {
            return;
        }

        if (deleteItemNumber == 1 && numVisibleItems.equals(deleteItemNumber)) {
            diaryEditViewModel.deleteItem(deleteItemNumber);
        } else {
            isDeletingItemTransition = true;
            hideItem(deleteItemNumber, false);
        }
    }

    private void receiveWeatherInformationDialogResult(SavedStateHandle savedStateHandle) {
        // 天気情報読込ダイアログフラグメントから結果受取
        Integer selectedButton =
                receiveResulFromDialog(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) {
            return;
        }

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            LocalDate loadDiaryDate = diaryEditViewModel.getDateLiveData().getValue();
            if (loadDiaryDate == null) {
                return;
            }
            diaryEditViewModel.fetchWeatherInformation(
                    loadDiaryDate,
                    settingsViewModel.getLatitude(),
                    settingsViewModel.getLongitude()
            );
        }
    }

    private void setUpDiaryData() {
        // 画面表示データ準備
        boolean isLoadingExistingDiary =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getIsLoadingDiary();
        LocalDate diaryDate =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getEditDiaryDate();
        if (!diaryEditViewModel.getHasPreparedDiary()) {
            diaryEditViewModel.prepareDiary(diaryDate, isLoadingExistingDiary);
        }

    }

    private void setUpToolBar() {
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
                        if (item.getItemId() == R.id.diaryEditToolbarOptionSaveDiary) {
                            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();
                            LocalDate savingDate = diaryEditViewModel.getDateLiveData().getValue();

                            boolean isNewDiary = true;
                            boolean isMatchedDate = false;
                            if (loadedDate != null) {
                                isNewDiary = false;
                                isMatchedDate = loadedDate.equals(savingDate);
                            }

                            boolean isUpdateDiary = diaryEditViewModel.hasDiary(savingDate);
                            if (isUpdateDiary) {
                                int updateType;
                                if (isMatchedDate) {
                                    Log.d("保存形式確認", "上書保存");
                                    boolean isSuccessful = diaryEditViewModel.saveDiary();
                                    if (isSuccessful) {
                                        showDiaryShowFragment(savingDate);
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
                                    isSuccessful = diaryEditViewModel.saveDiary();
                                } else {
                                    Log.d("保存形式確認", "日付変更新規保存");
                                    isSuccessful = diaryEditViewModel.deleteExistingDiaryAndSaveDiary();
                                }
                                if (isSuccessful) {
                                    showDiaryShowFragment(savingDate);
                                }
                            }
                            return true;
                        } else if (item.getItemId() == R.id.diaryEditToolbarOptionDeleteDiary) {
                            boolean isSuccessful = diaryEditViewModel.deleteDiary();
                            if (isSuccessful) {
                                navController.navigateUp();
                            }
                        }
                        return false;
                    }
                });

        diaryEditViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate date) {
                        Menu menu = binding.materialToolbarTopAppBar.getMenu();
                        MenuItem deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary);
                        if (date == null) {
                            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_NEW);
                            deleteMenuItem.setEnabled(false);
                        } else {
                            binding.materialToolbarTopAppBar.setTitle(TOOL_BAR_TITLE_EDIT);
                            deleteMenuItem.setEnabled(true);
                        }
                    }
                });
    }

    // 日付入力欄設定
    private void setUpDateInputField() {
        binding.textInputEditTextDate.setInputType(EditorInfo.TYPE_NULL); //キーボード非表示設定
        binding.textInputEditTextDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Objects.requireNonNull(v);

                if (event.getAction() != MotionEvent.ACTION_UP) return false;

                hideKeyboard(v);

                LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
                showDatePickerDialog(date);
                return false;
            }
        });

        diaryEditViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
            @Override
            public void onChanged(LocalDate date) {
                if (date == null) {
                    return;
                }

                DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
                binding.textInputEditTextDate.setText(dateTimeStringConverter.toStringDate(date));
                Log.d("DiaryEditInputDate", "SelectedDate:" + date);
                Log.d("DiaryEditInputDate", "lastSelectedDate:" + lastSelectedDate);
                boolean shouldShowDialog = shouldShowLoadingExistingDiaryDialogOnDateChanged(date);
                if (shouldShowDialog) {
                    showLoadingExistingDiaryDialog(date);
                } else {
                    // 読込確認Dialog表示時は、確認後下記処理を行う。
                    if (!date.equals(lastSelectedDate)) {
                        fetchWeatherInformation(date);
                    }
                }
                lastSelectedDate = date;
            }
        });
    }

    private boolean shouldShowLoadingExistingDiaryDialogOnDateChanged(@NonNull LocalDate changedDate) {
        if (changedDate.equals(lastSelectedDate)) {
            Log.d("EditFragment", "shouldShowLoadingExistingDiaryDialogOnDateChanged:changedDate.equals(lastSelectedDate) = true");
            return false;
        }
        LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();
        if (changedDate.equals(loadedDate)) {
            Log.d("EditFragment", "shouldShowLoadingExistingDiaryDialogOnDateChanged:changedDate.equals(loadedDate) = true");
            return false;
        }
        boolean hasDiary = diaryEditViewModel.hasDiary(changedDate);
        Log.d("EditFragment", "shouldShowLoadingExistingDiaryDialogOnDateChanged:hasDiary(changedDate) = " + hasDiary);
        return hasDiary;
    }

    // 天気入力欄。
    private void setUpWeatherInputField() {
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter();
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter);
        weather2ArrayAdapter = createWeatherSpinnerAdapter();
        binding.autoCompleteTextWeather2.setAdapter(weather2ArrayAdapter);

        // TODO:天気情報取得が同期処理なら不要
        /*binding.autoCompleteTextWeather1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    diaryEditViewModel.cancelWeatherSelectionPreparation();
                }
                return false;
            }
        });*/

        binding.autoCompleteTextWeather1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter listAdapter = binding.autoCompleteTextWeather1.getAdapter();
                ArrayAdapter<?> arrayAdapter;
                if (listAdapter instanceof ArrayAdapter) {
                    arrayAdapter = (ArrayAdapter<?>) listAdapter;
                } else {
                    throw new ClassCastException();
                }
                String strWeather = (String) arrayAdapter.getItem(position);
                WeatherConverter weatherConverter = new WeatherConverter();
                Weathers weather = weatherConverter.toWeather(requireContext(), strWeather);
                diaryEditViewModel.updateWeather1(weather);
                binding.autoCompleteTextWeather1.clearFocus();
            }
        });

        diaryEditViewModel.getWeather1LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Weathers>() {
                    @Override
                    public void onChanged(Weathers weather) {
                        if (weather == null) return;

                        String strWeather = weather.toString(requireContext());
                        binding.autoCompleteTextWeather1.setText(strWeather, false);

                        // Weather2 Spinner有効無効切替
                        boolean isEnabled = (weather != Weathers.UNKNOWN);
                        binding.textInputLayoutWeather2.setEnabled(isEnabled);
                        binding.autoCompleteTextWeather2.setEnabled(isEnabled);

                        if (weather == Weathers.UNKNOWN || diaryEditViewModel.isEqualWeathers()) {
                            binding.autoCompleteTextWeather2.setAdapter(weatherArrayAdapter);
                            diaryEditViewModel.updateWeather2(Weathers.UNKNOWN);
                        } else {
                            weather2ArrayAdapter = createWeatherSpinnerAdapter(weather);
                            binding.autoCompleteTextWeather2.setAdapter(weather2ArrayAdapter);
                        }
                    }
                });

        binding.autoCompleteTextWeather2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter listAdapter = binding.autoCompleteTextWeather2.getAdapter();
                ArrayAdapter<?> arrayAdapter;
                if (listAdapter instanceof ArrayAdapter) {
                    arrayAdapter = (ArrayAdapter<?>) listAdapter;
                } else {
                    throw new ClassCastException();
                }
                String strWeather = (String) arrayAdapter.getItem(position);
                WeatherConverter weatherConverter = new WeatherConverter();
                Weathers weather = weatherConverter.toWeather(requireContext(), strWeather);
                diaryEditViewModel.updateWeather2(weather);
                binding.autoCompleteTextWeather2.clearFocus();
            }
        });

        diaryEditViewModel.getWeather2LiveData()
                .observe(getViewLifecycleOwner(), new Observer<Weathers>() {
                    @Override
                    public void onChanged(Weathers weather) {
                        if (weather == null) return;

                        String strWeather = weather.toString(requireContext());
                        binding.autoCompleteTextWeather2.setText(strWeather, false);
                    }
                });

        settingsViewModel.getHasUpdatedLocationLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            Log.d("20240829", "getHasUpdatedLocationLiveData():null");
                            return;
                        }
                        Log.d("20240829", "getHasUpdatedLocationLiveData():" + aBoolean);
                        Log.d("20240829", "shouldPrepareWeatherSelection:" + shouldPrepareWeatherSelection);
                        if (aBoolean && shouldPrepareWeatherSelection) {
                            LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
                            if (date == null) {
                                throw new NullPointerException();
                            }
                            fetchWeatherInformation(date);
                        }
                    }
                });
    }

    @NonNull
    private ArrayAdapter<String> createWeatherSpinnerAdapter(@Nullable Weathers... excludedWeathers) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        int themeResId = themeColor.getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireContext(), themeResId);

        List<String> weatherItemList = new ArrayList<>();
        for (Weathers weather: Weathers.values()) {
            if (!isExcludedWeather(weather, excludedWeathers)) {
                weatherItemList.add(weather.toString(requireContext()));
            }
        }

        return new ArrayAdapter<>(contextWithTheme, R.layout.layout_drop_down_list_item, weatherItemList);
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
        // ドロップダウン設定
        ArrayAdapter<String> conditionArrayAdapter = createConditionSpinnerAdapter();
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter);
        binding.autoCompleteTextCondition.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter listAdapter = binding.autoCompleteTextCondition.getAdapter();
                ArrayAdapter<?> arrayAdapter;
                if (listAdapter instanceof ArrayAdapter) {
                    arrayAdapter = (ArrayAdapter<?>) listAdapter;
                } else {
                    throw new ClassCastException();
                }
                String strCondition = (String) arrayAdapter.getItem(position);
                ConditionConverter converter = new ConditionConverter();
                Conditions condition = converter.toCondition(requireContext(), strCondition);
                diaryEditViewModel.updateCondition(condition);
                binding.autoCompleteTextCondition.clearFocus();
            }
        });

        diaryEditViewModel.getConditionLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Conditions>() {
                    @Override
                    public void onChanged(Conditions condition) {
                        if (condition == null) return;

                        String strCondition = condition.toString(requireContext());
                        binding.autoCompleteTextCondition.setText(strCondition, false);
                    }
                });
    }

    @NonNull
    private ArrayAdapter<String> createConditionSpinnerAdapter() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        int themeResId = themeColor.getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireContext(), themeResId);

        List<String> conditonItemList = new ArrayList<>();
        for (Conditions condition: Conditions.values()) {
            conditonItemList.add(condition.toString(requireContext()));
        }

        return new ArrayAdapter<>(contextWithTheme, R.layout.layout_drop_down_list_item, conditonItemList);
    }

    private void setUpTitleInputField() {
        // 処理なし
    }

    private void setUpItemInputField() {
        // 項目入力欄関係Viewを配列に格納
        final int MAX_ITEMS = DiaryLiveData.MAX_ITEMS;
        TextView[] textItems = new TextView[MAX_ITEMS];
        TextInputLayout[] textInputLayoutItemsTitle = new TextInputLayout[MAX_ITEMS];
        TextInputEditText[] textInputEditTextItemsTitle = new TextInputEditText[MAX_ITEMS];
        TextInputEditText[] textInputEditTextItemsComment = new TextInputEditText[MAX_ITEMS];
        ImageButton[] imageButtonItemsDelete = new ImageButton[MAX_ITEMS];

        textItems[0] = binding.includeItem1.textItemNumber;
        textInputLayoutItemsTitle[0] = binding.includeItem1.textInputLayoutItemTitle;
        textInputEditTextItemsTitle[0] = binding.includeItem1.textInputEditTextItemTitle;
        textInputEditTextItemsComment[0] = binding.includeItem1.textInputEditTextItemComment;
        imageButtonItemsDelete[0] = binding.includeItem1.imageButtonItemDelete;

        textItems[1] = binding.includeItem2.textItemNumber;
        textInputLayoutItemsTitle[1] = binding.includeItem2.textInputLayoutItemTitle;
        textInputEditTextItemsTitle[1] = binding.includeItem2.textInputEditTextItemTitle;
        textInputEditTextItemsComment[1] = binding.includeItem2.textInputEditTextItemComment;
        imageButtonItemsDelete[1] = binding.includeItem2.imageButtonItemDelete;

        textItems[2] = binding.includeItem3.textItemNumber;
        textInputLayoutItemsTitle[2] = binding.includeItem3.textInputLayoutItemTitle;
        textInputEditTextItemsTitle[2] = binding.includeItem3.textInputEditTextItemTitle;
        textInputEditTextItemsComment[2] = binding.includeItem3.textInputEditTextItemComment;
        imageButtonItemsDelete[2] = binding.includeItem3.imageButtonItemDelete;

        textItems[3] = binding.includeItem4.textItemNumber;
        textInputLayoutItemsTitle[3] = binding.includeItem4.textInputLayoutItemTitle;
        textInputEditTextItemsTitle[3] = binding.includeItem4.textInputEditTextItemTitle;
        textInputEditTextItemsComment[3] = binding.includeItem4.textInputEditTextItemComment;
        imageButtonItemsDelete[3] = binding.includeItem4.imageButtonItemDelete;

        textItems[4] = binding.includeItem5.textItemNumber;
        textInputLayoutItemsTitle[4] = binding.includeItem5.textInputLayoutItemTitle;
        textInputEditTextItemsTitle[4] = binding.includeItem5.textInputEditTextItemTitle;
        textInputEditTextItemsComment[4] = binding.includeItem5.textInputEditTextItemComment;
        imageButtonItemsDelete[4] = binding.includeItem5.imageButtonItemDelete;

        // 項目欄設定
        // 項目タイトル入力欄設定
        for (int i = 0; i < textInputEditTextItemsTitle.length; i++) {
            int inputItemNumber =  i + 1;
            textInputEditTextItemsTitle[i].setInputType(EditorInfo.TYPE_NULL); //キーボード非表示設定
            textInputEditTextItemsTitle[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Objects.requireNonNull(v);
                    if (event.getAction() != MotionEvent.ACTION_UP) return false;

                    hideKeyboard(v);

                    // 項目タイトル入力フラグメント起動
                    String inputItemTitle =
                            diaryEditViewModel.getItemTitleLiveData(inputItemNumber).getValue();
                    showDiaryItemTitleEditFragment(inputItemNumber, inputItemTitle);
                    return false;
                }
            });
        }

        // 項目追加ボタン設定
        binding.imageButtonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageButtonAddItem.setEnabled(false);
                diaryEditViewModel.incrementVisibleItemsCount();
            }
        });

        // 項目削除ボタン設定
        for (int i = 0; i < MAX_ITEMS; i++) {
            int deleteItemNumber = i + 1;
            imageButtonItemsDelete[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDiaryItemDeleteConfirmationDiaryDialog(deleteItemNumber);
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
                    Log.d("MotionLayout", "ItemLiveData" + itemNumber + " onTransitionCompleted");
                    // 対象項目欄削除後の処理
                    if (currentId == R.id.motion_scene_edit_diary_item_hided_state) {
                        Log.d("MotionLayout", "currentId:hided_state");
                        if (isDeletingItemTransition) {
                            diaryEditViewModel.deleteItem(itemNumber);
                            isDeletingItemTransition = false;
                        }
                        binding.imageButtonAddItem.setVisibility(View.VISIBLE);

                    // 対象項目欄追加後の処理
                    } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                        Log.d("MotionLayout", "currentId:showed_state");
                        binding.imageButtonAddItem.setEnabled(true);
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
                    // 処理なし
                }
            });

        }

        diaryEditViewModel.getNumVisibleItemsLiveData()
                        .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer integer) {
                                if (integer == null) {
                                    return;
                                }

                                // 項目欄追加ボタン表示切替
                                // TODO:使用不可時はボタンをぼかすように変更する。
                                if (integer == MAX_ITEMS) {
                                    binding.imageButtonAddItem.setVisibility(View.INVISIBLE);
                                }

                                setUpItemsLayout(integer);
                            }
                        });
    }

    private MotionLayout selectItemMotionLayout(int itemNumber) {
        MotionLayout itemMotionLayout = null;
        switch (itemNumber) {
            case 1:
                itemMotionLayout = binding.includeItem1.motionLayoutDiaryEditItem;
                break;
            case 2:
                itemMotionLayout = binding.includeItem2.motionLayoutDiaryEditItem;
                break;
            case 3:
                itemMotionLayout = binding.includeItem3.motionLayoutDiaryEditItem;
                break;
            case 4:
                itemMotionLayout = binding.includeItem4.motionLayoutDiaryEditItem;
                break;
            case 5:
                itemMotionLayout = binding.includeItem5.motionLayoutDiaryEditItem;
                break;
        }

        if (itemMotionLayout == null) {
            throw new IllegalArgumentException();
        }

        return itemMotionLayout;
    }

    private void setUpItemsLayout(Integer numItems) {
        Preconditions.checkNotNull(numItems);
        if (numItems < 1) {
            throw new IllegalArgumentException();
        }
        if (numItems > DiaryLiveData.MAX_ITEMS) {
            throw new IllegalArgumentException();
        }

        // MEMO:LifeCycleがResumedの時のみ項目欄のモーション追加処理を行う。
        //      削除処理はObserverで適当なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
        if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            int numShowedItems = countShowedItems();
            int differenceValue = numItems - numShowedItems;
            if (numItems > numShowedItems && differenceValue == 1) {
                showItem(numItems, false);
                return;
            }
        }

        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            if (itemNumber <= numItems) {
                showItem(itemNumber, true);
            } else {
                hideItem(itemNumber, true);
            }
        }
    }

    private void hideItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (isJump) {
            itemMotionLayout
                    .transitionToState(R.id.motion_scene_edit_diary_item_hided_state, 1);
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_hided_state);
        }
    }

    private void showItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (isJump) {
            itemMotionLayout
                    .transitionToState(R.id.motion_scene_edit_diary_item_showed_state, 1);
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_showed_state);
        }
    }

    private int countShowedItems() {
        int numShowwdItems = 0;
        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            MotionLayout motionLayout = selectItemMotionLayout(itemNumber);
            if (motionLayout.getCurrentState() != R.id.motion_scene_edit_diary_item_showed_state) {
                continue;
            }
            numShowwdItems++;
        }
        return numShowwdItems;
    }

    private void setUpPictureInputField() {
        // TODO
    }

    private void setupEditText() {
        TextInputSetup textInputSetup = new TextInputSetup(requireActivity());

        TextInputLayout[] allTextInputLayouts = createAllTextInputLayoutList().toArray(new TextInputLayout[0]);
        textInputSetup.setUpFocusClearOnClickBackground(binding.viewNestedScrollBackground, allTextInputLayouts);

        textInputSetup.setUpKeyboardCloseOnEnter(binding.textInputLayoutTitle);

        TextInputLayout[] scrollableTextInputLayouts = {
                binding.includeItem1.textInputLayoutItemComment,
                binding.includeItem2.textInputLayoutItemComment,
                binding.includeItem3.textInputLayoutItemComment,
                binding.includeItem4.textInputLayoutItemComment,
                binding.includeItem5.textInputLayoutItemComment,
        };
        textInputSetup.setUpScrollable(scrollableTextInputLayouts);

        TextInputLayout[] clearableTextInputLayouts = {
                binding.textInputLayoutTitle,
                binding.includeItem1.textInputLayoutItemTitle,
                binding.includeItem2.textInputLayoutItemTitle,
                binding.includeItem3.textInputLayoutItemTitle,
                binding.includeItem4.textInputLayoutItemTitle,
                binding.includeItem5.textInputLayoutItemTitle,
        };
        TextInputSetup.ClearButtonSetUpTransitionListener transitionListener =
                textInputSetup.createClearButtonSetupTransitionListener(clearableTextInputLayouts);
        addTransitionListener(transitionListener);
    }

    private void clearFocusAllEditText() {
        List<TextInputLayout> textInputLayoutList = createAllTextInputLayoutList();
        textInputLayoutList.stream().forEach(x -> {
            Objects.requireNonNull(x);

            EditText editText = x.getEditText();
            Objects.requireNonNull(editText);
            editText.clearFocus();
        });
    }

    private List<TextInputLayout> createAllTextInputLayoutList() {
        return List.of(
                binding.textInputLayoutDate,
                binding.textInputLayoutWeather1,
                binding.textInputLayoutWeather2,
                binding.textInputLayoutCondition,
                binding.textInputLayoutTitle,
                binding.includeItem1.textInputLayoutItemTitle,
                binding.includeItem1.textInputLayoutItemComment,
                binding.includeItem2.textInputLayoutItemTitle,
                binding.includeItem2.textInputLayoutItemComment,
                binding.includeItem3.textInputLayoutItemTitle,
                binding.includeItem3.textInputLayoutItemComment,
                binding.includeItem4.textInputLayoutItemTitle,
                binding.includeItem4.textInputLayoutItemComment,
                binding.includeItem5.textInputLayoutItemTitle,
                binding.includeItem5.textInputLayoutItemComment
        );
    }

    private boolean fetchWeatherInformation(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }

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
                diaryEditViewModel.fetchWeatherInformation(
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

    public void showDiaryShowFragment(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        boolean isStartDiaryFragment =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getIsStartDiaryFragment();
        NavDirections action;
        // 循環型画面遷移を成立させるためにPopup対象Fragmentが異なるactionを切り替える。
        if (isStartDiaryFragment) {
            action = DiaryEditFragmentDirections
                    .actionDiaryEditFragmentToDiaryShowFragmentPattern2(date);
        } else {
            action = DiaryEditFragmentDirections
                    .actionDiaryEditFragmentToDiaryShowFragmentPattern1(date);
        }
        navController.navigate(action);
    }

    private void showDiaryItemTitleEditFragment(int inputItemNumber, String inputItemTitle) {
        if (inputItemNumber < 1) throw new IllegalArgumentException();
        if (inputItemNumber > DiaryLiveData.MAX_ITEMS) throw new IllegalArgumentException();
        Objects.requireNonNull(inputItemTitle);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToSelectItemTitleFragment(inputItemNumber, inputItemTitle);
        navController.navigate(action);
    }

    private void showDatePickerDialog(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date);
        navController.navigate(action);
    }

    public void showUpdateExistingDiaryDialog(LocalDate date, int updateType) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (updateType < 0) {
            throw new IllegalArgumentException();
        }
        if (updateType > 1) {
            throw new IllegalArgumentException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToUpdateExistingDiaryDialog(date, updateType);
        navController.navigate(action);
    }



    private void showLoadingExistingDiaryDialog(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToLoadExistingDiaryDialog(date);
        navController.navigate(action);
    }

    private void showDiaryItemDeleteConfirmationDiaryDialog(int itemNumber) {
        // TODO:int -> objectに変更してnullチェックにする
        if (itemNumber < 1) {
            throw new IllegalArgumentException();
        }
        if (itemNumber > 5) {
            throw new IllegalArgumentException();
        }

        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryItemDeleteConfirmationDialog(itemNumber);
        navController.navigate(action);
    }

    private void showWeatherInformationDialog(LocalDate date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        // 今日の日付以降は天気情報を取得できないためダイアログ表示不要
        LocalDate currentDate = LocalDate.now();
        if (date.isAfter(currentDate)) {
            return;
        }

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToWeatherInformationDialog(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        diaryEditViewModel.triggerAppErrorBufferListObserver();
        settingsViewModel.triggerAppErrorBufferListObserver();
    }

    private void hideKeyboard(View view) {
        // キーボードを隠す。
        KeyboardInitializer keyboardInitializer =
                new KeyboardInitializer(requireActivity());
        keyboardInitializer.hide(view);
        onResume();
    }

    @Override
    public void onResume() {
        super.onResume();

        // HACK:DiaryItemTitleEditFragmentから本Fragmentへ画面遷移(戻る)した時、
        //      スピナーのアダプターが選択中アイテムのみで構成されたアダプターに更新されてしまうので
        //      onResume()メソッドにて再度アダプターを設定して対策。
        //      (Weather2はWeather1のObserver内で設定している為不要)
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter(/*themeColor*/);
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter);
        ArrayAdapter<String> conditionArrayAdapter = createConditionSpinnerAdapter(/*themeColor*/);
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter);
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
