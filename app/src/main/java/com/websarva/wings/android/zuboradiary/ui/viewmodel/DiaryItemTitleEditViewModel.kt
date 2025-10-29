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
import com.websarva.wings.android.zuboradiary.ui.model.result.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ErrorType
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

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val loadDiaryItemTitleSelectionHistoryListUseCase: LoadDiaryItemTitleSelectionHistoryListUseCase,
    private val deleteDiaryItemTitleSelectionHistoryUseCase: DeleteDiaryItemTitleSelectionHistoryUseCase,
    private val validateInputTextUseCase: ValidateInputTextUseCase
) : BaseViewModel<DiaryItemTitleEditEvent, DiaryItemTitleEditAppMessage, DiaryItemTitleEditUiState>(
    handle.get<DiaryItemTitleEditUiState>(SAVED_UI_STATE_KEY)?.let { savedUiState ->
        DiaryItemTitleEditUiState().copy(
            itemNumber = savedUiState.itemNumber,
            title = savedUiState.title,
        )
    } ?: DiaryItemTitleEditUiState()
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val DIARY_ITEM_TITLE_ARGUMENT_KEY = "diary_item_title"

        // ViewModel状態保存キー
        private const val SAVED_UI_STATE_KEY = "uiState"
    }

    override val isProgressIndicatorVisible =
        uiState
            .map {
                it.isProcessing
            }.stateInWhileSubscribed(
                false
            )

    private val currentUiState
        get() = uiState.value

    // キャッシュパラメータ
    private var pendingHistoryItemDeleteParameters: HistoryItemDeleteParameters? = null

    init {
        setUpTitle(handle)
        setUpTitleSelectionHistoryList()
        observeDerivedUiStateChanges(handle)
    }

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): DiaryItemTitleEditEvent {
        return DiaryItemTitleEditEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: DiaryItemTitleEditAppMessage): DiaryItemTitleEditEvent {
        return DiaryItemTitleEditEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): DiaryItemTitleEditAppMessage {
        return DiaryItemTitleEditAppMessage.Unexpected(e)
    }

    private fun setUpTitle(handle: SavedStateHandle) {
        if (handle.contains(SAVED_UI_STATE_KEY)) return

        val diaryItemTitleSelection =
            handle.get<DiaryItemTitleSelectionUi>(DIARY_ITEM_TITLE_ARGUMENT_KEY)
                ?: throw IllegalArgumentException()
        val itemNumberInt = diaryItemTitleSelection.itemNumber
        val itemTitle = diaryItemTitleSelection.title
        updateItemNumberAndTitle(itemNumberInt, itemTitle)
    }

    private fun setUpTitleSelectionHistoryList() {
        loadDiaryItemTitleSelectionHistoryListUseCase().onStart {
            updateToTitleSelectionHistoryListLoadingState()
        }.map {
            when (it) {
                is UseCaseResult.Success -> {
                    if (it.value.isEmpty) {
                        LoadState.Empty
                    } else {
                        LoadState.Success(it.value.toUiModel())
                    }
                }
                is UseCaseResult.Failure -> {
                    when (it.exception) {
                        is DiaryItemTitleSelectionHistoryLoadException.LoadFailure -> {
                            emitAppMessageEvent(
                                DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadFailure
                            )
                        }
                        is DiaryItemTitleSelectionHistoryLoadException.Unknown -> {
                            emitUnexpectedAppMessage(it.exception)
                        }
                    }
                    LoadState.Error(ErrorType.Unexpected(it.exception))
                }
            }
        }.catchUnexpectedError(
            LoadState.Idle // TODO:仮で設定(LoadState.ErrorのErrorTypeが設定できない為)
        ).distinctUntilChanged().onEach {
            updateToTitleSelectionHistoryListLoadCompletedState(it)
        }.launchIn(viewModelScope)
    }

    private fun observeDerivedUiStateChanges(handle: SavedStateHandle) {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        uiState.mapNotNull {
            val result = validateInputTextUseCase(it.title).value
            when (result) {
                InputTextValidationResult.Valid,
                InputTextValidationResult.Invalid,
                InputTextValidationResult.InvalidInitialCharUnmatched -> result

                // 空の時は選択ボタン押下時にエラーを表示するようにする。
                InputTextValidationResult.InvalidEmpty -> null
            }
        }.catchUnexpectedError(
            InputTextValidationResult.Invalid
        ).distinctUntilChanged().onEach { validationResult ->
            updateTitleInputTextValidationResult(validationResult)
        }.launchIn(viewModelScope)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onNavigationIconClick() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onNewDiaryItemTitleSelectionButtonClick() {
        val itemNumber = currentUiState.itemNumber
        val itemTitle = currentUiState.title
        launchWithUnexpectedErrorHandler {
            completeItemTitleEdit(itemNumber, itemTitle = itemTitle)
        }
    }

    fun onDiaryItemTitleSelectionHistoryListItemClick(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemNumber = currentUiState.itemNumber
        val itemId = item.id
        val itemTitle = item.title
        launchWithUnexpectedErrorHandler {
            completeItemTitleEdit(
                itemNumber,
                DiaryItemTitleSelectionHistoryId(itemId),
                itemTitle
            )
        }
    }

    fun onDiaryItemTitleSelectionHistoryListItemSwipe(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemId = item.id
        val itemTitle = item.title
        launchWithUnexpectedErrorHandler {
            updatePendingHistoryItemDeleteParameters(
                DiaryItemTitleSelectionHistoryId(itemId),
                DiaryItemTitle(itemTitle)
            )
            emitUiEvent(
                DiaryItemTitleEditEvent
                    .NavigateSelectionHistoryItemDeleteDialog(
                        itemTitle
                    )
            )
        }
    }

    // View状態処理
    fun onItemTitleTextChanged(text: CharSequence) {
        val textString = text.toString()
        updateTitle(textString)
    }

    fun onDiaryItemTitleSelectionHistoryDeleteDialogResultReceived(
        result: DialogResult<Unit>
    ) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryItemTitleSelectionHistoryDeleteDialogPositiveResult(
                    pendingHistoryItemDeleteParameters
                )
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                handleDiaryItemTitleSelectionHistoryDeleteDialogNegativeResult()
            }
        }
        clearPendingHistoryItemDeleteParameters()
    }

    private fun handleDiaryItemTitleSelectionHistoryDeleteDialogPositiveResult(
        parameters: HistoryItemDeleteParameters?
    ) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                deleteDiaryItemTitleSelectionHistory(it.itemId, it.itemTitle)
            }
        }
    }

    private fun handleDiaryItemTitleSelectionHistoryDeleteDialogNegativeResult() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryItemTitleEditEvent.CloseSwipedItem
            )
        }
    }

    // データ処理
    private suspend fun completeItemTitleEdit(
        itemNumberInt: Int,
        itemId: DiaryItemTitleSelectionHistoryId = DiaryItemTitleSelectionHistoryId.generate(),
        itemTitle: String
    ) {
        when (val result = validateInputTextUseCase(itemTitle).value) {
            InputTextValidationResult.Valid -> {
                val diaryItemTitleSelection =
                    DiaryItemTitleSelectionUi(itemNumberInt, itemId.value, itemTitle)
                emitUiEvent(
                    DiaryItemTitleEditEvent.CompleteEdit(diaryItemTitleSelection)
                )
            }
            InputTextValidationResult.Invalid,
            InputTextValidationResult.InvalidEmpty,
            InputTextValidationResult.InvalidInitialCharUnmatched -> {
                updateTitleInputTextValidationResult(result)
            }
        }
    }

    private suspend fun deleteDiaryItemTitleSelectionHistory(
        id: DiaryItemTitleSelectionHistoryId,
        title: DiaryItemTitle
    ) {
        val logMsg = "日記項目タイトル選択履歴アイテム削除"
        Log.i(logTag, "${logMsg}_開始")

        val result = deleteDiaryItemTitleSelectionHistoryUseCase(id, title)
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗")
                when (result.exception) {
                    is DiaryItemTitleSelectionHistoryDeleteException.DeleteFailure -> {
                        emitAppMessageEvent(DiaryItemTitleEditAppMessage.ItemTitleHistoryDeleteFailure)
                    }
                    is DiaryItemTitleSelectionHistoryDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private fun updateItemNumberAndTitle(itemNumberInt: Int, title: String) {
        updateUiState {
            it.copy(
                itemNumber = itemNumberInt,
                title = title
            )
        }
    }

    private fun updateTitle(itemTitle: String) {
        updateUiState {
            it.copy(
                title = itemTitle
            )
        }
    }

    private fun updateTitleInputTextValidationResult(result: InputTextValidationResult) {
        updateUiState {
            it.copy(
                titleValidationResult = result
            )
        }
    }

    private fun updateToTitleSelectionHistoryListLoadingState() {
        updateUiState {
            it.copy(
                titleSelectionHistoriesLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

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

    private fun updatePendingHistoryItemDeleteParameters(
        itemId: DiaryItemTitleSelectionHistoryId,
        itemTitle: DiaryItemTitle
    ) {
        pendingHistoryItemDeleteParameters = HistoryItemDeleteParameters(itemId, itemTitle)
    }

    private fun clearPendingHistoryItemDeleteParameters() {
        pendingHistoryItemDeleteParameters = null
    }

    private data class HistoryItemDeleteParameters(
        val itemId: DiaryItemTitleSelectionHistoryId,
        val itemTitle: DiaryItemTitle
    )
}
