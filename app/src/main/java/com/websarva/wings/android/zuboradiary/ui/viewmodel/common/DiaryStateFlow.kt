package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import com.websarva.wings.android.zuboradiary.ui.model.ConditionUi
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime

internal open class DiaryStateFlow {

    companion object {
        const val MAX_ITEMS: Int = ItemNumber.MAX_NUMBER
    }

    protected val initialDiary = Diary()

    protected val initialId = initialDiary.id
    open val id = MutableStateFlow(initialId)

    // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
    //      StateFlow変数を用意しても意味がないので作成しない。
    protected val initialDate = null // MEMO:日付選択機能(前回選択日付機能)関係でinitialDiaryの日付はnullとする。
    open val date =
        MutableStateFlow<LocalDate?>(initialDate) // MEMO:初期化時日付が未定の為、null許容型とする。

    protected val initialWeather = initialDiary.weather1.toUiModel()
    open val weather1 = MutableStateFlow(initialWeather)
    open val weather2 = MutableStateFlow(initialWeather)

    protected val initialCondition = initialDiary.condition.toUiModel()
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
        Array(MAX_ITEMS) { i -> DiaryItemStateFlow(i + 1) }

    protected val initialImageFileName = initialDiary.imageFileName?.toUiModel()
    open val imageFileName = MutableStateFlow(initialImageFileName)

    private val initialImageFilePath: ImageFilePathUi? = null
    val imageFilePath = MutableStateFlow(initialImageFilePath)

    protected val initialLog = null // MEMO:Logは保存記録の意味合となるため日記新規作成時を考慮してnullとする。
    open val log =
        MutableStateFlow<LocalDateTime?>(initialLog) // MEMO:初期化時日付有無が未定の為、null許容型とする。

    fun initialize() {
        updateDate(initialDate)
        updateWeather1(initialWeather)
        updateWeather2(initialWeather)
        updateCondition(initialCondition)
        updateTitle(initialTitle)
        updateNumVisibleItems(initialNumVisibleItems)
        for (item in items) {
            item.initialize()
        }
        updateImageFileName(initialImageFileName)
        updateLog(initialLog)
    }

    fun getItemStateFlow(itemNumber: ItemNumber): DiaryItemStateFlow {
        val arrayNumber = itemNumber.value - ItemNumber.MIN_NUMBER
        return items[arrayNumber]
    }

    fun update(diary: Diary) {
        diary.run {
            updateId(id)
            updateDate(date)
            updateWeather1(weather1.toUiModel())
            updateWeather2(weather2.toUiModel())
            updateCondition(condition.toUiModel())
            updateTitle(title)

            updateItem(ItemNumber(1), item1Title, item1Comment)
            updateItem(ItemNumber(2), item2Title, item2Comment)
            updateItem(ItemNumber(3), item3Title, item3Comment)
            updateItem(ItemNumber(4), item4Title, item4Comment)
            updateItem(ItemNumber(5), item5Title, item5Comment)

            var numVisibleItems = items.size
            val maxArrayNumber = numVisibleItems - 1
            for (i in maxArrayNumber downTo 1) {
                if (items[i].isEmpty) {
                    numVisibleItems--
                } else {
                    break
                }
            }
            updateNumVisibleItems(numVisibleItems)

            updateImageFileName(imageFileName?.toUiModel())
            updateLog(log)
        }
    }

    private fun updateId(id: UUIDString) {
        this.id.value = id
    }

    private fun updateDate(date: LocalDate?) {
        this.date.value = date
    }

    private fun updateWeather1(weather: WeatherUi) {
        this.weather1.value = weather
    }

    private fun updateWeather2(weather: WeatherUi) {
        this.weather2.value = weather
    }

    private fun updateCondition(condition: ConditionUi) {
        this.condition.value = condition
    }

    private fun updateTitle(title: String) {
        this.title.value = title
    }

    protected fun updateItem(
        itemNumber: ItemNumber,
        title: String?,
        comment: String?,
        titleUpdateLog: LocalDateTime? = null
    ) {
        if (titleUpdateLog == null) {
            getItemStateFlow(itemNumber).update(title, comment)
        } else {
            getItemStateFlow(itemNumber).update(title, comment, titleUpdateLog)
        }
    }

    protected fun updateItemTitleWithTimestamp(
        itemNumber: ItemNumber,
        title: String
    ) {
        getItemStateFlow(itemNumber).updateTitleWithTimestamp(title)
    }

    protected fun updateNumVisibleItems(count: Int) {
        this.numVisibleItems.value = count
    }

    private fun updateImageFileName(imageFileName: ImageFileNameUi?) {
        this.imageFileName.value = imageFileName
    }

    private fun updateLog(log: LocalDateTime?) {
        this.log.value = log
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

        protected val initialTitleUpdateLog = null
        open val titleUpdateLog = MutableStateFlow<LocalDateTime?>(initialTitleUpdateLog)

        protected val initialComment = if (itemNumber == 1) "" else null
        open val comment = MutableStateFlow(initialComment)

        val isEmpty: Boolean
            get() {
                val title = this.title.value ?: return true
                val comment = this.comment.value ?: return true
                return title.isEmpty() && comment.isEmpty()
            }

        init {
            require(isItemNumberInRange(itemNumber))
        }

        private fun isItemNumberInRange(itemNumber: Int): Boolean {
            return itemNumber in MIN_ITEM_NUMBER..MAX_ITEM_NUMBER
        }

        fun initialize() {
            updateTitle(initialTitle)
            updateComment(initialComment)
            updateTitleUpdateLog(initialTitleUpdateLog)
        }

        fun update(
            title: String?,
            comment: String?,
            titleUpdateLog: LocalDateTime? = initialTitleUpdateLog
        ) {
            require(
                (title == null && comment == null && titleUpdateLog == null)
                        || (title != null && comment != null)
            )

            updateTitle(title)
            updateComment(comment)
            updateTitleUpdateLog(titleUpdateLog)
        }

        fun updateTitleWithTimestamp(title: String) {
            this.title.value = title
            this.titleUpdateLog.value = LocalDateTime.now()
        }

        private fun updateTitle(title: String?) {
            this.title.value = title
        }

        private fun updateComment(comment: String?) {
            this.comment.value = comment
        }

        private fun updateTitleUpdateLog(log: LocalDateTime?) {
            this.titleUpdateLog.value = log
        }
    }
}
