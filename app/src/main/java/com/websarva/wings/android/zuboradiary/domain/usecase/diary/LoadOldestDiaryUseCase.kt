package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.OldestDiaryLoadException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 最古の日記データを読み込むユースケース。
 *
 * 保存されている日記の中で、最も日付が古い日記の情報を取得する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadOldestDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "最古日記読込_"

    /**
     * ユースケースを実行し、最古の日記データを返す。
     *
     * @return 最古の日記データが存在する場合は [UseCaseResult.Success] にその [Diary] オブジェクトを格納して返す。
     *   日記が存在しない場合、読み込みに失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(): UseCaseResult<Diary, OldestDiaryLoadException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            val diary = diaryRepository.loadOldestDiary()

            Log.i(logTag, "${logMsg}完了 (結果: $diary)")
            return UseCaseResult.Success(diary)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_読込処理エラー", e)
            return UseCaseResult.Failure(
                OldestDiaryLoadException.LoadFailure(e)
            )
        } catch (e: NotFoundException) {
            Log.e(logTag, "${logMsg}失敗_保存された日記が存在しない")
            return UseCaseResult.Failure(
                OldestDiaryLoadException.DataNotFound(e)
            )
        }
    }
}
