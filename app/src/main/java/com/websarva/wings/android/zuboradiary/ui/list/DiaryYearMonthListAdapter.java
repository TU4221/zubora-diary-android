package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryYearMonthListBinding;
import com.websarva.wings.android.zuboradiary.ui.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.CustomSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryYearMonthListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultYearMonthListItem;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DiaryYearMonthListAdapter extends ListAdapter<DiaryYearMonthListItemBase, RecyclerView.ViewHolder> {
    private final Context context;
    private final Consumer<LocalDate> processOnChildItemClick;
    private final boolean canSwipeItem;
    private static final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    private static final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private final List<Map<String, Object>> diaryYearMonthList = new ArrayList<>(); // TODO:不要確認後削除
    private final List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();
    public static final int VIEW_TYPE_DIARY = 0;
    public static final int VIEW_TYPE_PROGRESS_BAR = 1;
    public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;

    public DiaryYearMonthListAdapter(
            Context context, Consumer<LocalDate> processOnChildItemClick, boolean canSwipeItem){
        super(new DiaryYearMonthListDiffUtilItemCallback());
        this.context = context;
        this.processOnChildItemClick = processOnChildItemClick;
        this.canSwipeItem = canSwipeItem;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DIARY) {
            RowDiaryYearMonthListBinding binding =
                    RowDiaryYearMonthListBinding
                            .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            DiaryYearMonthListViewHolder holder = new DiaryYearMonthListViewHolder(binding);

            // ホルダーアイテムアニメーション設定(理由は年月RecyclerView設定コード付近にコメントで記載)
            holder.binding.recyclerDayList.setItemAnimator(null);

            // ホルダー内の日記リスト(日)のアイテム装飾設定
            // MEMO:onBindViewHolder で設定すると、設定内容が重複してアイテムが小さくなる為、
            //      onCreateViewHolder で設定。
            holder.binding.recyclerDayList.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(
                        @NonNull Rect outRect,
                        @NonNull View view,
                        @NonNull RecyclerView parent,
                        @NonNull RecyclerView.State state) {
                    Log.d("リスト装飾確認","getItemOffsets()呼び出し");
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.top = DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                    outRect.left = DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;
                    outRect.right = DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;

                    RecyclerView.ViewHolder viewHolder = parent.findContainingViewHolder(view);
                    if (viewHolder == null) {
                        // TODO:assert
                        return;
                    }
                    RecyclerView.Adapter<?> adapter = parent.getAdapter();
                    if (adapter == null) {
                        // TODO:assert
                        return;
                    }
                    Log.d("リスト装飾確認", Integer.toString(viewHolder.getBindingAdapterPosition()));
                    if (viewHolder.getBindingAdapterPosition() == (adapter.getItemCount() - 1)) {
                        outRect.bottom = DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                    }
                }
            });

            if (canSwipeItem) {
                // TODO:上手くいかないので保留
                // ホルダー内の日記リスト(日)のアイテムにスワイプ機能(背面ボタン表示)を設定。
                // MEMO:スワイプでの背面ボタン表示機能はAndroidには存在しないので、
                //      ItemTouchHelper.Callback を継承して作成。
            /*CustomSimpleCallback simpleCallback = new CustomSimpleCallback(
                    ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT,
                    holder.binding.recyclerDayList,
                    getContext(),
                    getResources().getDisplayMetrics().density,
                    getChildFragmentManager(), // TODO:左記不要確認後削除
                    navController,
                    binding.recyclerDiaryYearMonthList);
            simpleCallbacks.add(simpleCallback);
            DiaryDayListSimpleCallBack simpleCallBack =
                    new DiaryDayListSimpleCallBack(
                            ItemTouchHelper.ACTION_STATE_IDLE,
                            ItemTouchHelper.LEFT,
                            new SwipedDiaryDate()
                    );

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallBack);
                itemTouchHelper.attachToRecyclerView(holder.binding.recyclerDayList);*/
            }

            return holder;
        } else if (viewType == VIEW_TYPE_PROGRESS_BAR) {
            View view =
                    inflater.inflate(R.layout.row_progress_bar, parent, false);
            return new ProgressBarViewHolder(view);
        } else {
            View view =
                    inflater.inflate(R.layout.row_no_diary_message, parent, false);
            return new NoDiaryMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DiaryYearMonthListViewHolder) {
            DiaryYearMonthListViewHolder _holder =
                    (DiaryYearMonthListViewHolder) holder;
            // 対象行の情報を取得
            DiaryYearMonthListItemBase item = getItem(position);
            YearMonth diaryYearMonth = item.getYearMonth();

            // セクションバー設定
            // 左端に余白を持たせる為、最初にスペースを入力。
            String diaryDate = "  " + diaryYearMonth.getYear() + context.getString(R.string.row_list_year)
                    + diaryYearMonth.getMonthValue() + context.getString(R.string.row_list_month);
            _holder.binding.textSectionBar.setText(diaryDate);
            // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
            _holder.binding.textSectionBar.setY(0);

            // 日記リスト(日)設定
            // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、
            //      日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
            _holder.binding.recyclerDayList.setLayoutManager(new LinearLayoutManager(context));

            if (item instanceof DiaryYearMonthListItem) {
                DiaryYearMonthListItem _item = (DiaryYearMonthListItem) item;
                List<DiaryDayListItem> diaryDayList = _item.getDiaryDayListItemList();
                DiaryDayListAdapter diaryDayListAdapter = new DiaryDayListAdapter(context, processOnChildItemClick);
                _holder.binding.recyclerDayList.setAdapter(diaryDayListAdapter);
                diaryDayListAdapter.submitList(diaryDayList);
            } else if (item instanceof WordSearchResultYearMonthListItem) {
                WordSearchResultYearMonthListItem _item = (WordSearchResultYearMonthListItem) item;
                // TODO:保留
            }
        }
    }

    @Override
    public int getItemViewType(int position ) {
        DiaryYearMonthListItemBase item = getItem(position);
        return item.getViewType();
    }

    // 日記リスト(年月)の指定したアイテムを削除。
    // TODO:スワイプ機能搭載後不要か判断
    public void deleteItem(int position) {
        diaryYearMonthList.remove(position);
        notifyItemRemoved(position);
    }

    // 日記リスト(年月)の一つのアイテム内の日記リスト(日)アイテムをスワイプした時、
    // 他の日記リスト(年月)のアイテム内の日記リスト(日)の全アイテムをスワイプ前の状態に戻す。
    public void recoverOtherSwipedItem(CustomSimpleCallback customSimpleCallback) {
        for (int i = 0; i < simpleCallbacks.size(); i++) {
            if (customSimpleCallback != simpleCallbacks.get(i)) {
                simpleCallbacks.get(i).recoverSwipeItem();
            }
        }
    }

    public static class DiaryYearMonthListViewHolder extends DiaryYearMonthListBaseViewHolder {
        RowDiaryYearMonthListBinding binding;

        public DiaryYearMonthListViewHolder(RowDiaryYearMonthListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class NoDiaryMessageViewHolder extends RecyclerView.ViewHolder {
        public NoDiaryMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class DiaryYearMonthListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<DiaryYearMonthListItemBase> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryYearMonthListItemBase oldItem, @NonNull DiaryYearMonthListItemBase newItem) {
            // MEMO:更新時はリストアイテムを再インスタンス化する為、IDが異なり全アイテムfalseとなり、
            //      更新時リストが最上部へスクロールされてしまう。これを防ぐために下記処理を記述。
            // return oldItem.getId().equals(newItem.getId());
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListItemBase oldItem, @NonNull DiaryYearMonthListItemBase newItem) {
            // 年月
            if (oldItem.getYearMonth() != null && newItem.getYearMonth() != null
                    && !oldItem.getYearMonth().equals(newItem.getYearMonth())) {
                return false;
            }
            if (oldItem.getViewType() != newItem.getViewType()) {
                return false;
            }

            // 日
            if (oldItem instanceof DiaryYearMonthListItem && newItem instanceof DiaryYearMonthListItem) {
                DiaryYearMonthListItem _oldItem = (DiaryYearMonthListItem) oldItem;
                DiaryYearMonthListItem _newItem = (DiaryYearMonthListItem) newItem;
                int _oldChildListSize = _oldItem.getDiaryDayListItemList().size();
                int _newChildListSize = _newItem.getDiaryDayListItemList().size();
                Log.d("DiaryList", "_oldChildListSize:" + _oldChildListSize);
                Log.d("DiaryList", "_newChildListSize:" + _newChildListSize);
                if (_oldChildListSize != _newChildListSize) {
                    return false;
                }

                for (int i = 0; i < _oldChildListSize; i++) {
                    DiaryDayListItem oldChildListItem = _oldItem.getDiaryDayListItemList().get(i);
                    DiaryDayListItem newChildListItem = _newItem.getDiaryDayListItemList().get(i);
                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        return false;
                    }
                    if (!oldChildListItem.getPicturePath().equals(newChildListItem.getPicturePath())) {
                        return false;
                    }
                }
            } else if (oldItem instanceof WordSearchResultYearMonthListItem
                            && newItem instanceof WordSearchResultYearMonthListItem) {
                WordSearchResultYearMonthListItem _oldItem = (WordSearchResultYearMonthListItem) oldItem;
                WordSearchResultYearMonthListItem _newItem = (WordSearchResultYearMonthListItem) newItem;
                int oldChildListSize = _oldItem.getWordSearchResultDayList().size();
                int newChildListSize = _newItem.getWordSearchResultDayList().size();
                if (oldChildListSize != newChildListSize) {
                    return false;
                }

                for (int i = 0; i < oldChildListSize; i++) {
                    WordSearchResultDayListItem oldChildListItem =
                            _oldItem.getWordSearchResultDayList().get(i);
                    WordSearchResultDayListItem newChildListItem =
                            _newItem.getWordSearchResultDayList().get(i);
                    if (oldChildListItem.getDayOfMonth() != newChildListItem.getDayOfMonth()) {
                        return false;
                    }
                    if (oldChildListItem.getDayOfWeek() != null
                            && newChildListItem.getDayOfWeek() != null
                            && !oldChildListItem.getDayOfWeek().equals(newChildListItem.getDayOfWeek())) {
                        return false;
                    }
                    if (oldChildListItem.getTitle() != null
                            && newChildListItem.getTitle() != null
                            && !oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        return false;
                    }
                    if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                        return false;
                    }
                    if (oldChildListItem.getItemTitle() != null
                            && newChildListItem.getItemTitle() != null
                            && !oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                        return false;
                    }
                    if (oldChildListItem.getItemComment() != null
                            && newChildListItem.getItemComment() != null
                            && !oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
