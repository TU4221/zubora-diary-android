package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.text.ValidateInputTextUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.state.InputTextValidationState
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.common.InputTextValidation
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryItemTitleEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * 日記項目タイトル編集画面のUIロジックと状態([DiaryItemTitleEditUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - 編集対象の項目タイトルと項目番号を管理
 * - 入力されたタイトル文字列のバリデーションを実行する
 * - 過去に使用したタイトル履歴の読み込み、表示、および削除処理を行う
 * - ユーザー操作（履歴の選択、新規タイトルの決定など）に応じて編集を完了し、結果を遷移元に通知する
 * - [SavedStateHandle]を利用して、プロセスの再生成後もUI状態を復元する
 */
@HiltViewModel
class DiaryItemTitleEditViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val loadDiaryItemTitleSelectionHistoryListUseCase: LoadDiaryItemTitleSelectionHistoryListUseCase,
    private val deleteDiaryItemTitleSelectionHistoryUseCase: DeleteDiaryItemTitleSelectionHistoryUseCase,
    private val validateInputTextUseCase: ValidateInputTextUseCase
) : BaseFragmentViewModel<DiaryItemTitleEditUiState, DiaryItemTitleEditUiEvent, DiaryItemTitleEditAppMessage>(
    handle.get<DiaryItemTitleEditUiState>(SAVED_STATE_UI_KEY)?.let {
        DiaryItemTitleEditUiState.fromSavedState(it)
    } ?: DiaryItemTitleEditUiState()
) {

    //region Properties
    /** 履歴アイテム削除処理が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingTitleSelectionHistoryDeleteParameters: TitleSelectionHistoryDeleteParameters? = null
    //endregion

    //region Initialization
    init {
        setupTitle()
        collectUiStates()
    }

    /** [SavedStateHandle]に保存されたデータがない場合のみ、引数からタイトルと項目番号をセットアップする。 */
    private fun setupTitle() {
        if (handle.contains(SAVED_STATE_UI_KEY)) return

        val diaryItemTitleSelection =
            handle.get<DiaryItemTitleSelectionUi>(ARGUMENT_DIARY_ITEM_TITLE_KEY)
                ?: throw IllegalArgumentException()
        updateItemNumber(diaryItemTitleSelection.itemNumber)
        updateTitle(diaryItemTitleSelection.title)
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiState()
        collectTitleValidation()
        collectTitleSelectionHistoryList()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** 入力されたタイトルのバリデーション結果を監視し、UIに反映させる。 */
    private fun collectTitleValidation() {
        uiState.distinctUntilChanged { old, new ->
            old.title == new.title
        }.mapNotNull { 
            when (val state = validateInputTextUseCase(it.title).value) {
                InputTextValidation.Valid,
                InputTextValidation.InitialCharUnmatched -> state.toUiModel()

                // 空の時は選択ボタン押下時にエラーを表示するようにする。
                InputTextValidation.Empty -> null
            }
        }.catchUnexpectedError(
            InputTextValidationState.Invalid
        ).distinctUntilChanged().onEach { validationResult ->
            updateTitleValidationState(validationResult)
        }.launchIn(viewModelScope)
    }

    /** 日記項目タイトル選択履歴リストの読み込みと状態更新を監視する。 */
    private fun collectTitleSelectionHistoryList() {
        loadDiaryItemTitleSelectionHistoryListUseCase().onStart {
            updateToTitleSelectionHistoryListLoadingState()
        }.onEach { result ->
            if (result is UseCaseResult.Failure) {
                when (result.exception) {
                    is DiaryItemTitleSelectionHistoryLoadException.LoadFailure -> {
                        emitAppMessageEvent(
                            DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadFailure
                        )
                    }
                    is DiaryItemTitleSelectionHistoryLoadException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }.map { 
            when (it) {
                is UseCaseResult.Success -> {
                    if (it.value.isEmpty) {
                        LoadState.Empty
                    } else {
                        LoadState.Success(it.value.toUiModel())
                    }
                }
                is UseCaseResult.Failure -> LoadState.Error
            }
        }.catchUnexpectedError(
            LoadState.Error
        ).distinctUntilChanged().onEach {
            updateToTitleSelectionHistoryListLoadCompletedState(it)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region UI Event Handlers
    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen()
        }
    }

    /**
     * ナビゲーションアイコンがクリックされた時に呼び出される事を想定。
     * 前の画面へ遷移する。
     */
    fun onNavigationIconClick() {
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen()
        }
    }

    /**
     * 新規タイトル選択ボタンがクリックされた時に呼び出される事を想定。
     * 編集を完了させる処理を開始する。
     */
    fun onNewDiaryItemTitleSelectionButtonClick() {
        val itemNumber = currentUiState.itemNumber
        val itemTitle = currentUiState.title
        launchWithUnexpectedErrorHandler {
            startTitleSelectionProcess(itemNumber, itemTitle = itemTitle)
        }
    }

    /**
     * 履歴リストのアイテムがクリックされた時に呼び出される事を想定。
     * 編集を完了させる処理を開始する。
     * @param item クリックされた履歴リストアイテム
     */
    internal fun onDiaryItemTitleSelectionHistoryListItemClick(
        item: DiaryItemTitleSelectionHistoryListItemUi
    ) {
        val itemNumber = currentUiState.itemNumber
        val itemId = item.id
        val itemTitle = item.title
        launchWithUnexpectedErrorHandler {
            startTitleSelectionProcess(
                itemNumber,
                DiaryItemTitleSelectionHistoryId(itemId),
                itemTitle
            )
        }
    }

    /**
     * 履歴リストのアイテムがスワイプされた時に呼び出される事を想定。
     * 削除確認ダイアログを表示する。
     * @param item スワイプされた履歴リストアイテム
     */
    internal fun onDiaryItemTitleSelectionHistoryListItemSwipe(
        item: DiaryItemTitleSelectionHistoryListItemUi
    ) {
        val itemId = item.id
        val itemTitle = item.title
        launchWithUnexpectedErrorHandler {
            showTitleSelectionHistoryDeleteDialog(itemId, itemTitle)
        }
    }

    /**
     * 項目タイトル入力欄のテキストが変更された時に呼び出される事を想定。
     * UI状態のタイトルを更新する。
     * @param text 変更後のテキスト
     */
    fun onItemTitleTextChanged(text: CharSequence) {
        val textString = text.toString()
        updateTitle(textString)
    }

    /**
     * 履歴アイテム削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 選択された日記項目タイトル選択履歴を削除する。
     */
    internal fun onDiaryItemTitleSelectionHistoryDeleteDialogPositiveResultReceived() {
        val parameters =checkNotNull(pendingTitleSelectionHistoryDeleteParameters)
        clearPendingTitleSelectionHistoryDeleteParameters()
        launchWithUnexpectedErrorHandler {
            deleteTitleSelectionHistory(parameters.itemId, parameters.itemTitle)
        }
    }

    /**
     * 履歴アイテム削除確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 選択された日記項目タイトル選択履歴のスワイプ状態を復元する。
     */
    internal fun onDiaryItemTitleSelectionHistoryDeleteDialogNegativeResultReceived() {
        clearPendingTitleSelectionHistoryDeleteParameters()
        launchWithUnexpectedErrorHandler {
            closeSwipedTitleSelectionHistory()
        }
    }
    //endregion

    //region Business Logic
    /**
     * ユーザーによるタイトル選択プロセスを開始する。
     * 入力されたタイトルを検証し、有効であれば編集を完了する（イベント発行）。
     * 無効な場合は、エラー状態をUIに反映させる。
     * @param itemNumberInt 項目番号
     * @param itemId 履歴ID（履歴から選択した場合）
     * @param itemTitle 項目タイトル
     */
    private suspend fun startTitleSelectionProcess(
        itemNumberInt: Int,
        itemId: DiaryItemTitleSelectionHistoryId = DiaryItemTitleSelectionHistoryId.generate(),
        itemTitle: String
    ) {
        when (val state = validateInputTextUseCase(itemTitle).value) {
            InputTextValidation.Valid -> {
                val diaryItemTitleSelection =
                    DiaryItemTitleSelectionUi(itemNumberInt, itemId.value, itemTitle)
                emitUiEvent(
                    DiaryItemTitleEditUiEvent.CompleteEdit(diaryItemTitleSelection)
                )
            }
            InputTextValidation.Empty,
            InputTextValidation.InitialCharUnmatched -> {
                updateTitleValidationState(state.toUiModel())
            }
        }
    }

    /**
     * 履歴の削除確認ダイアログを表示する。
     * 渡されたパラメータをキャッシュし、ダイアログを表示する（イベント発行）。
     *
     * @param historyId 削除対象の履歴のID。
     * @param historyTitle 削除対象の履歴のタイトル。
     */
    private suspend fun showTitleSelectionHistoryDeleteDialog(historyId: String, historyTitle: String) {
        cachePendingTitleSelectionHistoryDeleteParameters(
            DiaryItemTitleSelectionHistoryId(historyId),
            DiaryItemTitle(historyTitle)
        )
        emitUiEvent(
            DiaryItemTitleEditUiEvent
                .ShowSelectionHistoryDeleteDialog(
                    historyTitle
                )
        )
    }

    /**
     * 指定されたIDの日記項目タイトル選択履歴を削除する。
     * @param id 削除対象の履歴ID
     * @param title 削除対象のタイトル（ログおよびエラーメッセージ用）
     */
    private suspend fun deleteTitleSelectionHistory(
        id: DiaryItemTitleSelectionHistoryId,
        title: DiaryItemTitle
    ) {
        val logMsg = "日記項目タイトル選択履歴アイテム削除"
        Log.i(logTag, "${logMsg}_開始")

        when (val result = deleteDiaryItemTitleSelectionHistoryUseCase(id, title)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗")
                when (result.exception) {
                    is DiaryItemTitleSelectionHistoryDeleteException.DeleteFailure -> {
                        emitAppMessageEvent(
                            DiaryItemTitleEditAppMessage.ItemTitleHistoryDeleteFailure
                        )
                    }
                    is DiaryItemTitleSelectionHistoryDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    // TODO;Event名をメソッド名と統一
    /** スワイプされた日記項目タイトル選択履歴を閉じる（イベント発行）。 */
    private suspend fun closeSwipedTitleSelectionHistory() {
        emitUiEvent(
            DiaryItemTitleEditUiEvent.CloseSwipedSelectionHistory
        )
    }

    /** 前の画面へ遷移する（イベント発行）。 */
    private suspend fun navigatePreviousScreen() {
        emitNavigatePreviousFragmentEvent()
    }
    //endregion

    //region UI State Update
    /**
     * 項目番号を更新する。
     * @param itemNumber 新しい項目番号
     */
    private fun updateItemNumber(itemNumber: Int) {
        updateUiState { it.copy(itemNumber = itemNumber) }
    }

    /**
     * 項目タイトルを更新する。
     * @param itemTitle 新しい項目タイトル
     */
    private fun updateTitle(itemTitle: String) {
        updateUiState { it.copy(title = itemTitle) }
    }

    /**
     * タイトルのバリデーション結果を更新する。
     * @param result 新しいバリデーション結果
     */
    private fun updateTitleValidationState(result: InputTextValidationState) {
        updateUiState { it.copy(titleValidationState = result) }
    }

    /** UIをタイトル履歴リスト読み込み中の状態に更新する。 */
    private fun updateToTitleSelectionHistoryListLoadingState() {
        updateUiState {
            it.copy(
                titleSelectionHistoriesLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /**
     * UIをタイトル履歴リスト読み込み完了の状態に更新する。
     * @param loadState 読み込み結果の状態
     */
    private fun updateToTitleSelectionHistoryListLoadCompletedState(
        loadState: LoadState<DiaryItemTitleSelectionHistoryListUi>
    ) {
        updateUiState {
            it.copy(
                titleSelectionHistoriesLoadState = loadState,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
    //endregion

    //region Pending History Item Delete Parameters
    /**
     * 保留中の日記項目選択履歴削除パラメータを更新する。
     * @param itemId 削除対象の履歴ID
     * @param itemTitle 削除対象の項目タイトル
     */
    private fun cachePendingTitleSelectionHistoryDeleteParameters(
        itemId: DiaryItemTitleSelectionHistoryId,
        itemTitle: DiaryItemTitle
    ) {
        pendingTitleSelectionHistoryDeleteParameters = TitleSelectionHistoryDeleteParameters(itemId, itemTitle)
    }

    /** 保留中の日記項目選択履歴削除パラメータをクリアする。 */
    private fun clearPendingTitleSelectionHistoryDeleteParameters() {
        pendingTitleSelectionHistoryDeleteParameters = null
    }

    /**
     * 日記項目選択履歴削除処理に必要なパラメータを保持するデータクラス。
     * @property itemId 削除対象の履歴ID
     * @property itemTitle 削除対象の項目タイトル
     */
    private data class TitleSelectionHistoryDeleteParameters(
        val itemId: DiaryItemTitleSelectionHistoryId,
        val itemTitle: DiaryItemTitle
    )
    //endregion

    private companion object {
        /** ナビゲーションコンポーネントで受け渡される日記項目タイトル情報のキー。 */
        const val ARGUMENT_DIARY_ITEM_TITLE_KEY = "diary_item_title"

        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
