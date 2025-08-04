package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryFetchFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryItemUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.InputTextValidateResult
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
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
                when (state) {
                    DiaryItemTitleEditState.LoadingSelectionHistory -> true

                    DiaryItemTitleEditState.Idle,
                    DiaryItemTitleEditState.NoSelectionHistory,
                    DiaryItemTitleEditState.ShowingSelectionHistory -> false
                }
            }.stateInWhileSubscribed(
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

    private val initialItemTitleErrorMessageResId = null
    private val _itemTitleErrorMessageResId =
        MutableStateFlow<Int?>(initialItemTitleErrorMessageResId)
    val itemTitleErrorMessageResId
        get() = _itemTitleErrorMessageResId.asStateFlow()

    val isNewItemTitleSelectionEnabled =
        _itemTitleErrorMessageResId
            .map { it == null }
            .stateInWhileSubscribed(false)

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

    fun onNavigationClicked() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onNewDiaryItemTitleSelectionButtonClicked() {
        val itemNumber = _itemNumber.requireValue()
        val itemTitle = _itemTitle.value
        viewModelScope.launch {
            completeItemTitleEdit(itemNumber, itemTitle)
        }
    }

    fun onDiaryItemTitleSelectionHistoryItemClicked(itemTitle: String) {
        val itemNumber = _itemNumber.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                DiaryItemTitleEditEvent.CompleteEdit(
                    DiaryItemTitle(
                        itemNumber,
                        itemTitle
                    )
                )
            )
        }
    }

    fun onDiaryItemTitleSelectionHistoryItemDeleteButtonClicked(itemTitle: String) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryItemTitleEditEvent
                    .NavigateSelectionHistoryItemDeleteDialog(
                        DiaryItemTitleSelectionHistoryItemDeleteParameters(itemTitle)
                    )
            )
        }
    }

    fun onDiaryItemTitleDataReceivedFromPreviousFragment(diaryItemTitle: DiaryItemTitle) {
        val itemNumber = diaryItemTitle.itemNumber
        val itemTitle = diaryItemTitle.title
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
            emitUiEvent(
                DiaryItemTitleEditEvent.CloseSwipedItem
            )
        }
    }

    fun onItemTitleChanged(title: String) {
        clearNewDiaryItemTitleErrorMessage(title)
    }

    private fun setUpItemTitleSelectionHistoryList() {
        val logMsg = "日記項目タイトル選択履歴読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryItemTitleEditState.LoadingSelectionHistory)
        itemTitleSelectionHistoryList =
            fetchDiaryItemTitleSelectionHistoryUseCase().value
                .catch {
                    when (it) {
                        is DiaryItemTitleSelectionHistoryFetchFailureException -> {
                            emitAppMessageEvent(
                                DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadingFailure
                            )
                        }
                        else -> throw it
                    }
                }.map { list ->
                    if (list.isEmpty()) {
                        updateUiState(DiaryItemTitleEditState.NoSelectionHistory)
                    } else {
                        updateUiState(DiaryItemTitleEditState.ShowingSelectionHistory)
                    }
                    SelectionHistoryList(
                        list.map { item ->
                            SelectionHistoryListItem(item)
                        }
                    )
                }.stateInWhileSubscribed(
                    initialItemTitleSelectionHistoryList
                )
    }

    private fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    private suspend fun completeItemTitleEdit(itemNumber: ItemNumber, itemTitle: String) {
        when (val result = validateNewDiaryItemTitleSelectable(itemTitle)) {
            InputTextValidateResult.Valid -> {
                val diaryItemTitle = DiaryItemTitle(itemNumber, itemTitle)
                emitUiEvent(
                    DiaryItemTitleEditEvent.CompleteEdit(
                        diaryItemTitle
                    )
                )
            }
            is InputTextValidateResult.Invalid -> {
                _itemTitleErrorMessageResId.value = result.errorMessageResId
            }
        }
    }

    private fun clearNewDiaryItemTitleErrorMessage(itemTitle: String) {
        if (_itemTitleErrorMessageResId.value == null) return

        when (val result =validateNewDiaryItemTitleSelectable(itemTitle)) {
            InputTextValidateResult.Valid -> {
                _itemTitleErrorMessageResId.value = null
            }
            is InputTextValidateResult.Invalid -> {
                _itemTitleErrorMessageResId.value = result.errorMessageResId
                return
            }
        }
    }

    private fun validateNewDiaryItemTitleSelectable(title: String): InputTextValidateResult {
        // 空欄
        if (title.isEmpty()) {
            return InputTextValidateResult.Invalid(
                R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_empty
            )
        }

        // 先頭が空白文字(\\s)
        if (title.matches("\\s+.*".toRegex())) {
            return InputTextValidateResult.Invalid(
                R.string.fragment_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched
            )
        }

        return InputTextValidateResult.Valid
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
