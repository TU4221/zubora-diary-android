package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllDataDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllSettingsInitializationException
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
            diaryRepository.deleteAllData()
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除処理エラー", e)
            return UseCaseResult.Failure(
                AllDataDeleteException.DiariesDeleteFailure(e)
            )
        }

        return try {
            fileRepository.clearAllImageFiles()
            initializeAllSettings()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_画像ファイル削除処理エラー", e)
            UseCaseResult.Failure(
                AllDataDeleteException.ImageFileDeleteFailure(e)
            )
        } catch (e: AllSettingsInitializationException) {
            val wrappedException =
                when (e) {
                    is AllSettingsInitializationException.Unknown -> {
                        Log.e(logTag, "${logMsg}失敗_原因不明", e)
                        AllDataDeleteException.Unknown(e)
                    }
                    else -> {
                        Log.e(logTag, "${logMsg}失敗_設定初期化処理エラー", e)
                        AllDataDeleteException.SettingsInitializationFailure(e)
                    }
                }
            UseCaseResult.Failure(wrappedException)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                AllDataDeleteException.Unknown(e)
            )
        }
    }

    /**
     * 全ての設定を初期化する。
     *
     * @throws AllSettingsInitializationException 設定の初期化に失敗した場合。
     */
    private suspend fun initializeAllSettings() {
        when (val result = initializeAllSettingsUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
