package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate
import kotlin.jvm.Throws

internal class RefreshDiaryListUseCase(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>> {
        val logMsg = "日記リスト再読込_"
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
                    0,
                    startDate
                )
            val resultList = updateDiaryListFooter(loadedDiaryList, startDate)

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
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                numLoadItems,
                loadOffset,
                startDate
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
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        return when (val result = updateDiaryListFooterUseCase(list, startDate)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
