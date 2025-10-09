package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.ImageProgressView

internal class DiaryImageUpdater {

    fun update(imageProgressView: ImageProgressView, path: FilePathUi?) {
        imageProgressView.loadImage(path?.path)
    }
}
