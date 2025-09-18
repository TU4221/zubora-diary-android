package com.websarva.wings.android.zuboradiary.domain.usecase.file

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 画像ファイルをキャッシュストレージへキャッシュするユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class CacheDiaryImageUseCase(
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記用画像キャッシュ_"

    /**
     * 指定された画像URIをもとにファイルを作成し、キャッシュストレージへキャッシュする。キャッシュされたファイルのパスを返す。
     *
     * @param uriString キャッシュされるファイルのもととなる画像URI文字列。
     * @param fileBaseName キャッシュされるファイルのベース名。
     * @return 処理に成功した場合は [UseCaseResult.Success] に キャッシュされたファイルのパス ( [ImageFileName] ) を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageCacheException] を格納して返す。
     */
    suspend operator fun invoke(
        uriString: String,
        fileBaseName: String
    ): UseCaseResult<ImageFileName, DiaryImageCacheException> {
        Log.i(logTag, "${logMsg}開始 (画像URI: $uriString、 ファイルベース名: $fileBaseName)")

        return try {
            val fileName = fileRepository.cacheImageFile(uriString, fileBaseName)
            Log.i(logTag, "${logMsg}完了 (ファイル名: $fileName)")
            UseCaseResult.Success(fileName)
        } catch (e: RepositoryException) {
            Log.e(logTag, "${logMsg}失敗_キャッシュ処理エラー", e)
            return UseCaseResult.Failure(
                DiaryImageCacheException.CacheFailure(e)
            )
        }
    }
}
