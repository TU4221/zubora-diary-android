package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private var wordSearchResultListLoadingJob: Job? = null // キャンセル用

    private val _wordSearchResultList = MutableLiveData<WordSearchResultYearMonthList>()
    val wordSearchResultList: LiveData<WordSearchResultYearMonthList>
        get() = _wordSearchResultList

    private val _numWordSearchResults = MutableLiveData<Int>()
    val numWordSearchResults: LiveData<Int>
        get() = _numWordSearchResults

    val canLoadWordSearchResultList: Boolean
        get() {
            Log.d("OnScrollDiaryList", "isLoadingDiaryList()")
            return wordSearchResultListLoadingJob?.isCompleted ?: true
        }

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val _isVisibleUpdateProgressBar = MutableLiveData<Boolean>()
    val isVisibleUpdateProgressBar: LiveData<Boolean>
        get() = _isVisibleUpdateProgressBar

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
        wordSearchResultListLoadingJob = null
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
        wordSearchResultListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
                loadSavedDiaryList(
                    creator,
                    spannableStringColor,
                    spannableStringBackgroundColor
                )
            }
    }

    private fun cancelPreviousLoading() {
        if (!canLoadWordSearchResultList) {
            Log.d("WordSearchLoading", "Cancel")
            wordSearchResultListLoadingJob?.cancel() ?: throw IllegalStateException()
        }
    }

    private suspend fun loadSavedDiaryList(
        resultListCreator: WordSearchResultListCreator,
        spannableStringColor: Int,
        spannableStringBackGroundColor: Int
    ) {
        val previousResultList = _wordSearchResultList.checkNotNull()

        try {
            val updateResultList =
                resultListCreator.create(spannableStringColor, spannableStringBackGroundColor)
            _wordSearchResultList.postValue(updateResultList)
        } catch (e: Exception) {
            Log.d("Exception", "ワード検索結果読込キャンセル", e)
            _wordSearchResultList.postValue(previousResultList)
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
        }
    }

    private interface WordSearchResultListCreator {
        @Throws(Exception::class)
        suspend fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList
    }

    private inner class NewWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            showWordSearchResultListFirstItemProgressIndicator()
            if (isValidityDelay) delay(1000)
            return loadWordSearchResultDiaryList(
                NUM_LOADING_ITEMS,
                0,
                spannableStringColor,
                spannableStringBackGroundColor
            )
        }

        fun showWordSearchResultListFirstItemProgressIndicator() {
            val list = WordSearchResultYearMonthList(false)
            _wordSearchResultList.postValue(list)
            _numWordSearchResults.postValue(0)
        }
    }

    private inner class AddedWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.checkNotNull()
            check(currentResultList.wordSearchResultYearMonthListItemList.isNotEmpty())

            if (isValidityDelay) delay(1000)
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

        @Throws(Exception::class)
        override suspend fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.checkNotNull()
            check(currentResultList.wordSearchResultYearMonthListItemList.isNotEmpty())

            _isVisibleUpdateProgressBar.postValue(true)
            try {
                if (isValidityDelay) delay(3000)
                var numLoadingItems = currentResultList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < NUM_LOADING_ITEMS) {
                    numLoadingItems = NUM_LOADING_ITEMS
                }
                return loadWordSearchResultDiaryList(
                    numLoadingItems,
                    0,
                    spannableStringColor,
                    spannableStringBackGroundColor
                )
            } finally {
                _isVisibleUpdateProgressBar.postValue(false)
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun loadWordSearchResultDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int,
        spannableStringColor: Int,
        spannableStringBackGroundColor: Int
    ): WordSearchResultYearMonthList {
        require(numLoadingItems > 0)
        require(loadingOffset >= 0)

        val searchWord = _searchWord.checkNotNull()
        val loadedResultList =
            diaryRepository.loadWordSearchResultDiaryList(
                numLoadingItems,
                loadingOffset,
                searchWord
            )

        if (loadedResultList.isEmpty()) return WordSearchResultYearMonthList()

        val resultDayListItemList: MutableList<WordSearchResultDayListItem> = ArrayList()
        loadedResultList.stream().forEach { x: WordSearchResultListItem ->
            resultDayListItemList.add(
                WordSearchResultDayListItem(
                    x,
                    searchWord,
                    spannableStringColor,
                    spannableStringBackGroundColor
                )
            )
        }
        val resultDayList = WordSearchResultDayList(resultDayListItemList)
        val existsUnloadedDiaries = existsUnloadedDiaries(resultDayList.countDiaries())
        return WordSearchResultYearMonthList(resultDayList, !existsUnloadedDiaries)
    }

    @Throws(Exception::class)
    private suspend fun existsUnloadedDiaries(numLoadedDiaries: Int): Boolean {
        val searchWord = _searchWord.checkNotNull()

        val numExistingDiaries = diaryRepository.countWordSearchResultDiaries(searchWord)
        _numWordSearchResults.postValue(numExistingDiaries)
        if (numExistingDiaries <= 0) return false

        return numLoadedDiaries < numExistingDiaries
    }
}
