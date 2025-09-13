package com.websarva.wings.android.zuboradiary.data.worker.exception

/**
 * ワーカーの実行が予期せず失敗したことを示す例外。
 * この例外は、Worker 内部でキャッチされ `ListenableWorker.Result.failure()` につながるような
 * エラーをラップし、より詳細な情報を提供したい場合などに限定的に使用します。
 * 通常、Worker の失敗は `ListenableWorker.Result.failure()` で表現され、
 * その結果を監視する側で処理します。
 * 
 * @param workerName 失敗したワーカーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class WorkerExecutionException(
    workerName: String,
    cause: Throwable? = null
) : WorkerException("ワーカー '$workerName' の実行に失敗しました。", cause)
