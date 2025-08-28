package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ReleasePersistableUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(uriString: String): DefaultUseCaseResult<Unit> {
        val logMsg = "URIの永続的権限解放_"
        Log.i(logTag, "${logMsg}開始")

        try {
            uriRepository.releasePersistableUriPermission(uriString)
        } catch (e: PersistableUriPermissionReleaseFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
