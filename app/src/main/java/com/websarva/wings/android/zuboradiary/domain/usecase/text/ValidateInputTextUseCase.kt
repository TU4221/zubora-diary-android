package com.websarva.wings.android.zuboradiary.domain.usecase.text

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.ui.model.InputTextValidateResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ValidateInputTextUseCase {

    private val logTag = createLogTag()

    operator fun invoke(
        inputText: String
    ): UseCaseResult.Success<InputTextValidateResult> {
        val logMsg = "インプットテキスト有効確認_"
        Log.i(logTag, "${logMsg}開始")

        val validateResult =
            // 空欄
            if (inputText.isEmpty()) {
                InputTextValidateResult.InvalidEmpty

            // 先頭が空白文字(\\s)
            } else if (inputText.matches("\\s+.*".toRegex())) {
                InputTextValidateResult.InvalidInitialCharUnmatched

            // 有効
            } else {
                InputTextValidateResult.Valid
            }

        Log.i(logTag, "${logMsg}完了_${validateResult}")
        return UseCaseResult.Success(validateResult)
    }
}
