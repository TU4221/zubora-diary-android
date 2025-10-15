package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultCountException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListRefreshException
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.WordSearchState
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class WordSearchViewModel @Inject internal constructor(
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase,
    private val loadNewWordSearchResultListUseCase: LoadNewWordSearchResultListUseCase,
    private val loadAdditionWordSearchResultListUseCase: LoadAdditionWordSearchResultListUseCase,
    private val refreshWordSearchResultListUseCase: RefreshWordSearchResultListUseCase
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

    private val initialWordSearchResultList = DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>()
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

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): WordSearchEvent {
        return WordSearchEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: WordSearchAppMessage): WordSearchEvent {
        return WordSearchEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): WordSearchAppMessage {
        return WordSearchAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onNavigationIconButtonClick() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onWordSearchResultListItemClick(item: DiaryDayListItemUi.WordSearchResult) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                WordSearchEvent.NavigateDiaryShowFragment(id, date)
            )
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
            launchWithUnexpectedErrorHandler {
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
            launchWithUnexpectedErrorHandler {
                updateWordSearchResultList(currentResultList, currentSearchWord)
            }
    }

    fun onUiGone() {
        updateShouldUpdateWordSearchResultList(true)
    }

    // StateFlow値変更時処理
    fun onSearchWordChanged(value: String) {
        launchWithUnexpectedErrorHandler {
            prepareKeyboard(value)
        }

        // HACK:画面再表示時(Pause -> Resume)にCollectorが起動してしまい
        //      同じキーワードで不必要に検索してしまう。防止策として下記条件追加。
        if (value == previousSearchWord) return

        val currentResultList = _wordSearchResultList.value
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            launchWithUnexpectedErrorHandler {
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
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String
    ) {
        executeLoadWordSearchResultList(
            WordSearchState.Searching,
            currentResultList,
            searchWord,
            { _, lambdaSearchWord ->
                showWordSearchResultListFirstItemProgressIndicator()
                loadNewWordSearchResultListUseCase(
                    SearchWord(lambdaSearchWord)
                )
            },
            { exception ->
                when (exception) {
                    is WordSearchResultListNewLoadException.LoadFailure -> {
                        emitAppMessageEvent(
                            WordSearchAppMessage.SearchResultListLoadFailure
                        )
                    }
                    is WordSearchResultListNewLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun loadAdditionWordSearchResultList(
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String
    ) {
        executeLoadWordSearchResultList(
            WordSearchState.AdditionLoading,
            currentResultList,
            searchWord,
            { lambdaCurrentList, lambdaSearchWord ->
                require(lambdaCurrentList.isNotEmpty)

                loadAdditionWordSearchResultListUseCase(
                    lambdaCurrentList.toDomainModel(),
                    SearchWord(lambdaSearchWord)
                )
            },
            { exception ->
                when (exception) {
                    is WordSearchResultListAdditionLoadException.LoadFailure -> {
                        emitAppMessageEvent(
                            WordSearchAppMessage.SearchResultListLoadFailure
                        )
                    }
                    is WordSearchResultListAdditionLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun updateWordSearchResultList(
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String
    ) {
        executeLoadWordSearchResultList(
            WordSearchState.Updating,
            currentResultList,
            searchWord,
            { lambdaCurrentList, lambdaSearchWord ->
                refreshWordSearchResultListUseCase(
                    lambdaCurrentList.toDomainModel(),
                    SearchWord(lambdaSearchWord)
                )
            },
            { exception ->
                when (exception) {
                    is WordSearchResultListRefreshException.RefreshFailure -> {
                        emitAppMessageEvent(
                            WordSearchAppMessage.SearchResultListLoadFailure
                        )
                    }
                    is WordSearchResultListRefreshException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun <E : UseCaseException> executeLoadWordSearchResultList(
        state: WordSearchState,
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String,
        processLoad: suspend (
            currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
            searchWord: String
        ) -> UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
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

        val searchWordNotEmpty = searchWord.ifEmpty { return }

        updateUiState(state)
        try {
            when (val result  = countWordSearchResultsUseCase(SearchWord(searchWordNotEmpty))) {
                is UseCaseResult.Success -> {
                    updateNumWordSearchResults(result.value)
                }
                is UseCaseResult.Failure -> {
                    when (result.exception) {
                        is WordSearchResultCountException.CountFailure -> {
                            emitAppMessageEvent(
                                WordSearchAppMessage.SearchResultListLoadFailure
                            )
                        }
                        is WordSearchResultCountException.Unknown -> {
                            emitUnexpectedAppMessage(result.exception)
                        }
                    }
                    return
                }
            }

            when (val result = processLoad(currentResultList, searchWordNotEmpty)) {
                is UseCaseResult.Success -> {
                    val updateResultList = result.value.toUiModel()
                    updateWordSearchResultList(updateResultList)
                    updateUiStateOnWorSearchResultListLoadCompleted(updateResultList)
                    Log.i(logTag, "${logMsg}_完了")
                }
                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}_失敗", result.exception)
                    updateWordSearchResultList(currentResultList)
                    updateUiStateOnWorSearchResultListLoadCompleted(currentResultList)
                    emitAppMessageOnFailure(result.exception)
                }
            }

        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateUiStateOnWorSearchResultListLoadCompleted(currentResultList)
            throw e // 再スローしてコルーチン処理を中断させる
        }
    }

    private fun updateUiStateOnWorSearchResultListLoadCompleted(
        list: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>
    ) {
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
            DiaryYearMonthListUi(
                listOf(
                    DiaryYearMonthListItemUi.ProgressIndicator()
                )
            )
        )
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

    private fun updateWordSearchResultList(list: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>) {
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
