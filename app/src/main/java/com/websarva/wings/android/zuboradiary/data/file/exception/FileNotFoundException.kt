package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * 指定されたファイルまたはディレクトリが見つからなかったことを示す例外。
 *
 * @param path 見つからなかったファイルまたはディレクトリのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class FileNotFoundException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("ファイルまたはディレクトリが見つかりません。 (パス: $path)", cause)
