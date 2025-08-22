package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlin.jvm.Throws

internal class LoadNewWordSearchResultListUseCase(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        numLoadItems: Int,
        searchWord: String
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>> {
        val logMsg = "新規ワード検索結果リスト読込_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val loadedDiaryList =
                loadDiaryList(
                    numLoadItems,
                    0,
                    searchWord
                )
            val resultList = updateDiaryListFooter(loadedDiaryList, searchWord)

            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(resultList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    @Throws(DomainException::class)
    private suspend fun loadDiaryList(
        numLoadItems: Int,
        loadOffset: Int,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        val result =
            loadWordSearchResultListUseCase(
                numLoadItems,
                loadOffset,
                searchWord
            )
        return when (result) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    @Throws(DomainException::class)
    private suspend fun updateDiaryListFooter(
        list: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        return when (val result = updateWordSearchResultListFooterUseCase(list, searchWord)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
