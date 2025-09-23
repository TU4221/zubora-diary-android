package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.widget.ImageView
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator

internal class DiaryImageUpdater {

    fun update(themeColor: ThemeColorUi, imageView: ImageView, path: ImageFilePathUi?) {
        DiaryImageConfigurator()
            .setUpImageOnDiary(
                imageView,
                path,
                themeColor
            )
    }
}
