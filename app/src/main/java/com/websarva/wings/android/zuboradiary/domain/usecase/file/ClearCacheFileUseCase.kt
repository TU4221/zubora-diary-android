package com.websarva.wings.android.zuboradiary.domain.usecase.file

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.CacheFileClearException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * キャッシュストレージのファイルをクリアするユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class ClearCacheFileUseCase(
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "キャッシュファイルクリア_"

    /**
     * ユースケースを実行し、キャッシュストレージのファイルをクリアする。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [CacheFileClearException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, CacheFileClearException> {
        Log.i(logTag, "${logMsg}開始")

        return try {
            fileRepository.clearAllImageFilesInCache()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_クリア処理エラー", e)
            return UseCaseResult.Failure(
                CacheFileClearException.ClearFailure(e)
            )
        }
    }
}
