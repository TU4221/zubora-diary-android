package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class CheckUnloadedWordSearchResultsExistUseCase(
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        searchWord: String,
        numLoadedDiaries: Int
    ): DefaultUseCaseResult<Boolean> {
        val logMsg = "未読込ワード検索結果日記確認_"
        Log.i(logTag, "${logMsg}開始")

        return when (val result = countWordSearchResultsUseCase(searchWord)) {
            is UseCaseResult.Success -> {
                val numExistingDiaries = result.value
                val value =
                    if (numExistingDiaries <= 0) {
                        false
                    } else {
                        numLoadedDiaries < numExistingDiaries
                    }
                Log.i(logTag, "${logMsg}完了")
                UseCaseResult.Success(value)
            }
            is UseCaseResult.Failure -> {
                UseCaseResult.Failure(result.exception)
            }
        }
    }
}
