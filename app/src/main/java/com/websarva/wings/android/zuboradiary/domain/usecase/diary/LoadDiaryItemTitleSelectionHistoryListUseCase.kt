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
     * @return タイトル選択履歴リストの読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] に [DiaryItemTitleSelectionHistoryList] を格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] に [DiaryItemTitleSelectionHistoryLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<
            UseCaseResult<DiaryItemTitleSelectionHistoryList, DiaryItemTitleSelectionHistoryLoadException>
    > {
        Log.i(logTag, "${logMsg}開始")

        return diaryRepository
            .loadDiaryItemTitleSelectionHistoryList(50, 0)
            .map {
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (読込件数: ${it.count()})"
                )
                UseCaseResult.Success(
                    DiaryItemTitleSelectionHistoryList(it)
                )
            }
            .catch {
                if (it is DataStorageException) {
                    UseCaseResult.Failure(
                        DiaryItemTitleSelectionHistoryLoadException.LoadFailure(it)
                    )
                } else {
                    throw it
                }
            }
    }
}

