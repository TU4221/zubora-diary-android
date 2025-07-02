package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DiaryListItemDeleteResult
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
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
    private val diaryRepository: DiaryRepository,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase
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
        loadAdditionDiaryList()
    }

    fun onDiaryListUpdated() {
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }

    fun onFragmentViewCreated() {
        prepareDiaryList()
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
        loadNewDiaryList()
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
        viewModelScope.launch {
            val isSuccessful = deleteDiary(date)
            if (!isSuccessful) return@launch

            releaseUriPermissionUseCase(uri)
        }
    }

    private fun prepareDiaryList() {
        viewModelScope.launch {
            val diaryList = diaryList.value
            if (diaryList.isEmpty) {
                val numSavedDiaries = diaryRepository.countDiaries()
                if (numSavedDiaries >= 1) loadNewDiaryList()
            } else {
                updateDiaryList()
            }
        }
    }

    private fun loadNewDiaryList() {
        loadDiaryList(NewDiaryListCreator())
    }

    private fun loadAdditionDiaryList() {
        loadDiaryList(AddedDiaryListCreator())
    }

    private fun updateDiaryList() {
        loadDiaryList(UpdateDiaryListCreator())
    }

    private fun loadDiaryList(creator: DiaryListCreator) {
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch {
                createDiaryList(creator)
            }
    }

    private fun cancelPreviousLoading() {
        val job = diaryListLoadingJob ?: return
        if (!job.isCompleted) {
            diaryListLoadingJob?.cancel() ?: throw IllegalStateException()
        }
    }

    private suspend fun createDiaryList(creator: DiaryListCreator) {
        val logMsg = "日記リスト読込"
        Log.i(logTag, "${logMsg}_開始")

        val previousDiaryList = _diaryList.requireValue()
        try {
            updateWordSearchStatusOnListLoadingStart(creator)
            val updateDiaryList = creator.create()
            _diaryList.value = updateDiaryList
            updateWordSearchStatusOnListLoadingFinish(updateDiaryList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _diaryList.value = previousDiaryList
            updateWordSearchStatusOnListLoadingFinish(previousDiaryList)
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
        @Throws(Exception::class)
        suspend fun create(): DiaryYearMonthList
    }

    private inner class NewDiaryListCreator : DiaryListCreator {

        @Throws(Exception::class)
        override suspend fun create(): DiaryYearMonthList {
            showDiaryListFirstItemProgressIndicator()
            return loadSavedDiaryList(NUM_LOADING_ITEMS, 0)
        }

        private fun showDiaryListFirstItemProgressIndicator() {
            val list = DiaryYearMonthList(false)
            _diaryList.value = list
        }
    }

    private inner class AddedDiaryListCreator : DiaryListCreator {

        @Throws(CancellationException::class)
        override suspend fun create(): DiaryYearMonthList {
            val currentDiaryList = _diaryList.requireValue()
            check(currentDiaryList.isNotEmpty)

            val loadingOffset = currentDiaryList.countDiaries()
            val loadedDiaryList =
                loadSavedDiaryList(NUM_LOADING_ITEMS, loadingOffset)
            val numLoadedDiaries = currentDiaryList.countDiaries() + loadedDiaryList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)
            return currentDiaryList.combineDiaryLists(loadedDiaryList, !existsUnloadedDiaries)
        }
    }

    private inner class UpdateDiaryListCreator : DiaryListCreator {

        @Throws(Exception::class)
        override suspend fun create(): DiaryYearMonthList {
            val currentDiaryList = _diaryList.requireValue()
            check(currentDiaryList.isNotEmpty)

            var numLoadingItems = currentDiaryList.countDiaries()
            // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
            //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
            //      対策として下記コードを記述。
            if (numLoadingItems < NUM_LOADING_ITEMS) {
                numLoadingItems = NUM_LOADING_ITEMS
            }
            return loadSavedDiaryList(numLoadingItems, 0)
        }
    }

    @Throws(Exception::class)
    private suspend fun loadSavedDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int
    ): DiaryYearMonthList {
        require(numLoadingItems > 0)
        require(loadingOffset >= 0)

        val loadedDiaryList =
            diaryRepository.fetchDiaryList(
                numLoadingItems,
                loadingOffset,
                sortConditionDate
            )

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

    @Throws(Exception::class)
    private suspend fun existsUnloadedDiaries(numLoadedDiaries: Int): Boolean {
        val numExistingDiaries =
            if (sortConditionDate == null) {
                diaryRepository.countDiaries()
            } else {
                diaryRepository.countDiaries(
                    checkNotNull(sortConditionDate)
                )
            }

        if (numExistingDiaries <= 0) return false

        return numLoadedDiaries < numExistingDiaries
    }

    private fun updateSortConditionDate(yearMonth: YearMonth) {
        Log.i(logTag, "日記リスト先頭年月更新 = $yearMonth")
        sortConditionDate = yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
    }

    private suspend fun deleteDiary(date: LocalDate): Boolean {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")
        try {
            diaryRepository.deleteDiary(date)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
            return false
        }

        updateDiaryList()
        Log.i(logTag, "${logMsg}_完了")
        return true
    }

    private suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        try {
            val diary = diaryRepository.fetchNewestDiary() ?: return null
            return  diary.date
        } catch (e: Exception) {
            Log.e(logTag, "最新日記読込_失敗", e)
            emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        try {
            val diary = diaryRepository.fetchOldestDiary() ?: return null
            return diary.date
        } catch (e: Exception) {
            Log.e(logTag, "最古日記読込_失敗", e)
            emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
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
