package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import com.websarva.wings.android.zuboradiary.ui.model.message.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi
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
class WordSearchViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase,
    private val loadNewWordSearchResultListUseCase: LoadNewWordSearchResultListUseCase,
    private val loadAdditionWordSearchResultListUseCase: LoadAdditionWordSearchResultListUseCase,
    private val refreshWordSearchResultListUseCase: RefreshWordSearchResultListUseCase
) : BaseFragmentViewModel<WordSearchUiState, WordSearchUiEvent, WordSearchAppMessage>(
    handle.get<WordSearchUiState>(SAVED_STATE_UI_KEY)?.let {
        WordSearchUiState.fromSavedState(it)
    } ?: WordSearchUiState()
) {

    //region Properties
    private val initialWordSearchResultListLoadJob: Job? = null
    private var wordSearchResultListLoadJob: Job? = initialWordSearchResultListLoadJob // キャンセル用

    private var isRestoringFromProcessDeath: Boolean = false

    private var needsRefreshWordSearchResultList: Boolean = false // MEMO:画面遷移、回転時の更新フラグ

    private var isLoadingOnScrolled: Boolean = false
    //endregion

    //region Initialization
    init {
        checkForRestoration()
        collectUiStates()
    }

    private fun checkForRestoration() {
        updateIsRestoringFromProcessDeath(
            handle.contains(SAVED_STATE_UI_KEY)
        )
    }

    private fun collectUiStates() {
        collectUiState()
        collectWordSearchState()
        collectSearchWord()
    }

    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectWordSearchState() {
        uiState.distinctUntilChanged { old, new ->
            old.searchWord == new.searchWord
        }.map { it.searchWord.isEmpty() }.onEach { isIdle ->
            updateIsWordSearchIdle(isIdle)
        }.launchIn(viewModelScope)
    }

    private fun collectSearchWord() {
        viewModelScope.launch {
            uiState.distinctUntilChanged { old, new ->
                old.searchWord == new.searchWord
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
                        emitUiEvent(WordSearchUiEvent.ShowKeyboard)
                        clearWordSearchResultList()
                    } else {
                        loadNewWordSearchResultList(wordSearchResultList, searchWord)
                    }
                }
            }
        }
    }
    //endregion

    //region UI Event Handlers
    internal fun onUiReady() {
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

    internal fun onUiGone() {
        updateNeedsRefreshWordSearchResultList(true)
    }

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

    internal fun onWordSearchResultListItemClick(item: DiaryListItemContainerUi.WordSearchResult) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                WordSearchUiEvent.NavigateDiaryShowFragment(id, date)
            )
        }
    }

    // View状態処理
    fun onSearchWordTextChanged(text: CharSequence) {
        updateSearchWord(text.toString())
    }

    internal fun onWordSearchResultListEndScrolled() {
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

    internal fun onWordSearchResultListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }
    //endregion

    //region Business Logic
    private fun cancelPreviousLoadJob() {
        val job = wordSearchResultListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    private suspend fun loadNewWordSearchResultList(
        currentResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>,
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
        currentResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>,
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
        currentResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>,
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
        currentResultList: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>,
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
    //endregion

    //region UI State Update
    private fun updateSearchWord(searchWord: String) {
        updateUiState { it.copy(searchWord = searchWord) }
    }

    private fun updateIsWordSearchIdle(isIdle: Boolean) {
        updateUiState { it.copy(isWordSearchIdle = isIdle) }
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
            DiaryYearMonthList.initialLoadingWordSearchResult().toUiModel()
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
        list: DiaryListUi<DiaryListItemContainerUi.WordSearchResult>
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
    //endregion

    //region Internal State Update
    private fun updateIsRestoringFromProcessDeath(bool: Boolean) {
        isRestoringFromProcessDeath = bool
    }

    private fun updateNeedsRefreshWordSearchResultList(needsRefresh: Boolean) {
        needsRefreshWordSearchResultList = needsRefresh
    }

    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        isLoadingOnScrolled = isLoading
    }
    //endregion

    private companion object {
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
