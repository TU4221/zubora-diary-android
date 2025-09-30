package com.websarva.wings.android.zuboradiary.data.common

// TODO:UI層での確認のみとし、権限未許可の場合は例外をスローするので、データ層での確認は不要(後日廃止)
/**
 * アプリケーションが必要とする特定のパーミッションが付与されているかどうかを確認する機能を提供するインターフェースである。
 *
 * このインターフェースの実装は、Androidフレームワークのパーミッション管理機能を利用して、
 * 指定されたパーミッションの現在の状態を問い合わせる責務を持つ。
 *
 * パーミッションに依存する機能（例: 通知の表示、位置情報の取得など）を実行する前に、
 * このインターフェースを通じて権限の状態を確認することで、安全な処理の実行が行える。
 */
internal interface PermissionChecker {

    /**
     * 通知の送信に必要な `POST_NOTIFICATIONS` パーミッションが現在付与されているかどうかを示す。
     *
     * @return 通知パーミッションが付与されていれば `true`、そうでなければ `false`。
     */
    val isPostNotificationsGranted: Boolean

    /**
     * 位置情報アクセスに必要な `ACCESS_FINE_LOCATION` 、又は `ACCESS_COARSE_LOCATION` パーミッションが現在付与されているかどうかを示す。
     *
     * @return 位置情報アクセスパーミッションが付与されていれば `true`、そうでなければ `false`。
     */
    val isAccessLocationGranted: Boolean
}
