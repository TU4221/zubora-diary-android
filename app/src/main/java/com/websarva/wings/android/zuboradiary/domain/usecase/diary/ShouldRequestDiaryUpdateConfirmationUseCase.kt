package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryUpdateConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記の更新確認ダイアログを表示する必要があるかどうかを判断するユースケース。
 *
 * 以下の条件のいずれかに該当する場合に、更新確認が必要と判断する。
 * - 新規日記ではない、かつ入力された日付が元の日付と異なる場合で、入力された日付の日記が既に存在する場合。
 * - 新規日記の場合で、入力された日付の日記が既に存在する場合。
 *
 * 日記保存時に、同じ日付の日記が既に存在する場合に上書きしてよいかユーザーに確認するかの判定に使用される。
 *
 * @property doesDiaryExistUseCase 指定された日付の日記が存在するかどうかを確認するユースケース。
 */
internal class ShouldRequestDiaryUpdateConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "日記更新確認要否判定_"

    /**
     * ユースケースを実行し、日記の更新確認ダイアログを表示する必要があるかどうかを返す。
     *
     * @param inputDate 現在入力されている日記の日付。
     * @param originalDate 編集中の日記の元の日付。
     * @param isNewDiary 新規の日記作成かどうかを示すフラグ。`true`の場合は新規作成、`false`の場合は既存日記の更新。
     * @return 日記の更新確認が必要な場合は `true`、そうでない場合は `false` を [UseCaseResult.Success] に格納して返す。
     *   日記存在確認処理でエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        inputDate: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): UseCaseResult<Boolean, DiaryUpdateConfirmationCheckException> {
        Log.i(logTag, "${logMsg}開始 (入力日: $inputDate, 元の日付: $originalDate, 新規: $isNewDiary)")

        // 1. 新規日記ではなく、かつ入力された日付が元の日付と同じ場合は、確認不要
        //    (上書きではなく、単純な内容更新のため)
        if (!isNewDiary && inputDate == originalDate) {
            Log.i(logTag, "${logMsg}完了_確認不要 (理由: 既存日記で入力日と元の日付が同じ)")
            return UseCaseResult.Success(false)
        }

        // 上記以外の場合 (新規日記、または既存日記で日付が変更された場合)、
        // 入力された日付の日記が存在するかどうかで判断
        return when (val result = doesDiaryExistUseCase(inputDate)) {
            is UseCaseResult.Success -> {
                val shouldConfirm = result.value
                Log.i(logTag, "${logMsg}完了 (結果: $shouldConfirm)")
                UseCaseResult.Success(shouldConfirm)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_日記存在確認エラー", result.exception)
                UseCaseResult.Failure(
                    DiaryUpdateConfirmationCheckException.CheckFailure(result.exception)
                )
            }
        }
    }
}
