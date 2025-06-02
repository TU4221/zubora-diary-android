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
import com.websarva.wings.android.zuboradiary.ui.model.state.WordSearchState
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.WordSearchFragmentAction
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class WordSearchViewModel @Inject internal constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    private val initialWordSearchState = WordSearchState.Idle
    private val _wordSearchState = MutableStateFlow<WordSearchState>(initialWordSearchState)
    val  wordSearchState
        get() = _wordSearchState.asStateFlow()

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

    // MEMO:画面回転時の不要なアップデートを防ぐ
    private val initialShouldUpdateWordSearchResultList = false
    private var shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList

    // MEMO:WordSearchViewModelのスコープ範囲はActivityになるが、
    //      WordSearchFragment、DiaryShowFragment、DiaryEditFragment、
    //      DiaryItemTitleEditFragment表示時のみ ViewModelのプロパティ値を保持できたらよいので、
    //      WordSearchFragmentを破棄するタイミングでViewModelのプロパティ値を初期化する。
    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroyed = false
    private var shouldInitializeOnFragmentDestroyed = initialShouldInitializeOnFragmentDestroyed

    // Fragment処理
    private val _fragmentAction = MutableSharedFlow<FragmentAction>()
    val fragmentAction
        get() = _fragmentAction.asSharedFlow()

    // 初回キーボード表示
    // HACK:WordSearchFragment表示時にFragmentActionでキーボードを表示させようとすると、
    //      SharedFlowが監視する前(フラグメントライフサイクがStarted前)に処理する可能性がある為、表示されないことがある。
    //      別途StateFlow変数で対応。確実な監視開始タイミングを取得できない為この方法で対応。
    private val initialShouldShowKeyboard = false
    private val _shouldShowKeyboard = MutableStateFlow(initialShouldShowKeyboard)
    val shouldShowKeyboard
        get() = _shouldShowKeyboard.asStateFlow()

    private val initialIsLoadingOnScrolled = false
    private var isLoadingOnScrolled = initialIsLoadingOnScrolled

    val isResultsVisible =
        wordSearchState.map { value: WordSearchState ->
            when (value) {
                WordSearchState.Idle,
                WordSearchState.NoResults -> false
                else -> true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isNumResultsVisible =
        combine(wordSearchState, isResultsVisible) { wordSearchState, isResultsVisible ->
            if (!isResultsVisible) return@combine false

            when (wordSearchState) {
                WordSearchState.Searching -> false
                else -> true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isNoResultsMessageVisible =
        wordSearchState.map { value: WordSearchState ->
            when (value) {
                WordSearchState.NoResults -> true
                else -> false
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    override fun initialize() {
        super.initialize()
        _wordSearchState.value = initialWordSearchState
        _searchWord.value = initialSearchWord
        previousSearchWord = initialPreviousSearchWord
        cancelPreviousLoading()
        wordSearchResultListLoadingJob = initialWordSearchResultListLoadingJob
        _wordSearchResultList.value = initialWordSearchResultList
        _numWordSearchResults.value = initialNumWordSearchResults
        shouldUpdateWordSearchResultList = initialShouldUpdateWordSearchResultList
        shouldInitializeOnFragmentDestroyed = initialShouldInitializeOnFragmentDestroyed
        _shouldShowKeyboard.value = initialShouldShowKeyboard
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            _fragmentAction.emit(FragmentAction.NavigatePreviousFragment)
        }
    }

    // Viewクリック処理
    fun onNavigationButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(FragmentAction.NavigatePreviousFragment)
        }
    }

    fun onSearchWordClearButtonClicked() {
        _searchWord.value = initialSearchWord
    }

    fun onWordSearchResultListItemClicked(date: LocalDate) {
        viewModelScope.launch {
            _fragmentAction.emit(WordSearchFragmentAction.NavigateDiaryShowFragment(date))
        }
    }

    // View状態処理
    fun onWordSearchResultListEndScrolled() {
        if (isLoadingOnScrolled) return
        isLoadingOnScrolled = true
        loadAdditionWordSearchResultList()
    }

    fun onWordSearchResultListUpdated() {
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }

    fun onShowedKeyboard() {
        _shouldShowKeyboard.value = initialShouldShowKeyboard
    }

    // Fragment状態処理
    fun onNextFragmentNavigated() {
        shouldUpdateWordSearchResultList = true
    }

    fun onPreviousFragmentNavigated() {
        shouldInitializeOnFragmentDestroyed = true
    }

    fun onFragmentDestroyed() {
        if (shouldInitializeOnFragmentDestroyed) initialize()
    }

    // StateFlow値変更処理
    fun onSearchWordChanged() {
        viewModelScope.launch {
            prepareKeyboard()

            if (shouldUpdateWordSearchResultList) {
                shouldUpdateWordSearchResultList = false
                val list = wordSearchResultList.value
                if (list.isEmpty) return@launch
                updateWordSearchResultList()
                return@launch
            }

            // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
            //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
            if (_searchWord.value == previousSearchWord) return@launch

            val value = searchWord.value
            if (value.isEmpty()) {
                clearWordSearchResultList()
            } else {
                loadNewWordSearchResultList()
            }

            previousSearchWord = value
        }
    }

    // データ処理
    private fun prepareKeyboard() {
        val searchWord = searchWord.value
        if (searchWord.isEmpty()) _shouldShowKeyboard.value = true
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
            viewModelScope.launch {
                createWordSearchResultList(creator)
            }
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
            updateWordSearchStatusOnSearchStart(resultListCreator)
            val updateResultList = resultListCreator.create()
            _wordSearchResultList.value = updateResultList
            updateWordSearchStatusOnSearchFinish(updateResultList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            // 処理なし
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            _wordSearchResultList.value = previousResultList
            updateWordSearchStatusOnSearchFinish(previousResultList)
            addAppMessage(WordSearchAppMessage.SearchResultListLoadingFailure)
        }
    }

    // MEMO:文字検索は処理途中でも再度検索できる仕様のため、createWordSearchResultList()処理内で状態更新を行う。
    //      再度検索時、createWordSearchResultList()処理前に状態更新を行うと一つ前の検索結果状態が上書きされる可能性あり。
    private fun updateWordSearchStatusOnSearchStart(creator: WordSearchResultListCreator) {
        _wordSearchState.value =
            when (creator) {
                is NewWordSearchResultListCreator -> WordSearchState.Searching
                is AddedWordSearchResultListCreator -> WordSearchState.AdditionLoading
                is UpdateWordSearchResultListCreator -> WordSearchState.Updating
                else -> throw IllegalArgumentException()
            }
    }

    private fun updateWordSearchStatusOnSearchFinish(list: WordSearchResultYearMonthList) {
        _wordSearchState.value =
            if (list.isNotEmpty) {
                WordSearchState.Results
            } else {
                WordSearchState.NoResults
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
            return loadWordSearchResultDiaryListFromDatabase(numLoadingItems, 0)
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
                loadWordSearchResultDiaryListFromDatabase(numLoadingItems, loadingOffset)
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

            try {
                var numLoadingItems = currentResultList.countDiaries()
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < this@WordSearchViewModel.numLoadingItems) {
                    numLoadingItems = this@WordSearchViewModel.numLoadingItems
                }
                return loadWordSearchResultDiaryListFromDatabase(numLoadingItems, 0)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun loadWordSearchResultDiaryListFromDatabase(
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

    private fun clearWordSearchResultList() {
        _wordSearchState.value = initialWordSearchState
        cancelPreviousLoading()
        wordSearchResultListLoadingJob = initialWordSearchResultListLoadingJob
        _wordSearchResultList.value = initialWordSearchResultList
        _numWordSearchResults.value = initialNumWordSearchResults
        isLoadingOnScrolled = initialIsLoadingOnScrolled
    }
}
