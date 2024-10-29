package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowItemTitleSelectionHistoryBinding;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;

import java.util.Objects;

public class ItemTitleSelectionHistoryListAdapter
        extends ListAdapter<DiaryItemTitleSelectionHistoryItem,
                        ItemTitleSelectionHistoryListAdapter.ItemTitleSelectionHistoryViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private final OnClickItemListener onClickItemListener;
    private final OnClickDeleteButtonListener onClickDeleteButtonListener;

    private ItemTitleSelectionHistorySimpleCallback itemTitleSelectionHistorySimpleCallback;

    public ItemTitleSelectionHistoryListAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            OnClickItemListener onClickItemListener,
            OnClickDeleteButtonListener onClickDeleteButtonListener){
        super(new DiaryItemTitleSelectionHistoryDiffUtilItemCallback());
        this.context = context;
        this.recyclerView = recyclerView;
        this.themeColor = themeColor;
        this.onClickItemListener = onClickItemListener;
        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    public void build() {
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        );
        itemTitleSelectionHistorySimpleCallback =
                new ItemTitleSelectionHistorySimpleCallback(recyclerView);
        itemTitleSelectionHistorySimpleCallback.build();
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
    public void onBindViewHolder(ItemTitleSelectionHistoryViewHolder holder, int position) {
        DiaryItemTitleSelectionHistoryItem item = getItem(position);
        String title = item.getTitle();
        Objects.requireNonNull(title);

        holder.binding.textItemTitle.setText(title);
        holder.binding.textItemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                onClickItemListener.onClick(title);
            }
        });
        holder.binding.includeBackground.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                // MEMO:onBindViewHolder()の引数であるpositionを使用すると警告がでる。
                onClickDeleteButtonListener.onClick(holder.getBindingAdapterPosition(), title);
            }
        });
    }

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(@NonNull String title);
    }

    @FunctionalInterface
    public interface OnClickDeleteButtonListener {
        void onClick(int position,@NonNull String title);
    }

    public static class ItemTitleSelectionHistoryViewHolder
            extends ItemTitleSelectionHistorySimpleCallback.LeftSwipeViewHolder {
        public RowItemTitleSelectionHistoryBinding binding;

        public ItemTitleSelectionHistoryViewHolder(RowItemTitleSelectionHistoryBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void setUpView(@NonNull ViewDataBinding binding) {
            RowItemTitleSelectionHistoryBinding rowItemTitleSelectionHistoryBinding;
            if (binding instanceof RowItemTitleSelectionHistoryBinding) {
                rowItemTitleSelectionHistoryBinding = (RowItemTitleSelectionHistoryBinding) binding;
            } else {
                return;
            }
            foregroundView = rowItemTitleSelectionHistoryBinding.textItemTitle;
            backgroundButtonView = rowItemTitleSelectionHistoryBinding.includeBackground.imageButtonDelete;
        }
    }

    public static class DiaryItemTitleSelectionHistoryDiffUtilItemCallback
            extends DiffUtil.ItemCallback<DiaryItemTitleSelectionHistoryItem> {
        @Override
        public boolean areItemsTheSame(
                @NonNull DiaryItemTitleSelectionHistoryItem oldItem,
                @NonNull DiaryItemTitleSelectionHistoryItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull DiaryItemTitleSelectionHistoryItem oldItem,
                @NonNull DiaryItemTitleSelectionHistoryItem newItem) {
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                return false;
            }
            if (oldItem.getLog().equals(newItem.getLog())) {
                return false;
            }
            return true;
        }
    }

    public void closeSwipedItem() {
        itemTitleSelectionHistorySimpleCallback.closeSwipedItem();
    }
}
