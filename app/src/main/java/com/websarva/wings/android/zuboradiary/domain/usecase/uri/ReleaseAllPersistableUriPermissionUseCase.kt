package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ReleaseAllPersistableUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): DefaultUseCaseResult<Unit> {
        val logMsg = "全永続的URI権限解放_"
        Log.i(logTag, "${logMsg}開始")

        try {
            uriRepository.releaseAllPersistableUriPermission()
        } catch (e: AllPersistableUriPermissionReleaseFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
