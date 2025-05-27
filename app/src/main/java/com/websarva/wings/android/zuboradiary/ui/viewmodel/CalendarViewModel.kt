package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.action.CalendarFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    private val initialCalendarState = CalendarState.Idle
    private val _calendarState = MutableStateFlow<CalendarState>(initialCalendarState)
    val calendarState get() = _calendarState.asStateFlow()

    private val initialSelectedDate = LocalDate.now()
    private val _selectedDate = MutableStateFlow<LocalDate>(initialSelectedDate)
    val selectedDate get() = _selectedDate.asStateFlow()

    private val initialPreviousSelectedDate = null
    private val _previousSelectedDate = MutableStateFlow<LocalDate?>(initialPreviousSelectedDate)
    val previousSelectedDate get() = _previousSelectedDate.asStateFlow()

    private var shouldSmoothScroll = false

    // Fragment処理
    private val _fragmentAction = MutableSharedFlow<FragmentAction>()
    val fragmentAction
        get() = _fragmentAction.asSharedFlow()

    override fun initialize() {
        super.initialize()
        _calendarState.value = initialCalendarState
        _selectedDate.value = initialSelectedDate
        _previousSelectedDate.value = initialPreviousSelectedDate
    }

    // ViewClicked処理
    fun onCalendarDayClicked(date: LocalDate) {
        updateSelectedDate(date)
    }

    fun onDiaryEditButtonClicked() {
        viewModelScope.launch {
            navigateDiaryEditFragment()
        }
    }

    fun onNavigationItemReselected() {
        viewModelScope.launch {
            // MEMO:StateFlowに現在値と同じ値を代入してもCollectメソッドに登録した処理が起動しないため、
            //      下記条件でカレンダースクロールのみ処理。
            val selectedDate = _selectedDate.value
            val today = LocalDate.now()
            if (selectedDate == today) {
                _fragmentAction.emit(
                    CalendarFragmentAction.SmoothScrollCalendar(today)
                )
            }
            shouldSmoothScroll = true
            updateSelectedDate(today)
        }
    }

    // 他Fragmentからの受取処理
    fun onDataReceivedFromDiaryShowFragment(date: LocalDate) {
        updateSelectedDate(date)
    }

    fun onDataReceivedFromDiaryEditFragment(date: LocalDate) {
        updateSelectedDate(date)
    }

    // StateFlow値変更時処理
    fun onChangedSelectedDate() {
        viewModelScope.launch {
            prepareDiary()
        }
    }

    // View変更処理
    private suspend fun prepareDiary() {
        val date = _selectedDate.value
        val action =
            if (shouldSmoothScroll) {
                shouldSmoothScroll = false
                CalendarFragmentAction.SmoothScrollCalendar(date)
            } else {
                CalendarFragmentAction.ScrollCalendar(date)
            }
        _fragmentAction.emit(action)

        val exists = existsSavedDiary(date) ?: false
        if (exists) {
            _calendarState.value = CalendarState.DiaryVisible
            _fragmentAction.emit(
                CalendarFragmentAction.LoadDiary(date)
            )
        } else {
            _calendarState.value = CalendarState.DiaryHidden
            _fragmentAction.emit(
                CalendarFragmentAction.InitializeDiary
            )
        }
    }

    // データ処理
    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == _selectedDate.value) return

        _previousSelectedDate.value = _selectedDate.value
        _selectedDate.value = date
    }

    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
            //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
            if (equalLastAppMessage(CalendarAppMessage.DiaryLoadingFailure)) return false
            Log.e(logTag, "日記既存確認_失敗", e)
            addAppMessage(CalendarAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun navigateDiaryEditFragment() {
        val date = _selectedDate.value
        val exists = existsSavedDiary(date) ?: false
        val isNewDiary = !exists
        _fragmentAction.emit(
            CalendarFragmentAction.NavigateDiaryEditFragment(date, isNewDiary)
        )
    }
}
