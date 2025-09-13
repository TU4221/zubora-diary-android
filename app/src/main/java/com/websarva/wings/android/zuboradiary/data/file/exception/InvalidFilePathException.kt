package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルパスが無効であるか、不正な形式であることを示す例外。
 *
 * @param path 問題のあるファイルパス。
 * @param reason パスが無効である理由。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidFilePathException(
    path: String,
    reason: String? = null,
    cause: Throwable? = null
) : FileOperationException("無効なファイルパスです。 $reason (パス: $path)", cause)
