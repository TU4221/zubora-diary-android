package com.websarva.wings.android.zuboradiary.domain.model.location

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * 緯度、経度の位置情報を表すデータクラス。
 *
 * @param latitude 緯度 (-90.0 から 90.0 の範囲)。
 * @param longitude 経度 (-180.0 から 180.0 の範囲)。
 * @throws IllegalArgumentException 緯度、経度が許容された範囲外の場合にスローされる。
 */
@Serializable
internal data class SimpleLocation(
    val latitude: Double,
    val longitude: Double
) : JavaSerializable {
    init {
        require(latitude >= -90)
        require(latitude <= 90)
        require(longitude >= -180)
        require(longitude <= 180)
    }
}
