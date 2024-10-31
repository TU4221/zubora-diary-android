package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;

import java.time.LocalDate;
import java.util.Objects;

public class WordSearchResultDayListAdapter
        extends ListAdapter<WordSearchResultDayListItem, WordSearchResultDayListAdapter.WordSearchResultDayViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private OnClickItemListener onClickItemListener;

    public WordSearchResultDayListAdapter(Context context, RecyclerView recyclerView, ThemeColor themeColor){
        super(new WordSearchResultDayListDiffUtilItemCallback());

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

    //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
    @NonNull
    @Override
    public WordSearchResultDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(context, inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();
        RowWordSearchResultListBinding binding =
                RowWordSearchResultListBinding.inflate(themeColorInflater, parent, false);
        return new WordSearchResultDayViewHolder(binding);
    }

    //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
    @Override
    public void onBindViewHolder(WordSearchResultDayViewHolder holder, int position) {
        WordSearchResultDayListItem item = getItem(position);
        LocalDate date = item.getDate();
        DayOfWeekStringConverter dayOfWeekStringConverter = new DayOfWeekStringConverter(context);
        String strDayOfWeek = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.getDayOfWeek());
        SpannableString title = item.getTitle();
        ItemNumber itemNumber = item.getItemNumber();
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
                if (onClickItemListener == null) {
                    return;
                }
                onClickItemListener.onClick(date);
            }
        });
    }

    @FunctionalInterface
    public interface OnClickItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public static class WordSearchResultDayViewHolder extends RecyclerView.ViewHolder {

        public RowWordSearchResultListBinding binding;
        public LocalDate date;

        public WordSearchResultDayViewHolder(RowWordSearchResultListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class WordSearchResultDayListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<WordSearchResultDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
            Log.d("WordSearchResultDayList", "DiffUtil.ItemCallback_areItemsTheSame()");
            Log.d("WordSearchResultDayList", "oldItem_Date:" + oldItem.getDate());
            Log.d("WordSearchResultDayList", "newItem_Date:" + newItem.getDate());
            return oldItem.getDate().equals(newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
            Log.d("WordSearchResultDayList", "DiffUtil.ItemCallback_areContentsTheSame()");
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                Log.d("WordSearchResultDayList", "Title不一致");
                return false;
            }
            if (oldItem.getItemNumber() != newItem.getItemNumber()) {
                Log.d("WordSearchResultDayList", "ItemNumber不一致");
                return false;
            }
            if (!oldItem.getItemTitle().equals(newItem.getItemTitle())) {
                Log.d("WordSearchResultDayList", "ItemTitle不一致");
                return false;
            }
            if (!oldItem.getItemComment().equals(newItem.getItemComment())) {
                Log.d("WordSearchResultDayList", "ItemComment不一致");
                return false;
            }
            return true;
        }
    }
}
