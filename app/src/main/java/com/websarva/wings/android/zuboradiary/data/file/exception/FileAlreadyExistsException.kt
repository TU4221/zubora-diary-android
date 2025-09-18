package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルの保存または移動操作時、指定されたパスに同名のファイルが既に存在することを示す例外。
 *
 * @param path 指定されたファイルのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class FileAlreadyExistsException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("保存、移動先にファイルが既に存在します。 (パス: $path)", cause)
