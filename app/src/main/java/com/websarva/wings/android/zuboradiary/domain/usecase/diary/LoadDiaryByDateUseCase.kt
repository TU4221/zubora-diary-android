package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 特定の日付の日記データを読み込むユースケース。
 *
 * 指定された日付に対応する日記情報をリポジトリから取得する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryByDateUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記取得_"

    /**
     * ユースケースを実行し、指定された日付の日記データを返す。
     *
     * @param date 読み込む日記の日付。
     * @return 処理に成功した場合は [UseCaseResult.Success] に日記データ( [Diary] )を格納して返す。
     *   失敗した場合、または該当する日記が存在しない場合は [UseCaseResult.Failure] に [DiaryLoadByDateException] を格納して返す。
     */
    suspend operator fun invoke(
        date: LocalDate
    ): UseCaseResult<Diary, DiaryLoadByDateException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date)")

        return try {
            val id = diaryRepository.loadDiaryId(date)
            val diary = diaryRepository.loadDiary(id)
            Log.i(logTag, "${logMsg}完了 (取得日記: $diary)")
            UseCaseResult.Success(diary)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明")
            UseCaseResult.Failure(DiaryLoadByDateException.Unknown(e))
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_読込処理エラー (日付: $date)", e)
            UseCaseResult.Failure(DiaryLoadByDateException.LoadFailure(date, e))
        }
    }
}
