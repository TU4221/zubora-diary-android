package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ファイルを操作する際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidFileOperationParameterException(
    cause: Throwable? = null
) : FileOperationException("ファイル操作のパラメータが不正。", cause)
