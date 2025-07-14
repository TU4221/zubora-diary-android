package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalDateTime

// TODO:DiaryEditViewModelのロジックを含めるか、分離するか検討
internal class DiaryStateFlow(scope: CoroutineScope, handle: SavedStateHandle) {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER

        private const val SAVED_DATE_STATE_KEY = "date"
        private const val SAVED_WEATHER_1_STATE_KEY = "weather1"
        private const val SAVED_WEATHER_2_STATE_KEY = "weather2"
        private const val SAVED_CONDITION_STATE_KEY = "condition"
        private const val SAVED_TITLE_STATE_KEY = "title"
        private const val SAVED_NUM_VISIBLE_ITEMS_STATE_KEY = "numVisibleItems"
        private const val SAVED_IMAGE_URI_STATE_KEY = "imageUri"
        private const val SAVED_LOG_STATE_KEY = "log"
    }

    // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
    //      StateFlow変数を用意しても意味がないので作成しない。
    private val initialDate = null
    val date =
        MutableStateFlow<LocalDate?>(
            handle[SAVED_DATE_STATE_KEY] ?: initialDate // MEMO:初期化時日付が未定の為、null許容型とする。
        )

    private val initialWeather = Weather.UNKNOWN
    val weather1 = MutableStateFlow(handle[SAVED_WEATHER_1_STATE_KEY] ?: initialWeather)
    val weather2 = MutableStateFlow(handle[SAVED_WEATHER_2_STATE_KEY] ?: initialWeather)

    private val initialCondition = Condition.UNKNOWN
    val condition = MutableStateFlow(handle[SAVED_CONDITION_STATE_KEY] ?: initialCondition)

    private val initialTitle = ""
    val title = MutableStateFlow(handle[SAVED_TITLE_STATE_KEY] ?: initialTitle)

    private val initialNumVisibleItems = 1
    val numVisibleItems =
        MutableStateFlow(handle[SAVED_NUM_VISIBLE_ITEMS_STATE_KEY] ?: initialNumVisibleItems)

    private val items = Array(MAX_ITEMS) { i -> DiaryItemStateFlow(scope, handle, i + 1)}

    private val initialImageUri = null
    val imageUri =
        MutableStateFlow<Uri?>( // MEMO:初期化時Uri有無が未定の為、null許容型とする。
            handle[SAVED_IMAGE_URI_STATE_KEY] ?: initialImageUri
        )

    private val initialLog = null
    val log =
        MutableStateFlow<LocalDateTime?>( // MEMO:初期化時日付有無が未定の為、null許容型とする。
            handle[SAVED_LOG_STATE_KEY] ?: initialLog
        )

    init {
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
        imageUri.onEach {
            handle[SAVED_IMAGE_URI_STATE_KEY] = it
        }.launchIn(scope)
        log.onEach {
            handle[SAVED_LOG_STATE_KEY] = it
        }.launchIn(scope)
    }

    fun initialize() {
        date.value = initialDate
        weather1.value = initialWeather
        weather2.value = initialWeather
        condition.value = initialCondition
        title.value = initialTitle
        numVisibleItems.value = initialNumVisibleItems
        for (item in items) {
            item.initialize()
        }
        imageUri.value = initialImageUri
        log.value = initialLog
    }

    fun update(diary: Diary) {
        date.value = diary.date
        weather1.value = diary.weather1
        weather2.value = diary.weather2
        condition.value = diary.condition
        title.value = diary.title

        val item1Title = diary.item1Title
        val item1Comment = diary.item1Comment
        items[0].update(item1Title, item1Comment)

        val item2Title = diary.item2Title
        val item2Comment = diary.item2Comment
        items[1].update(item2Title, item2Comment)

        val item3Title = diary.item3Title
        val item3Comment = diary.item3Comment
        items[2].update(item3Title, item3Comment)

        val item4Title = diary.item4Title
        val item4Comment = diary.item4Comment
        items[3].update(item4Title, item4Comment)

        val item5Title = diary.item5Title
        val item5Comment = diary.item5Comment
        items[4].update(item5Title, item5Comment)

        var numVisibleItems = items.size
        val maxArrayNumber = numVisibleItems - 1
        for (i in maxArrayNumber downTo 1) {
            if (items[i].isEmpty) {
                numVisibleItems--
            } else {
                break
            }
        }
        this.numVisibleItems.value = numVisibleItems

        imageUri.value = diary.imageUriString?.let { Uri.parse(it) }
        log.value = diary.log
    }

    fun createDiary(): Diary {
        return Diary(
            date.value ?:throw IllegalStateException("日付なし(null)"),
            LocalDateTime.now(),
            weather1.value,
            weather2.value,
            condition.value,
            title.value.trim(),
            items[0].title.value.trim(),
            items[0].comment.value.trim(),
            items[1].title.value.trim(),
            items[1].comment.value.trim(),
            items[2].title.value.trim(),
            items[2].comment.value.trim(),
            items[3].title.value.trim(),
            items[3].comment.value.trim(),
            items[4].title.value.trim(),
            items[4].comment.value.trim(),
            imageUri.value?.toString()
            )
    }

    fun createDiaryItemTitleSelectionHistoryList(): List<DiaryItemTitleSelectionHistoryItem> {
        val list: MutableList<DiaryItemTitleSelectionHistoryItem> = ArrayList()
        for (i in 0 until MAX_ITEMS) {
            val itemTitle = items[i].title.value
            val itemTitleUpdateLog = items[i].titleUpdateLog.value ?: continue
            if (itemTitle.matches("\\S+.*".toRegex())) {
                val item =
                    DiaryItemTitleSelectionHistoryItem(
                        itemTitle,
                        itemTitleUpdateLog
                    )
                list.add(item)
            }
        }
        return list
    }

    fun incrementVisibleItemsCount() {
        val numVisibleItems = numVisibleItems.requireValue()
        val incrementedNumVisibleItems = numVisibleItems + 1
        this.numVisibleItems.value = incrementedNumVisibleItems
    }

    fun deleteItem(itemNumber: ItemNumber) {
        getItemStateFlow(itemNumber).initialize()
        val numVisibleItems = numVisibleItems.value

        if (itemNumber.value < numVisibleItems) {
            for (i in itemNumber.value until numVisibleItems) {
                val targetItemNumber = ItemNumber(i)
                val nextItemNumber = targetItemNumber.inc()
                getItemStateFlow(targetItemNumber).update(
                    getItemStateFlow(nextItemNumber).title.value,
                    getItemStateFlow(nextItemNumber).comment.value,
                    getItemStateFlow(nextItemNumber).titleUpdateLog.value
                )
                getItemStateFlow(nextItemNumber).initialize()
            }
        }

        if (numVisibleItems > ItemNumber.MIN_NUMBER) {
            val decrementedNumVisibleItems = numVisibleItems - 1
            this.numVisibleItems.value = decrementedNumVisibleItems
        }
    }

    fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        getItemStateFlow(itemNumber).updateItemTitle(title)
    }

    fun getItemStateFlow(itemNumber: ItemNumber): DiaryItemStateFlow {
        val arrayNumber = itemNumber.value - 1
        return items[arrayNumber]
    }

    class DiaryItemStateFlow(
        scope: CoroutineScope,
        handle: SavedStateHandle,
        val itemNumber: Int
    ) {

        companion object {
            private const val MIN_ITEM_NUMBER = ItemNumber.MIN_NUMBER
            private const val MAX_ITEM_NUMBER = ItemNumber.MAX_NUMBER

            private const val SAVED_ITEM_TITLE_STATE_KEY = "itemTitle"
            private const val SAVED_ITEM_COMMENT_STATE_KEY = "itemComment"
            private const val SAVED_ITEM_UPDATE_LOG_STATE_KEY = "itemUpdateLog"
        }

        // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
        //      StateFlow変数を用意しても意味がないので作成しない。
        private val initialTitle = ""
        val title =
            MutableStateFlow(handle[SAVED_ITEM_TITLE_STATE_KEY+ itemNumber] ?: initialTitle)

        private val initialComment = ""
        val comment =
            MutableStateFlow(handle[SAVED_ITEM_COMMENT_STATE_KEY+ itemNumber] ?: initialComment)

        // MEMO:初期化時日付有無が未定、タイトル未更新のケースがある為、null許容型とする。
        private val initialUpdateLog = null
        val titleUpdateLog =
            MutableStateFlow<LocalDateTime?>(
                handle[SAVED_ITEM_UPDATE_LOG_STATE_KEY+ itemNumber] ?: initialUpdateLog
            )

        init {
            require(isItemNumberInRange(itemNumber))

            title.onEach {
                handle[SAVED_ITEM_TITLE_STATE_KEY+ itemNumber] = it
            }.launchIn(scope)
            comment.onEach {
                handle[SAVED_ITEM_COMMENT_STATE_KEY+ itemNumber] = it
            }.launchIn(scope)
            titleUpdateLog.onEach {
                handle[SAVED_ITEM_UPDATE_LOG_STATE_KEY+ itemNumber] = it
            }.launchIn(scope)
        }

        private fun isItemNumberInRange(itemNumber: Int): Boolean {
            return itemNumber in MIN_ITEM_NUMBER..MAX_ITEM_NUMBER
        }

        fun initialize() {
            title.value = initialTitle
            comment.value = initialComment
            titleUpdateLog.value = initialUpdateLog
        }

        fun update(
            title: String,
            comment: String,
            titleUpdateLog: LocalDateTime? = initialUpdateLog
        ) {
            this.title.value = title
            this.comment.value = comment
            this.titleUpdateLog.value = titleUpdateLog
        }

        fun updateItemTitle(title: String) {
            this.title.value = title
            titleUpdateLog.value = LocalDateTime.now()
        }

        val isEmpty: Boolean
            get() {
                val title = this.title.value
                val comment = this.comment.value
                return title.isEmpty() && comment.isEmpty()
            }
    }
}
