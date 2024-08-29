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

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
public abstract class DiaryYearMonthListAdapter extends ListAdapter<DiaryYearMonthListItemBase, RecyclerView.ViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private OnClickChildItemListener onClickChildItemListener;
    private OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener;
    private final boolean canSwipeItem;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private final List<DiaryListSimpleCallback> simpleCallbackList = new ArrayList<>();
    public static final int VIEW_TYPE_DIARY = 0;
    public static final int VIEW_TYPE_PROGRESS_BAR = 1;
    public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;
    private boolean isLoadingListOnScrolled;

    public DiaryYearMonthListAdapter(
            Context context,
            RecyclerView recyclerView,
            boolean canSwipeItem) {
        super(new DiaryYearMonthListDiffUtilItemCallback());
        this.context = context;
        this.recyclerView = recyclerView;
        this.canSwipeItem = canSwipeItem;
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
        recyclerView.addOnScrollListener(new SectionBarTranslationOnScrollListener());
        recyclerView.addOnLayoutChangeListener(new SectionBarInitializationOnLayoutChangeListener());


        recyclerView.addOnScrollListener(new ListAdditonalLoadingOnScrollListener());
        this.registerAdapterDataObserver(new ListLoadingCompleteNotificationAdapterDataObserver());
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

            // ホルダーアイテムアニメーション設定(build()メソッド内にて理由記載)
            // MEMO:子RecyclerViewのアニメーションを共通にする為、親Adapterクラス内で実装。
            holder.binding.recyclerDayList.setItemAnimator(null);

            // ホルダー内の日記リスト(日)のアイテム装飾設定
            // MEMO:onBindViewHolder()で設定すると、設定内容が重複してアイテムが小さくなる為、onCreateViewHolderで設定。
            // MEMO:子RecyclerViewの装飾を共通にする為、親Adapterクラス内で実装。
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

            // MEMO:子RecyclerViewに実装したSimpleCallbackクラスを親RecyclerViewで管理する為、親Adapterクラス内で実装。
            if (canSwipeItem) {
                DiaryListSimpleCallback diaryListSimpleCallback =
                        new DiaryListSimpleCallback(recyclerView, holder.binding.recyclerDayList);
                diaryListSimpleCallback.build();
                simpleCallbackList.add(diaryListSimpleCallback);
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
                DiaryDayListAdapter diaryDayListAdapter = createDiaryDayListAdapter(_holder);
                List<DiaryDayListItem> diaryDayList = _item.getDiaryDayListItemList();
                diaryDayListAdapter.submitList(diaryDayList);

            } else if (item instanceof WordSearchResultYearMonthListItem) {
                WordSearchResultYearMonthListItem _item = (WordSearchResultYearMonthListItem) item;
                WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                                                    createWordSearchResultDayListAdapter(_holder);
                List<WordSearchResultDayListItem> wordSearchResultDayList =
                                                                _item.getWordSearchResultDayList();
                wordSearchResultDayListAdapter.submitList(wordSearchResultDayList);
            }
        }
    }

    @FunctionalInterface
    public interface OnClickChildItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemListener(OnClickChildItemListener onClickChildItemListener) {
        this.onClickChildItemListener = onClickChildItemListener;
    }

    @FunctionalInterface
    public interface OnClickChildItemBackgroundButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemBackgroundButtonListener(
            OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener) {
        this.onClickChildItemBackgroundButtonListener = onClickChildItemBackgroundButtonListener;
    }

    private @NonNull DiaryDayListAdapter createDiaryDayListAdapter(DiaryYearMonthListViewHolder _holder) {
        DiaryDayListAdapter diaryDayListAdapter =
                new DiaryDayListAdapter(context, _holder.binding.recyclerDayList);
        diaryDayListAdapter.build();
        diaryDayListAdapter.setOnClickItemListener(new DiaryDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(LocalDate date) {
                if (onClickChildItemListener == null) {
                    return;
                }
                onClickChildItemListener.onClick(date);
            }
        });
        diaryDayListAdapter.setOnClickDeleteButtonListener(new DiaryDayListAdapter.OnClickDeleteButtonListener() {
            @Override
            public void onClick(LocalDate date) {
                if (onClickChildItemBackgroundButtonListener == null) {
                    return;
                }
                onClickChildItemBackgroundButtonListener.onClick(date);
            }
        });
        return diaryDayListAdapter;
    }

    private @NonNull WordSearchResultDayListAdapter createWordSearchResultDayListAdapter(DiaryYearMonthListViewHolder _holder) {
        WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                new WordSearchResultDayListAdapter(context, _holder.binding.recyclerDayList);
        wordSearchResultDayListAdapter.build();
        wordSearchResultDayListAdapter.setOnClickItemListener(new WordSearchResultDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(LocalDate date) {
                if (onClickChildItemListener == null) {
                    return;
                }
                onClickChildItemListener.onClick(date);
            }
        });
        return wordSearchResultDayListAdapter;
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
            Log.d("DiaryYearMonthList", "DiffUtil.ItemCallback_areItemsTheSame()");
            Log.d("DiaryYearMonthList", "oldItem_YearMonth:" + oldItem.getYearMonth());
            Log.d("DiaryYearMonthList", "newItem_YearMonth:" + newItem.getYearMonth());

            // ViewType
            if (oldItem.getViewType() != newItem.getViewType()) {
                Log.d("DiaryYearMonthList", "ViewType不一致");
                return false;
            }
            // HACK:RecyclerViewの初回アイテム表示時にスクロール初期位置がズレる事がある。
            //      原因はプログレスバーの存在。最初にアイテムを表示する時、読込中の意味を込めてプログレスバーのみを表示させている。
            //      スクロール読込機能の仕様により、読込データをRecyclerViewに表示する際、アイテムリスト末尾にプログレスバーを追加している。
            //      これにより、初回読込中プログレスバーとアイテムリスト末尾のプログレスバーが同一アイテムと認識するため、
            //      ListAdapterクラスの仕様により表示されていたプログレスバーが更新後も表示されるようにスクロール位置がズレた。
            //      プログレスバー同士が同一アイテムと認識されないようにするために、下記条件を追加して対策。
            if (oldItem.getViewType() == VIEW_TYPE_PROGRESS_BAR) {
                return false;
            }

            // 年月
            if (oldItem.getYearMonth() != null && newItem.getYearMonth() != null
                    && !oldItem.getYearMonth().equals(newItem.getYearMonth())) {
                Log.d("DiaryYearMonthList", "YearMonth不一致");
                return false;
            }

            Log.d("DiaryYearMonthList", "一致");
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListItemBase oldItem, @NonNull DiaryYearMonthListItemBase newItem) {
            Log.d("DiaryYearMonthList", "DiffUtil.ItemCallback_areContentsTheSame()");
            Log.d("DiaryYearMonthList", "oldItem_YearMonth:" + oldItem.getYearMonth());
            Log.d("DiaryYearMonthList", "newItem_YearMonth:" + newItem.getYearMonth());
            // 日
            if (oldItem instanceof DiaryYearMonthListItem && newItem instanceof DiaryYearMonthListItem) {
                Log.d("DiaryYearMonthList", "DiaryYearMonthListItem");
                DiaryYearMonthListItem _oldItem = (DiaryYearMonthListItem) oldItem;
                DiaryYearMonthListItem _newItem = (DiaryYearMonthListItem) newItem;

                int _oldChildListSize = _oldItem.getDiaryDayListItemList().size();
                int _newChildListSize = _newItem.getDiaryDayListItemList().size();
                if (_oldChildListSize != _newChildListSize) {
                    Log.d("DiaryYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < _oldChildListSize; i++) {
                    DiaryDayListItem oldChildListItem = _oldItem.getDiaryDayListItemList().get(i);
                    DiaryDayListItem newChildListItem = _newItem.getDiaryDayListItemList().get(i);
                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Date不一致");
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Title不一致");
                        return false;
                    }
                    if (!oldChildListItem.getPicturePath().equals(newChildListItem.getPicturePath())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致");
                        return false;
                    }
                }
            } else if (oldItem instanceof WordSearchResultYearMonthListItem
                    && newItem instanceof WordSearchResultYearMonthListItem) {
                Log.d("DiaryYearMonthList", "WordSearchResultYearMonthListItem");
                WordSearchResultYearMonthListItem _oldItem = (WordSearchResultYearMonthListItem) oldItem;
                WordSearchResultYearMonthListItem _newItem = (WordSearchResultYearMonthListItem) newItem;
                int oldChildListSize = _oldItem.getWordSearchResultDayList().size();
                int newChildListSize = _newItem.getWordSearchResultDayList().size();
                if (oldChildListSize != newChildListSize) {
                    Log.d("DiaryYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < oldChildListSize; i++) {
                    WordSearchResultDayListItem oldChildListItem =
                            _oldItem.getWordSearchResultDayList().get(i);
                    WordSearchResultDayListItem newChildListItem =
                            _newItem.getWordSearchResultDayList().get(i);
                    Log.d("DiaryYearMonthList", "oldChildListItem_Date:" + oldChildListItem.getDate());
                    Log.d("DiaryYearMonthList", "newChildListItem_Date:" + newChildListItem.getDate());

                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Date不一致");
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_Title不一致");
                        return false;
                    }
                    if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                        Log.d("DiaryYearMonthList", "ChildListItem_ItemNumber不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_ItemTitle不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                        Log.d("DiaryYearMonthList", "ChildListItem_ItemComment不一致");
                        return false;
                    }
                }
            }
            Log.d("DiaryYearMonthList", "一致");
            return true;
        }
    }

    /**
     * RecyclerViewを最終端までスクロールした時にリストアイテムを追加読込するコードを記述すること。
     */
    public abstract void loadListOnScrollEnd();

    /**
     * RecyclerViewを最終端までスクロールした時にリストアイテムを追加読込可能か確認するコードを記述すること。
     */
    public abstract boolean canLoadList();

    // MEMO:読込スクロールをスムーズに処理できるように下記項目を考慮してクラス作成
    //      1. リスト最終アイテムまで見え始める所までスクロールした時に、アイテム追加読込中の目印として最終アイテムの下に
    //         プログレスバーを追加する予定だったが、プログレスバーが追加される前にスクロールしきってしまい、
    //         プログレスバーが表示される前にスクロールが止まってしまうことがあった。
    //         これを解消する為、あらかじめにリスト最終アイテムにプログレスバーを追加するようにして、
    //         読込中はプログレスバーまでスクロールできるようにした。
    //      2. スクロール読込開始条件として、データベースの読込処理状態を監視していたが、読込完了からRecyclerViewへ
    //         反映されるまでの間にタイムラグがあるため、その間にスクロール読込開始条件が揃って読込処理が重複してしまう。
    //         これを解消するために独自のフラグを用意した。

    private class ListAdditonalLoadingOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isLoadingListOnScrolled) {
                return;
            }

            if (!canLoadList()) {
                return;
            }

            if (dy <= 0) {
                return;
            }

            LinearLayoutManager layoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null) {
                // TODO:assert
                return;
            }

            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            int totalItemCount = layoutManager.getItemCount();
            if (totalItemCount <= 0) {
                // TODO:assert
                return;
            }
            RecyclerView.Adapter<?> recyclerViewAdapter = recyclerView.getAdapter();
            if (recyclerViewAdapter == null) {
                return;
            }

            int lastItemPosition = totalItemCount - 1;
            if (lastVisibleItemPosition != lastItemPosition) {
                return;
            }

            int lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition);
            if (lastItemViewType == VIEW_TYPE_PROGRESS_BAR) {
                Log.d("OnScrollDiaryList", "DiaryListLoading");
                loadListOnScrollEnd();
                isLoadingListOnScrolled = true;
            }
        }
    }

    private class ListLoadingCompleteNotificationAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            Log.d("OnScrollDiaryList", "RecyclerView_OnChanged()");
            super.onChanged();
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            Log.d("OnScrollDiaryList", "RecyclerView_onItemRangeChanged()");
            super.onItemRangeChanged(positionStart, itemCount);
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            Log.d("OnScrollDiaryList", "RecyclerView_onItemRangeChanged()");
            super.onItemRangeChanged(positionStart, itemCount, payload);
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Log.d("OnScrollDiaryList", "RecyclerView_onItemRangeInserted()");
            super.onItemRangeInserted(positionStart, itemCount);
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            Log.d("OnScrollDiaryList", "RecyclerView_onItemRangeRemoved()");
            super.onItemRangeRemoved(positionStart, itemCount);
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            Log.d("OnScrollDiaryList", "RecyclerView_onItemRangeMoved()");
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            clearIsLoadingListOnScrolled();
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            Log.d("OnScrollDiaryList", "RecyclerView_onStateRestorationPolicyChanged()");
            super.onStateRestorationPolicyChanged();
        }
    }

    private void clearIsLoadingListOnScrolled() {
        if (getItemCount() == 0) {
            return;
        }
        if (getItemViewType(0) != DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR) {
            isLoadingListOnScrolled = false;
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
