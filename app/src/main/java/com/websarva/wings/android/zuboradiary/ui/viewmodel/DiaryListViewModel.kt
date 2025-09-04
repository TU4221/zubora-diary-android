package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadOldestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshDiaryListUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val loadNewestDiaryUseCase: LoadNewestDiaryUseCase,
    private val loadOldestDiaryUseCase: LoadOldestDiaryUseCase
) : BaseViewModel<DiaryListEvent, DiaryListAppMessage, DiaryListState>(
    DiaryListState.Idle
) {

    private val logTag = createLogTag()

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

    private val _diaryList = MutableStateFlow(DiaryYearMonthListUi<DiaryDayListItemUi.Standard>())
    val diaryList
        get() = _diaryList.asStateFlow()

    // MEMO:画面遷移、回転時の更新フラグ
    private var shouldUpdateDiaryList = false

    private var sortConditionDate: LocalDate? = null

    private var isLoadingOnScrolled = false

    init {
        initializeDiaryListData()
    }

    private fun initializeDiaryListData() {
        if (uiState.value != DiaryListState.Idle) return

        val currentList = _diaryList.value
        viewModelScope.launch {
            val logMsg = "日記リスト準備"
            Log.i(logTag, "${logMsg}_開始")
            try {
                loadNewDiaryList(currentList)
            } catch (e: CancellationException) {
                Log.i(logTag, "${logMsg}_キャンセル", e)
                updateUiStateOnDiaryListLoadCompleted(currentList)
            } catch (e: DomainException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                updateUiStateOnDiaryListLoadCompleted(currentList)
                emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                return@launch
            }
            Log.i(logTag, "${logMsg}_完了")
        }
    }

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryListEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: DiaryListAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryListEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        // MEMO:DiaListFragmentはスタートフラグメントに該当するため、
        //      BaseFragmentでOnBackPressedCallbackを登録せずにNavigation機能のデフォルト戻る機能を使用する。
        //      そのため、本メソッドは呼び出されない。
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onWordSearchMenuClick() {
        viewModelScope.launch {
            emitUiEvent(DiaryListEvent.NavigateWordSearchFragment)
        }
    }

    fun onNavigationIconClick() {
        viewModelScope.launch {
            val newestDiaryDate = loadNewestSavedDiaryDate() ?: LocalDate.now()
            val oldestDiaryDate = loadOldestSavedDiaryDate() ?: LocalDate.now()
            val newestYear = Year.of(newestDiaryDate.year)
            val oldestYear = Year.of(oldestDiaryDate.year)
            emitUiEvent(
                DiaryListEvent.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
            )
        }
    }

    fun onDiaryListItemClick(item: DiaryDayListItemUi.Standard) {
        val date = item.date
        viewModelScope.launch {
            emitUiEvent(DiaryListEvent.NavigateDiaryShowFragment(date))
        }
    }

    fun onDiaryListItemDeleteButtonClick(item: DiaryDayListItemUi.Standard) {
        if (uiState.value != DiaryListState.ShowingDiaryList) return

        val date = item.date
        val uri = item.imageUri
        viewModelScope.launch {
            emitUiEvent(
                DiaryListEvent.NavigateDiaryDeleteDialog(
                    DiaryDeleteParameters(date, uri)
                )
            )
        }
    }

    fun onDiaryEditButtonClick() {
        viewModelScope.launch {
            val today = LocalDate.now()
            emitUiEvent(DiaryListEvent.NavigateDiaryEditFragment(today))
        }
    }

    // View状態処理
    fun onDiaryListEndScrolled() {
        if (isLoadingOnScrolled) return
        updateIsLoadingOnScrolled(true)

        val currentList = _diaryList.value
        cancelPreviousLoadJob()
        diaryListLoadJob =
            viewModelScope.launch {
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
            viewModelScope.launch {
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
            viewModelScope.launch {
                loadNewDiaryList(currentList)
            }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<DiaryDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryDeleteParameters> -> {
                handleDiaryDeleteDialogPositiveResult(
                    result.data.date,
                    result.data.imageUri
                )
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDiaryDeleteDialogPositiveResult(date: LocalDate, uri: Uri?) {
        val currentList = _diaryList.value
        viewModelScope.launch {
            deleteDiary(date, uri, currentList)
        }
    }

    // データ処理
    private fun cancelPreviousLoadJob() {
        val job = diaryListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewDiaryList(currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>) {
        loadDiaryList(
            DiaryListState.LoadingNewDiaryList,
            currentList
        ) { _ ->
            showDiaryListFirstItemProgressIndicator()
            loadNewDiaryListUseCase(sortConditionDate)
        }
    }

    private suspend fun loadAdditionDiaryList(currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>) {
        loadDiaryList(
            DiaryListState.LoadingAdditionDiaryList,
            currentList
        ) { lambdaCurrentList ->
            require(lambdaCurrentList.isNotEmpty)

            loadAdditionDiaryListUseCase(
                lambdaCurrentList.toDomainModel(),
                sortConditionDate
            )
        }
    }

    private suspend fun refreshDiaryList(currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>) {
        loadDiaryList(
            DiaryListState.UpdatingDiaryList,
            currentList
        ) { lambdaCurrentList ->
            refreshDiaryListUseCase(lambdaCurrentList.toDomainModel(), sortConditionDate)
        }
    }

    private suspend fun loadDiaryList(
        state: DiaryListState,
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>,
        processLoad: suspend (DiaryYearMonthListUi<DiaryDayListItemUi.Standard>)
        -> DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>>
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
            val updateDiaryList =
                when (val result = processLoad(currentList)) {
                    is UseCaseResult.Success -> result.value.toUiModel()
                    is UseCaseResult.Failure -> throw result.exception
                }
            updateDiaryList(updateDiaryList)
            updateUiStateOnDiaryListLoadCompleted(updateDiaryList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateUiStateOnDiaryListLoadCompleted(currentList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            updateDiaryList(currentList)
            updateUiStateOnDiaryListLoadCompleted(currentList)
            emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
        }
    }

    private fun showDiaryListFirstItemProgressIndicator() {
        val list =
            DiaryYearMonthListUi<DiaryDayListItemUi.Standard>(
                listOf(
                    DiaryYearMonthListItemUi.ProgressIndicator()
                )
            )
        updateDiaryList(list)
    }

    private suspend fun deleteDiary(
        date: LocalDate,
        uri: Uri?,
        currentList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>
    ) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryListState.DeletingDiary)
        val uriString = uri?.toString()
        when (val result = deleteDiaryUseCase(date, uriString)) {
            is UseCaseResult.Success -> {
                refreshDiaryList(currentList)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiStateOnDiaryListLoadCompleted(currentList)
                emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
            }
        }
    }

    private suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        when (val result = loadNewestDiaryUseCase()) {
            is UseCaseResult.Success -> return result.value.date
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is DiaryLoadException.AccessFailure -> {
                        Log.e(logTag, "最新日記読込_失敗", result.exception)
                        emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryLoadException.DataNotFound -> {
                        // 処理なし
                    }
                }
                return null
            }
        }
    }

    private suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        when (val result = loadOldestDiaryUseCase()) {
            is UseCaseResult.Success -> return result.value.date
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is DiaryLoadException.AccessFailure -> {
                        Log.e(logTag, "最古日記読込_失敗", result.exception)
                        emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryLoadException.DataNotFound -> {
                        // 処理なし
                    }
                }
                return null
            }
        }
    }

    private fun updateUiStateOnDiaryListLoadCompleted(
        list: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>
    ) {
        val state =
            if (list.isNotEmpty) {
                DiaryListState.ShowingDiaryList
            } else {
                DiaryListState.NoDiaries
            }
        updateUiState(state)
    }

    private fun updateDiaryList(diaryList: DiaryYearMonthListUi<DiaryDayListItemUi.Standard>) {
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
}
