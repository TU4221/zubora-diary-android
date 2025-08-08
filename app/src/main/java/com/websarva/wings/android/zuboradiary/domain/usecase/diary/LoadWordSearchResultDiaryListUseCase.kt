package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class LoadWordSearchResultDiaryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        numLoadItems: Int,
        loadOffset: Int,
        searchWord: String
    ): DefaultUseCaseResult<List<WordSearchResultListItem>> {
        val logMsg = "日記リスト読込_"
        Log.i(logTag, "${logMsg}開始")
        require(numLoadItems >= 1)
        require(loadOffset >= 0)
        require(searchWord.isNotEmpty())

        try {
            val wordSearchResultList =
                diaryRepository.loadWordSearchResultDiaryList(
                    numLoadItems,
                    loadOffset,
                    searchWord
                )
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(wordSearchResultList)
        } catch (e: DiaryListLoadFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
