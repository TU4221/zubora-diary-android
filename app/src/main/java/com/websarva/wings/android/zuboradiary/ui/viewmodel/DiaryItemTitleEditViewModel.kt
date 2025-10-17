package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.text.ValidateInputTextUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.result.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryItemTitleEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.utils.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    private val loadDiaryItemTitleSelectionHistoryListUseCase: LoadDiaryItemTitleSelectionHistoryListUseCase,
    private val deleteDiaryItemTitleSelectionHistoryUseCase: DeleteDiaryItemTitleSelectionHistoryUseCase,
    private val validateInputTextUseCase: ValidateInputTextUseCase
) : BaseViewModel<DiaryItemTitleEditEvent, DiaryItemTitleEditAppMessage, DiaryItemTitleEditState>(
    DiaryItemTitleEditState.Idle
) {

    override val isProgressIndicatorVisible =
        uiState
            .map { state ->
                when (state) {
                    DiaryItemTitleEditState.LoadingSelectionHistory -> true

                    DiaryItemTitleEditState.Idle,
                    DiaryItemTitleEditState.NoSelectionHistory,
                    DiaryItemTitleEditState.ShowingSelectionHistory -> false
                }
            }.stateInWhileSubscribed(
                false
            )

    private val _itemNumber = MutableStateFlow<DiaryItemNumber?>(null)
    val itemNumber get() =
        _itemNumber.map { it?.value }.stateInWhileSubscribed(_itemNumber.value)

    private val _itemTitle = MutableStateFlow("")
    val itemTitle get() = _itemTitle.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val itemTitleMutable get() = _itemTitle

    private val _itemTitleInputTextValidationResult =
        MutableStateFlow<InputTextValidationResult>(InputTextValidationResult.Valid)
    val itemTitleInputTextValidationResult
        get() = _itemTitleInputTextValidationResult.asStateFlow()

    val isNewItemTitleSelectionEnabled =
        _itemTitleInputTextValidationResult
            .map { it == InputTextValidationResult.Valid }
            .stateInWhileSubscribed(false)

    private val initialItemTitleSelectionHistoryList = DiaryItemTitleSelectionHistoryListUi(emptyList())
    lateinit var itemTitleSelectionHistoryList: StateFlow<DiaryItemTitleSelectionHistoryListUi>

    // キャッシュパラメータ
    private var pendingHistoryItemDeleteParameters: HistoryItemDeleteParameters? = null

    init {
        setUpItemTitleSelectionHistoryList()
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
        val itemNumber = _itemNumber.requireValue()
        val itemTitle = _itemTitle.value
        launchWithUnexpectedErrorHandler {
            completeItemTitleEdit(itemNumber, itemTitle = itemTitle)
        }
    }

    fun onDiaryItemTitleSelectionHistoryListItemClick(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemNumber = _itemNumber.requireValue()
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

    // Fragmentからの結果受取処理
    fun onDiaryItemTitleDataReceived(diaryItemTitleSelection: DiaryItemTitleSelectionUi) {
        val itemNumber = DiaryItemNumber(diaryItemTitleSelection.itemNumber)
        updateItemNumber(itemNumber)

        val itemTitle = diaryItemTitleSelection.title
        updateItemTitle(itemTitle)
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

    // StateFlow値変更時処理
    fun onItemTitleChanged(title: String) {
        clearNewDiaryItemTitleErrorMessage(title)
    }

    // データ処理
    private fun setUpItemTitleSelectionHistoryList() {
        val logMsg = "日記項目タイトル選択履歴読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryItemTitleEditState.LoadingSelectionHistory)
        itemTitleSelectionHistoryList =
            loadDiaryItemTitleSelectionHistoryListUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            if (it.value.isEmpty) {
                                updateUiState(DiaryItemTitleEditState.NoSelectionHistory)
                            } else {
                                updateUiState(DiaryItemTitleEditState.ShowingSelectionHistory)
                            }
                            it.value.toUiModel()
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
                            initialItemTitleSelectionHistoryList
                        }
                    }
                }.stateInWhileSubscribed(
                    initialItemTitleSelectionHistoryList
                )
    }

    private suspend fun completeItemTitleEdit(
        itemNumber: DiaryItemNumber,
        itemId: DiaryItemTitleSelectionHistoryId = DiaryItemTitleSelectionHistoryId.generate(),
        itemTitle: String
    ) {
        when (val result = validateInputTextUseCase(itemTitle).value) {
            InputTextValidationResult.Valid -> {
                val diaryItemTitleSelection =
                    DiaryItemTitleSelectionUi(itemNumber.value, itemId.value, itemTitle)
                emitUiEvent(
                    DiaryItemTitleEditEvent.CompleteEdit(diaryItemTitleSelection)
                )
            }
            InputTextValidationResult.InvalidEmpty -> {
                updateItemTitleInputTextValidationResult(result)
            }
            InputTextValidationResult.InvalidInitialCharUnmatched -> {
                updateItemTitleInputTextValidationResult(result)
            }
        }
    }

    private fun clearNewDiaryItemTitleErrorMessage(itemTitle: String) {
        if (_itemTitleInputTextValidationResult.value == InputTextValidationResult.Valid) return

        updateItemTitleInputTextValidationResult(
            validateInputTextUseCase(itemTitle).value
        )
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

    private fun updateItemNumber(itemNumber: DiaryItemNumber?) {
        _itemNumber.value = itemNumber
    }

    private fun updateItemTitle(itemTitle: String) {
        _itemTitle.value = itemTitle
    }

    private fun updateItemTitleInputTextValidationResult(result: InputTextValidationResult) {
        _itemTitleInputTextValidationResult.value = result
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
