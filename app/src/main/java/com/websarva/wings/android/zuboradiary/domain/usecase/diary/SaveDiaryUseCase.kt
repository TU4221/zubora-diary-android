package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageUriPermissionReleaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.exception.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記データと関連情報を保存するユースケース。
 *
 * 新規日記の保存、または既存日記の更新を行う。
 * 日記の日付が変更された場合は、元の日付の日記を削除してから新しい日付で保存する。
 * また、日記に関連付けられた画像のURI永続的権限の管理（解放と取得）も行う。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property takePersistableUriPermissionUseCase 永続的なURI権限を取得するためのユースケース。
 * @property releaseDiaryImageUriPermissionUseCase 日記画像URIの永続的権限を解放するためのユースケース。
 */
internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val takePersistableUriPermissionUseCase: TakePersistableUriPermissionUseCase,
    private val releaseDiaryImageUriPermissionUseCase: ReleaseDiaryImageUriPermissionUseCase,
) {

    private val logTag = createLogTag()
    private val logMsg = "日記保存_"

    /**
     * ユースケースを実行し、日記データと関連情報を保存する。
     *
     * 1. 日記データとタイトル選択履歴を保存する。
     * 2. 元の日記に画像URIが存在した場合、その権限を解放する。
     * 3. 新しい日記に画像URIが存在する場合、その権限を取得する。
     *
     * 画像URIの権限解放・取得処理でエラーが発生しても、日記保存処理が成功していれば全体としては成功とみなす。
     *
     * @param diary 保存または更新する日記データ。
     * @param diaryItemTitleSelectionHistoryItemList 保存する日記項目のタイトル選択履歴リスト。
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

        try {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                originalDiary.date,
                isNewDiary
            )
        } catch (e: DiaryExistenceCheckException) {
            Log.e(logTag, "${logMsg}失敗_同じ日付の日記データの既存確認エラー", e)
            return UseCaseResult.Failure(
                DiarySaveException.SaveFailure(diary.date, e)
            )
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_日記データ保存エラー", e)
            return UseCaseResult.Failure(
                DiarySaveException.SaveFailure(diary.date, e)
            )
        }

        try {
            releaseImageUriPermission(originalDiary.imageUriString)
        } catch (e: DiaryImageUriPermissionReleaseException) {
            // Uri権限の解放に失敗しても日記保存がメインの為、成功とみなす。
            Log.w(logTag, "${logMsg}元の画像URI権限解放失敗 (URI: \"${originalDiary.imageUriString}\")", e)
            DiarySaveException.PermissionReleaseFailure(originalDiary.imageUriString ?: "null", e)
        }

        try {
            takeSavedImageUriPermission(diary.imageUriString)
        } catch (e: PersistableUriPermissionTakeFailureException) {
            // Uri権限の取得に失敗しても日記保存がメインの為、成功とみなす。
            Log.w(logTag, "${logMsg}保存画像のURI権限取得失敗 (URI: \"${diary.imageUriString}\")", e)
            DiarySaveException.PermissionTakeFailure(diary.imageUriString ?: "null", e)
        }

        Log.i(logTag, "${logMsg}完了 (日付: ${diary.date})")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 日記データとタイトル選択履歴を保存する。
     *
     * 同じ日付の(識別番号が)異なる日記データがデータベースに存在する場合、データベースの日記を削除してから保存する。
     * これにより、同じ日付の日記データが複数存在することを防ぐ。
     *
     * @param diary 保存する日記データ。
     * @param diaryItemTitleSelectionHistoryItemList 保存する日記項目のタイトル選択履歴リスト。
     * @param originalDate 編集前の日記の日付。新規作成の場合は、保存する日記の日付と同じ。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。
     * @throws DataStorageException 日記データの保存または削除に失敗した場合。
     * @throws DiaryExistenceCheckException 保存する日記データと同じ日付の日記データを
     * 削除する必要があるかどうかを確認するのに失敗した場合。
     */
    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        val saveDate = diary.date
        if (shouldDeleteSameDateDiary(saveDate, originalDate, isNewDiary)) {
            Log.i(logTag, "${logMsg}同じ日付の日記削除と保存実行 (日付: $saveDate)")
            diaryRepository
                .deleteAndSaveDiary(
                    diary,
                    diaryItemTitleSelectionHistoryItemList
                )
        } else {
            Log.i(logTag, "${logMsg}日記保存実行 (日付: $saveDate)")
            diaryRepository
                .saveDiary(diary, diaryItemTitleSelectionHistoryItemList)
        }
    }

    /**
     * 保存する日記データと同じ日付の既存日記データを削除する必要があるかどうかを判断する。
     *
     * 新規日記、かつ同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     * 編集日記、かつ編集前後の日付が異なる、かつ編集後と同じ日付の日記データがデータベースに存在する場合は削除を必要とする。
     *
     * @param inputDate 保存する(編集後の)日記の日付。
     * @param originalDate 編集前の日記の日付。
     * @param isNewDiary 新規作成の日記かどうかを示すフラグ。
     * @return 保存する日記データと同じ日付の既存日記データを削除する必要があれば `true`、そうでなければ `false`。
     * @throws DiaryExistenceCheckException 確認に失敗した場合。
     */
    private suspend fun shouldDeleteSameDateDiary(
        inputDate: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): Boolean {
        /*
        * 新規保存 -> 同日付既存日記なし -> 新規日記保存
        *         -> 同日付既存日記あり -> 同日付既存日記削除、新規日記保存 (既存日記は異なるIdとなる為削除必須)
        *
        * 既存編集保存 -> 日付変更なし -> (同日付既存日記なし) -> (既存日記保存) (日付変更なしパターンで同日付既存日記なしはありえない)
        *                         -> (同日付既存日記あり) -> 編集日記保存
        *            -> 日付変更あり -> 同日付既存日記なし -> 編集日記保存
        *                         -> 同日付既存日記あり -> 同日付既存日記削除、新規日記保存
        * */
        return if (isNewDiary) {
            doesDiaryExist(inputDate)
        } else {
            if (inputDate == originalDate) {
                false
            } else {
                doesDiaryExist(inputDate)
            }
        }
    }

    /**
     * 指定された日付の日記データがデータベースに存在するかどうかを確認。
     *
     * @param date 確認対象の日付。
     * @return 指定された日付の日記が存在する場合は `true`、存在しない場合は `false`を返す。
     * @throws DiaryExistenceCheckException 確認に失敗した場合。
     */
    private suspend fun doesDiaryExist(date: LocalDate): Boolean {
        return when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 指定された画像URI文字列の永続的権限を解放する。
     *
     * URI文字列が `null` の場合は何も行わない。
     *
     * @param uriString 権限を解放する対象の画像URI文字列。`null` の場合は処理をスキップ。
     * @throws DiaryImageUriPermissionReleaseException 権限解放処理に失敗した場合。
     */
    private suspend fun releaseImageUriPermission(
        uriString: String?
    ) {
        if (uriString == null) {
            Log.i(logTag, "${logMsg}_URI解放不要 (画像URIなし)")
            return
        }

        when (val result = releaseDiaryImageUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 保存された日記の画像URI文字列に対して永続的な権限を取得する。
     *
     * URI文字列が `null` の場合は何も行わない。
     *
     * @param uriString 権限を取得する対象の画像URI文字列。`null` の場合は処理をスキップ。
     * @throws PersistableUriPermissionTakeFailureException 権限取得処理に失敗した場合。
     */
    private fun takeSavedImageUriPermission(
        uriString: String?
    ) {
        if (uriString == null) {
            Log.i(logTag, "${logMsg}_URI取得不要 (画像URIなし)")
            return
        }

        when (val result = takePersistableUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
