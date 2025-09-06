package com.websarva.wings.android.zuboradiary.data.worker
/**
 * リマインダー通知に使用されるメッセージ（タイトルと本文）を提供するインターフェース。
 *
 * このインターフェースの実装は、通知の具体的な内容を決定する責務を持ちます。
 */
interface ReminderNotificationMessageProvider {

    /**
     * リマインダー通知のタイトル。
     *
     * 通知のヘッダー部分に表示されるテキスト。
     */
    val title: String

    /**
     * リマインダー通知の本文。
     *
     * 通知の主要なメッセージ内容です。
     */
    val text: String
}
