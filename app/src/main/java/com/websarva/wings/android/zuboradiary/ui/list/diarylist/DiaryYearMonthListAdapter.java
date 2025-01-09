package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryYearMonthListBaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DiaryYearMonthListAdapter extends SwipeDiaryYearMonthListBaseAdapter {

    DiaryYearMonthListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor) {
        super(context, recyclerView, themeColor, new DiffUtilItemCallback());
    }

    @Override
    public void createDiaryDayList(
            DiaryYearMonthListBaseAdapter.DiaryYearMonthListViewHolder holder,
            DiaryYearMonthListBaseItem item) {
        DiaryYearMonthListItem _item = (DiaryYearMonthListItem) item;
        DiaryDayListAdapter diaryDayListAdapter = createDiaryDayListAdapter(holder);
        List<DiaryDayListItem> diaryDayList = _item.getDiaryDayList().getDiaryDayListItemList();
        List<DiaryDayListBaseItem> convertedList = new ArrayList<>(diaryDayList);
        diaryDayListAdapter.submitList(convertedList);
    }

    @NonNull
    private DiaryDayListAdapter createDiaryDayListAdapter(
            DiaryYearMonthListBaseAdapter.DiaryYearMonthListViewHolder holder) {
        Objects.requireNonNull(holder);

        DiaryDayListAdapter diaryDayListAdapter =
                new DiaryDayListAdapter(context, holder.binding.recyclerDayList, themeColor);
        diaryDayListAdapter.build();
        diaryDayListAdapter.setOnClickItemListener(new DiaryDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(DiaryDayListBaseItem item) {
                Objects.requireNonNull(item);
                if (onClickChildItemListener == null) return;

                onClickChildItemListener.onClick(item);
            }
        });
        diaryDayListAdapter.setOnClickDeleteButtonListener(new DiaryDayListAdapter.OnClickDeleteButtonListener() {
            @Override
            public void onClick(DiaryDayListBaseItem item) {
                Objects.requireNonNull(item);
                if (onClickChildItemBackgroundButtonListener == null) return;

                onClickChildItemBackgroundButtonListener.onClick(item);
            }
        });
        return diaryDayListAdapter;
    }

    private static class DiffUtilItemCallback extends DiaryYearMonthListBaseAdapter.DiffUtilItemCallback {

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListBaseItem oldItem, @NonNull DiaryYearMonthListBaseItem newItem) {
            Log.d("DiaryYearMonthList", "DiffUtil.ItemCallback_areContentsTheSame()");
            Log.d("DiaryYearMonthList", "oldItem_YearMonth:" + oldItem.getYearMonth());
            Log.d("DiaryYearMonthList", "newItem_YearMonth:" + newItem.getYearMonth());
            // 日
            if (oldItem instanceof DiaryYearMonthListItem && newItem instanceof DiaryYearMonthListItem) {
                Log.d("DiaryYearMonthList", "DiaryYearMonthListItem");
                DiaryYearMonthListItem _oldItem = (DiaryYearMonthListItem) oldItem;
                DiaryYearMonthListItem _newItem = (DiaryYearMonthListItem) newItem;

                int _oldChildListSize = _oldItem.getDiaryDayList().getDiaryDayListItemList().size();
                int _newChildListSize = _newItem.getDiaryDayList().getDiaryDayListItemList().size();
                if (_oldChildListSize != _newChildListSize) {
                    Log.d("DiaryYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < _oldChildListSize; i++) {
                    DiaryDayListItem oldChildListItem = _oldItem.getDiaryDayList().getDiaryDayListItemList().get(i);
                    DiaryDayListItem newChildListItem = _newItem.getDiaryDayList().getDiaryDayListItemList().get(i);
                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Date不一致");
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Title不一致");
                        return false;
                    }
                    if (oldChildListItem.getPicturePath() == null && newChildListItem.getPicturePath() != null) {
                        Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致");
                        return false;
                    }
                    if (oldChildListItem.getPicturePath() != null && newChildListItem.getPicturePath() == null) {
                        Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致");
                        return false;
                    }
                    if ((oldChildListItem.getPicturePath() != null && newChildListItem.getPicturePath() != null)
                            && (!oldChildListItem.getPicturePath().equals(newChildListItem.getPicturePath()))) {
                        Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致");
                        return false;
                    }
                }
            }
            Log.d("DiaryYearMonthList", "一致");
            return true;
        }
    }
}
