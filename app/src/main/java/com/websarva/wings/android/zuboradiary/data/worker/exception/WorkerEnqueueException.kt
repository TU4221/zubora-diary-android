package com.websarva.wings.android.zuboradiary.data.worker.exception

/**
 * ワーカーのエンキュー (登録) に失敗したことを示す例外。
 * 
 * @param workerName 失敗したワーカーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class WorkerEnqueueException(
    workerName: String,
    cause: Throwable? = null
) : WorkerOperationException("ワーカー '$workerName' のエンキューに失敗しました。", cause)
