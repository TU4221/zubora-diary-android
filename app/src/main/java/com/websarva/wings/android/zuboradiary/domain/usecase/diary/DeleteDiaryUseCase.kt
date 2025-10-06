package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

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
     * ユースケースを実行し、指定された日付の日記を削除し、関連する画像ファイルを削除する。
     *
     * @param id 削除する日記のID。
     * @param date 削除する日記の日付。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   日記の削除、または画像ファイルの削除に失敗した場合は [UseCaseResult.Failure] に [DiaryDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        id: UUIDString,
        date: LocalDate
    ): UseCaseResult<Unit, DiaryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (ID: ${id.value}, 日付: $date)")

        var deleteDiary: Diary? = null
            // 日記データ削除
        try {
            deleteDiary = diaryRepository.loadDiary(id)
            diaryRepository.deleteDiary(id)
        } catch (e: ResourceNotFoundException) {
            Log.w(logTag, "${logMsg}警告_削除する日記データがみつからないため、成功とみなす", e)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.DiaryDataDeleteFailure(
                    deleteDiary?.date ?:date,
                    e
                )
            )
        }

        // 日記添付画像ファイル削除
        deleteDiary?.imageFileName?.let {
            try {
                fileRepository.deleteImageFileInPermanent(it)
            } catch (e: ResourceNotFoundException) {
                Log.w(logTag, "${logMsg}警告_削除する日記の画像ファイルがみつからないため、成功とみなす", e)
            } catch (e: UnknownException) {
                Log.e(logTag, "${logMsg}失敗_原因不明", e)
                return UseCaseResult.Failure(
                    DiaryDeleteException.Unknown(e)
                )
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
                    DiaryDeleteException.ImageFileDeleteFailure(
                        deleteDiary.date,
                        it,
                        e
                    )
                )
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
