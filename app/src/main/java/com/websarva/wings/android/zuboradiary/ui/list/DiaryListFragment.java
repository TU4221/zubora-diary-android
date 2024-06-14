package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchViewModel;

public class DiaryListFragment extends Fragment {

    // View関係
    private FragmentDiaryListBinding binding;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private DiaryListSetting<DiaryYearMonthListViewHolder> diaryListSetting;
    private boolean isLoading = false;
    private DiaryYearMonthListAdapter diaryYearMonthListAdapter;

    // Navigation関係
    private NavController navController;

    // ViewModel
    private ListViewModel listViewModel;
    private DiaryViewModel diaryViewModel;
    private WordSearchViewModel wordSearchViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreate()処理");
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.listViewModel = provider.get(ListViewModel.class);
        this.diaryViewModel = provider.get(DiaryViewModel.class);
        this.wordSearchViewModel = provider.get(WordSearchViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

        // 日記リスト設定クラスインスタンス化
        this.diaryListSetting = new DiaryListSetting<>();
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreateView()処理");
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        this.binding = FragmentDiaryListBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(this);
        this.binding.setListViewModel(this.listViewModel);

        // フィールド初期化
        // MEMO:クラスフィールド上でインスタンス化すると、ボトムナビゲーションのフラグメント切り替え時に例外発生。
        //      インスタンス化に引数がある為、匿名クラスは使用不可。
        this.diaryListYearMonthLinearLayoutManager = new CustomLinearLayoutManager(getContext());

        // 画面遷移時のアニメーション設定
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // TO - FROM の FROM として現れるアニメーション
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setReenterTransition(new MaterialFadeThrough());
            mainActivity.resetTabWasSelected();
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }
        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        return this.binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ダイアログフラグメントからの結果受取設定
        NavBackStackEntry navBackStackEntry =
                this.navController.getBackStackEntry(R.id.navigation_diary_list_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    // 日付入力ダイアログフラグメントから結果受取
                    boolean containsDatePickerDialogFragmentResults =
                            savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_YEAR)
                                    && savedStateHandle
                                    .contains(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                    if (containsDatePickerDialogFragmentResults) {
                        Integer selectedYear =
                                savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                        Integer selectedMonth =
                                savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                        DiaryListFragment.this.listViewModel.updateSortConditionDate(
                                selectedYear,
                                selectedMonth
                        );
                        DiaryListFragment.this.diaryListScrollToFirstPosition();
                        DiaryListFragment.this.listViewModel.loadList(ListViewModel.LoadType.NEW);
                        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                    }

                    // 日記削除ダイアログフラグメントから結果受取
                    boolean ConfirmDeleteDialogFragmentFragmentResults =
                            savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
                    if (ConfirmDeleteDialogFragmentFragmentResults) {
                        String deleteDiaryDate =
                                savedStateHandle.get(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
                        DiaryListFragment.this.listViewModel.deleteDiary(deleteDiaryDate);
                        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
                    }
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR);
                    savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_MONTH);
                    savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });


        // ツールバー設定
        this.binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // リスト先頭年月切り替えダイアログ起動
                        String newestDate = DiaryListFragment.this.listViewModel.loadNewestDiary().getDate();
                        String oldestDate = DiaryListFragment.this.listViewModel.loadOldestDiary().getDate();
                        int newestYear = DateConverter.toLocalDate(newestDate).getYear();
                        int oldestYear = DateConverter.toLocalDate(oldestDate).getYear();
                        NavDirections action =
                                DiaryListFragmentDirections
                                        .actionDiaryListFragmentToDatePickerDialog(newestYear, oldestYear);
                        DiaryListFragment.this.navController.navigate(action);
                    }
                });
        this.binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // ワード検索フラグメント起動
                        if (item.getItemId() == R.id.listToolbarOptionWordSearch) {
                            DiaryListFragment.this.wordSearchViewModel.initialize();

                            NavDirections action =
                                    DiaryListFragmentDirections
                                            .actionNavigationDiaryListFragmentToWordSearchFragment();
                            DiaryListFragment.this.navController.navigate(action);
                            return true;

                        }
                        return false;
                    }
                });


        // 新規作成FAB設定
        this.binding.fabEditDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 日記編集(新規作成)フラグメント起動。
                LocalDate localDate = LocalDate.now();
                NavDirections action =
                        DiaryListFragmentDirections
                                .actionNavigationDiaryListFragmentToEditDiaryFragment(
                                        true,
                                        false,
                                        localDate.getYear(),
                                        localDate.getMonthValue(),
                                        localDate.getDayOfMonth()
                                );
                DiaryListFragment.this.navController.navigate(action);
            }
        });

        // 日記リスト(年月)設定
        RecyclerView recyclerDiaryYearMonthList = this.binding.recyclerDiaryYearMonthList;
        recyclerDiaryYearMonthList
                .setLayoutManager(this.diaryListYearMonthLinearLayoutManager);
        this.diaryYearMonthListAdapter =
                new DiaryYearMonthListAdapter(new DiaryYearMonthListDiffUtilItemCallback());
        recyclerDiaryYearMonthList.setAdapter(diaryYearMonthListAdapter);
        // TODO:下記別の方法を検討(問題1アイテム変更時もやがかかる。問題2最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerDiaryYearMonthList.setItemAnimator(null);
        recyclerDiaryYearMonthList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                DiaryListFragment.this.listViewModel.setScrollPointY(recyclerView.getScrollY());


                // 日記リスト先頭アイテムセクションバー位置更新
                DiaryListFragment.this.diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerView,
                                DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                        );


                // 日記リスト追加読込
                int firstVisibleItem =
                        DiaryListFragment.this.diaryListYearMonthLinearLayoutManager
                                .findFirstVisibleItemPosition();
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount =
                        DiaryListFragment.this.diaryListYearMonthLinearLayoutManager.getItemCount();
                // MEMO:下記条件"dy > 0"は検索結果リストが更新されたときに
                //      "RecyclerView.OnScrollListener#onScrolled"が起動するための対策。
                if (!isLoading
                        && (firstVisibleItem + visibleItemCount) >= totalItemCount
                        && dy > 0) {
                    isLoading = true;
                    DiaryListFragment.this.listViewModel.loadList(ListViewModel.LoadType.ADD);
                }

            }
        });
        recyclerDiaryYearMonthList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 日記追加読込後RecyclerView更新時、セクションバーが元の位置に戻るので再度位置更新
                DiaryListFragment.this.diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerDiaryYearMonthList,
                                DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                        );
            }
        });


        // 日記リスト読込
        if (this.listViewModel.getLiveDataDiaryList().getValue().isEmpty()) {
            if (this.listViewModel.countDiaries() >= 1) {
                this.listViewModel.loadList(ListViewModel.LoadType.NEW);
            }
        } else {
            this.listViewModel.loadList(ListViewModel.LoadType.UPDATE);
        }

        this.listViewModel.getLiveDataDiaryList().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryYearMonthListItem>>() {
                    @Override
                    public void onChanged(List<DiaryYearMonthListItem> diaryListItems) {
                        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        DiaryListFragment.this.binding
                                                .recyclerDiaryYearMonthList.getAdapter();
                        diaryYearMonthListAdapter.submitList(diaryListItems);
                        if (diaryListItems.isEmpty()) {
                            isLoading = false;
                        } else {
                            int lastItemPosition = diaryListItems.size() - 1;
                            int lastItemViewType = diaryListItems.get(lastItemPosition).getViewType();
                            if (lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                                isLoading = false;
                            }
                        }
                    }
                }
        );
    }


    // TODO:public -> なし or private に変更しても良いか検証する
    //日記リスト(日)リサイクルビューホルダークラス
    public class DiaryDayListViewHolder extends RecyclerView.ViewHolder {
        public TextView textDayOfWeek;
        public TextView textDayOfMonth;
        public TextView textTitle;
        public ImageView imagePicture;

        public String date;
        public DiaryDayListViewHolder(View itemView) {
            super(itemView);
            this.textDayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            this.textDayOfMonth = itemView.findViewById(R.id.text_day_of_month);
            this.textTitle = itemView.findViewById(R.id.text_row_diary_list_day_title);
            this.imagePicture = itemView.findViewById(R.id.image_row_diary_list_day_picture);
        }
    }

    //日記リスト(日)リサイクルビューアダプタクラス
    public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListViewHolder> {

        public DiaryDayListAdapter(@NonNull DiffUtil.ItemCallback<DiaryDayListItem> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public DiaryDayListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_day_list, parent, false);
            return new DiaryDayListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DiaryDayListViewHolder holder, int position) {
            DiaryDayListItem item = getItem(position);
            int year = item.getYear();
            int month = item.getMonth();
            int dayOfMonth = item.getDayOfMonth();
            String dayOfWeek = item.getDayOfWeek();
            String title = item.getTitle();
            holder.date = DateConverter.toStringLocalDate(year, month, dayOfMonth); // ホルダー毎に日記の日付情報一式付与
            holder.textDayOfWeek.setText(dayOfWeek);
            holder.textDayOfMonth.setText(String.valueOf(dayOfMonth));
            holder.textTitle.setText(title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 日記表示フラグメント起動。
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionNavigationDiaryListFragmentToShowDiaryFragment(
                                            year, month, dayOfMonth);
                    DiaryListFragment.this.navController.navigate(action);
                }
            });
        }
    }

    public class DiaryDayListDiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    }


    //日記リスト(年月)リサイクルビューホルダークラス
    public class DiaryYearMonthListViewHolder extends DiaryYearMonthListBaseViewHolder {
        public RecyclerView recyclerDayList;

        public DiaryYearMonthListViewHolder(View itemView) {
            super(itemView);
            this.textSectionBar = itemView.findViewById(R.id.text_section_bar);
            this.recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }

    public class DiaryYearMonthListAdapter
            extends ListAdapter<DiaryYearMonthListItem, RecyclerView.ViewHolder> {
        private List<Map<String, Object>> diaryYearMonthList = new ArrayList<>();
        private List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();
        public static final int VIEW_TYPE_DIARY = 0;
        public static final int VIEW_TYPE_PROGRESS_BAR = 1;

        public DiaryYearMonthListAdapter(@NonNull DiffUtil.ItemCallback<DiaryYearMonthListItem> diffCallback){
            super(diffCallback);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("20240530", "onCreateViewHolder()");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_DIARY) {
                View view =
                        inflater.inflate(R.layout.row_diary_year_month_list, parent, false);
                DiaryYearMonthListViewHolder holder = new DiaryYearMonthListViewHolder(view);

                // ホルダー内の日記リスト(日)のアイテム装飾設定
                // MEMO:onBindViewHolder で設定すると、設定内容が重複してアイテムが小さくなる為、
                //      onCreateViewHolder で設定。
                holder.recyclerDayList.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(
                            @NonNull Rect outRect,
                            @NonNull View view,
                            @NonNull RecyclerView parent,
                            @NonNull RecyclerView.State state) {
                        Log.d("リスト装飾確認","getItemOffsets()呼び出し");
                        super.getItemOffsets(outRect, view, parent, state);
                        outRect.top = DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                        outRect.left = DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;
                        outRect.right = DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;

                        // TODO:Fragment切り替え方法をNavigationへの置換後、代替メソッド検討
                        Log.d("リスト装飾確認",
                                Integer.toString(
                                        parent.findContainingViewHolder(view).getAdapterPosition()));
                        if (parent.findContainingViewHolder(view).getAdapterPosition()
                                == (parent.getAdapter().getItemCount() - 1)) {
                            outRect.bottom = DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                        }
                    }
                });

                // ホルダー内の日記リスト(日)のアイテムにスワイプ機能(背面ボタン表示)を設定。
                // MEMO:スワイプでの背面ボタン表示機能はAndroidには存在しないので、
                //      ItemTouchHelper.Callback を継承して作成。
                CustomSimpleCallback simpleCallback = new CustomSimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT,
                        holder.recyclerDayList,
                        getContext(),
                        getResources().getDisplayMetrics().density,
                        getChildFragmentManager(), // TODO:左記不要確認後削除
                        DiaryListFragment.this.navController,
                        DiaryListFragment.this.binding.recyclerDiaryYearMonthList);
                this.simpleCallbacks.add(simpleCallback);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(holder.recyclerDayList);
                return holder;
            } else {
                Log.d("20240612","progressBarViewHolder 追加");
                View view =
                        inflater.inflate(R.layout.row_progress_bar, parent, false);
                return new ProgressBarViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Log.d("20240530", "onBindViewHolder()");

            if (holder instanceof DiaryYearMonthListViewHolder) {
                DiaryYearMonthListViewHolder _holder = (DiaryYearMonthListViewHolder) holder;
                // 対象行の情報を取得
                DiaryYearMonthListItem item = getItem(position);
                int diaryYear = item.getYear();
                int diaryMonth = item.getMonth();
                List<DiaryDayListItem> diaryDayListItemList = item.getDiaryDayListItemList();

                // セクションバー設定
                // 左端に余白を持たせる為、最初にスペースを入力。
                String diaryDate = "  " + diaryYear + getString(R.string.row_list_year)
                        + diaryMonth + getString(R.string.row_list_month);
                _holder.textSectionBar.setText(diaryDate);
                // 日記リストスクロール時に移動させているので、バインディング時に位置リセット
                _holder.textSectionBar.setY(0);

                // 日記リスト(日)設定
                // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、
                //      日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
                _holder.recyclerDayList.setLayoutManager(new LinearLayoutManager(getContext()));
                DiaryDayListAdapter diaryDayListAdapter =
                        new DiaryDayListAdapter(new DiaryDayListDiffUtilItemCallback());
                _holder.recyclerDayList.setAdapter(diaryDayListAdapter);
                diaryDayListAdapter.submitList(diaryDayListItemList);
            }

        }

        @Override
        public int getItemViewType(int position ) {
            DiaryYearMonthListItem item = getItem(position);
            return item.getViewType();
        }

        // 日記リスト(年月)の指定したアイテムを削除。
        public void deleteItem(int position) {
            this.diaryYearMonthList.remove(position);
            notifyItemRemoved(position);
        }

        // 日記リスト(年月)の一つのアイテム内の日記リスト(日)アイテムをスワイプした時、
        // 他の日記リスト(年月)のアイテム内の日記リスト(日)の全アイテムをスワイプ前の状態に戻す。
        public void recoverOtherSwipedItem(CustomSimpleCallback customSimpleCallback) {
            for (int i = 0; i < this.simpleCallbacks.size(); i++) {
                if (customSimpleCallback != this.simpleCallbacks.get(i)) {
                    this.simpleCallbacks.get(i).recoverSwipeItem();
                }
            }
        }
    }

    public class DiaryYearMonthListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<DiaryYearMonthListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryYearMonthListItem oldItem, @NonNull DiaryYearMonthListItem newItem) {
            Log.d("20240613_1", "areItemsTheSame");
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListItem oldItem, @NonNull DiaryYearMonthListItem newItem) {
            Log.d("20240613_1", "areContentsTheSame");
            if (oldItem.getId().equals(newItem.getId())) {
                return false;
            }

            int oldChildListSize = oldItem.getDiaryDayListItemList().size();
            int newChildListSize = newItem.getDiaryDayListItemList().size();
            if (oldChildListSize != newChildListSize) {
                return false;
            }

            int maxSize = Math.max(oldChildListSize, newChildListSize);
            for (int i = 0; i < maxSize; i++) {
                String oldChildListItemId = oldItem.getDiaryDayListItemList().get(i).getId();
                String newChildListItemId = newItem.getDiaryDayListItemList().get(i).getId();
                if (oldChildListItemId != newChildListItemId) {
                    return false;
                }
            }

            return true;
        }
    }

    //日記リスト(年月)(リサイクラービュー)のライナーレイアウトマネージャークラス
    //(コンストラクタに引数があるため、匿名クラス作成不可。メンバクラス作成。)
    private class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        //recyclerView.smoothScrollToPositionの挙動をWEBを参考にオーバーライドで修正(修正出来てるか怪しい？)
        //https://qiita.com/Air_D/items/c253d385f9d443283602
        @Override
        public void smoothScrollToPosition(
                RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                @Override
                protected int getVerticalSnapPreference() {
                    //下記リターン値を固定とした。
                    return SNAP_TO_START;
                }
            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }


    // TODO:Sampleアプリへ移行
    private void createMenu() {
        //アクションバーオプションメニュー更新
        //https://qiita.com/Nabe1216/items/b26b03cbc750ac70a842
        /*MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                this.listmenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);*/
    }


    private class ListMenuProvider implements MenuProvider {

        // アクションバーオプションメニュー設定。
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            menuInflater.inflate(R.menu.diary_list_toolbar_menu, menu);

            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_calendar_month_24);

            /*
            //メモ
            //下記不具合が発生する為、 WordSearchResultYearMonthListAdapter#onBindViewHolder()でアクションバーのタイトルをセットする。
            //初めのフラグメント表示時は下記プログラムは問題ないが、
            //子フラグメントでこのフラグメントを隠して、子フラグメントを閉じるときに下記プログラムを処理させると、
            //ViewHolder が null で返されるため、テキストを取得しようとすると例外が発生する。
            //これは子フラグメントが完全に閉じる前(日記リストが表示(アダプターの計算)されていない状態)に
            //findViewHolderForAdapterPosition()が処理するため null が戻ってくる。

            int pos = diaryListYearMonthLinearLayoutManager.findFirstVisibleItemPosition();
            DiaryYearMonthListViewHolder viewHolder = (DiaryYearMonthListViewHolder) diaryListYearMonthRecyclerView.findViewHolderForAdapterPosition(pos);
            actionBar.setTitle(viewHolder._tvRowDiaryListYearMonth_Year.getText() + "年" + viewHolder._tvRowDiaryListYearMonth_Month.getText() + "月");

             */
        }

        // アクションバーメニュー選択処理設定。
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            // ワード検索フラグメント起動
            if (menuItem.getItemId() == R.id.listToolbarOptionWordSearch) {
                DiaryListFragment.this.wordSearchViewModel.initialize();

                NavDirections action =
                        DiaryListFragmentDirections
                                .actionNavigationDiaryListFragmentToWordSearchFragment();
                DiaryListFragment.this.navController.navigate(action);
                return true;

            // リスト先頭年月切り替えダイアログ起動
            } else if (menuItem.getItemId() == android.R.id.home) {

                String newestDate = DiaryListFragment.this.listViewModel.loadNewestDiary().getDate();
                String oldestDate = DiaryListFragment.this.listViewModel.loadOldestDiary().getDate();
                int newestYear = DateConverter.toLocalDate(newestDate).getYear();
                int oldestYear = DateConverter.toLocalDate(oldestDate).getYear();
                NavDirections action =
                        DiaryListFragmentDirections
                                .actionDiaryListFragmentToDatePickerDialog(newestYear, oldestYear);
                return true;

            } else {
                return false;
            }
        }
    }


    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    public void diaryListScrollToFirstPosition() {
        Log.d("ボトムナビゲーションタップ確認", "scrollToFirstPosition()呼び出し");
        int position;
        position = this.diaryListYearMonthLinearLayoutManager.findLastVisibleItemPosition();
        Log.d("スクロール動作確認", "position：" + position);

        // TODO:保留
        // 日記リスト(年月)のアイテム数が多い場合、
        // ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition()を起動すると先頭にたどり着くのに時間がかかる。
        // その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
        // エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
                /*
                if (position >= 1) {
                    Log.d("スクロール動作確認", "scrollToPosition()呼出");
                    diaryListYearMonthRecyclerView.scrollToPosition(1);
                }
                 */

        Log.d("スクロール動作確認", "smoothScrollToPosition()呼出");
        this.binding.recyclerDiaryYearMonthList.smoothScrollToPosition(0);
    }

}
