package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.diary.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListStartYearMonthPickerDateRangeLoadException
import com.websarva.wings.android.zuboradiary.utils.logTag

/**
 * 日記リストの先頭年月を選択するピッカーに表示するための日付範囲を読み込むユースケース。
 *
 * 保存されている日記の中で最も新しい日付と最も古い日付を取得し、ピッカーの選択可能な範囲として提供する。
 * 日記データが存在しない場合や、データの取得に失敗した場合は、適切なフォールバック処理やエラー通知を行う。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryListStartYearMonthPickerDateRangeUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logMsg = "日記リスト先頭年月選択範囲の読込_"

    /**
     * ユースケースを実行し、日記リストの先頭年月ピッカー用の日付範囲情報を返す。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に日付範囲( [SavedDiaryDateRange] )を格納して返す。
     *   日記データが存在しない場合も、デフォルトの日付範囲 ( [SavedDiaryDateRange] のデフォルトコンストラクタ値) を格納。
     *   失敗した場合は [UseCaseResult.Failure] に [DiaryListStartYearMonthPickerDateRangeLoadException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<
            SavedDiaryDateRange, DiaryListStartYearMonthPickerDateRangeLoadException> {
        Log.i(logTag, "${logMsg}開始")

        return try {
            val newestDiary = diaryRepository.loadNewestDiary()
            val oldestDiary = diaryRepository.loadOldestDiary()

            val dateRange =
                SavedDiaryDateRange(
                    newestDiary.date,
                    oldestDiary.date
                )
            Log.i(logTag, "${logMsg}完了 (結果: $dateRange)")
            UseCaseResult.Success(dateRange)
        } catch (e: ResourceNotFoundException) {
            Log.i(logTag, "${logMsg}完了_保存された日記が存在しない為、今日の日付のみを範囲とする", e)
            UseCaseResult.Success(SavedDiaryDateRange.empty())
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryListStartYearMonthPickerDateRangeLoadException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_読込エラー", e)
            UseCaseResult.Failure(
                DiaryListStartYearMonthPickerDateRangeLoadException
                    .DiaryInfoLoadFailure(e)
            )
        }
    }
}
