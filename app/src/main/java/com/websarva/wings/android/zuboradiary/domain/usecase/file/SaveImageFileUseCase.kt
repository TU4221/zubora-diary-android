package com.websarva.wings.android.zuboradiary.domain.usecase.file

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.ImageSize
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.ImageFileSaveException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * ファイルをキャッシュディレクトリへ保存するユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class SaveImageFileUseCase(
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "画像ファイル保存_"

    /**
     * 指定された画像URIをもとにファイルを作成し、キャッシュディレクトリへ保存。保存されたファイルのパスを返す。
     *
     * @param uriString 保存されるファイルのもととなる画像URI文字列。
     * @param fileBaseName 保存されるファイルのベース名。
     * @param size 保存される画像のサイズ。
     * @return 処理に成功した場合は [UseCaseResult.Success] に 保存されたファイルのパス( `String` )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [ImageFileSaveException] を格納して返す。
     */
    suspend operator fun invoke(
        uriString: String,
        fileBaseName: String,
        size: ImageSize
    ): UseCaseResult<String, ImageFileSaveException> {
        Log.i(logTag, "${logMsg}開始 (画像URI: $uriString、 ファイルベース名: $fileBaseName、 サイズ: $size)")

        return try {
            val fullSizeFilePath = fileRepository.saveImageFileToCache(uriString, fileBaseName, size)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(fullSizeFilePath)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_保存処理エラー", e)
            return UseCaseResult.Failure(
                ImageFileSaveException.SaveFailure(e)
            )
        }
    }
}
