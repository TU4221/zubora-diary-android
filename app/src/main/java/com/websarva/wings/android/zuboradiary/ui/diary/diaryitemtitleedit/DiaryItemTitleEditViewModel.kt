package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.notNullValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
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

    lateinit var itemTitleSelectionHistoryList: LiveData<SelectionHistoryList>

    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        _itemNumber.value = null
        _itemTitle.value = ""
        setUpItemTitleSelectionHistoryList()
    }

    private fun setUpItemTitleSelectionHistoryList() {
        itemTitleSelectionHistoryList =
            diaryItemTitleSelectionHistoryRepository
                .loadSelectionHistory(MAX_LOADED_ITEM_TITLES, 0)
                .map { list ->
                    list.map { item ->
                        SelectionHistoryListItem(item)

                    }
                }.map { list ->
                    SelectionHistoryList(list)
                }
                .asLiveData()
    }

    fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    suspend fun deleteDiaryItemTitleSelectionHistoryItem(deletePosition: Int): Boolean {
        require(deletePosition >= 0)

        val currentList = itemTitleSelectionHistoryList.notNullValue()
        val listSize = currentList.selectionHistoryListItemList.size
        require(deletePosition < listSize)

        val deleteItem = currentList.selectionHistoryListItemList[deletePosition]
        val deleteTitle = deleteItem.title
        try {
            diaryItemTitleSelectionHistoryRepository.deleteSelectionHistoryItem(deleteTitle)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE_ERROR)
            return false
        }
        return true
    }
}
