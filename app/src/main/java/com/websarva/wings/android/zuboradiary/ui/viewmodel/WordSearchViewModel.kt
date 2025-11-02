package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultCountException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListRefreshException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.WordSearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WordSearchViewModel @Inject internal constructor(
    handle: SavedStateHandle,
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase,
    private val loadNewWordSearchResultListUseCase: LoadNewWordSearchResultListUseCase,
    private val loadAdditionWordSearchResultListUseCase: LoadAdditionWordSearchResultListUseCase,
    private val refreshWordSearchResultListUseCase: RefreshWordSearchResultListUseCase
) : BaseViewModel<WordSearchEvent, WordSearchAppMessage, WordSearchUiState>(
    handle.get<WordSearchUiState>(SAVED_UI_STATE_KEY)?.let { savedUiState ->
        WordSearchUiState().copy(
            searchWord = savedUiState.searchWord,
            numWordSearchResults = savedUiState.numWordSearchResults,
            wordSearchResultList = savedUiState.wordSearchResultList,
        )
    } ?: WordSearchUiState()
) {

    companion object {
        private const val SAVED_UI_STATE_KEY = "uiState"
    }

    private val initialWordSearchResultListLoadJob: Job? = null
    private var wordSearchResultListLoadJob: Job? = initialWordSearchResultListLoadJob // キャンセル用

    private var isRestoringFromProcessDeath: Boolean = false

    private var needsRefreshWordSearchResultList: Boolean = false // MEMO:画面遷移、回転時の更新フラグ

    private var isLoadingOnScrolled: Boolean = false

    init {
        checkForRestoration(handle)
        observeDerivedUiStateChanges(handle)
        observeUiStateChanges()
    }

    private fun checkForRestoration(handle: SavedStateHandle) {
        updateIsRestoringFromProcessDeath(
            handle.contains(SAVED_UI_STATE_KEY)
        )
    }

    private fun observeDerivedUiStateChanges(handle: SavedStateHandle) {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        uiState.map { it.searchWord }.distinctUntilChanged().onEach { searchWord ->
            updateUiState {
                it.copy(
                    isIdle = searchWord.isEmpty()
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeUiStateChanges() {
        viewModelScope.launch {
            uiState.distinctUntilChanged { oldState, newState ->
                oldState.searchWord == newState.searchWord
            }.map {
                Pair(it.wordSearchResultList, it.searchWord)
            }.collectLatest { (wordSearchResultList, searchWord) ->
                withUnexpectedErrorHandler {
                    if (isRestoringFromProcessDeath) {
                        updateIsRestoringFromProcessDeath(false)
                        refreshWordSearchResultList(wordSearchResultList, searchWord)
                        return@withUnexpectedErrorHandler
                    }

                    if (searchWord.isEmpty()) {
                        emitUiEvent(WordSearchEvent.ShowKeyboard)
                        clearWordSearchResultList()
                    } else {
                        loadNewWordSearchResultList(wordSearchResultList, searchWord)
                    }
                }
            }
        }
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
    fun onSearchWordTextChanged(text: CharSequence) {
        updateSearchWord(text.toString())
    }

    fun onWordSearchResultListEndScrolled() {
        if (isLoadingOnScrolled) return
        updateIsLoadingOnScrolled(true)

        val currentResultList = currentUiState.wordSearchResultList
        val searchWord = currentUiState.searchWord
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            launchWithUnexpectedErrorHandler {
                loadAdditionWordSearchResultList(
                    currentResultList,
                    searchWord
                )
            }
    }

    fun onWordSearchResultListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }

    // Ui状態処理
    fun onUiReady() {
        if (!needsRefreshWordSearchResultList) return
        updateNeedsRefreshWordSearchResultList(false)
        if (!isReadyForOperation) return

        val currentResultList = currentUiState.wordSearchResultList
        val currentSearchWord = currentUiState.searchWord
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob =
            launchWithUnexpectedErrorHandler {
                refreshWordSearchResultList(
                    currentResultList,
                    currentSearchWord
                )
            }
    }

    fun onUiGone() {
        updateNeedsRefreshWordSearchResultList(true)
    }

    // データ処理
    private fun cancelPreviousLoadJob() {
        val job = wordSearchResultListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewWordSearchResultList(
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String
    ) {
        executeLoadWordSearchResultList(
            { updateToWordSearchResultListNewLoadState() },
            currentResultList,
            searchWord,
            { _, searchWord ->
                loadNewWordSearchResultListUseCase(searchWord)
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
            { updateToWordSearchResultListAdditionLoadState() },
            currentResultList,
            searchWord,
            { currentList, searchWord ->
                loadAdditionWordSearchResultListUseCase(currentList, searchWord)
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

    private suspend fun refreshWordSearchResultList(
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String
    ) {
        executeLoadWordSearchResultList(
            { updateToWordSearchResultListRefreshState() },
            currentResultList,
            searchWord,
            { currentList, searchWord ->
                refreshWordSearchResultListUseCase(currentList, searchWord)
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
        updateToLoadingUiState: suspend () -> Unit,
        currentResultList: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>,
        searchWord: String,
        executeLoad: suspend (
            currentResultList: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
            searchWord: SearchWord
        ) -> UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {
        val searchWord = SearchWord(searchWord)

        val logMsg = "ワード検索結果読込"
        Log.i(logTag, "${logMsg}_開始")


        updateToLoadingUiState()
        try {
            val updateNumWordSearchResults: Int
            when (val result  = countWordSearchResultsUseCase(searchWord)) {
                is UseCaseResult.Success -> {
                    updateNumWordSearchResults = result.value
                }
                is UseCaseResult.Failure -> {
                    updateToIdleState()
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

            when (val result = executeLoad(currentResultList.toDomainModel(), searchWord)) {
                is UseCaseResult.Success -> {
                    val updateResultList = result.value.toUiModel()
                    updateToWordSearchResultListLoadCompletedState(
                        updateNumWordSearchResults,
                        updateResultList
                    )
                    Log.i(logTag, "${logMsg}_完了")
                }
                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}_失敗", result.exception)
                    updateToIdleState()
                    emitAppMessageOnFailure(result.exception)
                }
            }

        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateToIdleState()
            throw e // 再スローしてコルーチン処理を中断させる
        }
    }

    private fun clearWordSearchResultList() {
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob = initialWordSearchResultListLoadJob
        updateUiState {
            WordSearchUiState()
        }
    }

    private fun updateIsRestoringFromProcessDeath(bool: Boolean) {
        isRestoringFromProcessDeath = bool
    }

    private fun updateNeedsRefreshWordSearchResultList(needsRefresh: Boolean) {
        needsRefreshWordSearchResultList = needsRefresh
    }

    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        isLoadingOnScrolled = isLoading
    }

    private fun updateSearchWord(searchWord: String) {
        updateUiState {
            it.copy(
                searchWord = searchWord
            )
        }
    }

    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }

    private fun updateToWordSearchResultListNewLoadState() {
        val list =
            DiaryYearMonthList<DiaryDayListItem.WordSearchResult>(
                listOf(
                    DiaryYearMonthListItem.ProgressIndicator()
                )
            ).toUiModel()
        updateUiState {
            it.copy(
                wordSearchResultList = list,

                hasWordSearchCompleted = false,
                hasNoWordSearchResults = false,

                isProcessing = false,
                isInputDisabled = true,
                isRefreshing = false
            )
        }
    }

    private fun updateToWordSearchResultListAdditionLoadState() {
        updateUiState {
            it.copy(
                hasWordSearchCompleted = true,
                hasNoWordSearchResults = false,
                isProcessing = false,
                isInputDisabled = true,
                isRefreshing = false
            )
        }
    }

    private fun updateToWordSearchResultListRefreshState() {
        updateUiState {
            it.copy(
                hasWordSearchCompleted = true,
                isProcessing = true,
                isInputDisabled = true,
                isRefreshing = true
            )
        }
    }

    private fun updateToWordSearchResultListLoadCompletedState(
        numWordSearchResults: Int,
        list: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>
    ) {
        updateUiState {
            it.copy(
                numWordSearchResults = numWordSearchResults,
                wordSearchResultList = list,

                hasWordSearchCompleted = true,
                hasNoWordSearchResults = list.isEmpty,

                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
}
