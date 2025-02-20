package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.notNullValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    companion object {
        const val NUM_LOADING_ITEMS: Int = 10 //初期読込時の対象リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    }

    private var diaryListLoadingJob: Job? = null // キャンセル用

    private val _diaryList = MutableLiveData<DiaryYearMonthList>()
    val diaryList: LiveData<DiaryYearMonthList>
        get() = _diaryList

    val canLoadDiaryList: Boolean
        get() {
        Log.d("OnScrollDiaryList", "isLoadingDiaryList()")
        return diaryListLoadingJob?.isCompleted ?: true
        }

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val _isVisibleUpdateProgressBar = MutableLiveData<Boolean>()
    val isVisibleUpdateProgressBar: LiveData<Boolean>
        get() = _isVisibleUpdateProgressBar

    private var sortConditionDate: LocalDate? = null

    private val isValidityDelay = true // TODO:調整用

    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        _diaryList.value = DiaryYearMonthList()
        _isVisibleUpdateProgressBar.value = false
        sortConditionDate = null
    }

    suspend fun loadDiaryListOnSetUp() {
        val diaryList = diaryList.notNullValue()
        if (diaryList.diaryYearMonthListItemList.isEmpty()) {
            try {
                val numSavedDiaries = diaryRepository.countDiaries()
                if (numSavedDiaries >= 1) loadNewDiaryList()
            } catch (e: Exception) {
                addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            }
        } else {
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
    }

    // TODO:suspend関数にしたいがJobをViewModel側で管理したい。後日検討。
    private fun loadDiaryList(creator: DiaryListCreator) {
        Log.d("DiaryListLoading", "loadDiaryList()")
        cancelPreviousLoading()
        diaryListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
                createDiaryList(creator)
            }
    }

    private fun cancelPreviousLoading() {
        if (!canLoadDiaryList) {
            diaryListLoadingJob?.cancel() ?: throw IllegalStateException()
        }
    }

    private suspend fun createDiaryList(creator: DiaryListCreator) {
        val previousDiaryList = _diaryList.checkNotNull()
        try {
            val updateDiaryList = creator.create()
            _diaryList.postValue(updateDiaryList)
        } catch (e: Exception) {
            Log.d("Exception", "日記読込失敗", e)
            _diaryList.postValue(previousDiaryList)
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
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

        fun showDiaryListFirstItemProgressIndicator() {
            val list = DiaryYearMonthList(false)
            _diaryList.postValue(list)
        }
    }

    private inner class AddedDiaryListCreator : DiaryListCreator {

        @Throws(CancellationException::class)
        override suspend fun create(): DiaryYearMonthList {
            val currentDiaryList = _diaryList.checkNotNull()
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
            val currentDiaryList = _diaryList.checkNotNull()
            check(currentDiaryList.diaryYearMonthListItemList.isNotEmpty())

            _isVisibleUpdateProgressBar.postValue(true)
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
                _isVisibleUpdateProgressBar.postValue(false)
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
        sortConditionDate = yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
    }

    suspend fun deleteDiary(date: LocalDate): Boolean {
        try {
            diaryRepository.deleteDiary(date)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }

        updateDiaryList()
        return true
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    suspend fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean? {
        try {
            return !diaryRepository.existsPicturePath(uri)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return null
        }
    }

    suspend fun loadNewestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadNewestDiary()
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }

    suspend fun loadOldestSavedDiaryDate(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadOldestDiary()
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }
}
