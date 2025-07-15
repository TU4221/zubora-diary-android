package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryItemTitleSelectionHistoryFailedException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow

internal class FetchDiaryItemTitleSelectionHistoryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    /**
     * @return [UseCaseResult.Success]
     * この内部の [Flow] は、実行中に [FetchDiaryItemTitleSelectionHistoryFailedException] を
     * スローする可能性があります。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<List<DiaryItemTitleSelectionHistoryItem>>> {
        val logMsg = "日記タイトル選択履歴読込_"
        Log.i(logTag, "${logMsg}開始")

        val flow = diaryRepository.fetchDiaryItemTitleSelectionHistory(50, 0)
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(flow)
    }
}
