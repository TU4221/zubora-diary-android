package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryEditBinding;
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.ui.TestDiariesSaver;
import com.websarva.wings.android.zuboradiary.ui.TextInputSetup;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit.DiaryItemTitleEditFragment;
import com.websarva.wings.android.zuboradiary.ui.UriPermissionManager;

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

    // Uri関係
    private UriPermissionManager pictureUriPermissionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pictureUriPermissionManager =
                new UriPermissionManager(requireContext()) {
                    @Override
                    public boolean checkUsedUriDoesNotExist(@NonNull Uri uri) {
                        return diaryEditViewModel.checkSavedPicturePathDoesNotExist(uri);
                    }
                };
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryEditViewModel = provider.get(DiaryEditViewModel.class);
        diaryEditViewModel.initialize();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
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
        binding.fabTest.setOnClickListener(v -> {
            Log.d("20240823", "OnClick");
            TestDiariesSaver testDiariesSaver = new TestDiariesSaver(diaryEditViewModel);
            testDiariesSaver.save(28);
        });
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {

        // DiaryItemTitleEditFragmentから編集結果受取
        MutableLiveData<String> newItemTitleLiveData =
                savedStateHandle.getLiveData(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
        newItemTitleLiveData.observe(getViewLifecycleOwner(), string -> {
            // MEMO:結果がない場合もあるので"return"で返す。
            if (string == null) return;

            ItemNumber itemNumber =
                    savedStateHandle.get(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
            Objects.requireNonNull(itemNumber);

            diaryEditViewModel.updateItemTitle(itemNumber, string);

            savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_UPDATE_ITEM_NUMBER);
            savedStateHandle.remove(DiaryItemTitleEditFragment.KEY_NEW_ITEM_TITLE);
        });
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDiaryLoadingDialogResult();
        receiveDiaryUpdateDialogResult();
        receiveDiaryDeleteDialogResult();
        receiveDatePickerDialogResult();
        receiveWeatherInfoFetchDialogResult();
        receiveDiaryItemDeleteDialogResult();
        receiveDiaryPictureDeleteDialogResult();
        retryOtherAppMessageDialogShow();
        clearFocusAllEditText();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_DATE);
        savedStateHandle.remove(WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER);
        savedStateHandle.remove(DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpOtherAppMessageDialog() {
        diaryEditViewModel.getAppMessageBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppMessageBufferListObserver(diaryEditViewModel));
    }

    // 既存日記読込ダイアログフラグメントから結果受取
    private void receiveDiaryLoadingDialogResult() {
        Integer selectedButton = receiveResulFromDialog(DiaryLoadingDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;

        LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
        Objects.requireNonNull(date);

        if (selectedButton == DialogInterface.BUTTON_POSITIVE) {
            diaryEditViewModel.initialize();
            diaryEditViewModel.prepareDiary(date, true);
        } else {
            if (!diaryEditViewModel.isNewDiaryDefaultStatus()) {
                fetchWeatherInfo(date, true);
            }
        }
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveDiaryUpdateDialogResult() {
        Integer selectedButton = receiveResulFromDialog(DiaryUpdateDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        boolean isSuccessful = diaryEditViewModel.saveDiary();
        if (!isSuccessful) return;

        updatePictureUriPermission();
        LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
        Objects.requireNonNull(date);
        showDiaryShowFragment(date);
    }

    // 既存日記上書きダイアログフラグメントから結果受取
    private void receiveDiaryDeleteDialogResult() {
        Integer selectedButton = receiveResulFromDialog(DiaryDeleteDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        boolean isSuccessful = diaryEditViewModel.deleteDiary();
        if (!isSuccessful) return;

        releaseLoadedPictureUriPermission();
        navController.navigateUp();
    }

    // 日付入力ダイアログフラグメントからデータ受取
    private void receiveDatePickerDialogResult() {
        LocalDate selectedDate = receiveResulFromDialog(DatePickerDialogFragment.KEY_SELECTED_DATE);
        if (selectedDate == null) return;

        diaryEditViewModel.updateDate(selectedDate);
    }

    private void receiveWeatherInfoFetchDialogResult() {
        // 天気情報読込ダイアログフラグメントから結果受取
        Integer selectedButton =
                receiveResulFromDialog(WeatherInfoFetchingDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        LocalDate loadDiaryDate = diaryEditViewModel.getDateLiveData().getValue();
        Objects.requireNonNull(loadDiaryDate);
        GeoCoordinates geoCoordinates = settingsViewModel.getGeoCoordinatesLiveData().getValue();
        Objects.requireNonNull(geoCoordinates);
        diaryEditViewModel.fetchWeatherInformation(loadDiaryDate, geoCoordinates);
    }

    // 項目削除確認ダイアログフラグメントから結果受取
    private void receiveDiaryItemDeleteDialogResult() {
        ItemNumber deleteItemNumber =
                receiveResulFromDialog(DiaryItemDeleteDialogFragment.KEY_DELETE_ITEM_NUMBER);
        if (deleteItemNumber == null) return;

        Integer numVisibleItems = diaryEditViewModel.getNumVisibleItemsLiveData().getValue();
        Objects.requireNonNull(numVisibleItems);

        if (deleteItemNumber.getValue() == 1 && numVisibleItems.equals(deleteItemNumber.getValue())) {
            diaryEditViewModel.deleteItem(deleteItemNumber);
        } else {
            isDeletingItemTransition = true;
            hideItem(deleteItemNumber, false);
        }
    }

    private void receiveDiaryPictureDeleteDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(DiaryPictureDeleteDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) return;

        diaryEditViewModel.deletePicturePath();
    }

    private void setUpDiaryData() {
        // 画面表示データ準備
        if (diaryEditViewModel.getHasPreparedDiary()) return;

        LocalDate diaryDate =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getDate();
        Objects.requireNonNull(diaryDate);
        boolean requiresDiaryLoading =
                DiaryEditFragmentArgs.fromBundle(requireArguments()).getRequiresDiaryLoading();
        diaryEditViewModel.prepareDiary(diaryDate, requiresDiaryLoading);
        if (!requiresDiaryLoading) fetchWeatherInfo(diaryDate,false);
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(v -> {
                    Objects.requireNonNull(v);

                    navController.navigateUp();
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(item -> {
                    Objects.requireNonNull(item);
                    LocalDate diaryDate = diaryEditViewModel.getDateLiveData().getValue();
                    Objects.requireNonNull(diaryDate);

                    //日記保存(日記表示フラグメント起動)。
                    if (item.getItemId() == R.id.diaryEditToolbarOptionSaveDiary) {
                        if (diaryEditViewModel.shouldShowUpdateConfirmationDialog()) {
                            showDiaryUpdateDialog(diaryDate);
                        } else {
                            boolean isSuccessful = diaryEditViewModel.saveDiary();
                            if (isSuccessful) {
                                updatePictureUriPermission();
                                showDiaryShowFragment(diaryDate);
                            }
                        }
                        return true;
                    } else if (item.getItemId() == R.id.diaryEditToolbarOptionDeleteDiary) {
                        showDiaryDeleteDialog(diaryDate);
                    }
                    return false;
                });

        diaryEditViewModel.getLoadedDateLiveData()
                .observe(getViewLifecycleOwner(), date -> {
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
                });
    }

    // 日付入力欄設定
    private void setUpDateInputField() {
        binding.textInputEditTextDate.setInputType(EditorInfo.TYPE_NULL); //キーボード非表示設定

        binding.textInputEditTextDate.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            LocalDate date = diaryEditViewModel.getDateLiveData().getValue();
            Objects.requireNonNull(date);
            showDatePickerDialog(date);
        });

        diaryEditViewModel.getDateLiveData().observe(getViewLifecycleOwner(), new DateObserver());
    }

    private class DateObserver implements Observer<LocalDate> {

        @Override
        public void onChanged(@Nullable LocalDate date) {
            if (date == null) return;
            if (diaryEditViewModel.getIsShowingItemTitleEditFragment()) return;

            DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
            binding.textInputEditTextDate.setText(dateTimeStringConverter.toYearMonthDayWeek(date));
            Log.d("DiaryEditInputDate", "currentDate:" + date);
            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();
            Log.d("DiaryEditInputDate", "loadedDate:" + loadedDate);
            LocalDate previousDate = diaryEditViewModel.getPreviousDateLiveData().getValue();
            Log.d("DiaryEditInputDate", "previousDate:" + previousDate);
            if (requiresDiaryLoadingDialogShow(date)) {
                showDiaryLoadingDialog(date);
            } else {
                // 読込確認Dialog表示時は、確認後下記処理を行う。
                if (requiresWeatherInfoFetching(date)) {
                    fetchWeatherInfo(date, true);
                }
            }
        }

        private boolean requiresDiaryLoadingDialogShow(LocalDate changedDate) {
            Objects.requireNonNull(changedDate);

            if (diaryEditViewModel.isNewDiaryDefaultStatus()) return diaryEditViewModel.existsSavedDiary(changedDate);

            LocalDate previousDate = diaryEditViewModel.getPreviousDateLiveData().getValue();
            LocalDate loadedDate = diaryEditViewModel.getLoadedDateLiveData().getValue();

            if (changedDate.equals(previousDate)) return false;
            if (changedDate.equals(loadedDate)) return false;
            return diaryEditViewModel.existsSavedDiary(changedDate);
        }

        private boolean requiresWeatherInfoFetching(LocalDate date) {
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

        binding.autoCompleteTextWeather1.setOnItemClickListener((parent, view, position, id) -> {
            Objects.requireNonNull(parent);
            requireView();

            ListAdapter listAdapter = binding.autoCompleteTextWeather1.getAdapter();
            Objects.requireNonNull(listAdapter);
            ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
            String strWeather = (String) arrayAdapter.getItem(position);
            Objects.requireNonNull(strWeather);
            Weather weather = Weather.of(requireContext(), strWeather);
            diaryEditViewModel.updateWeather1(weather);
            binding.autoCompleteTextWeather1.clearFocus();
        });

        diaryEditViewModel.getWeather1LiveData()
                .observe(getViewLifecycleOwner(), weather -> {
                    Objects.requireNonNull(weather);

                    String strWeather = weather.toString(requireContext());
                    binding.autoCompleteTextWeather1.setText(strWeather, false);

                    // Weather2 Spinner有効無効切替
                    boolean isEnabled = (weather != Weather.UNKNOWN);
                    binding.textInputLayoutWeather2.setEnabled(isEnabled);
                    binding.autoCompleteTextWeather2.setEnabled(isEnabled);

                    if (weather == Weather.UNKNOWN || diaryEditViewModel.isEqualWeathers()) {
                        binding.autoCompleteTextWeather2.setAdapter(weatherArrayAdapter);
                        diaryEditViewModel.updateWeather2(Weather.UNKNOWN);
                    } else {
                        weather2ArrayAdapter = createWeatherSpinnerAdapter(weather);
                        binding.autoCompleteTextWeather2.setAdapter(weather2ArrayAdapter);
                    }
                });

        binding.autoCompleteTextWeather2.setOnItemClickListener((parent, view, position, id) -> {
            Objects.requireNonNull(parent);
            requireView();

            ListAdapter listAdapter = binding.autoCompleteTextWeather2.getAdapter();
            ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
            String strWeather = (String) arrayAdapter.getItem(position);
            Weather weather = Weather.of(requireContext(), strWeather);
            diaryEditViewModel.updateWeather2(weather);
            binding.autoCompleteTextWeather2.clearFocus();
        });

        diaryEditViewModel.getWeather2LiveData()
                .observe(getViewLifecycleOwner(), weather -> {
                    Objects.requireNonNull(weather);

                    String strWeather = weather.toString(requireContext());
                    binding.autoCompleteTextWeather2.setText(strWeather, false);
                });
    }

    @NonNull
    private ArrayAdapter<String> createWeatherSpinnerAdapter(@Nullable Weather... excludedWeathers) {
        int themeResId = requireThemeColor().getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireContext(), themeResId);

        List<String> weatherItemList = new ArrayList<>();
        Arrays.stream(Weather.values()).forEach(x -> {
            boolean isIncluded = !isExcludedWeather(x, excludedWeathers);
            if (isIncluded) weatherItemList.add(x.toString(requireContext()));
        });

        return new ArrayAdapter<>(contextWithTheme, R.layout.layout_drop_down_list_item, weatherItemList);
    }

    private boolean isExcludedWeather(Weather weather, @Nullable Weather... excludedWeathers) {
        if (excludedWeathers == null) return false;
        for(Weather excludedWeather: excludedWeathers) {
            if (weather == excludedWeather) return true;
        }
        return false;
    }

    // 気分入力欄。
    private void setUpConditionInputField() {
        // ドロップダウン設定
        ArrayAdapter<String> conditionArrayAdapter = createConditionSpinnerAdapter();
        binding.autoCompleteTextCondition.setAdapter(conditionArrayAdapter);
        binding.autoCompleteTextCondition.setOnItemClickListener((parent, view, position, id) -> {
            Objects.requireNonNull(parent);
            requireView();

            ListAdapter listAdapter = binding.autoCompleteTextCondition.getAdapter();
            ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) listAdapter;
            String strCondition = (String) arrayAdapter.getItem(position);
            Condition condition = Condition.of(requireContext(), strCondition);
            diaryEditViewModel.updateCondition(condition);
            binding.autoCompleteTextCondition.clearFocus();
        });

        diaryEditViewModel.getConditionLiveData()
                .observe(getViewLifecycleOwner(), condition -> {
                    Objects.requireNonNull(condition);

                    String strCondition = condition.toString(requireContext());
                    binding.autoCompleteTextCondition.setText(strCondition, false);
                });
    }

    @NonNull
    private ArrayAdapter<String> createConditionSpinnerAdapter() {
        int themeResId = requireThemeColor().getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(requireContext(), themeResId);

        List<String> conditonItemList = new ArrayList<>();
        Arrays.stream(Condition.values())
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
        textInputLayoutItemsTitle[0] = binding.includeItem1.textInputLayoutTitle;
        textInputEditTextItemsTitle[0] = binding.includeItem1.textInputEditTextTitle;
        textInputEditTextItemsComment[0] = binding.includeItem1.textInputEditTextComment;
        imageButtonItemsDelete[0] = binding.includeItem1.imageButtonItemDelete;

        textItems[1] = binding.includeItem2.textItemNumber;
        textInputLayoutItemsTitle[1] = binding.includeItem2.textInputLayoutTitle;
        textInputEditTextItemsTitle[1] = binding.includeItem2.textInputEditTextTitle;
        textInputEditTextItemsComment[1] = binding.includeItem2.textInputEditTextComment;
        imageButtonItemsDelete[1] = binding.includeItem2.imageButtonItemDelete;

        textItems[2] = binding.includeItem3.textItemNumber;
        textInputLayoutItemsTitle[2] = binding.includeItem3.textInputLayoutTitle;
        textInputEditTextItemsTitle[2] = binding.includeItem3.textInputEditTextTitle;
        textInputEditTextItemsComment[2] = binding.includeItem3.textInputEditTextComment;
        imageButtonItemsDelete[2] = binding.includeItem3.imageButtonItemDelete;

        textItems[3] = binding.includeItem4.textItemNumber;
        textInputLayoutItemsTitle[3] = binding.includeItem4.textInputLayoutTitle;
        textInputEditTextItemsTitle[3] = binding.includeItem4.textInputEditTextTitle;
        textInputEditTextItemsComment[3] = binding.includeItem4.textInputEditTextComment;
        imageButtonItemsDelete[3] = binding.includeItem4.imageButtonItemDelete;

        textItems[4] = binding.includeItem5.textItemNumber;
        textInputLayoutItemsTitle[4] = binding.includeItem5.textInputLayoutTitle;
        textInputEditTextItemsTitle[4] = binding.includeItem5.textInputEditTextTitle;
        textInputEditTextItemsComment[4] = binding.includeItem5.textInputEditTextComment;
        imageButtonItemsDelete[4] = binding.includeItem5.imageButtonItemDelete;

        // 項目欄設定
        // 項目タイトル入力欄設定
        for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
            ItemNumber inputItemNumber =  new ItemNumber(i);
            int ItemArrayNumber = i - 1;
            textInputEditTextItemsTitle[ItemArrayNumber].setInputType(EditorInfo.TYPE_NULL); //キーボード非表示設定

            textInputEditTextItemsTitle[ItemArrayNumber].setOnClickListener(v -> {
                Objects.requireNonNull(v);

                // 項目タイトル入力フラグメント起動
                String inputItemTitle =
                        diaryEditViewModel.getItemTitleLiveData(inputItemNumber).getValue();
                showDiaryItemTitleEditFragment(inputItemNumber, inputItemTitle);
            });
        }

        // 項目追加ボタン設定
        binding.imageButtonItemAddition.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            binding.imageButtonItemAddition.setEnabled(false);
            diaryEditViewModel.incrementVisibleItemsCount();
        });

        // 項目削除ボタン設定
        for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
            ItemNumber deleteItemNumber = new ItemNumber(i);
            int itemArrayNumber = i - 1;
            imageButtonItemsDelete[itemArrayNumber].setOnClickListener(v -> {
                Objects.requireNonNull(v);

                showDiaryItemDeleteDialog(deleteItemNumber);
            });
        }

        // 項目欄MotionLayout設定
        for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
            ItemNumber itemNumber = new ItemNumber(i);
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

                    // 対象項目欄追加後の処理
                    } else if (currentId == R.id.motion_scene_edit_diary_item_showed_state) {
                        Log.d("MotionLayout", "currentId:showed_state");
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
    private MotionLayout selectItemMotionLayout(ItemNumber itemNumber) {
        switch (itemNumber.getValue()) {
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

            enableItemAdditionButton(integer < DiaryLiveData.MAX_ITEMS);
            setUpItemsLayout(integer);
        }

        private void enableItemAdditionButton(boolean enabled) {
            binding.imageButtonItemAddition.setEnabled(enabled);
            int alphaResId;
            if (enabled) {
                alphaResId = R.dimen.view_enabled_alpha;
            } else {
                alphaResId = R.dimen.view_disabled_alpha;
            }
            float alpha = ResourcesCompat.getFloat(getResources(), alphaResId);
            binding.imageButtonItemAddition.setAlpha(alpha);
        }

        private void setUpItemsLayout(Integer numItems) {
            Objects.requireNonNull(numItems);
            if (numItems < ItemNumber.MIN_NUMBER || numItems > ItemNumber.MAX_NUMBER) {
                throw new IllegalArgumentException();
            }

            // MEMO:LifeCycleがResumedの時のみ項目欄のモーション追加処理を行う。
            //      削除処理はObserverで適切なモーション削除処理を行うのは難しいのでここでは処理せず、削除ダイアログから処理する。
            if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                int numShowedItems = countShowedItems();
                int differenceValue = numItems - numShowedItems;
                if (numItems > numShowedItems && differenceValue == 1) {
                    showItem(new ItemNumber(numItems), false);
                    return;
                }
            }

            for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
                ItemNumber itemNumber = new ItemNumber(i);
                if (itemNumber.getValue() <= numItems) {
                    showItem(itemNumber, true);
                } else {
                    hideItem(itemNumber, true);
                }
            }
        }
    }

    private void hideItem(ItemNumber itemNumber, boolean isJump) {
        MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
        if (isJump) {
            itemMotionLayout
                    .jumpToState(R.id.motion_scene_edit_diary_item_hided_state);
        } else {
            itemMotionLayout.transitionToState(R.id.motion_scene_edit_diary_item_hided_state);
        }
    }

    private void showItem(ItemNumber itemNumber, boolean isJump) {
        Objects.requireNonNull(itemNumber);

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
        for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
            ItemNumber itemNumber = new ItemNumber(i);
            MotionLayout motionLayout = selectItemMotionLayout(itemNumber);
            if (motionLayout.getCurrentState() != R.id.motion_scene_edit_diary_item_showed_state) {
                continue;
            }
            numShowedItems++;
        }
        return numShowedItems;
    }

    private void setUpPictureInputField() {
        binding.imageAttachedPicture.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            requireMainActivity().loadPicturePath();
        });

        diaryEditViewModel.getPicturePathLiveData()
                .observe(getViewLifecycleOwner(), new PicturePathObserver());

        binding.imageButtonAttachedPictureDelete.setOnClickListener(v -> showDiaryPictureDeleteDialog());
    }

    private class PicturePathObserver implements Observer<Uri> {

        @Override
        public void onChanged(Uri uri) {
            DiaryPictureManager diaryPictureManager =
                    new DiaryPictureManager(
                            requireContext(),
                            binding.imageAttachedPicture,
                            requireThemeColor().getOnSurfaceVariantColor(requireContext().getResources())
                    );

            diaryPictureManager.setUpPictureOnDiary(uri);
            enablePictureDeleteButton(uri != null);

        }

        private void enablePictureDeleteButton(boolean enabled) {
            binding.imageButtonAttachedPictureDelete.setEnabled(enabled);
            int alphaResId;
            if (enabled) {
                alphaResId = R.dimen.view_enabled_alpha;
            } else {
                alphaResId = R.dimen.view_disabled_alpha;
            }
            float alpha = ResourcesCompat.getFloat(getResources(), alphaResId);
            binding.imageButtonAttachedPictureDelete.setAlpha(alpha);
        }
    }

    private void updatePictureUriPermission() {
        Uri latestPictureUri = diaryEditViewModel.getPicturePathLiveData().getValue();
        Uri loadedPictureUri = diaryEditViewModel.getLoadedPicturePathLiveData().getValue();

        try {
            if (latestPictureUri == null && loadedPictureUri == null) return;

            if (latestPictureUri != null && loadedPictureUri == null) {
                pictureUriPermissionManager.takePersistablePermission(latestPictureUri);
                return;
            }

            if (latestPictureUri == null) {
                pictureUriPermissionManager.releasePersistablePermission(loadedPictureUri);
                return;
            }

            if (latestPictureUri.equals(loadedPictureUri)) return;

            pictureUriPermissionManager.takePersistablePermission(latestPictureUri);
            pictureUriPermissionManager.releasePersistablePermission(loadedPictureUri);
        } catch (SecurityException e) {
            // 対処できないがアプリを落としたくない為、catchのみ処理する。
        }

    }

    private void releaseLoadedPictureUriPermission() {
        Uri loadedPictureUri = diaryEditViewModel.getLoadedPicturePathLiveData().getValue();
        if (loadedPictureUri == null) return;
        pictureUriPermissionManager.releasePersistablePermission(loadedPictureUri);
    }

    private void setupEditText() {
        TextInputSetup textInputSetup = new TextInputSetup(requireActivity());

        TextInputLayout[] allTextInputLayouts = createAllTextInputLayoutList().toArray(new TextInputLayout[0]);
        textInputSetup.setUpFocusClearOnClickBackground(binding.viewNestedScrollBackground, allTextInputLayouts);

        textInputSetup.setUpKeyboardCloseOnEnter(binding.textInputLayoutTitle);

        TextInputLayout[] scrollableTextInputLayouts = {
                binding.includeItem1.textInputLayoutComment,
                binding.includeItem2.textInputLayoutComment,
                binding.includeItem3.textInputLayoutComment,
                binding.includeItem4.textInputLayoutComment,
                binding.includeItem5.textInputLayoutComment,
        };
        textInputSetup.setUpScrollable(scrollableTextInputLayouts);

        TextInputLayout[] clearableTextInputLayouts = {
                binding.textInputLayoutTitle,
                binding.includeItem1.textInputLayoutTitle,
                binding.includeItem2.textInputLayoutTitle,
                binding.includeItem3.textInputLayoutTitle,
                binding.includeItem4.textInputLayoutTitle,
                binding.includeItem5.textInputLayoutTitle,
        };
        TextInputSetup.ClearButtonSetUpTransitionListener transitionListener =
                textInputSetup.createClearButtonSetupTransitionListener(clearableTextInputLayouts);
        addTransitionListener(transitionListener);

        // TODO:キーボード表示時の自動スクロールを無効化(自動スクロール時toolbarが隠れる為)している為、listenerで代用したいが上手くいかない。
        /*binding.includeItem1.textInputEditTextComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                binding.includeItem1.textInputLayoutTitle,
                binding.includeItem1.textInputLayoutComment,
                binding.includeItem2.textInputLayoutTitle,
                binding.includeItem2.textInputLayoutComment,
                binding.includeItem3.textInputLayoutTitle,
                binding.includeItem3.textInputLayoutComment,
                binding.includeItem4.textInputLayoutTitle,
                binding.includeItem4.textInputLayoutComment,
                binding.includeItem5.textInputLayoutTitle,
                binding.includeItem5.textInputLayoutComment
        );
    }

    private void fetchWeatherInfo(LocalDate date, boolean requestsShowingDialog) {
        Objects.requireNonNull(date);

        // HACK:EditFragment起動時、設定値を参照してから位置情報を取得する為、タイムラグが発生する。
        //      対策として記憶boolean変数を用意し、true時は位置情報取得処理コードにて天気情報も取得する。
        boolean isChecked = settingsViewModel.isCheckedWeatherInfoAcquisitionSetting();
        if (!isChecked) return;

        boolean hasUpdatedLocation = settingsViewModel.hasUpdatedGeoCoordinates();
        if (!hasUpdatedLocation) {
            diaryEditViewModel.addWeatherInfoFetchErrorMessage();
            return;
        }

        // 本フラグメント起動時のみダイアログなしで天気情報取得
        if (requestsShowingDialog) {
            showWeatherInfoFetchingDialog(date);
        } else {
            GeoCoordinates geoCoordinates = settingsViewModel.getGeoCoordinatesLiveData().getValue();
            Objects.requireNonNull(geoCoordinates);
            diaryEditViewModel.fetchWeatherInformation(date, geoCoordinates);
        }
    }

    private void showDiaryShowFragment(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

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

    private void showDiaryItemTitleEditFragment(ItemNumber inputItemNumber, String inputItemTitle) {
        Objects.requireNonNull(inputItemNumber);
        Objects.requireNonNull(inputItemTitle);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToSelectItemTitleFragment(inputItemNumber, inputItemTitle);
        navController.navigate(action);
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(true);
    }

    private void showDiaryLoadingDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryLoadingDialog(date);
        navController.navigate(action);
    }

    private void showDiaryUpdateDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryUpdateDialog(date);
        navController.navigate(action);
    }

    private void showDiaryDeleteDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryDeleteDialog(date);
        navController.navigate(action);
    }

    private void showDatePickerDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDatePickerDialog(date);
        navController.navigate(action);
    }

    private void showWeatherInfoFetchingDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;
        if (!diaryEditViewModel.canFetchWeatherInformation(date)) return;

        // 今日の日付以降は天気情報を取得できないためダイアログ表示不要
        diaryEditViewModel.canFetchWeatherInformation(date);

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToWeatherInfoFetchingDialog(date);
        navController.navigate(action);
    }

    private void showDiaryItemDeleteDialog(ItemNumber itemNumber) {
        Objects.requireNonNull(itemNumber);
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryItemDeleteDialog(itemNumber);
        navController.navigate(action);
    }

    private void showDiaryPictureDeleteDialog() {
        if (isDialogShowing()) return;

        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToDiaryPictureDeleteDialog();
        navController.navigate(action);
    }

    @Override
    protected void navigateAppMessageDialog(@NonNull AppMessage appMessage) {
        NavDirections action =
                DiaryEditFragmentDirections
                        .actionDiaryEditFragmentToAppMessageDialog(appMessage);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherAppMessageDialogShow() {
        diaryEditViewModel.triggerAppMessageBufferListObserver();
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

        // HACK:ItemTitleEditFragmentから戻ってきた時に処理させたく箇所を
        //      変数(DiaryEditViewModel.IsShowingItemTitleEditFragment)で分岐させる。
        diaryEditViewModel.updateIsShowingItemTitleEditFragment(false);
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }

    public void attachPicture(Uri uri) {
        Objects.requireNonNull(uri);

        diaryEditViewModel.updatePicturePath(uri);
    }
}
