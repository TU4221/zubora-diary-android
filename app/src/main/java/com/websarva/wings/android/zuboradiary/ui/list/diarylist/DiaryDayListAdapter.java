package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;
import com.websarva.wings.android.zuboradiary.ui.list.ListThemeColorSwitcher;

import java.time.LocalDate;

public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListAdapter.DiaryDayListViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private OnClickItemListener onClickItemListener;
    private OnClickDeleteButtonListener onClickDeleteButtonListener;

    public DiaryDayListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor) {
        super(new DiaryDayListDiffUtilItemCallback());

        if (context == null) {
            throw new NullPointerException();
        }
        if (recyclerView == null) {
            throw new NullPointerException();
        }
        if (themeColor == null) {
            throw new NullPointerException();
        }

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
                new ThemeColorInflaterCreator(context, inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();

        RowDiaryDayListBinding binding =
                RowDiaryDayListBinding
                        .inflate(themeColorInflater, parent, false);
        return new DiaryDayListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DiaryDayListViewHolder holder, int position) {
        DiaryDayListItem item = getItem(position);
        LocalDate date = item.getDate();
        String title = item.getTitle();
        String picturePath = item.getPicturePath();

        holder.date = date; // ホルダー毎に日記の日付情報一式付与

        DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(context);
        String strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.getDayOfWeek());
        holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);

        holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
        holder.binding.textRowDiaryListDayTitle.setText(title);
        // TODO:picturePath

        holder.binding.linerLayoutForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickItemListener == null) {
                    return;
                }
                onClickItemListener.onClick(date);
            }
        });

        holder.binding.includeBackground.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickDeleteButtonListener == null) {
                    return;
                }
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
                backgroundButtonView = rowDiaryDayListBinding.includeBackground.imageButtonDelete;
            }
        }
    }

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    @FunctionalInterface
    public interface OnClickDeleteButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickDeleteButtonListener(OnClickDeleteButtonListener onClickDeleteButtonListener) {
        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
    }

    public static class DiaryDayListDiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListItem> {
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
