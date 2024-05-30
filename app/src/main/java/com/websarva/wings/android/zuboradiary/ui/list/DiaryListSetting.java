package com.websarva.wings.android.zuboradiary.ui.list;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 日記リストとワード検索結果リストの共通設定をここにまとめる。
public class DiaryListSetting<E extends DiaryYearMonthListViewHolder> {
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
        E firstVisibleViewHolder =
                (E) recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        E secondVisibleViewHolder =
                (E) recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1);
        View firstVisibleItemView =
                layoutManager.getChildAt(0);
        View secondVisibleItemView =
                layoutManager.getChildAt(1);
        if (firstVisibleViewHolder != null && firstVisibleItemView != null) {
            float firstVisibleItemViewPositionY = firstVisibleItemView.getY();
            if (secondVisibleViewHolder != null && secondVisibleItemView != null) {
                secondVisibleViewHolder.textSectionBar.setY(0); // ズレ防止
                int sectionBarHeight = firstVisibleViewHolder.textSectionBar.getHeight();
                float secondVisibleItemViewPositionY = secondVisibleItemView.getY();
                int border = sectionBarHeight + itemMarginVertical;
                if (secondVisibleItemViewPositionY >= border) {
                    firstVisibleViewHolder.textSectionBar.setY(-(firstVisibleItemViewPositionY));
                } else {
                    if (secondVisibleItemViewPositionY < itemMarginVertical) {
                        firstVisibleViewHolder.textSectionBar.setY(0);
                    } else if (firstVisibleViewHolder.textSectionBar.getY() == 0) {
                        firstVisibleViewHolder.textSectionBar.setY(
                                -(firstVisibleItemViewPositionY) - sectionBarHeight
                        );
                    }
                }
            } else {
                firstVisibleViewHolder.textSectionBar.setY(-(firstVisibleItemViewPositionY));
            }
        }
    }
}
