package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;

import java.time.LocalDate;
import java.util.Objects;

public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListAdapter.DiaryDayListViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private OnClickItemListener onClickItemListener;
    private OnClickDeleteButtonListener onClickDeleteButtonListener;

    public DiaryDayListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor) {
        super(new DiaryDayListDiffUtilItemCallback());
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
    public DiaryDayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(parent.getContext(), inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();

        RowDiaryDayListBinding binding =
                RowDiaryDayListBinding.inflate(themeColorInflater, parent, false);
        return new DiaryDayListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryDayListViewHolder holder, int position) {
        DiaryDayListItem item = getItem(position);
        Objects.requireNonNull(item);

        setUpDate(holder, item);
        setUpTitle(holder, item);
        setUpPicture(holder, item);
        setUpClickListener(holder, item);
    }

    private void setUpDate(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        LocalDate date = item.getDate();

        DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(context);
        String strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.getDayOfWeek());
        holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);

        holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
    }

    private void setUpTitle(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        String title = item.getTitle();
        holder.binding.textTitle.setText(title);
    }

    private void setUpPicture(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        String picturePath = item.getPicturePath();
        // TODO:picturePath
    }

    private void setUpClickListener(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        LocalDate date = item.getDate();
        holder.binding.linerLayoutForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);
                if (onClickItemListener == null) return;

                onClickItemListener.onClick(date);
            }
        });

        holder.binding.includeBackground.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);
                if (onClickDeleteButtonListener == null) return;

                onClickDeleteButtonListener.onClick(date);
            }
        });
    }

    public static class DiaryDayListViewHolder extends DiaryListSimpleCallback.LeftSwipeViewHolder {

        public final RowDiaryDayListBinding binding;

        public DiaryDayListViewHolder(@NonNull RowDiaryDayListBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        protected void setUpView(@NonNull ViewBinding binding) {
            RowDiaryDayListBinding rowDiaryDayListBinding;
            if (binding instanceof RowDiaryDayListBinding) {
                rowDiaryDayListBinding = (RowDiaryDayListBinding) binding;
                foregroundView = rowDiaryDayListBinding.linerLayoutForeground;
                backgroundButtonView = rowDiaryDayListBinding.includeBackground.imageButtonDelete;
            }
        }
    }

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        Objects.requireNonNull(onClickItemListener);

        this.onClickItemListener = onClickItemListener;
    }

    @FunctionalInterface
    public interface OnClickDeleteButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickDeleteButtonListener(OnClickDeleteButtonListener onClickDeleteButtonListener) {
        Objects.requireNonNull(onClickDeleteButtonListener);

        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    private static class DiaryDayListDiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            Log.d("DiaryDayList", "DiffUtil.ItemCallback_areItemsTheSame()");
            Log.d("DiaryDayList", "oldItem_Date:" + oldItem.getDate());
            Log.d("DiaryDayList", "newItem_Date:" + newItem.getDate());
            return oldItem.getDate().equals(newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            Log.d("DiaryDayList", "DiffUtil.ItemCallback_areContentsTheSame()");
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                Log.d("DiaryDayList", "Title不一致");
                return false;
            }
            if (!oldItem.getPicturePath().equals(newItem.getPicturePath())) {
                Log.d("DiaryDayList", "PicturePath不一致");
                return false;
            }
            return true;
        }
    }
}
