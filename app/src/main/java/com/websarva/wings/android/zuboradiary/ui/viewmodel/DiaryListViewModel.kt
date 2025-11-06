package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.diary.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListStartYearMonthPickerDateRangeUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListRefreshException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListStartYearMonthPickerDateRangeLoadException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val loadNewDiaryListUseCase: LoadNewDiaryListUseCase,
    private val loadAdditionDiaryListUseCase: LoadAdditionDiaryListUseCase,
    private val refreshDiaryListUseCase: RefreshDiaryListUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val loadDiaryListStartYearMonthPickerDateRangeUseCase: LoadDiaryListStartYearMonthPickerDateRangeUseCase,
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseFragmentViewModel<DiaryListUiState, DiaryListUiEvent, DiaryListAppMessage>(
    handle.get<DiaryListUiState>(SAVED_STATE_UI_KEY)?.let { savedUiState ->
        DiaryListUiState().copy(
            diaryList = savedUiState.diaryList,
            sortConditionDate = savedUiState.sortConditionDate
        )
    } ?: DiaryListUiState()
) {

    //region Properties
    private var diaryListLoadJob: Job? = null // キャンセル用

    private var isRestoringFromProcessDeath: Boolean = false

    private var needsRefreshDiaryList = false // MEMO:画面遷移、回転時の更新フラグ

    private val _isLoadingOnScrolled = MutableStateFlow(false)
    val isLoadingOnScrolled = _isLoadingOnScrolled.asStateFlow()

    // キャッシュパラメータ
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    //endregion

    //region Initialization
    init {
        checkForRestoration()
        collectUiStates()
    }

    private fun checkForRestoration() {
        updateIsRestoringFromProcessDeath(
            handle.contains(SAVED_STATE_UI_KEY)
        )
    }

    private fun collectUiStates() {
        collectUiState()
        collectDiaryListSortConditionDate()
    }

    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectDiaryListSortConditionDate() {
        viewModelScope.launch {
            uiState.distinctUntilChanged { oldState, newState ->
                oldState.sortConditionDate == newState.sortConditionDate
            }.map {
                Pair(it.diaryList, it.sortConditionDate)
            }.collectLatest { (diaryList, sortConditionDate) ->
                withUnexpectedErrorHandler {
                    if (isRestoringFromProcessDeath) {
                        updateIsRestoringFromProcessDeath(false)
                        refreshDiaryList(diaryList, sortConditionDate)
                        return@withUnexpectedErrorHandler
                    }

                    loadNewDiaryList(
                        diaryList,
                        sortConditionDate
                    )
                }
            }
        }
    }
    //endregion

    //region UI Event Handlers - Observation
    internal fun onUiReady() {
        if (!needsRefreshDiaryList) return
        updateNeedsRefreshDiaryList(false)
        if (!isReadyForOperation) return

        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                refreshDiaryList(currentList, sortConditionDate)
            }
    }

    internal fun onUiGone() {
        updateNeedsRefreshDiaryList(true)
    }
    //endregion

    //region UI Event Handlers - Action
    override fun onBackPressed() {
        // MEMO:DiaListFragmentはスタートフラグメントに該当するため、
        //      BaseFragmentでOnBackPressedCallbackを登録せずにNavigation機能のデフォルト戻る機能を使用する。
        //      そのため、本メソッドは呼び出されない。
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    internal fun onWordSearchMenuClick() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListUiEvent.NavigateWordSearchFragment)
        }
    }

    fun onNavigationIconClick() {
        launchWithUnexpectedErrorHandler {
            val dateRange = loadSavedDiaryDateRange()
            val newestDiaryDate = dateRange.newestDiaryDate
            val oldestDiaryDate = dateRange.oldestDiaryDate
            val newestYear = Year.of(newestDiaryDate.year)
            val oldestYear = Year.of(oldestDiaryDate.year)
            emitUiEvent(
                DiaryListUiEvent.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
            )
        }
    }

    internal fun onDiaryListItemClick(item: DiaryDayListItemUi.Standard) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListUiEvent.NavigateDiaryShowFragment(id, date))
        }
    }

    internal fun onDiaryListItemDeleteButtonClick(item: DiaryDayListItemUi.Standard) {
        if (!isReadyForOperation) return

        val id = item.id
        val date = item.date
        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                DiaryId(id),
                currentList,
                sortConditionDate
            )
            emitUiEvent(
                DiaryListUiEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onDiaryEditButtonClick() {
        launchWithUnexpectedErrorHandler {
            val today = LocalDate.now()
            emitUiEvent(DiaryListUiEvent.NavigateDiaryEditFragment(date = today))
        }
    }

    internal fun onDiaryListEndScrolled() {
        if (_isLoadingOnScrolled.value) return
        updateIsLoadingOnScrolled(true)

        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                loadAdditionDiaryList(currentList, sortConditionDate)
            }
    }

    internal fun onDiaryListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }
    //endregion

    //region UI Event Handlers - Results
    internal fun onDatePickerDialogResultReceived(result: DialogResult<YearMonth>) {
        when (result) {
            is DialogResult.Positive<YearMonth> -> {
                handleDatePickerDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDatePickerDialogPositiveResult(yearMonth: YearMonth) {
        val sortConditionDate =
            yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
        updateSortConditionDate(sortConditionDate)
    }

    internal fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
        Log.d("20251004", "onDiaryDeleteDialogResultReceived")
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryDeleteDialogPositiveResult(pendingDiaryDeleteParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryDeleteParameters()
    }

    private fun handleDiaryDeleteDialogPositiveResult(parameters: DiaryDeleteParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                deleteDiary(
                    it.id,
                    it.currentList,
                    it.sortConditionDate
                )
            } ?: throw IllegalStateException()
        }
    }
    //endregion

    //region Business Logic
    private fun cancelPreviousLoadJob() {
        val job = diaryListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewDiaryList(
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListNewLoadState() },
            currentList,
            { _ ->
                loadNewDiaryListUseCase(sortConditionDate)
            },
            { exception ->
                when (exception) {
                    is DiaryListNewLoadException.LoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListNewLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun loadAdditionDiaryList(
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListAdditionLoadState() },
            currentList,
            { lambdaCurrentList ->
                require(lambdaCurrentList.isNotEmpty)

                loadAdditionDiaryListUseCase(
                    lambdaCurrentList,
                    sortConditionDate
                )
            },
            { exception ->
                when (exception) {
                    is DiaryListAdditionLoadException.LoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListAdditionLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun refreshDiaryList(
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListRefreshState() },
            currentList,
            { lambdaCurrentList ->
                refreshDiaryListUseCase(lambdaCurrentList, sortConditionDate)
            },
            { exception ->
                when (exception) {
                    is DiaryListRefreshException.RefreshFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListRefreshException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun <E : UseCaseException> executeLoadDiaryList(
        updateToLoadingUiState: suspend () -> Unit,
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        executeLoad: suspend (DiaryYearMonthList<DiaryDayListItem.Standard>)
        -> UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {

        val logMsg = "日記リスト読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToLoadingUiState()
        try {
            when (val result = executeLoad(currentList.toDomainModel())) {
                is UseCaseResult.Success -> {
                    val updateDiaryList = mapDiaryListUiModel(result.value)
                    updateToDiaryListLoadCompletedState(updateDiaryList)
                    Log.i(logTag, "${logMsg}_完了")
                }
                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}_失敗", result.exception)
                    updateToIdleState()
                    emitAppMessageOnFailure(result.exception)
                }
            }
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateToIdleState()
            throw e // 再スローしてコルーチン処理を中断させる
        }
    }

    private suspend fun mapDiaryListUiModel(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>
    ): DiaryYearMonthListUi<DiaryDayListItemUi.Standard>{
        return list.toUiModel { fileName: DiaryImageFileName? ->
            fileName?.let {
                try {
                    when (val buildResult = buildDiaryImageFilePathUseCase(fileName)) {
                        is UseCaseResult.Success -> FilePathUi.Available(buildResult.value)
                        is UseCaseResult.Failure -> FilePathUi.Unavailable
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        throw e
                    } else {
                        FilePathUi.Unavailable
                    }
                }

            }
        }
    }

    private suspend fun deleteDiary(
        id: DiaryId,
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateToProcessingState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                refreshDiaryList(currentList, sortConditionDate)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToIdleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun loadSavedDiaryDateRange(): SavedDiaryDateRange {
        updateToProcessingState()
        val dateRange = when (val result = loadDiaryListStartYearMonthPickerDateRangeUseCase()) {
            is UseCaseResult.Success -> result.value
            is UseCaseResult.Failure -> {
                when (val exception = result.exception) {
                    is DiaryListStartYearMonthPickerDateRangeLoadException.DiaryInfoLoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryListStartYearMonthPickerDateRangeLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
                result.exception.fallbackDateRange
            }
        }
        updateToIdleState()
        return dateRange
    }
    //endregion

    //region UI State Update
    private fun updateSortConditionDate(date: LocalDate?) {
        updateUiState { it.copy(sortConditionDate = date) }
    }

    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }

    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private suspend fun updateToDiaryListNewLoadState() {
        val list =
            DiaryYearMonthList<DiaryDayListItem.Standard>(
                listOf(
                    DiaryYearMonthListItem.ProgressIndicator()
                )
            ).let { mapDiaryListUiModel(it) }
        updateUiState {
            it.copy(
                diaryList = list,
                hasNoDiaries = false,
                isProcessing = false,
                isInputDisabled = true
            )
        }
    }

    private fun updateToDiaryListAdditionLoadState() {
        updateUiState {
            it.copy(
                hasNoDiaries = false,
                isProcessing = false,
                isInputDisabled = true
            )
        }
    }

    private fun updateToDiaryListRefreshState() {
        updateUiState {
            it.copy(
                hasNoDiaries = false,
                isProcessing = true,
                isInputDisabled = true,
                isRefreshing = true,
            )
        }
    }

    private fun updateToDiaryListLoadCompletedState(
        list: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>
    ) {
        updateUiState {
            it.copy(
                diaryList = list,
                hasNoDiaries = list.isEmpty,
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
    //endregion

    //region Internal State Update
    private fun updateIsRestoringFromProcessDeath(bool: Boolean) {
        isRestoringFromProcessDeath = bool
    }

    private fun updateNeedsRefreshDiaryList(shouldUpdate: Boolean) {
        needsRefreshDiaryList = shouldUpdate
    }

    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        _isLoadingOnScrolled.value = isLoading
    }
    //endregion

    //region Pending Diary Delete Parameters
    private fun updatePendingDiaryDeleteParameters(
        id: DiaryId,
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, currentList, sortConditionDate)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        val sortConditionDate: LocalDate?
    )
    //endregion

    private companion object {
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
