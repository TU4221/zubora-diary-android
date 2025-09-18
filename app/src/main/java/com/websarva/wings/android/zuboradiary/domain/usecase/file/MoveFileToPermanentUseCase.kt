package com.websarva.wings.android.zuboradiary.domain.usecase.file

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.FileMoveException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * ファイルを永続的ディレクトリへ移動するユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class MoveFileToPermanentUseCase(
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "永続ディレクトリへのファイル移動_"

    /**
     * ユースケースを実行し、指定されたファイルパスを永続ディレクトリへ移動する。
     *
     * @param filePath 移動するファイルパス。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [FileMoveException] を格納して返す。
     */
    suspend operator fun invoke(
        filePath: ImageFileName
    ): UseCaseResult<Unit, FileMoveException> {
        Log.i(logTag, "${logMsg}開始 (ファイルパス: $filePath)")

        return try {
            fileRepository.moveImageFileToPermanent(filePath)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_移動処理エラー", e)
            return UseCaseResult.Failure(
                FileMoveException.MoveFailure(e)
            )
        }
    }
}
