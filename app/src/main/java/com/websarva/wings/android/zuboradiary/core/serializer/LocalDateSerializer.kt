package com.websarva.wings.android.zuboradiary.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

/**
 * java.time.LocalDate を ISO-8601 形式の文字列としてシリアライズ/デシリアライズするためのカスタムシリアライザー。
 */
internal object LocalDateSerializer : KSerializer<LocalDate> {
    // (1) このデータがどのようなプリミティブ型に変換されるかを定義（今回はString）
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    // (2) オブジェクト → 文字列 への変換ルール
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString()) // 例: "2025-10-26"
    }

    // (3) 文字列 → オブジェクト への変換ルール
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString()) // 例: "2025-10-26" から LocalDateへ
    }
}
