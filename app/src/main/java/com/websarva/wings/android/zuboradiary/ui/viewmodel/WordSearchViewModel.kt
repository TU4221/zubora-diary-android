package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedWordSearchResultDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadWordSearchResultDiaryListUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultDayList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultDayListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.WordSearchState
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class WordSearchViewModel @Inject internal constructor(
    private val loadWordSearchResultDiaryListUseCase: LoadWordSearchResultDiaryListUseCase,
    private val countWordSearchResultDiariesUseCase: CountWordSearchResultDiariesUseCase,
    private val checkUnloadedWordSearchResultDiariesExistUseCase: CheckUnloadedWordSearchResultDiariesExistUseCase
) : BaseViewModel<WordSearchEvent, WordSearchAppMessage, WordSearchState>(
    WordSearchState.Idle
) {

    private val logTag = createLogTag()

    override val isProgressIndicatorVisible =
        uiState
            .map { state ->
                when (state) {
                    WordSearchState.Updating -> true

                    WordSearchState.Searching,
                    WordSearchState.AdditionLoading -> false

                    WordSearchState.Idle,
                    WordSearchState.ShowingResultList,
                    WordSearchState.NoResults -> false
                }
            }.stateInWhileSubscribed(
                false
            )

    private val _searchWord = MutableStateFlow("")
    val searchWord
        get() = _searchWord.asStateFlow()
    /**
     * LayoutDataBinding用
     * */
    val searchWordMutableStateFlow: MutableStateFlow<String>
        get() = _searchWord

    private var previousSearchWord = "" // 二重検索防止用

    private val initialWordSearchResultListLoadJob: Job? = null
    private var wordSearchResultListLoadJob: Job? = initialWordSearchResultListLoadJob // キャンセル用

    private val numLoadItems = DiaryListViewModel.NUM_LOAD_ITEMS
    private val initialWordSearchResultList = WordSearchResultYearMonthList()
    private val _wordSearchResultList = MutableStateFlow(initialWordSearchResultList)
    val wordSearchResultList
        get() = _wordSearchResultList.asStateFlow()

    private val initialNumWordSearchResults = 0
    private val _numWordSearchResults = MutableStateFlow(initialNumWordSearchResults)
    val numWordSearchResults
        get() = _numWordSearchResults.asStateFlow()

    // MEMO:画面遷移、回転時の更新フラグ
    private var shouldUpdateWordSearchResultList = false

    private val initialIsLoadingOnScrolled = false
    private var isLoadingOnScrolled = initialIsLoadingOnScrolled

    val isResultsVisible =
        uiState.map { value ->
            when (value) {
                WordSearchState.Searching,
                WordSearchState.AdditionLoading,
                WordSearchState.Updating,
                WordSearchState.ShowingResultList -> true

                WordSearchState.Idle,
                WordSearchState.NoResults -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    val isNumResultsVisible =
        uiState.map { value ->
            when (value) {
                WordSearchState.AdditionLoading,
                WordSearchState.Updating,
                WordSearchState.ShowingResultList -> true

                WordSearchState.Idle,
                WordSearchState.Searching,
                WordSearchState.NoResults -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    val isNoResultsMessageVisible =
        uiState.map { value ->
            when (value) {
                WordSearchState.NoResults -> true

                WordSearchState.Idle,
                WordSearchState.Searching,
                WordSearchState.AdditionLoading,
                WordSearchState.Updating,
                WordSearchState.ShowingResultList -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                WordSearchEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: WordSearchAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                WordSearchEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onNavigationIconButtonClick() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onWordSearchResultListItemClick(date: LocalDate) {
        viewModelScope.launch {
            emitUiEvent(WordSearchEvent.NavigateDiaryShowFragment(date))
        }
    }

    // View状態処理
    fun onWordSearchResultListEndScrolled() {
        if (isLoadingOnScrolled) return
        updateIsLoadingOnScrolled(true)

        val currentResultList = _wordSearchResultList.value
        val searchWord = _searchWord.value
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            viewModelScope.launch {
                loadAdditionWordSearchResultList(currentResultList, searchWord)
            }
    }

    fun onWordSearchResultListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }

    // Ui状態処理
    fun onUiReady() {
        if (!shouldUpdateWordSearchResultList) return
        updateShouldUpdateWordSearchResultList(false)
        if (uiState.value != WordSearchState.ShowingResultList) return

        val currentResultList = _wordSearchResultList.value
        val currentSearchWord = _searchWord.value

        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            viewModelScope.launch {
                updateWordSearchResultList(currentResultList, currentSearchWord)
            }
    }

    fun onUiGone() {
        updateShouldUpdateWordSearchResultList(true)
    }

    // StateFlow値変更時処理
    fun onSearchWordChanged(value: String) {
        viewModelScope.launch {
            prepareKeyboard(value)
        }

        // HACK:画面再表示時(Pause -> Resume)にCollectorが起動してしまい
        //      同じキーワードで不必要に検索してしまう。防止策として下記条件追加。
        if (value == previousSearchWord) return

        val currentResultList = _wordSearchResultList.value
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            viewModelScope.launch {
                if (value.isEmpty()) {
                    clearWordSearchResultList()
                } else {
                    loadNewWordSearchResultList(currentResultList, value)
                }

                updatePreviousSearchWord(value)
            }
    }

    // データ処理
    private suspend fun prepareKeyboard(searchWord: String) {
        if (searchWord.isEmpty()) emitUiEvent(WordSearchEvent.ShowKeyboard)
    }

    private fun cancelPreviousLoadJob() {
        val job = wordSearchResultListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewWordSearchResultList(
        currentResultList: WordSearchResultYearMonthList,
        searchWord: String
    ) {
        loadWordSearchResultList(
            WordSearchState.Searching,
            currentResultList,
            searchWord
        ) { _, lambdaWordSearch ->
            showWordSearchResultListFirstItemProgressIndicator()
            val value =
                loadWordSearchResultDiaryList(numLoadItems, 0, lambdaWordSearch)
            toUiWordSearchResultList(value, lambdaWordSearch)
        }
    }

    private suspend fun loadAdditionWordSearchResultList(
        currentResultList: WordSearchResultYearMonthList,
        searchWord: String
    ) {
        loadWordSearchResultList(
            WordSearchState.AdditionLoading,
            currentResultList,
            searchWord
        ) { lambdaCurrentList, lambdaWordSearch ->
            require(lambdaCurrentList.isNotEmpty)

            val loadOffset = lambdaCurrentList.countDiaries()
            val value =
                loadWordSearchResultDiaryList(numLoadItems, loadOffset, lambdaWordSearch)
            val loadedResultList = toUiWordSearchResultList(value, lambdaWordSearch)
            val numLoadedDiaries =
                lambdaCurrentList.countDiaries() + loadedResultList.countDiaries()
            val existsUnloadedDiaries =
                existsUnloadedDiaries(lambdaWordSearch, numLoadedDiaries)
            lambdaCurrentList.combineDiaryLists(loadedResultList, !existsUnloadedDiaries)
        }
    }

    private suspend fun updateWordSearchResultList(
        currentResultList: WordSearchResultYearMonthList,
        searchWord: String
    ) {
        loadWordSearchResultList(
            WordSearchState.Updating,
            currentResultList,
            searchWord
        ) { lambdaCurrentList, lambdaWordSearch ->

            var numLoadItems = lambdaCurrentList.countDiaries()
            // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
            //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
            //      対策として下記コードを記述。
            if (numLoadItems < this@WordSearchViewModel.numLoadItems) {
                numLoadItems = this@WordSearchViewModel.numLoadItems
            }
            val value =
                loadWordSearchResultDiaryList(
                    numLoadItems,
                    0,
                    lambdaWordSearch
                )
            toUiWordSearchResultList(value, lambdaWordSearch)
        }
    }

    private suspend fun loadWordSearchResultList(
        state: WordSearchState,
        currentResultList: WordSearchResultYearMonthList,
        searchWord: String,
        processLoad: suspend (
            currentResultList: WordSearchResultYearMonthList,
            searchWord: String
        ) -> WordSearchResultYearMonthList
    ) {
        require(
            when (state) {
                WordSearchState.Searching,
                WordSearchState.AdditionLoading,
                WordSearchState.Updating -> true

                WordSearchState.Idle,
                WordSearchState.NoResults,
                WordSearchState.ShowingResultList -> false
            }
        )

        val logMsg = "ワード検索結果読込($state)"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(state)
        try {
            updateNumWordSearchResults(
                countWordSearchResultDiaries(searchWord)
            )
            val updateResultList = processLoad(currentResultList, searchWord)
            updateWordSearchResultList(
                processLoad(currentResultList, searchWord)
            )
            updateUiStateForResultList(updateResultList)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateUiStateForResultList(currentResultList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            updateWordSearchResultList(currentResultList)
            updateUiStateForResultList(currentResultList)
            emitAppMessageEvent(WordSearchAppMessage.SearchResultListLoadFailure)
        }
    }

    private fun updateUiStateForResultList(list: WordSearchResultYearMonthList) {
        val state =
            if (list.isNotEmpty) {
                WordSearchState.ShowingResultList
            } else {
                WordSearchState.NoResults
            }
        updateUiState(state)
    }

    private fun showWordSearchResultListFirstItemProgressIndicator() {
        updateWordSearchResultList(
            WordSearchResultYearMonthList(false)
        )
    }

    @Throws(DomainException::class)
    private suspend fun loadWordSearchResultDiaryList(
        numLoadItems: Int,
        loadOffset: Int,
        searchWord: String
    ): List<WordSearchResultListItem> {
        require(numLoadItems > 0)
        require(loadOffset >= 0)

        val result =
            loadWordSearchResultDiaryListUseCase(
                numLoadItems,
                loadOffset,
                searchWord
            )
        return when (result) {
            is UseCaseResult.Success -> result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    @Throws(DomainException::class)
    private suspend fun toUiWordSearchResultList(
        list: List<WordSearchResultListItem>,
        searchWord: String
    ): WordSearchResultYearMonthList {
        Log.d("20250714", list.toString())
        if (list.isEmpty()) return WordSearchResultYearMonthList()

        val resultDayListItemList: MutableList<WordSearchResultDayListItem> = ArrayList()
        list.stream().forEach { x: WordSearchResultListItem ->
            resultDayListItemList.add(
                WordSearchResultDayListItem(x, searchWord)
            )
        }
        val resultDayList = WordSearchResultDayList(resultDayListItemList)
        val existsUnloadedDiaries =
            existsUnloadedDiaries(
                searchWord,
                resultDayList.countDiaries()
            )
        return WordSearchResultYearMonthList(resultDayList, !existsUnloadedDiaries)
    }

    @Throws(DomainException::class)
    private suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        when (val result = countWordSearchResultDiariesUseCase(searchWord)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    @Throws(DomainException::class)
    private suspend fun existsUnloadedDiaries(searchWord: String, numLoadedDiaries: Int): Boolean {
        val result = checkUnloadedWordSearchResultDiariesExistUseCase(searchWord, numLoadedDiaries)
        when (result) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    private fun clearWordSearchResultList() {
        updateUiState(WordSearchState.Idle)
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob = initialWordSearchResultListLoadJob
        updateWordSearchResultList(initialWordSearchResultList)
        updateNumWordSearchResults(initialNumWordSearchResults)
        updateIsLoadingOnScrolled(initialIsLoadingOnScrolled)
    }

    private fun updatePreviousSearchWord(searchWord: String) {
        previousSearchWord = searchWord
    }

    private fun updateWordSearchResultList(list: WordSearchResultYearMonthList) {
        _wordSearchResultList.value = list
    }

    private fun updateNumWordSearchResults(count: Int) {
        _numWordSearchResults.value = count
    }

    private fun updateShouldUpdateWordSearchResultList(shouldUpdate: Boolean) {
        shouldUpdateWordSearchResultList = shouldUpdate
    }

    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        isLoadingOnScrolled = isLoading
    }
}
