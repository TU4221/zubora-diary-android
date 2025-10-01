package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

// TODO:日記IDで削除するように変更
/**
 * 特定の日付の日記を削除するユースケース。
 *
 * 日記データと共に、関連付けられた画像URIの永続的な権限も解放する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property fileRepository ファイル関連へのアクセスを提供するリポジトリ。
 */
internal class DeleteDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記削除_"

    /**
     * ユースケースを実行し、指定された日付の日記を削除し、関連する画像画像ファイルを削除する。
     *
     * @param date 削除する日記の日付。
     * @param imageFileName 削除する日記に添付された画像ファイル名。
     *                       `null`の場合は画像ファイル削除処理をスキップする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   日記の削除、または画像ファイルの削除に失敗した場合は [UseCaseResult.Failure] に [DiaryDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        date: LocalDate,
        imageFileName: FileName?
    ): UseCaseResult<Unit, DiaryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date, 画像ファイル名: ${imageFileName?.let { "\"$it\"" } ?: "なし"})")

        // 日記データ削除
        try {
            diaryRepository.deleteDiary(date)
        } catch (e: ResourceNotFoundException) {
            Log.w(logTag, "${logMsg}警告_削除する日記データがみつからないため、成功とみなす", e)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.DiaryDataDeleteFailure(date, e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.Unknown(e)
            )
        }

        // 日記添付画像ファイル削除
        if (imageFileName != null) {
            try {
                fileRepository.deleteImageFileInPermanent(imageFileName)
            } catch (e: ResourceNotFoundException) {
                Log.w(logTag, "${logMsg}警告_削除する日記の画像ファイルがみつからないため、成功とみなす", e)
            } catch (e: DomainException) {
                // TODO:下記仕様で問題ないか後で検討。
                //      現在考えれる問題点
                //      - 画像ファイル名が日記IDで構成されている為、ユーザーが手動で削除するのは難しい。
                //      - 画像ファイル名に日記日付を加えると画像添付もとの日記の日付が変更された時、
                //        画像ファイル名も変更する必要がある。
                //        (データベースの日記データの添付画像ファイル名も変更する必要が出てくる)
                // MEMO:本アプリの日記は一日一件のみ保存が可能となる仕様のため、画像ファイル削除失敗が続くと、
                //      その日付に対しての新しい日記を作成できなくなる。
                //      そのためロールバック処理を廃止し、画像ファイル削除失敗をユーザーに通知するのみとする。
                Log.e(logTag, "${logMsg}警告_画像ファイル削除エラー", e)
                return UseCaseResult.Failure(
                    DiaryDeleteException.ImageFileDeleteFailure(date, imageFileName, e)
                )
            } catch (e: Exception) {
                Log.e(logTag, "${logMsg}失敗_原因不明", e)
                return UseCaseResult.Failure(
                    DiaryDeleteException.Unknown(e)
                )
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
