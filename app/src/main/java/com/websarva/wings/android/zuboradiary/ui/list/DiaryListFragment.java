package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryDayListBinding;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiaryListFragment extends Fragment {

    // View関係
    private FragmentDiaryListBinding binding;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private DiaryListSetting<DiaryYearMonthListViewHolder> diaryListSetting;

    // Navigation関係
    private NavController navController;

    // ViewModel
    private DiaryListViewModel diaryListViewModel;
    private WordSearchViewModel wordSearchViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreate()処理");
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.diaryListViewModel = provider.get(DiaryListViewModel.class);
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

        // データバインディング設定
        this.binding.setLifecycleOwner(this);
        this.binding.setListViewModel(this.diaryListViewModel);

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

        setUpDialogResultReceiver();
        setUpToolBar();
        setUpFloatActionButton();
        setUpDiaryList();
    }

    // ダイアログフラグメントからの結果受取設定
    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry =
                this.navController.getBackStackEntry(R.id.navigation_diary_list_fragment);
        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    receiveDatePickerDialogResults(savedStateHandle);
                    receiveConfirmDeleteDialogResults(savedStateHandle);
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
    }

    // 日付入力ダイアログフラグメントから結果受取
    private void receiveDatePickerDialogResults(SavedStateHandle savedStateHandle) {
        boolean containsDatePickerDialogFragmentResults =
                savedStateHandle.contains(DatePickerDialogFragment.KEY_SELECTED_YEAR)
                        && savedStateHandle
                        .contains(DatePickerDialogFragment.KEY_SELECTED_MONTH);
        if (containsDatePickerDialogFragmentResults) {
            Integer selectedYear =
                    savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_YEAR);
            Integer selectedMonth =
                    savedStateHandle.get(DatePickerDialogFragment.KEY_SELECTED_MONTH);
            diaryListViewModel.updateSortConditionDate(selectedYear, selectedMonth);
            diaryListScrollToFirstPosition();
            diaryListViewModel.loadList(
                    DiaryListViewModel.LoadType.NEW,
                    new Runnable() {
                        @Override
                        public void run() {
                            String messageTitle = "通信エラー";
                            String message = "日記リストの読込に失敗しました。";
                            navigateMessageDialog(messageTitle, message);
                        }
                    });
        }
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_YEAR);
        savedStateHandle.remove(DatePickerDialogFragment.KEY_SELECTED_MONTH);
    }

    //
    private void receiveConfirmDeleteDialogResults(SavedStateHandle savedStateHandle) {
        // 日記削除ダイアログフラグメントから結果受取
        boolean ConfirmDeleteDialogFragmentFragmentResults =
                savedStateHandle.contains(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
        if (ConfirmDeleteDialogFragmentFragmentResults) {
            LocalDate deleteDiaryDate =
                    savedStateHandle.get(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
            try {
                DiaryListFragment.this.diaryListViewModel.deleteDiary(deleteDiaryDate);
            } catch (Exception e) {
                String messageTitle = "通信エラー";
                String message = "日記の削除に失敗しました。";
                navigateMessageDialog(messageTitle, message);
            }
        }
        savedStateHandle.remove(DeleteConfirmationDialogFragment.KEY_DELETE_DIARY_DATE);
    }

    // ツールバー設定
    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // リスト先頭年月切り替えダイアログ起動
                        try {
                            String newestDate = diaryListViewModel.loadNewestDiary().getDate();
                            String oldestDate = diaryListViewModel.loadOldestDiary().getDate();
                            int newestYear = DateConverter.toLocalDate(newestDate).getYear();
                            int oldestYear = DateConverter.toLocalDate(oldestDate).getYear();
                            NavDirections action =
                                    DiaryListFragmentDirections
                                            .actionDiaryListFragmentToDatePickerDialog(newestYear, oldestYear);
                            navController.navigate(action);
                        } catch (Exception e) {
                            String messageTitle = "通信エラー";
                            String message = "日記情報の読込に失敗しました。";
                            navigateMessageDialog(messageTitle, message);
                        }
                    }
                });
        binding.materialToolbarTopAppBar
                .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // ワード検索フラグメント起動
                        if (item.getItemId() == R.id.listToolbarOptionWordSearch) {
                            wordSearchViewModel.initialize();

                            NavDirections action =
                                    DiaryListFragmentDirections
                                            .actionNavigationDiaryListFragmentToWordSearchFragment();
                            navController.navigate(action);
                            return true;

                        }
                        return false;
                    }
                });
    }

    // 新規作成FAB設定
    private void setUpFloatActionButton() {
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
    }

    // 日記リスト(年月)設定
    private void setUpDiaryList() {
        RecyclerView recyclerDiaryYearMonthList = binding.recyclerDiaryYearMonthList;
        recyclerDiaryYearMonthList
                .setLayoutManager(diaryListYearMonthLinearLayoutManager);
        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                new DiaryYearMonthListAdapter(new DiaryYearMonthListDiffUtilItemCallback());
        recyclerDiaryYearMonthList.setAdapter(diaryYearMonthListAdapter);
        // HACK:下記問題が発生する為アイテムアニメーションを無効化
        //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
        //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
        //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
        //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
        //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerDiaryYearMonthList.setItemAnimator(null);
        recyclerDiaryYearMonthList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 日記リスト先頭アイテムセクションバー位置更新
                DiaryListFragment.this.diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerView,
                                DiaryListFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                        );


                // 日記リスト追加読込
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if (totalItemCount == 0) {
                    return; // Adapter#getItemViewType()例外対策
                }
                int lastItemPosition = totalItemCount - 1;
                int lastItemViewType = recyclerView.getAdapter().getItemViewType(lastItemPosition);
                // MEMO:下記条件"dy > 0"は検索結果リストが更新されたときに
                //      "RecyclerView.OnScrollListener#onScrolled"が起動するための対策。
                if (!DiaryListFragment.this.diaryListViewModel.getIsLoading()
                        && (firstVisibleItem + visibleItemCount) >= totalItemCount
                        && dy > 0
                        && lastItemViewType == DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                    DiaryListFragment.this.diaryListViewModel
                            .loadList(
                                    DiaryListViewModel.LoadType.ADD,
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            String messageTitle = "通信エラー";
                                            String message = "日記リストの読込に失敗しました。";
                                            navigateMessageDialog(messageTitle, message);
                                        }
                                    }
                            );
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
        if (this.diaryListViewModel.getLiveDataDiaryList().getValue().isEmpty()) {
            int numSavedDiaries = 0;
            try {
                numSavedDiaries = this.diaryListViewModel.countDiaries();
                Log.d("20240724", "numSavedDiaries:" + String.valueOf(numSavedDiaries));
            } catch (Exception e) {
                String messageTitle = "通信エラー";
                String message = "日記リストの読込に失敗しました。";
                navigateMessageDialog(messageTitle, message);
            }
            if (numSavedDiaries >= 1) {
                this.diaryListViewModel
                        .loadList(
                                DiaryListViewModel.LoadType.NEW,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        String messageTitle = "通信エラー";
                                        String message = "日記リストの読込に失敗しました。";
                                        navigateMessageDialog(messageTitle, message);
                                    }
                                }
                        );
            }
        } else {
            this.diaryListViewModel
                    .loadList(
                            DiaryListViewModel.LoadType.UPDATE,
                            new Runnable() {
                                @Override
                                public void run() {
                                    String messageTitle = "通信エラー";
                                    String message = "日記リストの読込に失敗しました。";
                                    navigateMessageDialog(messageTitle, message);
                                }
                            }
                    );
        }

        this.diaryListViewModel.getLiveDataDiaryList().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryYearMonthListItem>>() {
                    @Override
                    public void onChanged(List<DiaryYearMonthListItem> diaryListItems) {
                        DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                (DiaryYearMonthListAdapter)
                                        DiaryListFragment.this.binding
                                                .recyclerDiaryYearMonthList.getAdapter();
                        diaryYearMonthListAdapter.submitList(diaryListItems);
                    }
                }
        );

        this.binding.viewDiaryListProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });
    }


    // TODO:public -> なし or private に変更しても良いか検証する
    //日記リスト(日)リサイクルビューホルダークラス
    public class DiaryDayListViewHolder extends RecyclerView.ViewHolder {
        RowDiaryDayListBinding binding;
        public String date; // TODO:最終的に削除
        int year;
        int month;
        int dayOfMonth;
        public DiaryDayListViewHolder(RowDiaryDayListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
            // TODO:確認後削除
            /*LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_day_list, parent, false);*/

            RowDiaryDayListBinding binding =
                    RowDiaryDayListBinding
                            .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DiaryDayListViewHolder(binding);
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
            holder.year = year;
            holder.month = month;
            holder.dayOfMonth = dayOfMonth;
            holder.binding.includeDay.textDayOfWeek.setText(dayOfWeek);
            holder.binding.includeDay.textDayOfMonth.setText(String.valueOf(dayOfMonth));
            holder.binding.textRowDiaryListDayTitle.setText(title);
            holder.binding.frameLayoutRowDiaryDayList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 日記表示フラグメント起動。
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionNavigationDiaryListFragmentToShowDiaryFragment(
                                            LocalDate.of(year, month, dayOfMonth));
                    DiaryListFragment.this.navController.navigate(action);
                }
            });
            holder.binding.textDeleteDiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 日記表示フラグメント起動。
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionDiaryListFragmentToDeleteConfirmationDialog(
                                            DateConverter.toLocalDate(holder.date));
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
            if (oldItem.getPicturePath() != null
                    && newItem.getPicturePath() != null
                    && !oldItem.getPicturePath().equals(newItem.getPicturePath())) {
                return false;
            }
            return true;
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
        public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;

        public DiaryYearMonthListAdapter(@NonNull DiffUtil.ItemCallback<DiaryYearMonthListItem> diffCallback){
            super(diffCallback);
        }

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
                        ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT,
                        holder.recyclerDayList,
                        getContext(),
                        getResources().getDisplayMetrics().density,
                        getChildFragmentManager(), // TODO:左記不要確認後削除
                        DiaryListFragment.this.navController,
                        DiaryListFragment.this.binding.recyclerDiaryYearMonthList);
                this.simpleCallbacks.add(simpleCallback);
                DiaryDayListSimpleCallBack simpleCallBack =
                        new DiaryDayListSimpleCallBack(
                                ItemTouchHelper.ACTION_STATE_IDLE,
                                ItemTouchHelper.LEFT,
                                new SwipedDiaryDate()
                        );
                //ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallBack);
                itemTouchHelper.attachToRecyclerView(holder.recyclerDayList);
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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DiaryYearMonthListViewHolder) {
                DiaryYearMonthListViewHolder _holder = (DiaryYearMonthListViewHolder) holder;
                // 対象行の情報を取得
                DiaryYearMonthListItem item = getItem(position);
                int diaryYear = item.getYear();
                int diaryMonth = item.getMonth();
                List<DiaryDayListItem> diaryDayList = item.getDiaryDayListItemList();

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
            // MEMO:更新時はリストアイテムを再インスタンス化する為、IDが異なり全アイテムfalseとなり、
            //      更新時リストが最上部へスクロールされてしまう。これを防ぐために下記処理を記述。
            // return oldItem.getId().equals(newItem.getId());
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryYearMonthListItem oldItem, @NonNull DiaryYearMonthListItem newItem) {
            // 年月
            if (oldItem.getYear() != newItem.getYear()) {
                return false;
            }
            if (oldItem.getMonth() != newItem.getMonth()) {
                return false;
            }
            if (oldItem.getViewType() != newItem.getViewType()) {
                return false;
            }

            // 日
            int oldChildListSize = oldItem.getDiaryDayListItemList().size();
            int newChildListSize = newItem.getDiaryDayListItemList().size();
            Log.d("20240625", "oldChildListSize:" + String.valueOf(oldChildListSize));
            Log.d("20240625", "newChildListSize:" + String.valueOf(newChildListSize));
            if (oldChildListSize != newChildListSize) {
                return false;
            }

            int maxSize = Math.max(oldChildListSize, newChildListSize);
            for (int i = 0; i < maxSize; i++) {
                DiaryDayListItem oldChildListItem = oldItem.getDiaryDayListItemList().get(i);
                DiaryDayListItem newChildListItem = newItem.getDiaryDayListItemList().get(i);
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
                if (oldChildListItem.getPicturePath() != null
                        && newChildListItem.getPicturePath() != null
                        && !oldChildListItem.getPicturePath().equals(newChildListItem.getPicturePath())) {
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

    // TODO:上手くいかないので保留
    private class DiaryDayListSimpleCallBack extends ItemTouchHelper.SimpleCallback {
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
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.5F;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            this.swipedDate.setYear(diaryDayListViewHolder.year);
            this.swipedDate.setMonth(diaryDayListViewHolder.month);
            this.swipedDate.setDayOfMonth(diaryDayListViewHolder.dayOfMonth);
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
            DiaryDayListViewHolder diaryDayListViewHolder = (DiaryDayListViewHolder) viewHolder;
            getDefaultUIUtil()
                    .onDraw(
                            c, recyclerView, diaryDayListViewHolder.binding.frameLayoutRowDiaryDayList,
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
                try {
                    String newestDate = DiaryListFragment.this.diaryListViewModel.loadNewestDiary().getDate();
                    String oldestDate = DiaryListFragment.this.diaryListViewModel.loadOldestDiary().getDate();
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

    private void navigateMessageDialog(String title, String message) {
        NavDirections action =
                DiaryListFragmentDirections
                        .actionDiaryListFragmentToMessageDialog(
                                title, message);
        DiaryListFragment.this.navController.navigate(action);
    }

}
