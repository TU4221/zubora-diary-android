package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryItemDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 日記項目のタイトル選択履歴から特定の履歴を削除するユースケース。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class DeleteDiaryItemTitleSelectionHistoryItemUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記項目タイトル選択履歴アイテム削除_"

    /**
     * ユースケースを実行し、指定されたタイトルを選択履歴から削除する。
     *
     * @param deleteTitle 削除するタイトル文字列。
     * @return 削除処理の成功を [UseCaseResult.Success] に `Unit` を格納して返す。
     *   削除処理に失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        deleteTitle: String
    ): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始 (削除タイトル: \"$deleteTitle\")")

        return try {
            diaryRepository.deleteDiaryItemTitleSelectionHistory(deleteTitle)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DiaryItemTitleSelectionHistoryItemDeleteFailureException) {
            Log.e(logTag, "${logMsg}失敗_削除処理エラー", e)
            UseCaseResult.Failure(e)
        }
    }
}
