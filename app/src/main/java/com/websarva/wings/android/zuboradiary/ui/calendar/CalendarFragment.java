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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.common.util.concurrent.FutureCallback;
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
import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
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
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends BaseFragment {

    // View関係
    private FragmentCalendarBinding binding;

    // Navigation関係
    private boolean shouldShowDiaryLoadingErrorDialog;

    // ViewModel
    private CalendarViewModel calendarViewModel;
    private DiaryShowViewModel diaryShowViewModel; // TODO:diaryViewModelの使用要素をcalendarViewModelに含めるか検討(DiaryFragment修正後)
    private SettingsViewModel settingsViewModel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        calendarViewModel = provider.get(CalendarViewModel.class);
        diaryShowViewModel = provider.get(DiaryShowViewModel.class);
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setDiaryShowViewModel(diaryShowViewModel);

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

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpErrorObserver();
        setUpCalendar();
        setUpDiaryShow();
        setUpFloatActionButton();
        setUpBottomLayout();
    }

    @Override
    protected void handleOnReceivedResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
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
    protected void handleOnReceivedResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        retryErrorDialogShow();
    }

    @Override
    protected void removeResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    private void setUpErrorObserver() {
        calendarViewModel.getIsDiaryLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new IsDiaryLoadingErrorObserver());
        diaryShowViewModel.getIsDiaryLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new IsDiaryLoadingErrorObserver());
    }

    private class IsDiaryLoadingErrorObserver implements Observer<Boolean> {

        @Override
        public void onChanged(Boolean aBoolean) {
            if (aBoolean == null) {
                return;
            }
            if (aBoolean) {
                showDiaryLoadingErrorDialog();
                calendarViewModel.clearDiaryLoadingError();
            }
        }
    }

    private void setUpCalendar() {
        CalendarView calendar = binding.calendar;
        List<DayOfWeek> daysOfWeek = createDayOfWeekList(); // 曜日リスト取得
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(60); //現在から過去5年分
        YearMonth endMonth = currentMonth.plusMonths(60); //現在から未来5年分
        configureCalendarBinders(daysOfWeek);
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

        calendarViewModel.getLastSelectedDateLiveData()
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
        Integer calendarStartDayOfWeekNumber =
                settingsViewModel.getCalendarStartDayOfWeekNumberLiveData().getValue();
        DayOfWeek firstDayOfWeek;
        if (calendarStartDayOfWeekNumber == null) {
            firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        } else {
            firstDayOfWeek = DayOfWeek.of(calendarStartDayOfWeekNumber);
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
    private void configureCalendarBinders(List<DayOfWeek> daysOfWeek) {
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

                    // 今日の日にちマス
                    if (calendarDay.getDate().isEqual(LocalDate.now())) {
                        textCalendarDay
                                .setTextColor(
                                        getResources().getColor(R.color.md_theme_light_onSecondaryContainer)
                                );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_today_bg);

                        // 選択中の日にちマス
                    } else if (calendarDay.getDate().isEqual(calendarViewModel.getSelectedDateLiveData().getValue())) {
                        textCalendarDay
                                .setTextColor(
                                        getResources().getColor(R.color.md_theme_light_onPrimaryContainer)
                                );
                        textCalendarDay.setBackgroundResource(R.drawable.calendar_selected_day_bg);

                        // それ以外の日にちマス
                    } else {
                        // TODO:祝日判定は手間がかかりそうなので保留
                        DayOfWeek dayOfWeek = calendarDay.getDate().getDayOfWeek();
                        int color = selectDayColor(dayOfWeek);
                        textCalendarDay.setTextColor(color);
                        textCalendarDay.setBackground(null);
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

                        int color = selectDayColor(dayOfWeek);
                        childTextView.setTextColor(color);

                    }
                }

            }
        });
    }

    // カレンダー日にち、曜日の色取得
    private int selectDayColor(DayOfWeek dayOfWeek) {
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
        DateConverter dateConverter = new DateConverter();
        String stringDate = dateConverter.toStringLocalDate(date);
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
                if (selectedDate == null) {
                    selectedDate = LocalDate.now();
                    // TODO:assert検討
                }
                showDiaryEditFragment(selectedDate);
            }
        });
    }

    // 表示日記の下余白設定(FABが日記と重なるのを防ぐため)
    // MEMO:FABのレイアウト高さは"wrap_content"を使用しているため、
    //      レイアウト後に機能するviewTreeObserver#addOnGlobalLayoutListenerを使用して取得する。
    private void setUpBottomLayout() {
        View viewDiaryShowBottomMargin = binding.viewDiaryShowBottomMargin;
        FloatingActionButton fabDiaryEdit = binding.floatActionButtonDiaryEdit;
        ViewTreeObserver viewTreeObserver = viewDiaryShowBottomMargin.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams viewMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) viewDiaryShowBottomMargin.getLayoutParams();
                ViewGroup.MarginLayoutParams buttonMarginLayoutParams =
                        (ViewGroup.MarginLayoutParams) fabDiaryEdit.getLayoutParams();

                viewMarginLayoutParams.height = fabDiaryEdit.getHeight()
                        + (buttonMarginLayoutParams.bottomMargin * 2);
                viewDiaryShowBottomMargin.setLayoutParams(viewMarginLayoutParams);

                // MEMO:例外で再度取得するように促される為、下記対応。
                viewDiaryShowBottomMargin.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
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

    private void showDiaryEditFragment(LocalDate localDate) {
        NavDirections action =
                CalendarFragmentDirections
                        .actionNavigationCalendarFragmentToDiaryEditFragment(
                                true,
                                true,
                                localDate
                        );
        navController.navigate(action);
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiaryLoadingErrorDialog) {
            showDiaryLoadingErrorDialog();
        }
    }

    private void showDiaryLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog(
                    getString(R.string.dialog_message_title_access_error),
                    getString(R.string.dialog_message_message_diary_loading_error)
            );
            shouldShowDiaryLoadingErrorDialog = false;
        } else {
            shouldShowDiaryLoadingErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                CalendarFragmentDirections
                        .actionCalendarFragmentToMessageDialog(
                                title, message);
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_calendar_fragment;
    }
}
