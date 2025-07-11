package com.websarva.wings.android.zuboradiary.data.preferences

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal sealed class UserPreferencesException (
    message: String,
    cause: Throwable? = null
) : DataException(message, cause) {

    class DataStoreAccessFailed(
        cause: Throwable? = null
    ) : UserPreferencesException("データストアへのアクセスに失敗しました。", cause)

    class DataNotFoundException(
        preferenceName: String,
        cause: Throwable? = null
    ) : UserPreferencesException("ユーザー設定($preferenceName)のデータが存在しません。", cause)
}
