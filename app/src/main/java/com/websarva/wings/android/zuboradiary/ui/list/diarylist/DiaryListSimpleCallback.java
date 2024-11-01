package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.ui.LeftSwipeBackgroundButtonSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.util.Objects;

public class DiaryListSimpleCallback extends LeftSwipeBackgroundButtonSimpleCallback {

    private final RecyclerView parentRecyclerView;

    public DiaryListSimpleCallback(RecyclerView parentRecyclerView, RecyclerView recyclerView) {
        super(recyclerView);
        this.parentRecyclerView = parentRecyclerView;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        // 他ChildRecyclerView(DayList)のスワイプ状態を閉じる
        RecyclerView.Adapter<?> adapter = parentRecyclerView.getAdapter();
        Objects.requireNonNull(adapter);
        DiaryYearMonthListAdapter diaryYearMonthListAdapter = (DiaryYearMonthListAdapter) adapter;
        diaryYearMonthListAdapter.closeSwipedItemOtherDayList(this);
    }
}
