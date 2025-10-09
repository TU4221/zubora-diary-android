package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 日記を保存せずに終了する際の確認ダイアログを表示する必要があるかどうかを判断するユースケース。
 *
 * 編集された日記の内容と元の内容を比較し(ログ記録用の情報は除く)、内容が異なる場合に確認が必要と判断する。
 *
 * 日記編集画面から離脱する際に、未保存の変更がある場合にユーザーに警告するための判定に使用される。
 */
internal class ShouldRequestExitWithoutDiarySaveConfirmationUseCase {

    private val logTag = createLogTag()
    private val logMsg = "日記未保存終了確認要否判定_"

    /**
     * ユースケースを実行し、日記を保存せずに終了する際の確認ダイアログを表示する必要があるかどうかを返す。
     *
     * @param editedDiary 現在編集されている日記データ。
     * @param originalDiary 編集前の元の日記データ。
     * @return ログ記録情報を除いて日記内容に差異がある場合は `true`、そうでない場合は `false` を
     *   [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(
        editedDiary: Diary,
        originalDiary: Diary
    ): UseCaseResult.Success<Boolean> {
        Log.i(logTag, "${logMsg}開始 (編集後日付: ${editedDiary.date}, 元日付: ${originalDiary.date})")

        val boolean = !editedDiary.isContentEqualToIgnoringLog(originalDiary)
        Log.i(logTag, "${logMsg}完了 (結果: $boolean)")
        return UseCaseResult.Success(boolean)
    }
}
