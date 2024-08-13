package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.os.Build;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.UUID;

public class DiaryDayListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private LocalDate date;
    private String title;
    private String picturePath;

    public DiaryDayListItem(@NonNull LocalDate date,
                            @NonNull String title , @NonNull String picturePath) {
        this.date = date;
        this.title = title;
        this.picturePath = picturePath;
    }

    // MEMO:ID以外同じインスタンスを作成(cloneはIDも同じになるため)
    public DiaryDayListItem(DiaryDayListItem diaryDayListItem) {
        this.date = diaryDayListItem.date;
        this.title = diaryDayListItem.title;
        this.picturePath = diaryDayListItem.picturePath;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryDayListItem clone() {
        DiaryDayListItem clone;
        try {
            clone = (DiaryDayListItem) super.clone();
        } catch (CloneNotSupportedException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                throw new InternalError(e);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        return clone;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    @NonNull
    public String getTitle() {
        return this.title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getPicturePath() {
        return this.picturePath;
    }

    public void setPicturePath(@NonNull String picturePath) {
        this.picturePath = picturePath;
    }

}
