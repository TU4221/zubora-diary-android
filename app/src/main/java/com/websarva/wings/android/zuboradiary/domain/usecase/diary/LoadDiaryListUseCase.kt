package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListLoadException
import com.websarva.wings.android.zuboradiary.domain.mapper.toDiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 指定された条件に基づいて日記リストを読み込むユースケース。
 *
 * 読み込むアイテム数、オフセット、および開始日を指定して日記データを取得し、
 * 年月ごとにグループ化されたリスト形式に変換する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記リスト読込_"

    /**
     * ユースケースを実行し、指定された条件で日記リストを読み込み、年月に基づいてグループ化されたリストを返す。
     *
     * @param numLoadItems 一度に読み込む日記のアイテム数。1以上の値を指定する必要がある。
     * @param loadOffset 読み込みを開始するオフセット。0以上の値を指定する必要がある。
     * @param startDate 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 処理に成功した場合は [UseCaseResult.Success] に日記リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListLoadException] を格納して返す。
     * @throws IllegalArgumentException `numLoadItems`が1未満、`loadOffset`が負数の場合にスローされる。
     */
    suspend operator fun invoke(
        numLoadItems: Int,
        loadOffset: Int,
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListLoadException> {
        Log.i(logTag, "${logMsg}開始 (読込件数: $numLoadItems, オフセット: $loadOffset, 開始日: ${startDate ?: "全期間"})")

        require(numLoadItems >= 1) {
            "${logMsg}不正引数_読み込みアイテム数は1以上必須 (読込件数: $numLoadItems)"
        }
        require(loadOffset >= 0) {
            "${logMsg}不正引数_読み込みオフセットは0以上必須 (オフセット: $loadOffset)"
        }

        return try {
            val loadedDiaryList =
                diaryRepository.loadDiaryList(
                    numLoadItems,
                    loadOffset,
                    startDate
                )
            val convertedList = convertDiaryYearMonthList(loadedDiaryList)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${convertedList.countDiaries()})")
            UseCaseResult.Success(convertedList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_読込エラー", e)
            UseCaseResult.Failure(DiaryListLoadException.LoadFailure(e))
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(DiaryListLoadException.Unknown(e))
        }
    }

    /**
     * 日記アイテムのリストを、年月でグループ化された [DiaryYearMonthList] に変換する。
     *
     * 入力リストが空の場合は、空の [DiaryYearMonthList] を返す。
     *
     * @param diaryList 変換対象の日記アイテムのリスト。
     * @return 年月でグループ化された日記リスト。
     */
    private fun convertDiaryYearMonthList(
        diaryList: List<DiaryDayListItem.Standard>
    ): DiaryYearMonthList<DiaryDayListItem.Standard> {
        if (diaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayList = DiaryDayList(diaryList)
        return diaryDayList.toDiaryYearMonthList()
    }
}
