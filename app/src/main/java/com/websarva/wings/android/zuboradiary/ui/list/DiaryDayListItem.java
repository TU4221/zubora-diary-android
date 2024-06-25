package com.websarva.wings.android.zuboradiary.ui.list;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.UUID;

public class DiaryDayListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private int year;
    private int month;
    private int dayOfMonth;
    private String dayOfWeek = "";
    private String title = "";
    private String picturePath = "";

    public DiaryDayListItem() {
    }

    public DiaryDayListItem(int year, int month, int dayOfMonth, @NonNull String dayOfWeek,
                            @NonNull String title , @NonNull String picturePath) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.title = title;
        this.picturePath = picturePath;
    }

    // MEMO:ID以外同じインスタンスを作成(cloneはIDも同じになるため)
    public DiaryDayListItem(DiaryDayListItem diaryDayListItem) {
        this.year = diaryDayListItem.year;
        this.month = diaryDayListItem.month;
        this.dayOfMonth = diaryDayListItem.dayOfMonth;
        this.dayOfWeek = diaryDayListItem.dayOfWeek;
        this.title = diaryDayListItem.title;
        this.picturePath = diaryDayListItem.picturePath;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryDayListItem clone() {
        DiaryDayListItem clone = null;
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

    public String getId() {
        return id;
    }
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getDayOfWeek() {
        return this.dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicturePath() {
        return this.picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

}
