package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListLoadException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.logTag
import java.time.LocalDate

/**
 * 既存の日記リストに追加の日記データを読み込み、結合してフッターを更新するユースケース。
 *
 * 日記リストの末尾までスクロールした際に、追加の日記を読み込むために使用される。
 *
 * @property loadDiaryListUseCase 日記リストを読み込むためのユースケース。
 * @property updateDiaryListFooterUseCase 日記リストのフッターを更新するためのユースケース。
 */
internal class LoadAdditionDiaryListUseCase(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logMsg = "追加日記リスト読込_"

    /**
     * ユースケースを実行し、現在のリストに追加の日記を読み込み、フッターを更新した新しいリストを返す。
     *
     * @param currentList 現在表示されている日記のリスト。
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい日記リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListAdditionLoadException] を格納して返す。
     */
    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListAdditionLoadException> {
        Log.i(logTag, "${logMsg}開始 (現リスト件数: ${currentList.countDiaries()}, 開始日: ${startDate ?: "全期間"})")


        return try {
            val loadedDiaryList =
                loadDiaryList(
                    currentList.countDiaries(),
                    startDate
                )
            val combinedList = currentList.combineDiaryLists(loadedDiaryList)
            val resultList =updateDiaryListFooter(combinedList, startDate)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: DiaryListLoadException) {
            when (e) {
                is DiaryListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_追加日記リスト読込エラー", e)
                    UseCaseResult.Failure(
                        DiaryListAdditionLoadException.LoadFailure(e)
                    )
                }
                is DiaryListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        DiaryListAdditionLoadException.Unknown(e)
                    )
                }
            }
        } catch (e: DiaryListFooterUpdateException) {
            when (e) {
                is DiaryListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(
                        DiaryListAdditionLoadException.LoadFailure(e)
                    )
                }
                is DiaryListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        DiaryListAdditionLoadException.Unknown(e)
                    )
                }
            }
        }
    }

    /**
     * 指定されたオフセットから追加の日記リストを読み込む。
     *
     * @param loadOffset 読み込みを開始するオフセット（既に読み込まれている日記の数）。
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 読み込まれた日記のリスト。
     * @throws DiaryListLoadException 日記の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        loadOffset: Int,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                NUM_LOAD_ITEMS,
                loadOffset,
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
