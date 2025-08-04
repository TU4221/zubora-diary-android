package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryItemDeletionFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DeleteDiaryItemTitleSelectionHistoryItemUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    // MEMO:日記表示、編集フラグメント以外からも削除できるように下記引数とする。
    suspend operator fun invoke(
        deleteTitle: String
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "日記項目タイトル選択履歴アイテム削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteDiaryItemTitleSelectionHistoryItem(deleteTitle)
        } catch (e: DiaryItemTitleSelectionHistoryItemDeletionFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
