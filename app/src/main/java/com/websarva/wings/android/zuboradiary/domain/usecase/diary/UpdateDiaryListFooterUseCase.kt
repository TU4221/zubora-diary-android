package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記リストのフッターを更新するユースケース。
 *
 * 未読込の日記が存在するかどうかを確認し、存在しない場合はリストのフッターを
 * プログレスインディケーターから「これ以上の日記はありません」というメッセージに置き換える。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class UpdateDiaryListFooterUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記リストフッター更新_"

    /**
     * ユースケースを実行し、必要に応じてフッターが更新された日記リストを返す。
     *
     * @param list フッターを更新する対象の日記リスト。
     * @param startDate 日記の読み込み開始日。未読込の日記存在確認に使用される。`null` の場合は全期間が対象。
     * @return 処理に成功した場合は [UseCaseResult.Success] に
     *   更新された日記リスト、または元のリスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListFooterUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListFooterUpdateException> {
        Log.i(logTag, "${logMsg}開始 (リスト件数: ${list.countDiaries()}, 開始日: ${startDate ?: "全期間"})")

        return try {
            val numLoadedDiaries = list.countDiaries()
            val unloadedDiariesExist = checkUnloadedDiariesExist(numLoadedDiaries, startDate)
            val replacedDiaryList =
                if (unloadedDiariesExist) {
                    Log.i(logTag, "${logMsg}完了_未読込の日記あり (リスト変更なし)")
                    list
                } else {
                    Log.i(logTag, "${logMsg}完了_未読込の日記なし (フッターをメッセージに置換)")
                    list.replaceFooterWithNoDiaryMessage()
                }
            UseCaseResult.Success(replacedDiaryList)
        } catch (e: DiaryListFooterUpdateException) {
            UseCaseResult.Failure(e)
        }
    }

    /**
     * 未読み込みの日記が存在するかどうかを確認する。
     *
     * 読み込み済みの日記数と全日記数を比較し、未読み込みの日記が存在するかを判定する。
     *
     * @param numLoadedDiaries 現在リストに読み込まれている日記の数。
     * @param startDate 日記の総数をカウントする際の開始日。`null` の場合は全期間が対象。
     * @return 未読み込みの日記が存在すれば `true`、そうでなければ `false`。
     * @throws DiaryListFooterUpdateException.UpdateFailure 日記の総数の取得に失敗した場合。
     */
    private suspend fun checkUnloadedDiariesExist(
        numLoadedDiaries: Int,
        startDate: LocalDate?
    ): Boolean {
        val numExistingDiaries =
            try {
                diaryRepository.countDiaries(startDate)
            } catch (e: RepositoryException) {
                Log.e(logTag, "${logMsg}失敗_未読込の日記存在確認エラー", e)
                throw DiaryListFooterUpdateException.UpdateFailure(e)
            }
        return if (numExistingDiaries <= 0) {
                false
            } else {
                numLoadedDiaries < numExistingDiaries
            }
    }
}
