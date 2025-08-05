package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryUseCase
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseDiaryShowViewModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val fetchDiaryUseCase: FetchDiaryUseCase
) : BaseDiaryShowViewModel<CalendarEvent, CalendarAppMessage, CalendarState>(
    CalendarState.Idle
) {

    val logTag = createLogTag()

    override val isProcessingState: StateFlow<Boolean> =
        uiState
            .map { state ->
                when (state) {
                    CalendarState.LoadingDiary,
                    CalendarState.LoadingDiaryInfo -> true

                    CalendarState.Idle,
                    CalendarState.LoadDiarySuccess,
                    CalendarState.LoadError,
                    CalendarState.NoDiary -> false
                }
            }.stateInWhileSubscribed(
                false
            )


    private val initialSelectedDate = LocalDate.now()
    private val _selectedDate = MutableStateFlow<LocalDate>(initialSelectedDate)
    val selectedDate get() = _selectedDate.asStateFlow()

    private val initialPreviousSelectedDate = null
    private val _previousSelectedDate = MutableStateFlow<LocalDate?>(initialPreviousSelectedDate)
    val previousSelectedDate get() = _previousSelectedDate.asStateFlow()

    private var shouldSmoothScroll = false

    override fun initialize() {
        super.initialize()
        _selectedDate.value = initialSelectedDate
        _previousSelectedDate.value = initialPreviousSelectedDate
    }

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                CalendarEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: CalendarAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                CalendarEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (isProcessing) return

        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onCalendarDayClick(date: LocalDate) {
        if (isProcessing) return

        updateSelectedDate(date)
    }

    fun onDiaryEditButtonClick() {
        if (isProcessing) return

        val date = _selectedDate.value
        viewModelScope.launch {
            navigateDiaryEditFragment(date)
        }
    }

    fun onBottomNavigationItemReselect() {
        if (isProcessing) return

        val selectedDate = _selectedDate.value
        val today = LocalDate.now()
        viewModelScope.launch {
            // MEMO:StateFlowに現在値と同じ値を代入してもCollectメソッドに登録した処理が起動しないため、
            //      下記条件でカレンダースクロールのみ処理。
            if (selectedDate == today) {
                emitUiEvent(
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

    fun onDiaryEditFragmentResultReceived(result: FragmentResult<LocalDate>) {
        when (result) {
            is FragmentResult.Some -> updateSelectedDate(result.data)
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    // StateFlow値変更時処理
    fun onSelectedDateChanged(date: LocalDate) {
        viewModelScope.launch {
            prepareDiary(date)
        }
    }

    // View状態処理
    fun onCalendarDayDotVisibilityCheck(date: LocalDate) {
        viewModelScope.launch {
            processCalendarDayDotUpdate(date)
        }
    }

    // データ処理
    private suspend fun prepareDiary(date: LocalDate) {
        val action =
            if (shouldSmoothScroll) {
                shouldSmoothScroll = false
                CalendarEvent.SmoothScrollCalendar(date)
            } else {
                CalendarEvent.ScrollCalendar(date)
            }
        emitUiEvent(action)

        val exists = existsSavedDiary(date)
        if (exists) {
            loadSavedDiary(date)
        } else {
            updateUiState(CalendarState.NoDiary)
            diaryStateFlow.initialize()
        }
    }

    override suspend fun loadSavedDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(CalendarState.LoadingDiary)
        when (val result = fetchDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateUiState(CalendarState.LoadDiarySuccess)
                val diary = result.value
                diaryStateFlow.update(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(CalendarState.LoadError)
                emitAppMessageEvent(
                    CalendarAppMessage.DiaryLoadingFailure
                )
            }
        }
    }

    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == _selectedDate.value) return

        _previousSelectedDate.value = _selectedDate.value
        _selectedDate.value = date
    }

    private suspend fun processCalendarDayDotUpdate(date: LocalDate) {
        emitUiEvent(
            CalendarEvent.UpdateCalendarDayDotVisibility(date, existsSavedDiary(date))
        )
    }

    // MEMO:呼び出し元で通信エラーが判断できるように戻り値をNullableとする。
    private suspend fun existsSavedDiary(date: LocalDate): Boolean {
        when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                emitAppMessageEvent(CalendarAppMessage.DiaryInfoLoadingFailure)
                return false
            }
        }
    }

    private suspend fun navigateDiaryEditFragment(date: LocalDate) {
        val exists = existsSavedDiary(date)
        val isNewDiary = !exists
        emitUiEvent(
            CalendarEvent.NavigateDiaryEditFragment(date, isNewDiary)
        )
    }
}
