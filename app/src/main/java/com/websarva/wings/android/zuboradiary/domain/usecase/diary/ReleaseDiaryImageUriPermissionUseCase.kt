package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageUriPermissionReleaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 日記画像URIの永続的権限を解放するユースケース。
 *
 * 指定されたURIが他の日記で使用されていない場合に限り、そのURIに対する永続的なアクセス権限を解放する。
 *
 * @property isImageUriUsedInDiariesUseCase 画像Uriがのいずれかの日記データで使用されているか確認するためのユースケース。。
 * @property releasePersistableUriPermissionUseCase 永続的なURI権限を解放するためのユースケース。
 */
internal class ReleaseDiaryImageUriPermissionUseCase(
    private val isImageUriUsedInDiariesUseCase: IsImageUriUsedInDiariesUseCase,
    private val releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "日記画像URIの永続的権限解放_"

    /**
     * ユースケースを実行し、指定された日記画像URIの永続的権限を解放する。
     *
     * 指定されたURIが他の日記で使用されているかを確認する。
     * 使用されていない場合のみ、権限を解放する。
     *
     * @param uriString 権限を解放する対象の画像URI文字列。
     * @return 権限解放処理が正常に完了した場合、またはURIがまだ使用中で解放処理がスキップされた場合は
     *   [UseCaseResult.Success] に `Unit` を格納して返す。
     *   URIの使用状況確認に失敗した場合、または権限解放処理自体に失敗した場合は
     *   [UseCaseResult.Failure] に [DiaryImageUriPermissionReleaseException] を格納して返す。
     */
    suspend operator fun invoke(
        uriString: String
    ): UseCaseResult<Unit, DiaryImageUriPermissionReleaseException> {
        Log.i(logTag, "${logMsg}開始 (URI: \"$uriString\")")

        when (val usedResult = isImageUriUsedInDiariesUseCase(uriString)) {
            is UseCaseResult.Success -> {
                val existsImageUri = usedResult.value
                if (existsImageUri) {
                    Log.i(logTag, "${logMsg}完了_URI使用中のためスキップ (URI: \"$uriString\")")
                    return UseCaseResult.Success(Unit)
                }
            }

            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_URI使用状況確認エラー", usedResult.exception)
                return UseCaseResult.Failure(
                    DiaryImageUriPermissionReleaseException
                        .ImageUriUsageCheckFailure(uriString, usedResult.exception)
                )
            }
        }

        return when (val releaseResult = releasePersistableUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了 (URI: \"$uriString\")")
                UseCaseResult.Success(Unit)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", releaseResult.exception)
                UseCaseResult.Failure(
                    DiaryImageUriPermissionReleaseException
                        .PermissionReleaseFailure(uriString, releaseResult.exception)
                )
            }
        }
    }
}
