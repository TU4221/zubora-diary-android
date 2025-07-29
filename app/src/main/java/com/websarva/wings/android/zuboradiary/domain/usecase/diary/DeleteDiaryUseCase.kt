package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class DeleteDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    // MEMO:日記表示、編集フラグメント以外からも削除できるように下記引数とする。
    suspend operator fun invoke(
        date: LocalDate,
        imageUriString: String?
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteDiary(date)
        } catch (e: DeleteDiaryFailedException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        try {
            releaseImageUriPermission(imageUriString)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            // MEMO:Uri権限の取り消しに失敗しても日記保存がメインの為、成功とみなす。
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(DeleteDiaryFailedException::class)
    private suspend fun deleteDiary(
        date: LocalDate
    ) {
        val logMsg = "日記データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteDiary(date)
        } catch (e: DeleteDiaryFailedException) {
            throw e
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DomainException::class)
    private suspend fun releaseImageUriPermission(
        uriString: String?
    ) {
        val logMsg = "画像URIの永続的権限権限解放_"

        if (uriString == null) {
            Log.i(logTag, "${logMsg}不要")
            return
        }

        Log.i(logTag, "${logMsg}開始")
        when (val result = releasePersistableUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
