package com.websarva.wings.android.zuboradiary.data.location.exception

import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException

/**
 * 位置情報の要求をする際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidLocationRequestParameterException(
    cause: Throwable? = null
) : DatabaseException("位置情報要求のパラメータが不正。", cause)
