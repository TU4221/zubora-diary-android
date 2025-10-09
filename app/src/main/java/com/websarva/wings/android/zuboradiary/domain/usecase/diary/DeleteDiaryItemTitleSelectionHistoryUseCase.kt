package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryDeleteException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 日記項目のタイトル選択履歴から特定の履歴を削除するユースケース。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class DeleteDiaryItemTitleSelectionHistoryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記項目タイトル選択履歴アイテム削除_"

    /**
     * ユースケースを実行し、指定されたタイトルを選択履歴から削除する。
     *
     * @param id 削除するタイトルのID。
     * @param title 削除するタイトル文字列。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryItemTitleSelectionHistoryDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        id: DiaryItemTitleSelectionHistoryId,
        title: DiaryItemTitle
    ): UseCaseResult<Unit, DiaryItemTitleSelectionHistoryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (ID: $id、タイトル: $title)")

        return try {
            diaryRepository.deleteDiaryItemTitleSelectionHistory(id)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryItemTitleSelectionHistoryDeleteException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_削除エラー", e)
            UseCaseResult.Failure(
                DiaryItemTitleSelectionHistoryDeleteException.DeleteFailure(title, e)
            )
        }
    }
}
