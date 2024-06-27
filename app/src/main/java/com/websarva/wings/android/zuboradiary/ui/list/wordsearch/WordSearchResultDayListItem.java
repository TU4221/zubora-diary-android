package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.os.Build;
import android.text.SpannableString;

import androidx.annotation.NonNull;

import java.util.UUID;

public class WordSearchResultDayListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private int year;
    private int month;
    private int dayOfMonth;
    private String dayOfWeek = "";
    private SpannableString title = new SpannableString("");
    private int itemNumber;
    private SpannableString itemTitle = new SpannableString("");
    private SpannableString itemComment = new SpannableString("");

    public WordSearchResultDayListItem() {
    }

    public WordSearchResultDayListItem(int year, int month, int dayOfMonth, @NonNull String dayOfWeek,
                                       @NonNull SpannableString title , int itemNo,
                                       @NonNull SpannableString itemTitle, @NonNull SpannableString itemComment) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.title = title;
        this.itemNumber = itemNo;
        this.itemTitle = itemTitle;
        this.itemComment = itemComment;
    }

    // MEMO:ID以外同じインスタンスを作成(cloneはIDも同じになるため)
    public WordSearchResultDayListItem(WordSearchResultDayListItem item) {
        this.year = item.year;
        this.month = item.month;
        this.dayOfMonth = item.dayOfMonth;
        this.dayOfWeek = item.dayOfWeek;
        this.title = item.title;
        this.itemNumber = item.itemNumber;
        this.itemTitle = item.title;
        this.itemComment = item.itemComment;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public WordSearchResultDayListItem clone() {
        WordSearchResultDayListItem clone = null;
        try {
            clone = (WordSearchResultDayListItem) super.clone();
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

    public SpannableString getTitle() {
        return this.title;
    }

    public void setTitle(SpannableString title) {
        this.title = title;
    }

    public int getItemNumber() {
        return this.itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public SpannableString getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(SpannableString itemTitle) {
        this.itemTitle = itemTitle;
    }

    public SpannableString getItemComment() {
        return itemComment;
    }

    public void setItemComment(SpannableString itemComment) {
        this.itemComment = itemComment;
    }
}
