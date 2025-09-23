package com.websarva.wings.android.zuboradiary.ui.view.imageview

import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil3.asImage
import coil3.dispose
import coil3.load
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import java.io.File

internal class DiaryImageConfigurator {

    fun setUpImageOnDiary(imageView: ImageView, path: ImageFilePathUi?, themeColor: ThemeColorUi) {
        val context = imageView.context
        loadImage(
            imageView,
            path,
            themeColor.getOnSurfaceVariantColor(context.resources),
            R.drawable.ic_photo_library_24px,
        )
    }

    fun setUpImageOnDiaryList(imageView: ImageView, path: ImageFilePathUi?, themeColor: ThemeColorUi) {
        val context = imageView.context
        loadImage(
            imageView,
            path,
            themeColor.getOnSecondaryContainerColor(context.resources),
            R.drawable.ic_image_24px
        )
    }

    private fun loadImage(
        imageView: ImageView,
        path: ImageFilePathUi?,
        colorInt: Int,
        defaultIconRes: Int
    ) {


        // 読み込み画像がない場合デフォルト画像を表示
        if (path == null) {
            imageView.apply {
                setImageResource(defaultIconRes)
                setColorFilter(colorInt)
            }
            return
        }

        val context = imageView.context
        val data = File(path.path)
        val currentImage = imageView.drawable?.asImage()
        val placeHolderImage =
            if (currentImage == null) {
                imageView.setColorFilter(colorInt)
                ContextCompat.getDrawable(context, defaultIconRes)?.asImage()
            } else {
                currentImage
            }
        imageView.dispose()
        imageView.load(data) {
            // デフォルトアイコン設定
            currentImage?.let { placeholder(placeHolderImage) }
            // エラーアイコン設定
            error(
                ContextCompat.getDrawable(context, R.drawable.ic_hide_image_24px)?.asImage()
            )
            // カラー設定
            listener(
                onSuccess = { _, _ ->
                    imageView.clearColorFilter()
                },
                onError = { _, _ ->
                    imageView.setColorFilter(colorInt)
                }
            )
        }
    }
}
