package com.websarva.wings.android.zuboradiary.ui.model

@JvmInline
internal value class ImageFilePathUi(
    val path: String = ""  // デフォルト値はエラーが発生した場合のダミー値(View側でエラー画像を表示させる)
)
