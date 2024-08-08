package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.os.Build;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WordSearchResultYearMonthListItem extends DiaryYearMonthListItemBase {
    private List<WordSearchResultDayListItem> WordSearchResultDayList = new ArrayList<>();

    // TODO:引数無しコンストラクト必要か確認
    public WordSearchResultYearMonthListItem() {
    }

    public WordSearchResultYearMonthListItem(int viewType) {
        super(viewType);
        WordSearchResultDayList = new ArrayList<>();
    }

    public WordSearchResultYearMonthListItem(
            @NonNull YearMonth yearMonth,
            @NonNull List<WordSearchResultDayListItem> wordSearchResultDayList, int viewType) {
        super(yearMonth, viewType);
        this.WordSearchResultDayList = wordSearchResultDayList;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public WordSearchResultYearMonthListItem clone() {
        WordSearchResultYearMonthListItem clone = (WordSearchResultYearMonthListItem) super.clone();
        List<WordSearchResultDayListItem> cloneWordSearchResultDayList = new ArrayList<>();
        for (WordSearchResultDayListItem item : WordSearchResultDayList) {
            WordSearchResultDayListItem cloneItem = item.clone();
            cloneWordSearchResultDayList.add(cloneItem);
        }
        clone.WordSearchResultDayList = cloneWordSearchResultDayList;
        return clone;
    }

    public List<WordSearchResultDayListItem> getWordSearchResultDayList() {
        return WordSearchResultDayList;
    }

    public void setWordSearchResultDayList(List<WordSearchResultDayListItem> wordSearchResultDayList) {
        this.WordSearchResultDayList = wordSearchResultDayList;
    }
}
