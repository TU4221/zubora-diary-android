package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 天気情報を新たに取得する必要があるかどうかを判断するユースケース。
 *
 * 日記新規作成時、又は入力された日付が、入力前の日付と異なる場合に天気情報を取得する必要があると判断する。
 * 日記編集画面で日付が変更された際に、新しい日付の天気情報を取得するかどうかの判定に使用される。
 */
internal class ShouldFetchWeatherInfoUseCase {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得要否判定_"

    /**
     * ユースケースを実行し、天気情報を新たに取得する必要があるかどうかを返す。
     *
     * @param inputDate 現在入力されている日記の日付。
     * @param previousDate 前回の日記の日付。`null` の場合は、常に取得が必要と判断される（新規作成時など）。
     * @return 天気情報を取得する必要がある場合は `true`、そうでない場合は `false` を [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?
    ): UseCaseResult.Success<Boolean> {
        Log.i(logTag, "${logMsg}開始 (入力日: $inputDate, 前回日: ${previousDate ?: "null"})")

        // previousDate が null の場合 (例: 新規日記作成時) は、inputDate とは必ず異なるため、
        // 天気情報を取得する必要があると判断される。
        // previousDate が null でない場合は、inputDate と比較する。
        val shouldFetch = inputDate != previousDate

        Log.i(logTag, "${logMsg}完了 (結果: $shouldFetch)")
        return UseCaseResult.Success(shouldFetch)
    }
}
