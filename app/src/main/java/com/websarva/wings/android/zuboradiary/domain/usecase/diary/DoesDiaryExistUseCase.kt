package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.utils.logTag
import java.time.LocalDate

/**
 * 指定された日付の日記が既に存在するかどうかを確認するユースケース。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class DoesDiaryExistUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logMsg = "日記既存確認_"

    /**
     * ユースケースを実行し、指定された日付の日記が存在するかどうかを返す。
     *
     * @param date 確認する日記の日付。
     * @return 日記が存在する場合は [UseCaseResult.Success] に `true` を、存在しない場合は `false` を格納して返す。
     *   確認に失敗した場合は [UseCaseResult.Failure] に [DiaryExistenceCheckException] を格納して返す。
     */
    suspend operator fun invoke(
        date: LocalDate
    ): UseCaseResult<Boolean, DiaryExistenceCheckException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date)")
        return try {
            val exists = diaryRepository.existsDiary(date)
            Log.i(logTag, "${logMsg}完了 (結果: $exists)")
            UseCaseResult.Success(exists)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                DiaryExistenceCheckException.Unknown(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_存在確認エラー", e)
            UseCaseResult.Failure(
                DiaryExistenceCheckException.CheckFailure(date, e)
            )
        }
    }
}
