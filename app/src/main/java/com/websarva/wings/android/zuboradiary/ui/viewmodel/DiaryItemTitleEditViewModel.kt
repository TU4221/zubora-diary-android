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
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
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
    // キャッシュパラメータ
    private var pendingHistoryItemDeleteParameters: HistoryItemDeleteParameters? = null
    //endregion

    //region Initialization
    init {
        setUpTitle()
        collectUiStates()
    }

    private fun setUpTitle() {
        if (handle.contains(SAVED_STATE_UI_KEY)) return

        val diaryItemTitleSelection =
            handle.get<DiaryItemTitleSelectionUi>(ARGUMENT_DIARY_ITEM_TITLE_KEY)
                ?: throw IllegalArgumentException()
        updateItemNumber(diaryItemTitleSelection.itemNumber)
        updateTitle(diaryItemTitleSelection.title)
    }

    private fun collectUiStates() {
        collectUiState()
        collectTitleValidation()
        collectTitleSelectionHistoryList()
    }

    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectTitleValidation() {
        uiState.distinctUntilChanged { old, new ->
            old.title == new.title
        }.mapNotNull { 
            val state = validateInputTextUseCase(it.title).value
            when (state) {
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

    private fun collectTitleSelectionHistoryList() {
        loadDiaryItemTitleSelectionHistoryListUseCase().onStart {
            updateToTitleSelectionHistoryListLoadingState()
        }.onEach {
            when (it) {
                is UseCaseResult.Success -> { /*処理なし*/ }
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
            emitNavigatePreviousFragmentEvent()
        }
    }

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

    internal fun onDiaryItemTitleSelectionHistoryListItemClick(item: DiaryItemTitleSelectionHistoryListItemUi) {
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

    internal fun onDiaryItemTitleSelectionHistoryListItemSwipe(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemId = item.id
        val itemTitle = item.title
        launchWithUnexpectedErrorHandler {
            updatePendingHistoryItemDeleteParameters(
                DiaryItemTitleSelectionHistoryId(itemId),
                DiaryItemTitle(itemTitle)
            )
            emitUiEvent(
                DiaryItemTitleEditUiEvent
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

    internal fun onDiaryItemTitleSelectionHistoryDeleteDialogResultReceived(
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
                DiaryItemTitleEditUiEvent.CloseSwipedItem
            )
        }
    }
    //endregion

    //region Business Logic
    private suspend fun completeItemTitleEdit(
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
    //endregion

    //region UI State Update
    private fun updateItemNumber(itemNumber: Int) {
        updateUiState { it.copy(itemNumber = itemNumber) }
    }

    private fun updateTitle(itemTitle: String) {
        updateUiState { it.copy(title = itemTitle) }
    }

    private fun updateTitleValidationState(result: InputTextValidationState) {
        updateUiState { it.copy(titleValidationState = result) }
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
    //endregion

    //region Pending History Item Delete Parameters
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
    //endregion

    private companion object {
        const val ARGUMENT_DIARY_ITEM_TITLE_KEY = "diary_item_title"

        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
