package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryCountFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class CountWordSearchResultsUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        searchWord: String
    ): DefaultUseCaseResult<Int> {
        val logMsg = "ワード検索結果日記総数取得_"
        Log.i(logTag, "${logMsg}開始")

        return try {
            val numDiaries =diaryRepository.countWordSearchResults(searchWord)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(numDiaries)
        } catch (e: DiaryCountFailureException) {
            Log.e(logTag, "${logMsg}失敗")
            UseCaseResult.Failure(e)
        }
    }
}
