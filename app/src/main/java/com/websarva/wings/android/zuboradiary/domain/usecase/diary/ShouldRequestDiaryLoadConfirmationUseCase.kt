package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import java.time.LocalDate

/**
 * 日記の読み込み確認ダイアログを表示する必要があるかどうかを判断するユースケース。
 *
 * 以下の条件のいずれにも該当しない場合に、指定された日付の日記が存在すれば、
 * 読み込み確認が必要と判断する。
 * - 入力された日付が前回の日付と同じ場合。
 * - 既存日記の編集中で、入力された日付が元の日記の日付と同じ場合。
 *
 * 日記編集画面で日付が変更された際に、その日付の日記を読み込むかユーザーに確認するかの判定に使用される。
 *
 * @property doesDiaryExistUseCase 指定された日付の日記が存在するかどうかを確認するユースケース。
 */
internal class ShouldRequestDiaryLoadConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logMsg = "日記読込確認要否判定_"

    /**
     * ユースケースを実行し、日記の読み込み確認ダイアログを表示する必要があるかどうかを返す。
     *
     * @param inputDate 現在入力されている日記の日付。
     * @param previousDate 前回の日記の日付。新規作成時の初期表示時は `null` の場合がある。
     * @param originalDate 編集中の日記の元の日付。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。`true`の場合は新規作成、`false`の場合は既存日記の更新。
     * @return 日記の読み込み確認が必要な場合は `true`、そうでない場合は `false` を [UseCaseResult.Success] に格納して返す。
     *   確認に失敗した場合は [UseCaseResult.Failure] に [DiaryLoadConfirmationCheckException] を格納して返す。
     */
    suspend operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): UseCaseResult<Boolean, DiaryLoadConfirmationCheckException> {
        Log.i(logTag, "${logMsg}開始 (入力日: $inputDate," +
                " 前回日: ${previousDate ?: "null"}, 元日付: $originalDate, 新規: $isNewDiary)")

        // 1. 入力された日付が前回の日付と同じ場合は、確認不要
        if (inputDate == previousDate) {
            Log.i(logTag, "${logMsg}完了_確認不要 (理由: 入力日と前回日が同一)")
            return UseCaseResult.Success(false)
        }
        // 2. 既存日記の編集中で、入力された日付が元の日記の日付と同じ場合は、確認不要
        if (!isNewDiary && inputDate == originalDate) {
            Log.i(logTag, "${logMsg}完了_確認不要 (理由: 既存日記編集中で入力日と元日付が同一)")
            return UseCaseResult.Success(false)
        }

        // 上記以外の場合、入力された日付の日記が存在するかどうかで判断
        return when (val result = doesDiaryExistUseCase(inputDate)) {
            is UseCaseResult.Success -> {
                val shouldConfirm = result.value
                Log.i(logTag, "${logMsg}完了 (結果: $shouldConfirm)")
                UseCaseResult.Success(shouldConfirm)
            }
            is UseCaseResult.Failure -> {
                val wrappedException =
                    when (val e = result.exception) {
                        is DiaryExistenceCheckException.CheckFailure -> {
                            Log.e(logTag, "${logMsg}失敗_日記存在確認エラー", e)
                            DiaryLoadConfirmationCheckException.CheckFailure(e)
                        }
                        is DiaryExistenceCheckException.Unknown -> {
                            Log.e(logTag, "${logMsg}失敗_原因不明", e)
                            DiaryLoadConfirmationCheckException.Unknown(e)
                        }
                    }
                UseCaseResult.Failure(wrappedException)
            }
        }
    }
}
