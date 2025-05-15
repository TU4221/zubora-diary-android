package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryListState
import com.websarva.wings.android.zuboradiary.ui.model.action.DiaryListFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class DiaryListViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    companion object {
        // MEMO:初期読込時の対象リストが画面全体に表示される値にすること。
        //      アイテム数が少ないと最後尾のプログラスインディケーターが表示される為。
        const val NUM_LOADING_ITEMS: Int = 14/*日(2週間分)*/
    }

    private val logTag = createLogTag()

    private val initialDiaryListState = DiaryListState.Idle
    private val _diaryListState = MutableStateFlow<DiaryListState>(initialDiaryListState)
    val  diaryListState
        get() = _diaryListState.asStateFlow()

    private val initialDiaryListLoadingJob: Job? = null
    private var diaryListLoadingJob: Job? = initialDiaryListLoadingJob // キャンセル用

    private val initialDiaryList = DiaryYearMonthList()
    private val _diaryList = MutableStateFlow(initialDiaryList)
    val diaryList
        get() = _diaryList.asStateFlow()

    private val initialSortConditionDate: LocalDate? = null
    private var sortConditionDate: LocalDate? = initialSortConditionDate

    // Fragment処理
    private val _fragmentAction = MutableSharedFlow<FragmentAction>()
    val fragmentAction
        get() = _fragmentAction.asSharedFlow()

    override fun initialize() {
        super.initialize()
        _diaryListState.value = initialDiaryListState
        diaryListLoadingJob = initialDiaryListLoadingJob
        _diaryList.value = initialDiaryList
        sortConditionDate = initialSortConditionDate
    }

    // ViewClicked処理
    fun onWordSearchMenuClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            navigateWordSearchFragment()
        }
    }

    fun onNavigationClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            navigateStartYearMonthPickerDialog()
        }
    }

    fun onDiaryListItemClicked(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            navigateDiaryShowFragment(date)
        }
    }

    fun onDiaryListItemDeleteButtonClicked(date: LocalDate, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            navigateDiaryDeleteDialog(date, uri)
        }
    }

    fun onDiaryEditButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            navigateDiaryEditFragment()
        }
    }

    // View状態処理
    fun onDiaryListEndScrolled() {
        loadAdditionDiaryList()
    }

    fun onDiaryListUpdated(list: DiaryYearMonthList) {
        if (list.isNotEmpty) {
            _diaryListState.value = DiaryListState.Results
        } else {
            _diaryListState.value = DiaryListState.NoResults
        }
    }

    fun onFragmentViewCreated() {
        prepareDiaryList()
    }

    // Fragmentデータ受取処理
    fun onDataReceivedFromDatePickerDialog(yearMonth: YearMonth) {
        updateSortConditionDate(yearMonth)
        loadNewDiaryList()
    }

    fun onDataReceivedFromDiaryDeleteDialog(date: LocalDate, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSuccessful = deleteDiary(date)
            if (!isSuccessful) return@launch

            if (uri == null) return@launch
            _fragmentAction.emit(
                DiaryListFragmentAction.ReleasePersistablePermissionUri(uri)
            )
        }
    }

    private fun prepareDiaryList() {
        viewModelScope.launch(Dispatchers.IO) {
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
        _diaryListState.value = DiaryListState.NewLoading
    }

    private fun loadAdditionDiaryList() {
        if (_diaryListState.value == DiaryListState.AdditionLoading) return
        loadDiaryList(AddedDiaryListCreator())
        _diaryListState.value = DiaryListState.AdditionLoading
    }

    private fun updateDiaryList() {
        loadDiaryList(UpdateDiaryListCreator())
        _diaryListState.value = DiaryListState.Updating
    }

    private fun loadDiaryList(creator: DiaryListCreator) {
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
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
            val updateDiaryList = creator.create()
            _diaryList.value = updateDiaryList
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _diaryList.value = previousDiaryList
            addAppMessage(DiaryListAppMessage.DiaryListLoadingFailure)
        }
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
            diaryRepository.loadDiaryList(
                numLoadingItems,
                loadingOffset,
                sortConditionDate
            )

        if (loadedDiaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayListItemList: MutableList<DiaryDayListItem> = ArrayList()
        loadedDiaryList.stream().forEach { x: DiaryListItem ->
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
            addAppMessage(DiaryListAppMessage.DiaryDeleteFailure)
            return false
        }

        updateDiaryList()
        Log.i(logTag, "${logMsg}_完了")
        return true
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    suspend fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean? {
        try {
            return !diaryRepository.existsPicturePath(uri)
        } catch (e: Exception) {
            Log.e(logTag, "端末写真URI使用状況確認_失敗", e)
            addAppMessage(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadNewestDiary() ?: return null
            val dateString = diaryEntity.date
            return LocalDate.parse(dateString)
        } catch (e: Exception) {
            Log.e(logTag, "最新日記読込_失敗", e)
            addAppMessage(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadOldestDiary() ?: return null
            val dateString = diaryEntity.date
            return LocalDate.parse(dateString)
        } catch (e: Exception) {
            Log.e(logTag, "最古日記読込_失敗", e)
            addAppMessage(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    private suspend fun navigateDiaryShowFragment(date: LocalDate) {
        _fragmentAction.emit(DiaryListFragmentAction.NavigateDiaryShowFragment(date))
    }

    private suspend fun navigateDiaryEditFragment() {
        val today = LocalDate.now()
        _fragmentAction.emit(DiaryListFragmentAction.NavigateDiaryEditFragment(today))
    }

    private suspend fun navigateWordSearchFragment() {
        _fragmentAction.emit(DiaryListFragmentAction.NavigateWordSearchFragment)
    }

    private suspend fun navigateStartYearMonthPickerDialog() {
        val newestDiaryDate = loadNewestSavedDiaryDate()
        val oldestDiaryDate = loadOldestSavedDiaryDate()
        if (newestDiaryDate == null) return
        if (oldestDiaryDate == null) return
        val newestYear = Year.of(newestDiaryDate.year)
        val oldestYear = Year.of(oldestDiaryDate.year)
        _fragmentAction.emit(
            DiaryListFragmentAction.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
        )
    }

    private suspend fun navigateDiaryDeleteDialog(date: LocalDate, uri: Uri?) {
        _fragmentAction.emit(DiaryListFragmentAction.NavigateDiaryDeleteDialog(date, uri))
    }
}
