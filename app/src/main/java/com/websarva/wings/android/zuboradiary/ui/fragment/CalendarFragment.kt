package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarDayBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarHeaderBinding
import com.websarva.wings.android.zuboradiary.ui.theme.CalendarThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.viewmodel.CalendarViewModel
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.absoluteValue

@AndroidEntryPoint
class CalendarFragment :
    BaseFragment<FragmentCalendarBinding, CalendarUiEvent>(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: CalendarViewModel by activityViewModels()

    override val destinationId = R.id.navigation_calendar_fragment
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCalendar()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun clearViewBindings() {
        with(binding.calendar) {
            dayBinder = null
            monthHeaderBinder = null
            monthScrollListener = null
        }

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setUpFragmentResultObservers() {
        observeDiaryShowFragmentResult()
        observeDiaryEditFragmentResult()
    }

    private fun observeDiaryShowFragmentResult() {
        observeFragmentResult(
            DiaryShowFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryShowFragmentResultReceived(result)
        }
    }

    private fun observeDiaryEditFragmentResult() {
        observeFragmentResult(
            DiaryEditFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryEditFragmentResultReceived(result)
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.id, event.date)
            }
            is CalendarUiEvent.ScrollCalendar -> {
                scrollCalendar(event.date)
            }
            is CalendarUiEvent.SmoothScrollCalendar -> {
                smoothScrollCalendar(event.date)
            }
            is CalendarUiEvent.RefreshCalendarDayDotVisibility -> {
                updateCalendarDayDotVisibility(event.date, event.isVisible)
            }
        }
    }

    override fun onCommonUiEventReceived(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.NavigatePreviousFragment<*> -> {
                mainActivityViewModel.onNavigateBackFromBottomNavigationTab()
            }
            is CommonUiEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
        }
    }

    override fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent) {
        when (event) {
            ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                processOnBottomNavigationItemReselected()
            }
        }
    }

    override fun setUpUiStateObservers() {
        super.setUpUiStateObservers()

        observeSelectedDate()
        observeCalendarStartDayOfWeek()
    }

    override fun setUpUiEventObservers() {
        super.setUpUiEventObservers()

        observeUiEventFromActivity()
    }

    private fun observeSelectedDate() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.selectedDate == new.selectedDate
                        && old.previousSelectedDate == new.previousSelectedDate
            }.map {
                Pair(it.selectedDate, it.previousSelectedDate)
            }.collect { (selectedDate, previousSelectedDate) ->
                updateCalendarSelectedDate(selectedDate, previousSelectedDate)
            }
        }
    }

    private fun observeCalendarStartDayOfWeek() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.calendarStartDayOfWeek == new.calendarStartDayOfWeek
            }.map {
                it.calendarStartDayOfWeek
            }.collect {
                // 週の開始曜日が変わった場合は、カレンダー全体を再構成する必要がある
                binding.calendar.notifyCalendarChanged()
            }
        }
    }

    private fun observeUiEventFromActivity() {
        fragmentHelper.observeActivityUiEvent(
            this,
            mainActivityViewModel,
            this
        )
    }
    //endregion

    //region View Manipulation
    private fun processOnBottomNavigationItemReselected() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop()
            return
        }
        mainViewModel.onBottomNavigationItemReselect()
    }

    private fun scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0)
    }
    //endregion

    //region Calendar View - Setup
    private fun setUpCalendar() {
        val calendar = binding.calendar

        val daysOfWeek = createDayOfWeekList() // 曜日リスト取得
        configureCalendarBinders(daysOfWeek, themeColor)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60) //現在から過去5年分
        val endMonth = currentMonth.plusMonths(60) //現在から未来5年分
        calendar.setup(startMonth, endMonth, daysOfWeek[0])
    }

    private fun createDayOfWeekList(): List<DayOfWeek> {
        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        val startDayOfWeek = mainViewModel.uiState.value.calendarStartDayOfWeek
        val startDayOfWeekListPos = startDayOfWeek.value
        // 開始曜日を先頭に並び替え
        val firstList =
            Arrays.stream(daysOfWeek)
                .skip((startDayOfWeekListPos - 1).toLong())
                .collect(Collectors.toList())
        val secondList =
            Arrays.stream(daysOfWeek)
                .limit((startDayOfWeekListPos - 1).toLong())
                .collect(Collectors.toList())
        return Stream
            .concat(firstList.stream(), secondList.stream())
            .collect(Collectors.toList())
    }

    // カレンダーBind設定
    private fun configureCalendarBinders(daysOfWeek: List<DayOfWeek>, themeColor: ThemeColorUi) {
        binding.calendar.dayBinder =
            CalendarMonthDayBinder(
                themeColor,
                { date: LocalDate -> mainViewModel.onCalendarDayClick(date) },
                { date: LocalDate -> mainViewModel.onCalendarDayDotVisibilityCheck(date) }
            )

        val format = getString(R.string.fragment_calendar_month_header_format)
        binding.calendar.monthHeaderBinder =
            CalendarMonthHeaderFooterBinder(daysOfWeek, themeColor, format)
    }

    private class CalendarMonthDayBinder(
        private val themeColor: ThemeColorUi,
        private val onDateClick: (date: LocalDate) -> Unit,
        private val processCheckDiaryExists: (date: LocalDate) -> Unit
    ) : MonthDayBinder<DayViewContainer> {

        private var selectedDate: LocalDate = LocalDate.now()

        private val dayDotVisibilityCache = mutableMapOf<LocalDate, Boolean>()

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: DayViewContainer, calendarDay: CalendarDay) {
            val textDay = container.binding.textDay.apply {
                setOnClickListener {
                    if (calendarDay.position == DayPosition.MonthDate) {
                        onDateClick(calendarDay.date)
                    }
                }

                // 数値設定
                val day = calendarDay.date.dayOfMonth.toString()
                text = day
            }
            val viewDayDot = container.binding.viewDayDot

            // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
            if (calendarDay.position == DayPosition.MonthDate) {
                textDay.visibility = View.VISIBLE
                setUpCalendarDayColor(calendarDay, textDay, viewDayDot)
                setUpCalendarDayDotVisibility(calendarDay, viewDayDot)
            } else {
                textDay.visibility = View.INVISIBLE
                viewDayDot.visibility = View.INVISIBLE
            }
        }

        private fun setUpCalendarDayColor(
            calendarDay: CalendarDay, textCalendarDay: TextView, viewCalendarDayDot: View
        ) {
            val themeColorChanger =
                CalendarThemeColorChanger()

            val isSelectedDay = calendarDay.date.isEqual(selectedDate)
            val isToday = calendarDay.date.isEqual(LocalDate.now())

            if (isSelectedDay) {
                themeColorChanger.applyCalendarSelectedDayColor(
                    textCalendarDay,
                    viewCalendarDayDot,
                    themeColor
                )
            } else if (isToday) {
                themeColorChanger
                    .applyCalendarTodayColor(
                        textCalendarDay,
                        viewCalendarDayDot,
                        themeColor
                    )
            } else {
                val dayOfWeek = calendarDay.date.dayOfWeek
                val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
                val isSunday = dayOfWeek == DayOfWeek.SUNDAY

                if (isSaturday) {
                    themeColorChanger.applyCalendarSaturdayColor(
                        textCalendarDay,
                        viewCalendarDayDot,
                        themeColor
                    )
                } else if (isSunday) {
                    themeColorChanger.applyCalendarSundayColor(
                        textCalendarDay,
                        viewCalendarDayDot,
                        themeColor
                    )
                } else {
                    themeColorChanger.applyCalendarWeekdaysColor(
                        textCalendarDay,
                        viewCalendarDayDot,
                        themeColor
                    )
                }
            }
        }

        private fun setUpCalendarDayDotVisibility(calendarDay: CalendarDay, viewCalendarDayDot: View) {
            val localDate = calendarDay.date
            val boolean = dayDotVisibilityCache[localDate]
            viewCalendarDayDot.visibility =
                if (boolean == null) {
                    processCheckDiaryExists(localDate)
                    View.INVISIBLE
                } else {
                    if (boolean) View.VISIBLE else View.INVISIBLE
                }
        }

        override fun create(view: View): DayViewContainer {
            return DayViewContainer(view)
        }

        fun updateSelectedDate(date: LocalDate) {
            selectedDate = date
        }

        fun updateDayDotVisibilityCache(date: LocalDate, isVisible: Boolean) {
            dayDotVisibilityCache[date] = isVisible
        }
    }

    private class CalendarMonthHeaderFooterBinder(
        private val daysOfWeek: List<DayOfWeek>,
        private val themeColor: ThemeColorUi,
        private val headerDateFormat: String
    ) : MonthHeaderFooterBinder<MonthViewContainer> {

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: MonthViewContainer, calendarMonth: CalendarMonth) {
            // カレンダーの年月表示設定
            val formatter = DateTimeFormatter.ofPattern(headerDateFormat)
            val stringYearMonth = calendarMonth.yearMonth.format(formatter)
            container.binding.textYearMonth.text = stringYearMonth

            // カレンダーの曜日設定(未設定アイテムのみ設定)
            val linearLayout = container.binding.legendLayout.root
            if (linearLayout.tag != null) return
            linearLayout.tag = calendarMonth.yearMonth

            // カレンダー曜日表示設定
            val max = linearLayout.childCount
            for (i in 0 until max) {
                val childView = linearLayout.getChildAt(i)
                val childTextView = childView as TextView
                val dayOfWeek = daysOfWeek[i]

                childTextView.text = dayOfWeek.name.substring(0, 3)

                setUpDayOfWeekColor(dayOfWeek, childTextView)
            }
        }

        private fun setUpDayOfWeekColor(dayOfWeek: DayOfWeek, dayOfWeekText: TextView) {
            val themeColorChanger = CalendarThemeColorChanger()

            val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
            val isSunday = dayOfWeek == DayOfWeek.SUNDAY

            if (isSaturday) {
                themeColorChanger.applyCalendarDayOfWeekSaturdayColor(dayOfWeekText)
            } else if (isSunday) {
                themeColorChanger.applyCalendarDayOfWeekSundayColor(dayOfWeekText)
            } else {
                themeColorChanger.applyCalendarDayOfWeekWeekdaysColor(dayOfWeekText, themeColor)
            }
        }

        override fun create(view: View): MonthViewContainer {
            return MonthViewContainer(view)
        }
    }

    // カレンダー日単位コンテナ
    private class DayViewContainer(view: View) : ViewContainer(view) {
        val binding: LayoutCalendarDayBinding = LayoutCalendarDayBinding.bind(view)
    }

    // カレンダー月単位コンテナ
    private class MonthViewContainer(view: View) : ViewContainer(view) {
        val binding: LayoutCalendarHeaderBinding = LayoutCalendarHeaderBinding.bind(view)
    }
    //endregion

    //region Calendar View Manipulation
    private fun scrollCalendar(date: LocalDate) {
        val calendar = binding.calendar
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val currentMonth = calendar.findFirstVisibleMonth()?.yearMonth
        if (targetYearMonth == currentMonth) return

        binding.calendar.scrollToMonth(targetYearMonth)
    }

    private fun smoothScrollCalendar(date: LocalDate) {
        val calendar = binding.calendar
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val currentMonth = calendar.findFirstVisibleMonth()?.yearMonth

        if (currentMonth == null) {
            calendar.scrollToMonth(targetYearMonth)
            return
        }
        if (targetYearMonth == currentMonth) return


        val monthsBetween =
            ChronoUnit.MONTHS.between(
                currentMonth,
                targetYearMonth
            ).absoluteValue

        if (monthsBetween > 3) {
            calendar.scrollToMonth(targetYearMonth)
        } else {
            calendar.smoothScrollToMonth(targetYearMonth)
        }
    }

    private fun updateCalendarDayDotVisibility(date: LocalDate, isVisible: Boolean) {
        val calendarMonthDayBinder = binding.calendar.dayBinder as CalendarMonthDayBinder
        calendarMonthDayBinder.updateDayDotVisibilityCache(date, isVisible)
        binding.calendar.notifyDateChanged(date)
    }

    private fun updateCalendarSelectedDate(
        selectedDate: LocalDate,
        previousSelectedDate: LocalDate?
    ) {
        val calendar = binding.calendar
        val calendarMonthDayBinder =
            calendar.dayBinder as CalendarMonthDayBinder
        calendarMonthDayBinder.updateSelectedDate(selectedDate)
        calendar.notifyDateChanged(selectedDate) // 今回選択日付更新
        previousSelectedDate?.let { calendar.notifyDateChanged(it) } // 前回選択日付更新
    }
    //endregion

    //region Navigation Helpers
    private fun navigateDiaryEditFragment(id: String?, date: LocalDate) {
        val directions =
            CalendarFragmentDirections
                .actionNavigationCalendarFragmentToDiaryEditFragment(id, date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            CalendarFragmentDirections.actionCalendarFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion
}
