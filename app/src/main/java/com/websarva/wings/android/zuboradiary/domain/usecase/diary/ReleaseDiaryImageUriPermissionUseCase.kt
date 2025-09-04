package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryImageUriUsageCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 日記画像URIの永続的権限を解放するユースケース。
 *
 * 指定されたURIが他の日記で使用されていない場合に限り、そのURIに対する永続的なアクセス権限を解放する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property releasePersistableUriPermissionUseCase 永続的なURI権限を解放するためのユースケース。
 */
internal class ReleaseDiaryImageUriPermissionUseCase(
    private val diaryRepository: DiaryRepository,
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
     * @return 権限解放処理が正常に完了した場合、またはURIがまだ使用中で解放処理がスキップされた場合は [UseCaseResult.Success] を返す。
     *   URIの使用状況確認に失敗した場合、または権限解放処理自体に失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(uriString: String): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始 (URI: \"$uriString\")")

        try {
            val existsImageUri = diaryRepository.existsImageUri(uriString)
            if (existsImageUri) {
                Log.i(logTag, "${logMsg}完了_URI使用中のためスキップ (URI: \"$uriString\")")
                return UseCaseResult.Success(Unit)
            }
        } catch (e: DiaryImageUriUsageCheckFailureException) {
            Log.e(logTag, "${logMsg}失敗_URI使用状況確認エラー", e)
            return UseCaseResult.Failure(e)
        }

        when (val result = releasePersistableUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了 (URI: \"$uriString\")")
                return UseCaseResult.Success(Unit)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", result.exception)
                return UseCaseResult.Failure(result.exception)
            }
        }
    }
}
