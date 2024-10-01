package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment;

import com.kizitonwose.calendar.view.CalendarView;
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowViewModel;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowConditionObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowLogObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowNumVisibleItemsObserver;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather1Observer;
import com.websarva.wings.android.zuboradiary.ui.observer.DiaryShowWeather2Observer;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
    private SettingsViewModel settingsViewModel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addOnBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireMainActivity().popBackStackToStartDestination();
            }
        });
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        calendarViewModel = provider.get(CalendarViewModel.class);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentCalendarBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpCalendar();
        setUpDiaryShow();
        setUpFloatActionButton();
    }

    @Override
    protected void setUpThemeColor() {
        // 処理なし
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
        retryErrorDialogShow();
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void setUpErrorMessageDialog() {
        calendarViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(calendarViewModel));
        diaryShowViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(diaryShowViewModel));
        settingsViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(settingsViewModel));
    }

    private void setUpCalendar(/*ThemeColor themeColor*/) {
        CalendarView calendar = binding.calendar;
        List<DayOfWeek> daysOfWeek = createDayOfWeekList(); // 曜日リスト取得
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(60); //現在から過去5年分
        YearMonth endMonth = currentMonth.plusMonths(60); //現在から未来5年分
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        configureCalendarBinders(daysOfWeek, themeColor);
        calendar.setup(startMonth,endMonth,daysOfWeek.get(0));
        if (calendarViewModel.getSelectedDateLiveData().getValue() == null) {
            calendarViewModel.updateSelectedDate(LocalDate.now());
        }

        calendarViewModel.getSelectedDateLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalDate>() {
                    @Override
                    public void onChanged(LocalDate localDate) {
                        if (localDate == null) {
                            return;
                        }
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
                        if (localDate == null) {
                            return;
                        }
                        binding.calendar.notifyDateChanged(localDate); // 前回選択日付更新
                    }
                });
    }

    private List<DayOfWeek> createDayOfWeekList() {
        DayOfWeek firstDayOfWeek =
                settingsViewModel.getCalendarStartDayOfWeekLiveData().getValue();
        if (firstDayOfWeek == null) {
            throw new NullPointerException();
        }
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        int firstDayOfWeekListPos = firstDayOfWeek.getValue();
        // 開始曜日を先頭に並び替え
        List<DayOfWeek> firstHalfList =
                Arrays.stream(daysOfWeek)
                        .skip(firstDayOfWeekListPos - 1)
                        .collect(Collectors.toList());
        List<DayOfWeek> secondHalfList =
                Arrays.stream(daysOfWeek)
                        .limit(firstDayOfWeekListPos - 1)
                        .collect(Collectors.toList());
        return Stream
                .concat(firstHalfList.stream(), secondHalfList.stream())
                .collect(Collectors.toList());
    }

    // カレンダーBind設定
    private void configureCalendarBinders(List<DayOfWeek> daysOfWeek, ThemeColor themeColor) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        // カレンダーの日にち設定
        binding.calendar.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay calendarDay) {
                container.calendarDay = calendarDay;
                TextView textCalendarDay = container.binding.textCalendarDay;
                View viewCalendarDayDot = container.binding.viewCalendarDayDot;

                // 数値設定
                String day = String.valueOf(calendarDay.getDate().getDayOfMonth());
                textCalendarDay.setText(day);

                // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    textCalendarDay.setVisibility(View.VISIBLE);

                    CalendarThemeColorSwitcher themeColorSwitcher =
                            new CalendarThemeColorSwitcher(requireContext(), themeColor);

                    // 選択中の日にちマス
                    if (calendarDay.getDate().isEqual(calendarViewModel.getSelectedDateLiveData().getValue())) {
                        themeColorSwitcher.switchCalendarSelectedDayColor(textCalendarDay, viewCalendarDayDot);

                        // 今日の日にちマス
                    } else if (calendarDay.getDate().isEqual(LocalDate.now())) {
                        themeColorSwitcher.switchCalendarTodayColor(textCalendarDay, viewCalendarDayDot);

                        // それ以外の日にちマス
                    } else {
                        themeColorSwitcher.switchCalendarNormalDayColor(textCalendarDay, viewCalendarDayDot);

                        // TODO:祝日判定は手間がかかりそうなので保留
                        DayOfWeek dayOfWeek = calendarDay.getDate().getDayOfWeek();
                        if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                            int color = selectDayColor(dayOfWeek);
                            textCalendarDay.setTextColor(color);
                            viewCalendarDayDot.setBackgroundColor(color);
                        }
                    }

                    // ドット有無設定
                    LocalDate localDate = calendarDay.getDate();
                    calendarViewModel.hasDiary(localDate, new FutureCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Log.d("20240806", "hasDiary()_onSuccess:" + result) ;
                            if (result) {
                                viewCalendarDayDot.setVisibility(View.VISIBLE);
                            } else {
                                viewCalendarDayDot.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            Log.d("20240806", "hasDiary()_onFailure") ;
                            // 例外はViewModelクラス内で例外用リスナーを追加して対応
                            viewCalendarDayDot.setVisibility(View.INVISIBLE);
                        }
                    });

                } else {
                    textCalendarDay.setVisibility(View.INVISIBLE);
                    viewCalendarDayDot.setVisibility(View.INVISIBLE);
                }

            }
        });

        // カレンダーのヘッダー設定
        binding.calendar.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth calendarMonth) {
                // カレンダーの年月表示設定
                String format = getString(R.string.fragment_calendar_month_header_format);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                String stringYearMonth = calendarMonth.getYearMonth().format(formatter);
                container.textYearMonth.setText(stringYearMonth);

                // カレンダーの曜日設定(未設定アイテムのみ設定)
                if (container.legendLayout.getTag() == null) {
                    container.legendLayout.setTag(calendarMonth.getYearMonth());

                    // カレンダー曜日表示設定
                    int max = container.legendLayout.getChildCount();
                    for (int i = 0; i < max; i++) {
                        View childView = container.legendLayout.getChildAt(i);
                        TextView childTextView = (TextView) childView;
                        DayOfWeek dayOfWeek = daysOfWeek.get(i);

                        childTextView.setText(dayOfWeek.name().substring(0,3));

                        CalendarThemeColorSwitcher themeColorSwitcher =
                                new CalendarThemeColorSwitcher(requireContext(), themeColor);
                        themeColorSwitcher.switchCalendarDayOfWeekColor(childTextView);

                        if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                            int color = selectDayColor(dayOfWeek);
                            childTextView.setTextColor(color);
                        }


                    }
                }

            }
        });
    }

    // カレンダー日にち、曜日の色取得
    private int selectDayColor(DayOfWeek dayOfWeek) {
        int colorResId;
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            colorResId = R.color.red;
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            colorResId = R.color.blue;
        } else {
            colorResId = R.color.black;
        }
        return getResources().getColor(colorResId);
    }

    // カレンダー日単位コンテナ
    class DayViewContainer extends ViewContainer {
        CalendarDay calendarDay;
        CalendarDayBinding binding;
        public DayViewContainer(View view) {
            super(view);
            binding = CalendarDayBinding.bind(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (calendarDay.getPosition() == DayPosition.MonthDate) {
                        calendarViewModel.updateSelectedDate(calendarDay.getDate());
                    }
                }
            });
        }
    }

    // カレンダー月単位コンテナ
    public static class MonthViewContainer extends ViewContainer {
        TextView textYearMonth;
        LinearLayout legendLayout;
        public MonthViewContainer(View view) {
            super(view);
            textYearMonth = CalendarHeaderBinding.bind(view).textYearMonth;
            // TODO:下記LinearLayoutをDataBindingで参照できなかったのでViewBindingで対応。原因を調査する。
            legendLayout = CalendarHeaderBinding.bind(view).legendLayout.getRoot();
        }
    }

    // カレンダーを指定した日付へ自動スクロール
    private void scrollCalendar(LocalDate date) {
        YearMonth targetYearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        CalendarMonth currentMonth = binding.calendar.findFirstVisibleMonth();
        if (currentMonth == null) {
            binding.calendar.scrollToMonth(targetYearMonth);
            return;
        }
        YearMonth currentYearMonth = currentMonth.getYearMonth();
        if (targetYearMonth == currentYearMonth) {
            return;
        }
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

    // ツールバー表示日付更新。
    private void updateToolBarDate(LocalDate date) {
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String stringDate = dateTimeStringConverter.toStringDate(date);
        binding.materialToolbarTopAppBar.setTitle(stringDate);
    }

    // CalendarViewで選択された日付の日記を表示
    private void showSelectedDiary(LocalDate date) {
        calendarViewModel.hasDiary(date, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    // ViewModelの読込日記の日付をセット
                    diaryShowViewModel.initialize();
                    diaryShowViewModel.loadDiary(date);
                    binding.linearLayoutDiaryShow.setVisibility(View.VISIBLE);
                    binding.textNoDiary.setVisibility(View.GONE);
                } else {
                    binding.linearLayoutDiaryShow.setVisibility(View.GONE);
                    binding.textNoDiary.setVisibility(View.VISIBLE);
                    diaryShowViewModel.initialize();
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                // 例外はViewModelクラス内で例外用リスナーを追加して対応
                binding.linearLayoutDiaryShow.setVisibility(View.GONE);
                binding.textNoDiary.setVisibility(View.VISIBLE);
                diaryShowViewModel.initialize();
            }
        });
    }

    private void setUpDiaryShow() {
        diaryShowViewModel.getWeather1LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowWeather1Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeather1Selected
                        )
                );

        diaryShowViewModel.getWeather2LiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowWeather2Observer(
                                requireContext(),
                                binding.includeDiaryShow.textWeatherSlush,
                                binding.includeDiaryShow.textWeather2Selected
                        )
                );

        diaryShowViewModel.getConditionLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowConditionObserver(
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
                .observe(getViewLifecycleOwner(), new DiaryShowNumVisibleItemsObserver(itemLayouts));

        diaryShowViewModel.getLogLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new DiaryShowLogObserver(binding.includeDiaryShow.textLogValue)
                );
    }



    private void setUpFloatActionButton() {
        binding.floatActionButtonDiaryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDate selectedDate = calendarViewModel.getSelectedDateLiveData().getValue();
                showDiaryEditFragment(selectedDate);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        if (date == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                CalendarFragmentDirections
                        .actionNavigationCalendarFragmentToDiaryEditFragment(
                                true,
                                true,
                                date
                        );
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                CalendarFragmentDirections
                        .actionCalendarFragmentToMessageDialog(
                                title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        calendarViewModel.triggerAppErrorBufferListObserver();
        diaryShowViewModel.triggerAppErrorBufferListObserver();
        settingsViewModel.triggerAppErrorBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
