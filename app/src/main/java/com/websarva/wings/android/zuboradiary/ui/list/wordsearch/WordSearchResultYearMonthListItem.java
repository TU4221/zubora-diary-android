package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WordSearchResultYearMonthListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private int year;
    private int month;
    private List<WordSearchResultDayListItem> WordSearchResultDayList = new ArrayList<>();
    private int viewType;

    public WordSearchResultYearMonthListItem() {
    }

    public WordSearchResultYearMonthListItem(
            int year, int month, List<WordSearchResultDayListItem> WordSearchResultDayList, int viewType) {
        this.year = year;
        this.month = month;
        this.WordSearchResultDayList = WordSearchResultDayList;
        this.viewType = viewType;
    }

    public WordSearchResultYearMonthListItem(WordSearchResultYearMonthListItem item) {
        this.year = item.year;
        this.month = item.month;
        this.WordSearchResultDayList = item.WordSearchResultDayList;
        this.viewType = item.viewType;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public WordSearchResultYearMonthListItem clone() {
        WordSearchResultYearMonthListItem clone = null;
        try {
            clone = (WordSearchResultYearMonthListItem) super.clone();
            List<WordSearchResultDayListItem> cloneWordSearchResultDayList = new ArrayList<>();
            for (WordSearchResultDayListItem item : this.WordSearchResultDayList) {
                WordSearchResultDayListItem cloneItem = item.clone();
                cloneWordSearchResultDayList.add(cloneItem);
            }
            clone.WordSearchResultDayList = cloneWordSearchResultDayList;
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

    public List<WordSearchResultDayListItem> getWordSearchResultDayList() {
        return WordSearchResultDayList;
    }

    public void setWordSearchResultDayList(List<WordSearchResultDayListItem> wordSearchResultDayList) {
        this.WordSearchResultDayList = wordSearchResultDayList;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
