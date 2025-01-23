package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.ui.DiaryPictureManager;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryDayListBaseAdapter;

import java.time.LocalDate;
import java.util.Objects;

public class DiaryDayListAdapter extends SwipeDiaryDayListBaseAdapter {

    public DiaryDayListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor) {
        super(context, recyclerView, themeColor, new DiaryDayListDiffUtilItemCallback());
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateDiaryDayViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater themeColorInflater) {
        RowDiaryDayListBinding binding =
                RowDiaryDayListBinding.inflate(themeColorInflater, parent, false);
        return new DiaryDayListViewHolder(binding);
    }

    @Override
    protected void onBindDate(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        DiaryDayListViewHolder _holder = (DiaryDayListViewHolder) holder;

        LocalDate date = item.getDate();
        DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(context);
        String strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.getDayOfWeek());
        _holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);
        _holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
    }

    @Override
    protected void onBindItemClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        DiaryDayListViewHolder _holder = (DiaryDayListViewHolder) holder;
        _holder.binding.linerLayoutForeground.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            onClickItem(item);
        });
    }

    @Override
    protected void onBindOtherView(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        DiaryDayListViewHolder _holder = (DiaryDayListViewHolder) holder;
        DiaryDayListItem _item = (DiaryDayListItem) item;

        onBindTitle(_holder, _item);
        onBindPicture(_holder, _item);
    }

    private void onBindTitle(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        String title = item.getTitle();
        holder.binding.textTitle.setText(title);
    }

    private void onBindPicture(DiaryDayListViewHolder holder, DiaryDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        DiaryPictureManager diaryPictureManager =
                new DiaryPictureManager(
                        context,
                        holder.binding.imageAttachedPicture,
                        themeColor.getOnSecondaryContainerColor(context.getResources())
                );
        Uri pictureUri = item.getPicturePath();
        diaryPictureManager.setUpPictureOnDiaryList(pictureUri);
    }

    @Override
    protected void onBindDeleteButtonClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        DiaryDayListViewHolder _holder = (DiaryDayListViewHolder) holder;

        _holder.binding.includeBackground.imageButtonDelete.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            onClickDeleteButton(item);
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

    private static class DiaryDayListDiffUtilItemCallback extends DiaryDayListBaseAdapter.DiffUtilItemCallback {

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListBaseItem oldItem, @NonNull DiaryDayListBaseItem newItem) {
            DiaryDayListItem _oldItem = (DiaryDayListItem) oldItem;
            DiaryDayListItem _newItem = (DiaryDayListItem) newItem;

            Log.d("DiaryDayList", "DiffUtil.ItemCallback_areContentsTheSame()");
            if (!_oldItem.getTitle().equals(_newItem.getTitle())) {
                Log.d("DiaryDayList", "Title不一致");
                return false;
            }
            if (_oldItem.getPicturePath() == null && _newItem.getPicturePath() != null) {
                Log.d("DiaryDayList", "PicturePath不一致");
                return false;
            }
            if (_oldItem.getPicturePath() != null && _newItem.getPicturePath() == null) {
                Log.d("DiaryDayList", "PicturePath不一致");
                return false;
            }
            if ((_oldItem.getPicturePath() != null && _newItem.getPicturePath() != null)
                    && (!_oldItem.getPicturePath().equals(_newItem.getPicturePath()))) {
                Log.d("DiaryDayList", "PicturePath不一致");
                return false;
            }
            return true;
        }
    }
}
