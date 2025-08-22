package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.mapper.toDiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class LoadDiaryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        numLoadItems: Int,
        loadOffset: Int,
        startDate: LocalDate?
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>> {
        val logMsg = "日記リスト読込_"
        Log.i(logTag, "${logMsg}開始")

        require(loadOffset >= 0)

        try {
            val loadedDiaryList =
                diaryRepository.loadDiaryList(
                    numLoadItems,
                    loadOffset,
                    startDate
                )
            val convertedList = convertDiaryYearMonthList(loadedDiaryList)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(convertedList)
        } catch (e: DiaryListLoadFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    private fun convertDiaryYearMonthList(
        diaryList: List<DiaryDayListItem.Standard>
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        if (diaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayList = DiaryDayList(diaryList)
        return diaryDayList.toDiaryYearMonthList()
    }
}
