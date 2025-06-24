package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryItemTitleEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryItemTitleEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel<DiaryItemTitleEditEvent, DiaryItemTitleEditAppMessage, DiaryItemTitleEditState>() {

    private val logTag = createLogTag()

    private val maxLoadedItemTitles = 50

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
        // TODO("Not yet implemented")
    }

    private fun setUpItemTitleSelectionHistoryList() {
        val logMsg = "日記項目タイトル選択履歴読込"
        Log.i(logTag, "${logMsg}_開始")

        itemTitleSelectionHistoryList =
            diaryRepository
                .loadSelectionHistory(maxLoadedItemTitles, 0).catch {
                    emitAppMessageEvent(DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadingFailure)
                }.map { list ->
                    SelectionHistoryList(
                        list.map { item ->
                            SelectionHistoryListItem(item)
                        }
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = initialItemTitleSelectionHistoryList
                )
    }

    fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    suspend fun deleteDiaryItemTitleSelectionHistoryItem(deletePosition: Int): Boolean {
        require(deletePosition >= 0)

        val logMsg = "日記項目タイトル選択履歴アイテム削除"
        Log.i(logTag, "${logMsg}_開始")

        val currentList = itemTitleSelectionHistoryList.value
        val listSize = currentList.itemList.size
        require(deletePosition < listSize)

        val deleteItem = currentList.itemList[deletePosition]
        val deleteTitle = deleteItem.title
        try {
            diaryRepository.deleteSelectionHistoryItem(deleteTitle)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            emitAppMessageEvent(DiaryItemTitleEditAppMessage.ItemTitleHistoryDeleteFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
    }
}
