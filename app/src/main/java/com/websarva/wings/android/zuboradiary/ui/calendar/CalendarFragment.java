package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.CalendarDayBinding;
import com.websarva.wings.android.zuboradiary.databinding.CalendarHeaderBinding;
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding;
import com.websarva.wings.android.zuboradiary.ui.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryViewModel;

import com.kizitonwose.calendar.view.CalendarView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarViewModel calendarViewModel;
    private EditDiaryViewModel diaryViewModel;
    private LocalDate today = LocalDate.now();
    private LocalDate selectedDate;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.calendarViewModel = provider.get(CalendarViewModel.class);
        this.diaryViewModel = provider.get(EditDiaryViewModel.class);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // 処理なし(無効化)

                    }
                }
        );

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // データバインディング設定
        this.binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        //データバインディング設定(ビューモデルのライブデータ画面反映設定)
        binding.setLifecycleOwner(CalendarFragment.this);
        binding.setDiaryViewModel(diaryViewModel);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // アクションバー表示日付更新。
        // showSelectedDiary()メソッド内で処理(後記述)


        // カレンダー日付切り替えリスナ設定
        // 20240403_旧Calendar
        /*CalendarView calendarView = this.binding.calendar;
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Log.d("20240328", "onSelectedDayChange");
                int _month = month + 1; // 引数の月の値がひと月小さい 例:1月 -> 0

                showSelectedDiary(year, _month, dayOfMonth);

                // 選択日付を保存(ボトムナビケージョン切り替え時復元用)
                Calendar selectedDateCalendar = loadCalender(year, month, dayOfMonth);
                calendarView.setDate(selectedDateCalendar.getTimeInMillis());
                calendarViewModel.setBackupCalendarDate(selectedDateCalendar.getTimeInMillis());

            }
        });*/


        // 20240403_新Calendar
        CalendarView calendarView1 = this.binding.calendar;

        List<DayOfWeek> daysOfWeek = daysOfWeek();
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(60); //現在から過去5年分
        YearMonth endMonth = currentMonth.plusMonths(60); //現在から未来5年分
        LocalDate selectedDate = calendarViewModel.getSelectedDate();

        configureCalendarBinders(daysOfWeek);

        calendarView1.setup(startMonth,endMonth,daysOfWeek.get(0));


        if (selectedDate != null) {
            YearMonth selectedMonth =
                    YearMonth.of(selectedDate.getYear(), selectedDate.getMonthValue());

            firstSelectDate(selectedDate);
            calendarView1.scrollToMonth(selectedMonth);

        } else {
            firstSelectDate(today);
            calendarView1.scrollToMonth(currentMonth);

        }


        // 天気表示欄設定
        // MEMO:ShowDiaryFragmentの中身を一つのレイアウトとして独立させた。
        //      bindingで中身のViewを取得できなくなった為、findViewById()メソッドを使用してViewを取得。
        TextView textWeatherSlush = getActivity().findViewById(R.id.text_weather_slush);
        TextView textWeather2Selected =
                getActivity().findViewById(R.id.text_weather_2_selected);
        diaryViewModel.getLiveIntWeather2().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if ((integer != 0) && (integer != null)) {
                    /*binding.textWeatherSlush.setVisibility(View.VISIBLE);
                    binding.textWeather2Selected.setVisibility(View.VISIBLE);*/
                    textWeatherSlush.setVisibility(View.VISIBLE);
                    textWeather2Selected.setVisibility(View.VISIBLE);

                } else {
                    /*binding.textWeatherSlush.setVisibility(View.GONE);
                    binding.textWeather2Selected.setVisibility(View.GONE);*/
                    textWeatherSlush.setVisibility(View.GONE);
                    textWeather2Selected.setVisibility(View.GONE);

                }


            }
        });

        diaryViewModel.getLiveIntWeather1().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather1();
            }
        });

        diaryViewModel.getLiveIntWeather2().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrWeather2();
            }
        });


        // 気分表示欄設定
        diaryViewModel.getLiveIntCondition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                diaryViewModel.updateStrCondition();
            }
        });


        // FAB設定
        FloatingActionButton floatActionButtonEditDiary = binding.floatActionButtonEditDiary;
        floatActionButtonEditDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:下記プログラムはテスト用。後で修正。
                calendarView1.scrollToMonth(currentMonth.plusYears(1));
            }
        });


        // 表示日記の下余白設定(FABが日記と重なるのを防ぐため)
        // MEMO:FABのレイアウト高さは"wrap_content"を使用しているため、
        //      レイアウト後に機能するviewTreeObserver#addOnGlobalLayoutListenerを使用して取得する。
        View viewShowDiaryBottomMargin = binding.viewShowDiaryBottomMargin;
        ViewTreeObserver viewTreeObserver = viewShowDiaryBottomMargin.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams viewMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) viewShowDiaryBottomMargin.getLayoutParams();
                ViewGroup.MarginLayoutParams buttonMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) floatActionButtonEditDiary.getLayoutParams();

                viewMarginLayoutParams.height = floatActionButtonEditDiary.getHeight()
                        + (buttonMarginLayoutParams.bottomMargin * 2);
                viewShowDiaryBottomMargin.setLayoutParams(viewMarginLayoutParams);

                viewTreeObserver.removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // MEMO:CalendarViewはRecyclerViewを元に作成されている。
        //      Bind済みの年月、又は既存ホルダーから溢れたを年月を確認する方法が無い。
        //      既存日記日付リストの不要な年月のリストを都度クリアするタイミングがない為、ここでまとめてクリアする。
        calendarViewModel.clearExistedDiaryDateLog();
    }

    // CalendarViewを元にCalendarクラスのインスタンスを取得
    private Calendar loadCurrentCalender() {
        CalendarView calendarView = binding.calendar;
        Calendar calendar = Calendar.getInstance();
        // 20240403_Calendar
        //calendar.setTimeInMillis(calendarView.getDate());
        return calendar;
    }

    private Calendar loadCalender(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return calendar;
    }

    private Calendar loadCalender(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    // CalendarViewで選択された日付の日記を表示
    private void showSelectedDiary(int year, int month, int dayOfMonth) {
        if (diaryViewModel.hasDiary(year, month, dayOfMonth)) {
            // ViewModelの読込日記の日付をセット
            diaryViewModel.updateLoadingDate(year, month, dayOfMonth);
            diaryViewModel.prepareShowDiary();
            binding.linearLayoutShowDiary.setVisibility(View.VISIBLE);
            binding.textNoDiary.setVisibility(View.GONE);

        } else {
            binding.linearLayoutShowDiary.setVisibility(View.GONE);
            binding.textNoDiary.setVisibility(View.VISIBLE);
            diaryViewModel.clear();

        }

    }

    private List<DayOfWeek> daysOfWeek() {
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        int pivot = 7 - firstDayOfWeek.ordinal(); // TODO:getValue()に変更した方が良い？
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        int firstDayOfWeekListPos = firstDayOfWeek.getValue();
        // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
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
    private void configureCalendarBinders(List<DayOfWeek> daysOfWeek) {
        CalendarView calendar = binding.calendar;

        // カレンダーの日にち設定
        calendar.setDayBinder(new MonthDayBinder<DayViewContainer>() {
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
                Log.d("20240408", String.valueOf(calendarDay.getDate().getMonthValue())+ "月" + day + "日");

                // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    textCalendarDay.setVisibility(View.VISIBLE);

                    // 今日の日にちマス
                    if (calendarDay.getDate().isEqual(today)) {
                        textCalendarDay.setTextColor(
                                getResources()
                                        .getColor(R.color.md_theme_light_onSecondaryContainer)
                        );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_today_bg);
                        //viewCalendarDayDot.setVisibility(View.INVISIBLE);

                    // 選択中の日にちマス
                    } else if (calendarDay.getDate().isEqual(calendarViewModel.getSelectedDate())) {
                        textCalendarDay.setTextColor(
                                getResources()
                                        .getColor(R.color.md_theme_light_onPrimaryContainer)
                        );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_selected_day_bg);
                        //viewCalendarDayDot.setVisibility(View.INVISIBLE);

                    // それ以外の日にちマス
                    } else {
                        // TODO:祝日判定は手間がかかりそうなので保留
                        DayOfWeek dayOfWeek = calendarDay.getDate().getDayOfWeek();
                        int color = dayColor(dayOfWeek);
                        textCalendarDay.setTextColor(color);
                        textCalendarDay.setBackground(null);
                        //viewCalendarDayDot.setVisibility(View.INVISIBLE); // TODO:日記ありなしで切り替える

                    }

                    // ドット有無設定
                    LocalDate localDate = calendarDay.getDate();
                    String date = DateConverter.toStringLocalDate(localDate);
                    if (calendarViewModel.existsDiary(date)) {
                        viewCalendarDayDot.setVisibility(View.VISIBLE);
                    } else {
                        viewCalendarDayDot.setVisibility(View.INVISIBLE);
                    }

                } else {
                    textCalendarDay.setVisibility(View.INVISIBLE);
                    viewCalendarDayDot.setVisibility(View.INVISIBLE);
                }

            }
        });

        // カレンダーのヘッダー設定
        calendar.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth calendarMonth) {
                // カレンダーの年月表示設定
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
                String stringYearMonth = calendarMonth.getYearMonth().format(formatter);
                container.textYearMonth.setText(stringYearMonth);
                Log.d("20240408", stringYearMonth + "作成");

                // 対象年月の既存日記確認リスト格納
                calendarViewModel.updateExistedDiaryDateLog(stringYearMonth);

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

                        int color = dayColor(dayOfWeek);
                        childTextView.setTextColor(color);

                    }
                }

            }
        });

    }

    // カレンダー日にち、曜日の色取得
    private int dayColor(DayOfWeek dayOfWeek) {
        int color = getResources().getColor(R.color.black);
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            color = getResources().getColor(R.color.red);
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            color = getResources().getColor(R.color.blue);
        }
        return color;
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
                        selectDate(calendarDay.getDate());
                    }
                }
            });
        }
    }

    // カレンダー月単位コンテナ
    class MonthViewContainer extends ViewContainer {
        TextView textYearMonth;
        LinearLayout legendLayout;
        public MonthViewContainer(View view) {
            super(view);
            textYearMonth = CalendarHeaderBinding.bind(view).textYearMonth;
            legendLayout = CalendarHeaderBinding.bind(view).legendLayout.getRoot();
        }
    }

    private void firstSelectDate(LocalDate date) {
        CalendarView calendar = binding.calendar;

        calendarViewModel.setSelectedDate(date);
        calendar.notifyDateChanged(date);
        showSelectedDiary(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        updateActionBarDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    // カレンダー日付選択時処理
    private void selectDate(LocalDate date) {
        CalendarView calendar = binding.calendar;
        LocalDate selectedDate = calendarViewModel.getSelectedDate();

        if (selectedDate != date) {
            LocalDate oldDate = selectedDate;

            calendarViewModel.setSelectedDate(date);

            if (oldDate != null) {
                Log.d("20240408", "notifyDateChanged(oldDate)");
                calendar.notifyDateChanged(oldDate);
            }
            calendar.notifyDateChanged(date);
            showSelectedDiary(
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth()
            );

            updateActionBarDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

        }
    }

    // アクションバー表示日付更新。
    private void updateActionBarDate(int year, int month, int dayOfMonth) {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        actionBar.setTitle(stringDate);
    }

    // ボタムナビゲーションタップ時処理
    public void onNavigationItemReselected() {
        NestedScrollView nestedScrollFullScreen = this.binding.nestedScrollFullScreen;
        if (nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop();
        } else {
            scrollCalendarToToday();
        }


    }

    // 先頭へ自動スクロール
    private void scrollToTop() {
        NestedScrollView nestedScrollFullScreen = this.binding.nestedScrollFullScreen;
        nestedScrollFullScreen.smoothScrollTo(0, 0);
    }

    // カレンダーを今日の日付へ自動スクロール
    // TODO:scrollとsmoothScrollを連続で処理するとかくつくので実機で確認。(PCが重いせいかもしれない)
    private void scrollCalendarToToday() {
        CalendarView calendar = this.binding.calendar;
        YearMonth thisMonth = YearMonth.of(today.getYear(), today.getMonthValue());
        YearMonth showedCalendarMonth = calendar.findFirstVisibleMonth().getYearMonth();

        // カレンダーが今日の日付月から遠い月を表示していたらsmoothScrollの処理時間が延びるので、
        // 手前にScroll処理を入れる。
        if (showedCalendarMonth.isAfter(thisMonth)) {
            YearMonth addedThisMonth  = thisMonth.plusMonths(6);
            YearMonth subtractedCalendarMonth  = showedCalendarMonth.minusMonths(3);

            if (showedCalendarMonth.isAfter(addedThisMonth)) {
                calendar.smoothScrollToMonth(subtractedCalendarMonth);
                calendar.scrollToMonth(addedThisMonth);
            }

        } else if (showedCalendarMonth.isBefore(thisMonth)) {
            YearMonth subtractedThisMonth  = thisMonth.minusMonths(6);
            YearMonth addedCalendarMonth  = showedCalendarMonth.plusMonths(3);

            if (showedCalendarMonth.isBefore(subtractedThisMonth)) {
                calendar.smoothScrollToMonth(addedCalendarMonth);
                calendar.scrollToMonth(subtractedThisMonth);
            }
        }

        calendar.smoothScrollToMonth(YearMonth.now());
        selectDate(today);
    }
}
