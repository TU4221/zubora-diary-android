package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryListBinding;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchViewModel;

public class DiaryListFragment extends Fragment {

    // View関係
    private FragmentDiaryListBinding binding;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;

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
                    savedStateHandle.remove(com.websarva.wings.android.zuboradiary.ui.editdiary.DatePickerDialogFragment.KEY_SELECTED_YEAR);
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
                        NavDirections action =
                                DiaryListFragmentDirections.actionDiaryListFragmentToDatePickerDialog();
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
                // ViewModel へデータセット
                DiaryListFragment.this.diaryViewModel.clear();
                DiaryListFragment.this.diaryViewModel.setLiveLoadingDate("");
                DiaryListFragment.this.diaryViewModel.setIsNewEditDiary(true);

                // 日記編集(新規作成)フラグメント起動。
                NavDirections action =
                        DiaryListFragmentDirections
                                .actionNavigationDiaryListFragmentToEditDiaryFragment(true);
                DiaryListFragment.this.navController.navigate(action);
            }
        });

        // 日記リスト(年月)設定
        RecyclerView recyclerDiaryYearMonthList = this.binding.recyclerDiaryYearMonthList;
        recyclerDiaryYearMonthList
                .setLayoutManager(this.diaryListYearMonthLinearLayoutManager);
        recyclerDiaryYearMonthList.setAdapter(new DiaryYearMonthListAdapter());
        recyclerDiaryYearMonthList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Log.d("RecyclerViewスクロール状態確認", "SCROLL_STATE_IDLE");
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Log.d("RecyclerViewスクロール状態確認", "SCROLL_STATE_DRAGGING");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        Log.d("RecyclerViewスクロール状態確認", "SCROLL_STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // ListHeaderSectionBarの年月更新
                updateListHeaderSectionBarDate();

                // 画面に表示されている日記リスト(年月)の2番目のアイテムのセクションバーが
                // ヘッダーセクションバーを押し出す動作。
                View secondViewHolder =
                        DiaryListFragment.this.diaryListYearMonthLinearLayoutManager
                                .getChildAt(1);
                TextView textDiaryListHeaderSectionBar =
                        DiaryListFragment.this.binding.textDiaryListHeaderSectionBar;
                if (secondViewHolder != null) {
                    float secondViewHolderPointY = secondViewHolder.getY();

                    if (secondViewHolderPointY
                            <= (recyclerView.getY() + textDiaryListHeaderSectionBar.getHeight())) {
                        float Y = secondViewHolderPointY
                                - (recyclerView.getY() + textDiaryListHeaderSectionBar.getHeight());
                        textDiaryListHeaderSectionBar.setY(Y);
                    } else {
                        textDiaryListHeaderSectionBar.setY(recyclerView.getY());
                    }

                } else {
                    textDiaryListHeaderSectionBar.setY(recyclerView.getY());
                }

                // 日記リスト追加読込
                // https://android.suzu-sd.com/2021/05/recyclerview_item_scroll/#i-5
                if (!recyclerView.canScrollVertically(1)) {
                    DiaryListFragment.this.listViewModel.setLiveIsLoading(true);
                    DiaryListFragment.this.listViewModel.loadList(ListViewModel.LoadType.ADD);
                    DiaryListFragment.this.listViewModel.setLiveIsLoading(false);
                }
            }
        });
        recyclerDiaryYearMonthList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateListHeaderSectionBarDate();
            }
        });


        // 日記リスト読込
        if (this.listViewModel.getLoadedListItemDiaries().getValue().isEmpty()) {
            if (this.listViewModel.countDiaries() >= 1) {
                this.listViewModel.loadList(ListViewModel.LoadType.NEW);
            }
        } else {
            this.listViewModel.loadList(ListViewModel.LoadType.UPDATE);
        }

        // データベースから読み込んだ日記リストをリサクラービューに反映
        this.listViewModel.getLoadedListItemDiaries().observe(
                getViewLifecycleOwner(),
                new Observer<List<DiaryListItem>>() {
                    @Override
                    public void onChanged(List<DiaryListItem> listItemDiaries) {
                        List<DiaryListItem> list =
                                DiaryListFragment.this.listViewModel
                                        .getLoadedListItemDiaries().getValue();
                        if (list != null && list.isEmpty()) {
                            DiaryListFragment.this.listViewModel
                                    .setLiveIsVisibleHeaderSectionBar(false);
                        } else {
                            // 型変換:List<DiaryListItem> -> List<Map<String, Object>>
                            List<Map<String, Object>> dayList = new ArrayList<>();
                            Map<String, Object> map;
                            String date;
                            String title;
                            String picturePath;
                            String year;
                            String month;
                            String dayOfMonth;
                            String dayOfWeek;
                            final String KEY_DATE = DiaryDayListAdapter.KEY_DATE;
                            final String KEY_YEAR = DiaryYearMonthListAdapter.KEY_YEAR;
                            final String KEY_MONTH = DiaryYearMonthListAdapter.KEY_MONTH;
                            final String KEY_DAY_OF_MONTH = DiaryDayListAdapter.KEY_DAY_OF_MONTH;
                            final String KEY_DAY_OF_WEEK = DiaryDayListAdapter.KEY_DAY_OF_WEEK;
                            final String KEY_TITLE = DiaryDayListAdapter.KEY_TITLE;
                            final String KEY_PICTURE_PATH = DiaryDayListAdapter.KEY_PICTURE_PATH;
                            final String KEY_ADAPTER = DiaryYearMonthListAdapter.KEY_ADAPTER;
                            int startIndex;
                            int endIndex;
                            for (DiaryListItem diaryListItem : listItemDiaries) {
                                date = diaryListItem.getDate();
                                title = diaryListItem.getTitle();
                                picturePath = diaryListItem.getPicturePath();
                                startIndex = 0;
                                endIndex = date.indexOf("年");
                                year = date.substring(startIndex, endIndex);
                                startIndex = endIndex + 1;
                                endIndex = date.indexOf("月");
                                month = date.substring(startIndex, endIndex);
                                startIndex = endIndex + 1;
                                endIndex = date.indexOf("日");
                                dayOfMonth = date.substring(startIndex, endIndex);
                                startIndex = date.indexOf("(") + 1;
                                endIndex = date.indexOf(")");
                                dayOfWeek = date.substring(startIndex, endIndex);
                                map = new HashMap<>();
                                map.put(KEY_DATE, date);
                                map.put(KEY_YEAR, year);
                                map.put(KEY_MONTH, month);
                                map.put(KEY_DAY_OF_MONTH, dayOfMonth);
                                map.put(KEY_DAY_OF_WEEK, dayOfWeek);
                                map.put(KEY_TITLE, title);
                                map.put(KEY_PICTURE_PATH, picturePath);
                                dayList.add(map);
                            }

                            // 日記リストを月別に振り分ける
                            List<Map<String, Object>> sortingList= new ArrayList<>();
                            DiaryDayListAdapter dayListAdapter;
                            Map<String, Object> monthListItem = new HashMap<>();
                            List<Map<String, Object>> monthList = new ArrayList<>();
                            String sortingYear = "";
                            String sortingMonth = "";

                            for (Map<String, Object> day: dayList) {

                                String _year = (String) day.get(KEY_YEAR);
                                String _Month = (String) day.get(KEY_MONTH);
                                if (!(_year.equals(sortingYear))
                                        || !(_Month.equals(sortingMonth))) {

                                    if (!(sortingYear.equals("")) && !(sortingMonth.equals(""))) {
                                        dayListAdapter = new DiaryDayListAdapter(sortingList);
                                        monthListItem.put(KEY_YEAR, sortingYear);
                                        monthListItem.put(KEY_MONTH, sortingMonth);
                                        monthListItem.put(KEY_ADAPTER, dayListAdapter);
                                        monthList.add(monthListItem);
                                        monthListItem = new HashMap<>();
                                    }

                                    sortingYear = (String) day.get(KEY_YEAR);
                                    sortingMonth = (String) day.get(KEY_MONTH);
                                    sortingList = new ArrayList<>();
                                }

                                sortingList.add(day);
                            }

                            dayListAdapter = new DiaryDayListAdapter(sortingList);
                            monthListItem.put(KEY_YEAR, sortingYear);
                            monthListItem.put(KEY_MONTH, sortingMonth);
                            monthListItem.put(KEY_ADAPTER, dayListAdapter);
                            monthList.add(monthListItem);
                            monthListItem = new HashMap<>();

                            DiaryYearMonthListAdapter diaryYearMonthListAdapter =
                                    (DiaryYearMonthListAdapter)
                                            DiaryListFragment
                                                    .this.binding.recyclerDiaryYearMonthList
                                                    .getAdapter();
                            diaryYearMonthListAdapter.changeItem(monthList);

                            DiaryListFragment
                                    .this.listViewModel.setLiveIsVisibleHeaderSectionBar(true);
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
    public class DiaryDayListAdapter extends RecyclerView.Adapter<DiaryDayListViewHolder> {
        private List<Map<String, Object>> DiaryDayList;
        public static final String KEY_DAY_OF_WEEK = "DayOfWeek";
        public static final String KEY_DAY_OF_MONTH = "DayOfMonth";
        public static final String KEY_TITLE = "Title";
        public static final String KEY_PICTURE_PATH = "PicturePath";
        public static final String KEY_DATE = "Date";

        public DiaryDayListAdapter(List<Map<String, Object>> DiaryDayList){
            this.DiaryDayList = DiaryDayList;
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
            Map<String, Object> item = this.DiaryDayList.get(position);
            String dayOfWeek = (String) item.get(KEY_DAY_OF_WEEK);
            String dayOfMonth = (String) item.get(KEY_DAY_OF_MONTH);
            String title = (String) item.get(KEY_TITLE);
            holder.date = (String) item.get(KEY_DATE); // ホルダー毎に日記の日付情報一式付与
            holder.textDayOfWeek.setText(dayOfWeek);
            holder.textDayOfMonth.setText(dayOfMonth);
            holder.textTitle.setText(title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ViewModelへデータセット
                    DiaryListFragment.this.diaryViewModel.clear();
                    DiaryListFragment.this.diaryViewModel
                            .setLiveLoadingDate(
                                    (String) item.get(KEY_DATE)
                            );

                    // 日記表示フラグメント起動。
                    NavDirections action =
                            DiaryListFragmentDirections
                                    .actionNavigationDiaryListFragmentToShowDiaryFragment();
                    DiaryListFragment.this.navController.navigate(action);
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.DiaryDayList.size();
        }
    }


    //日記リスト(年月)リサイクルビューホルダークラス
    public class DiaryYearMonthListViewHolder extends RecyclerView.ViewHolder {
        public TextView textSectionBar;
        public RecyclerView recyclerDayList;

        public DiaryYearMonthListViewHolder(View itemView) {
            super(itemView);
            this.textSectionBar = itemView.findViewById(R.id.text_section_bar);
            this.recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }

    // 日記リスト(年月)リサイクルビューアダプタクラス
    public class DiaryYearMonthListAdapter
            extends RecyclerView.Adapter<DiaryYearMonthListViewHolder> {
        private List<Map<String, Object>> diaryYearMonthList = new ArrayList<>();
        private List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();
        public static final String KEY_YEAR = "Year";
        public static final String KEY_MONTH = "Month";
        public static final String KEY_ADAPTER = "Adapter";

        public DiaryYearMonthListAdapter(){
        }

        @Override
        public DiaryYearMonthListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("onCreateViewHolder確認", "起動");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
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
                    outRect.top = 16;
                    outRect.left = 32;
                    outRect.right = 32;

                    // TODO:Fragment切り替え方法をNavigationへの置換後、代替メソッド検討
                    Log.d("リスト装飾確認",
                            Integer.toString(
                                    parent.findContainingViewHolder(view).getAdapterPosition()));
                    if (parent.findContainingViewHolder(view).getAdapterPosition()
                            == (parent.getAdapter().getItemCount() - 1)) {
                        outRect.bottom = 16;
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
        }

        @Override
        public void onBindViewHolder(DiaryYearMonthListViewHolder holder, int position) {
            Log.d("リスト表示確認","onBindViewHolder呼び出し");

            // 対象行の情報を取得
            Map<String, Object> item = diaryYearMonthList.get(position);
            String diaryYear = (String) item.get(KEY_YEAR);
            String diaryMonth = (String) item.get(KEY_MONTH);
            DiaryDayListAdapter diaryDayListAdapter =
                    (DiaryDayListAdapter) item.get(KEY_ADAPTER);

            // セクションバー設定
            // 左端に余白を持たせる為、最初にスペースを入力。
            String diaryDate = "  " + diaryYear + getString(R.string.row_list_year)
                    + diaryMonth + getString(R.string.row_list_month);
            holder.textSectionBar.setText(diaryDate);

            // 日記リスト(日)設定
            // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、
            //      日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
            holder.recyclerDayList.setLayoutManager(new LinearLayoutManager(getContext()));
            holder.recyclerDayList.setAdapter(diaryDayListAdapter);

        }

        @Override
        public int getItemCount() {
            return this.diaryYearMonthList.size();
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

        // 日記リスト更新
        public void changeItem(List<Map<String, Object>> list) {
            this.diaryYearMonthList =list;

            // TODO:下記ページにエラー回避方法が記載されてる。後日修正。
            // https://qiita.com/toastkidjp/items/f6fffc44acbf4d3690fd
            notifyDataSetChanged();
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
                NavDirections action =
                        DiaryListFragmentDirections.actionDiaryListFragmentToDatePickerDialog();
                DiaryListFragment.this.navController.navigate(action);
                return true;

            } else {
                return false;
            }
        }
    }

    // 日記リストヘッダーセクションバーのテキスト更新
    private void updateListHeaderSectionBarDate() {
        Log.d("アクションバー日付更新確認", "更新");
        // 日記リスト(年月)の画面表示中先頭アイテムのビューホルダーを取得
        int DiaryListFirstPosition =
                this.diaryListYearMonthLinearLayoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder diaryListFirstViewHolder =
                this.binding.recyclerDiaryYearMonthList
                        .findViewHolderForAdapterPosition(DiaryListFirstPosition);
        if (diaryListFirstViewHolder != null) {
            Log.d("アクションバー日付更新確認", "diaryListFirstViewHolder != null");
            DiaryYearMonthListViewHolder diaryListFirstYearMonthViewHolder =
                    (DiaryYearMonthListViewHolder) diaryListFirstViewHolder;

            // 日記リスト先頭ビューホルダーの年月情報を取得・リストヘッダーセクションバーへ表示
            String showedDate =
                    diaryListFirstYearMonthViewHolder.textSectionBar.getText().toString();
            this.binding.textDiaryListHeaderSectionBar.setText(showedDate);
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
