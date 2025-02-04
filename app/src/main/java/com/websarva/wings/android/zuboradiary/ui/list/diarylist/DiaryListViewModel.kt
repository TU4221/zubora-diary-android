package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    companion object {
        const val NUM_LOADING_ITEMS: Int = 10 //初期読込時の対象リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    }

    private var diaryListLoadingFuture: Future<*>? = null // キャンセル用

    private val _diaryList = MutableLiveData<DiaryYearMonthList>()
    val diaryList: LiveData<DiaryYearMonthList>
        get() = _diaryList

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val _isVisibleUpdateProgressBar = MutableLiveData<Boolean>()
    val isVisibleUpdateProgressBar: LiveData<Boolean>
        get() = _isVisibleUpdateProgressBar

    private var sortConditionDate: LocalDate? = null
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

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

    fun canLoadDiaryList(): Boolean {
        Log.d("OnScrollDiaryList", "isLoadingDiaryList()")
        return diaryListLoadingFuture?.isDone ?: true
    }

    fun loadNewDiaryList() {
        loadSavedDiaryList(NewDiaryListCreator())
    }

    fun loadAdditionDiaryList() {
        loadSavedDiaryList(AddedDiaryListCreator())
    }

    fun updateDiaryList() {
        loadSavedDiaryList(UpdateDiaryListCreator())
    }

    private fun loadSavedDiaryList(creator: DiaryListCreator) {
        Log.d("DiaryListLoading", "loadDiaryList()")
        cancelPreviousLoading()
        val runnable = DiaryListLoadingRunnable(creator)
        diaryListLoadingFuture = executorService.submit(runnable)
    }

    private fun cancelPreviousLoading() {
        if (!canLoadDiaryList()) {
            diaryListLoadingFuture?.cancel(true) ?: throw IllegalStateException()
        }
    }

    private interface DiaryListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        fun create(): DiaryYearMonthList
    }

    private inner class DiaryListLoadingRunnable(private val diaryListCreator: DiaryListCreator)
        : Runnable {

        override fun run() {
            Log.d("DiaryListLoading", "DiaryListLoadingRunnable.run()")
            val previousDiaryList = _diaryList.checkNotNull()
            try {
                val updateDiaryList = diaryListCreator.create()
                Log.d("DiaryListLoading", "diaryList.postValue()")
                _diaryList.postValue(updateDiaryList)
            } catch (e: CancellationException) {
                Log.d("Exception", "日記読込キャンセル", e)
                // 例外処理なし
            } catch (e: InterruptedException) {
                Log.d("Exception", "日記読込失敗", e)
                if (!isValidityDelay) {
                    _diaryList.postValue(previousDiaryList)
                    addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                }
            } catch (e: Exception) {
                Log.d("Exception", "日記読込失敗", e)
                _diaryList.postValue(previousDiaryList)
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            }
        }
    }

    private inner class NewDiaryListCreator : DiaryListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(): DiaryYearMonthList {
            showDiaryListFirstItemProgressIndicator()
            if (isValidityDelay) Thread.sleep(1000)
            return loadSavedDiaryList(NUM_LOADING_ITEMS, 0)
        }

        fun showDiaryListFirstItemProgressIndicator() {
            val list = DiaryYearMonthList(false)
            _diaryList.postValue(list)
        }
    }

    private inner class AddedDiaryListCreator : DiaryListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(): DiaryYearMonthList {
            val currentDiaryList = _diaryList.checkNotNull()
            check(currentDiaryList.diaryYearMonthListItemList.isNotEmpty())

            if (isValidityDelay) Thread.sleep(1000)
            val loadingOffset = currentDiaryList.countDiaries()
            val loadedDiaryList = loadSavedDiaryList(NUM_LOADING_ITEMS, loadingOffset)
            val numLoadedDiaries = currentDiaryList.countDiaries() + loadedDiaryList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)
            return currentDiaryList.combineDiaryLists(loadedDiaryList, !existsUnloadedDiaries)
        }
    }

    private inner class UpdateDiaryListCreator : DiaryListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(): DiaryYearMonthList {
            val currentDiaryList = _diaryList.checkNotNull()
            check(currentDiaryList.diaryYearMonthListItemList.isNotEmpty())

            _isVisibleUpdateProgressBar.postValue(true)
            try {
                if (isValidityDelay) Thread.sleep(3000)
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

    @Throws(
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    private fun loadSavedDiaryList(numLoadingItems: Int, loadingOffset: Int): DiaryYearMonthList {
        require(numLoadingItems > 0)
        require(loadingOffset >= 0)


        val listListenableFuture =
            diaryRepository.loadDiaryList(
                numLoadingItems,
                loadingOffset,
                sortConditionDate
            )

        val loadedDiaryList = listListenableFuture.get()
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

    @Throws(
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    private fun existsUnloadedDiaries(numLoadedDiaries: Int): Boolean {
        val numExistingDiaries = if (sortConditionDate == null) {
            diaryRepository.countDiaries().get()
        } else {
            diaryRepository.countDiaries(sortConditionDate).get()
        }
        if (numExistingDiaries <= 0) return false

        return numLoadedDiaries < numExistingDiaries
    }

    fun updateSortConditionDate(yearMonth: YearMonth) {
        sortConditionDate = yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
    }

    fun deleteDiary(date: LocalDate): Boolean {
        val result: Int
        try {
            result = diaryRepository.deleteDiary(date).get()
        } catch (e: CancellationException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }

        // 削除件数 = 1が正常
        if (result != 1) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }

        updateDiaryList()
        return true
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean {
        try {
            return !diaryRepository.existsPicturePath(uri).get()
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        }
    }

    fun countSavedDiaries(): Int? {
        try {
            return diaryRepository.countDiaries().get()
        } catch (e: CancellationException) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }


    fun loadNewestSavedDiary(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadNewestDiary().get()
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }

    fun loadOldestSavedDiary(): LocalDate? {
        try {
            val diaryEntity = diaryRepository.loadOldestDiary().get()
            val strDate = diaryEntity.date
            return LocalDate.parse(strDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }

    override fun onCleared() {
        super.onCleared()
        executorService.shutdown()
    }
}
