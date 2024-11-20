package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.google.common.util.concurrent.FutureCallback;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.CalendarDayBinding;
import com.websarva.wings.android.zuboradiary.databinding.CalendarHeaderBinding;
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment;

import com.kizitonwose.calendar.view.CalendarView;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowViewModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends BaseFragment {

    // View関係
    private FragmentCalendarBinding binding;

    // ViewModel
    private CalendarViewModel calendarViewModel;
    private DiaryShowViewModel diaryShowViewModel; // TODO:diaryViewModelの使用要素をcalendarViewModelに含めるか検討(DiaryFragment修正後)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addOnBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireMainActivity().popBackStackToStartFragment();
            }
        });
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        calendarViewModel = provider.get(CalendarViewModel.class);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
        binding = FragmentCalendarBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpCalendar();
        setUpDiaryShow();
        setUpFloatActionButton();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        MutableLiveData<LocalDate> showedDiaryDateLiveData =
                savedStateHandle.getLiveData(DiaryShowFragment.KEY_SHOWED_DIARY_DATE);
        showedDiaryDateLiveData.observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
            @Override
            public void onChanged(LocalDate localDate) {
                calendarViewModel.updateSelectedDate(localDate);
                savedStateHandle.remove(DiaryShowFragment.KEY_SHOWED_DIARY_DATE);
            }
        });
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        retryOtherErrorDialogShow();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void setUpOtherErrorMessageDialog() {
        calendarViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(calendarViewModel));
        diaryShowViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryShowViewModel));
    }

    private void setUpCalendar() {
        CalendarView calendar = binding.calendar;

        List<DayOfWeek> daysOfWeek = createDayOfWeekList(); // 曜日リスト取得
        configureCalendarBinders(daysOfWeek, requireThemeColor());

        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(60); //現在から過去5年分
        YearMonth endMonth = currentMonth.plusMonths(60); //現在から未来5年分
        calendar.setup(startMonth,endMonth,daysOfWeek.get(0));

        LocalDate selectedDate = calendarViewModel.getSelectedDateLiveData().getValue();
        Objects.requireNonNull(selectedDate);
        calendarViewModel.updateSelectedDate(selectedDate);

        calendarViewModel.getSelectedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate localDate) {
                        Objects.requireNonNull(localDate);

                        binding.calendar.notifyDateChanged(localDate); // 今回選択日付更新
                        scrollCalendar(localDate);
                        updateToolBarDate(localDate);
                        showSelectedDiary(localDate);
                    }
                });

        calendarViewModel.getPreviousSelectedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate localDate) {
                        // MEMO:一度も日付選択をしていない場合はnullが代入されている。
                        if (localDate == null) return;

                        binding.calendar.notifyDateChanged(localDate); // 前回選択日付更新
                    }
                });
    }

    private List<DayOfWeek> createDayOfWeekList() {
        DayOfWeek firstDayOfWeek = settingsViewModel.loadCalendarStartDaySettingValue();

        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        int firstDayOfWeekListPos = firstDayOfWeek.getValue();
        // 開始曜日を先頭に並び替え
        List<DayOfWeek> firstList =
                Arrays.stream(daysOfWeek)
                        .skip(firstDayOfWeekListPos - 1)
                        .collect(Collectors.toList());
        List<DayOfWeek> secondList =
                Arrays.stream(daysOfWeek)
                        .limit(firstDayOfWeekListPos - 1)
                        .collect(Collectors.toList());
        return Stream
                .concat(firstList.stream(), secondList.stream())
                .collect(Collectors.toList());
    }

    // カレンダーBind設定
    private void configureCalendarBinders(List<DayOfWeek> daysOfWeek, ThemeColor themeColor) {
        Objects.requireNonNull(daysOfWeek);
        daysOfWeek.stream().forEach(Objects::requireNonNull);
        Objects.requireNonNull(themeColor);

        binding.calendar.setDayBinder(new CalendarMonthDayBinder(themeColor));
        binding.calendar.setMonthHeaderBinder(new CalendarMonthHeaderFooterBinder(daysOfWeek, themeColor));
    }

    private class CalendarMonthDayBinder implements MonthDayBinder<DayViewContainer> {

        private final ThemeColor themeColor;

        private CalendarMonthDayBinder(ThemeColor themeColor) {
            Objects.requireNonNull(themeColor);

            this.themeColor = themeColor;
        }

        @Override
        public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay calendarDay) {

            TextView textCalendarDay = container.binding.textCalendarDay;
            View viewCalendarDayDot = container.binding.viewCalendarDayDot;

            textCalendarDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Objects.requireNonNull(v);

                    if (calendarDay.getPosition() == DayPosition.MonthDate) {
                        calendarViewModel.updateSelectedDate(calendarDay.getDate());
                    }
                }
            });

            // 数値設定
            String day = String.valueOf(calendarDay.getDate().getDayOfMonth());
            textCalendarDay.setText(day);

            // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
            if (calendarDay.getPosition() == DayPosition.MonthDate) {
                textCalendarDay.setVisibility(View.VISIBLE);
                setUpCalendarDayColor(calendarDay, textCalendarDay, viewCalendarDayDot);
                setUpCalendarDayDotVisibility(calendarDay, viewCalendarDayDot);
            } else {
                textCalendarDay.setVisibility(View.INVISIBLE);
                viewCalendarDayDot.setVisibility(View.INVISIBLE);
            }
        }

        private void setUpCalendarDayColor(
                @NonNull CalendarDay calendarDay, TextView textCalendarDay, View viewCalendarDayDot) {
            Objects.requireNonNull(textCalendarDay);
            Objects.requireNonNull(viewCalendarDayDot);

            CalendarThemeColorSwitcher themeColorSwitcher =
                    new CalendarThemeColorSwitcher(requireContext(), themeColor);

            LocalDate selectedDate = calendarViewModel.getSelectedDateLiveData().getValue();
            boolean isSelectedDay = calendarDay.getDate().isEqual(selectedDate);
            boolean isToday = calendarDay.getDate().isEqual(LocalDate.now());

            if (isSelectedDay) {
                themeColorSwitcher.switchCalendarSelectedDayColor(textCalendarDay, viewCalendarDayDot);
            } else if (isToday) {
                themeColorSwitcher.switchCalendarTodayColor(textCalendarDay, viewCalendarDayDot);
            } else {
                // TODO:祝日判定は手間がかかりそうなので保留
                DayOfWeek dayOfWeek = calendarDay.getDate().getDayOfWeek();
                boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;
                boolean isSunday = dayOfWeek == DayOfWeek.SUNDAY;

                if (isSaturday) {
                    themeColorSwitcher.switchCalendarSaturdayColor(textCalendarDay, viewCalendarDayDot);
                } else if (isSunday) {
                    themeColorSwitcher.switchCalendarSundayColor(textCalendarDay, viewCalendarDayDot);
                } else {
                    themeColorSwitcher.switchCalendarWeekdaysColor(textCalendarDay, viewCalendarDayDot);
                }
            }
        }

        private void setUpCalendarDayDotVisibility(
                @NonNull CalendarDay calendarDay, View viewCalendarDayDot) {
            Objects.requireNonNull(viewCalendarDayDot);

            LocalDate localDate = calendarDay.getDate();
            calendarViewModel.existsSavedDiary(localDate, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        viewCalendarDayDot.setVisibility(View.VISIBLE);
                    } else {
                        viewCalendarDayDot.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    // 例外はViewModelクラス内で例外用リスナーを追加して対応
                    viewCalendarDayDot.setVisibility(View.INVISIBLE);
                }
            });
        }

        @NonNull
        @Override
        public DayViewContainer create(@NonNull View view) {
            return new DayViewContainer(view);
        }
    }

    private class CalendarMonthHeaderFooterBinder implements MonthHeaderFooterBinder<MonthViewContainer> {

        private final List<DayOfWeek> daysOfWeek;
        private final ThemeColor themeColor;

        private CalendarMonthHeaderFooterBinder(List<DayOfWeek> daysOfWeek, ThemeColor themeColor) {
            Objects.requireNonNull(daysOfWeek);
            daysOfWeek.stream().forEach(Objects::requireNonNull);
            Objects.requireNonNull(themeColor);

            this.daysOfWeek = daysOfWeek;
            this.themeColor = themeColor;
        }

        @Override
        public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth calendarMonth) {
            // カレンダーの年月表示設定
            String format = getString(R.string.fragment_calendar_month_header_format);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            String stringYearMonth = calendarMonth.getYearMonth().format(formatter);
            container.binding.textYearMonth.setText(stringYearMonth);

            // カレンダーの曜日設定(未設定アイテムのみ設定)
            LinearLayout linearLayout = container.binding.legendLayout.getRoot();
            if (linearLayout.getTag() != null) return;
            linearLayout.setTag(calendarMonth.getYearMonth());

            // カレンダー曜日表示設定
            int max = linearLayout.getChildCount();
            for (int i = 0; i < max; i++) {
                View childView = linearLayout.getChildAt(i);
                TextView childTextView = (TextView) childView;
                DayOfWeek dayOfWeek = daysOfWeek.get(i);

                childTextView.setText(dayOfWeek.name().substring(0,3));

                setUpDayOfWeekColor(dayOfWeek, childTextView);
            }
        }

        private void setUpDayOfWeekColor(DayOfWeek dayOfWeek, TextView dayOfWeekText) {
            Objects.requireNonNull(dayOfWeek);
            Objects.requireNonNull(dayOfWeekText);

            CalendarThemeColorSwitcher themeColorSwitcher =
                    new CalendarThemeColorSwitcher(requireContext(), themeColor);

            boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;
            boolean isSunday = dayOfWeek == DayOfWeek.SUNDAY;

            if (isSaturday) {
                themeColorSwitcher.switchCalendarDayOfWeekSaturdayColor(dayOfWeekText);
            } else if (isSunday) {
                themeColorSwitcher.switchCalendarDayOfWeekSundayColor(dayOfWeekText);
            } else {
                themeColorSwitcher.switchCalendarDayOfWeekWeekdaysColor(dayOfWeekText);
            }
        }

        @NonNull
        @Override
        public MonthViewContainer create(@NonNull View view) {
            return new MonthViewContainer(view);
        }
    }

    // カレンダー日単位コンテナ
    private static class DayViewContainer extends ViewContainer {

        private final CalendarDayBinding binding;

        private DayViewContainer(View view) {
            super(view);
            binding = CalendarDayBinding.bind(view);
        }
    }

    // カレンダー月単位コンテナ
    private static class MonthViewContainer extends ViewContainer {

        private final CalendarHeaderBinding binding;

        private MonthViewContainer(View view) {
            super(view);
            binding = CalendarHeaderBinding.bind(view);
        }
    }

    // カレンダーを指定した日付へ自動スクロール
    private void scrollCalendar(LocalDate date) {
        Objects.requireNonNull(date);

        YearMonth targetYearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        CalendarMonth currentMonth = binding.calendar.findFirstVisibleMonth();
        if (currentMonth == null) {
            binding.calendar.scrollToMonth(targetYearMonth);
            return;
        }

        YearMonth currentYearMonth = currentMonth.getYearMonth();
        Objects.requireNonNull(currentYearMonth);

        // MEMO:カレンダーが今日の日付月から遠い月を表示していたらsmoothScrollの処理時間が延びるので、
        //      間にScroll処理を入れる。
        if (currentYearMonth.isAfter(targetYearMonth)) {
            YearMonth firstSwitchPoint  = currentYearMonth.minusMonths(3);
            YearMonth secondSwitchPoint  = targetYearMonth.plusMonths(6);
            if (currentYearMonth.isAfter(secondSwitchPoint)) {
                binding.calendar.smoothScrollToMonth(firstSwitchPoint);
                binding.calendar.scrollToMonth(secondSwitchPoint);
            }
        } else {
            YearMonth firstSwitchPoint  = currentYearMonth.plusMonths(3);
            YearMonth secondSwitchPoint  = targetYearMonth.minusMonths(6);
            if (currentYearMonth.isBefore(secondSwitchPoint)) {
                binding.calendar.smoothScrollToMonth(firstSwitchPoint);
                binding.calendar.scrollToMonth(secondSwitchPoint);
            }
        }
        binding.calendar.smoothScrollToMonth(targetYearMonth);
    }

    private void updateToolBarDate(LocalDate date) {
        Objects.requireNonNull(date);

        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String stringDate = dateTimeStringConverter.toYearMonthDayWeek(date);
        binding.materialToolbarTopAppBar.setTitle(stringDate);
    }

    // CalendarViewで選択された日付の日記を表示
    private void showSelectedDiary(LocalDate date) {
        Objects.requireNonNull(date);

        calendarViewModel.existsSavedDiary(date, new DiaryShowFutureCallback(date));
    }

    private class DiaryShowFutureCallback implements FutureCallback<Boolean> {

        private final LocalDate date;

        private DiaryShowFutureCallback(LocalDate date) {
            Objects.requireNonNull(date);

            this.date = date;
        }

        @Override
        public void onSuccess(Boolean result) {
            Objects.requireNonNull(result);

            if (result) {
                showDiary();
            } else {
                closeDiary();
            }
        }

        @Override
        public void onFailure(@NonNull Throwable t) {
            // 例外はViewModelクラス内で例外用リスナーを追加して対応
            closeDiary();
        }

        private void showDiary() {
            diaryShowViewModel.initialize();
            diaryShowViewModel.loadSavedDiary(date);
            binding.linearLayoutDiaryShow.setVisibility(View.VISIBLE);
            binding.textNoDiary.setVisibility(View.GONE);
        }

        private void closeDiary() {
            binding.linearLayoutDiaryShow.setVisibility(View.GONE);
            binding.textNoDiary.setVisibility(View.VISIBLE);
            diaryShowViewModel.initialize();
        }
    }

    private void setUpDiaryShow() {
        diaryShowViewModel.getWeather1LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowFragment.Weather1Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeather1Selected
                        )
                );

        diaryShowViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowFragment.Weather2Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeatherSlush,
                                binding.includeDiaryShow.textWeather2Selected
                        )
                );

        diaryShowViewModel.getConditionLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowFragment.ConditionObserver(
                                requireContext(),
                                binding.includeDiaryShow.textConditionSelected
                        )
                );

        // 項目レイアウト設定
        View[] itemLayouts = new View[DiaryLiveData.MAX_ITEMS];
        itemLayouts[0] = binding.includeDiaryShow.includeItem1.linerLayoutDiaryShowItem;
        itemLayouts[1] = binding.includeDiaryShow.includeItem2.linerLayoutDiaryShowItem;
        itemLayouts[2] = binding.includeDiaryShow.includeItem3.linerLayoutDiaryShowItem;
        itemLayouts[3] = binding.includeDiaryShow.includeItem4.linerLayoutDiaryShowItem;
        itemLayouts[4] = binding.includeDiaryShow.includeItem5.linerLayoutDiaryShowItem;
        diaryShowViewModel.getNumVisibleItemsLiveData()
                .observe(getViewLifecycleOwner(), new DiaryShowFragment.NumVisibleItemsObserver(itemLayouts));

        diaryShowViewModel.getLogLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowFragment.LogObserver(binding.includeDiaryShow.textLogValue)
                );
    }

    private void setUpFloatActionButton() {
        binding.floatActionButtonDiaryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                LocalDate selectedDate = calendarViewModel.getSelectedDateLiveData().getValue();
                showDiaryEditFragment(selectedDate);
            }
        });
    }

    // 選択中ボトムナビゲーションタブを再選択時の処理
    public void processOnReselectNavigationItem() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop();
        } else {
            calendarViewModel.updateSelectedDate(LocalDate.now());
        }
    }

    // 先頭へ自動スクロール
    private void scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0);
    }

    private void showDiaryEditFragment(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                CalendarFragmentDirections
                        .actionNavigationCalendarFragmentToDiaryEditFragment(
                                true, true, date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                CalendarFragmentDirections
                        .actionCalendarFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherErrorDialogShow() {
        calendarViewModel.triggerAppErrorBufferListObserver();
        diaryShowViewModel.triggerAppErrorBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
