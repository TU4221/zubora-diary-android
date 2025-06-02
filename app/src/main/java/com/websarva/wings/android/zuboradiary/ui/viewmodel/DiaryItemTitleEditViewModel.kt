package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle.SelectionHistoryListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiaryItemTitleEditViewModel @Inject constructor(
    private val diaryItemTitleSelectionHistoryRepository: DiaryItemTitleSelectionHistoryRepository
) : BaseViewModel() {

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
    private val _itemTitleSelectionHistoryList = MutableStateFlow(initialItemTitleSelectionHistoryList)
    val itemTitleSelectionHistoryList = _itemTitleSelectionHistoryList.asStateFlow()

    init {
        setUpItemTitleSelectionHistoryList()
    }

    override fun initialize() {
        super.initialize()
        _itemNumber.value = initialItemNumber
        _itemTitle.value = initialItemTitle
        _itemTitleSelectionHistoryList.value = initialItemTitleSelectionHistoryList
        setUpItemTitleSelectionHistoryList()
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        // TODO("Not yet implemented")
    }

    private fun setUpItemTitleSelectionHistoryList() {
        val logMsg = "日記項目タイトル選択履歴読込"
        Log.i(logTag, "${logMsg}_開始")

        viewModelScope.launch {
            try {
                diaryItemTitleSelectionHistoryRepository
                    .loadSelectionHistory(maxLoadedItemTitles, 0)
                    .map { list ->
                        SelectionHistoryList(
                            list.map { item ->
                                SelectionHistoryListItem(item)
                            }
                        )
                    }.stateIn(this)
                    .collectLatest { selectionHistoryList ->
                        _itemTitleSelectionHistoryList.value = selectionHistoryList
                    }
                Log.i(logTag, "${logMsg}_完了")
            } catch (e: Exception) {
                Log.e(logTag, "${logMsg}_失敗", e)
                addAppMessage(DiaryItemTitleEditAppMessage.ItemTitleHistoryLoadingFailure)
            }
        }
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
            diaryItemTitleSelectionHistoryRepository.deleteSelectionHistoryItem(deleteTitle)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            addAppMessage(DiaryItemTitleEditAppMessage.ItemTitleHistoryDeleteFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
    }
}
