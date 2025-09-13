package com.websarva.wings.android.zuboradiary.data.exception

/**
 * バックグラウンドタスクの実行や管理に関連する問題を示す抽象例外。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal abstract class BackgroundTaskException(
    message: String,
    cause: Throwable? = null
) : DataLayerException(message, cause)
