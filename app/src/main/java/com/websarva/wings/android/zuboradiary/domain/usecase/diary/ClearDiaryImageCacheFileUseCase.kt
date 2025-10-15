package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheFileClearException
import com.websarva.wings.android.zuboradiary.utils.logTag

/**
 * キャッシュストレージの日記画像ファイルをクリアするユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class ClearDiaryImageCacheFileUseCase(
    private val fileRepository: FileRepository
) {

    private val logMsg = "キャッシュファイルクリア_"

    /**
     * ユースケースを実行し、キャッシュストレージのファイルをクリアする。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageCacheFileClearException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, DiaryImageCacheFileClearException> {
        Log.i(logTag, "${logMsg}開始")

        return try {
            fileRepository.clearAllImageFilesInCache()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: ResourceNotFoundException) {
            Log.e(logTag, "${logMsg}失敗_対象ファイルなしのため、成功とみなす", e)
            UseCaseResult.Success(Unit)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryImageCacheFileClearException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_クリアエラー", e)
            UseCaseResult.Failure(
                DiaryImageCacheFileClearException.ClearFailure(e)
            )
        }
    }
}
