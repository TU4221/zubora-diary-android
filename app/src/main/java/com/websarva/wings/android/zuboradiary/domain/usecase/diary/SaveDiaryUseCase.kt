package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiarySaveFailureException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
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
     * @return 日記の保存処理が成功した場合は [UseCaseResult.Success] を返す。
     *   日記の保存処理に失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始 (新規: $isNewDiary, 日付: ${diary.date}, 元日付: ${originalDiary.date})")

        try {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                originalDiary.date,
                isNewDiary
            )
        } catch (e: DiarySaveFailureException) {
            Log.e(logTag, "${logMsg}失敗_日記データ保存エラー", e)
            return UseCaseResult.Failure(e)
        }

        try {
            releaseImageUriPermission(originalDiary.imageUriString)
        } catch (e: DomainException) {
            // Uri権限の解放に失敗しても日記保存がメインの為、成功とみなす。
            Log.w(logTag, "${logMsg}元の画像URI権限解放失敗 (URI: \"${originalDiary.imageUriString}\")", e)
        }

        try {
            takeSavedImageUriPermission(diary.imageUriString)
        } catch (e: DomainException) {
            // Uri権限の取得に失敗しても日記保存がメインの為、成功とみなす。
            Log.w(logTag, "${logMsg}保存画像のURI権限取得失敗 (URI: \"${diary.imageUriString}\")", e)
        }

        Log.i(logTag, "${logMsg}完了 (日付: ${diary.date})")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 日記データとタイトル選択履歴を保存する。
     *
     * 日付が変更された既存日記の場合、元の日付の日記を削除してから新しい日付で保存する。
     *
     * @param diary 保存する日記データ。
     * @param diaryItemTitleSelectionHistoryItemList 保存する日記項目のタイトル選択履歴リスト。
     * @param originalDate 更新前の日記の日付。新規作成の場合は、保存する日記の日付と同じ。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。
     * @throws DiarySaveFailureException 日記データの保存または削除に失敗した場合。
     */
    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        val saveDate = diary.date
        try {
            if (shouldDeleteOriginalDateDiary(saveDate, originalDate, isNewDiary)) {
                Log.i(logTag, "${logMsg}元の日付の日記削除と新規保存実行 (元日付: $originalDate, 新日付: $saveDate)")
                diaryRepository
                    .deleteAndSaveDiary(
                        originalDate,
                        diary,
                        diaryItemTitleSelectionHistoryItemList
                    )
            } else {
                Log.i(logTag, "${logMsg}日記保存実行 (日付: $saveDate)")
                diaryRepository
                    .saveDiary(diary, diaryItemTitleSelectionHistoryItemList)
            }
        } catch (e: DiarySaveFailureException) {
            throw e
        }
    }

    /**
     * 元の日付の日記を削除する必要があるかどうかを判断する。
     *
     * 新規日記の場合は削除不要。既存日記で、かつ入力された日付が元の日付と異なる場合に削除が必要と判断する。
     *
     * @param inputDate 入力された日記の日付。
     * @param originalDate 元の日記の日付。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。
     * @return 元の日付の日記を削除する必要があれば `true`、そうでなければ `false`。
     */
    private fun shouldDeleteOriginalDateDiary(
        inputDate: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): Boolean {
        if (isNewDiary) return false

        return inputDate != originalDate
    }

    /**
     * 指定された画像URI文字列の永続的権限を解放する。
     *
     * URI文字列が `null` の場合は何も行わない。
     *
     * @param uriString 権限を解放する対象の画像URI文字列。`null` の場合は処理をスキップ。
     * @throws DomainException 権限解放処理に失敗した場合
     *   ([ReleaseDiaryImageUriPermissionUseCase] からスローされる例外)。
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
     * @throws DomainException 権限取得処理に失敗した場合
     *   ([TakePersistableUriPermissionUseCase] からスローされる例外)。
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
