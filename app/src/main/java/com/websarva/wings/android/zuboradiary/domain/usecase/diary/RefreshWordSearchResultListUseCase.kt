package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlin.jvm.Throws

internal class RefreshWordSearchResultListUseCase(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>> {
        val logMsg = "ワード検索結果リスト再読込_"
        Log.i(logTag, "${logMsg}開始")

        var numLoadItems = currentList.countDiaries()
        // HACK:リストが空の状態、又は画面サイズより少ないアイテム数で日記を追加し、
        //      リスト画面に戻った際に以下の問題が発生する回避策。
        //      問題点:
        //      1. 新しく追加された日記が表示されず、追加前のアイテム数でリストが描画される。
        //      2. スクロールによる追加読み込みも機能しない。
        //      対策:
        //      NUM_LOAD_ITEMS に満たない場合は、強制的に NUM_LOAD_ITEMS 分の読み込みを行うことで、
        //      新規追加されたアイテムの表示とスクロール更新を可能にする。
        if (numLoadItems < NUM_LOAD_ITEMS) {
            numLoadItems = NUM_LOAD_ITEMS
        }
        try {
            val loadedDiaryList =
                loadDiaryList(
                    numLoadItems,
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
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        val result =
            loadWordSearchResultListUseCase(
                numLoadItems,
                0,
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
