package com.websarva.wings.android.zuboradiary.ui.view.imageview

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DiaryPictureConfigurator {

    private val logTag = createLogTag()

    fun setUpPictureOnDiary(imageView: ImageView, uri: Uri?, iconColor: Int) {
        if (uri == null) {
            setUpDefaultIconOnDiary(imageView, iconColor)
            return
        }

        val logMsg = "日記添付写真読込"
        try {
            Log.e(logTag, "${logMsg}_開始")
            setUpPicture(imageView, uri)
            Log.e(logTag, "${logMsg}_完了")
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            setUpPermissionDenialIconOnDiary(imageView, iconColor)
        }
    }

    private fun setUpDefaultIconOnDiary(imageView: ImageView, iconColor: Int) {
        setUpIcon(imageView, R.drawable.diary_edit_image_ic_photo_library_24px, iconColor)
    }

    private fun setUpPermissionDenialIconOnDiary(imageView: ImageView, iconColor: Int) {
        setUpIcon(imageView, R.drawable.diary_image_ic_hide_image_24px, iconColor)
    }

    fun setUpPictureOnDiaryList(imageView: ImageView, uri: Uri?, iconColor: Int) {
        if (uri == null) {
            setUpDefaultIconOnDiaryList(imageView, iconColor)
            return
        }

        val logMsg = "日記リスト添付写真読込"
        try {
            Log.e(logTag, "${logMsg}_開始")
            setUpPicture(imageView, uri)
            Log.e(logTag, "${logMsg}_完了")
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            setUpPermissionDenialIconOnDiaryList(imageView, iconColor)
        }
    }

    private fun setUpDefaultIconOnDiaryList(imageView: ImageView, iconColor: Int) {
        setUpIcon(imageView, R.drawable.ic_image_24px, iconColor)
    }

    private fun setUpPermissionDenialIconOnDiaryList(imageView: ImageView, iconColor: Int) {
        setUpIcon(imageView, R.drawable.ic_hide_image_24px, iconColor)
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

    private fun setUpPicture(imageView: ImageView, pictureUri: Uri) {
        imageView.setImageURI(pictureUri)
        imageView.colorFilter = null
    }
}
