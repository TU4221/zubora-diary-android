package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavDirections;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryShowFragment extends BaseFragment {

    // View関係
    private FragmentDiaryShowBinding binding;// 項目入力欄最大数

    // Navigation関係
    private static final String fromClassName = "From" + DiaryShowFragment.class.getName();
    public static final String KEY_SHOWED_DIARY_DATE = "ShowedDiaryDate" + fromClassName;

    // ViewModel
    private DiaryShowViewModel diaryShowViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addOnBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backFragment(false);
            }
        });
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(this);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);

        ViewModelProvider activityScopeProvider = new ViewModelProvider(requireActivity());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
        binding = FragmentDiaryShowBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDiaryData();
        setUpToolBar();
        setUpWeatherLayout();
        setUpConditionLayout();
        setUpItemLayout();
        setUpLogLayout();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveDeleteConfirmationDialogResult();
        retryOtherErrorDialogShow();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpOtherErrorMessageDialog() {
        diaryShowViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryShowViewModel));
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private void receiveDeleteConfirmationDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(DiaryDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        diaryShowViewModel.deleteDiary();
        backFragment(true);
    }

    // 画面表示データ準備
    private void setUpDiaryData() {
        diaryShowViewModel.initialize();
        LocalDate diaryDate = DiaryShowFragmentArgs.fromBundle(requireArguments()).getDate();

        // 日記編集Fragmentで日記を削除して日記表示Fragmentに戻って来た時は更に一つ前のFragmentへ戻る。
        if (!diaryShowViewModel.existsSavedDiary(diaryDate)) {
            navController.navigateUp();
            return;
        }

        diaryShowViewModel.loadSavedDiary(diaryDate);
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Objects.requireNonNull(v);

                        backFragment(true);
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Objects.requireNonNull(item);

                        // 日記編集フラグメント起動
                        if (item.getItemId() == R.id.diaryShowToolbarOptionEditDiary) {
                            LocalDate editDiaryDate = diaryShowViewModel.getDateLiveData().getValue();
                            showDiaryEdit(editDiaryDate);
                            return true;
                        } else if (item.getItemId() == R.id.diaryShowToolbarOptionDeleteDiary) {
                            LocalDate deleteDiaryDate = diaryShowViewModel.getDateLiveData().getValue();
                            showDiaryDeleteConfirmationDialog(deleteDiaryDate);
                            return true;
                        }
                        return false;
                    }
                });

        diaryShowViewModel.getDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate date) {
                        // MEMO:DiaryViewModelを初期化するとDiaryDateにnullが代入されるため、下記"return"を処理。
                        if (date == null) return;

                        DateTimeStringConverter converter = new DateTimeStringConverter();
                        String stringDate = converter.toYearMonthDayWeek(date);
                        binding.materialToolbarTopAppBar.setTitle(stringDate);
                    }
                });
    }

    // 天気表示欄設定
    private void setUpWeatherLayout() {
        diaryShowViewModel.getWeather1LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Weather1Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeather1Selected
                        )
                );

        diaryShowViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Weather2Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeatherSlush,
                                binding.includeDiaryShow.textWeather2Selected
                        )
                );
    }

    public static class Weather1Observer implements Observer<Weather> {
        private final Context context;
        private final TextView textWeather;

        public Weather1Observer(Context context, TextView textWeather) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(textWeather);

            this.context = context;
            this.textWeather = textWeather;
        }

        @Override
        public void onChanged(Weather weather) {
            Objects.requireNonNull(weather);

            textWeather.setText(weather.toString(context));
        }
    }

    public static class Weather2Observer implements Observer<Weather> {
        private final Context context;
        private final TextView slush;
        private final TextView textWeather;

        public Weather2Observer(Context context, TextView slush, TextView textWeather) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(slush);
            Objects.requireNonNull(textWeather);

            this.context = context;
            this.slush = slush;
            this.textWeather = textWeather;
        }

        @Override
        public void onChanged(Weather weather) {
            Objects.requireNonNull(weather);

            if (weather == Weather.UNKNOWN) {
                slush.setVisibility(View.GONE);
                textWeather.setVisibility(View.GONE);
            } else {
                slush.setVisibility(View.VISIBLE);
                textWeather.setVisibility(View.VISIBLE);
            }
            textWeather.setText(weather.toString(context));
        }
    }

    private void setUpConditionLayout() {
        diaryShowViewModel.getConditionLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new ConditionObserver(
                                requireContext(),
                                binding.includeDiaryShow.textConditionSelected
                        )
                );
    }

    public static class ConditionObserver implements Observer<Condition> {
        private final Context context;
        private final TextView textCondition;

        public ConditionObserver(Context context, TextView textCondition) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(textCondition);

            this.context = context;
            this.textCondition = textCondition;
        }

        @Override
        public void onChanged(Condition condition) {
            Objects.requireNonNull(condition);

            textCondition.setText(condition.toString(context));
        }
    }

    private void setUpItemLayout() {
        View[] itemLayouts = new View[ItemNumber.MAX_NUMBER];
        itemLayouts[0] = binding.includeDiaryShow.includeItem1.linerLayoutDiaryShowItem;
        itemLayouts[1] = binding.includeDiaryShow.includeItem2.linerLayoutDiaryShowItem;
        itemLayouts[2] = binding.includeDiaryShow.includeItem3.linerLayoutDiaryShowItem;
        itemLayouts[3] = binding.includeDiaryShow.includeItem4.linerLayoutDiaryShowItem;
        itemLayouts[4] = binding.includeDiaryShow.includeItem5.linerLayoutDiaryShowItem;
        diaryShowViewModel.getNumVisibleItemsLiveData()
                .observe(getViewLifecycleOwner(), new NumVisibleItemsObserver(itemLayouts));
    }

    public static class NumVisibleItemsObserver implements Observer<Integer> {
        private final View[] itemLayouts;

        public NumVisibleItemsObserver(View[] itemLayouts) {
            Objects.requireNonNull(itemLayouts);
            Arrays.stream(itemLayouts).forEach(Objects::requireNonNull);

            this.itemLayouts = itemLayouts;
        }

        @Override
        public void onChanged(Integer integer) {
            Objects.requireNonNull(integer);
            if (integer < ItemNumber.MIN_NUMBER || integer > ItemNumber.MAX_NUMBER) {
                throw new IllegalArgumentException();
            }

            for (int i = ItemNumber.MIN_NUMBER; i <= ItemNumber.MAX_NUMBER; i++) {
                int itemArrayNumber = i - 1;
                if (i <= integer) {
                    itemLayouts[itemArrayNumber].setVisibility(View.VISIBLE);
                } else {
                    itemLayouts[itemArrayNumber].setVisibility(View.GONE);
                }
            }
        }
    }

    private void setUpLogLayout() {
        diaryShowViewModel.getLogLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new LogObserver(binding.includeDiaryShow.textLogValue)
                );
    }

    public static class LogObserver implements Observer<LocalDateTime> {
        private final TextView textLog;

        public LogObserver(TextView textLog) {
            Objects.requireNonNull(textLog);

            this.textLog = textLog;
        }

        @Override
        public void onChanged(LocalDateTime localDateTime) {
            // MEMO:DiaryViewModelを初期化するとDiaryLogにnullが代入されるため、下記"return"を処理。
            if (localDateTime == null) return;

            DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
            String strDate = dateTimeStringConverter.toYearMonthDayWeekHourMinuteSeconds(localDateTime);
            textLog.setText(strDate);
        }
    }

    private void showDiaryEdit(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryShowFragmentDirections
                        .actionNavigationDiaryShowFragmentToDiaryEditFragment(
                                false,
                                true,
                                date
                        );
        navController.navigate(action);
    }

    private void showDiaryDeleteConfirmationDialog(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                DiaryShowFragmentDirections.
                        actionDiaryShowFragmentToDiaryDeleteConfirmationDialog(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                DiaryShowFragmentDirections
                        .actionDiaryShowFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherErrorDialogShow() {
        diaryShowViewModel.triggerAppErrorBufferListObserver();
    }

    // 一つ前のフラグメントを表示
    // MEMO:ツールバーの戻るボタンと端末の戻るボタンを区別している。
    //      ツールバーの戻るボタン:アプリ内でのみ戻る
    //      端末の戻るボタン:端末内で戻る(アプリ外から本アプリを起動した場合起動もとへ戻る)
    private void backFragment(boolean requestsNavigateUp) {
        NavBackStackEntry navBackStackEntry = navController.getPreviousBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);

        int destinationId = navBackStackEntry.getDestination().getId();
        if (destinationId == R.id.navigation_calendar_fragment) {
            SavedStateHandle savedStateHandle =
                    navController.getPreviousBackStackEntry().getSavedStateHandle();
            LocalDate showedDiaryLocalDate = diaryShowViewModel.getDateLiveData().getValue();
            savedStateHandle.set(KEY_SHOWED_DIARY_DATE, showedDiaryLocalDate);
        }

        if (requestsNavigateUp) {
            navController.navigateUp();
        } else {
            navController.popBackStack();
        }
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
