package com.websarva.wings.android.zuboradiary.ui.diary

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.ui.requireValue
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime

internal class DiaryStateFlow {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER
    }

    // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
    //      StateFlow変数を用意しても意味がないので作成しない。
    private val initialDate = null
    val date = MutableStateFlow<LocalDate?>(initialDate) // MEMO:初期化時日付が未定の為、null許容型とする。

    private val initialWeather = Weather.UNKNOWN
    val weather1 = MutableStateFlow(initialWeather)
    val weather2 = MutableStateFlow(initialWeather)

    private val initialCondition = Condition.UNKNOWN
    val condition = MutableStateFlow(initialCondition)

    private val initialTitle = ""
    val title = MutableStateFlow(initialTitle)

    private val initialNumVisibleItems = 1
    val numVisibleItems = MutableStateFlow(initialNumVisibleItems)

    private val items = Array(MAX_ITEMS) { i -> DiaryItemStateFlow(i + 1)}

    private val initialPicturePath = null
    val picturePath = MutableStateFlow<Uri?>(initialPicturePath) // MEMO:初期化時Uri有無が未定の為、null許容型とする。

    private val initialLog = null
    val log = MutableStateFlow<LocalDateTime?>(initialLog) // MEMO:初期化時日付有無が未定の為、null許容型とする。

    init {
        initialize()
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
        picturePath.value = initialPicturePath
        log.value = initialLog
    }

    fun update(diaryEntity: DiaryEntity) {
        date.value = LocalDate.parse(diaryEntity.date)
        val intWeather1 = diaryEntity.weather1
        weather1.value = Weather.of(intWeather1)
        val intWeather2 = diaryEntity.weather2
        weather2.value = Weather.of(intWeather2)
        val intCondition = diaryEntity.condition
        condition.value = Condition.of(intCondition)
        val title = diaryEntity.title
        this.title.value = title

        val item1Title = diaryEntity.item1Title
        val item1Comment = diaryEntity.item1Comment
        items[0].update(item1Title, item1Comment)

        val item2Title = diaryEntity.item2Title
        val item2Comment = diaryEntity.item2Comment
        items[1].update(item2Title, item2Comment)

        val item3Title = diaryEntity.item3Title
        val item3Comment = diaryEntity.item3Comment
        items[2].update(item3Title, item3Comment)

        val item4Title = diaryEntity.item4Title
        val item4Comment = diaryEntity.item4Comment
        items[3].update(item4Title, item4Comment)

        val item5Title = diaryEntity.item5Title
        val item5Comment = diaryEntity.item5Comment
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

        val uriString = diaryEntity.picturePath
        if (uriString.isEmpty()) {
            picturePath.value = null
        } else {
            picturePath.value = Uri.parse(uriString)
        }

        log.value = LocalDateTime.parse(diaryEntity.log)
    }

    fun createDiaryEntity(): DiaryEntity {
        return DiaryEntity(
            date.value?.toString() ?:throw IllegalStateException("日付なし(null)"),
            LocalDateTime.now().toString(),
            weather1.value.toNumber(),
            weather2.value.toNumber(),
            condition.value.toNumber(),
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
            picturePath.value?.toString() ?:"",
            )
    }

    fun createDiaryItemTitleSelectionHistoryItemEntityList(): List<DiaryItemTitleSelectionHistoryItemEntity> {
        val list: MutableList<DiaryItemTitleSelectionHistoryItemEntity> = ArrayList()
        for (i in 0 until MAX_ITEMS) {
            val itemTitle = items[i].title.value
            val itemTitleUpdateLog = items[i].titleUpdateLog.value ?: continue
            if (itemTitle.matches("\\S+.*".toRegex())) {
                val item =
                    DiaryItemTitleSelectionHistoryItemEntity(
                        itemTitle,
                        itemTitleUpdateLog.toString()
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
                val nextItemNumber = ItemNumber(i + 1)
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

    inner class DiaryItemStateFlow(val itemNumber: Int) {

        private val minItemNumber = ItemNumber.MIN_NUMBER
        private val maxItemNumber = ItemNumber.MAX_NUMBER

        // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
        //      StateFlow変数を用意しても意味がないので作成しない。
        private val initialTitle = ""
        val title = MutableStateFlow(initialTitle)

        private val initialComment = ""
        val comment = MutableStateFlow(initialComment)

        // MEMO:初期化時日付有無が未定、タイトル未更新のケースがある為、null許容型とする。
        private val initialUpdateLog = null
        val titleUpdateLog = MutableStateFlow<LocalDateTime?>(initialUpdateLog)

        init {
            require(isItemNumberInRange(itemNumber))

            initialize()
        }

        private fun isItemNumberInRange(itemNumber: Int): Boolean {
            return itemNumber in minItemNumber..maxItemNumber
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
