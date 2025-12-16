package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.DiaryListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.DiaryListLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.DiaryListRefreshException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import java.time.LocalDate
import javax.inject.Inject

/**
 * 既存の日記リストを再読み込みし、フッターを更新するユースケース。
 *
 * 日記の新規保存、編集、削除後にリストを最新の状態に更新するために使用される。
 * 読み込むアイテム数は、現在のリストのアイテム数、または最低読み込み数のうち大きい方が採用される。
 *
 * @property loadDiaryListUseCase 日記リストを読み込むためのユースケース。
 * @property updateDiaryListFooterUseCase 日記リストのフッターを更新するためのユースケース。
 */
internal class RefreshDiaryListUseCase @Inject constructor(
    private val loadDiaryListUseCase: LoadDiaryListUseCase,
    private val updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
) {

    private val logMsg = "日記リスト再読込_"

    /**
     * ユースケースを実行し、日記リストを再読み込みし、フッターを更新した新しいリストを返す。
     *
     * @param currentList 現在表示されている日記のリスト。
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい日記リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListRefreshException] を格納して返す。
     */
    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListRefreshException> {
        Log.i(logTag, "${logMsg}開始 (現リスト件数: ${currentList.countDiaries()}," +
                " 読込予定件数: ${currentList.countDiaries()}, 開始日: ${startDate ?: "全期間"})")




        return try {
            var numLoadItems = currentList.countDiaries()

            // HACK:リストが空の状態、又は画面サイズより少ないアイテム数で日記を追加し、
            //      リスト画面に戻った際に以下の問題が発生する回避策。
            //      問題点:
            //      1. 新しく追加された日記が表示されず、追加前のアイテム数でリストが描画される。
            //      2. スクロールによる追加読み込みも機能しない。
            //      対策:
            //      NUM_LOAD_ITEMS に満たない場合は、強制的に NUM_LOAD_ITEMS 分の読み込みを行うことで、
            //      新規追加されたアイテムの表示とスクロール更新を可能にする。
            if (numLoadItems < NUM_LOAD_ITEMS) {
                numLoadItems = NUM_LOAD_ITEMS
            }

            val loadedDiaryList = loadDiaryList(numLoadItems, startDate)
            val resultList = updateDiaryListFooter(loadedDiaryList, startDate)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: DiaryListLoadException) {
            when (e) {
                is DiaryListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_再読込エラー", e)
                    UseCaseResult.Failure(DiaryListRefreshException.RefreshFailure(e))
                }
                is DiaryListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(DiaryListRefreshException.Unknown(e))
                }
            }
        } catch (e: DiaryListFooterUpdateException) {
            when (e) {
                is DiaryListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(DiaryListRefreshException.RefreshFailure(e))
                }
                is DiaryListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(DiaryListRefreshException.Unknown(e))
                }
            }
        }
    }

    /**
     * 指定されたアイテム数で日記リストを読み込む。オフセットは0で固定される。
     *
     * @param numLoadItems 読み込む日記のアイテム数。
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 読み込まれた日記のリスト。
     * @throws DiaryListLoadException 日記の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        numLoadItems: Int,
        startDate: LocalDate?
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        val result =
            loadDiaryListUseCase(
                numLoadItems,
                0, // 再読み込みのためオフセットは0
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
