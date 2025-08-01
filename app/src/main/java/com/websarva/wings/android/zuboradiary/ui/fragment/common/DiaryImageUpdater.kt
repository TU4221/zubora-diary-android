package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.net.Uri
import android.widget.ImageView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryImageConfigurator

internal class DiaryImageUpdater {

    fun update(themeColor: ThemeColor, imageView: ImageView, imageUri: Uri?) {
        DiaryImageConfigurator()
            .setUpImageOnDiary(
                imageView,
                imageUri,
                themeColor
            )
    }
}
