package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryListFailedException
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class FetchWordSearchResultDiaryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        numLoadingItems: Int,
        loadingOffset: Int,
        searchWord: String
    ): DefaultUseCaseResult<List<WordSearchResultListItem>> {
        val logMsg = "日記リスト読込_"
        Log.i(logTag, "${logMsg}開始")
        require(numLoadingItems >= 1)
        require(loadingOffset >= 0)
        require(searchWord.isNotEmpty())

        try {
            val wordSearchResultList =
                diaryRepository.loadWordSearchResultDiaryList(
                    numLoadingItems,
                    loadingOffset,
                    searchWord
                )
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(wordSearchResultList)
        } catch (e: FetchDiaryListFailedException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
