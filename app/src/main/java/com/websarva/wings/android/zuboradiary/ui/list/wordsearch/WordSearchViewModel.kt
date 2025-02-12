package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject

@HiltViewModel
class WordSearchViewModel @Inject internal constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    companion object {
        private const val NUM_LOADING_ITEMS = DiaryListViewModel.NUM_LOADING_ITEMS
    }

    private val _searchWord: MutableLiveData<String> = MutableLiveData()
    val searchWord: LiveData<String>
        get() = _searchWord
    /**
     * LayoutDataBinding用
     * */
    val searchWordMutable: MutableLiveData<String>
        get() = _searchWord

    private var wordSearchResultListLoadingFuture: Future<*>? = null // キャンセル用

    private val _wordSearchResultList = MutableLiveData<WordSearchResultYearMonthList>()
    val wordSearchResultList: LiveData<WordSearchResultYearMonthList>
        get() = _wordSearchResultList

    private val _numWordSearchResults = MutableLiveData<Int>()
    val numWordSearchResults: LiveData<Int>
        get() = _numWordSearchResults

    val canLoadWordSearchResultList: Boolean
        get() {
            Log.d("OnScrollDiaryList", "isLoadingDiaryList()")
            return wordSearchResultListLoadingFuture?.isDone ?: true
        }

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val _isVisibleUpdateProgressBar = MutableLiveData<Boolean>()
    val isVisibleUpdateProgressBar: LiveData<Boolean>
        get() = _isVisibleUpdateProgressBar

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    private val isValidityDelay = true // TODO:調整用

    init {
        initialize()
    }

    public override fun initialize() {
        initializeAppMessageList()
        _searchWord.value = ""
        _wordSearchResultList.value = WordSearchResultYearMonthList()
        _numWordSearchResults.value = 0
        _isVisibleUpdateProgressBar.value = false
        cancelPreviousLoading()
        wordSearchResultListLoadingFuture = null
    }

    fun loadNewWordSearchResultList(
        spannableStringColor: Int,
        spannableStringBackgroundColor: Int
    ) {
        loadWordSearchResultDiaryList(
            NewWordSearchResultListCreator(),
            spannableStringColor,
            spannableStringBackgroundColor
        )
    }

    fun loadAdditionWordSearchResultList(
        spannableStringColor: Int,
        spannableStringBackgroundColor: Int
    ) {
        loadWordSearchResultDiaryList(
            AddedWordSearchResultListCreator(),
            spannableStringColor,
            spannableStringBackgroundColor
        )
    }

    fun updateWordSearchResultList(
        spannableStringColor: Int,
        spannableStringBackgroundColor: Int
    ) {
        loadWordSearchResultDiaryList(
            UpdateWordSearchResultListCreator(),
            spannableStringColor,
            spannableStringBackgroundColor
        )
    }

    private fun loadWordSearchResultDiaryList(
        creator: WordSearchResultListCreator,
        spannableStringColor: Int,
        spannableStringBackgroundColor: Int
    ) {
        cancelPreviousLoading()
        val loadWordSearchResultList =
            WordSearchResultListLoadingRunnable(
                creator, spannableStringColor, spannableStringBackgroundColor
            )
        wordSearchResultListLoadingFuture = executorService.submit(loadWordSearchResultList)
    }

    private fun cancelPreviousLoading() {
        if (!canLoadWordSearchResultList) {
            Log.d("WordSearchLoading", "Cancel")
            wordSearchResultListLoadingFuture?.cancel(true) ?: throw IllegalStateException()
        }
    }

    private interface WordSearchResultListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList
    }

    private inner class WordSearchResultListLoadingRunnable(
        private val resultListCreator: WordSearchResultListCreator,
        private val spannableStringColor: Int,
        private val spannableStringBackGroundColor: Int
    ) : Runnable {

        override fun run() {
            Log.d("WordSearchLoading", "run()_start")
            val previousResultList = _wordSearchResultList.checkNotNull()

            try {
                val updateResultList =
                    resultListCreator.create(spannableStringColor, spannableStringBackGroundColor)
                _wordSearchResultList.postValue(updateResultList)
            } catch (e: CancellationException) {
                Log.d("Exception", "ワード検索結果読込キャンセル", e)
                // 例外処理なし
            } catch (e: InterruptedException) {
                Log.d("Exception", "ワード検索結果読込キャンセル", e)
                if (!isValidityDelay) {
                    _wordSearchResultList.postValue(previousResultList)
                    addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                }
            } catch (e: Exception) {
                Log.d("Exception", "ワード検索結果読込キャンセル", e)
                _wordSearchResultList.postValue(previousResultList)
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            }
        }
    }

    private inner class NewWordSearchResultListCreator : WordSearchResultListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            showWordSearchResultListFirstItemProgressIndicator()
            if (isValidityDelay) Thread.sleep(1000)
            return loadWordSearchResultDiaryList(
                NUM_LOADING_ITEMS, 0, spannableStringColor, spannableStringBackGroundColor
            )
        }

        fun showWordSearchResultListFirstItemProgressIndicator() {
            val list = WordSearchResultYearMonthList(false)
            _wordSearchResultList.postValue(list)
            _numWordSearchResults.postValue(0)
        }
    }

    private inner class AddedWordSearchResultListCreator : WordSearchResultListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.checkNotNull()
            check(currentResultList.wordSearchResultYearMonthListItemList.isNotEmpty())

            if (isValidityDelay) Thread.sleep(1000)
            val loadingOffset = currentResultList.countDiaries()
            val loadedResultList =
                loadWordSearchResultDiaryList(
                    NUM_LOADING_ITEMS,
                    loadingOffset,
                    spannableStringColor,
                    spannableStringBackGroundColor
                )
            val numLoadedDiaries =
                currentResultList.countDiaries() + loadedResultList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)
            return currentResultList.combineDiaryLists(loadedResultList, !existsUnloadedDiaries)
        }
    }

    private inner class UpdateWordSearchResultListCreator : WordSearchResultListCreator {
        @Throws(
            CancellationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        override fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.checkNotNull()
            check(currentResultList.wordSearchResultYearMonthListItemList.isNotEmpty())

            _isVisibleUpdateProgressBar.postValue(true)
            try {
                if (isValidityDelay) Thread.sleep(3000)
                var numLoadingItems = currentResultList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < NUM_LOADING_ITEMS) {
                    numLoadingItems = NUM_LOADING_ITEMS
                }
                return loadWordSearchResultDiaryList(
                    numLoadingItems, 0, spannableStringColor, spannableStringBackGroundColor
                )
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
    private fun loadWordSearchResultDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int,
        spannableStringColor: Int,
        spannableStringBackGroundColor: Int
    ): WordSearchResultYearMonthList {
        require(numLoadingItems > 0)
        require(loadingOffset >= 0)

        val searchWord = _searchWord.checkNotNull()

        val listenableFutureResults =
            diaryRepository.loadWordSearchResultDiaryList(
                numLoadingItems, loadingOffset, searchWord
            )

        val loadedResultList = listenableFutureResults.get()

        if (loadedResultList.isEmpty()) return WordSearchResultYearMonthList()
        val resultDayListItemList: MutableList<WordSearchResultDayListItem> = ArrayList()
        loadedResultList.stream().forEach { x: WordSearchResultListItem ->
            resultDayListItemList.add(
                WordSearchResultDayListItem(
                    x, searchWord, spannableStringColor, spannableStringBackGroundColor
                )
            )
        }
        val resultDayList = WordSearchResultDayList(resultDayListItemList)
        val existsUnloadedDiaries = existsUnloadedDiaries(resultDayList.countDiaries())
        return WordSearchResultYearMonthList(resultDayList, !existsUnloadedDiaries)
    }

    @Throws(
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    private fun existsUnloadedDiaries(numLoadedDiaries: Int): Boolean {
        val searchWord = _searchWord.checkNotNull()

        val numExistingDiaries = diaryRepository.countWordSearchResultDiaries(searchWord).get()
        _numWordSearchResults.postValue(numExistingDiaries)
        if (numExistingDiaries <= 0) return false

        return numLoadedDiaries < numExistingDiaries
    }

    override fun onCleared() {
        super.onCleared()
        executorService.shutdown()
    }
}
