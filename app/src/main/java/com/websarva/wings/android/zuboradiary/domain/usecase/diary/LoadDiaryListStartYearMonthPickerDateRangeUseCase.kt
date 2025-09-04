package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadException
import com.websarva.wings.android.zuboradiary.domain.model.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.LoadDiaryListStartYearMonthPickerDateRangeUseCaseException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記リストの先頭年月を選択するピッカーに表示するための日付範囲を読み込むユースケース。
 *
 * 保存されている日記の中で最も新しい日付と最も古い日付を取得し、ピッカーの選択可能な範囲として提供する。
 * 日記データが存在しない場合や、データの取得に失敗した場合は、適切なフォールバック処理やエラー通知を行う。
 *
 * @property loadNewestDiaryUseCase 最新の日記を読み込むためのユースケース。
 * @property loadOldestDiaryUseCase 最古の日記を読み込むためのユースケース。
 */
internal class LoadDiaryListStartYearMonthPickerDateRangeUseCase(
    private val loadNewestDiaryUseCase: LoadNewestDiaryUseCase,
    private val loadOldestDiaryUseCase: LoadOldestDiaryUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "日記リスト先頭年月選択範囲の読込_"

    /**
     * ユースケースを実行し、日記リストの先頭年月ピッカー用の日付範囲情報を返す。
     *
     * @return 日付範囲の取得に成功した場合は [UseCaseResult.Success] にその [SavedDiaryDateRange] オブジェクトを格納して返す。
     *   日記データが存在しない場合も、デフォルトの日付範囲 ([SavedDiaryDateRange] のデフォルトコンストラクタ値) を格納。
     *   データの読み込みアクセスに失敗した場合は [UseCaseResult.Failure] に [LoadDiaryListStartYearMonthPickerDateRangeUseCaseException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<
            SavedDiaryDateRange, LoadDiaryListStartYearMonthPickerDateRangeUseCaseException> {
        Log.i(logTag, "${logMsg}開始")

        val dateRange =
            try {
                val newestDiaryDate =loadNewestDiaryDate()
                val oldestDiaryDate =loadOldestDiaryDate()
                SavedDiaryDateRange(newestDiaryDate, oldestDiaryDate)
            } catch (e: DiaryLoadException.AccessFailure) {
                Log.e(logTag, "${logMsg}失敗_読込処理エラー", e)
                return UseCaseResult.Failure(
                    LoadDiaryListStartYearMonthPickerDateRangeUseCaseException(cause = e)
                )
            } catch (e: DiaryLoadException.DataNotFound) {
                Log.i(logTag, "${logMsg}完了_保存された日記が存在しない")
                SavedDiaryDateRange()
            }

        Log.i(logTag, "${logMsg}完了 (結果: $dateRange)")
        return UseCaseResult.Success(dateRange)
    }

    /**
     * 最新の日記の日付を読み込む。
     *
     * @return 読み込まれた最新の日記の日付。
     * @throws DiaryLoadException 最新の日記の読み込みに失敗した場合 ([LoadOldestDiaryUseCase] からスローされる例外)。
     */
    private suspend fun loadNewestDiaryDate(): LocalDate {
        return when (val result = loadNewestDiaryUseCase()) {
            is UseCaseResult.Success -> {
                result.value.date
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 最古の日記の日付を読み込む。
     *
     * @return 読み込まれた最古の日記の日付。
     * @throws DiaryLoadException 最古の日記の読み込みに失敗した場合 ([LoadOldestDiaryUseCase] からスローされる例外)。
     */
    private suspend fun loadOldestDiaryDate(): LocalDate {
        return when (val result = loadOldestDiaryUseCase()) {
            is UseCaseResult.Success -> {
                result.value.date
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
