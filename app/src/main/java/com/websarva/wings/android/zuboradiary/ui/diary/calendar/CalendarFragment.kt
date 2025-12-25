package com.websarva.wings.android.zuboradiary.ui.diary.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.FragmentCalendarBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarDayBinding
import com.websarva.wings.android.zuboradiary.databinding.LayoutCalendarHeaderBinding
import com.websarva.wings.android.zuboradiary.ui.common.fragment.BaseFragment
import com.websarva.wings.android.zuboradiary.ui.common.fragment.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.common.fragment.RequiresBottomNavigation
import com.websarva.wings.android.zuboradiary.ui.common.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.diary.edit.DiaryEditScreenParams
import com.websarva.wings.android.zuboradiary.ui.common.navigation.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.diary.common.navigation.DiaryFlowLaunchSource
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

/**
 * カレンダー表示と選択された日付の日記を表示するフラグメント。
 *
 * 以下の責務を持つ:
 * - カレンダーUIの表示と更新
 * - 日付の選択と、選択された日付に対応する日記の表示
 * - 新しい日記を作成するための画面遷移
 */
@AndroidEntryPoint
class CalendarFragment :
    BaseFragment<FragmentCalendarBinding, CalendarUiEvent, CalendarNavDestination, DummyNavBackDestination>(),
    RequiresBottomNavigation,
    ActivityCallbackUiEventHandler {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: CalendarViewModel by activityViewModels()

    override val destinationId = R.id.navigation_calendar_fragment

    override val resultKey: String? get() = null
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、カレンダービューの初期設定を行う。*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
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

    /** カレンダーのBinderとListenerもnullに設定する。 */
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
    override fun setupFragmentResultObservers() {
        observeDiaryFragmentResult()
    }

    /** 日記表示・編集画面からの結果を監視する。 */
    private fun observeDiaryFragmentResult() {
        observeFragmentResult(
            RESULT_KEY_DIARY
        ) { result ->
            when (result) {
                is FragmentResult.Some -> {
                    mainViewModel.onDiaryFragmentResultReceived(result.data)
                }
                is FragmentResult.None -> { /*処理なし*/ }
            }
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.ScrollCalendar -> {
                scrollCalendar(event.date)
            }
            is CalendarUiEvent.SmoothScrollCalendar -> {
                smoothScrollCalendar(event.date)
            }
        }
    }

    override fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent) {
        when (event) {
            ActivityCallbackUiEvent.ProcessOnBottomNavigationItemReselect -> {
                onBottomNavigationItemReselected()
            }
        }
    }

    override fun setupUiStateObservers() {
        super.setupUiStateObservers()

        observeSelectedDate()
        observeCalendarStartDayOfWeek()
        observeExistingDiaryDates()
    }

    override fun setupUiEventObservers() {
        super.setupUiEventObservers()

        observeUiEventFromActivity()
    }

    /** 選択日付の変更を監視し、カレンダーのUIを更新する。 */
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

    /** 週の開始曜日の変更を監視し、カレンダーを再描画する。 */
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

    /** 日記が存在する日付の変更を監視し、カレンダーを再描画する。 */
    private fun observeExistingDiaryDates() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.existingDiaryDates == new.existingDiaryDates
            }.map {
                it.existingDiaryDates
            }.collect {
                updateCalendarDayDotVisibility(it)
            }
        }
    }

    /** ActivityからのUIイベントを監視する。 */
    private fun observeUiEventFromActivity() {
        fragmentHelper.observeActivityUiEvent(
            this,
            mainActivityViewModel,
            this
        )
    }
    //endregion

    //region View Control
    /** BottomNavigationViewの同じタブが再選択されたときに呼び出される。 */
    private fun onBottomNavigationItemReselected() {
        if (binding.nestedScrollFullScreen.canScrollVertically(-1)) {
            scrollToTop()
            return
        }
        mainViewModel.onBottomNavigationItemReselect()
    }

    /** 画面の最上部までスムーズにスクロールする。 */
    private fun scrollToTop() {
        binding.nestedScrollFullScreen.smoothScrollTo(0, 0)
    }
    //endregion

    //region Calendar View - Setup
    /**
     * カレンダービューの初期設定を行う。
     * 表示範囲、曜日のリスト、各種Binderを設定する。
     */
    private fun setupCalendar() {
        val calendar = binding.calendar

        val daysOfWeek = createDayOfWeekList() // 曜日リスト取得
        configureCalendarBinders(daysOfWeek, themeColor)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60) //現在から過去5年分
        val endMonth = currentMonth.plusMonths(60) //現在から未来5年分
        calendar.setup(startMonth, endMonth, daysOfWeek[0])
    }

    /**
     * 設定に基づいた週の開始曜日が先頭に来るようにソートされた曜日のリストを生成する。
     * @return ソートされた[DayOfWeek]のリスト
     */
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

    /**
     * カレンダーのヘッダーと日付セルのBinderを設定する。
     * @param daysOfWeek 表示する曜日のリスト
     * @param themeColor 現在のテーマカラー
     */
    private fun configureCalendarBinders(daysOfWeek: List<DayOfWeek>, themeColor: ThemeColorUi) {
        with (binding.calendar) {
            val format = getString(R.string.fragment_calendar_month_header_format)
            monthHeaderBinder =
                CalendarMonthHeaderFooterBinder(daysOfWeek, themeColor, format)

            dayBinder =
                CalendarMonthDayBinder(themeColor) { date: LocalDate ->
                    mainViewModel.onCalendarDayClick(date)
                }
        }
    }

    /**
     * カレンダーの月ヘッダーの描画を担うBinder。
     * @property daysOfWeek 表示する曜日のリスト
     * @property themeColor 現在のテーマカラー
     * @property headerDateFormat 年月を表示するためのフォーマット文字列
     */
    private class CalendarMonthHeaderFooterBinder(
        private val daysOfWeek: List<DayOfWeek>,
        private val themeColor: ThemeColorUi,
        private val headerDateFormat: String
    ) : MonthHeaderFooterBinder<MonthViewContainer> {

        /** 月ヘッダービューがバインドされるときに呼び出される。 */
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: MonthViewContainer, calendarMonth: CalendarMonth) {
            container.bind(
                calendarMonth.yearMonth,
                daysOfWeek,
                headerDateFormat,
                themeColor
            )
        }

        /** 月ヘッダーのViewContainerを生成する。 */
        override fun create(view: View): MonthViewContainer {
            return MonthViewContainer(view)
        }
    }

    /**
     * カレンダーの月ヘッダーのビューを保持するコンテナ。
     * @param view コンテナのルートビュー
     */
    private class MonthViewContainer(view: View) : ViewContainer(view) {

        private val binding: LayoutCalendarHeaderBinding = LayoutCalendarHeaderBinding.bind(view)

        private val themeColorChanger = CalendarThemeColorChanger()

        /**
         * ヘッダービューに年月と曜日を描画する。
         * @param yearMonth 表示する年月
         * @param daysOfWeek 表示する曜日のリスト
         * @param headerDateFormat 年月を表示するためのフォーマット
         * @param themeColor 現在のテーマカラー
         */
        fun bind(
            yearMonth: YearMonth,
            daysOfWeek: List<DayOfWeek>,
            headerDateFormat: String,
            themeColor: ThemeColorUi
        ) {
            // カレンダーの年月表示設定
            val formatter = DateTimeFormatter.ofPattern(headerDateFormat)
            binding.textYearMonth.text = yearMonth.format(formatter)

            // カレンダーの曜日設定 (一度だけ設定する)
            val legendLayout = binding.legendLayout.root
            if (legendLayout.tag == yearMonth) return
            legendLayout.tag = yearMonth

            // カレンダー曜日表示と色の設定
            val max = legendLayout.childCount
            for (i in 0 until max) {
                val childView = legendLayout.getChildAt(i)
                val childTextView = childView as TextView
                val dayOfWeek = daysOfWeek[i]

                childTextView.text = dayOfWeek.name.substring(0, 3)

                // 曜日の色設定
                setupDayOfWeekColor(dayOfWeek, childTextView, themeColor)
            }
        }

        /**
         * 曜日に応じてテキストの色を設定する。
         * @param dayOfWeek 対象の曜日
         * @param dayOfWeekText 色を設定するTextView
         * @param themeColor 現在のテーマカラー
         */
        private fun setupDayOfWeekColor(
            dayOfWeek: DayOfWeek,
            dayOfWeekText: TextView,
            themeColor: ThemeColorUi
        ) {
            when (dayOfWeek) {
                DayOfWeek.SATURDAY -> {
                    themeColorChanger.applyCalendarDayOfWeekSaturdayColor(dayOfWeekText)
                }
                DayOfWeek.SUNDAY -> {
                    themeColorChanger.applyCalendarDayOfWeekSundayColor(dayOfWeekText)
                }
                else -> {
                    themeColorChanger
                        .applyCalendarDayOfWeekWeekdaysColor(dayOfWeekText, themeColor)
                }
            }
        }
    }

    /**
     * カレンダーの日付セルの描画を担うBinder。
     * @property themeColor 現在のテーマカラー
     * @property onDateClick 日付クリック時のコールバック
     */
    private class CalendarMonthDayBinder(
        private val themeColor: ThemeColorUi,
        private val onDateClick: (date: LocalDate) -> Unit
    ) : MonthDayBinder<DayViewContainer> {

        /** 現在選択されている日付。 */
        private var selectedDate: LocalDate = LocalDate.now()

        /** ドットを表示する日付。 */
        private var datesWithDots: Set<LocalDate> = emptySet()

        /** 日付セルのViewContainerを生成する。 */
        override fun create(view: View): DayViewContainer {
            return DayViewContainer(view)
        }

        /** 日付セルビューがバインドされるときに呼び出される。 */
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(container: DayViewContainer, calendarDay: CalendarDay) {
            container.view.setOnClickListener {
                if (calendarDay.position == DayPosition.MonthDate) {
                    onDateClick(calendarDay.date)
                }
            }

            val dayState =
                when {
                    calendarDay.date.isEqual(selectedDate) -> DayViewContainer.DayState.SELECTED
                    calendarDay.date.isEqual(LocalDate.now()) -> DayViewContainer.DayState.TODAY
                    calendarDay.date.dayOfWeek == DayOfWeek.SATURDAY -> DayViewContainer.DayState.SATURDAY
                    calendarDay.date.dayOfWeek == DayOfWeek.SUNDAY -> DayViewContainer.DayState.SUNDAY
                    else -> DayViewContainer.DayState.WEEKDAY
                }

            val dotIsVisible = datesWithDots.contains(calendarDay.date)

            container.bind(
                calendarDay,
                dayState,
                dotIsVisible,
                themeColor
            )
        }

        /** 選択されている日付を更新する。 */
        fun updateSelectedDate(date: LocalDate) {
            selectedDate = date
        }

        /** ドットを表示する日付を更新する。 */
        fun updateDatesWithDots(dates: Set<LocalDate>) {
            datesWithDots = dates
        }
    }

    /**
     * カレンダーの日付セルのビューを保持するコンテナ。
     * @param view コンテナのルートビュー
     */
    private class DayViewContainer(view: View) : ViewContainer(view) {

        private val binding: LayoutCalendarDayBinding = LayoutCalendarDayBinding.bind(view)

        private val themeColorChanger = CalendarThemeColorChanger()

        /**
         * 日付セルビューに日付とドットを描画し、状態に応じてスタイルを適用する。
         * @param calendarDay 描画する日付の情報
         * @param dayState 日付の状態（選択、今日など）
         * @param dotIsVisible ドットを表示するかどうか
         * @param themeColor 現在のテーマカラー
         */
        fun bind(
            calendarDay: CalendarDay,
            dayState: DayState,
            dotIsVisible: Boolean,
            themeColor: ThemeColorUi
        ) {
            val textDay = binding.textDay
            val viewDayDot = binding.viewDayDot

            textDay.text = calendarDay.date.dayOfMonth.toString()

            // 日にちマス状態(可視、数値色、背景色、ドット有無)設定
            if (calendarDay.position == DayPosition.MonthDate) {
                textDay.visibility = View.VISIBLE
                viewDayDot.visibility = if (dotIsVisible) View.VISIBLE else View.INVISIBLE

                when (dayState) {
                    DayState.SELECTED -> {
                        themeColorChanger
                            .applyCalendarSelectedDayColor(textDay, viewDayDot, themeColor)
                    }
                    DayState.TODAY -> {
                        themeColorChanger
                            .applyCalendarTodayColor(textDay, viewDayDot, themeColor)
                    }
                    DayState.SATURDAY -> {
                        themeColorChanger
                            .applyCalendarSaturdayColor(textDay, viewDayDot, themeColor)
                    }
                    DayState.SUNDAY -> {
                        themeColorChanger
                            .applyCalendarSundayColor(textDay, viewDayDot, themeColor)
                    }
                    DayState.WEEKDAY -> {
                        themeColorChanger
                            .applyCalendarWeekdaysColor(textDay, viewDayDot, themeColor)
                    }
                }
            } else {
                textDay.visibility = View.INVISIBLE
                viewDayDot.visibility = View.INVISIBLE
                (textDay.parent as? View)?.background = null
            }
        }

        /** 日付セルの状態を表すenum。 */
        enum class DayState {
            SELECTED,
            TODAY,
            SATURDAY,
            SUNDAY,
            WEEKDAY
        }
    }
    //endregion

    //region Calendar View Control
    /**
     * 指定された日付が含まれる月にカレンダーをスクロールする。
     * @param date スクロール先のターゲット日付
     */
    private fun scrollCalendar(date: LocalDate) {
        val calendar = binding.calendar
        val targetYearMonth = YearMonth.of(date.year, date.monthValue)
        val currentMonth = calendar.findFirstVisibleMonth()?.yearMonth
        if (targetYearMonth == currentMonth) return

        binding.calendar.scrollToMonth(targetYearMonth)
    }

    /**
     * 指定された日付が含まれる月にカレンダーをスムーズにスクロールする。
     * 月が3ヶ月以上離れている場合は、アニメーションなしで即座にスクロールする。
     * @param date スクロール先のターゲット日付
     */
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

    /**
     * カレンダーの日付のドットのUIを更新する。
     * @param dates ドットを表示する日付
     */
    private fun updateCalendarDayDotVisibility(dates: Set<LocalDate>) {
        val calendar = binding.calendar
        val calendarMonthDayBinder = calendar.dayBinder as CalendarMonthDayBinder
        calendarMonthDayBinder.updateDatesWithDots(dates)
        calendar.notifyCalendarChanged()
    }

    /**
     * 選択された日付のUIを更新する。
     * @param selectedDate 新しく選択された日付
     * @param previousSelectedDate 以前に選択されていた日付
     */
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
    override fun toNavDirections(destination: CalendarNavDestination): NavDirections {
        return when (destination) {
            is CalendarNavDestination.AppMessageDialog -> {
                navigationEventHelper.createAppMessageDialogNavDirections(destination.message)
            }
            is CalendarNavDestination.DiaryEditScreen -> {
                createDiaryEditFragmentNavDirections(destination.id, destination.date)
            }
        }
    }

    override fun toNavDestinationId(destination: DummyNavBackDestination): Int {
        // 処理なし
        throw IllegalStateException("NavDestinationIdへの変換は不要の為、未対応。")
    }

    /**
     * 日記編集画面へ遷移する為の [NavDirections] オブジェクトを生成する。
     * @param id 編集する日記のID（新規作成の場合はnull）
     * @param date 対象の日付
     *  */
    private fun createDiaryEditFragmentNavDirections(id: String?, date: LocalDate): NavDirections {
        val params = DiaryEditScreenParams(
            RESULT_KEY_DIARY,
            id,
            date,
            DiaryFlowLaunchSource.Calendar
        )
        return CalendarFragmentDirections
                .actionNavigationCalendarFragmentToDiaryEditFragment(params)
    }
    //endregion

    internal companion object {
        /** 日記表示・編集画面からの遷移戻り時に、結果データを受け取るためのリクエストキー。 */
        private const val RESULT_KEY_DIARY = "diary_result"
    }
}
