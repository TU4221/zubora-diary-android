package com.websarva.wings.android.zuboradiary.ui.calendar

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarDayBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarHeaderBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.requireValue
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.ConditionObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.LogObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.NumVisibleItemsObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.PicturePathObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.Weather1Observer
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.Weather2Observer
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream

@AndroidEntryPoint
class CalendarFragment : BaseFragment() {

    // View関係
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    private val calendarViewModel: CalendarViewModel by activityViewModels()

    // MEMO:CalendarFragment内にDiaryShowFragmentと同等のものを表示する為、DiaryShowViewModelを使用する。
    //      (CalendarViewModelにDiaryShowViewModelと重複するデータは持たせない)
    private val diaryShowViewModel: DiaryShowViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainActivity.popBackStackToStartFragment()
            }
        })
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentCalendarBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@CalendarFragment.viewLifecycleOwner
            diaryShowViewModel = this@CalendarFragment.diaryShowViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCalendar()
        setUpDiaryShow()
        setUpFloatActionButton()
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        val showedDiaryDate =
            receiveResulFromPreviousFragment<LocalDate>(DiaryShowFragment.KEY_SHOWED_DIARY_DATE)

        launchAndRepeatOnViewLifeCycleStarted {
            showedDiaryDate.collectLatest { value: LocalDate? ->
                value ?: return@collectLatest

                calendarViewModel.updateSelectedDate(value)
                removeResulFromFragment(DiaryShowFragment.KEY_SHOWED_DIARY_DATE)
            }
        }
    }

    override fun handleOnReceivingDialogResult() {
        // 処理なし
    }

    override fun removeDialogResultOnDestroy() {
        // 処理なし
    }

    override fun setUpOtherAppMessageDialog() {
        launchAndRepeatOnViewLifeCycleStarted {
            calendarViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(calendarViewModel).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.appMessageBufferList
                .collectLatest { value: AppMessageList ->
                    AppMessageBufferListObserver(diaryShowViewModel).onChanged(value)
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
            calendarViewModel.selectedDate
                .collectLatest { value: LocalDate ->
                    binding.calendar.notifyDateChanged(value) // 今回選択日付更新
                    scrollCalendar(value)
                    updateToolBarDate(value)
                    showSelectedDiary(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            calendarViewModel.previousSelectedDate
                .collectLatest { value: LocalDate? ->
                    // MEMO:一度も日付選択をしていない場合はnullが代入されている。
                    if (value == null) return@collectLatest

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
        binding.calendar.dayBinder = CalendarMonthDayBinder(themeColor)
        binding.calendar.monthHeaderBinder =
            CalendarMonthHeaderFooterBinder(daysOfWeek, themeColor)
    }

    private inner class CalendarMonthDayBinder(private val themeColor: ThemeColor) :
        MonthDayBinder<DayViewContainer> {

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: DayViewContainer, calendarDay: CalendarDay) {
            val textDay = container.binding.textDay.apply {
                setOnClickListener {
                    if (calendarDay.position == DayPosition.MonthDate) {
                        calendarViewModel.updateSelectedDate(calendarDay.date)
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
            val themeColorSwitcher =
                CalendarThemeColorSwitcher(requireContext(), themeColor)

            val selectedDate = calendarViewModel.selectedDate.value
            val isSelectedDay = calendarDay.date.isEqual(selectedDate)
            val isToday = calendarDay.date.isEqual(LocalDate.now())

            if (isSelectedDay) {
                themeColorSwitcher.switchCalendarSelectedDayColor(
                    textCalendarDay,
                    viewCalendarDayDot
                )
            } else if (isToday) {
                themeColorSwitcher.switchCalendarTodayColor(textCalendarDay, viewCalendarDayDot)
            } else {
                val dayOfWeek = calendarDay.date.dayOfWeek
                val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
                val isSunday = dayOfWeek == DayOfWeek.SUNDAY

                if (isSaturday) {
                    themeColorSwitcher.switchCalendarSaturdayColor(
                        textCalendarDay,
                        viewCalendarDayDot
                    )
                } else if (isSunday) {
                    themeColorSwitcher.switchCalendarSundayColor(
                        textCalendarDay,
                        viewCalendarDayDot
                    )
                } else {
                    themeColorSwitcher.switchCalendarWeekdaysColor(
                        textCalendarDay,
                        viewCalendarDayDot
                    )
                }
            }
        }

        private fun setUpCalendarDayDotVisibility(calendarDay: CalendarDay, viewCalendarDayDot: View) {
            val localDate = calendarDay.date

            lifecycleScope.launch(Dispatchers.IO) {
                val exists = calendarViewModel.existsSavedDiary(localDate)
                withContext(Dispatchers.Main) {
                    if (exists == true) {
                        viewCalendarDayDot.visibility = View.VISIBLE
                    } else {
                        viewCalendarDayDot.visibility = View.INVISIBLE
                    }
                }
            }
        }

        override fun create(view: View): DayViewContainer {
            return DayViewContainer(view)
        }
    }

    private inner class CalendarMonthHeaderFooterBinder(
        private val daysOfWeek: List<DayOfWeek>,
        private val themeColor: ThemeColor
    ) : MonthHeaderFooterBinder<MonthViewContainer> {

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: MonthViewContainer, calendarMonth: CalendarMonth) {
            // カレンダーの年月表示設定
            val format = getString(R.string.fragment_calendar_month_header_format)
            val formatter = DateTimeFormatter.ofPattern(format)
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
            val themeColorSwitcher =
                CalendarThemeColorSwitcher(requireContext(), themeColor)

            val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
            val isSunday = dayOfWeek == DayOfWeek.SUNDAY

            if (isSaturday) {
                themeColorSwitcher.switchCalendarDayOfWeekSaturdayColor(dayOfWeekText)
            } else if (isSunday) {
                themeColorSwitcher.switchCalendarDayOfWeekSundayColor(dayOfWeekText)
            } else {
                themeColorSwitcher.switchCalendarDayOfWeekWeekdaysColor(dayOfWeekText)
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

    // カレンダーを指定した日付へ自動スクロール
    private fun scrollCalendar(date: LocalDate) {
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val currentMonth = binding.calendar.findFirstVisibleMonth()
        if (currentMonth == null) {
            binding.calendar.scrollToMonth(targetYearMonth)
            return
        }

        val currentYearMonth = currentMonth.yearMonth

        // MEMO:カレンダーが今日の日付月から遠い月を表示していたらsmoothScrollの処理時間が延びるので、
        //      間にScroll処理を入れる。
        if (currentYearMonth.isAfter(targetYearMonth)) {
            val firstSwitchPoint = currentYearMonth.minusMonths(3)
            val secondSwitchPoint = targetYearMonth.plusMonths(6)
            if (currentYearMonth.isAfter(secondSwitchPoint)) {
                binding.calendar.smoothScrollToMonth(firstSwitchPoint)
                binding.calendar.scrollToMonth(secondSwitchPoint)
            }
        } else {
            val firstSwitchPoint = currentYearMonth.plusMonths(3)
            val secondSwitchPoint = targetYearMonth.minusMonths(6)
            if (currentYearMonth.isBefore(secondSwitchPoint)) {
                binding.calendar.smoothScrollToMonth(firstSwitchPoint)
                binding.calendar.scrollToMonth(secondSwitchPoint)
            }
        }
        binding.calendar.smoothScrollToMonth(targetYearMonth)
    }

    private fun updateToolBarDate(date: LocalDate) {
        val dateTimeStringConverter = DateTimeStringConverter()
        val stringDate = dateTimeStringConverter.toYearMonthDayWeek(date)
        binding.materialToolbarTopAppBar.title = stringDate
    }

    // CalendarViewで選択された日付の日記を表示
    private fun showSelectedDiary(date: LocalDate) {
        lifecycleScope.launch(Dispatchers.IO) {
            val exists = calendarViewModel.existsSavedDiary(date)
            withContext(Dispatchers.Main) {
                if (exists == true) {
                    showDiary(date)
                } else {
                    closeDiary()
                }
            }
        }
    }

    private fun showDiary(date: LocalDate) {
        diaryShowViewModel.initialize()
        lifecycleScope.launch(Dispatchers.IO) {
            val isSuccessful = diaryShowViewModel.loadSavedDiary(date)
            withContext(Dispatchers.Main) {
                if (isSuccessful) {
                    binding.apply {
                        frameLayoutDiaryShow.visibility = View.VISIBLE
                        textNoDiaryMessage.visibility = View.GONE
                    }
                } else {
                    closeDiary()
                }
            }
        }
    }

    private fun closeDiary() {
        binding.apply {
            frameLayoutDiaryShow.visibility = View.GONE
            textNoDiaryMessage.visibility = View.VISIBLE
        }
        diaryShowViewModel.initialize()
    }

    private fun setUpDiaryShow() {

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.weather1
                .collectLatest { value: Weather ->
                    Weather1Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeather1Selected
                    ).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.weather2
                .collectLatest { value: Weather ->
                    Weather2Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeatherSlush,
                        binding.includeDiaryShow.textWeather2Selected
                    ).onChanged(value)
                }

        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.condition
                .collectLatest { value: Condition ->
                    ConditionObserver(
                        requireContext(),
                        binding.includeDiaryShow.textConditionSelected
                    ).onChanged(value)
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

            diaryShowViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    NumVisibleItemsObserver(itemLayouts).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver(
                        requireContext(),
                        themeColor,
                        binding.includeDiaryShow.textAttachedPicture,
                        binding.includeDiaryShow.imageAttachedPicture
                    ).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            diaryShowViewModel.log
                .collectLatest { value: LocalDateTime? ->
                    LogObserver(binding.includeDiaryShow.textLogValue).onChanged(value)
                }
        }

    }

    private fun setUpFloatActionButton() {
        binding.floatingActionButtonDiaryEdit.setOnClickListener {
            val selectedDate = calendarViewModel.selectedDate.value
            showDiaryEditFragment(selectedDate)
        }
    }

    // 選択中ボトムナビゲーションタブを再選択時の処理
    fun processOnReselectNavigationItem() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop()
            return
        }
        // MEMO:StateFlowに現在値と同じ値を代入してもCollectメソッドに登録した処理が起動しないため、
        //      下記条件でカレンダースクロールのみ処理。
        if (calendarViewModel.selectedDate.value == LocalDate.now()) {
            scrollCalendar(LocalDate.now())
        } else {
            calendarViewModel.updateSelectedDate(LocalDate.now())
        }
    }

    // 先頭へ自動スクロール
    private fun scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0)
    }

    @MainThread
    private fun showDiaryEditFragment(date: LocalDate) {
        if (isDialogShowing) return

        val directions =
            CalendarFragmentDirections
                .actionNavigationCalendarFragmentToDiaryEditFragment(
                    true,
                    true,
                    date
                )
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            CalendarFragmentDirections
                .actionCalendarFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun retryOtherAppMessageDialogShow() {
        calendarViewModel.triggerAppMessageBufferListObserver()
        diaryShowViewModel.triggerAppMessageBufferListObserver()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
