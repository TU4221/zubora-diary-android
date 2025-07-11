package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal sealed class UserSettingsException (
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {

    class AccessFailed(
        cause: Throwable? = null
    ) : UserSettingsException("ユーザー設定へのアクセスに失敗しました。", cause)

    class DataNotFoundException(
        cause: Throwable? = null
    ) : UserSettingsException("指定されたユーザー設定のデータが存在しません。", cause)
}
