package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekConverter;
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultBinding;

import java.time.LocalDate;
import java.util.function.Consumer;

public class WordSearchResultDayListAdapter
        extends ListAdapter<WordSearchResultDayListItem, WordSearchResultDayListAdapter.WordSearchResultDayViewHolder> {

    private final Context context;
    private final Consumer<LocalDate> processOnClick;

    public WordSearchResultDayListAdapter(Context context, Consumer<LocalDate> processOnClick){
        super(new WordSearchResultDayListDiffUtilItemCallback());
        this.context = context;
        this.processOnClick = processOnClick;
    }

    //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
    @NonNull
    @Override
    public WordSearchResultDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowWordSearchResultBinding binding =
                RowWordSearchResultBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WordSearchResultDayViewHolder(binding);
    }

    //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
    @Override
    public void onBindViewHolder(WordSearchResultDayViewHolder holder, int position) {
        WordSearchResultDayListItem item = getItem(position);
        LocalDate date = item.getDate();
        DayOfWeekConverter dayOfWeekConverter = new DayOfWeekConverter(context);
        String strDayOfWeek = dayOfWeekConverter.toStringShortName(date.getDayOfWeek());
        SpannableString title = item.getTitle();
        int itemNumber = item.getItemNumber();
        SpannableString itemTitle = item.getItemTitle();
        SpannableString itemComment = item.getItemComment();
        holder.date = date; // ホルダー毎に日記の日付情報一式付与
        holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);
        holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
        holder.binding.textWordSearchResultTitle.setText(title);
        String strItemNumber = context.getString(R.string.fragment_word_search_result_item) + itemNumber;
        holder.binding.textWordSearchResultItemNumber.setText(strItemNumber);
        holder.binding.textWordSearchResultItemTitle.setText(itemTitle);
        holder.binding.textWordSearchResultItemComment.setText(itemComment);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processOnClick.accept(date);
            }
        });
    }

    public static class WordSearchResultDayViewHolder extends RecyclerView.ViewHolder {

        public RowWordSearchResultBinding binding;
        public LocalDate date;

        public WordSearchResultDayViewHolder(RowWordSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class WordSearchResultDayListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<WordSearchResultDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
            if (!oldItem.getDate().equals(newItem.getDate())) {
                return false;
            }
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                return false;
            }
            if (oldItem.getItemNumber() != newItem.getItemNumber()) {
                return false;
            }
            if (!oldItem.getItemTitle().equals(newItem.getItemTitle())) {
                return false;
            }
            if (!oldItem.getItemComment().equals(newItem.getItemComment())) {
                return false;
            }
            return true;
        }
    }
}
