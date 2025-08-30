package com.websarva.wings.android.zuboradiary.data.preferences

internal sealed class UserPreferencesException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class DataStoreAccessFailure(
        cause: Throwable
    ) : UserPreferencesException("データストアへのアクセスに失敗しました。", cause)

    class DataNotFound(
        preferenceName: String
    ) : UserPreferencesException("ユーザー設定($preferenceName)のデータが存在しません。")
}
