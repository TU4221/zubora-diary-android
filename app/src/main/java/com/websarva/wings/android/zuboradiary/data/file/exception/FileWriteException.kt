package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルへの書き込み中にI/Oエラー等が発生したことを示す例外。
 *
 * @param path 書き込みに失敗したファイルのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class FileWriteException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("ファイルへの書き込みに失敗しました。 (パス: $path)", cause)
