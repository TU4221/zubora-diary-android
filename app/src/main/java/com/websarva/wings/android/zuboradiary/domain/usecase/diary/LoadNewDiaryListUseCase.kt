package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 新規に日記リストを読み込み、フッターを更新するユースケース。
 *
 * 日記リスト初期表示時に、最初の日記リストを取得するために使用される。
 *
 * @property loadDiaryListUseCase 日記リストを読み込むためのユースケース。
 * @property updateDiaryListFooterUseCase 日記リストのフッターを更新するためのユースケース。
 */
internal class LoadNewDiaryListUseCase(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "新規日記リスト読込_"

    /**
     * ユースケースを実行し、新規に日記リストを読み込み、フッターを更新したリストを返す。
     *
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい日記リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListNewLoadException] を格納して返す。
     */
    suspend operator fun invoke(
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListNewLoadException> {
        Log.i(logTag, "${logMsg}開始")

        return try {
            val loadedDiaryList = loadDiaryList(startDate)
            val resultList = updateDiaryListFooter(loadedDiaryList, startDate)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: DiaryListLoadException) {
            when (e) {
                is DiaryListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_新規読込エラー", e)
                    UseCaseResult.Failure(DiaryListNewLoadException.LoadFailure(e))
                }
                is DiaryListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(DiaryListNewLoadException.Unknown(e))
                }
            }
        } catch (e: DiaryListFooterUpdateException) {
            when (e) {
                is DiaryListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(DiaryListNewLoadException.LoadFailure(e))
                }
                is DiaryListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(DiaryListNewLoadException.Unknown(e))
                }
            }
        }
    }

    /**
     * 新規の日記リストを読み込む。
     *
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 読み込まれた日記のリスト。
     * @throws DiaryListLoadException 日記の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                NUM_LOAD_ITEMS,
                0, // 新規読み込みのためオフセットは0
                startDate
            )
        return when (result) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 指定された日記リストのフッター情報を更新する。
     *
     * @param list フッターを更新する対象の日記リスト。
     * @param startDate 日記を読み込む期間の開始日（フッターの内容決定に使用される場合がある）。
     *                  `null` の場合は全期間を対象とする。
     * @return フッターが更新された日記リスト。
     * @throws DiaryListFooterUpdateException フッターの更新処理に失敗した場合。
     */
    private suspend fun updateDiaryListFooter(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        return when (val result = updateDiaryListFooterUseCase(list, startDate)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
