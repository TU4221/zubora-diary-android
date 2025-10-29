package com.websarva.wings.android.zuboradiary.domain.usecase.text

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.ui.model.result.InputTextValidationState
import com.websarva.wings.android.zuboradiary.core.utils.logTag

// TODO:戻り値がUiModelの為修正する
/**
 * 入力されたテキストの有効性を検証するユースケース。
 *
 * テキストが空でないか、先頭が空白文字でないかをチェックする。
 */
internal class ValidateInputTextUseCase {

    private val logMsg = "インプットテキスト有効確認_"

    /**
     * ユースケースを実行し、入力されたテキストの有効性を検証する。
     *
     * @param inputText 検証するテキスト文字列。
     * @return 検証結果 ([InputTextValidationState]) を [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(
        inputText: String
    ): UseCaseResult.Success<InputTextValidationState> {
        Log.i(logTag, "${logMsg}開始 (入力テキスト: \"$inputText\")")

        val validateResult =
            // 空欄
            if (inputText.isEmpty()) {
                InputTextValidationState.InvalidEmpty

                // 先頭が空白文字(\\s)
            } else if (inputText.matches("\\s+.*".toRegex())) {
                InputTextValidationState.InvalidInitialCharUnmatched

                // 有効
            } else {
                InputTextValidationState.Valid
            }

        Log.i(logTag, "${logMsg}完了 (結果: $validateResult)")
        return UseCaseResult.Success(validateResult)
    }
}
