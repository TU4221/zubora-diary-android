package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public abstract class WordSearchResultYearMonthListAdapter extends DiaryYearMonthListBaseAdapter {
    public WordSearchResultYearMonthListAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor) {
        super(context, recyclerView, themeColor, new DiffUtilItemCallback());
    }

    @Override
    public void createDiaryDayList(
            DiaryYearMonthListBaseAdapter.DiaryYearMonthListViewHolder holder, DiaryYearMonthListBaseItem item) {
        WordSearchResultYearMonthListItem _item = (WordSearchResultYearMonthListItem) item;
        WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                createWordSearchResultDayListAdapter(holder);
        List<WordSearchResultDayListItem> wordSearchResultDayList =
                _item.getWordSearchResultDayList().getWordSearchResultDayListItemList();
        wordSearchResultDayListAdapter.submitList(wordSearchResultDayList);
    }

    @NonNull
    private WordSearchResultDayListAdapter createWordSearchResultDayListAdapter(
            DiaryYearMonthListBaseAdapter.DiaryYearMonthListViewHolder holder) {
        Objects.requireNonNull(holder);

        WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                new WordSearchResultDayListAdapter(context, holder.binding.recyclerDayList, themeColor);
        wordSearchResultDayListAdapter.build();
        wordSearchResultDayListAdapter.setOnClickItemListener(new WordSearchResultDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);
                if (onClickChildItemListener == null) return;

                onClickChildItemListener.onClick(date);
            }
        });
        return wordSearchResultDayListAdapter;
    }

    private static class DiffUtilItemCallback extends DiaryYearMonthListBaseAdapter.DiffUtilItemCallback {

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListBaseItem oldItem, @NonNull DiaryYearMonthListBaseItem newItem) {
            Log.d("WordSearchYearMonthList", "DiffUtil.ItemCallback_areContentsTheSame()");
            Log.d("WordSearchYearMonthList", "oldItem_YearMonth:" + oldItem.getYearMonth());
            Log.d("WordSearchYearMonthList", "newItem_YearMonth:" + newItem.getYearMonth());
            // 日
            if (oldItem instanceof WordSearchResultYearMonthListItem
                    && newItem instanceof WordSearchResultYearMonthListItem) {
                Log.d("WordSearchYearMonthList", "WordSearchResultYearMonthListItem");
                WordSearchResultYearMonthListItem _oldItem = (WordSearchResultYearMonthListItem) oldItem;
                WordSearchResultYearMonthListItem _newItem = (WordSearchResultYearMonthListItem) newItem;
                int oldChildListSize =
                        _oldItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().size();
                int newChildListSize =
                        _newItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().size();
                if (oldChildListSize != newChildListSize) {
                    Log.d("WordSearchYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < oldChildListSize; i++) {
                    WordSearchResultDayListItem oldChildListItem =
                            _oldItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().get(i);
                    WordSearchResultDayListItem newChildListItem =
                            _newItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().get(i);
                    Log.d("WordSearchYearMonthList", "oldChildListItem_Date:" + oldChildListItem.getDate());
                    Log.d("WordSearchYearMonthList", "newChildListItem_Date:" + newChildListItem.getDate());

                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_Date不一致");
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_Title不一致");
                        return false;
                    }
                    if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemNumber不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemTitle不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemComment不一致");
                        return false;
                    }
                }
            }
            Log.d("WordSearchYearMonthList", "一致");
            return true;
        }
    }
}
