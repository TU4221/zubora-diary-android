package com.websarva.wings.android.zuboradiary.data.worker.exception

import com.websarva.wings.android.zuboradiary.data.exception.BackgroundTaskException

/**
 * ワーカー操作に関連する問題を示す例外。
 * [BackgroundTaskException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class WorkerOperationException(
    message: String,
    cause: Throwable? = null
) : BackgroundTaskException(message, cause)
