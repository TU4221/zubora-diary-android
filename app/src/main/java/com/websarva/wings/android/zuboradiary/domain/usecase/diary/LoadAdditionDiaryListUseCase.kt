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

internal class LoadAdditionDiaryListUseCase(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>> {
        val logMsg = "追加日記リスト読込_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val loadedDiaryList =
                loadDiaryList(
                    currentList.countDiaries(),
                    startDate
                )
            val combinedList = currentList.combineDiaryLists(loadedDiaryList)
            val resultList = updateDiaryListFooter(combinedList, startDate)

            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(resultList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    @Throws(DomainException::class)
    private suspend fun loadDiaryList(
        loadOffset: Int,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                NUM_LOAD_ITEMS,
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
