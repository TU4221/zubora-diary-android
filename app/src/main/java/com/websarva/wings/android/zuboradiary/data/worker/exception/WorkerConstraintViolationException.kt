package com.websarva.wings.android.zuboradiary.data.worker.exception

/**
 * ワーカーの制約が実行時に満たされなかったことを示す例外。
 * 通常、WorkManager は制約が満たされるまで実行を遅延させますが、
 * 何らかの理由で実行が試みられたものの、直前で制約違反が検知された場合など、
 * 特殊なケースでデータソース側がこれを検知して通知したい場合に利用できます。
 * 
 * @param workerName ワーカーの名前。
 * @param unmetConstraints 満たされなかった制約の説明。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class WorkerConstraintViolationException(
    workerName: String,
    unmetConstraints: String,
    cause: Throwable? = null
) : WorkerException("ワーカー '$workerName' の制約が実行時に満たされませんでした: $unmetConstraints", cause)
