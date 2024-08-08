package com.websarva.wings.android.zuboradiary.ui.list;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListViewModel;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DiaryListOnScrollListener extends RecyclerView.OnScrollListener {
    int listItemMarginVertical;
    boolean isLoading;
    Supplier<Boolean> a;

    public DiaryListOnScrollListener(int listItemMarginVertical, boolean isLoading) {
        this.listItemMarginVertical = listItemMarginVertical;
        this.isLoading = isLoading;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        DiaryListSetting<DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder> diaryListSetting =
                new DiaryListSetting<>();
        diaryListSetting
                .updateFirstVisibleSectionBarPosition(
                        recyclerView,
                        listItemMarginVertical
                );


        LinearLayoutManager layoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) {
            // TODO:assert
            return;
        }
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (totalItemCount <= 0) {
            return; // Adapter#getItemViewType()例外対策
        }
        int lastItemPosition = totalItemCount - 1;
        RecyclerView.Adapter<?> recyclerViewAdapter = recyclerView.getAdapter();
        if (recyclerViewAdapter == null) {
            return;
        }
        int lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition);
        // MEMO:下記条件"dy > 0"は検索結果リストが更新されたときに
        //      "RecyclerView.OnScrollListener#onScrolled"が起動するための対策。
        if (!isLoading
                && (firstVisibleItem + visibleItemCount) >= totalItemCount
                && dy > 0
                && lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
        }
    }

    abstract void loadList();
}
