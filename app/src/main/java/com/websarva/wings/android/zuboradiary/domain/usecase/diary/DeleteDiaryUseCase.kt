package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageUriPermissionReleaseException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 特定の日付の日記を削除するユースケース。
 *
 * 日記データと共に、関連付けられた画像URIの永続的な権限も解放する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property releaseDiaryImageUriPermissionUseCase 画像URIの永続的権限を解放するためのユースケース。
 */
internal class DeleteDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseDiaryImageUriPermissionUseCase: ReleaseDiaryImageUriPermissionUseCase,
) {

    private val logTag = createLogTag()
    private val logMsg = "日記削除_"

    /**
     * ユースケースを実行し、指定された日付の日記を削除し、関連する画像URIの権限を解放する。
     *
     * @param date 削除する日記の日付。
     * @param imageUriString 削除する日記に関連付けられた画像のURI文字列。権限解放の対象となる。
     *                       `null`の場合は権限解放処理をスキップする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   画像URI権限の解放に失敗しても日記削除が成功すれば成功とみなす。
     *   日記の削除処理自体に失敗した場合は [UseCaseResult.Failure] に [DiaryDeleteException] を格納して返す。
     */
    suspend operator fun invoke(
        date: LocalDate,
        imageUriString: String?
    ): UseCaseResult<Unit, DiaryDeleteException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date, 画像URI: ${imageUriString?.let { "\"$it\"" } ?: "なし"})")

        try {
            diaryRepository.deleteDiary(date)
        } catch (e: DiaryDeleteException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                DiaryDeleteException.DeleteFailure(date, e)
            )
        }

        try {
            releaseImageUriPermission(imageUriString)
        } catch (e: DiaryImageUriPermissionReleaseException) {
            // Uri権限の取り消しに失敗しても日記保存がメインの為、成功とみなす。
            Log.w(logTag, "${logMsg}警告_画像URI権限解放エラー", e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 指定された画像URI文字列に対する永続的な権限を解放する。
     *
     * URI文字列が `null` の場合は何も行わない。
     *
     * @param uriString 権限を解放する画像のURI文字列。
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
}
