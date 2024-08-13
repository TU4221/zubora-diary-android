package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.os.Build;
import android.text.SpannableString;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.UUID;

public class WordSearchResultDayListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private LocalDate date;
    private SpannableString title;
    private int itemNumber;
    private SpannableString itemTitle;
    private SpannableString itemComment;

    public WordSearchResultDayListItem(@NonNull LocalDate date,
                                       @NonNull SpannableString title , int itemNo,
                                       @NonNull SpannableString itemTitle, @NonNull SpannableString itemComment) {
        this.date = date;
        this.title = title;
        this.itemNumber = itemNo;
        this.itemTitle = itemTitle;
        this.itemComment = itemComment;
    }

    // MEMO:ID以外同じインスタンスを作成(cloneはIDも同じになるため)
    public WordSearchResultDayListItem(WordSearchResultDayListItem item) {
        this.date = item.date;
        this.title = item.title;
        this.itemNumber = item.itemNumber;
        this.itemTitle = item.title;
        this.itemComment = item.itemComment;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public WordSearchResultDayListItem clone() {
        WordSearchResultDayListItem clone;
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

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    @NonNull
    public SpannableString getTitle() {
        return this.title;
    }

    public void setTitle(@NonNull SpannableString title) {
        this.title = title;
    }

    public int getItemNumber() {
        return this.itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    @NonNull
    public SpannableString getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(@NonNull SpannableString itemTitle) {
        this.itemTitle = itemTitle;
    }

    @NonNull
    public SpannableString getItemComment() {
        return itemComment;
    }

    public void setItemComment(@NonNull SpannableString itemComment) {
        this.itemComment = itemComment;
    }
}
