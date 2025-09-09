package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryItemTitleSelectionHistoryLoadException
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * 日記項目のタイトル選択履歴リストを読み込むユースケース。
 *
 * ユーザーが過去に選択した日記のタイトル候補のリストを提供する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryItemTitleSelectionHistoryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記タイトル選択履歴読込_"

    /**
     * ユースケースを実行し、日記項目のタイトル選択履歴のFlowを返す。
     *
     * @return タイトル選択履歴リストを内包するFlowを [UseCaseResult.Success] に格納して返す。
     *   この内部の [Flow] は、実行中に [DiaryItemTitleSelectionHistoryLoadException] を
     *   スローする可能性がある。[UseCaseResult.Failure] は返さない。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<DiaryItemTitleSelectionHistoryList>> {
        Log.i(logTag, "${logMsg}開始")

        val flow =
            diaryRepository
                .loadDiaryItemTitleSelectionHistoryList(50, 0)
                .map { DiaryItemTitleSelectionHistoryList(it) }
                .catch {
                    throw if (it is DataStorageException) {
                        DiaryItemTitleSelectionHistoryLoadException.LoadFailure(it)
                    } else {
                        it
                    }
                }
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(flow)
    }
}

