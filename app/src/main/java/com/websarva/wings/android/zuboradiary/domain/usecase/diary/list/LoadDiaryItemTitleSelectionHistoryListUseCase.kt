package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list

import android.util.Log
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.DiaryItemTitleSelectionHistoryListLoadException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 日記項目のタイトル選択履歴リストを読み込むユースケース。
 *
 * ユーザーが過去に選択した日記のタイトル候補のリストを提供する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryItemTitleSelectionHistoryListUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {

    private val logMsg = "日記タイトル選択履歴読込_"

    /**
     * ユースケースを実行し、日記項目のタイトル選択履歴のFlowを返す。
     *
     * @return タイトル選択履歴リストの読み込み結果を [com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult] へ [kotlinx.coroutines.flow.Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult.Success] に [com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList] を格納して返す。
     *   失敗した場合は、[com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult.Failure] に [DiaryItemTitleSelectionHistoryListLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<
            UseCaseResult<DiaryItemTitleSelectionHistoryList, DiaryItemTitleSelectionHistoryListLoadException>
            > {
        Log.i(logTag, "${logMsg}開始")

        return diaryRepository
            .loadDiaryItemTitleSelectionHistoryList(50, 0)
            .map {
                Log.d(
                    logTag,
                    "${logMsg}成功 (読込件数: ${it.count()})"
                )
                UseCaseResult.Success(
                    DiaryItemTitleSelectionHistoryList(it)
                )
            }
            .catch {
                val wrappedException =
                    when (it) {
                        is UnknownException -> {
                            Log.d(logTag, "${logMsg}読込失敗_原因不明")
                            DiaryItemTitleSelectionHistoryListLoadException.Unknown(it)
                        }
                        is DomainException -> {
                            Log.d(logTag, "${logMsg}失敗_読込エラー")
                            DiaryItemTitleSelectionHistoryListLoadException.LoadFailure(it)
                        }
                        else -> throw it
                    }
                UseCaseResult.Failure(wrappedException)
            }
    }
}
