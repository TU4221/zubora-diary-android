package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.mapper.toDiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class LoadWordSearchResultListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    private val itemNumberKey = "ItemNumber"
    private val itemTitleKey = "ItemTitle"
    private val itemCommentKey = "ItemComment"

    suspend operator fun invoke(
        numLoadItems: Int,
        loadOffset: Int,
        searchWord: String
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>> {
        val logMsg = "ワード検索結果リスト読込_"
        Log.i(logTag, "${logMsg}開始")
        require(numLoadItems >= 1)
        require(loadOffset >= 0)
        require(searchWord.isNotEmpty())

        try {
            val wordSearchResultList =
                diaryRepository.loadWordSearchResultList(
                    numLoadItems,
                    loadOffset,
                    searchWord
                )
            val convertedList = convertWordSearchResultList(wordSearchResultList, searchWord)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(convertedList)
        } catch (e: DiaryListLoadFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    // MEMO:本来はDataSource側で処理するべき内容だが、対象日記項目のみを抽出するには複雑なロジックになる為、
    //      ドメイン側で処理する。
    private fun convertWordSearchResultList(
        diaryList: List<WordSearchResultListItem>,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        if (diaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayList =
            DiaryDayList(
                diaryList.map { convertWordSearchResultListItem(it, searchWord) }
            )
        return diaryDayList.toDiaryYearMonthList()
    }

    private fun convertWordSearchResultListItem(
        item: WordSearchResultListItem,
        searchWord: String
    ): DiaryDayListItem.WordSearchResult {
        val diaryItem = extractWordSearchResultTargetItem(item, searchWord)
        val itemNumberInt = diaryItem[itemNumberKey] as Int
        return DiaryDayListItem.WordSearchResult(
            item.date,
            item.title,
            ItemNumber(itemNumberInt),
            diaryItem[itemTitleKey] as String,
            diaryItem[itemCommentKey] as String,
            searchWord
        )
    }

    private fun extractWordSearchResultTargetItem(
        item: WordSearchResultListItem,
        searchWord: String
    ): Map<String, Any> {
        val regex = ".*$searchWord.*"
        val itemTitles = arrayOf(
            item.item1Title,
            item.item2Title,
            item.item3Title,
            item.item4Title,
            item.item5Title,
        )
        val itemComments = arrayOf(
            item.item1Comment,
            item.item2Comment,
            item.item3Comment,
            item.item4Comment,
            item.item5Comment,
        )
        var itemNumber = 0
        var itemTitle = ""
        var itemComment = ""
        for (i in itemTitles.indices) {
            val targetItemTitle = itemTitles[i] ?: continue
            val targetItemComment = itemComments[i] ?: continue

            if (targetItemTitle.matches(regex.toRegex())
                || targetItemComment.matches(regex.toRegex())
            ) {
                itemNumber = i + 1
                itemTitle = targetItemTitle
                itemComment = targetItemComment
                break
            }
        }

        // 検索ワードが項目タイトル、コメントに含まれていない場合、アイテムNo.1を抽出
        if (itemNumber == 0) {
            itemNumber = 1
            itemTitle = itemTitles[0] ?: throw IllegalStateException()
            itemComment = itemComments[0] ?: throw IllegalStateException()
        }

        val result: MutableMap<String, Any> = HashMap()
        result[itemNumberKey] = itemNumber
        result[itemTitleKey] = itemTitle
        result[itemCommentKey] = itemComment
        return result
    }
}
