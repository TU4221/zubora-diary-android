package com.websarva.wings.android.zuboradiary.data.preferences

internal sealed class UserPreferencesException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class DataStoreAccessFailed(
        cause: Throwable? = null
    ) : UserPreferencesException("データストアへのアクセスに失敗しました。", cause)

    class DataNotFoundException(
        preferenceName: String,
        cause: Throwable? = null
    ) : UserPreferencesException("ユーザー設定($preferenceName)のデータが存在しません。", cause)
}
