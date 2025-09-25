package com.websarva.wings.android.zuboradiary.data.worker.exception

/**
 * ワーカーのキャンセル処理に関連する問題が発生したことを示す例外。
 * 
 * @param workerName キャンセルしようとしたワーカーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class WorkerCancellationException(
    workerName: String,
    cause: Throwable? = null
) : WorkerOperationException("ワーカー '$workerName' のキャンセル処理に失敗しました。", cause)
