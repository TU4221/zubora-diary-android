package com.websarva.wings.android.zuboradiary.ui.view.imageview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DiaryImageConfigurator {

    private val logTag = createLogTag()

    fun setUpImageOnDiary(imageView: ImageView, uri: Uri?, themeColor: ThemeColor) {
        if (uri == null) {
            setUpDefaultIconOnDiary(imageView, themeColor)
            return
        }

        val logMsg = "日記添付写真読込"
        try {
            Log.i(logTag, "${logMsg}_開始")
            setUpImage(imageView, uri)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            setUpPermissionDenialIconOnDiary(imageView, themeColor)
        }
    }

    private fun setUpDefaultIconOnDiary(imageView: ImageView, themeColor: ThemeColor) {
        val iconColorInt = getIconColorOnDiary(imageView.context, themeColor)
        setUpIcon(imageView, R.drawable.diary_edit_image_ic_photo_library_24px, iconColorInt)
    }

    private fun setUpPermissionDenialIconOnDiary(imageView: ImageView, themeColor: ThemeColor) {
        val iconColorInt = getIconColorOnDiary(imageView.context, themeColor)
        setUpIcon(imageView, R.drawable.diary_image_ic_hide_image_24px, iconColorInt)
    }

    private fun getIconColorOnDiary(context: Context, themeColor: ThemeColor): Int {
        return themeColor.getOnSurfaceVariantColor(context.resources)
    }

    fun setUpImageOnDiaryList(imageView: ImageView, uri: Uri?, themeColor: ThemeColor) {
        if (uri == null) {
            setUpDefaultIconOnDiaryList(imageView, themeColor)
            return
        }

        val logMsg = "日記リスト添付写真読込"
        try {
            Log.i(logTag, "${logMsg}_開始")
            setUpImage(imageView, uri)
            Log.i(logTag, "${logMsg}_完了")
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            setUpPermissionDenialIconOnDiaryList(imageView, themeColor)
        }
    }

    private fun setUpDefaultIconOnDiaryList(imageView: ImageView, themeColor: ThemeColor) {
        val iconColorInt = getIconColorOnDiaryList(imageView.context, themeColor)
        setUpIcon(imageView, R.drawable.ic_image_24px, iconColorInt)
    }

    private fun setUpPermissionDenialIconOnDiaryList(imageView: ImageView, themeColor: ThemeColor) {
        val iconColorInt = getIconColorOnDiaryList(imageView.context, themeColor)
        setUpIcon(imageView, R.drawable.ic_hide_image_24px, iconColorInt)
    }

    private fun getIconColorOnDiaryList(context: Context, themeColor: ThemeColor): Int {
        return themeColor.getOnSecondaryContainerColor(context.resources)
    }

    private fun setUpIcon(imageView: ImageView, iconResId: Int, iconColor: Int) {
        val icon = ContextCompat.getDrawable(imageView.context, iconResId)
        imageView.setImageDrawable(icon)
        setUpIconColor(imageView, iconColor)
    }

    // MEMO:添付写真と未添付時のアイコンを動的に切替表示を行うため下記処理が必要。
    //      ImageView#setImageDrawable()で設定したDrawableの色はThemeColorInflaterの設定色ではなく、
    //      Manifest.xmlのapplicationタグのtheme属性で設定されている色が反映される為、毎度下記コードで色を設定する。
    private fun setUpIconColor(imageView: ImageView, iconColor: Int) {
        imageView.setColorFilter(iconColor)
    }

    private fun setUpImage(imageView: ImageView, uri: Uri) {
        imageView.setImageURI(uri)
        imageView.colorFilter = null
    }
}
