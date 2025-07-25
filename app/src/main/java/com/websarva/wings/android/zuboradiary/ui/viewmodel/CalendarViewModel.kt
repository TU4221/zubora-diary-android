package com.websarva.wings.android.zuboradiary.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryShowState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// TODO:DiaryShowViewModelをCalendarViewModelに含めるか検討する
@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) : BaseViewModel<CalendarEvent, CalendarAppMessage, CalendarState>(
    CalendarState.Idle
) {

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

    override suspend fun emitNavigatePreviousFragmentEvent() {
        viewModelScope.launch {
            emitUiEvent(
                CalendarEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment
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

    // ViewClicked処理
    fun onCalendarDayClicked(date: LocalDate) {
        if (isProcessing) return

        updateSelectedDate(date)
    }

    fun onDiaryEditButtonClicked() {
        if (isProcessing) return

        val date = _selectedDate.value
        viewModelScope.launch {
            navigateDiaryEditFragment(date)
        }
    }

    fun onBottomNavigationItemReselected() {
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

    fun onDataReceivedFromDiaryEditFragment(result: FragmentResult<LocalDate>) {
        when (result) {
            is FragmentResult.Some -> updateSelectedDate(result.data)
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    // StateFlow値変更時処理
    fun onChangedSelectedDate(date: LocalDate) {
        viewModelScope.launch {
            prepareDiary(date)
        }
    }

    fun onChangedDiaryShowViewModelState(state: DiaryShowState) {
        when (state) {
            DiaryShowState.Loading -> updateUiState(CalendarState.LoadingDiary)
            DiaryShowState.LoadSuccess -> updateUiState(CalendarState.LoadDiarySuccess)
            DiaryShowState.LoadError -> updateUiState(CalendarState.LoadError)

            DiaryShowState.Idle,
            DiaryShowState.Deleting -> {
                // MEMO:CalendarFragmentからの日記削除機能は無いため、処理不要
            }
        }
    }

    // View変更処理
    private suspend fun prepareDiary(date: LocalDate) {
        val action =
            if (shouldSmoothScroll) {
                shouldSmoothScroll = false
                CalendarEvent.SmoothScrollCalendar(date)
            } else {
                CalendarEvent.ScrollCalendar(date)
            }
        emitUiEvent(action)

        val exists = existsSavedDiary(date) ?: false
        if (exists) {
            emitUiEvent(
                CalendarEvent.LoadDiary(date)
            )
        } else {
            updateUiState(CalendarState.NoDiary)
            emitUiEvent(
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

    // MEMO:呼び出し元で通信エラーが判断できるように戻り値をNullableとする。
    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                emitAppMessageEvent(CalendarAppMessage.DiaryInfoLoadingFailure)
                return null
            }
        }
    }

    private suspend fun navigateDiaryEditFragment(date: LocalDate) {
        val exists = existsSavedDiary(date) ?: false
        val isNewDiary = !exists
        emitUiEvent(
            CalendarEvent.NavigateDiaryEditFragment(date, isNewDiary)
        )
    }
}
