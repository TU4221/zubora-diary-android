package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
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
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.utils.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class DiaryListViewModel @Inject constructor(
    private val loadNewDiaryListUseCase: LoadNewDiaryListUseCase,
    private val loadAdditionDiaryListUseCase: LoadAdditionDiaryListUseCase,
    private val refreshDiaryListUseCase: RefreshDiaryListUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val loadDiaryListStartYearMonthPickerDateRangeUseCase: LoadDiaryListStartYearMonthPickerDateRangeUseCase,
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseViewModel<DiaryListEvent, DiaryListAppMessage, DiaryListState>(
    DiaryListState.Idle
) {

    override val isProgressIndicatorVisible =
        uiState
            .map { state ->
                when (state) {
                    DiaryListState.LoadingDiaryInfo,
                    DiaryListState.UpdatingDiaryList,
                    DiaryListState.DeletingDiary -> true

                    DiaryListState.LoadingNewDiaryList,
                    DiaryListState.LoadingAdditionDiaryList -> false

                    DiaryListState.Idle,
                    DiaryListState.NoDiaries,
                    DiaryListState.ShowingDiaryList -> false
                }
            }.stateInWhileSubscribed(
                false
            )

    private var diaryListLoadJob: Job? = null // キャンセル用

    private val initialDiaryList = DiaryYearMonthList<DiaryDayListItem.Standard>()
    private val _diaryList = MutableStateFlow(initialDiaryList)
    val diaryList
        get() = _diaryList
            .map { mapDiaryListUiModel(it) }
            .catchUnexpectedError(DiaryYearMonthListUi())
            .stateInWhileSubscribed(DiaryYearMonthListUi())

    // MEMO:画面遷移、回転時の更新フラグ
    private var shouldUpdateDiaryList = false

    private var sortConditionDate: LocalDate? = null

    private var isLoadingOnScrolled = false

    // キャッシュパラメータ
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null

    init {
        initializeDiaryListData()
    }

    private fun initializeDiaryListData() {
        if (uiState.value != DiaryListState.Idle) return

        val currentList = _diaryList.value
        launchWithUnexpectedErrorHandler {
            val logMsg = "日記リスト準備"
            Log.i(logTag, "${logMsg}_開始")
            try {
                loadNewDiaryList(currentList)
            } catch (e: CancellationException) {
                Log.i(logTag, "${logMsg}_キャンセル", e)
                updateUiStateOnDiaryListLoadCompleted(currentList)
            } catch (e: UseCaseException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                updateUiStateOnDiaryListLoadCompleted(currentList)
                emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                return@launchWithUnexpectedErrorHandler
            }
            Log.i(logTag, "${logMsg}_完了")
        }
    }

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): DiaryListEvent {
        return DiaryListEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: DiaryListAppMessage): DiaryListEvent {
        return DiaryListEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): DiaryListAppMessage {
        return DiaryListAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        // MEMO:DiaListFragmentはスタートフラグメントに該当するため、
        //      BaseFragmentでOnBackPressedCallbackを登録せずにNavigation機能のデフォルト戻る機能を使用する。
        //      そのため、本メソッドは呼び出されない。
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onWordSearchMenuClick() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListEvent.NavigateWordSearchFragment)
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
                DiaryListEvent.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
            )
        }
    }

    fun onDiaryListItemClick(item: DiaryDayListItemUi.Standard) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListEvent.NavigateDiaryShowFragment(id, date))
        }
    }

    fun onDiaryListItemDeleteButtonClick(item: DiaryDayListItemUi.Standard) {
        if (uiState.value != DiaryListState.ShowingDiaryList) return

        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                DiaryId(id)
            )
            emitUiEvent(
                DiaryListEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onDiaryEditButtonClick() {
        launchWithUnexpectedErrorHandler {
            val today = LocalDate.now()
            emitUiEvent(DiaryListEvent.NavigateDiaryEditFragment(date = today))
        }
    }

    // View状態処理
    fun onDiaryListEndScrolled() {
        if (isLoadingOnScrolled) return
        updateIsLoadingOnScrolled(true)

        val currentList = _diaryList.value
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                loadAdditionDiaryList(currentList)
            }
    }

    fun onDiaryListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }

    // Ui状態処理
    fun onUiReady() {
        if (!shouldUpdateDiaryList) return
        updateShouldUpdateDiaryList(false)
        when (uiState.value) {
            DiaryListState.Idle,
            DiaryListState.LoadingNewDiaryList,
            DiaryListState.LoadingAdditionDiaryList,
            DiaryListState.UpdatingDiaryList,
            DiaryListState.DeletingDiary,
            DiaryListState.LoadingDiaryInfo -> return

            DiaryListState.ShowingDiaryList,
            DiaryListState.NoDiaries -> { /* 処理継続 */ }
        }

        val currentList = _diaryList.value
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                refreshDiaryList(currentList)
            }
    }

    fun onUiGone() {
        updateShouldUpdateDiaryList(true)
    }

    // Fragmentからの結果受取処理
    fun onDatePickerDialogResultReceived(result: DialogResult<YearMonth>) {
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
        updateSortConditionDate(yearMonth)
        val currentList = _diaryList.value
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                loadNewDiaryList(currentList)
            }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
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
        val currentList = _diaryList.value
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                deleteDiary(it.id, currentList)
            } ?: throw IllegalStateException()
        }
    }

    // データ処理
    private fun cancelPreviousLoadJob() {
        val job = diaryListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewDiaryList(currentList: DiaryYearMonthList<DiaryDayListItem.Standard>) {
        executeLoadDiaryList(
            DiaryListState.LoadingNewDiaryList,
            currentList,
            { _ ->
                showDiaryListFirstItemProgressIndicator()
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

    private suspend fun loadAdditionDiaryList(currentList: DiaryYearMonthList<DiaryDayListItem.Standard>) {
        executeLoadDiaryList(
            DiaryListState.LoadingAdditionDiaryList,
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

    private suspend fun refreshDiaryList(currentList: DiaryYearMonthList<DiaryDayListItem.Standard>) {
        executeLoadDiaryList(
            DiaryListState.UpdatingDiaryList,
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
        state: DiaryListState,
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        executeLoad: suspend (DiaryYearMonthList<DiaryDayListItem.Standard>)
        -> UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {
        require(
            when (state) {
                DiaryListState.LoadingNewDiaryList,
                DiaryListState.LoadingAdditionDiaryList,
                DiaryListState.UpdatingDiaryList -> true

                DiaryListState.Idle,
                DiaryListState.DeletingDiary,
                DiaryListState.LoadingDiaryInfo,
                DiaryListState.NoDiaries,
                DiaryListState.ShowingDiaryList -> false
            }
        )

        val logMsg = "日記リスト読込($state)"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(state)
        try {
            when (val result = executeLoad(currentList)) {
                is UseCaseResult.Success -> {
                    val updateDiaryList = result.value
                    updateDiaryList(updateDiaryList)
                    updateUiStateOnDiaryListLoadCompleted(updateDiaryList)
                    Log.i(logTag, "${logMsg}_完了")
                }
                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}_失敗", result.exception)
                    updateDiaryList(currentList)
                    updateUiStateOnDiaryListLoadCompleted(currentList)
                    emitAppMessageOnFailure(result.exception)
                }
            }
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateUiStateOnDiaryListLoadCompleted(currentList)
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

    private fun showDiaryListFirstItemProgressIndicator() {
        val list =
            DiaryYearMonthList<DiaryDayListItem.Standard>(
                listOf(
                    DiaryYearMonthListItem.ProgressIndicator()
                )
            )
        updateDiaryList(list)
    }

    private suspend fun deleteDiary(
        id: DiaryId,
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>
    ) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryListState.DeletingDiary)
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                refreshDiaryList(currentList)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiStateOnDiaryListLoadCompleted(currentList)
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
        when (val result = loadDiaryListStartYearMonthPickerDateRangeUseCase()) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                when (val exception = result.exception) {
                    is DiaryListStartYearMonthPickerDateRangeLoadException.DiaryInfoLoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryListStartYearMonthPickerDateRangeLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
                return result.exception.fallbackDateRange
            }
        }
    }

    private fun updateUiStateOnDiaryListLoadCompleted(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>
    ) {
        val state =
            if (list.isNotEmpty) {
                DiaryListState.ShowingDiaryList
            } else {
                DiaryListState.NoDiaries
            }
        updateUiState(state)
    }

    private fun updateDiaryList(diaryList: DiaryYearMonthList<DiaryDayListItem.Standard>) {
        _diaryList.value = diaryList
    }

    private fun updateShouldUpdateDiaryList(shouldUpdate: Boolean) {
        shouldUpdateDiaryList = shouldUpdate
    }

    private fun updateSortConditionDate(yearMonth: YearMonth) {
        Log.i(logTag, "日記リスト先頭年月更新 = $yearMonth")
        updateSortConditionDate(
            yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
        )
    }

    private fun updateSortConditionDate(date: LocalDate?) {
        sortConditionDate = date
    }

    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        isLoadingOnScrolled = isLoading
    }

    private fun updatePendingDiaryDeleteParameters(id: DiaryId) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private data class DiaryDeleteParameters(
        val id: DiaryId
    )
}
