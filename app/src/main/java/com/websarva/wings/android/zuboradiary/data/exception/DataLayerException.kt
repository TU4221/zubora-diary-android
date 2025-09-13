package com.websarva.wings.android.zuboradiary.data.exception

/**
 * データアクセスレイヤーで発生する問題を示す抽象例外。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal abstract class DataLayerException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
