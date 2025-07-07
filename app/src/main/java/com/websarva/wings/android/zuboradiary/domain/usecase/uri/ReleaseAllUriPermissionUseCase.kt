package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.uri.RevokePersistentAccessAllUriFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ReleaseAllUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): DefaultUseCaseResult<Unit> {
        val logMsg = "全Uri権限解放_"
        Log.i(logTag, "${logMsg}開始")

        try {
            uriRepository.releaseAllPersistablePermission()
        } catch (e: RevokePersistentAccessAllUriFailedException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
