package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
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
        imageFileName: ImageFileName?
    ): UseCaseResult<Unit, DiaryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date, 画像ファイル名: ${imageFileName?.let { "\"$it\"" } ?: "なし"})")

        val backupDiary: Diary?

        // 日記データ削除
        try {
            backupDiary = diaryRepository.loadDiary(date)
            diaryRepository.deleteDiary(date)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.DiaryDataDeleteFailure(date, e)
            )
        } catch (e: NotFoundException) {
            Log.e(logTag, "${logMsg}失敗_削除する日記データがみつからない", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.DiaryDataNotFound(date, e)
            )
        }

        // 日記添付画像ファイル削除
        if (imageFileName != null) {
            try {
                fileRepository.deleteImageFileInPermanent(imageFileName)
            } catch (e: NotFoundException) {
                Log.w(logTag, "${logMsg}警告_削除する日記の画像ファイルがみつからない", e)
                // 成功とみなす
            } catch (e: DomainException) {
                Log.e(logTag, "${logMsg}失敗_画像ファイル削除エラー", e)

                try {
                    diaryRepository.saveDiary(backupDiary)
                } catch (e: DataStorageException) {
                    Log.w(logTag, "${logMsg}警告_日記データロールバックエラーの為、削除成功とみなす", e)
                    // 日記データがメインとなる為、成功とみなす
                    return UseCaseResult.Success(Unit)
                }

                return UseCaseResult.Failure(
                    DiaryDeleteException.DiaryImageFileDeleteFailure(date, imageFileName, e)
                )
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
