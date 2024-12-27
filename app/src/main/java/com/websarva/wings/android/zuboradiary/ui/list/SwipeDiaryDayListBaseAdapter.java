package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

import java.time.LocalDate;
import java.util.Objects;

public abstract class SwipeDiaryDayListBaseAdapter extends DiaryDayListBaseAdapter {

    private OnClickDeleteButtonListener onClickDeleteButtonListener;

    protected SwipeDiaryDayListBaseAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            DiffUtilItemCallback diffUtilItemCallback) {
        super(context, recyclerView, themeColor, diffUtilItemCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        DiaryDayListBaseItem item = getItem(position);
        Objects.requireNonNull(item);

        onBindDeleteButtonClickListener(holder, item);
    }

    protected abstract void onBindDeleteButtonClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item);

    @FunctionalInterface
    public interface OnClickDeleteButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickDeleteButtonListener(OnClickDeleteButtonListener onClickDeleteButtonListener) {
        Objects.requireNonNull(onClickDeleteButtonListener);

        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    protected void onClickDeleteButton(LocalDate date) {
        Objects.requireNonNull(date);
        if (onClickDeleteButtonListener == null) return;

        onClickDeleteButtonListener.onClick(date);
    }
}
