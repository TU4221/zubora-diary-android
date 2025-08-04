package com.websarva.wings.android.zuboradiary.data.preferences

internal sealed class UserPreferencesException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class DataStoreAccessFailure(
        cause: Throwable? = null
    ) : UserPreferencesException("データストアへのアクセスに失敗しました。", cause)

    class DataNotFound(
        preferenceName: String,
        cause: Throwable? = null
    ) : UserPreferencesException("ユーザー設定($preferenceName)のデータが存在しません。", cause)
}
