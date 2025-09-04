package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDiariesDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDiariesUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 全ての日記データ (項目タイトル選択履歴除く) と、それに関連する永続的なURI権限を削除するユースケース。
 *
 * 具体的には以下の処理を実行する。
 * 1. 全ての日記データ (項目タイトル選択履歴除く) を削除する。
 * 2. 全ての永続的なURI権限を解放する。
 *
 * @property diaryRepository 日記関連の操作を行うリポジトリ。
 * @property releaseAllPersistableUriPermissionUseCase 全ての永続的なURI権限を解放するユースケース。
 */
internal class DeleteAllDiariesUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase,
) {

    private val logTag = createLogTag()
    private val logMsg = "全日記データ削除_"

    /**
     * ユースケースを実行し、全ての日記データ (項目タイトル選択履歴除く) と関連URI権限を削除する。
     *
     * @return 全ての削除処理が成功した場合は [UseCaseResult.Success] を返す。
     *   いずれかの処理でエラーが発生した場合は、対応する [DeleteAllDiariesUseCaseException] を
     *   [UseCaseResult.Failure] に格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, DeleteAllDiariesUseCaseException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllDiaries()
            releaseAllImageUriPermission()
        } catch (e: DeleteAllDiariesUseCaseException) {
            when (e) {
                is DeleteAllDiariesUseCaseException.AllDiariesDeleteFailure ->
                    Log.e(logTag, "${logMsg}失敗_日記データ削除処理エラー", e)
                is DeleteAllDiariesUseCaseException.AllPersistableUriPermissionReleaseFailure ->
                    Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", e)
            }
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 全ての日記データを削除する。
     *
     * @throws DeleteAllDiariesUseCaseException.AllDiariesDeleteFailure 日記データの削除に失敗した場合。
     */
    private suspend fun deleteAllDiaries() {
        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: AllDiariesDeleteFailureException) {
            throw DeleteAllDiariesUseCaseException.AllDiariesDeleteFailure(e)
        }
    }

    /**
     * 全ての永続的なURI権限を解放する。
     *
     * @throws DeleteAllDiariesUseCaseException.AllPersistableUriPermissionReleaseFailure URI権限の解放に失敗した場合。
     */
    private fun releaseAllImageUriPermission() {
        when (val result = releaseAllPersistableUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDiariesUseCaseException
                    .AllPersistableUriPermissionReleaseFailure(result.exception)
            }
        }
    }
}
