package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RollbackException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記データと関連情報を保存するユースケース。
 *
 * 新規日記の保存、または既存日記の更新を行う。
 * 保存、更新する日記データの日付が、既存の日記データの日付と重複する場合、
 * 既存の日記データを削除してから新しい日記データを保存、更新する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property fileRepository ファイル関連へのアクセスを提供するリポジトリ。
 */
internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記保存_"

    /**
     * ユースケースを実行し、日記データと関連情報を保存する。
     *
     * 同じ日付の(識別番号が)異なる日記データがデータベースに存在する場合、データベースの日記を削除してから保存する。
     * これにより、同じ日付の日記データが複数存在することを防ぐ。
     *
     * @param diary 保存または更新する日記データ。
     * @param diaryItemTitleSelectionHistoryItemList 更新する日記項目のタイトル選択履歴リスト。
     * @param originalDiary 更新前の元の日記データ。新規作成の場合は、ダミー日記データ。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。`true`の場合は新規作成、`false`の場合は既存日記の更新。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiarySaveException] を格納して返す。
     */
    suspend operator fun invoke(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ): UseCaseResult<Unit, DiarySaveException> {
        Log.i(logTag, "${logMsg}開始 (新規: $isNewDiary, 日付: ${diary.date}, 元日付: ${originalDiary.date})")

        val saveDate = diary.date
        val originalDate = originalDiary.date
        var deleteDiary: Diary? = null

        // 日記データ保存処理方法判断
        try {
            if (shouldDeleteSameDateDiary(saveDate, originalDate, isNewDiary)) {
                Log.i(logTag, "${logMsg}同じ日付の日記削除と保存実行 (日付: $saveDate)")

                deleteDiary = diaryRepository.loadDiary(diary.date)
                diaryRepository.deleteAndSaveDiary(diary)
            } else {
                Log.i(logTag, "${logMsg}日記保存実行 (日付: $saveDate)")
                diaryRepository.saveDiary(diary)
            }
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_日記データ保存処理エラー", e)
            return UseCaseResult.Failure(
                DiarySaveException.SaveDiaryDataFailure(saveDate, e)
            )
        }

        // 画像ファイル処理
        try {
            updateStorageDiaryImageFile(
                diary.imageFileName,
                originalDiary.imageFileName,
                deleteDiary?.imageFileName
            )
        } catch (e: DiarySaveException.StorageImageFileUpdateFailure) {
            try {
                rollbackDiaryData(diary, originalDiary, isNewDiary, deleteDiary)
            } catch (re: RollbackException) {
                Log.w(logTag, "${logMsg}警告_日記データロールバック処理エラー", e)
                e.addSuppressed(re)
            }
            Log.e(logTag, "${logMsg}失敗_日記画像ファイル処理エラー", e)
            return UseCaseResult.Failure(e)
        }

        // 日記項目選択履歴データ処理
        try {
            diaryRepository
                .updateDiaryItemTitleSelectionHistory(
                    diaryItemTitleSelectionHistoryItemList
                )
        } catch (e: DataStorageException) {
            try {
                rollbackImageFiles(
                    diary.imageFileName,
                    originalDiary.imageFileName,
                    deleteDiary?.imageFileName
                )
                rollbackDiaryData(diary, originalDiary, isNewDiary, deleteDiary)
            } catch (re: RollbackException) {
                Log.w(logTag, "${logMsg}警告_日記データ、画像ロールバック処理エラー", e)
                e.addSuppressed(re)
            }
            Log.e(logTag, "${logMsg}失敗_日記項目選択履歴更新処理エラー", e)
            return UseCaseResult.Failure(
                DiarySaveException.SaveDiaryItemTileSelectionHistoryFailure(e)
            )
        }

        // バックアップ画像ファイル削除
        try {
            fileRepository.clearAllImageFilesInBackup()
        } catch (e: RepositoryException) {
            Log.w(logTag, "${logMsg}バックアップ画像ファイル削除処理エラー", e)
            // 日記保存は成功している為、成功とみなす。
        }

        Log.i(logTag, "${logMsg}完了 (日付: ${diary.date})")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 保存する日記データと同じ日付の既存日記データを削除する必要があるかどうかを判断する。
     *
     * 新規日記、かつ同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     * 編集日記、かつ編集前後の日付が異なる、かつ編集後と同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     *
     * @param saveDiaryDate 保存する(編集後の)日記の日付。
     * @param originalDiaryDate 編集前の日記の日付。
     * @param isNewDiary 新規作成の日記かどうかを示すフラグ。
     * @return 保存する日記データと同じ日付の既存日記データを削除する必要があれば `true`、そうでなければ `false`。
     * @throws DataStorageException 確認に失敗した場合。
     */
    private suspend fun shouldDeleteSameDateDiary(
        saveDiaryDate: LocalDate,
        originalDiaryDate: LocalDate,
        isNewDiary: Boolean
    ): Boolean {
        /* TODO:動作確認後削除
        * 新規保存 -> 同日付既存日記なし -> 新規日記保存
        *         -> 同日付既存日記あり -> 新規日記画像保存 -> 同日付既存日記削除、新規日記保存 (既存日記は異なるIdとなる為削除必須) -> 同日付既存日記画像削除
        *
        * 既存編集保存 -> 日付変更なし -> (同日付既存日記なし) -> (既存日記保存) (日付変更なしパターンで同日付既存日記なしはありえない)
        *                         -> (同日付既存日記あり) -> 画像変更なし ------------> 編集日記保存
        *                                             -> 画像変更あり -> 新画像保存 -> 編集日記保存 -> 旧画像削除
        *
        *            -> 日付変更あり -> 同日付既存日記なし -> 画像変更なし ------------> 編集日記保存
        *                                             -> 画像変更あり -> 新画像保存 -> 編集日記保存 -> 旧画像削除
        *
        *                         -> 同日付既存日記あり -> 画像変更なし --------------> 同日付既存日記削除、新規日記保存 -> 同日付既存日記画像削除
        *                                            -> 画像変更あり -> 新画像保存 -> 同日付既存日記削除、新規日記保存 -> 旧画像削除 -> 同日付既存日記画像削除
        *
        * 問題点：新旧の画像ファイルをどのように変更する？データベース処理のエラーハンドリング考慮
        * */
        return if (isNewDiary) {
            diaryRepository.existsDiary(saveDiaryDate)
        } else {
            if (saveDiaryDate == originalDiaryDate) {
                false
            } else {
                diaryRepository.existsDiary(saveDiaryDate)
            }
        }
    }

    /**
     * ストレージ内の日記画像ファイルを更新する。
     *
     * 新しい画像ファイルの保存、古い画像ファイルのバックアップ移動、
     * または日付変更に伴い削除される別日記の画像ファイルのバックアップ移動を行う。
     * 各ファイル操作でエラーが発生した場合、それまでの操作を可能な範囲でロールバックし、
     * [DiarySaveException.StorageImageFileUpdateFailure] をスローする。
     *
     * @param saveDiaryImageFileName 保存または更新する日記の画像ファイル名。画像がない場合は `null`。
     * @param originalDiaryImageFileName 更新前の元の日記の画像ファイル名。新規作成時や元画像がない場合は `null`。
     * @param deleteDiaryImageFileName 日付重複により削除される日記の画像ファイル名。該当がない、画像がない場合は `null`。
     * @throws DiarySaveException.StorageImageFileUpdateFailure 画像ファイルの更新処理に失敗した場合。
     */
    private suspend fun updateStorageDiaryImageFile(
        saveDiaryImageFileName: ImageFileName?,
        originalDiaryImageFileName: ImageFileName?,
        deleteDiaryImageFileName: ImageFileName?
    ) {

        if (deleteDiaryImageFileName != null) {
            // 別ID削除日記画像ファイル削除(バックアップへ移動)
            try {
                fileRepository.moveImageFileToBackup(deleteDiaryImageFileName)
            } catch (e: RepositoryException) {
                throw DiarySaveException.StorageImageFileUpdateFailure(e)
            }
        }

        if (saveDiaryImageFileName == null) {
            if (originalDiaryImageFileName != null) {
                // 同ID日記旧画像ファイル削除(バックアップへ移動)
                try {
                    fileRepository.moveImageFileToBackup(originalDiaryImageFileName)
                } catch (e: RepositoryException) {
                    try {
                        rollbackImageFiles(null, originalDiaryImageFileName, deleteDiaryImageFileName)
                    } catch (re: RollbackException) {
                        e.addSuppressed(re)
                    }
                    throw DiarySaveException.StorageImageFileUpdateFailure(e)
                }
            }
        } else {
            // 同ID日記新画像ファイル保存
            try {
                if (fileRepository.existsImageFileInPermanent(saveDiaryImageFileName)) {
                    fileRepository.moveImageFileToBackup(saveDiaryImageFileName)
                }
            } catch (e: RepositoryException) {
                try {
                    rollbackImageFiles(saveDiaryImageFileName, originalDiaryImageFileName, deleteDiaryImageFileName)
                } catch (re: RollbackException) {
                    e.addSuppressed(re)
                }
                throw DiarySaveException.StorageImageFileUpdateFailure(e)
            }
            try {
                fileRepository.moveImageFileToPermanent(saveDiaryImageFileName)
            } catch (e: RepositoryException) {
                try {
                    rollbackImageFiles(saveDiaryImageFileName, originalDiaryImageFileName, deleteDiaryImageFileName)
                } catch (re: RollbackException) {
                    e.addSuppressed(re)
                }
                throw DiarySaveException.StorageImageFileUpdateFailure(e)
            }
        }
    }

    /**
     * 日記データの保存、更新処理をロールバックする。
     *
     * 操作の種類（新規/更新、日付変更の有無）や既存データの状態に応じて、以下の復元処理を行う。
     * - 新規保存時: 保存した日記を削除。重複により既存日記が削除されていた場合は復元。
     * - 既存更新時: 日記を更新前の内容に戻す。
     *   - 日付変更があり、変更後の日付に別の日記が存在し削除された場合: その別日記を復元し、元の日記も復元。
     *
     * @param savedDiary 保存または更新された日記データ。
     * @param originalDiary 編集前の日記データ。新規作成の場合は初期状態を示す。
     * @param isNewDiary `true` の場合は新規日記の保存に対するロールバック、`false` の場合は既存日記の更新に対するロールバック。
     * @param deletedDiary 新規保存または日付変更更新の際に、日付の重複により事前に削除された可能性のある日記データ。該当がない場合は `null`。
     * @throws RollbackException 日記データのロールバック処理に失敗した場合。
     */
    private suspend fun rollbackDiaryData(
        savedDiary: Diary,
        originalDiary: Diary,
        isNewDiary: Boolean,
        deletedDiary: Diary?,
    ) {
        try {
            if (isNewDiary) {
                if (deletedDiary == null) {
                    diaryRepository.deleteDiary(savedDiary.date)
                } else {
                    diaryRepository.deleteAndSaveDiary(deletedDiary)
                }
            } else {
                if (deletedDiary == null) {
                    diaryRepository.saveDiary(originalDiary)
                } else {
                    diaryRepository.deleteAndSaveDiary(deletedDiary)
                }
            }
        } catch (e: RepositoryException) {
            throw RollbackException(cause = e)
        }
    }

    /**
     * 画像ファイルのストレージ操作をロールバックする。
     *
     * [updateStorageDiaryImageFile] で行われたファイル移動を元の状態に戻す。
     *
     * @param savedDiaryImageFileName 保存または更新された日記の画像ファイル名。画像が無い場合は `null`。
     * @param originalDiaryImageFileName 編集前の元の日記の画像ファイル名。新規作成時や元画像がない場合は `null`。
     * @param deletedDiaryImageFileName 日付重複により削除された日記の画像ファイル名。該当がない、画像がない場合は `null`。
     * @throws RollbackException 画像ファイルのロールバック処理に失敗した場合。
     */
    private suspend fun rollbackImageFiles(
        savedDiaryImageFileName: ImageFileName?,
        originalDiaryImageFileName: ImageFileName?,
        deletedDiaryImageFileName: ImageFileName?
    ) {
        try {
            if (savedDiaryImageFileName != null) {
                if (fileRepository.existsImageFileInPermanent(savedDiaryImageFileName)) {
                    fileRepository.restoreImageFileFromPermanent(savedDiaryImageFileName)
                }
                if (fileRepository.existsImageFileInBackup(savedDiaryImageFileName)) {
                    fileRepository.restoreImageFileFromBackup(savedDiaryImageFileName)
                }
            }
            if (originalDiaryImageFileName != null) {
                if (fileRepository.existsImageFileInBackup(originalDiaryImageFileName)) {
                    fileRepository.restoreImageFileFromBackup(originalDiaryImageFileName)
                }
            }
            if (deletedDiaryImageFileName != null) {
                if (fileRepository.existsImageFileInBackup(deletedDiaryImageFileName)) {
                    fileRepository.restoreImageFileFromBackup(deletedDiaryImageFileName)
                }
            }
        } catch (e: RepositoryException) {
            throw RollbackException(cause = e)
        }
    }
}
