package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.CalendarDayBinding;
import com.websarva.wings.android.zuboradiary.databinding.CalendarHeaderBinding;
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding;
import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryViewModel;

import com.kizitonwose.calendar.view.CalendarView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarFragment extends Fragment {

    // View関係
    private FragmentCalendarBinding binding;
    private final LocalDate today = LocalDate.now();
    private final int MAX_ITEMS_COUNT = DiaryViewModel.MAX_ITEMS_COUNT; // 項目入力欄最大数

    // Navigation関係
    private NavController navController;

    // ViewModel
    private CalendarViewModel calendarViewModel;
    private DiaryViewModel diaryViewModel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.calendarViewModel = provider.get(CalendarViewModel.class);
        this.diaryViewModel = provider.get(DiaryViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        this.binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(CalendarFragment.this);
        binding.setDiaryViewModel(diaryViewModel);

        // 画面遷移時のアニメーション設定
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setEnterTransition(new MaterialFadeThrough());
            mainActivity.resetTabWasSelected();
        } else {
            setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        }
        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // TO - FROM の FROM として現れるアニメーション
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 項目タイトル入力フラグメントからデータ受取
        SavedStateHandle savedStateHandle =
                this.navController.getCurrentBackStackEntry().getSavedStateHandle();
        MutableLiveData<String> liveDataShowedDiaryDate =
                savedStateHandle.getLiveData(ShowDiaryFragment.KEY_SHOWED_DIARY_DATE);
        liveDataShowedDiaryDate.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                LocalDate localDate = DateConverter.toLocalDate(string);
                CalendarFragment.this.diaryViewModel.initialize();
                YearMonth selectedMonth =
                        YearMonth.of(localDate.getYear(), localDate.getMonthValue());
                selectDate(localDate);
                CalendarView calendar = CalendarFragment.this.binding.calendar;
                calendar.scrollToMonth(selectedMonth);
                savedStateHandle.remove(ShowDiaryFragment.KEY_SHOWED_DIARY_DATE);
            }
        });


        // カレンダー設定
        CalendarView calendar = this.binding.calendar;
        List<DayOfWeek> daysOfWeek = daysOfWeek(); // 曜日リスト取得
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(60); //現在から過去5年分
        YearMonth endMonth = currentMonth.plusMonths(60); //現在から未来5年分
        LocalDate selectedDate = this.calendarViewModel.getSelectedDate();
        configureCalendarBinders(daysOfWeek);
        calendar.setup(startMonth,endMonth,daysOfWeek.get(0));
        if (selectedDate != null) {
            YearMonth selectedMonth =
                    YearMonth.of(selectedDate.getYear(), selectedDate.getMonthValue());
            firstSelectDate(selectedDate);
            calendar.scrollToMonth(selectedMonth);
        } else {
            firstSelectDate(this.today);
            calendar.scrollToMonth(currentMonth);
        }

        // 天気表示欄設定
        this.diaryViewModel.getLiveIntWeather1()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        CalendarFragment.this.diaryViewModel.updateStrWeather1();
                    }
                });

        this.diaryViewModel.getLiveIntWeather2()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        TextView textWeatherSlush =
                                CalendarFragment.this.binding.includeShowDiary.textWeatherSlush;
                        TextView textWeather2Selected =
                                CalendarFragment.this.binding.includeShowDiary.textWeather2Selected;
                        if (integer != 0) {
                            textWeatherSlush.setVisibility(View.VISIBLE);
                            textWeather2Selected.setVisibility(View.VISIBLE);
                            CalendarFragment.this.diaryViewModel.updateStrWeather2();
                        } else {
                            textWeatherSlush.setVisibility(View.GONE);
                            textWeather2Selected.setVisibility(View.GONE);
                        }
                    }
                });


        // 気分表示欄設定
        this.diaryViewModel.getLiveIntCondition()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        diaryViewModel.updateStrCondition();
                    }
                });


        // FAB設定
        this.binding.floatActionButtonEditDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 日記編集(新規作成)フラグメント起動。
                LocalDate selectedDate = CalendarFragment.this.calendarViewModel.getSelectedDate();
                String editDiaryDate = DateConverter.toStringLocalDate(selectedDate);
                NavDirections action =
                        CalendarFragmentDirections
                                .actionNavigationCalendarFragmentToEditDiaryFragment(
                                        true,
                                        editDiaryDate
                                );
                CalendarFragment.this.navController.navigate(action);
            }
        });


        // 表示日記の下余白設定(FABが日記と重なるのを防ぐため)
        // MEMO:FABのレイアウト高さは"wrap_content"を使用しているため、
        //      レイアウト後に機能するviewTreeObserver#addOnGlobalLayoutListenerを使用して取得する。
        View viewShowDiaryBottomMargin = this.binding.viewShowDiaryBottomMargin;
        FloatingActionButton fabEditDiary = this.binding.floatActionButtonEditDiary;
        ViewTreeObserver viewTreeObserver = viewShowDiaryBottomMargin.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams viewMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) viewShowDiaryBottomMargin.getLayoutParams();
                ViewGroup.MarginLayoutParams buttonMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) fabEditDiary.getLayoutParams();

                viewMarginLayoutParams.height = fabEditDiary.getHeight()
                        + (buttonMarginLayoutParams.bottomMargin * 2);
                viewShowDiaryBottomMargin.setLayoutParams(viewMarginLayoutParams);

                // MEMO:例外で再度取得するように促される為、下記対応。
                viewShowDiaryBottomMargin.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // MEMO:CalendarViewはRecyclerViewを元に作成されている。
        //      Bind済みの年月、又は既存ホルダーから溢れたを年月を確認する方法が無い。
        //      既存日記日付リストの不要な年月のリストを都度クリアするタイミングがない為、ここでまとめてクリアする。
        this.calendarViewModel.clearExistedDiaryDateLog();
    }


    // CalendarViewで選択された日付の日記を表示
    private void showSelectedDiary() {

        LocalDate selectedDate = this.calendarViewModel.getSelectedDate();
        int year = selectedDate.getYear();
        int month = selectedDate.getMonthValue();
        int dayOfMonth = selectedDate.getDayOfMonth();

        if (this.diaryViewModel.hasDiary(year, month, dayOfMonth)) {
            // ViewModelの読込日記の日付をセット
            this.diaryViewModel.initialize();
            this.diaryViewModel.updateLoadingDate(year, month, dayOfMonth);
            this.diaryViewModel.prepareShowDiary();
            setupItemLayout(); // 必要数の項目欄表示
            this.binding.linearLayoutShowDiary.setVisibility(View.VISIBLE);
            this.binding.textNoDiary.setVisibility(View.GONE);
        } else {
            this.binding.linearLayoutShowDiary.setVisibility(View.GONE);
            this.binding.textNoDiary.setVisibility(View.VISIBLE);
            this.diaryViewModel.initialize();
        }
    }


    private MotionLayout selectItemMotionLayout(int itemNumber) {
        switch (itemNumber) {
            case 1:
                return this.binding.includeShowDiary.includeItem1.motionLayoutShowDiaryItem;
            case 2:
                return this.binding.includeShowDiary.includeItem2.motionLayoutShowDiaryItem;

            case 3:
                return this.binding.includeShowDiary.includeItem3.motionLayoutShowDiaryItem;

            case 4:
                return this.binding.includeShowDiary.includeItem4.motionLayoutShowDiaryItem;

            case 5:
                return this.binding.includeShowDiary.includeItem5.motionLayoutShowDiaryItem;
            default:
                return null;
        }
    }

    private void setupItemLayout() {
        int visibleItemsCount = this.diaryViewModel.getVisibleItemsCount();
        for (int i = 0; i < this.MAX_ITEMS_COUNT; i++) {
            int itemNumber = i + 1;
            MotionLayout itemMotionLayout = selectItemMotionLayout(itemNumber);
            if (itemNumber <= visibleItemsCount) {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_showed_state, 1);
            } else {
                itemMotionLayout
                        .transitionToState(R.id.motion_scene_show_diary_item_hided_state, 1);
            }
        }
    }

    // 曜日リスト取得
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
        CalendarView calendar = this.binding.calendar;

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

                // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    textCalendarDay.setVisibility(View.VISIBLE);

                    // 今日の日にちマス
                    if (calendarDay.getDate().isEqual(CalendarFragment.this.today)) {
                        textCalendarDay.setTextColor(
                                getResources()
                                        .getColor(R.color.md_theme_light_onSecondaryContainer)
                        );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_today_bg);

                    // 選択中の日にちマス
                    } else if (calendarDay.getDate()
                            .isEqual(CalendarFragment.this.calendarViewModel.getSelectedDate())) {
                        textCalendarDay.setTextColor(
                                getResources()
                                        .getColor(R.color.md_theme_light_onPrimaryContainer)
                        );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_selected_day_bg);

                    // それ以外の日にちマス
                    } else {
                        // TODO:祝日判定は手間がかかりそうなので保留
                        DayOfWeek dayOfWeek = calendarDay.getDate().getDayOfWeek();
                        int color = dayColor(dayOfWeek);
                        textCalendarDay.setTextColor(color);
                        textCalendarDay.setBackground(null);

                    }

                    // ドット有無設定
                    LocalDate localDate = calendarDay.getDate();
                    String date = DateConverter.toStringLocalDate(localDate);
                    if (CalendarFragment.this.calendarViewModel.existsDiary(date)) {
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
                CalendarFragment.this.calendarViewModel.updateExistedDiaryDateLog(stringYearMonth);

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
        this.calendarViewModel.setSelectedDate(date);
        this.binding.calendar.notifyDateChanged(date);
        updateActionBarDate();
        showSelectedDiary();

        // MEMO:アプリ初回起動時では、onViewCreatedの時点でアクションバーが確立していないため例外となる。
        //updateActionBarDate();
    }

    // カレンダー日付選択時処理
    private void selectDate(LocalDate date) {
        CalendarView calendar = this.binding.calendar;
        LocalDate selectedDate = this.calendarViewModel.getSelectedDate();

        if (selectedDate != date) {
            this.calendarViewModel.setSelectedDate(date);
            if (selectedDate != null) {
                Log.d("20240408", "notifyDateChanged(selectedDate)");
                calendar.notifyDateChanged(selectedDate);
            }
            calendar.notifyDateChanged(date);
            updateActionBarDate();
        }

        showSelectedDiary();
    }


    // アクションバー表示日付更新。
    private void updateActionBarDate() {
        LocalDate selectedDate = this.calendarViewModel.getSelectedDate();
        String stringDate = DateConverter.toStringLocalDate(selectedDate);
        this.binding.materialToolbarTopAppBar.setTitle(stringDate);
    }


    // 選択中ボトムナビゲーションタブを再選択時の処理
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
        YearMonth thisMonth = YearMonth.of(this.today.getYear(), this.today.getMonthValue());
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
        selectDate(this.today);
    }
}
