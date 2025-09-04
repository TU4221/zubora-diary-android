package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 特定の日付の日記データを読み込むユースケース。
 *
 * 指定された日付に対応する日記情報をリポジトリから取得する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記取得_"

    /**
     * ユースケースを実行し、指定された日付の日記データを返す。
     *
     * @param date 読み込む日記の日付。
     * @return 読み込まれた日記データを [UseCaseResult.Success] に格納して返す。
     *   日記の読み込みに失敗した場合、または該当する日記が存在しない場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        date: LocalDate
    ): DefaultUseCaseResult<Diary> {
        Log.i(logTag, "${logMsg}開始 (日付: $date)")

        try {
            val diary = diaryRepository.loadDiary(date)

            if (diary == null) {
                Log.e(logTag, "${logMsg}失敗_該当日記が存在しない (日付: $date)")
                return UseCaseResult.Failure(
                    DiaryLoadException.DataNotFound(date)
                )
            }

            Log.i(logTag, "${logMsg}完了 (取得日記: $diary)")
            return UseCaseResult.Success(diary)
        } catch (e: DiaryLoadException) {
            Log.e(logTag, "${logMsg}失敗_読込処理エラー (日付: $date)", e)
            return UseCaseResult.Failure(e)
        }
    }
}
