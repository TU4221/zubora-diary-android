package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LoadDiaryItemTitleSelectionHistoryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    /**
     * @return [UseCaseResult.Success]
     * この内部の [Flow] は、実行中に [DiaryItemTitleSelectionHistoryLoadFailureException] を
     * スローする可能性があります。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<DiaryItemTitleSelectionHistoryList>> {
        val logMsg = "日記タイトル選択履歴読込_"
        Log.i(logTag, "${logMsg}開始")

        val flow =
            diaryRepository
                .loadDiaryItemTitleSelectionHistoryList(50, 0)
                .map { DiaryItemTitleSelectionHistoryList(it) }
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(flow)
    }
}
