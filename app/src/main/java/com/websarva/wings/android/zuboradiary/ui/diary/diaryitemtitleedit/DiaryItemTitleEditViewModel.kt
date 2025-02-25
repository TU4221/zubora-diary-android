package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryItemTitleEditViewModel @Inject constructor(
    private val diaryItemTitleSelectionHistoryRepository: DiaryItemTitleSelectionHistoryRepository
) : BaseViewModel() {

    companion object {
        private const val MAX_LOADED_ITEM_TITLES = 50
    }

    private val initialItemNumber = null
    private val _itemNumber = MutableStateFlow<ItemNumber?>(initialItemNumber)
    val itemNumber get() = _itemNumber.asStateFlow()

    private val initialItemTitle = ""
    private val _itemTitle = MutableStateFlow(initialItemTitle)
    val itemTitle get() = _itemTitle.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val itemTitleMutable get() = _itemTitle

    lateinit var itemTitleSelectionHistoryList: StateFlow<SelectionHistoryList>
        private set

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
        viewModelScope.launch(Dispatchers.IO) {
            itemTitleSelectionHistoryList =
                diaryItemTitleSelectionHistoryRepository
                    .loadSelectionHistory(MAX_LOADED_ITEM_TITLES, 0)
                    .map { list ->
                        list.map { item ->
                            SelectionHistoryListItem(item)

                        }
                    }.map { list ->
                        SelectionHistoryList(list)
                    }.stateIn(this)
        }
    }

    fun updateDiaryItemTitle(itemNumber: ItemNumber, itemTitle: String) {
        _itemNumber.value = itemNumber
        _itemTitle.value = itemTitle
    }

    suspend fun deleteDiaryItemTitleSelectionHistoryItem(deletePosition: Int): Boolean {
        require(deletePosition >= 0)

        val currentList = itemTitleSelectionHistoryList.value
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
