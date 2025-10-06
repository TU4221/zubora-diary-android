package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.text.ValidateInputTextUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.result.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryItemTitleEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    private val loadDiaryItemTitleSelectionHistoryListUseCase: LoadDiaryItemTitleSelectionHistoryListUseCase,
    private val deleteDiaryItemTitleSelectionHistoryUseCase: DeleteDiaryItemTitleSelectionHistoryUseCase,
    private val validateInputTextUseCase: ValidateInputTextUseCase
) : BaseViewModel<DiaryItemTitleEditEvent, DiaryItemTitleEditAppMessage, DiaryItemTitleEditState>(
    DiaryItemTitleEditState.Idle
) {

    private val logTag = createLogTag()

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

    private val _itemNumber = MutableStateFlow<ItemNumber?>(null)
    val itemNumber get() = _itemNumber.asStateFlow()

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
    private data class HistoryItemDeleteParameters(
        val itemId: UUIDString,
        val itemTitle: String
    )
    private var pendingHistoryItemDeleteParameters: HistoryItemDeleteParameters? = null

    init {
        setUpItemTitleSelectionHistoryList()
    }

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryItemTitleEditEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: DiaryItemTitleEditAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryItemTitleEditEvent.CommonEvent(
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
    fun onNavigationIconClick() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onNewDiaryItemTitleSelectionButtonClick() {
        val itemNumber = _itemNumber.requireValue()
        val itemTitle = _itemTitle.value
        viewModelScope.launch {
            completeItemTitleEdit(itemNumber, itemTitle = itemTitle)
        }
    }

    fun onDiaryItemTitleSelectionHistoryListItemClick(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemNumber = _itemNumber.requireValue()
        val itemId = item.id
        val itemTitle = item.title
        viewModelScope.launch {
            completeItemTitleEdit(
                itemNumber,
                UUIDString(itemId),
                itemTitle
            )
        }
    }

    fun onDiaryItemTitleSelectionHistoryListItemSwipe(item: DiaryItemTitleSelectionHistoryListItemUi) {
        val itemId = item.id
        val itemTitle = item.title
        viewModelScope.launch {
            updatePendingHistoryItemDeleteParameters(UUIDString(itemId), itemTitle)
            emitUiEvent(
                DiaryItemTitleEditEvent
                    .NavigateSelectionHistoryItemDeleteDialog(
                        itemTitle
                    )
            )
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryItemTitleDataReceived(diaryItemTitle: DiaryItemTitle) {
        val itemNumber = ItemNumber(diaryItemTitle.itemNumber)
        updateItemNumber(itemNumber)

        val itemTitle = diaryItemTitle.title
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
        viewModelScope.launch {
            parameters?.let {
                deleteDiaryItemTitleSelectionHistory(it.itemId, it.itemTitle)
            }
        }
    }

    private fun handleDiaryItemTitleSelectionHistoryDeleteDialogNegativeResult() {
        viewModelScope.launch {
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
                            emitAppMessageEvent(
                                DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadFailure
                            )
                            initialItemTitleSelectionHistoryList
                        }
                    }
                }.stateInWhileSubscribed(
                    initialItemTitleSelectionHistoryList
                )
    }

    private suspend fun completeItemTitleEdit(
        itemNumber: ItemNumber,
        itemId: UUIDString = UUIDString(),
        itemTitle: String
    ) {
        when (val result = validateInputTextUseCase(itemTitle).value) {
            InputTextValidationResult.Valid -> {
                val diaryItemTitle =
                    DiaryItemTitle(itemNumber.value, itemId.value, itemTitle)
                emitUiEvent(
                    DiaryItemTitleEditEvent.CompleteEdit(diaryItemTitle)
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

    private suspend fun deleteDiaryItemTitleSelectionHistory(id: UUIDString, title: String) {
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
                emitAppMessageEvent(DiaryItemTitleEditAppMessage.ItemTitleHistoryDeleteFailure)
            }
        }
    }

    private fun updateItemNumber(itemNumber: ItemNumber?) {
        _itemNumber.value = itemNumber
    }

    private fun updateItemTitle(itemTitle: String) {
        _itemTitle.value = itemTitle
    }

    private fun updateItemTitleInputTextValidationResult(result: InputTextValidationResult) {
        _itemTitleInputTextValidationResult.value = result
    }

    private fun updatePendingHistoryItemDeleteParameters(itemId: UUIDString, itemTitle: String) {
        pendingHistoryItemDeleteParameters = HistoryItemDeleteParameters(
            itemId,
            itemTitle
        )
    }

    private fun clearPendingHistoryItemDeleteParameters() {
        pendingHistoryItemDeleteParameters = null
    }
}
