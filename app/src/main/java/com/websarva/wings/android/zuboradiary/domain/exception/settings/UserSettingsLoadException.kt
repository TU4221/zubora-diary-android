package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal sealed class UserSettingsLoadException (
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {

    class AccessFailure(
        cause: Throwable? = null
    ) : UserSettingsLoadException("ユーザー設定へのアクセスに失敗しました。", cause)

    class DataNotFound(
        cause: Throwable? = null
    ) : UserSettingsLoadException("指定されたユーザー設定のデータが存在しません。", cause)
}
