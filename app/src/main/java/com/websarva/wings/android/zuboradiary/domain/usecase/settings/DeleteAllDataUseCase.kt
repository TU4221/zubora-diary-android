package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllDataDeleteException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

// TODO:冗長UseCase廃止後、例外ハンドリング方法見直し
/**
 * アプリケーションの全データを削除するユースケース。
 *
 * 具体的には以下の処理を実行する。
 * 1. 全ての日記データ (項目タイトル選択履歴含む) を削除する。
 * 2. 全ての画像ファイルを削除する。
 * 3. 全ての設定を初期化する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 * @property fileRepository ファイル関連へのアクセスを提供するリポジトリ。
 * @property initializeAllSettingsUseCase 全ての設定を初期化するユースケース。
 */
internal class DeleteAllDataUseCase(
    private val diaryRepository: DiaryRepository,
    private val fileRepository: FileRepository,
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
            deleteAllImageFile()
            initializeAllSettings()
        } catch (e: AllDataDeleteException) {
            when (e) {
                is AllDataDeleteException.DiariesDeleteFailure ->
                    Log.e(logTag, "${logMsg}失敗_日記データ削除処理エラー", e)

                is AllDataDeleteException.ImageFileDeleteFailure ->
                    Log.e(logTag, "${logMsg}失敗_画像ファイル削除処理エラー", e)

                is AllDataDeleteException.SettingsInitializationFailure ->
                    Log.e(logTag, "${logMsg}失敗_設定初期化処理エラー", e)
            }
            return UseCaseResult.Failure(e)
        } catch (e: DataStorageException) {
            return UseCaseResult.Failure(
                AllDataDeleteException.DiariesDeleteFailure(e)
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
     * 全ての画像ファイルを削除する。
     *
     * @throws AllDataDeleteException.ImageFileDeleteFailure 画像ファイルの削除に失敗した場合。
     */
    private suspend fun deleteAllImageFile() {
        try {
            fileRepository.clearAllImageFiles()
        } catch (e: DomainException) {
            throw AllDataDeleteException.ImageFileDeleteFailure(e)
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
