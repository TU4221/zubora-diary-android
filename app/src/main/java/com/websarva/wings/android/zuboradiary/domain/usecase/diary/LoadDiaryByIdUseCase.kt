package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 特定のIDの日記データを読み込むユースケース。
 *
 * 指定されたIDに対応する日記情報をリポジトリから取得する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadDiaryByIdUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "IDによる日記取得_"

    /**
     * ユースケースを実行し、指定されたIDの日記データを返す。
     *
     * @param id 読み込む日記のID。
     * @return 処理に成功した場合は [UseCaseResult.Success] に日記データ( [Diary] )を格納して返す。
     *   失敗した場合、または該当する日記が存在しない場合は [UseCaseResult.Failure] に [DiaryLoadByDateException] を格納して返す。
     */
    suspend operator fun invoke(
        id: DiaryId
    ): UseCaseResult<Diary, DiaryLoadByIdException> {
        Log.i(logTag, "${logMsg}開始 (ID: $id)")

        return try {
            val diary = diaryRepository.loadDiary(id)
            Log.i(logTag, "${logMsg}完了 (取得日記: $diary)")
            UseCaseResult.Success(diary)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明")
            UseCaseResult.Failure(DiaryLoadByIdException.Unknown(e))
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_読込処理エラー (ID: $id)", e)
            UseCaseResult.Failure(DiaryLoadByIdException.LoadFailure(id, e))
        }
    }
}
