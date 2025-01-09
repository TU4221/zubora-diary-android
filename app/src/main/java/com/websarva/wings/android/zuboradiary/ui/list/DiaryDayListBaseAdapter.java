package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;

import java.util.Objects;

public abstract class DiaryDayListBaseAdapter extends ListAdapter<DiaryDayListBaseItem, RecyclerView.ViewHolder> {

    protected final Context context;
    protected final RecyclerView recyclerView;
    protected final ThemeColor themeColor;
    private OnClickItemListener onClickItemListener;

    protected DiaryDayListBaseAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            DiffUtilItemCallback diffUtilItemCallback) {
        super(diffUtilItemCallback);

        Objects.requireNonNull(context);
        Objects.requireNonNull(recyclerView);
        Objects.requireNonNull(themeColor);

        this.context = context;
        this.recyclerView = recyclerView;
        this.themeColor = themeColor;
    }

    public void build() {
        recyclerView.setAdapter(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(parent.getContext(), inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();

        return onCreateDiaryDayViewHolder(parent,themeColorInflater);
    }

    @NonNull
    protected abstract RecyclerView.ViewHolder onCreateDiaryDayViewHolder(
            @NonNull ViewGroup parent, @NonNull LayoutInflater themeColorInflater);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiaryDayListBaseItem item = getItem(position);
        Objects.requireNonNull(item);

        onBindDate(holder, item);
        onBindItemClickListener(holder, item);
        onBindOtherView(holder, item);
    }

    protected abstract void onBindDate(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item);

    protected abstract void onBindItemClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item);

    protected abstract void onBindOtherView(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item);

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(DiaryDayListBaseItem item);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        Objects.requireNonNull(onClickItemListener);

        this.onClickItemListener = onClickItemListener;
    }

    protected void onClickItem(DiaryDayListBaseItem item) {
        Objects.requireNonNull(item);
        if (onClickItemListener == null) return;

        onClickItemListener.onClick(item);
    }

    protected static abstract class DiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListBaseItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryDayListBaseItem oldItem, @NonNull DiaryDayListBaseItem newItem) {
            Log.d("DiaryDayList", "DiffUtil.ItemCallback_areItemsTheSame()");
            Log.d("DiaryDayList", "oldItem_Date:" + oldItem.getDate());
            Log.d("DiaryDayList", "newItem_Date:" + newItem.getDate());

            return oldItem.getDate().equals(newItem.getDate());
        }
    }
}
