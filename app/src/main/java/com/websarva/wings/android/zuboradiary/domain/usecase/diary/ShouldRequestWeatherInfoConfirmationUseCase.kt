package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.logTag
import java.time.LocalDate

/**
 * 天気情報の取得確認ダイアログを表示する必要があるかどうかを判断するユースケース。
 *
 * 以下の条件をすべて満たす場合に、天気情報の取得確認が必要と判断する。
 * - 編集元の日記データから日付が変更されている。
 * - 天気情報を新たに取得する必要があると判断( [ShouldFetchWeatherInfoUseCase] )した場合。
 *
 * 日記編集画面で日付が変更された場合に、ユーザーに天気情報を取得するかどうかを確認するかの判定に使用される。
 *
 * @property shouldFetchWeatherInfoUseCase 天気情報を新たに取得する必要があるかどうかを判断するユースケース。
 */
internal class ShouldRequestWeatherInfoConfirmationUseCase(
    val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase
) {

    private val logMsg = "天気情報取得確認要否判定_"

    /**
     * ユースケースを実行し、天気情報の取得確認ダイアログを表示する必要があるかどうかを返す。
     *
     * @param inputDate 現在入力されている日記の日付。
     * @param previousDate 前回の日記の日付。新規作成時の初期表示時は `null` の場合がある。
     * @return 天気情報の取得確認が必要な場合は `true`、そうでない場合は `false` を [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?
    ): UseCaseResult.Success<Boolean> {
        Log.i(logTag, "${logMsg}開始 (入力日: $inputDate, 前回日: ${previousDate ?: "null"})")

        if (previousDate == null) {
            Log.i(logTag, "${logMsg}完了_確認不要 (理由: 前回日がnull(新規作成初期表示時))")
            return UseCaseResult.Success(false)
        }

        val shouldFetchResult = shouldFetchWeatherInfoUseCase(inputDate, previousDate)
        val shouldConfirm = shouldFetchResult.value

        Log.i(logTag, "${logMsg}完了 (結果: $shouldConfirm)")
        return UseCaseResult.Success(shouldConfirm)
    }
}
