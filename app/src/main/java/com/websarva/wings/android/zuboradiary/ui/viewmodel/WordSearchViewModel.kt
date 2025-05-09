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
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.WordSearchFragmentAction
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private var previousSearchWord = initialPreviousSearchWord // 二重検索防止用

    private val shouldLoadWordSearchResultList: Boolean
        get() = _searchWord.value != previousSearchWord

    private val initialWordSearchResultListLoadingJob: Job? = null
    private var wordSearchResultListLoadingJob: Job? = initialWordSearchResultListLoadingJob // キャンセル用

    // MEMO:RecyclerViewのスクロール時のアイテム追加更新処理の重複防止フラグ
    private val initialIsWordSearchResultLoading = false
    private var isWordSearchResultLoading = initialIsWordSearchResultLoading

    private val numLoadingItems = DiaryListViewModel.NUM_LOADING_ITEMS
    private val initialWordSearchResultList = WordSearchResultYearMonthList()
    private val _wordSearchResultList = MutableStateFlow(initialWordSearchResultList)
    val wordSearchResultList
        get() = _wordSearchResultList.asStateFlow()

    private val initialNumWordSearchResults = 0
    private val _numWordSearchResults = MutableStateFlow(initialNumWordSearchResults)
    val numWordSearchResults
        get() = _numWordSearchResults.asStateFlow()

    // MEMO:画面回転時の不要なアップデートを防ぐ
    private val initialShouldUpdateWordSearchResultList = false
    private var shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private val initialIsVisibleUpdateProgressBar = false
    private val _isVisibleUpdateProgressBar = MutableStateFlow(initialIsVisibleUpdateProgressBar)
    val isVisibleUpdateProgressBar
        get() = _isVisibleUpdateProgressBar.asStateFlow()

    // MEMO:WordSearchViewModelのスコープ範囲はActivityになるが、
    //      WordSearchFragment、DiaryShowFragment、DiaryEditFragment、
    //      DiaryItemTitleEditFragment表示時のみ ViewModelのプロパティ値を保持できたらよいので、
    //      WordSearchFragmentを破棄するタイミングでViewModelのプロパティ値を初期化する。
    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroyed = false
    private var shouldInitializeOnFragmentDestroyed = initialShouldInitializeOnFragmentDestroyed

    // Fragment処理
    private val initialFragmentAction = FragmentAction.None
    private val _fragmentAction: MutableStateFlow<FragmentAction> =
        MutableStateFlow(initialFragmentAction)
    val fragmentAction: StateFlow<FragmentAction>
        get() = _fragmentAction

    override fun initialize() {
        super.initialize()
        _searchWord.value = initialSearchWord
        previousSearchWord = initialPreviousSearchWord
        cancelPreviousLoading()
        wordSearchResultListLoadingJob = initialWordSearchResultListLoadingJob
        isWordSearchResultLoading = initialIsWordSearchResultLoading
        _wordSearchResultList.value = initialWordSearchResultList
        _numWordSearchResults.value = initialNumWordSearchResults
        shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList
        _isVisibleUpdateProgressBar.value = initialIsVisibleUpdateProgressBar
        shouldInitializeOnFragmentDestroyed = initialShouldInitializeOnFragmentDestroyed
    }

    // Viewクリック処理
    fun onSearchWordClearButtonClicked() {
        clearSearchWord()
    }

    fun onWordSearchResultListItemClicked(date: LocalDate) {
        navigateDiaryShowFragment(date)
    }

    // View状態処理
    fun onWordSearchResultListEndScrolled() {
        if (isWordSearchResultLoading) return
        loadAdditionWordSearchResultList()
    }

    fun onWordSearchResultListUpdated() {
        clearIsWordSearchResultUpdating()
    }

    // Fragment状態処理
    fun onNextFragmentNavigated() {
        shouldUpdateWordSearchResultList = true
    }

    fun onPreviousFragmentNavigated() {
        shouldInitializeOnFragmentDestroyed = true
    }

    fun onFragmentViewCreated() {
        prepareKeyboard()
    }

    fun onFragmentDestroyed() {
        if (shouldInitializeOnFragmentDestroyed) initialize()
    }

    // StateFlow値変更処理
    fun onSearchWordChanged() {
        if (shouldUpdateWordSearchResultList) {
            shouldUpdateWordSearchResultList = false
            val list = wordSearchResultList.value
            if (list.isEmpty) return
            updateWordSearchResultList()
            return
        }

        // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
        //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
        if (!shouldLoadWordSearchResultList) return

        val value = searchWord.value
        if (value.isEmpty()) {
            initialize()
        } else {
            loadNewWordSearchResultList()
        }

        previousSearchWord = value
    }

    fun onWordSearchResultListChanged() {
        val searchWord = searchWord.value
        val wordSearchResultList = wordSearchResultList.value
        if (searchWord.isEmpty()) {
            showResultsInitialLayout()
        } else if (wordSearchResultList.isEmpty) {
            showNoResultsLayout()
        } else {
            showResultsLayout()
        }
    }

    fun onFragmentActionChanged() {
        clearFragmentAction()
    }

    // データ処理
    private fun prepareKeyboard() {
        val searchWord = searchWord.value
        if (searchWord.isEmpty()) showKeyboard()
    }

    private fun loadNewWordSearchResultList() {
        loadWordSearchResultDiaryList(
            NewWordSearchResultListCreator()
        )
    }

    private fun loadAdditionWordSearchResultList() {
        loadWordSearchResultDiaryList(
            AddedWordSearchResultListCreator()
        )
    }

    private fun updateWordSearchResultList() {
        loadWordSearchResultDiaryList(
            UpdateWordSearchResultListCreator()
        )
    }

    private fun loadWordSearchResultDiaryList(
        creator: WordSearchResultListCreator
    ) {
        cancelPreviousLoading()
        wordSearchResultListLoadingJob =
            viewModelScope.launch(Dispatchers.IO) {
                createWordSearchResultList(creator)
            }
        isWordSearchResultLoading = true
    }

    private fun cancelPreviousLoading() {
        val job = wordSearchResultListLoadingJob ?: return
        if (!job.isCompleted) {
            wordSearchResultListLoadingJob?.cancel() ?: throw IllegalStateException()
        }
    }

    private suspend fun createWordSearchResultList(
        resultListCreator: WordSearchResultListCreator
    ) {
        val logMsg = "ワード検索結果読込"
        Log.i(logTag, "${logMsg}_開始")
        val previousResultList = _wordSearchResultList.requireValue()
        try {
            val updateResultList = resultListCreator.create()
            _wordSearchResultList.value = updateResultList
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _wordSearchResultList.value = previousResultList
            addAppMessage(WordSearchAppMessage.SearchResultListLoadingFailure)
        }
    }

    private fun interface WordSearchResultListCreator {
        @Throws(Exception::class)
        suspend fun create(): WordSearchResultYearMonthList
    }

    private inner class NewWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(): WordSearchResultYearMonthList {
            showWordSearchResultListFirstItemProgressIndicator()
            return loadWordSearchResultDiaryList(numLoadingItems, 0)
        }

        private fun showWordSearchResultListFirstItemProgressIndicator() {
            val list = WordSearchResultYearMonthList(false)
            _wordSearchResultList.value = list
            _numWordSearchResults.value = 0
        }
    }

    private inner class AddedWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.requireValue()
            check(currentResultList.isNotEmpty)

            val loadingOffset = currentResultList.countDiaries()
            val loadedResultList =
                loadWordSearchResultDiaryList(numLoadingItems, loadingOffset)
            val numLoadedDiaries =
                currentResultList.countDiaries() + loadedResultList.countDiaries()
            val existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries)
            return currentResultList.combineDiaryLists(loadedResultList, !existsUnloadedDiaries)
        }
    }

    private inner class UpdateWordSearchResultListCreator : WordSearchResultListCreator {

        @Throws(Exception::class)
        override suspend fun create(): WordSearchResultYearMonthList {
            val currentResultList = _wordSearchResultList.requireValue()
            check(currentResultList.isNotEmpty)

            _isVisibleUpdateProgressBar.value = true
            try {
                var numLoadingItems = currentResultList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < this@WordSearchViewModel.numLoadingItems) {
                    numLoadingItems = this@WordSearchViewModel.numLoadingItems
                }
                return loadWordSearchResultDiaryList(numLoadingItems, 0)
            } finally {
                _isVisibleUpdateProgressBar.value = false
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun loadWordSearchResultDiaryList(
        numLoadingItems: Int,
        loadingOffset: Int
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
                WordSearchResultDayListItem(x, searchWord)
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

    // FragmentAction処理
    private fun updateFragmentAction(action: FragmentAction) {
        _fragmentAction.value = action
    }

    private fun navigateDiaryShowFragment(date: LocalDate) {
        updateFragmentAction(WordSearchFragmentAction.NavigateDiaryShowFragment(date))
    }

    private fun showKeyboard() {
        updateFragmentAction(WordSearchFragmentAction.ShowKeyboard)
    }

    private fun showResultsInitialLayout() {
        updateFragmentAction(WordSearchFragmentAction.ShowResultsInitialLayout)
    }

    private fun showResultsLayout() {
        updateFragmentAction(WordSearchFragmentAction.ShowResultsLayout)
    }

    private fun showNoResultsLayout() {
        updateFragmentAction(WordSearchFragmentAction.ShowNoResultsLayout)
    }

    // クリア処理
    private fun clearSearchWord() {
        _searchWord.value = initialSearchWord
    }

    private fun clearIsWordSearchResultUpdating() {
        isWordSearchResultLoading = initialIsWordSearchResultLoading
    }

    private fun clearFragmentAction() {
        _fragmentAction.value = initialFragmentAction
    }
}
