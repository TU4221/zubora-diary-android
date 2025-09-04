package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryCountFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 保存されている日記の総数を取得するユースケース。
 *
 * 特定の開始日以降の日記、または全期間の日記の数をカウントする。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class CountDiariesUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "日記総数取得_"

    /**
     * ユースケースを実行し、日記の総数を返す。
     *
     * @param startDate 日記数をカウントする期間の開始日。`null` の場合は全期間の日記を対象とする。
     * @return 日記の総数を [UseCaseResult.Success] に格納して返す。
     *   日記数のカウントに失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        startDate: LocalDate? = null
    ): DefaultUseCaseResult<Int> {
        Log.i(logTag, "${logMsg}開始 (開始日: ${startDate ?: "全期間"})")

        return try {
            val numDiaries =
                if (startDate == null) {
                    diaryRepository.countDiaries()
                } else {
                    diaryRepository.countDiaries(
                        startDate
                    )
                }
            Log.i(logTag, "${logMsg}完了 (結果: $numDiaries)")
            UseCaseResult.Success(numDiaries)
        } catch (e: DiaryCountFailureException) {
            Log.e(logTag, "${logMsg}失敗_カウント処理エラー", e)
            UseCaseResult.Failure(e)
        }
    }
}
