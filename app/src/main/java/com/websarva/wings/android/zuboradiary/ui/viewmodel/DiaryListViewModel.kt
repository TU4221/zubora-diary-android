package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchOldestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteDiaryUseCaseException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DiaryListItemDeleteResult
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
    private val countDiariesUseCase: CountDiariesUseCase,
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
                // TODO:保留
                when (state) {
                    DiaryListState.NewLoading,
                    DiaryListState.AdditionLoading,
                    DiaryListState.Updating -> true

                    DiaryListState.Idle,
                    DiaryListState.NoResults,
                    DiaryListState.Results -> false
                }
            }.stateInDefault(
                viewModelScope,
                false
            )

    private val initialDiaryListLoadingJob: Job? = null
    private var diaryListLoadingJob: Job? = initialDiaryListLoadingJob // キャンセル用

    private val initialDiaryList = DiaryYearMonthList()
    private val _diaryList = MutableStateFlow(initialDiaryList)
    val diaryList
        get() = _diaryList.asStateFlow()

    private val initialSortConditionDate: LocalDate? = null
    private var sortConditionDate: LocalDate? = initialSortConditionDate

    private val initialIsLoadingOnScrolled = false
    private var isLoadingOnScrolled = initialIsLoadingOnScrolled

    override fun initialize() {
        super.initialize()
        diaryListLoadingJob = initialDiaryListLoadingJob
        _diaryList.value = initialDiaryList
        sortConditionDate = initialSortConditionDate
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // ViewClicked処理
    fun onWordSearchMenuClicked() {
        viewModelScope.launch {
            navigateWordSearchFragment()
        }
    }

    fun onNavigationClicked() {
        viewModelScope.launch {
            navigateStartYearMonthPickerDialog()
        }
    }

    fun onDiaryListItemClicked(date: LocalDate) {
        viewModelScope.launch {
            navigateDiaryShowFragment(date)
        }
    }

    fun onDiaryListItemDeleteButtonClicked(date: LocalDate, uri: Uri?) {
        viewModelScope.launch {
            navigateDiaryDeleteDialog(date, uri)
        }
    }

    fun onDiaryEditButtonClicked() {
        viewModelScope.launch {
            navigateDiaryEditFragment()
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

    fun onFragmentViewCreated() {
        val currentList = _diaryList.value
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch {
                prepareDiaryList(currentList)
            }
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

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<DiaryListItemDeleteResult>) {
        when (result) {
            is DialogResult.Positive<DiaryListItemDeleteResult> -> {
                onDiaryDeleteDialogPositiveResultReceived(
                    result.data.date,
                    result.data.uri
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
        if (currentList.isEmpty) {
            try {
                val numSavedDiaries = countSavedDiaries()
                if (numSavedDiaries >= 1) loadNewDiaryList(currentList)
            } catch (e: DomainException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadingFailure)
                return
            }
        } else {
            updateDiaryList(currentList)
        }
        Log.i(logTag, "${logMsg}_完了")
    }

    @Throws(DomainException::class)
    private suspend fun countSavedDiaries(): Int{
        when (val result = countDiariesUseCase()) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    private suspend fun loadNewDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(NewDiaryListCreator(), currentList)
    }

    private suspend fun loadAdditionDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(AddedDiaryListCreator(), currentList)
    }

    private suspend fun updateDiaryList(currentList: DiaryYearMonthList) {
        loadDiaryList(UpdateDiaryListCreator(), currentList)
    }

    private suspend fun loadDiaryList(creator: DiaryListCreator, currentList: DiaryYearMonthList) {
        val logMsg = "日記リスト読込"
        Log.i(logTag, "${logMsg}_開始")

        try {
            updateWordSearchStatusOnListLoadingStart(creator)
            val updateDiaryList = creator.create(currentList)
            _diaryList.value = updateDiaryList
            updateWordSearchStatusOnListLoadingFinish(updateDiaryList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _diaryList.value = currentList
            updateWordSearchStatusOnListLoadingFinish(currentList)
            emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadingFailure)
        }
    }

    // MEMO:日記リスト読込は処理途中でも再読込できる仕様のため、createDiaryList()処理内で状態更新を行う。
    //      再読込、createDiaryList()処理前に状態更新を行うと一つ前の検索結果状態が上書きされる可能性あり。
    private fun updateWordSearchStatusOnListLoadingStart(creator: DiaryListCreator) {
         val state =
            when (creator) {
                is NewDiaryListCreator -> DiaryListState.NewLoading
                is AddedDiaryListCreator -> DiaryListState.AdditionLoading
                is UpdateDiaryListCreator -> DiaryListState.Updating
                else -> throw IllegalArgumentException()
            }
        updateUiState(state)
    }

    private fun updateWordSearchStatusOnListLoadingFinish(list: DiaryYearMonthList) {
        val state =
            if (list.isNotEmpty) {
                DiaryListState.Results
            } else {
                DiaryListState.NoResults
            }
        updateUiState(state)
    }

    private fun interface DiaryListCreator {
        @Throws(DomainException::class)
        suspend fun create(currentList: DiaryYearMonthList): DiaryYearMonthList
    }

    private inner class NewDiaryListCreator : DiaryListCreator {

        @Throws(DomainException::class)
        override suspend fun create(currentList: DiaryYearMonthList): DiaryYearMonthList {
            showDiaryListFirstItemProgressIndicator()
            return loadSavedDiaryList(NUM_LOADING_ITEMS, 0)
        }

        private fun showDiaryListFirstItemProgressIndicator() {
            val list = DiaryYearMonthList(false)
            _diaryList.value = list
        }
    }

    private inner class AddedDiaryListCreator : DiaryListCreator {

        @Throws(DomainException::class)
        override suspend fun create(currentList: DiaryYearMonthList): DiaryYearMonthList {
            check(currentList.isNotEmpty)

            val loadingOffset = currentList.countDiaries()
            val loadedDiaryList =
                loadSavedDiaryList(NUM_LOADING_ITEMS, loadingOffset)
            val numLoadedDiaries = currentList.countDiaries() + loadedDiaryList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)
            return currentList.combineDiaryLists(loadedDiaryList, !existsUnloadedDiaries)
        }
    }

    private inner class UpdateDiaryListCreator : DiaryListCreator {

        @Throws(DomainException::class)
        override suspend fun create(currentList: DiaryYearMonthList): DiaryYearMonthList {
            check(currentList.isNotEmpty)

            var numLoadingItems = currentList.countDiaries()
            // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
            //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
            //      対策として下記コードを記述。
            if (numLoadingItems < NUM_LOADING_ITEMS) {
                numLoadingItems = NUM_LOADING_ITEMS
            }
            return loadSavedDiaryList(numLoadingItems, 0)
        }
    }

    @Throws(DomainException::class)
    private suspend fun loadSavedDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int
    ): DiaryYearMonthList {
        val result =
            fetchDiaryListUseCase(
                numLoadingItems,
                loadingOffset,
                sortConditionDate
            )
        val loadedDiaryList =
            when (result) {
                is UseCaseResult.Success -> result.value
                is UseCaseResult.Failure -> throw result.exception
            }

        if (loadedDiaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayListItemList: MutableList<DiaryDayListItem> = ArrayList()
        loadedDiaryList.stream()
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

        when (val result = deleteDiaryUseCase(date, uri)) {
            is UseCaseResult.Success -> {
                updateDiaryList(currentList)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                when (result.exception) {
                    is DeleteDiaryUseCaseException.DeleteDiaryFailed -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
                    }
                    is DeleteDiaryUseCaseException.RevokePersistentAccessUriFailed -> {
                        updateDiaryList(currentList)
                    }
                }
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

    private suspend fun navigateDiaryShowFragment(date: LocalDate) {
        emitViewModelEvent(DiaryListEvent.NavigateDiaryShowFragment(date))
    }

    private suspend fun navigateDiaryEditFragment() {
        val today = LocalDate.now()
        emitViewModelEvent(DiaryListEvent.NavigateDiaryEditFragment(today))
    }

    private suspend fun navigateWordSearchFragment() {
        emitViewModelEvent(DiaryListEvent.NavigateWordSearchFragment)
    }

    private suspend fun navigateStartYearMonthPickerDialog() {
        val newestDiaryDate = loadNewestSavedDiaryDate()
        val oldestDiaryDate = loadOldestSavedDiaryDate()
        if (newestDiaryDate == null) return
        if (oldestDiaryDate == null) return
        val newestYear = Year.of(newestDiaryDate.year)
        val oldestYear = Year.of(oldestDiaryDate.year)
        emitViewModelEvent(
            DiaryListEvent.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
        )
    }

    private suspend fun navigateDiaryDeleteDialog(date: LocalDate, uri: Uri?) {
        emitViewModelEvent(DiaryListEvent.NavigateDiaryDeleteDialog(date, uri))
    }
}
