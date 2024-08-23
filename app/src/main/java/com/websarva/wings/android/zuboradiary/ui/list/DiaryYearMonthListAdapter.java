package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryYearMonthListBinding;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryYearMonthListItem;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultYearMonthListItem;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DiaryYearMonthListAdapter extends ListAdapter<DiaryYearMonthListItemBase, RecyclerView.ViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final OnScrollEndItemLoadingListener onScrollEndItemLoadingListener;
    private final OnScrollLoadingConfirmationListener onScrollLoadingConfirmationListener;
    private final OnClickChildItemListener onClickChildItemListener;
    private final OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener;
    private final boolean canSwipeItem;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private final List<DiaryListSimpleCallback> simpleCallbackList = new ArrayList<>();
    public static final int VIEW_TYPE_DIARY = 0;
    public static final int VIEW_TYPE_PROGRESS_BAR = 1;
    public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;

    public DiaryYearMonthListAdapter(
            Context context,
            RecyclerView recyclerView,
            OnScrollEndItemLoadingListener onScrollEndItemLoadingListener,
            OnScrollLoadingConfirmationListener onScrollLoadingConfirmationListener,
            OnClickChildItemListener onClickChildItemListener,
            boolean canSwipeItem,
            @Nullable OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener){
        super(new DiaryYearMonthListDiffUtilItemCallback());
        this.context = context;
        this.recyclerView = recyclerView;
        this.onScrollEndItemLoadingListener = onScrollEndItemLoadingListener;
        this.onScrollLoadingConfirmationListener = onScrollLoadingConfirmationListener;
        this.onClickChildItemListener = onClickChildItemListener;
        this.canSwipeItem = canSwipeItem;
        this.onClickChildItemBackgroundButtonListener = onClickChildItemBackgroundButtonListener;
    }

    public void build() {
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // スクロール時スワイプ閉
                    if (canSwipeItem) {
                        closeSwipedItemOtherDayList(null);
                    }
                }
            }
        });

        // HACK:下記問題が発生する為アイテムアニメーションを無効化
        //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
        //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
        //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
        //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
        //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerView.setItemAnimator(null);
        recyclerView.addOnScrollListener(new ListAdditonalLoadingOnScrollListener());
        recyclerView.addOnScrollListener(new SectionBarTranslationOnScrollListener());
        recyclerView.addOnLayoutChangeListener(new SectionBarInitializationOnLayoutChangeListener());
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
                DiaryListSimpleCallback diaryListSimpleCallBack =
                        new DiaryListSimpleCallback(recyclerView, holder.binding.recyclerDayList);
                diaryListSimpleCallBack.build();
                simpleCallbackList.add(diaryListSimpleCallBack);
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
            String diaryDate = "  " + diaryYearMonth.getYear() + context.getString(R.string.row_diary_year_month_list_section_bar_year)
                    + diaryYearMonth.getMonthValue() + context.getString(R.string.row_diary_year_month_list_section_bar_month);
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
                DiaryDayListAdapter diaryDayListAdapter =
                        new DiaryDayListAdapter(
                                context,
                                _holder.binding.recyclerDayList,
                                new DiaryDayListAdapter.OnClickItemListener() {
                                    @Override
                                    public void onClick(LocalDate date) {
                                        onClickChildItemListener.onClick(date);
                                    }
                                },
                                new DiaryDayListAdapter.OnClickDeleteButtonListener() {
                                    @Override
                                    public void onClick(LocalDate date) {
                                        if (onClickChildItemBackgroundButtonListener == null) {
                                            return;
                                        }
                                        onClickChildItemBackgroundButtonListener.onClick(date);
                                    }
                                });
                diaryDayListAdapter.build();
                diaryDayListAdapter.submitList(diaryDayList);

            } else if (item instanceof WordSearchResultYearMonthListItem) {
                WordSearchResultYearMonthListItem _item = (WordSearchResultYearMonthListItem) item;
                List<WordSearchResultDayListItem> wordSearchResultDayList = _item.getWordSearchResultDayList();
                WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                        new WordSearchResultDayListAdapter(
                                context,
                                _holder.binding.recyclerDayList,
                                new WordSearchResultDayListAdapter.OnClickItemListener() {
                                    @Override
                                    public void onClick(LocalDate date) {
                                        onClickChildItemListener.onClick(date);
                                    }
                                });
                wordSearchResultDayListAdapter.build();
                wordSearchResultDayListAdapter.submitList(wordSearchResultDayList);
            }
        }
    }

    @Override
    public int getItemViewType(int position ) {
        DiaryYearMonthListItemBase item = getItem(position);
        return item.getViewType();
    }

    public void closeSwipedItemOtherDayList(@Nullable DiaryListSimpleCallback simpleCallback) {
        if (simpleCallback == null) {
            for (DiaryListSimpleCallback _simpleCallback: simpleCallbackList) {
                _simpleCallback.closeSwipedItem();
            }
        } else {
            for (int i = 0; i < simpleCallbackList.size(); i++) {
                if (simpleCallbackList.get(i) != simpleCallback) {
                    simpleCallbackList.get(i).closeSwipedItem();
                }
            }
        }
    }

    @FunctionalInterface
    public interface OnClickChildItemListener {
        void onClick(LocalDate date);
    }

    @FunctionalInterface
    public interface OnClickChildItemBackgroundButtonListener {
        void onClick(LocalDate date);
    }

    public static class DiaryYearMonthListViewHolder extends RecyclerView.ViewHolder {
        public RowDiaryYearMonthListBinding binding;

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
                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        return false;
                    }
                    if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                        return false;
                    }
                    if (!oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                        return false;
                    }
                    if (!oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @FunctionalInterface
    public interface OnScrollEndItemLoadingListener {
        void Load();
    }

    @FunctionalInterface
    public interface OnScrollLoadingConfirmationListener {
        boolean isLoading();
    }

    private class ListAdditonalLoadingOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LinearLayoutManager layoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null) {
                // TODO:assert
                return;
            }
            int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            if (totalItemCount <= 0) {
                // TODO:assert
                return;
            }
            int lastItemPosition = totalItemCount - 1;
            RecyclerView.Adapter<?> recyclerViewAdapter = recyclerView.getAdapter();
            if (recyclerViewAdapter == null) {
                return;
            }
            int lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition);
            // MEMO:下記条件"dy > 0"は検索結果リストが更新されたときに
            //      "RecyclerView.OnScrollListener#onScrolled"が起動するための対策。
            if (!onScrollLoadingConfirmationListener.isLoading()
                    && (firstVisibleItem + visibleItemCount) >= totalItemCount
                    && dy > 0
                    && lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                onScrollEndItemLoadingListener.Load();
            }
        }
    }

    private class SectionBarTranslationOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            updateFirstVisibleSectionBarPosition();
        }
    }

    private class SectionBarInitializationOnLayoutChangeListener implements View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(
                View v, int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom) {
            updateFirstVisibleSectionBarPosition();
        }
    }

    private void updateFirstVisibleSectionBarPosition() {
        RecyclerView.LayoutManager _layoutManager = recyclerView.getLayoutManager();
        LinearLayoutManager layoutManager;
        if (_layoutManager instanceof LinearLayoutManager) {
            layoutManager = (LinearLayoutManager) _layoutManager;
        } else {
            return;
        }
        int firstVisibleItemPosition =
                layoutManager.findFirstVisibleItemPosition();

        RecyclerView.ViewHolder firstVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        RecyclerView.ViewHolder secondVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1);

        if (firstVisibleViewHolder instanceof DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) {
            DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder _firstVisibleViewHolder =
                    (DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) firstVisibleViewHolder;
            View firstVisibleItemView =
                    layoutManager.getChildAt(0);
            View secondVisibleItemView =
                    layoutManager.getChildAt(1);
            if (firstVisibleItemView != null) {
                float firstVisibleItemViewPositionY = firstVisibleItemView.getY();
                if (secondVisibleItemView != null) {
                    int sectionBarHeight = _firstVisibleViewHolder.binding.textSectionBar.getHeight();
                    float secondVisibleItemViewPositionY = secondVisibleItemView.getY();
                    int border = sectionBarHeight + DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                    if (secondVisibleItemViewPositionY >= border) {
                        _firstVisibleViewHolder.binding.textSectionBar.setY(-(firstVisibleItemViewPositionY));
                    } else {
                        if (secondVisibleItemViewPositionY < DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL) {
                            _firstVisibleViewHolder.binding.textSectionBar.setY(0);
                        } else if (_firstVisibleViewHolder.binding.textSectionBar.getY() == 0) {
                            _firstVisibleViewHolder.binding.textSectionBar.setY(
                                    -(firstVisibleItemViewPositionY) - sectionBarHeight
                            );
                        }
                    }
                } else {
                    _firstVisibleViewHolder.binding.textSectionBar.setY(-(firstVisibleItemViewPositionY));
                }
            }
        }
        if (secondVisibleViewHolder instanceof DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) {
            DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder _secondVisibleViewHolder =
                    (DiaryYearMonthListAdapter.DiaryYearMonthListViewHolder) secondVisibleViewHolder;
            _secondVisibleViewHolder.binding.textSectionBar.setY(0); // ズレ防止
        }
    }

    public void scrollToFirstPosition() {
        Log.d("ボトムナビゲーションタップ確認", "scrollToFirstPosition()呼び出し");
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        LinearLayoutManager linearLayoutManager = null;
        if (layoutManager instanceof LinearLayoutManager) {
            linearLayoutManager = (LinearLayoutManager) layoutManager;
        }
        if (linearLayoutManager != null) {
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            Log.d("スクロール動作確認", "firstVisibleItemPosition：" + firstVisibleItemPosition);

            // HACK:日記リスト(年月)のアイテム数が多い場合、
            //      ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition(0)を起動すると先頭にたどり着くのに時間がかかる。
            //      その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
            //      エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
            int jumpPosition = 2;
            if (firstVisibleItemPosition >= jumpPosition) {
                Log.d("スクロール動作確認", "scrollToPosition()呼出");
                recyclerView.scrollToPosition(jumpPosition);
            }
        }

        Log.d("スクロール動作確認", "smoothScrollToPosition()呼出");
        recyclerView.smoothScrollToPosition(0);
    }
}
