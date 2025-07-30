package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime

internal open class DiaryStateFlow {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER
    }

    private val initialDiary = Diary()

    // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
    //      StateFlow変数を用意しても意味がないので作成しない。
    protected val initialDate = null // MEMO:日付選択機能(前回選択日付機能)関係でinitialDiaryの日付はnullとする。
    open val date =
        MutableStateFlow<LocalDate?>(initialDate) // MEMO:初期化時日付が未定の為、null許容型とする。

    protected val initialWeather = initialDiary.weather1
    open val weather1 = MutableStateFlow(initialWeather)
    open val weather2 = MutableStateFlow(initialWeather)

    protected val initialCondition = initialDiary.condition
    open val condition = MutableStateFlow(initialCondition)

    protected val initialTitle = initialDiary.title
    open val title = MutableStateFlow(initialTitle)

    private val initialNumVisibleItems = run {
        var count = 1
        if (initialDiary.item2Title != null) count++
        if (initialDiary.item3Title != null) count++
        if (initialDiary.item4Title != null) count++
        if (initialDiary.item5Title != null) count++
        count
    }
    open val numVisibleItems = MutableStateFlow(initialNumVisibleItems)

    protected open val items: Array<out DiaryItemStateFlow> =
        Array(MAX_ITEMS) { i -> DiaryItemStateFlow(i + 1)}

    protected val initialImageUri = initialDiary.imageUriString?.let { Uri.parse(it) }
    open val imageUri = MutableStateFlow(initialImageUri)

    protected val initialLog = null // MEMO:Logは保存記録の意味合となるため日記新規作成時を考慮してnullとする。
    open val log =
        MutableStateFlow<LocalDateTime?>(initialLog) // MEMO:初期化時日付有無が未定の為、null許容型とする。

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

    fun getItemStateFlow(itemNumber: ItemNumber): DiaryItemStateFlow {
        val arrayNumber = itemNumber.value - 1
        return items[arrayNumber]
    }

    open class DiaryItemStateFlow(val itemNumber: Int) {

        companion object {
            private const val MIN_ITEM_NUMBER = ItemNumber.MIN_NUMBER
            private const val MAX_ITEM_NUMBER = ItemNumber.MAX_NUMBER
        }

        // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
        //      StateFlow変数を用意しても意味がないので作成しない。
        protected val initialTitle = if (itemNumber == 1) "" else null
        open val title = MutableStateFlow(initialTitle)

        protected val initialComment = if (itemNumber == 1) "" else null
        open val comment = MutableStateFlow(initialComment)

        // MEMO:初期化時日付有無が未定、タイトル未更新のケースがある為、null許容型とする。
        protected val initialUpdateLog = null
        open val titleUpdateLog = MutableStateFlow<LocalDateTime?>(initialUpdateLog)

        init {
            require(isItemNumberInRange(itemNumber))
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
            title: String?,
            comment: String?,
            titleUpdateLog: LocalDateTime? = initialUpdateLog
        ) {
            require(
                (title == null && comment == null && titleUpdateLog == null)
                        || (title != null && comment != null)
            )

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
                val title = this.title.value ?: return true
                val comment = this.comment.value ?: return true
                return title.isEmpty() && comment.isEmpty()
            }
    }
}
