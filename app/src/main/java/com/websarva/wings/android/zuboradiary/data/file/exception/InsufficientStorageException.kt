package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ストレージの空き容量が不足しているためにファイル操作に失敗したことを示す例外。
 *
 * @param path 操作対象だったファイルのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InsufficientStorageException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("ストレージの空き容量が不足しています。 (パス: $path)", cause)
