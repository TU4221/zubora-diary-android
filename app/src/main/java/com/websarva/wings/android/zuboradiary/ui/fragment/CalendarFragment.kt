package com.websarva.wings.android.zuboradiary.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarDayBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarHeaderBinding
import com.websarva.wings.android.zuboradiary.ui.theme.CalendarThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.viewmodel.CalendarViewModel
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryConditionTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryImageUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryItemsVisibilityUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryLogTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryWeatherTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ReselectableFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream
@AndroidEntryPoint
class CalendarFragment :
    BaseFragment<FragmentCalendarBinding, CalendarEvent>(),
    ReselectableFragment,
    RequiresBottomNavigation {

    override val destinationId = R.id.navigation_calendar_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: CalendarViewModel by activityViewModels()

    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
                baseDiaryShowViewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCalendar()
        setUpDiaryShow()
        setUpFloatActionButton()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryShowFragmentResultReceiver()
        setUpDiaryEditFragmentResultReceiver()
    }

    private fun setUpDiaryShowFragmentResultReceiver() {
        setUpFragmentResultReceiver(
            DiaryShowFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryShowFragmentResultReceived(result)
        }
    }

    private fun setUpDiaryEditFragmentResultReceiver() {
        setUpFragmentResultReceiver(
            DiaryEditFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDataReceivedFromDiaryEditFragment(result)
        }
    }

    override fun onMainUiEventReceived(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.date, !event.isNewDiary)
            }
            is CalendarEvent.ScrollCalendar -> {
                scrollCalendar(event.date)
            }
            is CalendarEvent.SmoothScrollCalendar -> {
                smoothScrollCalendar(event.date)
            }
            is CalendarEvent.UpdateCalendarDayDotVisibility -> {
                updateCalendarDayDotVisibility(event.date, event.isVisible)
            }
            is CalendarEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        mainActivity.navigateToStartTab()
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    private fun setUpCalendar() {
        val calendar = binding.calendar

        val daysOfWeek = createDayOfWeekList() // 曜日リスト取得
        configureCalendarBinders(daysOfWeek, themeColor)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60) //現在から過去5年分
        val endMonth = currentMonth.plusMonths(60) //現在から未来5年分
        calendar.setup(startMonth, endMonth, daysOfWeek[0])

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.selectedDate
                .collectLatest { value: LocalDate ->
                    val calendarMonthDayBinder =
                        binding.calendar.dayBinder as CalendarMonthDayBinder
                    calendarMonthDayBinder.updateSelectedDate(value)
                    binding.calendar.notifyDateChanged(value) // 今回選択日付更新
                    updateToolBarDate(value)
                    mainViewModel.onChangedSelectedDate(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.previousSelectedDate.filterNotNull()
                .collectLatest { value: LocalDate ->
                    binding.calendar.notifyDateChanged(value) // 前回選択日付更新
                }
        }
    }

    private fun createDayOfWeekList(): List<DayOfWeek> {
        val firstDayOfWeek = settingsViewModel.calendarStartDayOfWeek.requireValue()

        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        val firstDayOfWeekListPos = firstDayOfWeek.value
        // 開始曜日を先頭に並び替え
        val firstList =
            Arrays.stream(daysOfWeek)
                .skip((firstDayOfWeekListPos - 1).toLong())
                .collect(Collectors.toList())
        val secondList =
            Arrays.stream(daysOfWeek)
                .limit((firstDayOfWeekListPos - 1).toLong())
                .collect(Collectors.toList())
        return Stream
            .concat(firstList.stream(), secondList.stream())
            .collect(Collectors.toList())
    }

    // カレンダーBind設定
    private fun configureCalendarBinders(daysOfWeek: List<DayOfWeek>, themeColor: ThemeColor) {
        binding.calendar.dayBinder =
            CalendarMonthDayBinder(
                themeColor,
                { date: LocalDate -> mainViewModel.onCalendarDayClicked(date) },
                { date: LocalDate -> mainViewModel.onCalendarDayDotVisibilityCheck(date) }
            )

        val format = getString(R.string.fragment_calendar_month_header_format)
        binding.calendar.monthHeaderBinder =
            CalendarMonthHeaderFooterBinder(daysOfWeek, themeColor, format)
    }

    private class CalendarMonthDayBinder(
        private val themeColor: ThemeColor,
        private val onDateClick: (date: LocalDate) -> Unit,
        private val processDiaryExistCheck: (date: LocalDate) -> Unit
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
                    processDiaryExistCheck(localDate)
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
        private val themeColor: ThemeColor,
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

    private fun scrollCalendar(date: LocalDate) {
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val visibleMonth = binding.calendar.findFirstVisibleMonth()
        if (visibleMonth != null && targetYearMonth == visibleMonth.yearMonth) return

        Log.d("20250517", "scrollCalendar()")
        binding.calendar.scrollToMonth(targetYearMonth)
    }

    // カレンダーを指定した日付へ自動スクロール
    private fun smoothScrollCalendar(date: LocalDate) {
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val visibleMonth = binding.calendar.findFirstVisibleMonth()
        if (visibleMonth == null) {
            binding.calendar.scrollToMonth(targetYearMonth)
            return
        }
        if (targetYearMonth == visibleMonth.yearMonth) return

        val visibleYearMonth = visibleMonth.yearMonth

        // MEMO:カレンダーが今日の日付月から遠い月を表示していたらsmoothScrollの処理時間が延びるので、
        //      間にScroll処理を入れる。
        if (visibleYearMonth.isAfter(targetYearMonth)) {
            val firstSwitchPoint = visibleYearMonth.minusMonths(3)
            val secondSwitchPoint = targetYearMonth.plusMonths(3)
            if (firstSwitchPoint.isAfter(secondSwitchPoint)) {
                binding.calendar.monthScrollListener =
                    MonthLongScrollListener(
                        binding.calendar,
                        firstSwitchPoint,
                        secondSwitchPoint,
                        targetYearMonth
                    )
                binding.calendar.smoothScrollToMonth(firstSwitchPoint)
                return
            }
        } else {
            val firstSwitchPoint = visibleYearMonth.plusMonths(3)
            val secondSwitchPoint = targetYearMonth.minusMonths(3)
            if (firstSwitchPoint.isBefore(secondSwitchPoint)) {
                binding.calendar.monthScrollListener =
                    MonthLongScrollListener(
                        binding.calendar,
                        firstSwitchPoint,
                        secondSwitchPoint,
                        targetYearMonth
                    )
                binding.calendar.smoothScrollToMonth(firstSwitchPoint)
                return
            }
        }

        binding.calendar.monthScrollListener =
            MisalignedMonthScrollListener(binding.calendar, targetYearMonth)
        binding.calendar.smoothScrollToMonth(targetYearMonth)
    }

    private class MonthLongScrollListener(
        private val calendar: CalendarView,
        private val firstSwitchYearMonth: YearMonth,
        private val secondSwitchYearMonth: YearMonth,
        private val targetYearMonth: YearMonth
    ) : MonthScrollListener {
        override fun invoke(calendarMonth: CalendarMonth) {
            if (calendarMonth.yearMonth == firstSwitchYearMonth) {
                calendar.scrollToMonth(secondSwitchYearMonth)
                return
            }
            calendar.monthScrollListener =
                MisalignedMonthScrollListener(calendar, targetYearMonth)
            calendar.smoothScrollToMonth(targetYearMonth)
        }
    }

    // HACK:CalendarView#smoothScrollToMonth()を使用してカレンダーをスクロールさせる時、
    //      表示中カレンダー月と目的カレンダー月の差が5ケ月以上ある状態でsmoothScrollToMonth()を呼び出すと、
    //      まったく異なるカレンダー月へスクロールする不具合が発生する。
    //      この不具合はアプリ起動後、一度発生したら発生しなくなる。
    //      CalendarView#smoothScrollToMonth()、又はscrollToMonth()は、
    //      CalendarFragment#scrollCalendar()内でしか使用していない為、CalendarViewの不具合の可能性が考えられる。
    private class MisalignedMonthScrollListener(
        private val calendar: CalendarView,
        private val targetYearMonth: YearMonth
    ) : MonthScrollListener {

        val logTag = createLogTag()

        override fun invoke(calendarMonth: CalendarMonth) {
            Log.d(logTag, "MisalignedMonthScrollListener_calendarMonth: $calendarMonth")
            if (calendarMonth.yearMonth == targetYearMonth) {
                calendar.monthScrollListener = null
                return
            }
            calendar.scrollToMonth(targetYearMonth)
        }
    }

    private fun updateToolBarDate(date: LocalDate) {
        val dateString = date.toJapaneseDateString(requireContext())
        binding.materialToolbarTopAppBar.title = dateString
    }

    private fun setUpDiaryShow() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1
                .collectLatest { value: Weather ->
                    DiaryWeatherTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textWeather1Selected,
                            value
                        )
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: Weather ->
                    DiaryWeatherTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textWeather2Selected,
                            value
                        )
                }

        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.condition
                .collectLatest { value: Condition ->
                    DiaryConditionTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textConditionSelected,
                            value
                        )
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            // 項目レイアウト設定
            val itemLayouts =
                binding.run{
                    arrayOf(
                        includeDiaryShow.includeItem1.linerLayoutDiaryShowItem,
                        includeDiaryShow.includeItem2.linerLayoutDiaryShowItem,
                        includeDiaryShow.includeItem3.linerLayoutDiaryShowItem,
                        includeDiaryShow.includeItem4.linerLayoutDiaryShowItem,
                        includeDiaryShow.includeItem5.linerLayoutDiaryShowItem,
                    )
                }

            mainViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    DiaryItemsVisibilityUpdater()
                        .update(
                            itemLayouts,
                            value
                        )
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            // MEMO:添付画像がないときはnullとなり、デフォルト画像をセットする。
            //      nullの時ImageView自体は非表示となるためデフォルト画像をセットする意味はないが、
            //      クリアという意味合いでデフォルト画像をセットする。
            mainViewModel.imageUri
                .collectLatest { value: Uri? ->
                    DiaryImageUpdater()
                        .update(
                            themeColor,
                            binding.includeDiaryShow.imageAttachedImage,
                            value
                        )
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.log.filterNotNull()
                .collectLatest { value: LocalDateTime ->
                    DiaryLogTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textLogValue,
                            value
                        )
                }
        }

    }

    private fun setUpFloatActionButton() {
        binding.floatingActionButtonDiaryEdit.setOnClickListener {
            mainViewModel.onDiaryEditButtonClicked()
        }
    }

    private fun updateCalendarDayDotVisibility(date: LocalDate, isVisible: Boolean) {
        val calendarMonthDayBinder = binding.calendar.dayBinder as CalendarMonthDayBinder
        calendarMonthDayBinder.updateDayDotVisibilityCache(date, isVisible)
        binding.calendar.notifyDateChanged(date)
    }

    private fun navigateDiaryEditFragment(date: LocalDate, shouldLoadDiary: Boolean) {
        val directions =
            CalendarFragmentDirections.actionNavigationCalendarFragmentToDiaryEditFragment(
                shouldLoadDiary,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            CalendarFragmentDirections.actionCalendarFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }

    override fun onBottomNavigationItemReselected() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop()
            return
        }
        mainViewModel.onBottomNavigationItemReselected()
    }

    private fun scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0)
    }
}
