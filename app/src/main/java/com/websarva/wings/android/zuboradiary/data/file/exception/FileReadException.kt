package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルの読み込み中にI/Oエラー等が発生したことを示す例外。
 *
 * @param path 読み込みに失敗したファイルのパス。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class FileReadException(
    path: String,
    cause: Throwable? = null
) : FileOperationException("ファイルの読み込みに失敗しました。 (パス: $path)", cause)
