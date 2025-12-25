package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import javax.inject.Inject

/**
 * 特定の日付の日記を削除するユースケース。
 *
 * 日記データと共に、関連付けられた画像URIの永続的な権限も解放する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property fileRepository ファイル関連へのアクセスを提供するリポジトリ。
 */
internal class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository
) {

    private val logMsg = "日記削除_"

    /**
     * ユースケースを実行し、指定された日付の日記を削除し、関連する画像ファイルを削除する。
     *
     * @param id 削除する日記のID。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   日記の削除、または画像ファイルの削除に失敗した場合は [UseCaseResult.Failure] に [DiaryDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        id: DiaryId
    ): UseCaseResult<Unit, DiaryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (ID: ${id.value})")

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
                    id,
                    deleteDiary?.date,
                    e
                )
            )
        }

        // 日記添付画像ファイル削除
        // MEMO:削除対象の日記が無かった場合は失敗とならず、読み出しデータが‘null‘の状態で処理が続行される為、
        //      セーフコール演算子で下記対応。
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

        val dateInfo = "日付:" + (deleteDiary?.date?.let { "$it" } ?: "不明（データなし）")
        val imageFileInfo = "画像ファイル名: " + (deleteDiary?.imageFileName?.let { "$it" } ?: "なし")
        Log.i(logTag, "${logMsg}完了 (ID: ${id.value}, $dateInfo, $imageFileInfo)")
        return UseCaseResult.Success(Unit)
    }
}
