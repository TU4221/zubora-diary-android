package com.websarva.wings.android.zuboradiary.ui.list;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class DiaryListListenerSetting {

    public void setUp(RecyclerView recyclerView, int listItemMarginVertical) {
        recyclerView.addOnScrollListener(new DiaryListOnScrollListener(listItemMarginVertical));
        recyclerView.addOnLayoutChangeListener(new DiaryListOnLayoutChangeListener(recyclerView, listItemMarginVertical));
    }

    private class DiaryListOnScrollListener extends RecyclerView.OnScrollListener {
        int listItemMarginVertical;

        public DiaryListOnScrollListener(int listItemMarginVertical) {
            this.listItemMarginVertical = listItemMarginVertical;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            updateFirstVisibleSectionBarPosition(recyclerView, listItemMarginVertical);

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
            if (!isLoadingDiaryList()
                    && (firstVisibleItem + visibleItemCount) >= totalItemCount
                    && dy > 0
                    && lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                loadDiaryList();
            }
        }
    }

    private void updateFirstVisibleSectionBarPosition(
            RecyclerView recyclerView, int itemMarginVertical) {
        RecyclerView.LayoutManager _layoutManager = recyclerView.getLayoutManager();
        LinearLayoutManager layoutManager;
        if (_layoutManager instanceof LinearLayoutManager) {
            layoutManager = (LinearLayoutManager) _layoutManager;
        } else {
            return;
        }
        int firstVisibleItemPosition =
                layoutManager.findFirstVisibleItemPosition();

        RecyclerView.ViewHolder firstVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        RecyclerView.ViewHolder secondVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1);

        if (firstVisibleViewHolder instanceof DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) {
            DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder _firstVisibleViewHolder =
                    (DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) firstVisibleViewHolder;
            View firstVisibleItemView =
                    layoutManager.getChildAt(0);
            View secondVisibleItemView =
                    layoutManager.getChildAt(1);
            if (firstVisibleItemView != null) {
                float firstVisibleItemViewPositionY = firstVisibleItemView.getY();
                if (secondVisibleItemView != null) {
                    int sectionBarHeight = _firstVisibleViewHolder.binding.textSectionBar.getHeight();
                    float secondVisibleItemViewPositionY = secondVisibleItemView.getY();
                    int border = sectionBarHeight + itemMarginVertical;
                    if (secondVisibleItemViewPositionY >= border) {
                        _firstVisibleViewHolder.binding.textSectionBar.setY(-(firstVisibleItemViewPositionY));
                    } else {
                        if (secondVisibleItemViewPositionY < itemMarginVertical) {
                            _firstVisibleViewHolder.binding.textSectionBar.setY(0);
                        } else if (_firstVisibleViewHolder.binding.textSectionBar.getY() == 0) {
                            _firstVisibleViewHolder.binding.textSectionBar.setY(
                                    -(firstVisibleItemViewPositionY) - sectionBarHeight
                            );
                        }
                    }
                } else {
                    _firstVisibleViewHolder.binding.textSectionBar.setY(-(firstVisibleItemViewPositionY));
                }
            }
        }
        if (secondVisibleViewHolder instanceof DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) {
            DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder _secondVisibleViewHolder =
                    (DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) secondVisibleViewHolder;
            _secondVisibleViewHolder.binding.textSectionBar.setY(0); // ズレ防止
        }
    }

    public abstract boolean isLoadingDiaryList();

    public abstract void loadDiaryList();

    private class DiaryListOnLayoutChangeListener implements View.OnLayoutChangeListener {

        RecyclerView recyclerView;
        int listItemMarginVertical;

        public DiaryListOnLayoutChangeListener(RecyclerView recyclerView, int listItemMarginVertical) {
            this.recyclerView = recyclerView;
            this.listItemMarginVertical = listItemMarginVertical;
        }

        @Override
        public void onLayoutChange(
                View v, int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom) {
            updateFirstVisibleSectionBarPosition(recyclerView, listItemMarginVertical);
        }
    }
}
