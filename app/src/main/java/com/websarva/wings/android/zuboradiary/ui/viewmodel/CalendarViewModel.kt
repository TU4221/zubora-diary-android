package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.ui.model.message.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.CalendarUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    handle: SavedStateHandle,
    diaryUiStateHelper: DiaryUiStateHelper,
    private val loadCalendarStartDayOfWeekSettingUseCase: LoadCalendarStartDayOfWeekSettingUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase
) : BaseViewModel<CalendarEvent, CalendarAppMessage, CalendarUiState>(
    handle.get<CalendarUiState>(SAVED_UI_STATE_KEY)?.let { savedUiState ->
        CalendarUiState().copy(
            calendarStartDayOfWeek = savedUiState.calendarStartDayOfWeek,
            selectedDate = savedUiState.selectedDate,
            previousSelectedDate = savedUiState.previousSelectedDate
        )
    } ?:CalendarUiState()
) {

    companion object {
        private const val SAVED_UI_STATE_KEY = "uiState"
    }

    private val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && (currentUiState.diaryLoadState is LoadState.Success
                        || currentUiState.diaryLoadState is LoadState.Empty)

    private val currentUiState
        get() = uiState.value

    private val diaryFlow =
        uiState.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    private var shouldSmoothScroll = false

    init {
        observeDerivedUiStateChanges(handle, diaryUiStateHelper)
        observeUiStateChanges()
        observeCalendarStartDayOfWeekSetting()
    }

    private fun observeDerivedUiStateChanges(
        handle: SavedStateHandle,
        diaryUiStateHelper: DiaryUiStateHelper
    ) {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        diaryFlow.map {
            diaryUiStateHelper.isWeather2Visible(it)
        }.distinctUntilChanged().onEach { isWeather2Visible ->
            updateUiState {
                it.copy(
                    isWeather2Visible = isWeather2Visible
                )
            }
        }.launchIn(viewModelScope)

        diaryFlow.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateUiState {
                it.copy(
                    numVisibleDiaryItems = numVisibleDiaryItems
                )
            }
        }.launchIn(viewModelScope)

        diaryFlow.map {
            diaryUiStateHelper.buildImageFilePath(it)
        }.distinctUntilChanged().catchUnexpectedError(null).onEach { path ->
            updateUiState {
                it.copy(
                    diaryImageFilePath = path
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCalendarStartDayOfWeekSetting() {
        loadCalendarStartDayOfWeekSettingUseCase()
            .onEach {
                when (it) {
                    is UseCaseResult.Success -> { /*処理なし*/ }
                    is UseCaseResult.Failure -> {
                        when (it.exception) {
                            is CalendarStartDayOfWeekSettingLoadException.LoadFailure -> {
                                emitAppMessageEvent(CalendarAppMessage.SettingsLoadFailure)
                            }
                            is CalendarStartDayOfWeekSettingLoadException.Unknown -> {
                                emitUnexpectedAppMessage(it.exception)
                            }
                        }
                    }
                }
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value.dayOfWeek
                    is UseCaseResult.Failure -> it.exception.fallbackSetting.dayOfWeek
                }
            }.catchUnexpectedError(
                CalendarStartDayOfWeekSetting.default().dayOfWeek
            ).distinctUntilChanged().onEach { value: DayOfWeek ->
                updateUiState {
                    it.copy(
                        calendarStartDayOfWeek = value
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun observeUiStateChanges() {
        viewModelScope.launch {
            uiState.map { it.selectedDate }.distinctUntilChanged().collectLatest {
                withUnexpectedErrorHandler {
                    prepareDiary(it)
                }
            }
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onCalendarDayClick(date: LocalDate) {
        updateSelectedDate(date)
    }

    fun onDiaryEditButtonClick() {
        if (!isReadyForOperation) return

        val diaryLoadState = currentUiState.diaryLoadState
        var id: String?
        var date: LocalDate
        when (diaryLoadState) {
            is LoadState.Success -> {
                val diary = diaryLoadState.data
                id = diary.id
                date = diary.date
            }
            LoadState.Empty,
            is LoadState.Error -> {
                id = null
                date = currentUiState.selectedDate
            }

            LoadState.Idle,
            LoadState.Loading -> return
        }

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                CalendarEvent.NavigateDiaryEditFragment(id, date)
            )
        }
    }

    fun onBottomNavigationItemReselect() {
        if (!isReadyForOperation) return

        val selectedDate = currentUiState.selectedDate
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
            updateToNoDiaryState()
        }
    }

    private suspend fun loadSavedDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToDiaryLoadingState()
        when (val result = loadDiaryByDateUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                val diary = result.value.toUiModel()
                updateToDiaryLoadSuccessState(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToDiaryLoadErrorState()
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

    private fun updateShouldSmoothScroll(shouldScroll: Boolean) {
        shouldSmoothScroll = shouldScroll
    }

    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == currentUiState.selectedDate) return

        updateUiState {
            it.copy(
                selectedDate = date,
                previousSelectedDate = it.selectedDate
            )
        }
    }

    private fun updateToDiaryLoadingState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToDiaryLoadSuccessState(diary: DiaryUi) {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Success(diary),
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToDiaryLoadErrorState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToNoDiaryState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Empty,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
}
