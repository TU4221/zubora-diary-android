package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.AllDiariesDeleteException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 全ての日記データ (項目タイトル選択履歴除く) と、それに関連する永続的なURI権限を削除するユースケース。
 *
 * 具体的には以下の処理を実行する。
 * 1. 全ての日記データ (項目タイトル選択履歴除く) を削除する。
 * 2. 全ての永続的なURI権限を解放する。
 *
 * @property diaryRepository 日記関連の操作を行うリポジトリ。
 * @property fileRepository ファイル関連へのアクセスを提供するリポジトリ。
 */
internal class DeleteAllDiariesUseCase(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "全日記データ削除_"

    /**
     * ユースケースを実行し、全ての日記データ (項目タイトル選択履歴除く) と関連画像ファイルを削除する。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [AllDiariesDeleteException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, AllDiariesDeleteException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                AllDiariesDeleteException.DeleteFailure(e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                AllDiariesDeleteException.Unknown(e)
            )
        }

        return try {
            fileRepository.clearAllImageFiles()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_画像ファイル削除エラー", e)
            UseCaseResult.Failure(
                AllDiariesDeleteException.ImageFileDeleteFailure(e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                AllDiariesDeleteException.Unknown(e)
            )
        }
    }
}
