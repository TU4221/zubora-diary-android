package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseDiaryShowViewModel
import com.websarva.wings.android.zuboradiary.utils.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase,
    buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseDiaryShowViewModel<CalendarEvent, CalendarAppMessage, CalendarState>(
    CalendarState.Idle,
    buildDiaryImageFilePathUseCase
) {

    override val isProgressIndicatorVisible =
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


    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate get() = _selectedDate.asStateFlow()

    private val _previousSelectedDate = MutableStateFlow<LocalDate?>(null)
    val previousSelectedDate get() = _previousSelectedDate.asStateFlow()

    private var shouldSmoothScroll = false

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): CalendarEvent {
        return CalendarEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: CalendarAppMessage): CalendarEvent {
        return CalendarEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): CalendarAppMessage {
        return CalendarAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onCalendarDayClick(date: LocalDate) {
        updateSelectedDate(date)
    }

    fun onDiaryEditButtonClick() {
        val id = diaryStateFlow.id.value
        val date = _selectedDate.value
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                CalendarEvent.NavigateDiaryEditFragment(id?.value, date)
            )
        }
    }

    fun onBottomNavigationItemReselect() {
        val selectedDate = _selectedDate.value
        val today = LocalDate.now()
        launchWithUnexpectedErrorHandler {
            // MEMO:StateFlowに現在値と同じ値を代入してもCollectメソッドに登録した処理が起動しないため、
            //      下記条件でカレンダースクロールのみ処理。
            if (selectedDate == today) {
                emitUiEvent(
                    CalendarEvent.SmoothScrollCalendar(today)
                )
            }

            updateShouldSmoothScroll(true)
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
        launchWithUnexpectedErrorHandler {
            prepareDiary(date)
        }
    }

    // View状態処理
    fun onCalendarDayDotVisibilityCheck(date: LocalDate) {
        launchWithUnexpectedErrorHandler {
            refreshCalendarDayDot(date)
        }
    }

    // データ処理
    private suspend fun prepareDiary(date: LocalDate) {
        val action =
            if (shouldSmoothScroll) {
                updateShouldSmoothScroll(false)
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
            initializeDiary()
        }
    }

    private suspend fun loadSavedDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(CalendarState.LoadingDiary)
        when (val result = loadDiaryByDateUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateUiState(CalendarState.LoadDiarySuccess)
                val diary = result.value
                updateDiary(diary.toUiModel())
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(CalendarState.LoadError)
                when (result.exception) {
                    is DiaryLoadByDateException.LoadFailure -> {
                        emitAppMessageEvent(
                            CalendarAppMessage.DiaryLoadFailure
                        )
                    }
                    is DiaryLoadByDateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun refreshCalendarDayDot(date: LocalDate) {
        emitUiEvent(
            CalendarEvent.RefreshCalendarDayDotVisibility(date, existsSavedDiary(date))
        )
    }

    private suspend fun existsSavedDiary(date: LocalDate): Boolean {
        when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is DiaryExistenceCheckException.CheckFailure -> {
                        emitAppMessageEvent(CalendarAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryExistenceCheckException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
                return false
            }
        }
    }

    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == _selectedDate.value) return

        updatePreviousSelectedDate(_selectedDate.value)
        _selectedDate.value = date
    }

    private fun updatePreviousSelectedDate(date: LocalDate?) {
        _previousSelectedDate.value = date
    }

    private fun updateShouldSmoothScroll(shouldScroll: Boolean) {
        shouldSmoothScroll = shouldScroll
    }
}
