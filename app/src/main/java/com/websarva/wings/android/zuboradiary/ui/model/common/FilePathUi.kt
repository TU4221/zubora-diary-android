package com.websarva.wings.android.zuboradiary.ui.model.common

internal sealed class FilePathUi {

    abstract val path: String

    data class Available(override val path: String) : FilePathUi()

    object Unavailable : FilePathUi() {
        override val path: String = ""
    }
}
