package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceAlreadyExistsException
import com.websarva.wings.android.zuboradiary.domain.exception.RollbackException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import java.time.LocalDate
import javax.inject.Inject

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
internal class SaveDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository,
) {

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
        val deleteDiary: Diary?

        // 日記データ保存
        try {
            deleteDiary = saveDiary(isNewDiary, diary, originalDiary)
            if (deleteDiary == null) {
                Log.i(logTag, "${logMsg}日記保存実行 (日付: $saveDate)")
            } else {
                Log.i(logTag, "${logMsg}同じ日付の日記削除と保存実行 (日付: $saveDate)")
            }
        } catch (e: InsufficientStorageException) {
            Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
            return UseCaseResult.Failure(DiarySaveException.InsufficientStorage(saveDate, e))
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(DiarySaveException.Unknown(e))
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_日記データ保存エラー", e)
            return UseCaseResult.Failure(DiarySaveException.SaveFailure(saveDate, e))
        }

        // 画像ファイル処理、日記項目選択履歴データ処理
        try {
            updateStorageDiaryImageFile(
                diary.imageFileName,
                originalDiary.imageFileName,
                deleteDiary?.imageFileName
            )
            updateDiaryItemTitleSelectionHistory(
                diaryItemTitleSelectionHistoryItemList
            )
        } catch (e: Exception) {
            try {
                rollbackDiaryImageFileAndData(
                    isNewDiary,
                    diary,
                    originalDiary,
                    deleteDiary
                )
            } catch (re: RollbackException) {
                Log.w(logTag, "${logMsg}警告_日記データロールバックエラー", re)
                e.addSuppressed(re)
            }
            val wrappedException =
                when (e) {
                    is InsufficientStorageException -> {
                        Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                        DiarySaveException.InsufficientStorage(saveDate, e)
                    }
                    is UnknownException -> {
                        Log.e(logTag, "${logMsg}失敗_原因不明", e)
                        DiarySaveException.Unknown(e)
                    }
                    is DomainException -> {
                        Log.e(logTag, "${logMsg}失敗_日記保存エラー", e)
                        DiarySaveException.SaveFailure(saveDate, e)
                    }
                    else -> throw e
                }
            return UseCaseResult.Failure(wrappedException)
        }

        // バックアップ画像ファイル削除
        try {
            fileRepository.clearAllImageFilesInBackup()
        } catch (e: DomainException) {
            Log.w(logTag, "${logMsg}警告_バックアップ画像ファイル削除エラー", e)
            // 日記保存は成功している為、成功とみなす。
        }

        Log.i(logTag, "${logMsg}完了 (日付: ${saveDate})")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 日記データを保存する。
     *
     * 保存する日記データの日付が、既存の日記データの日付と重複する場合、
     * 既存の日記データを削除してから保存する。
     * 保存完了後は削除した古い日記データを返す。
     *
     * @param isNewDiary 新規作成の日記かどうかを示すフラグ。
     * @param saveDiary 保存する(編集後の)日記。
     * @param originalDiary 編集前の日記。
     * @return 保存する日記データと同じ日付の既存日記データを削除した場合、削除した日記データを返す。削除しなかった場合は `null` を返す。
     * @throws DataStorageException 保存に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    private suspend fun saveDiary(
        isNewDiary: Boolean,
        saveDiary: Diary,
        originalDiary: Diary
    ): Diary? {
        val shouldDeleteSameDateDiary =
            shouldDeleteSameDateDiary(
                isNewDiary,
                saveDiary.date,
                originalDiary.date
            )
        return if (shouldDeleteSameDateDiary) {
            val deleteDiaryId = diaryRepository.loadDiaryId(saveDiary.date)
            val deleteDiary = diaryRepository.loadDiary(deleteDiaryId)
            diaryRepository.deleteAndSaveDiary(deleteDiaryId, saveDiary)
            deleteDiary
        } else {
            diaryRepository.saveDiary(saveDiary)
            null
        }
    }

    /**
     * 保存する日記データと同じ日付の既存日記データを削除する必要があるかどうかを判断する。
     *
     * 新規日記、かつ同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     * 編集日記、かつ編集前後の日付が異なる、かつ編集後と同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     *
     * @param isNewDiary 新規作成の日記かどうかを示すフラグ。
     * @param saveDiaryDate 保存する(編集後の)日記の日付。
     * @param originalDiaryDate 編集前の日記の日付。
     * @return 保存する日記データと同じ日付の既存日記データを削除する必要があれば `true`、そうでなければ `false`。
     * @throws DataStorageException 確認に失敗した場合。
     */
    private suspend fun shouldDeleteSameDateDiary(
        isNewDiary: Boolean,
        saveDiaryDate: LocalDate,
        originalDiaryDate: LocalDate
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
     *
     * @param saveDiaryImageFileName 保存または更新する日記の画像ファイル名。画像がない場合は `null`。
     * @param originalDiaryImageFileName 更新前の元の日記の画像ファイル名。新規作成時や元画像がない場合は `null`。
     * @param deleteDiaryImageFileName 日付重複により削除される日記の画像ファイル名。該当がない、画像がない場合は `null`。
     * @throws DataStorageException いずれかの画像ファイルの更新処理に失敗した場合。
     * @throws InvalidParameterException いずれかの画像ファイル名が無効の場合。
     * @throws ResourceNotFoundException 保存日記の添付画像ファイルが見つからない場合。
     * @throws PermissionException いずれかの画像ファイルへのアクセス権限がない場合。
     * @throws ResourceAlreadyExistsException いずれかの画像ファイルの移動先に同名のファイルが既に存在する場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    private suspend fun updateStorageDiaryImageFile(
        saveDiaryImageFileName: DiaryImageFileName?,
        originalDiaryImageFileName: DiaryImageFileName?,
        deleteDiaryImageFileName: DiaryImageFileName?
    ) {
        deleteDiaryImageFileName?.let {
            try {
                fileRepository.moveImageFileToBackup(it)
            } catch (e: ResourceNotFoundException) {
                Log.w(logTag, "${logMsg}警告_削除する日記の画像ファイルがみつからない為、削除スキップ", e)
            }
        }
        if (saveDiaryImageFileName == originalDiaryImageFileName) return
        originalDiaryImageFileName?.let {
            try {
                fileRepository.moveImageFileToBackup(it)
            } catch (e: ResourceNotFoundException) {
                Log.w(logTag, "${logMsg}警告_編集元の日記の画像ファイルがみつからないため、バックアップ移動スキップ", e)
            }
        }
        saveDiaryImageFileName?.let {
            fileRepository.moveImageFileToPermanent(saveDiaryImageFileName)
        }
    }

    /**
     * 日記項目のタイトル選択履歴をデータベースに保存または更新する。
     *
     * 渡されたタイトル選択リスト( [selectionList] )を元に、以下の処理を行う。
     * 1. リスト内で同じ`title`を持つ項目が複数ある場合、`log`(日時)が最も新しいものだけを残す。
     * 2. データベース内に同じ`title`の履歴が既に存在するかを確認する。
     *    - 存在する場合：既存レコードのIDを引き継いで更新する。
     *    - 存在しない場合：新しいIDで新規履歴として保存する。
     *
     * @param selectionList 更新または作成する日記項目タイトル選択履歴のリスト。
     * @throws DataStorageException 履歴の確認、更新に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    private suspend fun updateDiaryItemTitleSelectionHistory(
        selectionList: List<DiaryItemTitleSelectionHistory>
    ) {
        if (selectionList.isEmpty()) return

        val latestUniqueList =
            selectionList
                .sortedByDescending { it.log }
                .distinctBy { it.title }

        val titles = latestUniqueList.map { it.title }
        val existingHistoriesList =
            diaryRepository.findDiaryItemTitleSelectionHistoriesByTitles(titles)
        val existingHistoryMap = existingHistoriesList.associateBy { it.title }

        val updateHistoryList =
            latestUniqueList.map { currentItem ->
                val existingItem = existingHistoryMap[currentItem.title]
                if (existingItem != null) {
                    currentItem.copy(id = existingItem.id)
                } else {
                    currentItem
                }
            }

        diaryRepository.updateDiaryItemTitleSelectionHistory(updateHistoryList)
    }

    /**
     * 画像ファイルのストレージ操作をロールバック ( [rollbackImageFiles] )し、
     * 日記データの保存、更新処理をロールバック ( [rollbackDiaryData] )する。
     *
     * ロールバック処理途中で例外が発生したら、その時点でロールバック処理を停止する。
     *
     * @param isNewDiary `true` の場合は新規日記の保存に対するロールバック、`false` の場合は既存日記の更新に対するロールバック。
     * @param savedDiary 保存または更新された日記データ。
     * @param originalDiary 編集前の日記データ。新規作成の場合は初期状態を示す。
     * @param deletedDiary 新規保存または日付変更更新の際に、日付の重複により事前に削除された可能性のある日記データ。該当がない場合は `null`。
     * @throws RollbackException 日記データのロールバック処理に失敗した場合。
     */
    private suspend fun rollbackDiaryImageFileAndData(
        isNewDiary: Boolean,
        savedDiary: Diary,
        originalDiary: Diary,
        deletedDiary: Diary?,
    ) {
        rollbackImageFiles(
            savedDiary.imageFileName,
            originalDiary.imageFileName,
            deletedDiary?.imageFileName
        )
        rollbackDiaryData(isNewDiary, savedDiary, originalDiary, deletedDiary)
    }

    /**
     * 日記データの保存、更新処理をロールバックする。
     *
     * 操作の種類（新規/更新、日付変更の有無）や既存データの状態に応じて、以下の復元処理を行う。
     * - 新規保存時: 保存した日記を削除。重複により既存日記が削除されていた場合は復元。
     * - 既存更新時: 日記を更新前の内容に戻す。
     *   - 日付変更があり、変更後の日付に別の日記が存在し削除された場合: その別日記を復元し、元の日記も復元。
     *
     * @param isNewDiary `true` の場合は新規日記の保存に対するロールバック、`false` の場合は既存日記の更新に対するロールバック。
     * @param savedDiary 保存または更新された日記データ。
     * @param originalDiary 編集前の日記データ。新規作成の場合は初期状態を示す。
     * @param deletedDiary 新規保存または日付変更更新の際に、日付の重複により事前に削除された可能性のある日記データ。該当がない場合は `null`。
     * @throws RollbackException 日記データのロールバック処理に失敗した場合。
     */
    private suspend fun rollbackDiaryData(
        isNewDiary: Boolean,
        savedDiary: Diary,
        originalDiary: Diary,
        deletedDiary: Diary?,
    ) {
        try {
            if (isNewDiary) {
                if (deletedDiary == null) {
                    Log.d(logTag, "新規ロールバック_保存日記削除 (保存日記日付: ${savedDiary.date})")
                    diaryRepository.deleteDiary(savedDiary.id)
                } else {
                    Log.d(logTag, "新規ロールバック_保存日記削除、削除日記復元 (保存日記日付: ${savedDiary.date}、削除日記日付: ${deletedDiary.date})")
                    diaryRepository.deleteAndSaveDiary(savedDiary.id, deletedDiary)
                }
            } else {
                if (deletedDiary != null) {
                    Log.d(logTag, "編集ロールバック_保存日記削除、削除日記復元 (保存日記日付: ${savedDiary.date}、削除日記日付: ${deletedDiary.date})")
                    diaryRepository.deleteAndSaveDiary(savedDiary.id, deletedDiary)
                }
                Log.d(logTag, "編集ロールバック_編集元日記復元 (編集元日記日付: ${originalDiary.date})")
                diaryRepository.saveDiary(originalDiary)
            }
        } catch (e: Exception) {
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
        savedDiaryImageFileName: DiaryImageFileName?,
        originalDiaryImageFileName: DiaryImageFileName?,
        deletedDiaryImageFileName: DiaryImageFileName?
    ) {
        try {
            if (savedDiaryImageFileName != originalDiaryImageFileName) {
                if (savedDiaryImageFileName != null) {
                    if (fileRepository.existsImageFileInPermanent(savedDiaryImageFileName)) {
                        Log.d(logTag, "保存日記画像ファイルロールバック (ファイル名: ${savedDiaryImageFileName.fullName})")
                        fileRepository.restoreImageFileFromPermanent(savedDiaryImageFileName)
                    }
                }
                if (originalDiaryImageFileName != null) {
                    if (fileRepository.existsImageFileInBackup(originalDiaryImageFileName)) {
                        Log.d(logTag, "編集元画像ファイルロールバック (ファイル名: ${originalDiaryImageFileName.fullName})")
                        fileRepository.restoreImageFileFromBackup(originalDiaryImageFileName)
                    }
                }
            }
            if (deletedDiaryImageFileName != null) {
                if (fileRepository.existsImageFileInBackup(deletedDiaryImageFileName)) {
                    Log.d(logTag, "削除日記画像ファイルロールバック (ファイル名: ${deletedDiaryImageFileName.fullName})")
                    fileRepository.restoreImageFileFromBackup(deletedDiaryImageFileName)
                }
            }
        } catch (e: Exception) {
            throw RollbackException(cause = e)
        }
    }
}
