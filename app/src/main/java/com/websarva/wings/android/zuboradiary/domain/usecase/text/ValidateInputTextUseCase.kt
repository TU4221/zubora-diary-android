package com.websarva.wings.android.zuboradiary.domain.usecase.text

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.common.InputTextValidation
import javax.inject.Inject

/**
 * 入力されたテキストの有効性を検証するユースケース。
 *
 * テキストが空でないか、先頭が空白文字でないかをチェックする。
 */
internal class ValidateInputTextUseCase @Inject constructor() {

    private val logMsg = "インプットテキスト有効確認_"

    /**
     * ユースケースを実行し、入力されたテキストの有効性を検証する。
     *
     * @param inputText 検証するテキスト文字列。
     * @return 検証結果 ([InputTextValidation]) を [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(
        inputText: String
    ): UseCaseResult.Success<InputTextValidation> {
        Log.i(logTag, "${logMsg}開始 (入力テキスト: \"$inputText\")")

        val validateResult =
            // 空欄
            if (inputText.isEmpty()) {
                InputTextValidation.Empty

                // 先頭が空白文字(\\s)
            } else if (inputText.matches("\\s+.*".toRegex())) {
                InputTextValidation.InitialCharUnmatched

                // 有効
            } else {
                InputTextValidation.Valid
            }

        Log.i(logTag, "${logMsg}完了 (結果: $validateResult)")
        return UseCaseResult.Success(validateResult)
    }
}
