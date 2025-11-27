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
import java.time.LocalDate
import javax.inject.Inject

/**
 * ワード検索画面のUIロジックと状態([WordSearchUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - 入力されたキーワードに基づく日記の検索と結果リストの管理
 * - 検索結果の新規読み込み、追加読み込み、および更新
 * - ユーザー操作（アイテムクリック、スクロールなど）に応じたイベント処理
 * - 日記表示画面への遷移イベントの発行
 * - [SavedStateHandle]を利用して、プロセスの再生成後もUI状態を復元とリストの再読み込み
 */
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
    /** 検索結果リストの読み込み処理を管理するための[Job]の初期値。 */
    private val initialWordSearchResultListLoadJob: Job? = null
    /** 検索結果リストの読み込み処理を管理するための[Job]。多重実行を防ぐために使用する。 */
    private var wordSearchResultListLoadJob: Job? = initialWordSearchResultListLoadJob // キャンセル用

    /** プロセス復帰（Process Deathからの復元）直後であるかを示すフラグ。 */
    private var isRestoringFromProcessDeath: Boolean = false

    /** 画面が非表示から復帰した際に、リストをリフレッシュする必要があるかを示すフラグ。 */
    private var needsRefreshWordSearchResultList: Boolean = false // MEMO:画面遷移、回転時の更新フラグ

    /** スクロールによる追加読み込みが現在実行中であるかを示すフラグ。 */
    private var isLoadingOnScrolled: Boolean = false
    //endregion

    //region Initialization
    init {
        checkForRestoration()
        collectUiStates()
    }

    /** プロセス復帰からのリストアかどうかを確認し、フラグを更新する。 */
    private fun checkForRestoration() {
        updateIsRestoringFromProcessDeath(
            handle.contains(SAVED_STATE_UI_KEY)
        )
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiState()
        collectWordSearchState()
        collectSearchWord()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** 検索ワードの有無を監視し、アイドリング状態を更新する。 */
    private fun collectWordSearchState() {
        uiState.distinctUntilChanged { old, new ->
            old.searchWord == new.searchWord
        }.map { it.searchWord.isEmpty() }.onEach { isIdle ->
            updateIsWordSearchIdle(isIdle)
        }.launchIn(viewModelScope)
    }

    /** 検索ワードの変更を監視し、リストのクリアまたは再読み込みをトリガーする。 */
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
    /**
     * UIが準備完了した時に、`Fragment`から呼び出される事を想定。
     * 必要に応じて検索結果リストのリフレッシュを行う。
     */
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

    /**
     * UIが非表示になる時に、`Fragment`から呼び出される事を想定。
     * 次回表示時にリストをリフレッシュするためのフラグを立てる。
     */
    internal fun onUiGone() {
        updateNeedsRefreshWordSearchResultList(true)
    }

    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            requestNavigatePreviousScreen()
        }
    }

    /**
     * ナビゲーションアイコンがクリックされた時に呼び出される事を想定。
     * 前の画面へ戻るイベントを発行する。
     */
    fun onNavigationIconButtonClick() {
        launchWithUnexpectedErrorHandler {
            requestNavigatePreviousScreen()
        }
    }

    /**
     * 検索結果リストのアイテムがクリックされた時に呼び出される事を想定。
     * 日記表示画面へ遷移するイベントを発行する。
     * @param item クリックされたリストアイテム
     */
    internal fun onWordSearchResultListItemClick(item: DiaryListItemContainerUi.WordSearchResult) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            requestNavigateDiaryShowScreen(id, date)
        }
    }

    /**
     * 検索ワード入力欄のテキストが変更された時に呼び出される事を想定。
     * UI状態の検索ワードを更新する。
     * @param text 変更後のテキスト
     */
    fun onSearchWordTextChanged(text: CharSequence) {
        updateSearchWord(text.toString())
    }

    /**
     * 検索結果リストが末尾までスクロールされた時に呼び出される事を想定。
     * 検索結果リストの追加読み込みを開始する。
     */
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

    /**
     * 検索結果リストのRecyclerViewのアダプター更新が完了した時に呼び出される事を想定。
     * 追加読み込み中フラグを解除する。
     */
    internal fun onWordSearchResultListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }
    //endregion

    //region Business Logic
    /** 実行中の検索結果読み込み処理があればキャンセルする。 */
    private fun cancelPreviousLoadJob() {
        val job = wordSearchResultListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    /**
     * 新しい検索結果リストを読み込む。
     * @param currentResultList 現在の検索結果リスト
     * @param searchWord 検索ワード
     */
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

    /**
     * 追加の検索結果リストを読み込む。
     * @param currentResultList 現在の検索結果リスト
     * @param searchWord 検索ワード
     */
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

    /**
     * 現在の検索結果リストをリフレッシュする。
     * @param currentResultList 現在の検索結果リスト
     * @param searchWord 検索ワード
     */
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

    /**
     * 検索結果リストの読み込み処理を共通のロジックで実行する。
     * @param updateToLoadingUiState 読み込み開始時のUI状態更新処理
     * @param currentResultList 現在の検索結果リスト
     * @param searchWord 検索ワード
     * @param executeLoad 実際の読み込みを行うUseCaseの実行処理
     * @param emitAppMessageOnFailure 読み込み失敗時のメッセージ発行処理
     */
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

    /** 検索結果リストをクリアし、UIを初期状態に戻す。 */
    private fun clearWordSearchResultList() {
        cancelPreviousLoadJob()
        wordSearchResultListLoadJob = initialWordSearchResultListLoadJob
        updateUiState {
            WordSearchUiState()
        }
    }

    /**
     * 前の画面への遷移を要求する。
     * 画面遷移イベントを発行する。
     */
    private suspend fun requestNavigatePreviousScreen() {
        emitNavigatePreviousFragmentEvent()
    }

    /**
     * 日記表示画面への遷移を要求する。
     * 画面遷移イベントを発行する。
     *
     * @param id 表示対象の日記のID。
     * @param date 表示対象の日記の日付。
     */
    private suspend fun requestNavigateDiaryShowScreen(id: String, date: LocalDate) {
        emitUiEvent(
            WordSearchUiEvent.NavigateDiaryShowScreen(id, date)
        )
    }
    //endregion

    //region UI State Update
    /**
     * 検索ワードを更新する。
     * @param searchWord 新しい検索ワード
     */
    private fun updateSearchWord(searchWord: String) {
        updateUiState { it.copy(searchWord = searchWord) }
    }

    /**
     * 検索がアイドル状態かどうかを更新する。
     * @param isIdle アイドル状態の場合はtrue
     */
    private fun updateIsWordSearchIdle(isIdle: Boolean) {
        updateUiState { it.copy(isWordSearchIdle = isIdle) }
    }

    /** UIをアイドル状態（操作可能）に更新する。 */
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }

    /** UIを新規リスト読み込み中の状態に更新する。 */
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

    /** UIを追加リスト読み込み中の状態に更新する。 */
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

    /** UIをリスト更新中の状態に更新する。 */
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

    /**
     * UIをリスト読み込み完了の状態に更新する。
     * @param numWordSearchResults 検索結果の総数
     * @param list 更新後の検索結果リスト
     */
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
    /**
     * プロセス復帰からのリストア中かどうかのフラグを更新する。
     * @param isRestoring リストア中の場合はtrue
     */
    private fun updateIsRestoringFromProcessDeath(isRestoring: Boolean) {
        isRestoringFromProcessDeath = isRestoring
    }

    /**
     * リストをリフレッシュする必要があるかどうかのフラグを更新する。
     * @param needsRefresh リフレッシュが必要な場合はtrue
     */
    private fun updateNeedsRefreshWordSearchResultList(needsRefresh: Boolean) {
        needsRefreshWordSearchResultList = needsRefresh
    }

    /**
     * スクロールによる追加読み込み中かどうかの状態を更新する。
     * @param isLoading 読み込み中の場合はtrue
     */
    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        isLoadingOnScrolled = isLoading
    }
    //endregion

    private companion object {
        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
