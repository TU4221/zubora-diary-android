package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    override fun initialize() {
        super.initialize()
        _calendarState.value = initialCalendarState
        _selectedDate.value = initialSelectedDate
        _previousSelectedDate.value = initialPreviousSelectedDate
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitViewModelEvent(ViewModelEvent.NavigatePreviousFragment)
        }
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
                emitViewModelEvent(
                    CalendarEvent.SmoothScrollCalendar(today)
                )
            }
            shouldSmoothScroll = true
            updateSelectedDate(today)
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryShowFragmentResultReceived(result: FragmentResult<LocalDate>) {
        when (result) {
            is FragmentResult.Some -> updateSelectedDate(result.data)
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    fun onDataReceivedFromDiaryEditFragment(result: FragmentResult<LocalDate>) {
        when (result) {
            is FragmentResult.Some -> updateSelectedDate(result.data)
            FragmentResult.None -> {
                // 処理なし
            }
        }
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
                CalendarEvent.SmoothScrollCalendar(date)
            } else {
                CalendarEvent.ScrollCalendar(date)
            }
        emitViewModelEvent(action)

        val exists = existsSavedDiary(date) ?: false
        if (exists) {
            _calendarState.value = CalendarState.DiaryVisible
            emitViewModelEvent(
                CalendarEvent.LoadDiary(date)
            )
        } else {
            _calendarState.value = CalendarState.DiaryHidden
            emitViewModelEvent(
                CalendarEvent.InitializeDiary
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
            Log.e(logTag, "日記既存確認_失敗", e)
            emitAppMessageEvent(CalendarAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun navigateDiaryEditFragment() {
        val date = _selectedDate.value
        val exists = existsSavedDiary(date) ?: false
        val isNewDiary = !exists
        emitViewModelEvent(
            CalendarEvent.NavigateDiaryEditFragment(date, isNewDiary)
        )
    }
}
