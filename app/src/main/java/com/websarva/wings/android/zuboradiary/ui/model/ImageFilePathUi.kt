package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class ImageFilePathUi {

    abstract val path: String

    data class Available(override val path: String) : ImageFilePathUi()

    object Unavailable : ImageFilePathUi() {
        override val path: String = ""
    }
}
