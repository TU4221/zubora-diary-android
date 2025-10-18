package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllDataDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllSettingsInitializationException
import com.websarva.wings.android.zuboradiary.core.utils.logTag

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
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                AllDataDeleteException.DiariesDeleteFailure(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_日記データ削除エラー", e)
            return UseCaseResult.Failure(
                AllDataDeleteException.DiariesDeleteFailure(e)
            )
        }

        try {
            fileRepository.clearAllImageFiles()
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            return UseCaseResult.Failure(
                AllDataDeleteException.ImageFileDeleteFailure(e)
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_画像ファイル削除エラー", e)
            return UseCaseResult.Failure(
                AllDataDeleteException.ImageFileDeleteFailure(e)
            )
        }

        return try {
            initializeAllSettings()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: AllSettingsInitializationException) {
            val wrappedException =
                when (e) {
                    is AllSettingsInitializationException.InitializationFailure -> {
                        Log.e(logTag, "${logMsg}失敗_設定初期化エラー", e)
                        AllDataDeleteException.SettingsInitializationFailure(e)
                    }
                    is AllSettingsInitializationException.InsufficientStorage -> {
                        Log.e(logTag, "${logMsg}失敗_ストレージ容量不足による設定初期化エラー", e)
                        AllDataDeleteException.SettingsInitializationInsufficientStorageFailure(e)
                    }
                    is AllSettingsInitializationException.Unknown -> {
                        Log.e(logTag, "${logMsg}失敗_原因不明", e)
                        AllDataDeleteException.Unknown(e)
                    }
                }
            UseCaseResult.Failure(wrappedException)
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
