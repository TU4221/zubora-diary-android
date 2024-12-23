package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
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
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryYearMonthListBinding;
import com.websarva.wings.android.zuboradiary.databinding.RowNoDiaryMessageBinding;
import com.websarva.wings.android.zuboradiary.databinding.RowProgressBarBinding;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorInflaterCreator;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryListSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryYearMonthListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultYearMonthListItem;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
public abstract class DiaryYearMonthListAdapter extends ListAdapter<DiaryYearMonthListItemBase, RecyclerView.ViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ThemeColor themeColor;
    private OnClickChildItemListener onClickChildItemListener;
    private OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener;
    private final boolean canSwipeItem;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    public static final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private final List<DiaryListSimpleCallback> simpleCallbackList = new ArrayList<>();
    private boolean isLoadingListOnScrolled;

    public enum ViewType {
        DIARY(0),
        PROGRESS_INDICATOR(1),
        NO_DIARY_MESSAGE(2);

        final int viewTypeNumber;
        ViewType(int viewTypeNumber) {
            this.viewTypeNumber = viewTypeNumber;
        }

        public int getViewTypeNumber() {
            return viewTypeNumber;
        }
    }

    public DiaryYearMonthListAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            boolean canSwipeItem) {
        super(new DiaryYearMonthListDiffUtilItemCallback());

        Objects.requireNonNull(context);
        Objects.requireNonNull(recyclerView);
        Objects.requireNonNull(themeColor);

        this.context = context;
        this.recyclerView = recyclerView;
        this.themeColor = themeColor;
        this.canSwipeItem = canSwipeItem;
    }

    public void build() {
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return;

                // スクロール時スワイプ閉
                if (canSwipeItem) closeSwipedItemOtherDayList(null);
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Objects.requireNonNull(parent);

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(context, inflater, themeColor);
        LayoutInflater themeColorInflater = creator.create();

        if (viewType == ViewType.DIARY.getViewTypeNumber()) {
            RowDiaryYearMonthListBinding binding =
                    RowDiaryYearMonthListBinding
                            .inflate(themeColorInflater, parent, false);
            DiaryYearMonthListViewHolder holder = new DiaryYearMonthListViewHolder(binding);

            // ホルダーアイテムアニメーション設定(build()メソッド内にて理由記載)
            // MEMO:子RecyclerViewのアニメーションを共通にする為、親Adapterクラス内で実装。
            holder.binding.recyclerDayList.setItemAnimator(null);

            // MEMO:子RecyclerViewに実装したSimpleCallbackクラスを親RecyclerViewで管理する為、親Adapterクラス内で実装。
            if (canSwipeItem) {
                DiaryListSimpleCallback diaryListSimpleCallback =
                        new DiaryListSimpleCallback(recyclerView, holder.binding.recyclerDayList);
                diaryListSimpleCallback.build();
                simpleCallbackList.add(diaryListSimpleCallback);
            }

            return holder;
        } else if (viewType == ViewType.PROGRESS_INDICATOR.getViewTypeNumber()) {
            RowProgressBarBinding binding =
                    RowProgressBarBinding.inflate(themeColorInflater, parent, false);
            return new ProgressBarViewHolder(binding);
        } else {
            RowNoDiaryMessageBinding binding =
                    RowNoDiaryMessageBinding.inflate(themeColorInflater, parent, false);
            return new NoDiaryMessageViewHolder(binding);
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
            _holder.binding.textSection.setText(diaryDate);
            // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
            _holder.binding.textSection.setY(0);

            // 日記リスト(日)設定
            // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、
            //      日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
            _holder.binding.recyclerDayList.setLayoutManager(new LinearLayoutManager(context));

            if (item instanceof DiaryYearMonthListItem) {
                DiaryYearMonthListItem _item = (DiaryYearMonthListItem) item;
                DiaryDayListAdapter diaryDayListAdapter = createDiaryDayListAdapter(_holder);
                List<DiaryDayListItem> diaryDayList = _item.getDiaryDayList().getDiaryDayListItemList();
                diaryDayListAdapter.submitList(diaryDayList);

            } else if (item instanceof WordSearchResultYearMonthListItem) {
                WordSearchResultYearMonthListItem _item = (WordSearchResultYearMonthListItem) item;
                WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                                                    createWordSearchResultDayListAdapter(_holder);
                List<WordSearchResultDayListItem> wordSearchResultDayList =
                        _item.getWordSearchResultDayList().getWordSearchResultDayListItemList();
                wordSearchResultDayListAdapter.submitList(wordSearchResultDayList);
            }
        }
    }

    @FunctionalInterface
    public interface OnClickChildItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemListener(@Nullable OnClickChildItemListener onClickChildItemListener) {
        this.onClickChildItemListener = onClickChildItemListener;
    }

    @FunctionalInterface
    public interface OnClickChildItemBackgroundButtonListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemBackgroundButtonListener(
            @Nullable OnClickChildItemBackgroundButtonListener onClickChildItemBackgroundButtonListener) {
        this.onClickChildItemBackgroundButtonListener = onClickChildItemBackgroundButtonListener;
    }

    @NonNull
    private DiaryDayListAdapter createDiaryDayListAdapter(DiaryYearMonthListViewHolder _holder) {
        Objects.requireNonNull(_holder);

        DiaryDayListAdapter diaryDayListAdapter =
                new DiaryDayListAdapter(context, _holder.binding.recyclerDayList, themeColor);
        diaryDayListAdapter.build();
        diaryDayListAdapter.setOnClickItemListener(new DiaryDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);
                if (onClickChildItemListener == null) return;

                onClickChildItemListener.onClick(date);
            }
        });
        diaryDayListAdapter.setOnClickDeleteButtonListener(new DiaryDayListAdapter.OnClickDeleteButtonListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);
                if (onClickChildItemBackgroundButtonListener == null) return;

                onClickChildItemBackgroundButtonListener.onClick(date);
            }
        });
        return diaryDayListAdapter;
    }

    @NonNull
    private WordSearchResultDayListAdapter createWordSearchResultDayListAdapter(DiaryYearMonthListViewHolder _holder) {
        Objects.requireNonNull(_holder);

        WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                new WordSearchResultDayListAdapter(context, _holder.binding.recyclerDayList, themeColor);
        wordSearchResultDayListAdapter.build();
        wordSearchResultDayListAdapter.setOnClickItemListener(new WordSearchResultDayListAdapter.OnClickItemListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);
                if (onClickChildItemListener == null) return;

                onClickChildItemListener.onClick(date);
            }
        });
        return wordSearchResultDayListAdapter;
    }

    @Override
    public int getItemViewType(int position ) {
        DiaryYearMonthListItemBase item = getItem(position);
        return item.getViewType().getViewTypeNumber();
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
        public NoDiaryMessageViewHolder(RowNoDiaryMessageBinding binding) {
            super(binding.getRoot());
        }
    }

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBarViewHolder(RowProgressBarBinding binding) {
            super(binding.getRoot());
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
            if (!oldItem.getViewType().equals(newItem.getViewType())) {
                Log.d("DiaryYearMonthList", "ViewType不一致");
                return false;
            }
            // HACK:RecyclerViewの初回アイテム表示時にスクロール初期位置がズレる事がある。
            //      原因はプログレスバーの存在。最初にアイテムを表示する時、読込中の意味を込めてプログレスバーのみを表示させている。
            //      スクロール読込機能の仕様により、読込データをRecyclerViewに表示する際、アイテムリスト末尾にプログレスバーを追加している。
            //      これにより、初回読込中プログレスバーとアイテムリスト末尾のプログレスバーが同一アイテムと認識するため、
            //      ListAdapterクラスの仕様により表示されていたプログレスバーが更新後も表示されるようにスクロール位置がズレた。
            //      プログレスバー同士が同一アイテムと認識されないようにするために、下記条件を追加して対策。
            if (oldItem.getViewType().equals(ViewType.PROGRESS_INDICATOR)) return false;

            // 年月
            if (!oldItem.getYearMonth().equals(newItem.getYearMonth())) {
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

                int _oldChildListSize = _oldItem.getDiaryDayList().getDiaryDayListItemList().size();
                int _newChildListSize = _newItem.getDiaryDayList().getDiaryDayListItemList().size();
                if (_oldChildListSize != _newChildListSize) {
                    Log.d("DiaryYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < _oldChildListSize; i++) {
                    DiaryDayListItem oldChildListItem = _oldItem.getDiaryDayList().getDiaryDayListItemList().get(i);
                    DiaryDayListItem newChildListItem = _newItem.getDiaryDayList().getDiaryDayListItemList().get(i);
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
                Log.d("WordSearchYearMonthList", "WordSearchResultYearMonthListItem");
                WordSearchResultYearMonthListItem _oldItem = (WordSearchResultYearMonthListItem) oldItem;
                WordSearchResultYearMonthListItem _newItem = (WordSearchResultYearMonthListItem) newItem;
                int oldChildListSize =
                        _oldItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().size();
                int newChildListSize =
                        _newItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().size();
                if (oldChildListSize != newChildListSize) {
                    Log.d("WordSearchYearMonthList", "ChildList_Size不一致");
                    return false;
                }

                for (int i = 0; i < oldChildListSize; i++) {
                    WordSearchResultDayListItem oldChildListItem =
                            _oldItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().get(i);
                    WordSearchResultDayListItem newChildListItem =
                            _newItem.getWordSearchResultDayList().getWordSearchResultDayListItemList().get(i);
                    Log.d("WordSearchYearMonthList", "oldChildListItem_Date:" + oldChildListItem.getDate());
                    Log.d("WordSearchYearMonthList", "newChildListItem_Date:" + newChildListItem.getDate());

                    if (!oldChildListItem.getDate().equals(newChildListItem.getDate())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_Date不一致");
                        return false;
                    }
                    if (!oldChildListItem.getTitle().equals(newChildListItem.getTitle())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_Title不一致");
                        return false;
                    }
                    if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemNumber不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemTitle不一致");
                        return false;
                    }
                    if (!oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                        Log.d("WordSearchYearMonthList", "ChildListItem_ItemComment不一致");
                        return false;
                    }
                }
            }
            Log.d("WordSearchYearMonthList", "一致");
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
            if (isLoadingListOnScrolled) return;
            if (!canLoadList()) return;
            if (dy <= 0) return;

            LinearLayoutManager layoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();
            Objects.requireNonNull(layoutManager);

            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            int totalItemCount = layoutManager.getItemCount();
            if (totalItemCount <= 0) return;

            RecyclerView.Adapter<?> recyclerViewAdapter = recyclerView.getAdapter();
            Objects.requireNonNull(recyclerViewAdapter);

            int lastItemPosition = totalItemCount - 1;
            if (lastVisibleItemPosition != lastItemPosition) return;

            int lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition);
            if (lastItemViewType == ViewType.PROGRESS_INDICATOR.getViewTypeNumber()) {
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
        if (getItemCount() == 0) return;
        if (getItemViewType(0) != ViewType.PROGRESS_INDICATOR.getViewTypeNumber()) {
            isLoadingListOnScrolled = false;
        }
    }

    private class SectionBarTranslationOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            updateVisibleSectionBarPosition();
        }
    }

    private class SectionBarInitializationOnLayoutChangeListener implements View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(
                View v, int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom) {
            updateVisibleSectionBarPosition();
        }
    }

    private void updateVisibleSectionBarPosition() {
        RecyclerView.LayoutManager _layoutManager = recyclerView.getLayoutManager();
        Objects.requireNonNull(_layoutManager);
        LinearLayoutManager layoutManager = (LinearLayoutManager) _layoutManager;

        updateFirstVisibleSectionBarPosition(layoutManager);
        updateSecondVisibleSectionBarPosition(layoutManager);
    }

    private void updateFirstVisibleSectionBarPosition(LinearLayoutManager layoutManager) {
        Objects.requireNonNull(layoutManager);

        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder firstVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (firstVisibleViewHolder == null) return; // MEMO:RecyclerViewが空の時nullとなる。
        if (!(firstVisibleViewHolder instanceof DiaryYearMonthListViewHolder)) return;
        DiaryYearMonthListViewHolder _firstVisibleViewHolder =
                (DiaryYearMonthListViewHolder) firstVisibleViewHolder;

        View firstVisibleItemView = layoutManager.getChildAt(0);
        Objects.requireNonNull(firstVisibleItemView);
        View secondVisibleItemView = layoutManager.getChildAt(1);

        float firstVisibleItemViewPositionY = firstVisibleItemView.getY();
        if (secondVisibleItemView == null) {
            _firstVisibleViewHolder.binding.textSection.setY(-(firstVisibleItemViewPositionY));
            return;
        }

        int sectionHeight = _firstVisibleViewHolder.binding.textSection.getHeight();
        float secondVisibleItemViewPositionY = secondVisibleItemView.getY();
        int betweenSectionsMargin = _firstVisibleViewHolder.binding.recyclerDayList.getPaddingBottom();
        int border = sectionHeight + betweenSectionsMargin;
        if (secondVisibleItemViewPositionY >= border) {
            _firstVisibleViewHolder.binding.textSection.setY(-(firstVisibleItemViewPositionY));
        } else {
            if (secondVisibleItemViewPositionY < betweenSectionsMargin) {
                _firstVisibleViewHolder.binding.textSection.setY(0);
            } else if (_firstVisibleViewHolder.binding.textSection.getY() == 0) {
                _firstVisibleViewHolder.binding.textSection.setY(
                        -(firstVisibleItemViewPositionY) - sectionHeight
                );
            }
        }
    }

    private void updateSecondVisibleSectionBarPosition(LinearLayoutManager layoutManager) {
        Objects.requireNonNull(layoutManager);

        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder secondVisibleViewHolder =
                recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition + 1);
        if (secondVisibleViewHolder == null) return;
        if (!(secondVisibleViewHolder instanceof DiaryYearMonthListViewHolder)) return;
        DiaryYearMonthListViewHolder _secondVisibleViewHolder =
                (DiaryYearMonthListViewHolder) secondVisibleViewHolder;

        _secondVisibleViewHolder.binding.textSection.setY(0); // ズレ防止
    }

    public void scrollToFirstPosition() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        Objects.requireNonNull(layoutManager);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

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

        // MEMO:RecyclerViewの先頭アイテム(年月)の上部が表示されている状態でRecyclerView#.smoothScrollToPosition(0)を呼び出すと、
        //      先頭アイテムの底部が表示されるようにスクロールしてしまう。対策として、下記条件追加。
        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        if (canScrollUp) recyclerView.smoothScrollToPosition(0);
    }
}
