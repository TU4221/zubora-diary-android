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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

// DiaryFragment、WordSearchFragmentの親RecyclerViewのListAdapter。
// 親RecyclerViewを同じ構成にする為、一つのクラスで両方の子RecyclerViewに対応できるように作成。
public abstract class DiaryYearMonthListBaseAdapter extends ListAdapter<DiaryYearMonthListBaseItem, RecyclerView.ViewHolder> {

    protected final Context context;
    protected final RecyclerView recyclerView;
    protected final ThemeColor themeColor;
    protected OnClickChildItemListener onClickChildItemListener;
    protected boolean isLoadingListOnScrolled;

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

    protected DiaryYearMonthListBaseAdapter(
            Context context,
            RecyclerView recyclerView,
            ThemeColor themeColor,
            DiffUtilItemCallback diffUtilItemCallback) {
        super(diffUtilItemCallback);

        Objects.requireNonNull(context);
        Objects.requireNonNull(recyclerView);
        Objects.requireNonNull(themeColor);

        this.context = context;
        this.recyclerView = recyclerView;
        this.themeColor = themeColor;
    }

    public void build() {
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

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
            DiaryYearMonthListBaseItem item = getItem(position);
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

            createDiaryDayList(_holder, item);
        }
    }

    public abstract void createDiaryDayList(
            DiaryYearMonthListViewHolder holder,
            DiaryYearMonthListBaseItem item
    );

    @FunctionalInterface
    public interface OnClickChildItemListener {
        void onClick(LocalDate date);
    }

    public void setOnClickChildItemListener(@Nullable OnClickChildItemListener onClickChildItemListener) {
        this.onClickChildItemListener = onClickChildItemListener;
    }

    @Override
    public int getItemViewType(int position ) {
        DiaryYearMonthListBaseItem item = getItem(position);
        return item.getViewType().getViewTypeNumber();
    }

    public static final class DiaryYearMonthListViewHolder extends RecyclerView.ViewHolder {
        public RowDiaryYearMonthListBinding binding;

        public DiaryYearMonthListViewHolder(RowDiaryYearMonthListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static final class NoDiaryMessageViewHolder extends RecyclerView.ViewHolder {
        public NoDiaryMessageViewHolder(RowNoDiaryMessageBinding binding) {
            super(binding.getRoot());
        }
    }

    public static final class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBarViewHolder(RowProgressBarBinding binding) {
            super(binding.getRoot());
        }
    }

    protected static abstract class DiffUtilItemCallback
            extends DiffUtil.ItemCallback<DiaryYearMonthListBaseItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryYearMonthListBaseItem oldItem, @NonNull DiaryYearMonthListBaseItem newItem) {
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
