package com.websarva.wings.android.zuboradiary.ui.diary

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.ui.orEmptyString
import java.time.LocalDate
import java.time.LocalDateTime

class DiaryLiveData {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER
    }

    // MEMO:双方向DataBindingが必要の為、MutableLiveData変数はアクセス修飾子をpublicとする。
    //      LiveData変数を用意しても意味がないので作成しない。
    val date: MutableLiveData<LocalDate?> = MutableLiveData() // MEMO:初期化時日付が未定の為、null許容型とする。
    val weather1: MutableLiveData<Weather> = MutableLiveData()
    val weather2: MutableLiveData<Weather> = MutableLiveData()
    val condition: MutableLiveData<Condition> = MutableLiveData()
    val title: MutableLiveData<String> = MutableLiveData()
    val numVisibleItems: MutableLiveData<Int> = MutableLiveData()
    private val items = Array(MAX_ITEMS) { i -> DiaryItemLiveData(i + 1)}
    val picturePath: MutableLiveData<Uri?> = MutableLiveData() // MEMO:初期化時Uri有無が未定の為、null許容型とする。
    val log: MutableLiveData<LocalDateTime?> = MutableLiveData() // MEMO:初期化時日付有無が未定の為、null許容型とする。

    init {
        initialize()
    }

    fun initialize() {
        date.value = null
        weather1.value = Weather.UNKNOWN
        weather2.value = Weather.UNKNOWN
        condition.value = Condition.UNKNOWN
        title.value = ""
        numVisibleItems.value = 1
        for (item in items) {
            item.initialize()
        }
        picturePath.value = null
        log.value = null
    }

    fun update(diaryEntity: DiaryEntity) {

        // TODO:DiaryEntityのプロパティをすべて非null型に変更してから下記"!!"削除
        date.value = LocalDate.parse(diaryEntity.date)
        val intWeather1 = diaryEntity.weather1
        weather1.value = Weather.of(intWeather1!!)
        val intWeather2 = diaryEntity.weather2
        weather2.value = Weather.of(intWeather2!!)
        val intCondition = diaryEntity.condition
        condition.value = Condition.of(intCondition!!)
        val title = diaryEntity.title
        this.title.value = title

        val nullDateTime: LocalDateTime? = null
        val item1Title = diaryEntity.item1Title
        val item1Comment = diaryEntity.item1Comment
        items[0].update(item1Title!!, item1Comment!!, nullDateTime)

        val item2Title = diaryEntity.item2Title
        val item2Comment = diaryEntity.item2Comment
        items[1].update(item2Title!!, item2Comment!!, nullDateTime)

        val item3Title = diaryEntity.item3Title
        val item3Comment = diaryEntity.item3Comment
        items[2].update(item3Title!!, item3Comment!!, nullDateTime)

        val item4Title = diaryEntity.item4Title
        val item4Comment = diaryEntity.item4Comment
        items[3].update(item4Title!!, item4Comment!!, nullDateTime)

        val item5Title = diaryEntity.item5Title
        val item5Comment = diaryEntity.item5Comment
        items[4].update(item5Title!!, item5Comment!!, nullDateTime)

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
        if (uriString!!.isEmpty()) {
            picturePath.setValue(null)
        } else {
            picturePath.setValue(Uri.parse(uriString))
        }

        log.value = LocalDateTime.parse(diaryEntity.log)
    }

    fun createDiaryEntity(): DiaryEntity {
        val diaryEntity = DiaryEntity()
        diaryEntity.date = date.value?.toString() ?:throw IllegalStateException("日付なし(null)")
        diaryEntity.weather1 = weather1.value?.toNumber() ?:Weather.UNKNOWN.toNumber()
        diaryEntity.weather2 = weather2.value?.toNumber() ?:Weather.UNKNOWN.toNumber()
        diaryEntity.condition = condition.value?.toNumber() ?:Condition.UNKNOWN.toNumber()
        diaryEntity.title = title.orEmptyString().trim()
        diaryEntity.item1Title = items[0].title.orEmptyString().trim()
        diaryEntity.item1Comment = items[0].comment.orEmptyString().trim()
        diaryEntity.item2Title = items[1].title.orEmptyString().trim()
        diaryEntity.item2Comment = items[1].comment.orEmptyString().trim()
        diaryEntity.item3Title = items[2].title.orEmptyString().trim()
        diaryEntity.item3Comment = items[2].comment.orEmptyString().trim()
        diaryEntity.item4Title = items[3].title.orEmptyString().trim()
        diaryEntity.item4Comment = items[3].comment.orEmptyString().trim()
        diaryEntity.item5Title = items[4].title.orEmptyString().trim()
        diaryEntity.item5Comment = items[4].comment.orEmptyString().trim()
        diaryEntity.picturePath = picturePath.value?.toString() ?:""
        diaryEntity.log = LocalDateTime.now().toString()
        return diaryEntity
    }

    fun createDiaryItemTitleSelectionHistoryItemEntityList(): List<DiaryItemTitleSelectionHistoryItemEntity> {
        val list: MutableList<DiaryItemTitleSelectionHistoryItemEntity> = ArrayList()
        for (i in 0 until MAX_ITEMS) {
            val itemTitle = items[i].title.orEmptyString()
            val itemTitleUpdateLog = items[i].titleUpdateLog.value ?: continue
            if (itemTitle.matches("\\S+.*".toRegex())) {
                val item = DiaryItemTitleSelectionHistoryItemEntity()
                item.title = itemTitle
                item.log = itemTitleUpdateLog.toString()
                list.add(item)
            }
        }
        return list
    }

    fun incrementVisibleItemsCount() {
        val numVisibleItems = checkNotNull(numVisibleItems.value)
        val incrementedNumVisibleItems = numVisibleItems + 1
        this.numVisibleItems.value = incrementedNumVisibleItems
    }

    fun deleteItem(itemNumber: ItemNumber) {
        getItemLiveData(itemNumber).initialize()
        val numVisibleItems = checkNotNull(numVisibleItems.value)

        if (itemNumber.value < numVisibleItems) {
            for (i in itemNumber.value until numVisibleItems) {
                val targetItemNumber = ItemNumber(i)
                val nextItemNumber = ItemNumber(i + 1)
                getItemLiveData(targetItemNumber).update(
                    getItemLiveData(nextItemNumber).title.orEmptyString(),
                    getItemLiveData(nextItemNumber).comment.orEmptyString(),
                    getItemLiveData(nextItemNumber).titleUpdateLog.value
                )
                getItemLiveData(nextItemNumber).initialize()
            }
        }

        if (numVisibleItems > ItemNumber.MIN_NUMBER) {
            val decrementedNumVisibleItems = numVisibleItems - 1
            this.numVisibleItems.value = decrementedNumVisibleItems
        }
    }

    fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        getItemLiveData(itemNumber).updateItemTitle(title)
    }

    fun getItemLiveData(itemNumber: ItemNumber): DiaryItemLiveData {
        val arrayNumber = itemNumber.value - 1
        return items[arrayNumber]
    }

    class DiaryItemLiveData(val itemNumber: Int) {

        companion object {
            const val MIN_ITEM_NUMBER: Int = ItemNumber.MIN_NUMBER
            const val MAX_ITEM_NUMBER: Int = ItemNumber.MAX_NUMBER
        }

        // MEMO:双方向DataBindingが必要の為、MutableLiveData変数はアクセス修飾子をpublicとする。
        //      LiveData変数を用意しても意味がないので作成しない。
        val title: MutableLiveData<String> = MutableLiveData()
        val comment: MutableLiveData<String> = MutableLiveData()

        // MEMO:初期化時日付有無が未定、タイトル未更新のケースがある為、null許容型とする。
        internal val titleUpdateLog: MutableLiveData<LocalDateTime?> = MutableLiveData()

        init {
            require(isItemNumberInRange(itemNumber))

            initialize()
        }

        private fun isItemNumberInRange(itemNumber: Int): Boolean {
            return itemNumber in MIN_ITEM_NUMBER..MAX_ITEM_NUMBER
        }

        fun initialize() {
            title.value = ""
            comment.value = ""
            titleUpdateLog.value = null
        }

        fun update(title: String, comment: String, titleUpdateLog: LocalDateTime?) {
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
                val title = title.orEmptyString()
                val comment = comment.orEmptyString()
                return title.isEmpty() && comment.isEmpty()
            }
    }
}
