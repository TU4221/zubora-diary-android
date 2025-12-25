package com.websarva.wings.android.zuboradiary.domain.usecase.diary.image

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import javax.inject.Inject

/**
 * 日記画像ファイルをキャッシュストレージへキャッシュするユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class CacheDiaryImageUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {

    private val logMsg = "日記用画像キャッシュ_"

    /**
     * 指定された画像URIをもとにファイルを作成し、キャッシュストレージへキャッシュする。キャッシュされたファイルのパスを返す。
     *
     * ファイル名はUUID形式 (UUID.拡張子) となる。
     *
     * @param uriString キャッシュされるファイルのもととなる画像URI文字列。
     * @return 処理に成功した場合は [UseCaseResult.Success] に キャッシュされたファイルのパス ( [DiaryImageFileName] ) を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageCacheException] を格納して返す。
     */
    suspend operator fun invoke(
        uriString: String
    ): UseCaseResult<DiaryImageFileName, DiaryImageCacheException> {
        Log.i(logTag, "${logMsg}開始 (画像URI: $uriString)")

        return try {
            val fileBaseName = DiaryImageFileName.generateRandomBaseName()
            val fileName = fileRepository.cacheImageFile(uriString, fileBaseName)
            Log.i(logTag, "${logMsg}完了 (ファイル名: $fileName)")
            UseCaseResult.Success(fileName)
        } catch (e: InsufficientStorageException) {
            Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
            UseCaseResult.Failure(
                DiaryImageCacheException.InsufficientStorage(e)
            )
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryImageCacheException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_キャッシュエラー", e)
            UseCaseResult.Failure(
                DiaryImageCacheException.CacheFailure(e)
            )
        }
    }
}
