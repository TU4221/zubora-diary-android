package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.DiaryImageFilePath
import com.websarva.wings.android.zuboradiary.domain.model.ImageSize
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.SaveImageFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageFileCreateException
import com.websarva.wings.android.zuboradiary.domain.usecase.file.MoveFileToPermanentUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.exception.ImageFileSaveException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.util.UUID

/**
 * 日記の添付画像ファイルを作成するユースケース。
 *
 * @property saveImageFileUseCase 画像ファイルをキャッシュディレクトリに保存するユースケース。
 */
internal class CreateDiaryImageFileUseCase(
    private val saveImageFileUseCase: SaveImageFileUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "日記画像ファイル作成_"

    /**
     * ユースケースを実行し、指定された画像URIをもとに日記で表示する添付画像(フルサイズ)と日記リストで表示するサムネ画像の
     * ファイルを作成し、作成されたファイルのパスを返す。
     *
     * 作成されたファイルはキャッシュディレクトリに保存される為、
     * 永続的にファイルを使用される場合は [MoveFileToPermanentUseCase] を使用して
     * 永続ディレクトリにファイルを移動させる必要がある。
     *
     * @param imageUriString 作成するファイルのもととなる日記の添付画像URI文字列。
     * @return 処理に成功した場合は [UseCaseResult.Success] に作成されたファイルのパス( [DiaryImageFilePath] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageFileCreateException] を格納して返す。
     */
    suspend operator fun invoke(
        imageUriString: String
    ): UseCaseResult<DiaryImageFilePath, DiaryImageFileCreateException> {
        Log.i(logTag, "${logMsg}開始 (画像URI: $imageUriString)")

        // TODO:本来はファイルネームをデータベースの主キー(UUID)にするべきだが、主キーを日付にしている為、仮でUUID。
        //      データベースの主キーをUUIDに変更してから下記をデータベースからのUUIDに変更する。
        val uuidString = UUID.randomUUID().toString()

        val fullSizeFilePath =
            try {
                val fullSizeFileBaseName = uuidString + DiaryImageFilePath.SUFFIX_FULL
                saveImageFilePath(imageUriString, fullSizeFileBaseName, ImageSize.Full)
            } catch (e: DataStorageException) {
                Log.e(logTag, "${logMsg}失敗_フルサイズ作成処理エラー", e)
                return UseCaseResult.Failure(
                    DiaryImageFileCreateException.CreateFailure(e)
                )
            }

        val thumbnailSizeFilePath =
            try {
                val thumbnailSizeFileBaseName = uuidString + DiaryImageFilePath.SUFFIX_THUMB
                saveImageFilePath(imageUriString, thumbnailSizeFileBaseName, ImageSize.Thumbnail)
            } catch (e: DataStorageException) {
                Log.e(logTag, "${logMsg}失敗_サムネイルサイズ作成処理エラー", e)
                return UseCaseResult.Failure(
                    DiaryImageFileCreateException.CreateFailure(e)
                )
            }

        val imageFilePath =
            DiaryImageFilePath(
                fullSizeFilePath,
                thumbnailSizeFilePath
            )

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(imageFilePath)
    }

    /**
     * 指定された画像URIをもとにファイルを作成し、キャッシュディレクトリへ保存。保存されたファイルのパスを返す。
     *
     * @param uriString 保存されるファイルのもととなる画像URI文字列。
     * @param fileBaseName 保存されるファイルのベース名。
     * @param size 保存される画像のサイズ。
     * @return 保存されたファイルのパス。
     * @throws ImageFileSaveException 画像ファイルの保存に失敗した場合。
     */
    private suspend fun saveImageFilePath(
        uriString: String,
        fileBaseName: String,
        size: ImageSize
    ): String {
        return when (val result = saveImageFileUseCase(uriString, fileBaseName, size)) {
            is UseCaseResult.Success -> result.value
            is UseCaseResult.Failure -> throw result.exception
        }
    }
}
