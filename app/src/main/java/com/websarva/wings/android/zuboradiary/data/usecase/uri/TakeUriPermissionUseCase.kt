package com.websarva.wings.android.zuboradiary.data.usecase.uri

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.usecase.uri.error.TakeUriPermissionError
import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class TakeUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(uri: Uri): UseCaseResult<Unit, TakeUriPermissionError> {
        val logMsg = "Uri権限取得_"
        Log.i(logTag, "${logMsg}開始")

        return try {
            uriRepository.takePersistablePermission(uri)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: Exception) {
            val error = TakeUriPermissionError.TakeUriPermission(e)
            Log.e(logTag, "${logMsg}失敗", error)
            UseCaseResult.Error(error)
        }
    }
}
