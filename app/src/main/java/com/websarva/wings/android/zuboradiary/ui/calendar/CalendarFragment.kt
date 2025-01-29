package com.websarva.wings.android.zuboradiary.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import com.google.common.util.concurrent.FutureCallback
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarDayBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarHeaderBinding
import com.websarva.wings.android.zuboradiary.ui.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.ConditionObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.LogObserver
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.Weather1Observer
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowFragment.Weather2Observer
import com.websarva.wings.android.zuboradiary.ui.diary.diaryshow.DiaryShowViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream

@AndroidEntryPoint
class CalendarFragment : BaseFragment() {

    // View関係
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private lateinit var calendarViewModel: CalendarViewModel

    // MEMO:CalendarFragment内にDiaryShowFragmentと同等のものを表示する為、DiaryShowViewModelを使用する。
    //      (CalendarViewModelにDiaryShowViewModelと重複するデータは持たせない)
    private lateinit var diaryShowViewModel: DiaryShowViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireMainActivity().popBackStackToStartFragment()
            }
        })
    }

    override fun initializeViewModel() {
        val provider = ViewModelProvider(requireActivity())
        calendarViewModel = provider[CalendarViewModel::class.java]
        diaryShowViewModel = provider[DiaryShowViewModel::class.java]
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentCalendarBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@CalendarFragment
            diaryShowViewModel = diaryShowViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCalendar()
        setUpDiaryShow()
        setUpFloatActionButton()
    }

    override fun handleOnReceivingResultFromPreviousFragment(savedStateHandle: SavedStateHandle) {
        val showedDiaryDateLiveData =
            savedStateHandle.getLiveData<LocalDate>(DiaryShowFragment.KEY_SHOWED_DIARY_DATE)
        showedDiaryDateLiveData.observe(viewLifecycleOwner) { localDate: LocalDate ->
            calendarViewModel.updateSelectedDate(localDate)
            savedStateHandle.remove<Any>(DiaryShowFragment.KEY_SHOWED_DIARY_DATE)
        }
    }

    override fun handleOnReceivingDialogResult(savedStateHandle: SavedStateHandle) {
        retryOtherAppMessageDialogShow()
    }

    override fun removeDialogResultOnDestroy(savedStateHandle: SavedStateHandle) {
        // 処理なし
    }

    override fun setUpOtherAppMessageDialog() {
        calendarViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(calendarViewModel))
        diaryShowViewModel.appMessageBufferList
            .observe(viewLifecycleOwner, AppMessageBufferListObserver(diaryShowViewModel))
    }

    private fun setUpCalendar() {
        val calendar = binding.calendar

        val daysOfWeek = createDayOfWeekList() // 曜日リスト取得
        configureCalendarBinders(daysOfWeek, requireThemeColor())

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60) //現在から過去5年分
        val endMonth = currentMonth.plusMonths(60) //現在から未来5年分
        calendar.setup(startMonth, endMonth, daysOfWeek[0])

        val selectedDate = calendarViewModel.selectedDateLiveData.value
        calendarViewModel.updateSelectedDate(selectedDate)

        calendarViewModel.selectedDateLiveData
            .observe(viewLifecycleOwner) { localDate: LocalDate ->
                binding.calendar.notifyDateChanged(localDate) // 今回選択日付更新
                scrollCalendar(localDate)
                updateToolBarDate(localDate)
                showSelectedDiary(localDate)
            }

        calendarViewModel.previousSelectedDateLiveData
            .observe(viewLifecycleOwner) { localDate: LocalDate? ->
                // MEMO:一度も日付選択をしていない場合はnullが代入されている。
                if (localDate == null) return@observe

                binding.calendar.notifyDateChanged(localDate) // 前回選択日付更新
            }
    }

    private fun createDayOfWeekList(): List<DayOfWeek> {
        val firstDayOfWeek = settingsViewModel.loadCalendarStartDaySettingValue()

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

        fun setUpCalendarDayColor(
            calendarDay: CalendarDay, textCalendarDay: TextView, viewCalendarDayDot: View
        ) {
            val themeColorSwitcher =
                CalendarThemeColorSwitcher(requireContext(), themeColor)

            val selectedDate = calendarViewModel.selectedDateLiveData.value
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

        fun setUpCalendarDayDotVisibility(calendarDay: CalendarDay, viewCalendarDayDot: View) {
            val localDate = calendarDay.date
            calendarViewModel.existsSavedDiary(localDate, object : FutureCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    if (result) {
                        viewCalendarDayDot.visibility = View.VISIBLE
                    } else {
                        viewCalendarDayDot.visibility = View.INVISIBLE
                    }
                }

                override fun onFailure(t: Throwable) {
                    // 例外はViewModelクラス内で例外用リスナーを追加して対応
                    viewCalendarDayDot.visibility = View.INVISIBLE
                }
            })
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

        fun setUpDayOfWeekColor(dayOfWeek: DayOfWeek, dayOfWeekText: TextView) {
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
        calendarViewModel.existsSavedDiary(date, DiaryShowFutureCallback(date))
    }

    private inner class DiaryShowFutureCallback(private val date: LocalDate) : FutureCallback<Boolean> {

        override fun onSuccess(result: Boolean) {
            if (result) {
                showDiary()
            } else {
                closeDiary()
            }
        }

        override fun onFailure(t: Throwable) {
            // 例外はViewModelクラス内で例外用リスナーを追加して対応
            closeDiary()
        }

        fun showDiary() {
            diaryShowViewModel.initialize()
            diaryShowViewModel.loadSavedDiary(date)
            binding.frameLayoutDiaryShow.visibility = View.VISIBLE
            binding.textNoDiaryMessage.visibility = View.GONE
        }

        fun closeDiary() {
            binding.frameLayoutDiaryShow.visibility = View.GONE
            binding.textNoDiaryMessage.visibility = View.VISIBLE
            diaryShowViewModel.initialize()
        }
    }

    private fun setUpDiaryShow() {
        diaryShowViewModel.weather1LiveData
            .observe(
                viewLifecycleOwner,
                Weather1Observer(
                    requireContext(),
                    binding.includeDiaryShow.textWeather1Selected
                )
            )

        diaryShowViewModel.weather2LiveData
            .observe(
                viewLifecycleOwner,
                Weather2Observer(
                    requireContext(),
                    binding.includeDiaryShow.textWeatherSlush,
                    binding.includeDiaryShow.textWeather2Selected
                )
            )

        diaryShowViewModel.conditionLiveData
            .observe(
                viewLifecycleOwner,
                ConditionObserver(
                    requireContext(),
                    binding.includeDiaryShow.textConditionSelected
                )
            )

        // 項目レイアウト設定
        val itemLayouts = arrayOfNulls<View>(DiaryLiveData.MAX_ITEMS)
        itemLayouts[0] = binding.includeDiaryShow.includeItem1.linerLayoutDiaryShowItem
        itemLayouts[1] = binding.includeDiaryShow.includeItem2.linerLayoutDiaryShowItem
        itemLayouts[2] = binding.includeDiaryShow.includeItem3.linerLayoutDiaryShowItem
        itemLayouts[3] = binding.includeDiaryShow.includeItem4.linerLayoutDiaryShowItem
        itemLayouts[4] = binding.includeDiaryShow.includeItem5.linerLayoutDiaryShowItem
        diaryShowViewModel.numVisibleItemsLiveData
            .observe(viewLifecycleOwner, DiaryShowFragment.NumVisibleItemsObserver(itemLayouts))

        diaryShowViewModel.picturePathLiveData
            .observe(
                viewLifecycleOwner,
                DiaryShowFragment.PicturePathObserver(
                    requireContext(),
                    requireThemeColor(),
                    binding.includeDiaryShow.textAttachedPicture,
                    binding.includeDiaryShow.imageAttachedPicture
                )
            )

        diaryShowViewModel.logLiveData
            .observe(
                viewLifecycleOwner,
                LogObserver(binding.includeDiaryShow.textLogValue)
            )
    }

    private fun setUpFloatActionButton() {
        binding.floatingActionButtonDiaryEdit.setOnClickListener {
            val selectedDate = requireNotNull(calendarViewModel.selectedDateLiveData.value)
            showDiaryEditFragment(selectedDate)
        }
    }

    // 選択中ボトムナビゲーションタブを再選択時の処理
    fun processOnReselectNavigationItem() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop()
        } else {
            calendarViewModel.updateSelectedDate(LocalDate.now())
        }
    }

    // 先頭へ自動スクロール
    private fun scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0)
    }

    private fun showDiaryEditFragment(date: LocalDate) {
        if (isDialogShowing()) return

        val action: NavDirections =
            CalendarFragmentDirections
                .actionNavigationCalendarFragmentToDiaryEditFragment(
                    true,
                    true,
                    date
                )
        navController.navigate(action)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val action: NavDirections =
            CalendarFragmentDirections
                .actionCalendarFragmentToAppMessageDialog(appMessage)
        navController.navigate(action)
    }

    override fun retryOtherAppMessageDialogShow() {
        calendarViewModel.triggerAppMessageBufferListObserver()
        diaryShowViewModel.triggerAppMessageBufferListObserver()
    }

    override fun destroyBinding() {
        _binding = null
    }
}
