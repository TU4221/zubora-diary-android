package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryItemTitleEditViewModel @Inject constructor(
    private val diaryItemTitleSelectionHistoryRepository: DiaryItemTitleSelectionHistoryRepository
) : BaseViewModel() {

    companion object {
        private const val MAX_LOADED_ITEM_TITLES = 50
    }

    private val _itemNumber = MutableLiveData<ItemNumber?>()
    val itemNumber: LiveData<ItemNumber?> get() = _itemNumber

    private val _itemTitle = MutableLiveData<String>()
    val itemTitle: LiveData<String> get() = _itemTitle

    /**
     * LayoutDataBinding用
     * */
    val itemTitleMutable get() = _itemTitle

    private val _itemTitleSelectionHistoryList = MutableLiveData<SelectionHistoryList>()
    val itemTitleSelectionHistoryLiveData: LiveData<SelectionHistoryList>
        get() = _itemTitleSelectionHistoryList


    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        _itemNumber.value = null
        _itemTitle.value = ""
        _itemTitleSelectionHistoryList.value = SelectionHistoryList()
    }

    fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    fun loadDiaryItemTitleSelectionHistory() {
        val loadedList: List<DiaryItemTitleSelectionHistoryItemEntity>
        try {
            loadedList =
                diaryItemTitleSelectionHistoryRepository
                    .loadSelectionHistory(MAX_LOADED_ITEM_TITLES, 0).get()
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_ITEM_TITLE_HISTORY_LOADING_ERROR)
            return
        }
        val itemList: MutableList<SelectionHistoryListItem> = ArrayList()
        loadedList.stream().forEach { x: DiaryItemTitleSelectionHistoryItemEntity ->
            itemList.add(
                SelectionHistoryListItem(x)
            )
        }
        val list = SelectionHistoryList(itemList)
        _itemTitleSelectionHistoryList.value = list
    }

    fun deleteDiaryItemTitleSelectionHistoryItem(deletePosition: Int) {
        require(deletePosition >= 0)

        val currentList = _itemTitleSelectionHistoryList.checkNotNull()
        val listSize = currentList.selectionHistoryListItemList.size
        require(deletePosition < listSize)

        val deleteItem = currentList.selectionHistoryListItemList[deletePosition]
        val deleteTitle = deleteItem.title
        try {
            diaryItemTitleSelectionHistoryRepository.deleteSelectionHistoryItem(deleteTitle).get()
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE_ERROR)
            return
        }

        _itemTitleSelectionHistoryList.value = currentList.deleteItem(deletePosition)
    }
}
