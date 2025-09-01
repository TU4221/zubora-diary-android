package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.net.Uri
import android.widget.ImageView
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator

internal class DiaryImageUpdater {

    fun update(themeColor: ThemeColorUi, imageView: ImageView, imageUri: Uri?) {
        DiaryImageConfigurator()
            .setUpImageOnDiary(
                imageView,
                imageUri,
                themeColor
            )
    }
}
