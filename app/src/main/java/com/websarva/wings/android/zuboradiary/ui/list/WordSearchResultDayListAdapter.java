package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultBinding;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchFragment;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchFragmentDirections;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListItem;

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
        int year = item.getYear();
        int month = item.getMonth();
        int dayOfMonth = item.getDayOfMonth();
        String dayOfWeek = item.getDayOfWeek();
        SpannableString title = item.getTitle();
        int itemNumber = item.getItemNumber();
        SpannableString itemTitle = item.getItemTitle();
        SpannableString itemComment = item.getItemComment();
        holder.date = DateConverter.toStringLocalDate(year, month, dayOfMonth); // ホルダー毎に日記の日付情報一式付与
        holder.textDayOfMonth.setText(dayOfWeek);
        holder.textDayOfMonth.setText(String.valueOf(dayOfMonth));
        holder.textWordSearchResultTitle.setText(title);
        String stringItemNumber = "項目" + String.valueOf(itemNumber);
        holder.textWordSearchResultItemNumber.setText(stringItemNumber);
        holder.textWordSearchResultItemTitle.setText(itemTitle);
        holder.textWordSearchResultItemComment.setText(itemComment);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processOnClick.accept();
            }
        });
    }

    public static class WordSearchResultDayViewHolder extends RecyclerView.ViewHolder {

        private final RowWordSearchResultBinding binding;
        private LocalDate date;

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
            if (oldItem.getDayOfMonth() != newItem.getDayOfMonth()) {
                return false;
            }
            if (oldItem.getDayOfWeek() != null
                    && newItem.getDayOfWeek() != null
                    && !oldItem.getDayOfWeek().equals(newItem.getDayOfWeek())) {
                return false;
            }
            if (oldItem.getTitle() != null
                    && newItem.getTitle() != null
                    && !oldItem.getTitle().equals(newItem.getTitle())) {
                return false;
            }
            if (oldItem.getItemNumber() != newItem.getItemNumber()) {
                return false;
            }
            if (oldItem.getItemTitle() != null
                    && newItem.getItemTitle() != null
                    && !oldItem.getItemTitle().equals(newItem.getItemTitle())) {
                return false;
            }
            if (oldItem.getItemComment() != null
                    && newItem.getItemComment() != null
                    && !oldItem.getItemComment().equals(newItem.getItemComment())) {
                return false;
            }
            return true;
        }
    }
}
