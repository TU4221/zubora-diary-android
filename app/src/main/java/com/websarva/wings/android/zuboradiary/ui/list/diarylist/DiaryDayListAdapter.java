package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

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

        // TODO:背面削除ボタン処理保留
            /*holder.binding.textDeleteDiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionDiaryListFragmentToDeleteConfirmationDialog(date);
                    navController.navigate(action);
                }
            });*/
        holder.binding.includeBackground.textDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "削除", Toast.LENGTH_LONG).show();
            }
        });

    }

    public static class DiaryDayListViewHolder extends RecyclerView.ViewHolder {
        public RowDiaryDayListBinding binding;
        public LocalDate date;
        public DiaryDayListViewHolder(RowDiaryDayListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.linerLayoutForeground.setClickable(true);
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
