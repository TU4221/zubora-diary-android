package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding;
import com.websarva.wings.android.zuboradiary.ui.LeftSwipeSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;

import java.util.Objects;

class ItemTitleSelectionHistoryListAdapter
        extends ListAdapter<SelectionHistoryListItem,
                        ItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private OnClickItemListener onClickItemListener;
    private OnClickDeleteButtonListener onClickDeleteButtonListener;

    private LeftSwipeSimpleCallback leftSwipeSimpleCallback;

    ItemTitleSelectionHistoryListAdapter(
            Context context, RecyclerView recyclerView, ThemeColor themeColor) {
        super(new DiaryItemTitleSelectionHistoryDiffUtilItemCallback());
        this.context = context;
        this.recyclerView = recyclerView;
        this.themeColor = themeColor;
    }

    void build() {
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        );
        leftSwipeSimpleCallback = new LeftSwipeSimpleCallback(recyclerView);
        leftSwipeSimpleCallback.build();
    }

    @NonNull
    @Override
    public ItemTitleSelectionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ThemeColorInflaterCreator creator = new ThemeColorInflaterCreator(context, inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();
        RowItemTitleSelectionHistoryBinding binding =
                RowItemTitleSelectionHistoryBinding.inflate(themeColorInflater, parent, false);
       return new ItemTitleSelectionHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemTitleSelectionHistoryViewHolder holder, int position) {
        SelectionHistoryListItem item = getItem(position);
        String title = item.getTitle();
        holder.binding.textTitle.setText(title);
        holder.binding.textTitle.setOnClickListener(v -> {
            Objects.requireNonNull(v);
            if (onClickItemListener == null) return;

            onClickItemListener.onClick(title);
        });
        holder.binding.includeBackground.imageButtonDelete.setOnClickListener(v -> {
            Objects.requireNonNull(v);
            if (onClickDeleteButtonListener == null) return;

            // MEMO:onBindViewHolder()の引数であるpositionを使用すると警告がでる。
            onClickDeleteButtonListener.onClick(holder.getBindingAdapterPosition(), title);
        });
    }

    @FunctionalInterface
    interface OnClickItemListener {
        void onClick(@NonNull String title);
    }

    void setOnClickItemListener(@Nullable OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    @FunctionalInterface
    interface OnClickDeleteButtonListener {
        void onClick(int position,@NonNull String title);
    }

    void setOnClickDeleteButtonListener(@Nullable OnClickDeleteButtonListener onClickDeleteButtonListener) {
        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    static class ItemTitleSelectionHistoryViewHolder
            extends LeftSwipeSimpleCallback.LeftSwipeViewHolder {
        public final RowItemTitleSelectionHistoryBinding binding;

        ItemTitleSelectionHistoryViewHolder(RowItemTitleSelectionHistoryBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void setUpView(@NonNull ViewBinding binding) {
            RowItemTitleSelectionHistoryBinding rowItemTitleSelectionHistoryBinding =
                    (RowItemTitleSelectionHistoryBinding) binding;
            foregroundView = rowItemTitleSelectionHistoryBinding.textTitle;
            backgroundButtonView = rowItemTitleSelectionHistoryBinding.includeBackground.imageButtonDelete;
        }
    }

    static class DiaryItemTitleSelectionHistoryDiffUtilItemCallback
            extends DiffUtil.ItemCallback<SelectionHistoryListItem> {

        @Override
        public boolean areItemsTheSame(@NonNull SelectionHistoryListItem oldItem, @NonNull SelectionHistoryListItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SelectionHistoryListItem oldItem, @NonNull SelectionHistoryListItem newItem) {
            return false;
        }
    }

    void closeSwipedItem() {
        leftSwipeSimpleCallback.closeSwipedItem();
    }
}
