package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.databinding.ViewDataBinding;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

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
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.ui.TestDiariesSaver;
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryEditFragment extends BaseFragment {

    // View関係
    private FragmentDiaryEditBinding binding;
    private boolean isDeletingItemTransition = false;
    private ArrayAdapter<String> weather2ArrayAdapter;

    // ViewModel
    private DiaryEditViewModel diaryEditViewModel;
    private SettingsViewModel settingsViewModel;


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
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentDiaryEditBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryEditViewModel(diaryEditViewModel);
        return binding;
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
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        MutableLiveData<String> newItemTitleLiveData =
                savedStateHandle.getLiveData(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
        newItemTitleLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String string) {
                // MEMO:結果がない場合もあるので"return"で返す。
                if (string == null) return;

                Integer itemNumber =
                        savedStateHandle.get(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                Objects.requireNonNull(itemNumber);

                diaryEditViewModel.updateItemTitle(itemNumber, string);

                savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
                savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
            }
        });
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDatePickerDialogResult();
        receiveLoadExistingDiaryDialogResult();
        receiveUpdateExistingDiaryDialogResult();
        receiveDeleteConfirmDialogResult();
        receiveWeatherInformationDialogResult();
        retryErrorDialogShow();
        clearFocusAllEditText();
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
        savedStateHandle.remove(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
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
    private void receiveDatePickerDialogResult() {
        LocalDate selectedDate = receiveResulFromDialog(DatePickerDialogFragment.KEY_SELECTED_DATE);
        if (selectedDate == null) return;

        diaryEditViewModel.updateDate(selectedDate);
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private void receiveLoadExistingDiaryDialogResult() {
        Integer selectedButton = receiveResulFromDialog(LoadExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;

        LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
        Objects.requireNonNull(date);

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            diaryEditViewModel.initialize();
            diaryEditViewModel.prepareDiary(date, true);
        } else {
            if (!diaryEditViewModel.isNewDiaryDefaultStatus()) {
                fetchWeatherInformation(date, true);
            }
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveUpdateExistingDiaryDialogResult() {
        Integer selectedButton = receiveResulFromDialog(UpdateExistingDiaryDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        boolean isSuccessful = diaryEditViewModel.saveDiary();
        if (isSuccessful) {
            LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
            Objects.requireNonNull(date);
            showDiaryShowFragment(date);
        }
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private void receiveDeleteConfirmDialogResult() {
        Integer deleteItemNumber =
                receiveResulFromDialog(DiaryItemDeleteConfirmationDialogFragment.KEY_DELETE_ITEM_NUMBER);
        if (deleteItemNumber == null) return;

        Integer numVisibleItems = diaryEditViewModel.getNumVisibleItemsLiveData().getValue();
        Objects.requireNonNull(numVisibleItems);

        if (deleteItemNumber == 1 && numVisibleItems.equals(deleteItemNumber)) {
            diaryEditViewModel.deleteItem(deleteItemNumber);
        } else {
            isDeletingItemTransition = true;
            hideItem(deleteItemNumber, false);
        }
    }

    private void receiveWeatherInformationDialogResult() {
        // 天気情報読込ダイアログフラグメントから結果受取
        Integer selectedButton =
                receiveResulFromDialog(WeatherInformationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        LocalDate loadDiaryDate = diaryEditViewModel.getDateLiveData().getValue();
        Objects.requireNonNull(loadDiaryDate);
        GeoCoordinates geoCoordinates = settingsViewModel.getGeoCoordinatesLiveData().getValue();
        Objects.requireNonNull(geoCoordinates);
        diaryEditViewModel.fetchWeatherInformation(loadDiaryDate, geoCoordinates);
    }

    private void setUpDiaryData() {
        // 画面表示データ準備
        boolean isLoadingExistingDiary =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getIsLoadingDiary();
        LocalDate diaryDate =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getEditDiaryDate();
        if (!diaryEditViewModel.getHasPreparedDiary()) {
            diaryEditViewModel.prepareDiary(diaryDate, isLoadingExistingDiary);
            if (!isLoadingExistingDiary) fetchWeatherInformation(diaryDate,false);
        }
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Objects.requireNonNull(v);

                        navController.navigateUp();
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Objects.requireNonNull(item);

                        //日記保存(日記表示フラグメント起動)。
                        if (item.getItemId() == R.id.diaryEditToolbarOptionSaveDiary) {
                            LocalDate savingDate = diaryEditViewModel.getDateLiveData().getValue();
                            Objects.requireNonNull(savingDate);
                            if (diaryEditViewModel.shouldShowUpdateConfirmationDialog()) {
                                showUpdateExistingDiaryDialog(savingDate);
                            } else {
                                boolean isSuccessful = diaryEditViewModel.saveDiary();
                                if (isSuccessful) showDiaryShowFragment(savingDate);
                            }
                            return true;
                        } else if (item.getItemId() == R.id.diaryEditToolbarOptionDeleteDiary) {
                            boolean isSuccessful = diaryEditViewModel.deleteDiary();
                            if (isSuccessful) navController.navigateUp();
                        }
                        return false;
                    }
                });

        diaryEditViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(@Nullable LocalDate date) {
                        String title;
                        boolean enabledDelete;
                        if (date == null) {
                            title = getString(R.string.fragment_diary_edit_toolbar_title_create_new);
                            enabledDelete = false;
                        } else {
                            title = getString(R.string.fragment_diary_edit_toolbar_title_edit);
                            enabledDelete = true;
                        }
                        binding.materialToolbarTopAppBar.setTitle(title);

                        Menu menu = binding.materialToolbarTopAppBar.getMenu();
                        Objects.requireNonNull(menu);
                        MenuItem deleteMenuItem = menu.findItem(R.id.diaryEditToolbarOptionDeleteDiary);
                        Objects.requireNonNull(deleteMenuItem);
                        deleteMenuItem.setEnabled(enabledDelete);
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
                Objects.requireNonNull(event);
                if (event.getAction() != MotionEvent.ACTION_UP) return false;

                hideKeyboard(v);
                LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
                Objects.requireNonNull(date);
                showDatePickerDialog(date);
                return false;
            }
        });

        diaryEditViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new DateObserver());
    }

    private class DateObserver implements Observer<LocalDate> {

        @Override
        public void onChanged(@Nullable LocalDate date) {
            if (date == null) return;

            DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
            binding.textInputEditTextDate.setText(dateTimeStringConverter.toStringDate(date));
            Log.d("DiaryEditInputDate", "currentDate:" + date);
            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();
            Log.d("DiaryEditInputDate", "loadedDate:" + loadedDate);
            LocalDate previousDate = diaryEditViewModel.getPreviousDateLiveData().getValue();
            Log.d("DiaryEditInputDate", "previousDate:" + previousDate);
            boolean shouldShowDialog = shouldShowLoadingExistingDiaryDialog(date);
            if (shouldShowDialog) {
                showLoadingExistingDiaryDialog(date);
            } else {
                // 読込確認Dialog表示時は、確認後下記処理を行う。
                if (requestsFetchingWeatherInformation(date)) {
                    fetchWeatherInformation(date, true);
                }
            }
        }

        private boolean shouldShowLoadingExistingDiaryDialog(LocalDate changedDate) {
            Objects.requireNonNull(changedDate);

            if (diaryEditViewModel.isNewDiaryDefaultStatus()) return diaryEditViewModel.hasDiary(changedDate);

            LocalDate previousDate = diaryEditViewModel.getPreviousDateLiveData().getValue();
            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();

            if (changedDate.equals(previousDate)) return false;
            if (changedDate.equals(loadedDate)) return false;
            return diaryEditViewModel.hasDiary(changedDate);
        }

        private boolean requestsFetchingWeatherInformation(LocalDate date) {
            Objects.requireNonNull(date);

            LocalDate previousDate = diaryEditViewModel.getPreviousDateLiveData().getValue();
            if (previousDate == null) return false;
            return !date.equals(previousDate);
        }
    }

    // 天気入力欄。
    private void setUpWeatherInputField() {
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter();
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter);
        weather2ArrayAdapter = createWeatherSpinnerAdapter();
        binding.autoCompleteTextWeather2.setAdapter(weather2ArrayAdapter);

        binding.autoCompleteTextWeather1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Objects.requireNonNull(parent);
                Objects.requireNonNull(view);

                ListAdapter listAdapter = binding.autoCompleteTextWeather1.getAdapter();
                Objects.requireNonNull(listAdapter);
                ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
                String strWeather = (String) arrayAdapter.getItem(position);
                Objects.requireNonNull(strWeather);
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
                        Objects.requireNonNull(weather);

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
                Objects.requireNonNull(parent);
                Objects.requireNonNull(view);

                ListAdapter listAdapter = binding.autoCompleteTextWeather2.getAdapter();
                ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
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
                        Objects.requireNonNull(weather);

                        String strWeather = weather.toString(requireContext());
                        binding.autoCompleteTextWeather2.setText(strWeather, false);
                    }
                });
    }

    @NonNull
    private ArrayAdapter<String> createWeatherSpinnerAdapter(@Nullable Weathers... excludedWeathers) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        int themeResId = themeColor.getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireContext(), themeResId);

        List<String> weatherItemList = new ArrayList<>();
        Arrays.stream(Weathers.values()).forEach(x -> {
            boolean isIncluded = !isExcludedWeather(x, excludedWeathers);
            if (isIncluded) weatherItemList.add(x.toString(requireContext()));
        });

        return new ArrayAdapter<>(contextWithTheme, R.layout.layout_drop_down_list_item, weatherItemList);
    }

    private boolean isExcludedWeather(Weathers weather, @Nullable Weathers... excludedWeathers) {
        if (excludedWeathers == null) return false;
        for(Weathers excludedWeather: excludedWeathers) {
            if (weather == excludedWeather) return true;
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
                Objects.requireNonNull(parent);
                Objects.requireNonNull(view);

                ListAdapter listAdapter = binding.autoCompleteTextCondition.getAdapter();
                ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
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
                        Objects.requireNonNull(condition);

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
        Arrays.stream(Conditions.values())
                .forEach(x -> conditonItemList.add(x.toString(requireContext())));

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
                    Objects.requireNonNull(event);
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
                Objects.requireNonNull(v);

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
                    Objects.requireNonNull(v);

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
                    Objects.requireNonNull(motionLayout);

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
                        .observe(getViewLifecycleOwner(), new NumVisibleItemsObserver());
    }

    @NonNull
    private MotionLayout selectItemMotionLayout(int itemNumber) {
        switch (itemNumber) {
            case 1:
                return binding.includeItem1.motionLayoutDiaryEditItem;
            case 2:
                return binding.includeItem2.motionLayoutDiaryEditItem;
            case 3:
                return binding.includeItem3.motionLayoutDiaryEditItem;
            case 4:
                return binding.includeItem4.motionLayoutDiaryEditItem;
            case 5:
                return binding.includeItem5.motionLayoutDiaryEditItem;
            default:
                throw new IllegalArgumentException();
        }
    }

    private class NumVisibleItemsObserver implements Observer<Integer> {

        @Override
        public void onChanged(Integer integer) {
            Objects.requireNonNull(integer);

            // 項目欄追加ボタン表示切替
            // TODO:使用不可時はボタンをぼかすように変更する。
            if (integer == DiaryLiveData.MAX_ITEMS) {
                binding.imageButtonAddItem.setVisibility(View.INVISIBLE);
            }

            setUpItemsLayout(integer);
        }

        private void setUpItemsLayout(Integer numItems) {
            Objects.requireNonNull(numItems);
            if (numItems < 1 || numItems > DiaryLiveData.MAX_ITEMS) throw new IllegalArgumentException();

            // MEMO:LifeCycleがResumedの時のみ項目欄のモーション追加処理を行う。
            //      削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
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
    }

    private void hideItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (isJump) {
            itemMotionLayout
                    .jumpToState(R.id.motion_scene_edit_diary_item_hided_state);
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_hided_state);
        }
    }

    private void showItem(int itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (isJump) {
            itemMotionLayout
                    .jumpToState(R.id.motion_scene_edit_diary_item_showed_state);
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_showed_state);
            binding.nestedScrollFullScreen
                    .smoothScrollBy(
                            0,
                            binding.includeItem1.linerLayoutDiaryEditItem.getHeight(),
                            1400
                    );
        }
    }

    private int countShowedItems() {
        int numShowedItems = 0;
        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            MotionLayout motionLayout = selectItemMotionLayout(itemNumber);
            if (motionLayout.getCurrentState() != R.id.motion_scene_edit_diary_item_showed_state) {
                continue;
            }
            numShowedItems++;
        }
        return numShowedItems;
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

        /*binding.includeItem1.textInputEditTextItemComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int scrollAmount = v.get
                    binding.nestedScrollFullScreen.smoothScrollBy(0, v.getHeight());
                    binding.nestedScrollFullScreen.scroll
                }
            }
        });*/
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

    @NonNull
    @Unmodifiable
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

    private void fetchWeatherInformation(LocalDate date, boolean requestsShowingDialog) {
        Objects.requireNonNull(date);

        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        boolean isChecked = settingsViewModel.isCheckedWeatherInfoAcquisitionSetting();
        if (!isChecked) return;

        boolean hasUpdatedLocation = settingsViewModel.hasUpdatedGeoCoordinates();
        if (!hasUpdatedLocation) return;

        // 本フラグメント起動時のみダイアログなしで天気情報取得
        if (requestsShowingDialog) {
            showWeatherInformationDialog(date);
        } else {
            GeoCoordinates geoCoordinates = settingsViewModel.getGeoCoordinatesLiveData().getValue();
            Objects.requireNonNull(geoCoordinates);
            diaryEditViewModel.fetchWeatherInformation(date, geoCoordinates);
        }
    }

    public void showDiaryShowFragment(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

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
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryEditFragmentDirections.actionDiaryEditFragmentToDatePickerDialog(date);
        navController.navigate(action);
    }

    public void showUpdateExistingDiaryDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToUpdateExistingDiaryDialog(date);
        navController.navigate(action);
    }



    private void showLoadingExistingDiaryDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

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

        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryItemDeleteConfirmationDialog(itemNumber);
        navController.navigate(action);
    }

    private void showWeatherInformationDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        // 今日の日付以降は天気情報を取得できないためダイアログ表示不要
        diaryEditViewModel.canFetchWeatherInformation(date);

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

    @SuppressLint("UseRequireInsteadOfGet")
    private void hideKeyboard(View view) {
        Objects.requireNonNull(view);

        KeyboardInitializer keyboardInitializer = new KeyboardInitializer(requireActivity());
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
        ArrayAdapter<String> weatherArrayAdapter = createWeatherSpinnerAdapter();
        binding.autoCompleteTextWeather1.setAdapter(weatherArrayAdapter);
        ArrayAdapter<String> conditionArrayAdapter = createConditionSpinnerAdapter();
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter);
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
