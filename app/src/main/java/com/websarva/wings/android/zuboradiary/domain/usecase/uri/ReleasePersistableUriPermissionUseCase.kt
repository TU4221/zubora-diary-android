package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.exception.PersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.PermissionException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

// TODO:URI関係はレポジトリ層までとする為、このクラスは削除
/**
 * 指定されたURI文字列に対応する永続的なURI権限を解放するユースケース。
 *
 * @property uriRepository URI関連の操作を行うリポジトリ。
 */
internal class ReleasePersistableUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "URIの永続的権限解放_"

    /**
     * ユースケースを実行し、指定されたURIの永続的な権限を解放する。
     *
     * @param uriString 権限を解放する対象のURI文字列。
     * @return 解放処理が成功した場合は [UseCaseResult.Success] を返す。
     *   解放処理中にエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    operator fun invoke(uriString: String): UseCaseResult<Unit, PersistableUriPermissionReleaseFailureException> {
        Log.i(logTag, "${logMsg}開始 (URI: \"$uriString\")")

        try {
            uriRepository.releasePersistableUriPermission(uriString)
        } catch (e: PermissionException) {
            Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", e)
            return UseCaseResult.Failure(
                PersistableUriPermissionReleaseFailureException(uriString, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
