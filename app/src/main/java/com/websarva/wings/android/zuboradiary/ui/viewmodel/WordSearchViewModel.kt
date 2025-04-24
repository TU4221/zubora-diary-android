package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WordSearchViewModel @Inject internal constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    private val initialSearchWord = ""
    private val _searchWord = MutableStateFlow(initialSearchWord)
    val searchWord
        get() = _searchWord.asStateFlow()
    /**
     * LayoutDataBinding用
     * */
    val searchWordMutableStateFlow: MutableStateFlow<String>
        get() = _searchWord

    private val initialPreviousSearchWord = ""
    var previousSearchWord = initialPreviousSearchWord // 二重検索防止用

    val shouldLoadWordSearchResultList: Boolean
        get() = _searchWord.value != previousSearchWord

    private val initialWordSearchResultListLoadingJob: Job? = null
    private var wordSearchResultListLoadingJob: Job? = initialWordSearchResultListLoadingJob // キャンセル用

    private val numLoadingItems = DiaryListViewModel.NUM_LOADING_ITEMS
    private val initialWordSearchResultList = WordSearchResultYearMonthList()
    private val _wordSearchResultList = MutableStateFlow(initialWordSearchResultList)
    val wordSearchResultList
        get() = _wordSearchResultList.asStateFlow()

    private val initialNumWordSearchResults = 0
    private val _numWordSearchResults = MutableStateFlow(initialNumWordSearchResults)
    val numWordSearchResults
        get() = _numWordSearchResults.asStateFlow()

    val canLoadWordSearchResultList: Boolean
        get() {
            val result = wordSearchResultListLoadingJob?.isCompleted ?: true
            Log.d(logTag, "canLoadWordSearchResultList() = $result")
            return result
        }

    // MEMO:画面回転時の不要なアップデートを防ぐ
    private val initialShouldUpdateWordSearchResultList = false
    var shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val initialIsVisibleUpdateProgressBar = false
    private val _isVisibleUpdateProgressBar = MutableStateFlow(initialIsVisibleUpdateProgressBar)
    val isVisibleUpdateProgressBar
        get() = _isVisibleUpdateProgressBar.asStateFlow()

    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroy = false
    var shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy

    private val isValidityDelay = true // TODO:調整用

    override fun initialize() {
        super.initialize()
        _searchWord.value = initialSearchWord
        previousSearchWord = initialPreviousSearchWord
        cancelPreviousLoading()
        wordSearchResultListLoadingJob = initialWordSearchResultListLoadingJob
        _wordSearchResultList.value = initialWordSearchResultList
        _numWordSearchResults.value = initialNumWordSearchResults
        shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList
        _isVisibleUpdateProgressBar.value = initialIsVisibleUpdateProgressBar
        shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy
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
        shouldUpdateWordSearchResultList = false
    }

    // MEMO:List読込JobをViewModel側で管理(読込重複防止)
    private fun loadWordSearchResultDiaryList(
        creator: WordSearchResultListCreator,
        spannableStringColor: Int,
        spannableStringBackgroundColor: Int
    ) {
        cancelPreviousLoading()
        wordSearchResultListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
                createWordSearchResultList(
                    creator,
                    spannableStringColor,
                    spannableStringBackgroundColor
                )
            }
    }

    private fun cancelPreviousLoading() {
        if (!canLoadWordSearchResultList) {
            wordSearchResultListLoadingJob?.cancel() ?: throw IllegalStateException()
        }
    }

    private suspend fun createWordSearchResultList(
        resultListCreator: WordSearchResultListCreator,
        spannableStringColor: Int,
        spannableStringBackGroundColor: Int
    ) {
        val logMsg = "ワード検索結果読込"
        Log.i(logTag, "${logMsg}_開始")
        val previousResultList = _wordSearchResultList.requireValue()
        try {
            val updateResultList =
                resultListCreator.create(spannableStringColor, spannableStringBackGroundColor)
            _wordSearchResultList.value = updateResultList
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.e(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _wordSearchResultList.value = previousResultList
            addAppMessage(WordSearchAppMessage.SearchResultListLoadingFailure)
        }
    }

    private fun interface WordSearchResultListCreator {
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
                numLoadingItems,
                0,
                spannableStringColor,
                spannableStringBackGroundColor
            )
        }

        private fun showWordSearchResultListFirstItemProgressIndicator() {
            val list = WordSearchResultYearMonthList(false)
            _wordSearchResultList.value = list
            _numWordSearchResults.value = 0
        }
    }

    private inner class AddedWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(
            spannableStringColor: Int,
            spannableStringBackGroundColor: Int
        ): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.requireValue()
            check(currentResultList.isNotEmpty)

            if (isValidityDelay) delay(1000)
            val loadingOffset = currentResultList.countDiaries()
            val loadedResultList =
                loadWordSearchResultDiaryList(
                    numLoadingItems,
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
            val currentResultList = _wordSearchResultList.requireValue()
            check(currentResultList.isNotEmpty)

            _isVisibleUpdateProgressBar.value = true
            try {
                if (isValidityDelay) delay(3000)
                var numLoadingItems = currentResultList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < this@WordSearchViewModel.numLoadingItems) {
                    numLoadingItems = this@WordSearchViewModel.numLoadingItems
                }
                return loadWordSearchResultDiaryList(
                    numLoadingItems,
                    0,
                    spannableStringColor,
                    spannableStringBackGroundColor
                )
            } finally {
                _isVisibleUpdateProgressBar.value = false
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

        val searchWord = _searchWord.requireValue()
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
        val searchWord = _searchWord.requireValue()

        val numExistingDiaries = diaryRepository.countWordSearchResultDiaries(searchWord)
        _numWordSearchResults.value = numExistingDiaries
        if (numExistingDiaries <= 0) return false

        return numLoadedDiaries < numExistingDiaries
    }
}
