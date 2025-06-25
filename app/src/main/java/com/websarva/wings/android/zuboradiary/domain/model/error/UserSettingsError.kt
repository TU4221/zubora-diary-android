package com.websarva.wings.android.zuboradiary.domain.model.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class UserSettingsError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class UpdateSettings(
        cause: Throwable? = null
    ) : UserSettingsError(
        "ユーザー設定の更新に失敗しました。",
        cause
    )
}
