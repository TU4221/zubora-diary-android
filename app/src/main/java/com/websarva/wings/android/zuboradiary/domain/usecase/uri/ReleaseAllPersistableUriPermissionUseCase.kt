package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * アプリが保持している全ての永続的なURI権限を解放するユースケース。
 *
 * @property uriRepository URI関連の操作を行うリポジトリ。
 */
internal class ReleaseAllPersistableUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "全永続的URI権限解放_"

    /**
     * ユースケースを実行し、全ての永続的なURI権限を解放する。
     *
     * @return 解放処理が成功した場合は [UseCaseResult.Success] を返す。
     *   解放処理中にエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    operator fun invoke(): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始")

        try {
            uriRepository.releaseAllPersistableUriPermission()
        } catch (e: AllPersistableUriPermissionReleaseFailureException) {
            Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
