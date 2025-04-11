package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.appmessage.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.ui.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class DiaryListViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    companion object {
        const val NUM_LOADING_ITEMS: Int = 10 //初期読込時の対象リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    }

    private val logTag = createLogTag()

    private val initialDiaryListLoadingJob: Job? = null
    private var diaryListLoadingJob: Job? = initialDiaryListLoadingJob // キャンセル用

    private val initialDiaryList = DiaryYearMonthList()
    private val _diaryList = MutableStateFlow(initialDiaryList)
    val diaryList
        get() = _diaryList.asStateFlow()

    val canLoadDiaryList: Boolean
        get() {
            val result =
                if (_isLoadingDiaryList.value) {
                    false
                } else {
                    diaryListLoadingJob?.isCompleted ?: true
                }
            Log.d(logTag, "canLoadDiaryList() = $result")
            return result
        }

    private val initialIsLoadingDiaryList = false
    private var _isLoadingDiaryList = MutableStateFlow(initialIsLoadingDiaryList)
    val isLoadingDiaryList
        get() = _isLoadingDiaryList.asStateFlow()

    // MEMO:画面回転時の不要なアップデートを防ぐ
    private val initialShouldUpdateDiaryList = false
    var shouldUpdateDiaryList = initialShouldUpdateDiaryList

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val initialIsVisibleUpdateProgressBar = false
    private val _isVisibleUpdateProgressBar = MutableStateFlow(initialIsVisibleUpdateProgressBar)
    val isVisibleUpdateProgressBar
        get() = _isVisibleUpdateProgressBar.asStateFlow()

    private val initialSortConditionDate: LocalDate? = null
    private var sortConditionDate: LocalDate? = initialSortConditionDate

    private val isValidityDelay = true // TODO:調整用

    override fun initialize() {
        super.initialize()
        diaryListLoadingJob = initialDiaryListLoadingJob
        _diaryList.value = initialDiaryList
        _isLoadingDiaryList.value = initialIsLoadingDiaryList
        shouldUpdateDiaryList = initialShouldUpdateDiaryList
        _isVisibleUpdateProgressBar.value = initialIsVisibleUpdateProgressBar
        sortConditionDate = initialSortConditionDate
    }

    suspend fun loadDiaryListOnSetUp() {
        val diaryList = this.diaryList.value
        if (diaryList.diaryYearMonthListItemList.isEmpty()) {
            val numSavedDiaries = diaryRepository.countDiaries()
            if (numSavedDiaries >= 1) loadNewDiaryList()
        } else {
            if (!shouldUpdateDiaryList) return

            updateDiaryList()
        }
    }

    fun loadNewDiaryList() {
        loadDiaryList(NewDiaryListCreator())
    }

    fun loadAdditionDiaryList() {
        loadDiaryList(AddedDiaryListCreator())
    }

    private fun updateDiaryList() {
        loadDiaryList(UpdateDiaryListCreator())
        shouldUpdateDiaryList = false
    }

    // MEMO:List読込JobをViewModel側で管理(読込重複防止)
    private fun loadDiaryList(creator: DiaryListCreator) {
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
                createDiaryList(creator)
            }
        _isLoadingDiaryList.value = true
    }

    private fun cancelPreviousLoading() {
        if (!canLoadDiaryList) {
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
            Log.e(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _diaryList.value = previousDiaryList
            addAppMessage(DiaryListAppMessage.DiaryListLoadingFailure)
        }
    }

    private interface DiaryListCreator {
        @Throws(Exception::class)
        suspend fun create(): DiaryYearMonthList
    }

    private inner class NewDiaryListCreator : DiaryListCreator {

        @Throws(Exception::class)
        override suspend fun create(): DiaryYearMonthList {
            showDiaryListFirstItemProgressIndicator()
            if (isValidityDelay) delay(1000)
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
            check(currentDiaryList.diaryYearMonthListItemList.isNotEmpty())

            if (isValidityDelay) delay(1000)
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
            check(currentDiaryList.diaryYearMonthListItemList.isNotEmpty())

            _isVisibleUpdateProgressBar.value = true
            try {
                if (isValidityDelay) delay(3000)
                var numLoadingItems = currentDiaryList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < NUM_LOADING_ITEMS) {
                    numLoadingItems = NUM_LOADING_ITEMS
                }
                return loadSavedDiaryList(numLoadingItems, 0)
            } finally {
                _isVisibleUpdateProgressBar.value = false
            }
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

    fun updateSortConditionDate(yearMonth: YearMonth) {
        Log.i(logTag, "日記リスト先頭年月更新 = $yearMonth")
        sortConditionDate = yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
    }

    suspend fun deleteDiary(date: LocalDate): Boolean {
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

    suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadNewestDiary() ?: return null
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            Log.e(logTag, "最新日記読込_失敗", e)
            addAppMessage(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadOldestDiary() ?: return null
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            Log.e(logTag, "最古日記読込_失敗", e)
            addAppMessage(DiaryListAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    fun clearIsLoadingDiaryList() {
        _isLoadingDiaryList.value = false
    }
}
