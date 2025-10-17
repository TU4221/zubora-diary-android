package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import com.websarva.wings.android.zuboradiary.domain.model.diary.Condition
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime

internal open class DiaryStateFlow {

    companion object {
        const val MAX_ITEMS: Int = DiaryItemNumber.MAX_NUMBER
    }

    protected val initialDiary = Diary.generate()

    protected val initialId = null
    open val id = MutableStateFlow<DiaryId?>(initialId) // MEMO:初期化時IDが未定の為、null許容型とする。

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

    protected val initialTitle = initialDiary.title.value
    open val title = MutableStateFlow(initialTitle)

    private val initialNumVisibleItems = run {
        listOf(
            initialDiary.item1Title,
            initialDiary.item2Title,
            initialDiary.item3Title,
            initialDiary.item4Title,
            initialDiary.item5Title
        ).indexOfLast { it != null }.let { if (it == -1) 0 else it + 1 }
    }
    open val numVisibleItems = MutableStateFlow(initialNumVisibleItems)

    protected open val items: Array<out DiaryItemStateFlow> =
        Array(MAX_ITEMS) { i ->
            DiaryItemStateFlow(DiaryItemNumber(i + 1))
        }

    protected val initialImageFileName = initialDiary.imageFileName
    open val imageFileName = MutableStateFlow(initialImageFileName)

    private val initialImageFilePath: FilePathUi? = null
    val imageFilePath = MutableStateFlow(initialImageFilePath)

    protected val initialLog = null // MEMO:Logは保存記録の意味合となるため日記新規作成時を考慮してnullとする。
    open val log =
        MutableStateFlow<LocalDateTime?>(initialLog) // MEMO:初期化時日付有無が未定の為、null許容型とする。

    fun initialize() {
        updateId(initialId)
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

    fun getItemStateFlow(itemNumber: DiaryItemNumber): DiaryItemStateFlow {
        val arrayNumber = itemNumber.value - DiaryItemNumber.MIN_NUMBER
        return items[arrayNumber]
    }

    fun update(diary: Diary) {
        diary.run {
            updateId(id)
            updateDate(date)
            updateWeather1(weather1)
            updateWeather2(weather2)
            updateCondition(condition)
            updateTitle(title.value)

            setUpItemTitleAndComment(
                DiaryItemNumber(1),
                item1Title.value,
                item1Comment.value
            )
            setUpItemTitleAndComment(
                DiaryItemNumber(2),
                item2Title?.value,
                item2Comment?.value
            )
            setUpItemTitleAndComment(
                DiaryItemNumber(3),
                item3Title?.value,
                item3Comment?.value
            )
            setUpItemTitleAndComment(
                DiaryItemNumber(4),
                item4Title?.value,
                item4Comment?.value
            )
            setUpItemTitleAndComment(
                DiaryItemNumber(5),
                item5Title?.value,
                item5Comment?.value
            )

            val numEmptyItemsAtEnd = items.takeLastWhile { it.isEmpty }.count()
            val numVisibleItems = items.size - numEmptyItemsAtEnd
            updateNumVisibleItems(numVisibleItems)

            updateImageFileName(imageFileName)
            updateLog(log)
        }
    }

    private fun updateId(id: DiaryId?) {
        this.id.value = id
    }

    private fun updateDate(date: LocalDate?) {
        this.date.value = date
    }

    private fun updateWeather1(weather: Weather) {
        this.weather1.value = weather
    }

    private fun updateWeather2(weather: Weather) {
        this.weather2.value = weather
    }

    private fun updateCondition(condition: Condition) {
        this.condition.value = condition
    }

    private fun updateTitle(title: String) {
        this.title.value = title
    }

    protected fun initializeItemForEdit(itemNumber: DiaryItemNumber) {
        getItemStateFlow(itemNumber).updateTitleAndCommentFromDiary("", "")
    }

    protected fun setUpItemTitleAndComment(itemNumber: DiaryItemNumber, title: String?, comment: String?) {
        getItemStateFlow(itemNumber).updateTitleAndCommentFromDiary(title, comment)
    }

    protected fun updateItem(
        itemNumber: DiaryItemNumber,
        id: DiaryItemTitleSelectionHistoryId?,
        title: String?,
        comment: String?,
        titleUpdateLog: LocalDateTime?
    ) {
        getItemStateFlow(itemNumber).updateAllFromOtherItem(id, title, comment, titleUpdateLog)
    }

    protected fun updateItemTitleWithTimestamp(
        itemNumber: DiaryItemNumber,
        id: DiaryItemTitleSelectionHistoryId,
        title: String
    ) {
        getItemStateFlow(itemNumber).updateTitleWithTimestamp(id, title)
    }

    protected fun updateNumVisibleItems(count: Int) {
        this.numVisibleItems.value = count
    }

    private fun updateImageFileName(imageFileName: DiaryImageFileName?) {
        this.imageFileName.value = imageFileName
    }

    private fun updateLog(log: LocalDateTime?) {
        this.log.value = log
    }

    open class DiaryItemStateFlow(val itemNumber: DiaryItemNumber) {

        private val initialDiary = Diary.generate()

        protected val initialTitleId = null
        open val titleId = MutableStateFlow<DiaryItemTitleSelectionHistoryId?>(initialTitleId)

        // MEMO:双方向DataBindingが必要の為、MutableStateFlow変数はアクセス修飾子をpublicとする。
        //      StateFlow変数を用意しても意味がないので作成しない。
        protected val initialTitle = when (itemNumber.value) {
            1 -> initialDiary.item1Title.value
            2 -> initialDiary.item2Title?.value
            3 -> initialDiary.item3Title?.value
            4 -> initialDiary.item4Title?.value
            5 -> initialDiary.item5Title?.value
            else -> throw IllegalStateException()
        }
        open val title = MutableStateFlow(initialTitle)

        protected val initialTitleUpdateLog = null
        open val titleUpdateLog = MutableStateFlow<LocalDateTime?>(initialTitleUpdateLog)

        protected val initialComment = when (itemNumber.value) {
            1 -> initialDiary.item1Comment.value
            2 -> initialDiary.item2Comment?.value
            3 -> initialDiary.item3Comment?.value
            4 -> initialDiary.item4Comment?.value
            5 -> initialDiary.item5Comment?.value
            else -> throw IllegalStateException()
        }
        open val comment = MutableStateFlow(initialComment)

        val isEmpty: Boolean
            get() {
                val title = this.title.value ?: return true
                val comment = this.comment.value ?: return true
                return title.isEmpty() && comment.isEmpty()
            }

        fun initialize() {
            updateTitleId(initialTitleId)
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

        fun updateTitleAndCommentFromDiary(
            title: String?,
            comment: String?,
        ) {
            updateTitle(title)
            updateComment(comment)
        }

        fun updateAllFromOtherItem(
            id: DiaryItemTitleSelectionHistoryId?,
            title: String?,
            comment: String?,
            titleUpdateLog: LocalDateTime?
        ) {
            updateTitleId(id)
            updateTitle(title)
            updateComment(comment)
            updateTitleUpdateLog(titleUpdateLog)
        }

        fun updateTitleWithTimestamp(id: DiaryItemTitleSelectionHistoryId, title: String) {
            updateTitleId(id)
            updateTitle(title)
            updateTitleUpdateLog(LocalDateTime.now())
        }

        private fun updateTitleId(id: DiaryItemTitleSelectionHistoryId?) {
            this.titleId.value = id
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
