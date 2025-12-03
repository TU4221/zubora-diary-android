package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageFilePathBuildingException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import javax.inject.Inject

/**
 * 日記画像ファイルの絶対パスを取得するユースケース。
 *
 * @property fileRepository ファイル関連の操作を行うリポジトリ。
 */
internal class BuildDiaryImageFilePathUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    private val logMsg = "日記画像ファイルパス取得_"

    /**
     * 指定された画像ファイル名をもとに、画像ファイルが保存されている場所の絶対パスを返す。
     *
     * @param fileName パスを取得したい画像ファイル名。
     * @return 処理に成功した場合は [UseCaseResult.Success] に 対象ファイルのパス( `String` )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryImageFilePathBuildingException] を格納して返す。
     */
    suspend operator fun invoke(
        fileName: DiaryImageFileName
    ): UseCaseResult<String, DiaryImageFilePathBuildingException> {
        Log.i(logTag, "${logMsg}開始 (ファイル名: $fileName)")

        return try {
            val path =
                if (fileRepository.existsImageFileInCache(fileName)) {
                    fileRepository.buildImageFileAbsolutePathFromCache(fileName)
                } else if (fileRepository.existsImageFileInPermanent(fileName)) {
                    fileRepository.buildImageFileAbsolutePathFromPermanent(fileName)
                } else {
                    throw ResourceNotFoundException()
                }
            Log.i(logTag, "${logMsg}完了 (ファイルパス: $path)")
            UseCaseResult.Success(path)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                DiaryImageFilePathBuildingException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_ファイルパス構築エラー", e)
            UseCaseResult.Failure(
                DiaryImageFilePathBuildingException
                    .BuildingFailure(fileName, e)
            )
        }
    }
}
