package com.websarva.wings.android.zuboradiary.ui.model.common

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class FilePathUi : Parcelable {

    abstract val path: String

    data class Available(override val path: String) : FilePathUi()

    object Unavailable : FilePathUi() {
        @IgnoredOnParcel
        override val path: String = ""
    }
}
