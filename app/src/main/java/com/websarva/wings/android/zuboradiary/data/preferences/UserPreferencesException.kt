package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * ユーザー設定 (DataStore) のアクセス失敗時にスローする例外クラス。
 *
 * ユーザー設定データの読み書き中に何らかの問題が発生した場合に使用する。
 *
 * @param message 例外メッセージ。
 * @param cause ユーザー設定アクセス失敗の根本原因となったThrowable (オプショナル)。
 */
internal sealed class UserPreferencesException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * DataStoreへのアクセスに失敗した場合の例外。
     *
     * @param cause DataStoreアクセス失敗の根本原因となったThrowable。
     */
    class DataStoreAccessFailure(
        cause: Throwable
    ) : UserPreferencesException("データストアへのアクセスに失敗しました。", cause)

    /**
     * 要求されたユーザー設定データが存在しない場合の例外。
     *
     * @param preferenceName 存在しなかった設定項目の名前。
     */
    class DataNotFound(
        preferenceName: String
    ) : UserPreferencesException("ユーザー設定($preferenceName)のデータが存在しません。")
}
