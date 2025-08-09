package com.websarva.wings.android.zuboradiary.domain.usecase.text

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.ui.model.InputTextValidationResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ValidateInputTextUseCase {

    private val logTag = createLogTag()

    operator fun invoke(
        inputText: String
    ): UseCaseResult.Success<InputTextValidationResult> {
        val logMsg = "インプットテキスト有効確認_"
        Log.i(logTag, "${logMsg}開始")

        val validateResult =
            // 空欄
            if (inputText.isEmpty()) {
                InputTextValidationResult.InvalidEmpty

            // 先頭が空白文字(\\s)
            } else if (inputText.matches("\\s+.*".toRegex())) {
                InputTextValidationResult.InvalidInitialCharUnmatched

            // 有効
            } else {
                InputTextValidationResult.Valid
            }

        Log.i(logTag, "${logMsg}完了_${validateResult}")
        return UseCaseResult.Success(validateResult)
    }
}
