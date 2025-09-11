package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.AllDiariesDeleteException
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

    // TODO:権限解放時のエラーハンドリングが不適切。(日記データ削除したあとでも失敗になる)
    /**
     * ユースケースを実行し、全ての日記データ (項目タイトル選択履歴除く) と関連URI権限を削除する。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [AllDiariesDeleteException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, AllDiariesDeleteException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllDiaries()
            releaseAllImageUriPermission()
        } catch (e: AllDiariesDeleteException) {
            when (e) {
                is AllDiariesDeleteException.DeleteFailure ->
                    Log.e(logTag, "${logMsg}失敗_日記データ削除処理エラー", e)
                is AllDiariesDeleteException.PersistableUriPermissionReleaseFailure ->
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
     * @throws AllDiariesDeleteException.DeleteFailure 日記データの削除に失敗した場合。
     */
    private suspend fun deleteAllDiaries() {
        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: DataStorageException) {
            throw AllDiariesDeleteException.DeleteFailure(e)
        }
    }

    /**
     * 全ての永続的なURI権限を解放する。
     *
     * @throws AllDiariesDeleteException.PersistableUriPermissionReleaseFailure URI権限の解放に失敗した場合。
     */
    private fun releaseAllImageUriPermission() {
        when (val result = releaseAllPersistableUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw AllDiariesDeleteException
                    .PersistableUriPermissionReleaseFailure(result.exception)
            }
        }
    }
}
