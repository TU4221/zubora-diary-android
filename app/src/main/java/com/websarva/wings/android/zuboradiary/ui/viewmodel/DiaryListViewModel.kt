package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchOldestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryListUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
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
    private val fetchDiaryListUseCase: FetchDiaryListUseCase,
    private val checkUnloadedDiariesExistUseCase: CheckUnloadedDiariesExistUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val fetchNewestDiaryUseCase: FetchNewestDiaryUseCase,
    private val fetchOldestDiaryUseCase: FetchOldestDiaryUseCase
) : BaseViewModel<DiaryListEvent, DiaryListAppMessage, DiaryListState>(
    DiaryListState.Idle
) {

    companion object {
        // MEMO:初期読込時の対象リストが画面全体に表示される値にすること。
        //      アイテム数が少ないと最後尾のプログラスインディケーターが表示される為。
        const val NUM_LOADING_ITEMS: Int = 14/*日(2週間分)*/
    }

    private val logTag = createLogTag()

    override val isProcessingState =
        uiState
            .map { state ->
                when (state) {
                    DiaryListState.LoadingDiaryInfo,
                    DiaryListState.LoadingNewDiaryList,
                    DiaryListState.LoadingAdditionDiaryList,
                    DiaryListState.UpdatingDiaryList,
                    DiaryListState.DeletingDiary -> true

                    DiaryListState.Idle,
                    DiaryListState.NoDiaries,
                    DiaryListState.ShowingDiaryList -> false
                }
            }.stateInWhileSubscribed(
                false
            )

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

    private val initialDiaryListLoadingJob: Job? = null
    private var diaryListLoadingJob: Job? = initialDiaryListLoadingJob // キャンセル用

    private val initialDiaryList = DiaryYearMonthList()
    private val _diaryList = MutableStateFlow(initialDiaryList)
    val diaryList
        get() = _diaryList.asStateFlow()

    // MEMO:画面遷移、回転時の更新フラグ
    private val initialShouldUpdateDiaryList = false
    private var shouldUpdateDiaryList = initialShouldUpdateDiaryList

    private val initialSortConditionDate: LocalDate? = null
    private var sortConditionDate: LocalDate? = initialSortConditionDate

    private val initialIsLoadingOnScrolled = false
    private var isLoadingOnScrolled = initialIsLoadingOnScrolled

    override fun initialize() {
        super.initialize()
        diaryListLoadingJob = initialDiaryListLoadingJob
        _diaryList.value = initialDiaryList
        shouldUpdateDiaryList = initialShouldUpdateDiaryList
        sortConditionDate = initialSortConditionDate
        isLoadingOnScrolled = initialIsLoadingOnScrolled
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

    // ViewClicked処理
    fun onWordSearchMenuClicked() {
        viewModelScope.launch {
            emitUiEvent(DiaryListEvent.NavigateWordSearchFragment)
        }
    }

    fun onNavigationClicked() {
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

    fun onDiaryListItemClicked(date: LocalDate) {
        viewModelScope.launch {
            emitUiEvent(DiaryListEvent.NavigateDiaryShowFragment(date))
        }
    }

    fun onDiaryListItemDeleteButtonClicked(date: LocalDate, uri: Uri?) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryListEvent.NavigateDiaryDeleteDialog(
                    DiaryDeleteParameters(date, uri)
                )
            )
        }
    }

    fun onDiaryEditButtonClicked() {
        viewModelScope.launch {
            val today = LocalDate.now()
            emitUiEvent(DiaryListEvent.NavigateDiaryEditFragment(today))
        }
    }

    // View状態処理
    fun onDiaryListEndScrolled() {
        if (isLoadingOnScrolled) return
        isLoadingOnScrolled = true

        val currentList = _diaryList.value
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch {
                loadAdditionDiaryList(currentList)
            }
    }

    fun onDiaryListUpdated() {
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }

    // Fragment状態処理
    fun onFragmentViewCreated() {
        val currentList = _diaryList.value
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch {
                prepareDiaryList(currentList)
            }
    }

    fun onFragmentDestroyView() {
        shouldUpdateDiaryList = true
    }

    // Fragmentからの結果受取処理
    fun onDatePickerDialogResultReceived(result: DialogResult<YearMonth>) {
        when (result) {
            is DialogResult.Positive<YearMonth> -> {
                onDatePickerDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDatePickerDialogPositiveResultReceived(yearMonth: YearMonth) {
        updateSortConditionDate(yearMonth)
        val currentList = _diaryList.value
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch {
                loadNewDiaryList(currentList)
            }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<DiaryDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryDeleteParameters> -> {
                onDiaryDeleteDialogPositiveResultReceived(
                    result.data.loadedDate,
                    result.data.loadedImageUri
                )
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryDeleteDialogPositiveResultReceived(date: LocalDate, uri: Uri?) {
        val currentList = _diaryList.value
        viewModelScope.launch {
            deleteDiary(date, uri, currentList)
        }
    }

    private fun cancelPreviousLoading() {
        val job = diaryListLoadingJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun prepareDiaryList(currentList: DiaryYearMonthList) {
        val logMsg = "日記リスト準備"
        Log.i(logTag, "${logMsg}_開始")
        if (uiState.value == DiaryListState.Idle) {
            updateUiState(DiaryListState.LoadingDiaryInfo)
            try {
                loadNewDiaryList(currentList)
            } catch (e: CancellationException) {
                Log.i(logTag, "${logMsg}_キャンセル", e)
                updateUiStateForDiaryList(currentList)
            } catch (e: DomainException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                updateUiStateForDiaryList(currentList)
                emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadingFailure)
                return
            }
        } else {
            if (shouldUpdateDiaryList) {
                shouldUpdateDiaryList = false
                if (currentList.isEmpty) {
                    loadNewDiaryList(currentList)
                } else {
                    updateDiaryList(currentList)
                }
            }
        }
        Log.i(logTag, "${logMsg}_完了")
    }

    private suspend fun loadNewDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(
            DiaryListState.LoadingNewDiaryList,
            currentList
        ) { _ ->
            showDiaryListFirstItemProgressIndicator()
            val value = fetchDiaryList(NUM_LOADING_ITEMS, 0)
            toUiDiaryList(value)
        }
    }

    private suspend fun loadAdditionDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(
            DiaryListState.LoadingAdditionDiaryList,
            currentList
        ) { lambdaCurrentList ->
            require(lambdaCurrentList.isNotEmpty)

            val loadingOffset = lambdaCurrentList.countDiaries()
            val value = fetchDiaryList(NUM_LOADING_ITEMS, loadingOffset)
            val loadedList = toUiDiaryList(value)

            val numLoadedDiaries = lambdaCurrentList.countDiaries() + loadedList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)

            lambdaCurrentList.combineDiaryLists(loadedList, !existsUnloadedDiaries)
        }
    }

    private suspend fun updateDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(
            DiaryListState.UpdatingDiaryList,
            currentList
        ) { lambdaCurrentList ->
            require(lambdaCurrentList.isNotEmpty)

            var numLoadingItems = lambdaCurrentList.countDiaries()
            // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
            //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
            //      対策として下記コードを記述。
            if (numLoadingItems < NUM_LOADING_ITEMS) {
                numLoadingItems = NUM_LOADING_ITEMS
            }
            val value = fetchDiaryList(numLoadingItems, 0)
            toUiDiaryList(value)
        }
    }

    private suspend fun loadDiaryList(
        state: DiaryListState,
        currentList: DiaryYearMonthList,
        processLoading: suspend (DiaryYearMonthList) -> DiaryYearMonthList
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
            val updateDiaryList = processLoading(currentList)
            _diaryList.value = updateDiaryList
            updateUiStateForDiaryList(updateDiaryList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateUiStateForDiaryList(currentList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _diaryList.value = currentList
            updateUiStateForDiaryList(currentList)
            emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadingFailure)
        }
    }

    private fun updateUiStateForDiaryList(list: DiaryYearMonthList) {
        val state =
            if (list.isNotEmpty) {
                DiaryListState.ShowingDiaryList
            } else {
                DiaryListState.NoDiaries
            }
        updateUiState(state)
    }

    private fun showDiaryListFirstItemProgressIndicator() {
        val list = DiaryYearMonthList(false)
        _diaryList.value = list
    }

    @Throws(DomainException::class)
    private suspend fun fetchDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int
    ): List<DiaryListItem> {
        val result =
            fetchDiaryListUseCase(
                numLoadingItems,
                loadingOffset,
                sortConditionDate
            )
        when (result) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    @Throws(DomainException::class)
    private suspend fun toUiDiaryList(diaryList: List<DiaryListItem>): DiaryYearMonthList {
        if (diaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayListItemList: MutableList<DiaryDayListItem> = ArrayList()
        diaryList.stream()
            .forEach { x: DiaryListItem ->
                diaryDayListItemList.add(
                    DiaryDayListItem(x)
                )
            }
        val diaryDayList = DiaryDayList(diaryDayListItemList)
        val existsUnloadedDiaries = existsUnloadedDiaries(diaryDayList.countDiaries())
        return DiaryYearMonthList(diaryDayList, !existsUnloadedDiaries)
    }

    @Throws(DomainException::class)
    private suspend fun existsUnloadedDiaries(numLoadedDiaries: Int): Boolean {
        val result =
            checkUnloadedDiariesExistUseCase(numLoadedDiaries, sortConditionDate)
        when (result) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    private fun updateSortConditionDate(yearMonth: YearMonth) {
        Log.i(logTag, "日記リスト先頭年月更新 = $yearMonth")
        sortConditionDate = yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
    }

    private suspend fun deleteDiary(date: LocalDate, uri: Uri?, currentList: DiaryYearMonthList) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryListState.DeletingDiary)
        val uriString = uri?.toString()
        when (val result = deleteDiaryUseCase(date, uriString)) {
            is UseCaseResult.Success -> {
                updateDiaryList(currentList)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiStateForDiaryList(currentList)
                emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
            }
        }
    }

    private suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        when (val result = fetchNewestDiaryUseCase()) {
            is UseCaseResult.Success -> return result.value?.date
            is UseCaseResult.Failure -> {
                Log.e(logTag, "最新日記読込_失敗", result.exception)
                emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadingFailure)
                return null
            }
        }
    }

    private suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        when (val result = fetchOldestDiaryUseCase()) {
            is UseCaseResult.Success -> return result.value?.date
            is UseCaseResult.Failure -> {
                Log.e(logTag, "最古日記読込_失敗", result.exception)
                emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadingFailure)
                return null
            }
        }
    }
}
