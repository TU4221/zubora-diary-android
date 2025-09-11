package com.websarva.wings.android.zuboradiary.domain.model

/**
 * 画像ファイルのサイズを表すシールドクラス。
 *
 * @property size TODO:仮でString(データ層の詳細がわかり次第修正)
 */
internal sealed class ImageSize(val size: String) {

    data object Full: ImageSize("full")

    data object Thumbnail: ImageSize("thumbnail")
}
