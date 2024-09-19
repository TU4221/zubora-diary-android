package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private final List<View> noKeyboardViews = new ArrayList<>();
    ArrayAdapter<String> weather2ArrayAdapter;

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
        binding = FragmentDiaryEditBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryEditViewModel(diaryEditViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDiaryData();
        setUpToolBar();
        setUpNoKeyBoardViews();
        setUpDateInputField();
        setUpTitleInputField();
        setUpItemInputField();
        setUpPictureInputField();

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
        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(getViewLifecycleOwner(), new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(ThemeColor themeColor) {
                        if (themeColor == null) {
                            return;
                        }

                        setUpWeatherInputField(themeColor);
                        setUpConditionInputField(themeColor);

                        DiaryThemeColorSwitcher switcher =
                                new DiaryThemeColorSwitcher(requireContext(), themeColor);

                        switcher.switchToolbarColor(binding.materialToolbarTopAppBar);

                        ColorSwitchingViewList<TextView> textViewList =
                                new ColorSwitchingViewList<>(
                                        binding.editTextDate,
                                        binding.editTextDate,
                                        binding.textWeather,
                                        binding.textWeatherSlush,
                                        binding.textCondition,
                                        binding.textTitle,
                                        binding.editTextTitle,
                                        binding.textTitleLength,
                                        binding.includeItem1.textItemNumber,
                                        binding.includeItem1.editTextItemTitle,
                                        binding.includeItem1.editTextItemComment,
                                        binding.includeItem1.textItemCommentLength,
                                        binding.includeItem2.textItemNumber,
                                        binding.includeItem2.editTextItemTitle,
                                        binding.includeItem2.editTextItemComment,
                                        binding.includeItem2.textItemCommentLength,
                                        binding.includeItem3.textItemNumber,
                                        binding.includeItem3.editTextItemTitle,
                                        binding.includeItem3.editTextItemComment,
                                        binding.includeItem3.textItemCommentLength,
                                        binding.includeItem4.textItemNumber,
                                        binding.includeItem4.editTextItemTitle,
                                        binding.includeItem4.editTextItemComment,
                                        binding.includeItem4.textItemCommentLength,
                                        binding.includeItem5.textItemNumber,
                                        binding.includeItem5.editTextItemTitle,
                                        binding.includeItem5.editTextItemComment,
                                        binding.includeItem5.textItemCommentLength
                                );
                        switcher.switchTextColorOnBackground(textViewList);

                        ColorSwitchingViewList<ImageButton> imageButtonList =
                                new ColorSwitchingViewList<>(
                                        binding.includeItem1.imageButtonItemDelete,
                                        binding.includeItem2.imageButtonItemDelete,
                                        binding.includeItem3.imageButtonItemDelete,
                                        binding.includeItem4.imageButtonItemDelete,
                                        binding.includeItem5.imageButtonItemDelete,
                                        binding.imageButtonAddItem
                                );
                        switcher.switchImageButtonColor(imageButtonList);

                        switcher.switchAttachedPictureImageViewColor(binding.imageAttach);
                    }
                });
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        MutableLiveData<String> newItemTitleLiveData =
                savedStateHandle.getLiveData(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
        newItemTitleLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                Integer itemNumber =
                        savedStateHandle.get(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                if (itemNumber != null) {
                    diaryEditViewModel.updateItemTitle(itemNumber, string);
                }
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
        // TODO:Observerへ移行
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
                LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
                showDatePickerDialog(date);
            }
        });

        diaryEditViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
            @Override
            public void onChanged(LocalDate date) {
                if (date == null) {
                    return;
                }
                DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
                binding.editTextDate.setText(dateTimeStringConverter.toStringDate(date));
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
    private void setUpWeatherInputField(ThemeColor themeColor) {
        // TODO:下記MEMOの意味が理解できないので後で確認ご文章を修正する
        // MEMO:下記 onItemSelected は DataBinding を使用して ViewModel 内にメソッドを用意していたが、
        //      画面作成処理時に onItemSelected が処理される為、初期値設定する為の setSelection メソッドの処理タイミングの兼合いで、
        //      DataBinding での使用を取りやめ、ここにまとめて記載することにした。
        //      他スピナーも同様。
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter(themeColor);
        binding.spinnerWeather1.setAdapter(weatherArrayAdapter);
        weather2ArrayAdapter = createWeatherSpinnerAdapter(themeColor);
        binding.spinnerWeather2.setAdapter(weather2ArrayAdapter);

        // TODO:天気情報取得が同期処理なら不要
        binding.spinnerWeather1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    diaryEditViewModel.cancelWeatherSelectionPreparation();
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
                        diaryEditViewModel.updateWeather1(weather);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        diaryEditViewModel.getWeather1LiveData()
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
                            weather2ArrayAdapter = createWeatherSpinnerAdapter(themeColor, weather);
                            binding.spinnerWeather2.setAdapter(weather2ArrayAdapter);
                            binding.spinnerWeather2.setEnabled(true);
                            // HACK:AdapterをセットするとSpinnerの選択アイテムがデフォルト値になるため下記対応。
                            diaryEditViewModel.updateWeather2(diaryEditViewModel.getWeather2LiveData().getValue());
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
                        diaryEditViewModel.updateWeather2(weather);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });

        diaryEditViewModel.getWeather2LiveData()
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

    private ArrayAdapter<String> createWeatherSpinnerAdapter(ThemeColor themeColor, @Nullable Weathers... excludedWeathers) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        List<String> weatherItemList = new ArrayList<>();
        for (Weathers weather: Weathers.values()) {
            if (!isExcludedWeather(weather, excludedWeathers)) {
                weatherItemList.add(weather.toString(requireContext()));
            }
        }
        return new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1 , weatherItemList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // 通常時の文字の色を設定
                TextView textView = (TextView) super.getView(position, convertView, parent);
                DiaryThemeColorSwitcher switcher =
                        new DiaryThemeColorSwitcher(requireContext(), themeColor);
                ColorSwitchingViewList<TextView> textViewList = new ColorSwitchingViewList<>(textView);
                switcher.switchTextColorOnBackground(textViewList);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                DiaryThemeColorSwitcher switcher =
                        new DiaryThemeColorSwitcher(requireContext(), themeColor);
                switcher.switchSpinnerDropDownTextColor(textView);
                return textView;
            }
        };
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
    private void setUpConditionInputField(ThemeColor themeColor) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        List<String> conditonItemList = new ArrayList<>();
        for (Conditions weather: Conditions.values()) {
            conditonItemList.add(weather.toString(requireContext()));
        }
        ArrayAdapter<String> conditionArrayAdapter =
                new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1 , conditonItemList) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        // 通常時の文字の色を設定
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        DiaryThemeColorSwitcher switcher =
                                new DiaryThemeColorSwitcher(requireContext(), themeColor);
                        ColorSwitchingViewList<TextView> textViewList = new ColorSwitchingViewList<>(textView);
                        switcher.switchTextColorOnBackground(textViewList);
                        return textView;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        DiaryThemeColorSwitcher switcher =
                                new DiaryThemeColorSwitcher(requireContext(), themeColor);
                        switcher.switchSpinnerDropDownTextColor(textView);
                        return textView;
                    }
                };
        binding.spinnerCondition.setAdapter(conditionArrayAdapter);

        binding.spinnerCondition
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        ConditionConverter conditionConverter = new ConditionConverter();
                        Conditions condition = conditionConverter.toCondition(position);
                        diaryEditViewModel.updateCondition(condition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // 処理なし
                    }
                });
        diaryEditViewModel.getConditionLiveData()
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
                            diaryEditViewModel.getItemTitleLiveData(inputItemNo).getValue();
                    NavDirections action =
                            DiaryEditFragmentDirections
                                    .actionDiaryEditFragmentToSelectItemTitleFragment(
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

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
