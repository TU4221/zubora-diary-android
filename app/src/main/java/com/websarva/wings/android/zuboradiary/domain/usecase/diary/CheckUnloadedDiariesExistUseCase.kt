package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.UnloadedDiariesExistCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * まだ読み込まれていない日記が存在するかどうかを確認するユースケース。
 *
 * このユースケースは、指定された開始日以降（または全期間）の日記の総数と、
 * 現在UI上に読み込まれている日記の数を比較し、未読込の日記が存在するかを判定する。
 * 追加のデータをロードする必要があるかどうかを判断するために使用される。
 *
 * @property countDiariesUseCase 既存の日記の総数を取得するためのユースケース。
 */
internal class CheckUnloadedDiariesExistUseCase(
    private val countDiariesUseCase: CountDiariesUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "未読込日記確認_"

    /**
     * ユースケースを実行し、未読込の日記が存在するかどうかを返す。
     *
     * @param numLoadedDiaries 現在UIに読み込まれている日記の数。
     * @param startDate 日記の存在を確認する期間の開始日。`null` の場合は全期間の日記を対象とする。
     * @return 未読込の日記が存在する場合は [UseCaseResult.Success] に `true` を、
     *         存在しない場合や確認対象の日記が0件の場合は `false` を格納して返す。
     *   日記数のカウントに失敗した場合は [UseCaseResult.Failure] を返す。
     *
     */
    suspend operator fun invoke(
        numLoadedDiaries: Int,
        startDate: LocalDate?
    ): UseCaseResult<Boolean, UnloadedDiariesExistCheckException> {
        Log.i(logTag, "${logMsg}開始 (読込済件数: $numLoadedDiaries, 開始日: ${startDate ?: "全期間"})")

        return when (val result = countDiariesUseCase(startDate)) {
            is UseCaseResult.Success -> {
                val numExistingDiaries = result.value
                val unloadedDiariesExist =
                    if (numExistingDiaries <= 0) {
                        false
                    } else {
                        numLoadedDiaries < numExistingDiaries
                    }
                Log.i(logTag, "${logMsg}完了 (結果: $unloadedDiariesExist)")
                UseCaseResult.Success(unloadedDiariesExist)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_カウント処理エラー")
                UseCaseResult.Failure(
                    UnloadedDiariesExistCheckException.CheckFailure(result.exception)
                )
            }
        }
    }
}
