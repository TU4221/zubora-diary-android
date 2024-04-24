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
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryFragment;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentListBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchViewModel;

public class ListFragment extends Fragment {

    private ListViewModel listViewModel;
    private EditDiaryViewModel diaryViewModel;
    private WordSearchViewModel wordSearchViewModel;
    private FragmentListBinding binding;
    private ListMenuProvider listmenuProvider = new ListMenuProvider();
    private TextView textListHeaderSectionBar;
    private RecyclerView recyclerDiaryListYearMonth;
    private CustomLinearLayoutManager diaryListYearMonthLinearLayoutManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreate()処理");
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.listViewModel = provider.get(ListViewModel.class);
        this.diaryViewModel = provider.get(EditDiaryViewModel.class);
        this.wordSearchViewModel = provider.get(WordSearchViewModel.class);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // 処理なし(無効化)

                    }
                }
        );

        // DatePickerDialogFragmentからのデータ受取、受取後の処理
        getChildFragmentManager().setFragmentResultListener(
                "ToListFragment_DatePickerDialogFragmentRequestKey",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        listViewModel.updateSortConditionDate(
                                result.getInt("Year"),
                                result.getInt("Month"),
                                result.getInt("DayOfMonth")
                        );
                        listViewModel.loadList(ListViewModel.LoadType.NEW);
                    }
                }
        );

        // ConfirmDeleteDialogFragmentからのデータ受取、受取後の処理
        getChildFragmentManager().setFragmentResultListener(
                "ToListFragment_ConfirmDeleteDialogFragmentRequestKey",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        listViewModel.deleteDiary(result.getString("DeleteDate"));
                    }
                }
        );

        // EditDiaryFragmentからのデータ受取、受取後の処理
        getActivity().getSupportFragmentManager().setFragmentResultListener(
                "ToListFragment_EditDiaryFragmentRequestKey",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        Log.d("20240325","FragmentResult起動");
                        createMenu();
                        updateListHeaderSectionBarDate();

                    }
                }
        );

        // ShowDiaryFragmentからのデータ受取、受取後の処理
        getActivity().getSupportFragmentManager().setFragmentResultListener(
                "ToListFragment_ShowDiaryFragmentRequestKey",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        createMenu();
                        updateListHeaderSectionBarDate();
                    }
                }
        );
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("フラグメントライフサイクル確認", "onCreateView()処理");

        // データバインディング設定
        this.binding = FragmentListBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(ListFragment.this);
        this.binding.setListViewModel(listViewModel);

        // フィールド初期化
        this.recyclerDiaryListYearMonth = this.binding.rvListYearMonth;
        this.textListHeaderSectionBar = this.binding.textListHeaderSectionBar;
        // MEMO:クラスフィールド上でインスタンス化すると、ボトムナビゲーションのフラグメント切り替え時に例外発生。
        //      インスタンス化に引数がある為、匿名クラスは使用不可。
        this.diaryListYearMonthLinearLayoutManager = new CustomLinearLayoutManager(getContext());

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int i = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
        Log.d("20240412", String.valueOf(i));

        // アクションバーのメニュー作成
        createMenu();

        // 新規作成FAB設定
        FloatingActionButton fab = binding.fabWriteDiary;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //アクションバーのメニューを削除
                //ナビフラグメントを閉じない状態(ナビフラグメントのライフサイクルがonResume状態)で前面フラグメントを起動する為、
                //ナビフラグメントで追加したメニューが残る。下記はその為の処理。
                MenuHost menuHost = requireActivity();
                menuHost.removeMenuProvider(listmenuProvider);

                // ViewModel へデータセット
                diaryViewModel.clear();
                diaryViewModel.setLiveLoadingDate("");
                diaryViewModel.setIsNewEditDiary(true);

                // 日記編集(新規作成)フラグメント起動。
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(
                        R.id.front_fragmentContainerView_activity_main,
                        EditDiaryFragment.class,
                        null
                );
                fragmentTransaction.commit();
            }
        });

        // 日記リスト(年月)設定
        this.recyclerDiaryListYearMonth.setLayoutManager(this.diaryListYearMonthLinearLayoutManager);
        this.recyclerDiaryListYearMonth.setAdapter(new DiaryListYearMonthAdapter());
        this.recyclerDiaryListYearMonth.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Log.d("リストスクロール状態確認", "SCROLL_STATE_IDLE");
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Log.d("リストスクロール状態確認", "SCROLL_STATE_DRAGGING");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        Log.d("リストスクロール状態確認", "SCROLL_STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // ListHeaderSectionBarの年月更新
                updateListHeaderSectionBarDate();

                // 画面に表示されている日記リスト(年月)の2番目のアイテムのセクションバーがヘッダーセクションバーを押し出す動作
                View secondViewHolder
                        = diaryListYearMonthLinearLayoutManager.getChildAt(1);
                if (secondViewHolder != null) {
                    float secondViewHolderPointY = secondViewHolder.getY();
                    Log.d("スクロール位置確認", String.valueOf(secondViewHolderPointY));

                    if (secondViewHolderPointY
                            <= (recyclerView.getY() + textListHeaderSectionBar.getHeight())) {
                        Float Y = secondViewHolderPointY
                                - (recyclerView.getY() + textListHeaderSectionBar.getHeight());
                        textListHeaderSectionBar.setY(Y);
                    } else {
                        textListHeaderSectionBar.setY(recyclerView.getY());
                    }

                } else {
                    textListHeaderSectionBar.setY(recyclerView.getY());
                }

                // 日記リスト追加読込
                // https://android.suzu-sd.com/2021/05/recyclerview_item_scroll/#i-5
                if (!recyclerView.canScrollVertically(1)) {
                    listViewModel.setLiveIsLoading(true);
                    listViewModel.loadList(ListViewModel.LoadType.ADD);
                    /*try {
                        Thread.sleep(3000);
                        wait(3000);
                        ListFragment.this.wait(3000);
                    } catch (InterruptedException e) {
                        Log.d("待機確認", "失敗");
                    }*/
                    listViewModel.setLiveIsLoading(false);
                }
            }
        });

        // 日記リスト読込
        if (listViewModel.getLoadedListItemDiaries().getValue().isEmpty()) {
            if (listViewModel.countDiaries() >= 1) {
                listViewModel.loadList(ListViewModel.LoadType.NEW);
            }
        } else {
            listViewModel.loadList(ListViewModel.LoadType.UPDATE);
        }

        // データベースから読み込んだ日記リストをリサクラービューに反映
        listViewModel.getLoadedListItemDiaries().observe(
                getViewLifecycleOwner(),
                new Observer<List<ListItemDiary>>() {
                    @Override
                    public void onChanged(List<ListItemDiary> listItemDiaries) {
                        if (listViewModel.getLoadedListItemDiaries().getValue().isEmpty()) {
                            listViewModel.setLiveIsVisibleHeaderSectionBar(false);
                        } else {
                            // 型変換:List<ListItemDiary> -> List<Map<String, Object>>
                            List<Map<String, Object>> dayList = new ArrayList<>();
                            Map<String, Object> map;
                            String date;
                            String title;
                            String imagePath;
                            String year;
                            String month;
                            String dayOfMonth;
                            String dayOfWeek;
                            int startIndex;
                            int endIndex;
                            for (ListItemDiary listItemDiary: listItemDiaries) {
                                date = listItemDiary.getDate();
                                title = listItemDiary.getTitle();
                                imagePath = listItemDiary.getImagePath();
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
                                map.put("Date", date);
                                map.put("Year", year);
                                map.put("Month", month);
                                map.put("DayOfMonth", dayOfMonth);
                                map.put("DayOfWeek", dayOfWeek);
                                map.put("Title", title);
                                map.put("ImagePath", imagePath);
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

                                if (!(((String) day.get("Year")).equals(sortingYear))
                                        || !(((String) day.get("Month")).equals(sortingMonth))) {

                                    if (!(sortingYear.equals("")) && !(sortingMonth.equals(""))) {
                                        dayListAdapter = new DiaryDayListAdapter(sortingList);
                                        monthListItem.put("Year", sortingYear);
                                        monthListItem.put("Month", sortingMonth);
                                        monthListItem.put("Adapter", dayListAdapter);
                                        monthList.add(monthListItem);
                                        monthListItem = new HashMap<>();
                                    }

                                    sortingYear = (String) day.get("Year");
                                    sortingMonth = (String) day.get("Month");
                                    sortingList = new ArrayList<>();
                                }

                                sortingList.add(day);
                            }

                            dayListAdapter = new DiaryDayListAdapter(sortingList);
                            monthListItem.put("Year", sortingYear);
                            monthListItem.put("Month", sortingMonth);
                            monthListItem.put("Adapter", dayListAdapter);
                            monthList.add(monthListItem);
                            monthListItem = new HashMap<>();

                            ((DiaryListYearMonthAdapter) ListFragment.this
                                    .recyclerDiaryListYearMonth.getAdapter())
                                    .changeItem(monthList);

                            listViewModel.setLiveIsVisibleHeaderSectionBar(true);
                        }
                    }
                }
        );
    }

    @Override
    public void onResume() {
        Log.d("フラグメントライフサイクル確認", "onResume()起動");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("フラグメントライフサイクル確認", "onPause()処理");
    }

    @Override
    public void onDestroyView() {
        Log.d("フラグメントライフサイクル確認", "onDestroyView()起動");
        super.onDestroyView();
        binding = null;
    }


    //日記リスト(年月)内の日記リスト(日)が全て削除されたときに呼び出す。
    //日記リスト(日)を持たない日記リスト(年月)アイテムを削除する。
    public void deleteDiaryListYearMonthEmptyItem() {
        DiaryListYearMonthAdapter diaryListYearMonthAdapter = (DiaryListYearMonthAdapter) recyclerDiaryListYearMonth.getAdapter();
        DiaryDayListAdapter diaryDayListAdapter;
        for (int i = 0; i < diaryListYearMonthAdapter.getItemCount(); i++) {
            diaryDayListAdapter = (DiaryDayListAdapter) diaryListYearMonthAdapter._diaryListYearMonth.get(i).get("DiaryListDayAdapter");
            if (diaryDayListAdapter.getItemCount() == 0) {
                diaryListYearMonthAdapter.deleteItem(i);
                return;
            }
        }
    }


    //日記リスト(日)リサイクルビューホルダークラス
    public class DiaryListDayViewHolder extends RecyclerView.ViewHolder {
        public TextView _tvRowDiaryListDay_DayOfWeek;
        public TextView _tvRowDiaryListDay_Day;
        public TextView _tvRowDiaryListDay_Title;

        public String date;
        public DiaryListDayViewHolder(View itemView) {
            super(itemView);
            _tvRowDiaryListDay_DayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            _tvRowDiaryListDay_Day = itemView.findViewById(R.id.text_day_of_month);
            _tvRowDiaryListDay_Title = itemView.findViewById(R.id.tvRowDiaryListDay_Title);
        }
    }

    //日記リスト(日)リサイクルビューアダプタクラス
    public class DiaryDayListAdapter extends RecyclerView.Adapter<DiaryListDayViewHolder> {
        private List<Map<String, Object>> _DiaryListDay;

        public DiaryDayListAdapter(List<Map<String, Object>> DiaryListDay){
            _DiaryListDay = DiaryListDay;
        }

        //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
        @Override
        public DiaryListDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_list_day, parent, false);
            DiaryListDayViewHolder holder = new DiaryListDayViewHolder(view);
            return holder;
        }

        //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(DiaryListDayViewHolder holder, int position) {
            Map<String, Object> item = _DiaryListDay.get(position);
            String dayOfWeek = (String) item.get("DayOfWeek");
            String dayOfMonth = (String) item.get("DayOfMonth");
            String title = (String) item.get("Title");
            holder._tvRowDiaryListDay_DayOfWeek.setText(dayOfWeek);
            holder._tvRowDiaryListDay_Day.setText(dayOfMonth);
            holder._tvRowDiaryListDay_Title.setText(title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //アクションバーのメニューを削除
                    //ナビフラグメントを閉じない状態(ナビフラグメントのライフサイクルがonResume状態)で前面フラグメントを起動する為、
                    //ナビフラグメントで追加したメニューが残る。下記はその為の処理。
                    MenuHost menuHost = requireActivity();
                    menuHost.removeMenuProvider(listmenuProvider);

                    // ViewModel へデータセット
                    diaryViewModel.clear();
                    diaryViewModel.setLiveLoadingDate((String) item.get("Date"));

                    //日記表示フラグメント起動。
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setReorderingAllowed(true);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.front_fragmentContainerView_activity_main, ShowDiaryFragment.class, null);
                    fragmentTransaction.commit();
                }
            });

            //ホルダー毎に日記情報を持たせる。
            holder.date = (String) item.get("Date");
        }

        //日記リスト(日)のアイテム数を戻すメソッド
        @Override
        public int getItemCount() {
            return _DiaryListDay.size();
        }
    }


    //日記リスト(年月)リサイクルビューホルダークラス
    private class DiaryListYearMonthViewHolder extends RecyclerView.ViewHolder {
        public TextView textSectionBar;
        public RecyclerView recyclerDayList;

        public DiaryListYearMonthViewHolder(View itemView) {
            super(itemView);
            textSectionBar = itemView.findViewById(R.id.text_section_bar);
            recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }

    //日記リスト(年月)リサイクルビューアダプタクラス

    //private class WordSearchResultYearMonthListAdapter extends RecyclerView.Adapter<DiaryListYearMonthViewHolder> {
    public class DiaryListYearMonthAdapter extends RecyclerView.Adapter<DiaryListYearMonthViewHolder> {
        private List<Map<String, Object>> _diaryListYearMonth = new ArrayList<>();
        private CustomSimpleCallback simpleCallback;
        private List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();

        public DiaryListYearMonthAdapter(){
        }

        public DiaryListYearMonthAdapter(List<Map<String, Object>> diaryListYearMonth){
            _diaryListYearMonth = diaryListYearMonth;
        }

        //日記リスト(年月)のホルダーと日記リスト(年月)のアイテムレイアウトを紐づける。
        @Override
        public DiaryListYearMonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("onCreateViewHolder確認", "起動");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_list_year_month, parent, false);
            DiaryListYearMonthViewHolder holder = new DiaryListYearMonthViewHolder(view);

            //ホルダー内の日記リスト(日)のアイテム装飾の設定。
            //(onBindViewHolder で設定すると、設定内容が重複してアイテムが小さくなる為、onCreateViewHolder で設定)
            holder.recyclerDayList.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    Log.d("リスト装飾確認","getItemOffsets()呼び出し");
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.top = 16;
                    outRect.left = 32;
                    outRect.right = 32;
                    Log.d("リスト装飾確認",Integer.toString(parent.findContainingViewHolder(view).getAdapterPosition()));
                    if (parent.findContainingViewHolder(view).getAdapterPosition() == (parent.getAdapter().getItemCount() - 1)) {
                        outRect.bottom = 16;
                    }
                }
            });

            //ホルダー内の日記リスト(日)のアイテムにスワイプ機能(背面ボタン表示)を設定。
            //(スワイプでの背面ボタン表示機能はAndroidには存在しないので、ItemTouchHelper.Callback を継承して作成)
            simpleCallback = new CustomSimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT,
                    holder.recyclerDayList,
                    getContext(),
                    getResources().getDisplayMetrics().density,
                    getChildFragmentManager(),
                    binding.rvListYearMonth);
            simpleCallbacks.add(simpleCallback);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(holder.recyclerDayList);

            return holder;
        }

        //日記リスト(年月)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(DiaryListYearMonthViewHolder holder, int position) {
            Log.d("リスト表示確認","onBindViewHolder呼び出し");

            // 対象行の情報を取得
            Map<String, Object> item = _diaryListYearMonth.get(position);
            String diaryYear = (String) item.get("Year");
            String diaryMonth = (String) item.get("Month");
            DiaryDayListAdapter diaryDayListAdapter = (DiaryDayListAdapter) item.get("Adapter");

            // セクションバー設定
            // 左端に余白を持たせる為、最初にスペースを入力。
            String diaryDate = "  " + diaryYear + getString(R.string.row_list_year)
                    + diaryMonth + getString(R.string.row_list_month);
            holder.textSectionBar.setText(diaryDate);

            // 日記リスト(日)設定
            // MEMO:日記リスト(年月)のLinearLayoutManagerとは併用できないので、日記リスト(日)用のLinearLayoutManagerをインスタンス化する。
            holder.recyclerDayList.setLayoutManager(new LinearLayoutManager(getContext()));
            holder.recyclerDayList.setAdapter(diaryDayListAdapter);

        }

        //日記リスト(年月)のアイテム数を戻す。
        @Override
        public int getItemCount() {
            return _diaryListYearMonth.size();
        }

        //日記リスト(年月)の指定したアイテムを削除。
        public void deleteItem(int position) {
            _diaryListYearMonth.remove(position);
            notifyItemRemoved(position);
        }

        //日記リスト(年月)の一つのアイテム内の日記リスト(日)アイテムをスワイプした時、
        //他の日記リスト(年月)のアイテム内の日記リスト(日)の全アイテムをスワイプ前の状態に戻す。
        public void recoverOtherSwipedItem(CustomSimpleCallback customSimpleCallback) {
            for (int i = 0; i < simpleCallbacks.size(); i++) {
                if (customSimpleCallback != simpleCallbacks.get(i)) {
                    simpleCallbacks.get(i).recoverSwipeItem();
                }
            }

        }

        public void changeItem(List<Map<String, Object>> list) {
            _diaryListYearMonth =list;
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
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
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


    private void createMenu() {
        //アクションバーオプションメニュー更新
        //https://qiita.com/Nabe1216/items/b26b03cbc750ac70a842
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this.listmenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }


    private class ListMenuProvider implements MenuProvider {

        //アクションバーオプションメニュー設定。
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            menuInflater.inflate(R.menu.list_toolbar_menu, menu);

            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_calendar_month_24);

            /*
            //メモ
            //下記不具合が発生する為、 WordSearchResultYearMonthListAdapter#onBindViewHolder() でアクションバーのタイトルをセットする。
            //初めのフラグメント表示時は下記プログラムは問題ないが、
            //子フラグメントでこのフラグメントを隠して、子フラグメントを閉じるときに下記プログラムを処理させると、
            //ViewHolder が null で返されるため、テキストを取得しようとすると例外が発生する。
            //これは子フラグメントが完全に閉じる前(日記リストが表示(アダプターの計算)されていない状態)に
            //findViewHolderForAdapterPosition()が処理するため null が戻ってくる。

            int pos = diaryListYearMonthLinearLayoutManager.findFirstVisibleItemPosition();
            DiaryListYearMonthViewHolder viewHolder = (DiaryListYearMonthViewHolder) diaryListYearMonthRecyclerView.findViewHolderForAdapterPosition(pos);
            actionBar.setTitle(viewHolder._tvRowDiaryListYearMonth_Year.getText() + "年" + viewHolder._tvRowDiaryListYearMonth_Month.getText() + "月");

             */
        }

        //アクションバーメニュー選択処理設定。
        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

            //ワード検索フラグメント起動
            if (menuItem.getItemId() == R.id.listToolbarOptionWordSearch) {
                ListFragment.this.wordSearchViewModel.initialize();

                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.action_navigation_list_to_navigation_word_search);
                return true;

            //リスト先頭年月切り替えダイアログ起動
            } else if (menuItem.getItemId() == android.R.id.home) {
                DatePickerDialogFragment datePickerFragment = new DatePickerDialogFragment();
                datePickerFragment.show(getChildFragmentManager(), "datePicker");
                return true;

            } else {
                return false;
            }
        }
    }

    private void updateListHeaderSectionBarDate() {
        Log.d("アクションバー日付更新確認", "更新");
        //日記リスト(年月)の画面表示中先頭アイテムのビューホルダーを取得
        int DiaryListFirstPosition = diaryListYearMonthLinearLayoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder diaryListFirstViewHolder = recyclerDiaryListYearMonth.findViewHolderForAdapterPosition(DiaryListFirstPosition);
        DiaryListYearMonthViewHolder diaryListFirstYearMonthViewHolder = (DiaryListYearMonthViewHolder) diaryListFirstViewHolder;

        // 日記リスト先頭ビューホルダーの年月情報を取得・リストヘッダーセクションバーへ表示
        String showedDate = diaryListFirstYearMonthViewHolder.textSectionBar.getText().toString();
        textListHeaderSectionBar.setText(showedDate);


    }


    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    public void diaryListScrollToFirstPosition() {
        Log.d("ボトムナビゲーションタップ確認", "scrollToFirstPosition()呼び出し");
        int position;
        if (recyclerDiaryListYearMonth != null) {
            if (recyclerDiaryListYearMonth != null) {
                position = diaryListYearMonthLinearLayoutManager.findLastVisibleItemPosition();
                Log.d("スクロール動作確認", "position：" + position);

                //下記プログラムは、保留。
                //日記リスト(年月)のアイテム数が多い場合、
                //ユーザーが数多くのアイテムをスクロールした状態でsmoothScrollToPosition()を起動すると先頭にたどり着くのに時間がかかる。
                //その時間を回避する為に先頭付近へジャンプ(scrollToPosition())してからsmoothScrollToPosition()を起動させたかったが、
                //エミュレーターでは処理落ちで上手く確認できなかった。(プログラムの可能性もある)
                /*
                if (position >= 1) {
                    Log.d("スクロール動作確認", "scrollToPosition()呼出");
                    diaryListYearMonthRecyclerView.scrollToPosition(1);
                }
                 */

                Log.d("スクロール動作確認", "smoothScrollToPosition()呼出");
                recyclerDiaryListYearMonth.smoothScrollToPosition(0);
            }

        }
    }

    public void updateList() {
        listViewModel.loadList(ListViewModel.LoadType.UPDATE);
    }


}
