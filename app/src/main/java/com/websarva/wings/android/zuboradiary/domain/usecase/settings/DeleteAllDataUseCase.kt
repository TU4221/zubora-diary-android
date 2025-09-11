package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.exception.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllDataDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * アプリケーションの全データを削除するユースケース。
 *
 * 具体的には以下の処理を実行する。
 * 1. 全ての日記データ (項目タイトル選択履歴含む) を削除する。
 * 2. 全ての永続的なURI権限を解放する。
 * 3. 全ての設定を初期化する。
 *
 * @property diaryRepository 日記関連の操作を行うリポジトリ。
 * @property releaseAllPersistableUriPermissionUseCase 全ての永続的なURI権限を解放するユースケース。
 * @property initializeAllSettingsUseCase 全ての設定を初期化するユースケース。
 */
internal class DeleteAllDataUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase,
    private val initializeAllSettingsUseCase: InitializeAllSettingsUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "アプリ全データ削除_"

    /**
     * ユースケースを実行し、アプリケーションの全データを削除する。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [AllDataDeleteException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, AllDataDeleteException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllData()
            releaseAllImageUriPermission()
            initializeAllSettings()
        } catch (e: AllDataDeleteException) {
            when (e) {
                is AllDataDeleteException.DiariesDeleteFailure ->
                    Log.e(logTag, "${logMsg}失敗_日記データ削除処理エラー", e)

                is AllDataDeleteException.UriPermissionReleaseFailure ->
                    Log.e(logTag, "${logMsg}失敗_権限解放処理エラー", e)

                is AllDataDeleteException.SettingsInitializationFailure ->
                    Log.e(logTag, "${logMsg}失敗_設定初期化処理エラー", e)
            }
            return UseCaseResult.Failure(e)
        } catch (e: DataStorageException) {
            return UseCaseResult.Failure(
                AllDataDeleteException.DiariesDeleteFailure(e)
            )
        } catch (e: AllPersistableUriPermissionReleaseFailureException) {
            return UseCaseResult.Failure(
                AllDataDeleteException.UriPermissionReleaseFailure(e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    /**
     * 全ての日記データを削除する。
     *
     * @throws DataStorageException 日記データの削除に失敗した場合。
     */
    private suspend fun deleteAllData() {
        diaryRepository.deleteAllData()
    }

    /**
     * 全ての永続的なURI権限を解放する。
     *
     * @throws AllPersistableUriPermissionReleaseFailureException URI権限の解放に失敗した場合。
     */
    private fun releaseAllImageUriPermission() {
        when (val result = releaseAllPersistableUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 全ての設定を初期化する。
     *
     * @throws AllDataDeleteException.SettingsInitializationFailure 設定の初期化に失敗した場合。
     */
    private suspend fun initializeAllSettings() {
        when (val result = initializeAllSettingsUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw AllDataDeleteException
                    .SettingsInitializationFailure(result.exception)
            }
        }
    }
}
