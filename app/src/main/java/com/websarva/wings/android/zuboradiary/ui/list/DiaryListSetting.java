package com.websarva.wings.android.zuboradiary.ui.list;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 日記リストとワード検索結果リストの共通設定をここにまとめる。
public class DiaryListSetting {
    public void updateFirstVisibleSectionBarPosition(
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
                        Log.d("20240614",String.valueOf(firstVisibleItemViewPositionY));
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
}
