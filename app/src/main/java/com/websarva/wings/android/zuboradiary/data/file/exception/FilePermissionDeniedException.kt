package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルまたはディレクトリへのアクセス権限がないことを示す例外。
 *
 * @param path アクセスが拒否されたファイルまたはディレクトリのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class FilePermissionDeniedException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("ファイルまたはディレクトリへのアクセスが拒否されました。 (パス: $path)", cause)
