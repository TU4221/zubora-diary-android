package com.websarva.wings.android.zuboradiary.domain.usecase.file

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.FileDeleteException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * ファイルを削除するユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class DeleteFileUseCase(
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "ファイル削除_"

    /**
     * ユースケースを実行し、指定されたファイルパスを削除する。
     *
     * @param filePath 削除するファイルパス。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [FileDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        filePath: String
    ): UseCaseResult<Unit, FileDeleteException> {
        Log.i(logTag, "${logMsg}開始 (ファイルパス: $filePath)")

        return try {
            fileRepository.deleteFile(filePath)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_削除処理エラー", e)
            return UseCaseResult.Failure(
                FileDeleteException.DeleteFailure(e)
            )
        }
    }
}
