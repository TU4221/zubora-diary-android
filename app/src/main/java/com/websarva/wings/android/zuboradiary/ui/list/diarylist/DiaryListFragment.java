package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListListenerSetting;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListSetting;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends Fragment {

    // View関係
    private FragmentDiaryListBinding binding;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;
    private static final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    private static final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private DiaryListSetting diaryListSetting;

    // Navigation関係
    private NavController navController;
    private boolean shouldShowDiaryListLoadingErrorDialog;
    private boolean shouldShowDiaryInformationLoadingErrorDialog;
    private boolean shouldShowDiaryDeleteErrorDialog;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;
    private WordSearchViewModel wordSearchViewModel; //TODO:削除の方向で

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreate()処理");
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        diaryListViewModel = provider.get(DiaryListViewModel.class);
        wordSearchViewModel = provider.get(WordSearchViewModel.class);

        // Navigation設定
        navController = NavHostFragment.findNavController(this);

        // 日記リスト設定クラスインスタンス化
        diaryListSetting = new DiaryListSetting(); // TODO:他の方法がないか検討
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreateView()処理");
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        binding = FragmentDiaryListBinding.inflate(inflater, container, false);

        // データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setListViewModel(diaryListViewModel);

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

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpDialogResultReceiver();
        setUpToolBar();
        setUpFloatActionButton();
        setUpDiaryList();
        setUpErrorObserver();
    }

    // ダイアログフラグメントからの結果受取設定
    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry =
                navController.getBackStackEntry(R.id.navigation_diary_list_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    receiveDatePickerDialogResults(savedStateHandle);
                    receiveConfirmDeleteDialogResults(savedStateHandle);
                    removeDialogResults(savedStateHandle);
                    retryErrorDialogShow();
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
                    removeDialogResults(savedStateHandle);
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    private void removeDialogResults(SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
    }

    // 日付入力ダイアログフラグメントから結果受取
    private void receiveDatePickerDialogResults(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
        if (containsDialogResult) {
            YearMonth selectedYearMonth =
                    savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_YEAR_MONTH);
            if (selectedYearMonth == null) {
                return;
            }
            diaryListViewModel.updateSortConditionDate(selectedYearMonth);
            diaryListScrollToFirstPosition();
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.NEW);
        }
    }

    // 日記削除ダイアログフラグメントから結果受取
    private void receiveConfirmDeleteDialogResults(SavedStateHandle savedStateHandle) {
        boolean containsDialogResult =
                savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
        if (containsDialogResult) {
            LocalDate deleteDiaryDate =
                    savedStateHandle.get(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
            if (deleteDiaryDate == null) {
                return;
            }
            diaryListViewModel.deleteDiary(deleteDiaryDate);
        }
    }

    // ツールバー設定
    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // リスト先頭年月切り替えダイアログ起動
                        Diary newestDiary = diaryListViewModel.loadNewestDiary();
                        Diary oldestDiary = diaryListViewModel.loadOldestDiary();
                        String newestDate;
                        String oldestDate;
                        if (newestDiary == null || oldestDiary == null) {
                            return;
                        } else {
                            newestDate = newestDiary.getDate();
                            oldestDate = oldestDiary.getDate();
                        }
                        Year newestYear = Year.of(LocalDate.parse(newestDate).getYear());
                        Year oldestYear = Year.of(LocalDate.parse(oldestDate).getYear());
                        showDatePickerDialog(newestYear, oldestYear);
                    }
                });

        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // ワード検索フラグメント起動
                        if (item.getItemId() == R.id.listToolbarOptionWordSearch) {
                            showWordSearchFragment();
                            return true;
                        }
                        return false;
                    }
                });
    }

    // 新規作成FAB設定
    private void setUpFloatActionButton() {
        binding.fabEditDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDiary();
            }
        });
    }

    // 日記リスト(年月)設定
    private void setUpDiaryList() {
        RecyclerView recyclerDiaryYearMonthList = binding.recyclerDiaryYearMonthList;
        diaryListYearMonthLinearLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerDiaryYearMonthList.setLayoutManager(diaryListYearMonthLinearLayoutManager);
        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                new DiaryYearMonthListAdapter(requireContext(), this::showShowDiaryFragment, true);
        recyclerDiaryYearMonthList.setAdapter(diaryYearMonthListAdapter);
        // HACK:下記問題が発生する為アイテムアニメーションを無効化
        //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
        //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
        //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
        //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
        //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerDiaryYearMonthList.setItemAnimator(null);
        /*recyclerDiaryYearMonthList.addOnScrollListener(
                new CustomDiaryListOnScrollListener(
                        DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL, diaryListViewModel.getIsLoading())
        );
        recyclerDiaryYearMonthList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 日記追加読込後RecyclerView更新時、セクションバーが元の位置に戻るので再度位置更新
                diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerDiaryYearMonthList,
                                DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                        );
            }
        });*/
        DiaryListListenerSetting diaryListListenerSetting = new DiaryListListenerSetting() {
            @Override
            public boolean isLoadingDiaryList() {
                return diaryListViewModel.getIsLoading();
            }

            @Override
            public void loadDiaryList() {
                diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
            }
        };
        diaryListListenerSetting.setUp(recyclerDiaryYearMonthList, DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL);

        diaryListViewModel.getDiaryListLiveData().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryYearMonthListItem>>() {
                    @Override
                    public void onChanged(List<DiaryYearMonthListItem> diaryListItems) {
                        if (diaryListItems == null) {
                            return;
                        }
                        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        binding.recyclerDiaryYearMonthList.getAdapter();
                        if (diaryYearMonthListAdapter == null) {
                            return;
                        }
                        List<DiaryYearMonthListItemBase> convertedList = new ArrayList<>(diaryListItems);
                        Log.d("20240812", "diaryListItems_size:" + diaryListItems.size());
                        Log.d("20240812", "convertedList_size:" + convertedList.size());
                        diaryYearMonthListAdapter.submitList(convertedList);
                    }
                }
        );

        binding.viewDiaryListProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        loadDiaryList();
    }

    private class updateFirstVisibleSectionBarPositionOnScrollListener
            extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            diaryListSetting
                    .updateFirstVisibleSectionBarPosition(
                            recyclerView,
                            DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                    );
        }
    }

    private class addDiaryListOnScrollListener extends RecyclerView.OnScrollListener {
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
                return; // Adapter#getItemViewType()例外対策
            }
            int lastItemPosition = totalItemCount - 1;
            RecyclerView.Adapter<?> recyclerViewAdapter = recyclerView.getAdapter();
            if (recyclerViewAdapter == null) {
                return;
            }
            int lastItemViewType = recyclerViewAdapter.getItemViewType(lastItemPosition);
            // MEMO:下記条件"dy > 0"は検索結果リストが更新されたときに
            //      "RecyclerView.OnScrollListener#onScrolled"が起動するための対策。
            if (!diaryListViewModel.getIsLoading()
                    && (firstVisibleItem + visibleItemCount) >= totalItemCount
                    && dy > 0
                    && lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                diaryListViewModel.loadList(DiaryListViewModel.LoadType.ADD);
            }
        }
    }

    private void loadDiaryList() {
        List<DiaryYearMonthListItem> diaryYearMonthList =
                diaryListViewModel.getDiaryListLiveData().getValue();
        if (diaryYearMonthList == null || diaryYearMonthList.isEmpty()) {
            Integer numSavedDiaries = diaryListViewModel.countDiaries();
            if (numSavedDiaries != null && numSavedDiaries >= 1) {
                diaryListViewModel.loadList(DiaryListViewModel.LoadType.NEW);
            }
        } else {
            diaryListViewModel.loadList(DiaryListViewModel.LoadType.UPDATE);
        }
    }


    // TODO:public -> なし or private に変更しても良いか検証する
    //日記リスト(日)リサイクルビューホルダークラス
    /*public static class DiaryDayListViewHolder extends RecyclerView.ViewHolder {
        RowDiaryDayListBinding binding;
        LocalDate date;
        public DiaryDayListViewHolder(RowDiaryDayListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }*/

    //日記リスト(日)リサイクルビューアダプタクラス
    /*public class DiaryDayListAdapter extends ListAdapter<DiaryDayListItem, DiaryDayListViewHolder> {

        public DiaryDayListAdapter(@NonNull DiffUtil.ItemCallback<DiaryDayListItem> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public DiaryDayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // TODO:確認後削除
            *//*LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_day_list, parent, false);*//*

            RowDiaryDayListBinding binding =
                    RowDiaryDayListBinding
                            .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DiaryDayListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(DiaryDayListViewHolder holder, int position) {
            DiaryDayListItem item = getItem(position);
            LocalDate date = item.getDate();
            String title = item.getTitle();
            String picturePath = item.getPicturePath();

            holder.date = date; // ホルダー毎に日記の日付情報一式付与

            DayOfWeekNameResIdGetter dayOfWeekNameResIdGetter = new DayOfWeekNameResIdGetter();
            int dayOfWeekNameResId = dayOfWeekNameResIdGetter.getResId(date.getDayOfWeek());
            String strDayOfWeek = getString(dayOfWeekNameResId);
            holder.binding.includeDay.textDayOfWeek.setText(strDayOfWeek);

            holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            holder.binding.textRowDiaryListDayTitle.setText(title);
            // TODO:picturePath

            holder.binding.frameLayoutRowDiaryDayList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showShowDiary(date);
                }
            });

            // TODO:背面削除ボタン処理保留
            *//*holder.binding.textDeleteDiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionDiaryListFragmentToDeleteConfirmationDialog(date);
                    navController.navigate(action);
                }
            });*//*
        }
    }*/

    /*public static class DiaryDayListDiffUtilItemCallback extends DiffUtil.ItemCallback<DiaryDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryDayListItem oldItem, @NonNull DiaryDayListItem newItem) {
            if (!oldItem.getDate().equals(newItem.getDate())) {
                return false;
            }
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                return false;
            }
            if (!oldItem.getPicturePath().equals(newItem.getPicturePath())) {
                return false;
            }
            return true;
        }
    }*/


    // TODO:確認後削除
    //日記リスト(年月)リサイクルビューホルダークラス
    /*public static class DiaryYearMonthListViewHolder extends DiaryYearMonthListBaseViewHolder {
        public RecyclerView recyclerDayList;

        public DiaryYearMonthListViewHolder(View itemView) {
            super(itemView);
            textSectionBar = itemView.findViewById(R.id.text_section_bar);
            recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }*/

    /*public class DiaryYearMonthListAdapter
            extends ListAdapter<DiaryYearMonthListItem, RecyclerView.ViewHolder> {
        private final List<Map<String, Object>> diaryYearMonthList = new ArrayList<>(); // TODO:不要確認後削除
        private final List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();
        public static final int VIEW_TYPE_DIARY = 0;
        public static final int VIEW_TYPE_PROGRESS_BAR = 1;
        public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;

        public DiaryYearMonthListAdapter(@NonNull DiffUtil.ItemCallback<DiaryYearMonthListItem> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_DIARY) {
                View view =
                        inflater.inflate(R.layout.row_diary_year_month_list, parent, false);
                DiaryYearMonthListViewHolder holder = new DiaryYearMonthListViewHolder(view);

                // ホルダーアイテムアニメーション設定(理由は年月RecyclerView設定コード付近にコメントで記載)
                holder.recyclerDayList.setItemAnimator(null);

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
                        Log.d("リスト装飾確認",
                                Integer.toString(
                                        viewHolder.getBindingAdapterPosition()));
                        if (viewHolder.getBindingAdapterPosition()
                                == (adapter.getItemCount() - 1)) {
                            outRect.bottom = DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                        }
                    }
                });

                // ホルダー内の日記リスト(日)のアイテムにスワイプ機能(背面ボタン表示)を設定。
                // MEMO:スワイプでの背面ボタン表示機能はAndroidには存在しないので、
                //      ItemTouchHelper.Callback を継承して作成。
                CustomSimpleCallback simpleCallback = new CustomSimpleCallback(
                        ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT,
                        holder.recyclerDayList,
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
                // TODO:上手くいかないので保留
                *//*ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallBack);
                itemTouchHelper.attachToRecyclerView(holder.recyclerDayList);*//*
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
                DiaryYearMonthListViewHolder _holder = (DiaryYearMonthListViewHolder) holder;
                // 対象行の情報を取得
                DiaryYearMonthListItem item = getItem(position);
                YearMonth diaryYearMonth = item.getYearMonth();
                List<DiaryDayListItem> diaryDayList = item.getDiaryDayListItemList();

                // セクションバー設定
                // 左端に余白を持たせる為、最初にスペースを入力。
                String diaryDate = "  " + diaryYearMonth.getYear() + getString(R.string.row_list_year)
                        + diaryYearMonth.getMonthValue() + getString(R.string.row_list_month);
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
                diaryDayListAdapter.submitList(diaryDayList);
            }

        }

        @Override
        public int getItemViewType(int position ) {
            DiaryYearMonthListItem item = getItem(position);
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
    }*/

    /*public static class DiaryYearMonthListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<DiaryYearMonthListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryYearMonthListItem oldItem, @NonNull DiaryYearMonthListItem newItem) {
            // MEMO:更新時はリストアイテムを再インスタンス化する為、IDが異なり全アイテムfalseとなり、
            //      更新時リストが最上部へスクロールされてしまう。これを防ぐために下記処理を記述。
            // return oldItem.getId().equals(newItem.getId());
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListItem oldItem, @NonNull DiaryYearMonthListItem newItem) {
            // 年月
            if (oldItem.getYearMonth() != null && newItem.getYearMonth() != null
                    && !oldItem.getYearMonth().equals(newItem.getYearMonth())) {
                return false;
            }
            if (oldItem.getViewType() != newItem.getViewType()) {
                return false;
            }

            // 日
            int oldChildListSize = oldItem.getDiaryDayListItemList().size();
            int newChildListSize = newItem.getDiaryDayListItemList().size();
            Log.d("DiaryList", "oldChildListSize:" + oldChildListSize);
            Log.d("DiaryList", "newChildListSize:" + newChildListSize);
            if (oldChildListSize != newChildListSize) {
                return false;
            }

            int maxSize = Math.max(oldChildListSize, newChildListSize);
            for (int i = 0; i < maxSize; i++) {
                DiaryDayListItem oldChildListItem = oldItem.getDiaryDayListItemList().get(i);
                DiaryDayListItem newChildListItem = newItem.getDiaryDayListItemList().get(i);
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

            return true;
        }
    }*/

    //日記リスト(年月)(リサイクラービュー)のライナーレイアウトマネージャークラス
    //(コンストラクタに引数があるため、匿名クラス作成不可。メンバクラス作成。)
    private static class CustomLinearLayoutManager extends LinearLayoutManager {
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

    // TODO:上手くいかないので保留
    private static class DiaryDayListSimpleCallBack extends ItemTouchHelper.SimpleCallback {
        SwipedDiaryDate swipedDate;
        public DiaryDayListSimpleCallBack(int dragDirs, int swipeDirs, SwipedDiaryDate swipedDate) {
            super(dragDirs, swipeDirs);
            this.swipedDate = swipedDate;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.5F;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder =
                    (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            swipedDate.setDate(diaryDayListViewHolder.getDate());
            Log.d("20240701_1", "onSwiped");
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX,
                                float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
            Log.d("20240701", "dX:" + dX);
            Log.d("20240701", "viewHolderWidth:" + viewHolder.itemView.getWidth());

            View itemView = viewHolder.itemView;
            float transitionX =  dX;
            float absTransitionX =  Math.abs(dX);
            if (absTransitionX > itemView.getWidth()) {
                transitionX = -((float) itemView.getWidth() / 4);
            }

            super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder =
                    (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.getBinding().frameLayoutRowDiaryDayList,
                            0, 0, actionState, isCurrentlyActive
                    );

            /*if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
                diaryDayListViewHolder.binding.linerLayoutFront.setTranslationX(dX);
            }*/

            /*super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);
            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.frameLayoutRowDiaryDayList,
                            0, 0, actionState, isCurrentlyActive
                    );
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.linerLayoutFront,
                            transitionX, dY, actionState, isCurrentlyActive
                    );*/


            /*View itemView = viewHolder.itemView;
            float transitionX =  dX;
            float absTransitionX =  Math.abs(dX);
            if (absTransitionX > itemView.getWidth()) {
                transitionX = -((float) itemView.getWidth() / 4);
            }
            super.onChildDraw(c, recyclerView, viewHolder, transitionX, dY, actionState, isCurrentlyActive);

            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.textDeleteDiary,
                            0, 0, actionState, isCurrentlyActive
                    );
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.linerLayoutFront,
                            dX, dY, actionState, isCurrentlyActive
                    );*/
        }

        @Override
        public void clearView(     @NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                   @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder ) {
            super.clearView(recyclerView, viewHolder);
            Log.d("20240701_1", "clearView");
        }
    }

    // TODO:Sampleアプリへ移行
    private void createMenu() {
        //アクションバーオプションメニュー更新
        //https://qiita.com/Nabe1216/items/b26b03cbc750ac70a842
        /*MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                listmenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);*/
    }


    /*private class ListMenuProvider implements MenuProvider {

        // アクションバーオプションメニュー設定。
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            menuInflater.inflate(R.menu.diary_list_toolbar_menu, menu);

            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_calendar_month_24);

            *//*
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

             *//*
        }

        // アクションバーメニュー選択処理設定。
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            // ワード検索フラグメント起動
            if (menuItem.getItemId() == R.id.listToolbarOptionWordSearch) {
                wordSearchViewModel.initialize();

                NavDirections action =
                        DiaryListFragmentDirections
                                .actionNavigationDiaryListFragmentToWordSearchFragment();
                navController.navigate(action);
                return true;

            // リスト先頭年月切り替えダイアログ起動
            } else if (menuItem.getItemId() == android.R.id.home) {
                try {
                    String newestDate = diaryListViewModel.loadNewestDiary().getDate();
                    String oldestDate = diaryListViewModel.loadOldestDiary().getDate();
                    int newestYear = DateConverter.toLocalDate(newestDate).getYear();
                    int oldestYear = DateConverter.toLocalDate(oldestDate).getYear();
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionDiaryListFragmentToDatePickerDialog(newestYear, oldestYear);
                } catch (Exception e) {
                    String messageTitle = "通信エラー";
                    String message = "日記情報の読込に失敗しました。";
                    navigateMessageDialog(messageTitle, message);
                }
                return true;

            } else {
                return false;
            }
        }
    }*/

    private void setUpErrorObserver() {
        // エラー表示
        diaryListViewModel.getIsDiaryListLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryListLoadingErrorDialog();
                            diaryListViewModel.clearIsDiaryListLoadingError();
                        }
                    }
                });

        diaryListViewModel.getIsDiaryInformationLoadingErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryInformationLoadingErrorDialog();
                            diaryListViewModel.clearIsDiaryInformationLoadingError();
                        }
                    }
                });

        diaryListViewModel.getIsDiaryDeleteErrorLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == null) {
                            return;
                        }
                        if (aBoolean) {
                            showDiaryDeleteErrorDialog();
                            diaryListViewModel.clearIsDiaryDeleteError();
                        }
                    }
                });
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    public void diaryListScrollToFirstPosition() {
        Log.d("ボトムナビゲーションタップ確認", "scrollToFirstPosition()呼び出し");
        int position;
        position = diaryListYearMonthLinearLayoutManager.findLastVisibleItemPosition();
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
        binding.recyclerDiaryYearMonthList.smoothScrollToPosition(0);
    }

    private void showEditDiary() {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToEditDiaryFragment(
                                true,
                                false,
                                LocalDate.now()
                        );
        navController.navigate(action);
    }

    private void showShowDiaryFragment(LocalDate date) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToShowDiaryFragment(date);
        navController.navigate(action);
    }

    private void showWordSearchFragment() {
        wordSearchViewModel.initialize();
        NavDirections action =
                DiaryListFragmentDirections
                        .actionNavigationDiaryListFragmentToWordSearchFragment();
        navController.navigate(action);
    }

    private void showDatePickerDialog(Year newestYear, Year oldestYear) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToDatePickerDialog(newestYear, oldestYear);
        navController.navigate(action);
    }

    // 他のダイアログで表示できなかったダイアログを表示
    private void retryErrorDialogShow() {
        if (shouldShowDiaryListLoadingErrorDialog) {
            showDiaryListLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryInformationLoadingErrorDialog) {
            showDiaryInformationLoadingErrorDialog();
            return;
        }
        if (shouldShowDiaryDeleteErrorDialog) {
            showDiaryDeleteErrorDialog();
        }
    }

    private void showDiaryListLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記リストの読込に失敗しました。");
            shouldShowDiaryListLoadingErrorDialog = false;
        } else {
            shouldShowDiaryListLoadingErrorDialog = true;
        }
    }

    private void showDiaryInformationLoadingErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記情報の読込に失敗しました。");
            shouldShowDiaryInformationLoadingErrorDialog = false;
        } else {
            shouldShowDiaryInformationLoadingErrorDialog = true;
        }
    }

    private void showDiaryDeleteErrorDialog() {
        if (canShowDialog()) {
            showMessageDialog("通信エラー", "日記の削除に失敗しました。");
            shouldShowDiaryDeleteErrorDialog = false;
        } else {
            shouldShowDiaryDeleteErrorDialog = true;
        }
    }

    private void showMessageDialog(String title, String message) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    private boolean canShowDialog() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            return false;
        }
        int currentDestinationId = navController.getCurrentDestination().getId();
        return currentDestinationId == R.id.navigation_diary_list_fragment;
    }
}
