package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryItemTitleSelectionHistoryFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryItemUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryItemTitleEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    private val fetchDiaryItemTitleSelectionHistoryUseCase: FetchDiaryItemTitleSelectionHistoryUseCase,
    private val deleteDiaryItemTitleSelectionHistoryItemUseCase: DeleteDiaryItemTitleSelectionHistoryItemUseCase
) : BaseViewModel<DiaryItemTitleEditEvent, DiaryItemTitleEditAppMessage, DiaryItemTitleEditState>(
    DiaryItemTitleEditState.Idle
) {

    private val logTag = createLogTag()

    override val isProcessingState =
        uiState
            .map { state ->
                // TODO:保留
                when (state) {
                    DiaryItemTitleEditState.Idle -> false
                }
            }.stateInDefault(
                viewModelScope,
                false
            )

    private val initialItemNumber: ItemNumber? = null
    private val _itemNumber = MutableStateFlow(initialItemNumber)
    val itemNumber get() = _itemNumber.asStateFlow()

    private val initialItemTitle = ""
    private val _itemTitle = MutableStateFlow(initialItemTitle)
    val itemTitle get() = _itemTitle.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val itemTitleMutable get() = _itemTitle

    private val initialItemTitleSelectionHistoryList = SelectionHistoryList(emptyList())
    lateinit var itemTitleSelectionHistoryList: StateFlow<SelectionHistoryList>

    init {
        setUpItemTitleSelectionHistoryList()
    }

    override fun initialize() {
        super.initialize()
        _itemNumber.value = initialItemNumber
        _itemTitle.value = initialItemTitle
        setUpItemTitleSelectionHistoryList()
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onNavigationClicked() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onNewDiaryItemTitleSelectionButtonClicked() {
        val itemNumber = _itemNumber.requireValue()
        val itemTitle = _itemTitle.value
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryItemTitleEditEvent.CompleteEdit(
                    itemNumber,
                    itemTitle
                )
            )
        }
    }

    fun onDiaryItemTitleSelectionHistoryItemClicked(itemTitle: String) {
        val itemNumber = _itemNumber.requireValue()
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryItemTitleEditEvent.CompleteEdit(
                    itemNumber,
                    itemTitle
                )
            )
        }
    }

    fun onDiaryItemTitleSelectionHistoryItemDeleteButtonClicked(itemTitle: String) {
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryItemTitleEditEvent
                    .NavigateSelectionHistoryItemDeleteDialog(
                        DiaryItemTitleSelectionHistoryItemDeleteParameters(itemTitle)
                    )
            )
        }
    }

    fun onDiaryItemTitleDataReceivedFromPreviousFragment(
        itemNumber: ItemNumber,
        itemTitle: String
    ) {
        updateDiaryItemTitle(itemNumber, itemTitle)
    }

    fun onDiaryItemTitleSelectionHistoryDeleteDialogResultReceived(
        result: DialogResult<DiaryItemTitleSelectionHistoryItemDeleteParameters>
    ) {
        when (result) {
            is DialogResult.Positive -> {
                onDiaryItemTitleSelectionHistoryDeleteDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                onDiaryItemTitleSelectionHistoryDeleteDialogNegativeResultReceived()
            }
        }
    }

    private fun onDiaryItemTitleSelectionHistoryDeleteDialogPositiveResultReceived(
        parameters: DiaryItemTitleSelectionHistoryItemDeleteParameters
    ) {
        val deleteTitle = parameters.itemTitle
        viewModelScope.launch {
            deleteDiaryItemTitleSelectionHistoryItem(deleteTitle)
        }
    }

    private fun onDiaryItemTitleSelectionHistoryDeleteDialogNegativeResultReceived() {
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryItemTitleEditEvent.CloseSwipedItem
            )
        }
    }

    private fun setUpItemTitleSelectionHistoryList() {
        val logMsg = "日記項目タイトル選択履歴読込"
        Log.i(logTag, "${logMsg}_開始")

        itemTitleSelectionHistoryList =
            fetchDiaryItemTitleSelectionHistoryUseCase().value
                .catch {
                    when (it) {
                        is FetchDiaryItemTitleSelectionHistoryFailedException -> {
                            emitAppMessageEvent(
                                DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadingFailure
                            )
                        }
                        else -> throw it
                    }
                }.map { list ->
                    SelectionHistoryList(
                        list.map { item ->
                            SelectionHistoryListItem(item)
                        }
                    )
                }.stateInDefault(
                    viewModelScope,
                    initialItemTitleSelectionHistoryList
                )
    }

    private fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    private suspend fun deleteDiaryItemTitleSelectionHistoryItem(deleteTitle: String) {
        val logMsg = "日記項目タイトル選択履歴アイテム削除"
        Log.i(logTag, "${logMsg}_開始")

        val result = deleteDiaryItemTitleSelectionHistoryItemUseCase(deleteTitle)
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
}
