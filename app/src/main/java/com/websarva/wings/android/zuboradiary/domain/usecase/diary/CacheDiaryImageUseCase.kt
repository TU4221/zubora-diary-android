package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 日記画像ファイルをキャッシュストレージへキャッシュするユースケース。
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
     * ファイル名は日記IDとタイムスタンプを組み合わせた形式 (日記ID_yyyyMMddHHmmssSSS.拡張子) となる。
     *
     * @param uriString キャッシュされるファイルのもととなる画像URI文字列。
     * @param diaryId 編集中の日記ID。画像ファイル名の一要素となる。
     * @return 処理に成功した場合は [UseCaseResult.Success] に キャッシュされたファイルのパス ( [ImageFileName] ) を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageCacheException] を格納して返す。
     */
    suspend operator fun invoke(
        uriString: String,
        diaryId: UUIDString
    ): UseCaseResult<ImageFileName, DiaryImageCacheException> {
        Log.i(logTag, "${logMsg}開始 (画像URI: $uriString、 ファイルベース名: $diaryId)")

        return try {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
            val currentDateTimeString = currentDateTime.format(formatter)
            val fileBaseName = "${diaryId.value}_${currentDateTimeString}"
            val fileName = fileRepository.cacheImageFile(uriString, fileBaseName)
            Log.i(logTag, "${logMsg}完了 (ファイル名: $fileName)")
            UseCaseResult.Success(fileName)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_キャッシュエラー", e)
            UseCaseResult.Failure(
                DiaryImageCacheException.CacheFailure(e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryImageCacheException.Unknown(e)
            )
        }
    }
}
