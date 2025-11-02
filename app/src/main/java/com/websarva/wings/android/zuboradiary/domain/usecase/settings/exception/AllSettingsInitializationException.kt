package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase

/**
 * [InitializeAllSettingsUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class AllSettingsInitializationException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "設定の初期化に失敗しました。",
        cause
    )

    /**
     * ストレージ容量不足により、設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InsufficientStorage(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "ストレージ容量不足により、設定の初期化に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
