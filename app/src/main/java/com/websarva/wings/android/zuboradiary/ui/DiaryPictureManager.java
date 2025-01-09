package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Objects;

public class DiaryPictureManager {

    private final Context context;
    private final ImageView imageView;
    private final int iconColor;

    public DiaryPictureManager(Context context, ImageView imageView, int iconColor) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(imageView);

        this.context = context;
        this.imageView = imageView;
        this.iconColor = iconColor;
    }

    public void setUpPictureOnDiary(Uri uri) {
        if (uri == null) {
            setUpDefaultIconOnDiary();
            return;
        }

        try {
            setUpPicture(uri);
        } catch (SecurityException e) {
            setUpPermissionDenialIconOnDiary();
        }
    }

    public void setUpDefaultIconOnDiary() {
        setUpIcon(R.drawable.diary_edit_image_ic_photo_library_360px);
    }

    private void setUpPermissionDenialIconOnDiary() {
        setUpIcon(R.drawable.diary_image_ic_hide_image_360px);
    }

    public void setUpPictureOnDiaryList(Uri uri) {
        if (uri == null) {
            setUpDefaultIconOnDiaryList();
            return;
        }

        try {
            setUpPicture(uri);
        } catch (SecurityException e) {
            setUpPermissionDenialIconOnDiaryList();
        }
    }

    public void setUpDefaultIconOnDiaryList() {
        setUpIcon(R.drawable.ic_image_24px);
    }

    private void setUpPermissionDenialIconOnDiaryList() {
        setUpIcon(R.drawable.ic_hide_image_24px);
    }

    private void setUpIcon(int iconResId) {
        Drawable icon = ContextCompat.getDrawable(context, iconResId);
        imageView.setImageDrawable(icon);
        setUpIconColor();
    }

    // MEMO:添付写真と未添付時のアイコンを動的に切替表示を行うため下記処理が必要。
    //      ImageView#setImageDrawable()で設定したDrawableの色はThemeColorInflaterの設定色ではなく、
    //      Manifest.xmlのapplicationタグのtheme属性で設定されている色が反映される為、毎度下記コードで色を設定する。
    private void setUpIconColor() {
        imageView.setColorFilter(iconColor);
    }

    private void setUpPicture(Uri pictureUri) {
        imageView.setImageURI(pictureUri);
        imageView.setColorFilter(null);
    }
}
