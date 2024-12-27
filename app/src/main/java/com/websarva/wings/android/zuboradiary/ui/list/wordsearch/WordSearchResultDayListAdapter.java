package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;

import java.time.LocalDate;
import java.util.Objects;

public class WordSearchResultDayListAdapter extends DiaryDayListBaseAdapter {

    public WordSearchResultDayListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor){
        super(context, recyclerView, themeColor, new DiffUtilItemCallback());
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateDiaryDayViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater themeColorInflater) {
        RowWordSearchResultListBinding binding =
                RowWordSearchResultListBinding.inflate(themeColorInflater, parent, false);
        return new WordSearchResultDayViewHolder(binding);
    }

    @Override
    protected void onBindDate(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        LocalDate date = item.getDate();
        DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(context);
        String strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.getDayOfWeek());
        WordSearchResultDayViewHolder _holder = (WordSearchResultDayViewHolder) holder;
        _holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);
        _holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
    }

    @Override
    protected void onBindItemClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        LocalDate date = item.getDate();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItem(date);
            }
        });
    }

    @Override
    protected void onBindOtherView(@NonNull RecyclerView.ViewHolder holder, @NonNull DiaryDayListBaseItem item) {
        WordSearchResultDayViewHolder _holder = (WordSearchResultDayViewHolder) holder;
        WordSearchResultDayListItem _item = (WordSearchResultDayListItem) item;

        onBindTitle(_holder, _item);
        onBindItem(_holder, _item);
    }

    private void onBindTitle(WordSearchResultDayViewHolder holder, WordSearchResultDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        SpannableString title = item.getTitle();
        holder.binding.textTitle.setText(title);
    }

    private void onBindItem(WordSearchResultDayViewHolder holder, WordSearchResultDayListItem item) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(item);

        ItemNumber itemNumber = item.getItemNumber();
        SpannableString itemTitle = item.getItemTitle();
        SpannableString itemComment = item.getItemComment();
        String strItemNumber = context.getString(R.string.fragment_word_search_result_item) + itemNumber;
        holder.binding.textItemNumber.setText(strItemNumber);
        holder.binding.textItemTitle.setText(itemTitle);
        holder.binding.textItemComment.setText(itemComment);
    }

    public static class WordSearchResultDayViewHolder extends RecyclerView.ViewHolder {

        public RowWordSearchResultListBinding binding;

        public WordSearchResultDayViewHolder(RowWordSearchResultListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class DiffUtilItemCallback extends DiaryDayListBaseAdapter.DiffUtilItemCallback {

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListBaseItem oldItem, @NonNull DiaryDayListBaseItem newItem) {
            Log.d("WordSearchResultDayList", "DiffUtil.ItemCallback_areContentsTheSame()");
            WordSearchResultDayListItem _oldItem = (WordSearchResultDayListItem) oldItem;
            WordSearchResultDayListItem _newItem = (WordSearchResultDayListItem) newItem;

            if (!_oldItem.getTitle().equals(_newItem.getTitle())) {
                Log.d("WordSearchResultDayList", "Title不一致");
                return false;
            }
            if (_oldItem.getItemNumber() != _newItem.getItemNumber()) {
                Log.d("WordSearchResultDayList", "ItemNumber不一致");
                return false;
            }
            if (!_oldItem.getItemTitle().equals(_newItem.getItemTitle())) {
                Log.d("WordSearchResultDayList", "ItemTitle不一致");
                return false;
            }
            if (!_oldItem.getItemComment().equals(_newItem.getItemComment())) {
                Log.d("WordSearchResultDayList", "ItemComment不一致");
                return false;
            }
            return true;
        }
    }
}
