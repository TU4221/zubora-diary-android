package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.model.message.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.CalendarState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseDiaryShowViewModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    val logTag = createLogTag()

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
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onCalendarDayClick(date: LocalDate) {
        updateSelectedDate(date)
    }

    // TODO:DiaryStateFlowクラスのID、DateをnavigateDiaryEditFragment()に代入したいが、
    //      IDのデフォルト値がnullでないためnavigateDiaryEditFragment()でselectedDateを元に
    //      保存された日記が存在するか確認してEvent引数値を設定している。
    //      IDのデフォルト値がnullでない理由はDiaryEditViewModelで新規作成時の時点でIDを用意したい為。
    //      (新規作成時にIDを用意する必要があるか確認)
    fun onDiaryEditButtonClick() {
        val id = diaryStateFlow.id.value
        val date = _selectedDate.value
        viewModelScope.launch {
            navigateDiaryEditFragment(id, date)
        }
    }

    fun onBottomNavigationItemReselect() {
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
                updateDiary(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(CalendarState.LoadError)
                emitAppMessageEvent(
                    CalendarAppMessage.DiaryLoadFailure
                )
            }
        }
    }

    private suspend fun processCalendarDayDotUpdate(date: LocalDate) {
        emitUiEvent(
            CalendarEvent.UpdateCalendarDayDotVisibility(date, existsSavedDiary(date))
        )
    }

    private suspend fun existsSavedDiary(date: LocalDate): Boolean {
        when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                emitAppMessageEvent(CalendarAppMessage.DiaryInfoLoadFailure)
                return false
            }
        }
    }

    private suspend fun navigateDiaryEditFragment(id: UUIDString, date: LocalDate) {
        val exists = existsSavedDiary(date)
        val _id = if (exists) id else null
        emitUiEvent(
            CalendarEvent.NavigateDiaryEditFragment(_id?.value, date)
        )
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
