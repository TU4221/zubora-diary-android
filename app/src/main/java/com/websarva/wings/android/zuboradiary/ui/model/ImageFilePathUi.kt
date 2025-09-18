package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class ImageFilePathUi {
    data class Valid(val path: String) : ImageFilePathUi()
    data object Invalid : ImageFilePathUi()
    data object NoImage : ImageFilePathUi()
}
