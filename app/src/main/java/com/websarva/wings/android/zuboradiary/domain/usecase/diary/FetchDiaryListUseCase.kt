package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListFetchFailureException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class FetchDiaryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        numLoadingItems: Int,
        loadingOffset: Int,
        startDate: LocalDate?
    ): DefaultUseCaseResult<List<DiaryListItem>> {
        val logMsg = "日記リスト読込_"
        Log.i(logTag, "${logMsg}開始")

        require(loadingOffset >= 0)

        try {
            val loadedDiaryList =
                diaryRepository.fetchDiaryList(
                    numLoadingItems,
                    loadingOffset,
                    startDate
                )
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(loadedDiaryList)
        } catch (e: DiaryListFetchFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
