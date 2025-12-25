package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list

import android.util.Log
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.ExistingDiaryDateListLoadException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日記が存在する日付のリストを読み込むユースケース。
 *
 * カレンダー等の日記有無表示に使用される日付のリストを提供する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadExistingDiaryDateListUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {

    private val logMsg = "日記が存在する日付のリスト読込_"

    /**
     * ユースケースを実行し、日記が存在する日付のリストのFlowを返す。
     *
     * @return 日記が存在する日付のリストの読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] に [LocalDate] のリストを格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] に [ExistingDiaryDateListLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<
            UseCaseResult<List<LocalDate>, ExistingDiaryDateListLoadException>
            > {
        Log.i(logTag, "${logMsg}開始")

        return diaryRepository
            .loadExistingDiaryDateList()
            .map {
                Log.d(logTag, "${logMsg}成功 (日記件数: ${it.count()})")
                UseCaseResult.Success(it)
            }.catch {
                val wrappedException =
                    when (it) {
                        is UnknownException -> {
                            Log.d(logTag, "${logMsg}失敗_原因不明")
                            ExistingDiaryDateListLoadException.Unknown(it)
                        }
                        is DomainException -> {
                            Log.d(logTag, "${logMsg}失敗_読込エラー")
                            ExistingDiaryDateListLoadException.LoadFailure(it)
                        }
                        else -> throw it
                    }
                UseCaseResult.Failure(wrappedException)
            }
    }
}
