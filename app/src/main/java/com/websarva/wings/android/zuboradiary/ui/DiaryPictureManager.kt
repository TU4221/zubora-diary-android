package com.websarva.wings.android.zuboradiary.ui

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.websarva.wings.android.zuboradiary.R

internal class DiaryPictureManager(
    private val context: Context, private val imageView: ImageView, private val iconColor: Int) {

    fun setUpPictureOnDiary(uri: Uri?) {
        if (uri == null) {
            setUpDefaultIconOnDiary()
            return
        }

        try {
            setUpPicture(uri)
        } catch (e: SecurityException) {
            setUpPermissionDenialIconOnDiary()
        }
    }

    private fun setUpDefaultIconOnDiary() {
        setUpIcon(R.drawable.diary_edit_image_ic_photo_library_24px)
    }

    private fun setUpPermissionDenialIconOnDiary() {
        setUpIcon(R.drawable.diary_image_ic_hide_image_24px)
    }

    fun setUpPictureOnDiaryList(uri: Uri?) {
        if (uri == null) {
            setUpDefaultIconOnDiaryList()
            return
        }

        try {
            setUpPicture(uri)
        } catch (e: SecurityException) {
            setUpPermissionDenialIconOnDiaryList()
        }
    }

    private fun setUpDefaultIconOnDiaryList() {
        setUpIcon(R.drawable.ic_image_24px)
    }

    private fun setUpPermissionDenialIconOnDiaryList() {
        setUpIcon(R.drawable.ic_hide_image_24px)
    }

    private fun setUpIcon(iconResId: Int) {
        val icon = ContextCompat.getDrawable(context, iconResId)
        imageView.setImageDrawable(icon)
        setUpIconColor()
    }

    // MEMO:添付写真と未添付時のアイコンを動的に切替表示を行うため下記処理が必要。
    //      ImageView#setImageDrawable()で設定したDrawableの色はThemeColorInflaterの設定色ではなく、
    //      Manifest.xmlのapplicationタグのtheme属性で設定されている色が反映される為、毎度下記コードで色を設定する。
    private fun setUpIconColor() {
        imageView.setColorFilter(iconColor)
    }

    private fun setUpPicture(pictureUri: Uri) {
        imageView.setImageURI(pictureUri)
        imageView.colorFilter = null
    }
}
