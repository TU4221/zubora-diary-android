package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleSelectionHistoryIdUi
import com.websarva.wings.android.zuboradiary.ui.model.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalDateTime

internal class DiaryEditStateFlow(scope: CoroutineScope, handle: SavedStateHandle) :
    DiaryStateFlow() {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER

        private const val SAVED_ID_STATE_KEY = "id"
        private const val SAVED_DATE_STATE_KEY = "date"
        private const val SAVED_WEATHER_1_STATE_KEY = "weather1"
        private const val SAVED_WEATHER_2_STATE_KEY = "weather2"
        private const val SAVED_CONDITION_STATE_KEY = "condition"
        private const val SAVED_TITLE_STATE_KEY = "title"
        private const val SAVED_NUM_VISIBLE_ITEMS_STATE_KEY = "numVisibleItems"
        private const val SAVED_IMAGE_FILE_NAME_STATE_KEY = "imageFileName"
        private const val SAVED_LOG_STATE_KEY = "log"
    }

    override val id = MutableStateFlow<DiaryIdUi?>(handle[SAVED_ID_STATE_KEY] ?: initialId)

    // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
    //      StateFlow変数を用意しても意味がないので作成しない。
    override val date =
        MutableStateFlow<LocalDate?>(
            handle[SAVED_DATE_STATE_KEY] ?: initialDate // MEMO:初期化時日付が未定の為、null許容型とする。
        )

    override val weather1 = MutableStateFlow(handle[SAVED_WEATHER_1_STATE_KEY] ?: initialWeather)
    override val weather2 = MutableStateFlow(handle[SAVED_WEATHER_2_STATE_KEY] ?: initialWeather)

    override val condition = MutableStateFlow(handle[SAVED_CONDITION_STATE_KEY] ?: initialCondition)

    override val title = MutableStateFlow(handle[SAVED_TITLE_STATE_KEY] ?: initialTitle)

    private val initialNumVisibleItems = run {
        var count = 1
        if (initialDiary.item2Title != null) count++
        if (initialDiary.item3Title != null) count++
        if (initialDiary.item4Title != null) count++
        if (initialDiary.item5Title != null) count++
        count
    }
    override val numVisibleItems =
        MutableStateFlow(handle[SAVED_NUM_VISIBLE_ITEMS_STATE_KEY] ?: initialNumVisibleItems)

    override val items =
        Array(MAX_ITEMS) { i -> DiaryItemStateFlow(scope, handle, i + 1) }

    override val imageFileName =
        MutableStateFlow(
            handle[SAVED_IMAGE_FILE_NAME_STATE_KEY] ?: initialImageFileName
        )

    override val log =
        MutableStateFlow<LocalDateTime?>( // MEMO:初期化時日付有無が未定の為、null許容型とする。
            handle[SAVED_LOG_STATE_KEY] ?: initialLog
        )

    init {
        id.onEach {
            handle[SAVED_ID_STATE_KEY] = it
        }.launchIn(scope)
        date.onEach {
            handle[SAVED_DATE_STATE_KEY] = it
        }.launchIn(scope)
        weather1.onEach {
            handle[SAVED_WEATHER_1_STATE_KEY] = it
        }.launchIn(scope)
        weather2.onEach {
            handle[SAVED_WEATHER_2_STATE_KEY] = it
        }.launchIn(scope)
        condition.onEach {
            handle[SAVED_CONDITION_STATE_KEY] = it
        }.launchIn(scope)
        title.onEach {
            handle[SAVED_TITLE_STATE_KEY] = it
        }.launchIn(scope)
        numVisibleItems.onEach {
            handle[SAVED_NUM_VISIBLE_ITEMS_STATE_KEY] = it
        }.launchIn(scope)
        log.onEach {
            handle[SAVED_LOG_STATE_KEY] = it
        }.launchIn(scope)
    }

    fun createDiary(): DiaryUi {
        return DiaryUi(
            id.value ?:throw IllegalStateException("IDなし(null)"),
            date.value ?:throw IllegalStateException("日付なし(null)"),
            LocalDateTime.now(),
            weather1.value,
            weather2.value,
            condition.value,
            title.value.trim(),
            items[0].title.value?.trim()
                ?: throw IllegalStateException("項目1タイトルなし(null)"),
            items[0].comment.value?.trim()
                ?: throw IllegalStateException("項目1コメントなし(null)"),
            items[1].title.value?.trim(),
            items[1].comment.value?.trim(),
            items[2].title.value?.trim(),
            items[2].comment.value?.trim(),
            items[3].title.value?.trim(),
            items[3].comment.value?.trim(),
            items[4].title.value?.trim(),
            items[4].comment.value?.trim(),
            imageFileName.value
            )
    }

    fun createDiaryItemTitleSelectionHistoryList(): List<DiaryItemTitleSelectionHistory> {
        return items.mapNotNull { item ->
            val title = item.title.value?.trim()
            val titleId = item.titleId.value
            val titleUpdateLog = item.titleUpdateLog.value

            if (title.isNullOrBlank() || titleId == null || titleUpdateLog == null) {
                null
            } else {
                DiaryItemTitleSelectionHistory(
                    titleId.toDomainModel(),
                    DiaryItemTitle(title),
                    titleUpdateLog
                )
            }
        }
    }

    fun incrementVisibleItemsCount() {
        val numVisibleItems = numVisibleItems.requireValue()
        val incrementedNumVisibleItems = numVisibleItems + 1
        Log.d("20250729", "incrementVisibleItemsCount()_$incrementedNumVisibleItems")
        updateNumVisibleItems(incrementedNumVisibleItems)
        initializeItemForEdit(ItemNumber(incrementedNumVisibleItems))
    }

    fun deleteItem(itemNumber: ItemNumber) {
        getItemStateFlow(itemNumber).initialize()
        val numVisibleItems = numVisibleItems.value

        if (itemNumber.value < numVisibleItems) {
            for (i in itemNumber.value until numVisibleItems) {
                val targetItemNumber = ItemNumber(i)
                val nextItemNumber = targetItemNumber.inc()

                updateItem(
                    targetItemNumber,
                    getItemStateFlow(nextItemNumber).titleId.value,
                    getItemStateFlow(nextItemNumber).title.value,
                    getItemStateFlow(nextItemNumber).comment.value,
                    getItemStateFlow(nextItemNumber).titleUpdateLog.value
                )
                getItemStateFlow(nextItemNumber).initialize()
            }
        }

        if (numVisibleItems > ItemNumber.MIN_NUMBER) {
            val decrementedNumVisibleItems = numVisibleItems - 1
            updateNumVisibleItems(decrementedNumVisibleItems)
        }
    }

    fun updateItemTitle(
        itemNumber: ItemNumber,
        titleId: DiaryItemTitleSelectionHistoryIdUi,
        title: String
    ) {
        updateItemTitleWithTimestamp(itemNumber, titleId, title)
    }

    class DiaryItemStateFlow(
        scope: CoroutineScope,
        handle: SavedStateHandle,
        itemNumber: Int
    ) : DiaryStateFlow.DiaryItemStateFlow(itemNumber) {

        companion object {
            private const val SAVED_ITEM_TITLE_STATE_KEY = "itemTitle"
            private const val SAVED_ITEM_COMMENT_STATE_KEY = "itemComment"
            private const val SAVED_ITEM_UPDATE_LOG_STATE_KEY = "itemUpdateLog"
        }

        // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
        //      StateFlow変数を用意しても意味がないので作成しない。
        override val title =
            MutableStateFlow(handle[SAVED_ITEM_TITLE_STATE_KEY + itemNumber] ?: initialTitle)

        override val comment =
            MutableStateFlow(handle[SAVED_ITEM_COMMENT_STATE_KEY + itemNumber] ?: initialComment)

        // MEMO:初期化時日付有無が未定、タイトル未更新のケースがある為、null許容型とする。
        override val titleUpdateLog =
            MutableStateFlow<LocalDateTime?>(
                handle[SAVED_ITEM_UPDATE_LOG_STATE_KEY + itemNumber] ?: initialTitleUpdateLog
            )

        init {
            title.onEach {
                handle[SAVED_ITEM_TITLE_STATE_KEY + itemNumber] = it
            }.launchIn(scope)
            comment.onEach {
                handle[SAVED_ITEM_COMMENT_STATE_KEY + itemNumber] = it
            }.launchIn(scope)
            titleUpdateLog.onEach {
                handle[SAVED_ITEM_UPDATE_LOG_STATE_KEY + itemNumber] = it
            }.launchIn(scope)
        }
    }
}
