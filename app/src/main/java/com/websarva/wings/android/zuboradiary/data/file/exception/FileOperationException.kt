package com.websarva.wings.android.zuboradiary.data.file.exception

import com.websarva.wings.android.zuboradiary.data.exception.StorageException

/**
 * ファイル操作で発生する問題を示す例外。
 * [StorageException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class FileOperationException(
    message: String,
    cause: Throwable? = null
) : StorageException(message, cause)
