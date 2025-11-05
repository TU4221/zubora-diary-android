package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.ui.model.message.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CalendarUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.CalendarUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
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
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val loadCalendarStartDayOfWeekSettingUseCase: LoadCalendarStartDayOfWeekSettingUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase
) : BaseFragmentViewModel<CalendarUiState, CalendarUiEvent, CalendarAppMessage>(
    handle.get<CalendarUiState>(SAVED_STATE_UI_KEY)?.let { savedUiState ->
        CalendarUiState().copy(
            calendarStartDayOfWeek = savedUiState.calendarStartDayOfWeek,
            selectedDate = savedUiState.selectedDate,
            previousSelectedDate = savedUiState.previousSelectedDate
        )
    } ?:CalendarUiState()
) {

    //region Properties
    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && (currentUiState.diaryLoadState is LoadState.Success
                        || currentUiState.diaryLoadState is LoadState.Empty)

    private val diaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.diaryLoadState == new.diaryLoadState
        }.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    private var shouldSmoothScroll = false
    //endregion

    //region Initialization
    init {
        collectUiStates()
    }

    private fun collectUiStates() {
        collectUiStateForSaveStateToHandle()
        collectCalendarStartDayOfWeekSetting()
        collectSelectedDate()
        collectWeather2Visible()
        collectNumVisibleDiaryItems()
        collectImageFilePath()
    }

    private fun collectUiStateForSaveStateToHandle() {
        uiState.onEach { 
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectCalendarStartDayOfWeekSetting() {
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
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                CalendarStartDayOfWeekSetting.default()
            ).distinctUntilChanged().onEach { setting ->
                updateCalendarStartDayOfWeek(setting.dayOfWeek)
            }.launchIn(viewModelScope)
    }

    private fun collectSelectedDate() {
        viewModelScope.launch {
            uiState.distinctUntilChanged{ old, new ->
                old.selectedDate == new.selectedDate
            }.map { it.selectedDate }.collectLatest { 
                withUnexpectedErrorHandler {
                    prepareDiary(it)
                }
            }
        }
    }

    private fun collectWeather2Visible() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.weather1 == new.weather1 && old.weather2 == new.weather2
        }.map { 
            diaryUiStateHelper.isWeather2Visible(it.weather1, it.weather2)
        }.distinctUntilChanged().onEach { isWeather2Visible ->
            updateIsWeather2Visible(isWeather2Visible)
        }.launchIn(viewModelScope)
    }

    private fun collectNumVisibleDiaryItems() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.itemTitles == new.itemTitles
        }.map { 
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it.itemTitles)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateNumVisibleDiaryItems(numVisibleDiaryItems)
        }.launchIn(viewModelScope)
    }

    private fun collectImageFilePath() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.imageFileName == new.imageFileName
        }.map { 
            diaryUiStateHelper.buildImageFilePath(it.imageFileName)
        }.catchUnexpectedError(
            FilePathUi.Unavailable
        ).distinctUntilChanged().onEach { path ->
            updateDiaryImageFilePath(path)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region UI Event Handlers
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onCalendarDayClick(date: LocalDate) {
        updateSelectedDate(date)
    }

    fun onCalendarDayDotVisibilityCheck(date: LocalDate) {
        launchWithUnexpectedErrorHandler {
            refreshCalendarDayDot(date)
        }
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
                CalendarUiEvent.NavigateDiaryEditFragment(id, date)
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
                    CalendarUiEvent.SmoothScrollCalendar(today)
                )
            }

            updateShouldSmoothScroll(true)
            updateSelectedDate(today)
        }
    }

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
    //endregion

    //region Business Logic
    private suspend fun prepareDiary(date: LocalDate) {
        val action =
            if (shouldSmoothScroll) {
                updateShouldSmoothScroll(false)
                CalendarUiEvent.SmoothScrollCalendar(date)
            } else {
                CalendarUiEvent.ScrollCalendar(date)
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
            CalendarUiEvent.RefreshCalendarDayDotVisibility(date, existsSavedDiary(date))
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
    //endregion

    //region UI State Update
    private fun updateCalendarStartDayOfWeek(dayOfWeek: DayOfWeek) {
        updateUiState { it.copy(calendarStartDayOfWeek = dayOfWeek) }
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

    private fun updateIsWeather2Visible(isVisible: Boolean) {
        updateUiState { it.copy(isWeather2Visible = isVisible) }
    }

    private fun updateNumVisibleDiaryItems(count: Int) {
        updateUiState { it.copy(numVisibleDiaryItems = count) }
    }

    private fun updateDiaryImageFilePath(path: FilePathUi?) {
        updateUiState { it.copy(diaryImageFilePath = path) }
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
    //endregion

    //region Internal State Update
    private fun updateShouldSmoothScroll(shouldScroll: Boolean) {
        shouldSmoothScroll = shouldScroll
    }
    //endregion

    companion object {
        private const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
