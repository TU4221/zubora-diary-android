package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListSimpleCallback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
public abstract class SwipeDiaryYearMonthListBaseAdapter extends DiaryYearMonthListBaseAdapter {

    protected OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener;
    protected final List<DiaryListSimpleCallback> simpleCallbackList = new ArrayList<>();

    protected SwipeDiaryYearMonthListBaseAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            DiffUtilItemCallback diffUtilItemCallback) {
        super(context, recyclerView, themeColor, diffUtilItemCallback);
    }

    public void build() {
        super.build();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return;

                // スクロール時スワイプ閉
                closeSwipedItemOtherDayList(null);
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Objects.requireNonNull(parent);

        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent,viewType);

        if (holder instanceof DiaryYearMonthListViewHolder) {
            DiaryYearMonthListViewHolder diaryYearMonthListViewHolder =
                                                            (DiaryYearMonthListViewHolder) holder;
            DiaryListSimpleCallback diaryListSimpleCallback =
                    new DiaryListSimpleCallback(recyclerView, diaryYearMonthListViewHolder.binding.recyclerDayList);
            diaryListSimpleCallback.build();
            simpleCallbackList.add(diaryListSimpleCallback);
        }

        return holder;
    }

    @FunctionalInterface
    public interface OnClickChildItemBackgroundButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemBackgroundButtonListener(
            @Nullable OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener) {
        this.onClickChildItemBackgroundButtonListener = onClickChildItemBackgroundButtonListener;
    }

    public void closeSwipedItemOtherDayList(@Nullable DiaryListSimpleCallback simpleCallback) {
        if (simpleCallback == null) {
            for (DiaryListSimpleCallback _simpleCallback: simpleCallbackList) {
                _simpleCallback.closeSwipedItem();
            }
        } else {
            for (int i = 0; i < simpleCallbackList.size(); i++) {
                if (simpleCallbackList.get(i) != simpleCallback) {
                    simpleCallbackList.get(i).closeSwipedItem();
                }
            }
        }
    }
}
