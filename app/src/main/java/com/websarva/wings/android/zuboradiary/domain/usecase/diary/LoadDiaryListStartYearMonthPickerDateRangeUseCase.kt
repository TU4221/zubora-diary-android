package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListStartYearMonthPickerDateRangeLoadException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

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

    private val logTag = createLogTag()
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

        val dateRange =
            try {
                val newestDiary = diaryRepository.loadNewestDiary()
                val oldestDiary = diaryRepository.loadOldestDiary()
                SavedDiaryDateRange(newestDiary.date, oldestDiary.date)
            } catch (e: DataStorageException) {
                Log.e(logTag, "${logMsg}失敗_読込処理エラー", e)
                return UseCaseResult.Failure(
                    DiaryListStartYearMonthPickerDateRangeLoadException
                        .DiaryInfoLoadFailure(cause = e)
                )
            } catch (e: NotFoundException) {
                Log.i(logTag, "${logMsg}保存された日記が存在しない", e)
                SavedDiaryDateRange()
            }

        Log.i(logTag, "${logMsg}完了 (結果: $dateRange)")
        return UseCaseResult.Success(dateRange)
    }
}
