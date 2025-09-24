package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.ImageProgressView

internal class DiaryImageUpdater {

    fun update(imageProgressView: ImageProgressView, path: ImageFilePathUi?) {
        imageProgressView.loadImage(path?.path)
    }
}
