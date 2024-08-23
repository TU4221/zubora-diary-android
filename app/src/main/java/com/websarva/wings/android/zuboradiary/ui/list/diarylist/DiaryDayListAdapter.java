package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekConverter;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;

import java.time.LocalDate;

public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListAdapter.DiaryDayListViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final OnClickItemListener onClickItemListener;
    private final OnClickDeleteButtonListener onClickDeleteButtonListener;

    public DiaryDayListAdapter(
            Context context,
            RecyclerView recyclerView,
            OnClickItemListener onClickItemListener,
            OnClickDeleteButtonListener onClickDeleteButtonListener) {
        super(new DiaryDayListDiffUtilItemCallback());
        this.context = context;
        this.recyclerView = recyclerView;
        this.onClickItemListener = onClickItemListener;
        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    public void build() {
        recyclerView.setAdapter(this);
    }

    @NonNull
    @Override
    public DiaryDayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowDiaryDayListBinding binding =
                RowDiaryDayListBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DiaryDayListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DiaryDayListViewHolder holder, int position) {
        DiaryDayListItem item = getItem(position);
        LocalDate date = item.getDate();
        String title = item.getTitle();
        String picturePath = item.getPicturePath();

        holder.date = date; // ホルダー毎に日記の日付情報一式付与

        DayOfWeekConverter dayOfWeekConverter = new DayOfWeekConverter(context);
        String strDayOfWeek = dayOfWeekConverter.toStringShortName(date.getDayOfWeek());
        holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);

        holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
        holder.binding.textRowDiaryListDayTitle.setText(title);
        // TODO:picturePath

        holder.binding.linerLayoutForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItemListener.onClick(date);
            }
        });

        holder.binding.includeBackground.textDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDeleteButtonListener.onClick(date);
            }
        });

    }

    public static class DiaryDayListViewHolder extends DiaryListSimpleCallback.LeftSwipeViewHolder {
        public RowDiaryDayListBinding binding;
        public LocalDate date;
        public DiaryDayListViewHolder(@NonNull RowDiaryDayListBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        void setUpView(@NonNull ViewDataBinding binding) {
            RowDiaryDayListBinding rowDiaryDayListBinding;
            if (binding instanceof RowDiaryDayListBinding) {
                rowDiaryDayListBinding = (RowDiaryDayListBinding) binding;
                foregroundView = rowDiaryDayListBinding.linerLayoutForeground;
                backgroundButtonView = rowDiaryDayListBinding.includeBackground.textDeleteButton;
            }
        }
    }

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(LocalDate date);
    }

    @FunctionalInterface
    public interface OnClickDeleteButtonListener {
        void onClick(LocalDate date);
    }

    public static class DiaryDayListDiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            if (!oldItem.getDate().equals(newItem.getDate())) {
                return false;
            }
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                return false;
            }
            if (!oldItem.getPicturePath().equals(newItem.getPicturePath())) {
                return false;
            }
            return true;
        }
    }
}
