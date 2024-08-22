package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekConverter;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;

import java.time.LocalDate;
import java.util.function.Consumer;

public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListAdapter.DiaryDayListViewHolder> {

    private final Context context;
    private final Consumer<LocalDate> processOnClick;

    public DiaryDayListAdapter(Context context, Consumer<LocalDate> processOnClick) {
        super(new DiaryDayListDiffUtilItemCallback());
        this.context = context;
        this.processOnClick = processOnClick;
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
                processOnClick.accept(date);
            }
        });

        holder.binding.includeBackground.textDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "削除", Toast.LENGTH_LONG).show();
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
