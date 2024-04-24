package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.ChangeFragment;
import com.websarva.wings.android.zuboradiary.Keyboard;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.UnitConverter;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.diary.showdiary.ShowDiaryFragment;
import com.websarva.wings.android.zuboradiary.ui.editdiary.EditDiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.CustomSimpleCallback;
import com.websarva.wings.android.zuboradiary.ui.list.ListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.ListViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSearchFragment extends Fragment {

    private FragmentWordSearchBinding binding;

    private WordSearchViewModel wordSearchViewModel;
    private EditDiaryViewModel diaryViewModel;
    private WordSearchMenuProvider wordSearchMenuProvider = new WordSearchMenuProvider();

    public WordSearchFragment() {
        // Required empty public constructor
    }

@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.wordSearchViewModel = provider.get(WordSearchViewModel.class);
        this.diaryViewModel = provider.get(EditDiaryViewModel.class);

        // 戻るボタン押下時の処理
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backFragment();

                    }
                }
        );

    // ShowDiaryFragmentからのデータ受取、受取後の処理
    getActivity().getSupportFragmentManager().setFragmentResultListener(
            "ToWordSearchFragment_ShowDiaryFragmentRequestKey",
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // データバインディング設定
        this.binding =
                FragmentWordSearchBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(WordSearchFragment.this);
        this.binding.setWordSearchViewModel(this.wordSearchViewModel);

        View root = this.binding.getRoot();

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        createMenu();

        // データベースから読み込んだ日記リストをリサクラービューに反映
        LiveData<List<WordSearchResultListItemDiary>> loadedWordSearchResultList =
                                        this.wordSearchViewModel.getLoadedWordSearchResultList();
        loadedWordSearchResultList.observe(
                getViewLifecycleOwner(),
                new Observer<List<WordSearchResultListItemDiary>>() {
                    @Override
                    public void onChanged(List<WordSearchResultListItemDiary> list) {
                        Log.d("20240424", "loadedWordSearchResultList onChanged起動");
                        String searchWord =
                                WordSearchFragment.this.wordSearchViewModel.getSearchWord();
                        if (searchWord.equals("")) {
                            return;
                        }
                        if (loadedWordSearchResultList.getValue().isEmpty()) {
                            Log.d("20240424", "onChanged起動 空確認");
                            WordSearchFragment.this.wordSearchViewModel
                                    .prepareWordSearchNoResultShowing();
                        } else {
                            Log.d("20240424", "onChanged起動 仕分け開始");
                            WordSearchFragment.this.wordSearchViewModel
                                    .prepareWordSearchResultShowing();
                            // 型変換:List<WordSearchResultListItemDiary> -> List<Map<String, Object>>
                            List<Map<String, Object>> dayList = new ArrayList<>();
                            Map<String, Object> map;
                            String date;
                            SpannableString title;
                            String itemNumber;
                            SpannableString itemTitle;
                            SpannableString itemComment;
                            String year;
                            String month;
                            String dayOfMonth;
                            String dayOfWeek;
                            int startIndex;
                            int endIndex;
                            for (WordSearchResultListItemDiary item: list) {
                                date = item.getDate();
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

                                title = createSpannableString(item.getTitle(), searchWord);

                                String regex = ".*" + searchWord + ".*";
                                String[] itemTitles = {
                                        item.getItem1Title(),
                                        item.getItem2Title(),
                                        item.getItem3Title(),
                                        item.getItem4Title(),
                                        item.getItem5Title(),
                                        };
                                String[] itemComments = {
                                        item.getItem1Comment(),
                                        item.getItem2Comment(),
                                        item.getItem3Comment(),
                                        item.getItem4Comment(),
                                        item.getItem5Comment(),
                                };
                                itemNumber = "";
                                itemTitle = new SpannableString("");
                                itemComment = new SpannableString("");
                                for (int i = 0; i < itemTitles.length; i++) {
                                    // HACK:タイトル、コメントは未入力の場合空文字("")が代入されるはずだが、
                                    //      nullの項目が存在する為、下記対策をとる。
                                    //      (例外：項目1のみ入力の場合は、2以降はnullとなる)
                                    if (itemTitles[i] == null) {
                                        itemTitles[i] = "";
                                    }
                                    if (itemComments[i] == null) {
                                        itemComments[i] = "";
                                    }
                                    if (itemTitles[i].matches(regex)
                                            || itemComments[i].matches(regex)) {
                                        itemNumber = "項目" + (i + 1);
                                        itemTitle =
                                                createSpannableString(itemTitles[i], searchWord);
                                        itemComment =
                                                createSpannableString(itemComments[i], searchWord);
                                        break;
                                    }
                                    if (i == (itemTitles.length - 1)) {
                                        itemNumber = "項目1";
                                        itemTitle =
                                                createSpannableString(itemTitles[0], searchWord);
                                        itemComment =
                                                createSpannableString(itemComments[0], searchWord);
                                    }
                                }

                                map = new HashMap<>();
                                map.put("Date", date);
                                map.put("Year", year);
                                map.put("Month", month);
                                map.put("DayOfMonth", dayOfMonth);
                                map.put("DayOfWeek", dayOfWeek);
                                map.put("Title", title);
                                map.put("ItemNumber", itemNumber);
                                map.put("ItemTitle", itemTitle);
                                map.put("ItemComment", itemComment);
                                dayList.add(map);
                            }


                            // 日記リストを月別に振り分ける
                            List<Map<String, Object>> sortingList= new ArrayList<>();
                            WordSearchResultDayListAdapter dayListAdapter;
                            Map<String, Object> monthListItem = new HashMap<>();
                            List<Map<String, Object>> monthList = new ArrayList<>();
                            String sortingYear = "";
                            String sortingMonth = "";

                            for (Map<String, Object> day: dayList) {

                                if (!(((String) day.get("Year")).equals(sortingYear))
                                        || !(((String) day.get("Month")).equals(sortingMonth))) {

                                    if (!(sortingYear.equals("")) && !(sortingMonth.equals(""))) {
                                        dayListAdapter =
                                                new WordSearchResultDayListAdapter(sortingList);
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

                            dayListAdapter = new WordSearchResultDayListAdapter(sortingList);
                            monthListItem.put("Year", sortingYear);
                            monthListItem.put("Month", sortingMonth);
                            monthListItem.put("Adapter", dayListAdapter);
                            monthList.add(monthListItem);

                            RecyclerView recyclerWordSearchResults =
                                    WordSearchFragment.this.binding.recyclerWordSearchResults;

                            ((WordSearchResultYearMonthListAdapter)
                                    recyclerWordSearchResults.getAdapter())
                                    .changeItem(monthList);

                        }
                    }
                }
        );

        RecyclerView recyclerWordSearchResults = this.binding.recyclerWordSearchResults;
        recyclerWordSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerWordSearchResults.setAdapter(new WordSearchResultYearMonthListAdapter());
        recyclerWordSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // ListHeaderSectionBarの年月更新
                updateListHeaderSectionBarDate();

                // 画面に表示されている日記リスト(年月)の2番目のアイテムのセクションバーがヘッダーセクションバーを押し出す動作
                View secondViewHolder
                        = recyclerWordSearchResults.getLayoutManager().getChildAt(1);
                TextView textHeaderSectionBar =
                        WordSearchFragment.this.binding.textHeaderSectionBar;

                if (secondViewHolder != null) {
                    float secondViewHolderPointY = secondViewHolder.getY();
                    Log.d("スクロール位置確認", String.valueOf(secondViewHolderPointY));

                    if (secondViewHolderPointY
                            <= (recyclerView.getY() + textHeaderSectionBar.getHeight())) {
                        Float Y = secondViewHolderPointY
                                - (recyclerView.getY() + textHeaderSectionBar.getHeight());
                        textHeaderSectionBar.setY(Y);
                    } else {
                        textHeaderSectionBar.setY(recyclerView.getY());
                    }

                } else {
                    textHeaderSectionBar.setY(recyclerView.getY());
                }

                // 日記リスト追加読込
                // https://android.suzu-sd.com/2021/05/recyclerview_item_scroll/#i-5
                if (!recyclerView.canScrollVertically(1)) {
                    WordSearchFragment.this.wordSearchViewModel.setLiveIsLoading(true);
                    WordSearchFragment.this.wordSearchViewModel.loadWordSearchResultList(
                            WordSearchViewModel.LoadType.ADD
                    );
                    /*try {
                        Thread.sleep(3000);
                        wait(3000);
                        ListFragment.this.wait(3000);
                    } catch (InterruptedException e) {
                        Log.d("待機確認", "失敗");
                    }*/
                    WordSearchFragment.this.wordSearchViewModel.setLiveIsLoading(false);
                }
            }
        });

    }


    private void createMenu() {
        //アクションバーオプションメニュー更新
        //https://qiita.com/Nabe1216/items/b26b03cbc750ac70a842
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(
                this.wordSearchMenuProvider,
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }


    private class WordSearchMenuProvider implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.word_search_toolbar_menu, menu);

            ActionBar actionBar = ((AppCompatActivity) requireActivity())
                    .getSupportActionBar();
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(null);

            // 検索欄設定
            MenuItem menuItem = menu.findItem(R.id.word_search_toolbar_menu_search);
            SearchView search = (SearchView) menuItem.getActionView();
            search.setIconifiedByDefault(false); // "false"でバー状態を常時表示
            int color = getResources().getColor(R.color.white);
            search.setBackgroundColor(color);

            search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d("20240418", "Focus");

                    View viewForHidingKeyboard = binding.viewForHidingKeyboard;
                    viewForHidingKeyboard.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Keyboard.hide(v);
                            search.clearFocus();

                            return false;
                        }
                    });
                }
            });

            // TODO:起動時即フォーカス、キーボード表示が上手くいかない
            //search.requestFocus();
            //search.requestFocusFromTouch();
            //Keyboard.show();

            // 検索欄横幅変更
            // HACK:アクションバーのメニューアイコンはLayoutParamsのMarginを変更しても
            //      変化しないのでX位置を変更して右余白を作成。)
            //      代わりにアイコン本体が変更した分見切れてしまう。
            //      その為検索欄をアクションバーの水平中央に移動させようとすると、おおきく見切れてしまう
            //      ので右余白を作成した状態で検索欄の横幅を大きくして対応。
            //      検索欄をアクションバー上に常時表示させようとすると、本来は個別でアクションバーに
            //      依存しないツールバーを用意する必要があるのかもしれない。
            ViewTreeObserver viewTreeObserver = search.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            ViewGroup.LayoutParams searchLayoutParams =
                                    search.getLayoutParams();
                            ViewGroup.MarginLayoutParams marginLayoutParams =
                                    (ViewGroup.MarginLayoutParams) searchLayoutParams;

                            //検索欄右余白設定
                            float rightMargin =
                                    UnitConverter.convertPx(16, getContext());
                            search.setX(- rightMargin); //初期位置が基準の為、"-"とする。

                            //検索欄横幅設定
                            marginLayoutParams.width = (int) (
                                    //画面横幅
                                    getView().getWidth()
                                            //戻るボタン横幅
                                            - UnitConverter.convertPx(48, getContext())
                                            //検索欄右余白
                                            - UnitConverter.convertPx(16, getContext())
                            );
                            search.setLayoutParams(marginLayoutParams);

                            ViewTreeObserver _viewTreeObserver =
                                    search.getViewTreeObserver();
                            _viewTreeObserver.removeOnGlobalLayoutListener(this);

                        }
                    }
            );
            String initialQuery =
                    WordSearchFragment.this.wordSearchViewModel.getSearchWord();
            search.setQuery(initialQuery, false);
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d("20240424", "onQueryTextChange起動");
                    WordSearchFragment.this.wordSearchViewModel
                            .setSearchWord(newText);
                    if (newText.equals("")) {
                        WordSearchFragment.this.wordSearchViewModel
                                .prepareBeforeWordSearchShowing();
                    } else {
                        WordSearchFragment.this.wordSearchViewModel
                                .loadWordSearchResultList(
                                        WordSearchViewModel.LoadType.NEW
                                );
                    }

                    return false;
                }
            });
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == android.R.id.home) {
                backFragment();
                return true;
            }
            return false;
        }
    }


    //日記リスト(日)リサイクルビューホルダークラス
    public class WordSearchResultDayViewHolder extends RecyclerView.ViewHolder {
        public TextView textDayOfWeek;
        public TextView textDayOfMonth;
        public TextView textWordSearchResultTitle;
        public TextView textWordSearchResultItemNumber;
        public TextView textWordSearchResultItemTitle;
        public TextView textWordSearchResultItemComment;

        public String date;
        public WordSearchResultDayViewHolder(View itemView) {
            super(itemView);
            textDayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            textDayOfMonth = itemView.findViewById(R.id.text_day_of_month);
            textWordSearchResultTitle = itemView.findViewById(R.id.text_word_search_result_title);
            textWordSearchResultItemNumber =
                    itemView.findViewById(R.id.text_word_search_result_item_number);
            textWordSearchResultItemTitle =
                    itemView.findViewById(R.id.text_word_search_result_item_title);
            textWordSearchResultItemComment =
                    itemView.findViewById(R.id.text_word_search_result_item_comment);

        }
    }

    //日記リスト(日)リサイクルビューアダプタクラス
    public class WordSearchResultDayListAdapter
            extends RecyclerView.Adapter<WordSearchResultDayViewHolder> {
        private List<Map<String, Object>> _DiaryListDay;

        public WordSearchResultDayListAdapter(List<Map<String, Object>> DiaryListDay){
            _DiaryListDay = DiaryListDay;
        }

        //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
        @Override
        public WordSearchResultDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_word_search_result, parent, false);
            WordSearchResultDayViewHolder holder = new WordSearchResultDayViewHolder(view);
            return holder;
        }

        //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(WordSearchResultDayViewHolder holder, int position) {
            Map<String, Object> item = _DiaryListDay.get(position);
            String dayOfWeek = (String) item.get("DayOfWeek");
            String dayOfMonth = (String) item.get("DayOfMonth");
            SpannableString title = (SpannableString) item.get("Title");
            String itemNumber = (String) item.get("ItemNumber");
            SpannableString itemTitle = (SpannableString) item.get("ItemTitle");
            SpannableString itemComment = (SpannableString) item.get("ItemComment");
            holder.textDayOfMonth.setText(dayOfWeek);
            holder.textDayOfMonth.setText(dayOfMonth);
            holder.textWordSearchResultTitle.setText(title);
            holder.textWordSearchResultItemNumber.setText(itemNumber);
            holder.textWordSearchResultItemTitle.setText(itemTitle);
            holder.textWordSearchResultItemComment.setText(itemComment);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //アクションバーのメニューを削除
                    //ナビフラグメントを閉じない状態(ナビフラグメントのライフサイクルがonResume状態)で前面フラグメントを起動する為、
                    //ナビフラグメントで追加したメニューが残る。下記はその為の処理。
                    // TODO:ツールバーをfragmentへもたせるため保留
                    MenuHost menuHost = requireActivity();
                    menuHost.removeMenuProvider(WordSearchFragment.this.wordSearchMenuProvider);

                    // ViewModel へデータセット
                    diaryViewModel.clear();
                    diaryViewModel.setLiveLoadingDate((String) item.get("Date"));

                    //日記表示フラグメント起動。
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    ChangeFragment.replaceFragment(
                            fragmentManager,
                            true,
                            R.id.front_fragmentContainerView_activity_main,
                            ShowDiaryFragment.class,
                            null
                    );
                }
            });

            //ホルダー毎に日記日付情報を持たせる。
            holder.date = (String) item.get("Date");
        }

        //日記リスト(日)のアイテム数を戻すメソッド
        @Override
        public int getItemCount() {
            return _DiaryListDay.size();
        }
    }


    //日記リスト(年月)リサイクルビューホルダークラス
    private class WordSearchResultYearMonthListViewHolder extends RecyclerView.ViewHolder {
        public TextView textSectionBar;
        public RecyclerView recyclerDayList;

        public WordSearchResultYearMonthListViewHolder(View itemView) {
            super(itemView);
            textSectionBar = itemView.findViewById(R.id.text_section_bar);
            recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }

    //日記リスト(年月)リサイクルビューアダプタクラス

    //private class WordSearchResultYearMonthListAdapter extends RecyclerView.Adapter<WordSearchResultYearMonthListViewHolder> {
    public class WordSearchResultYearMonthListAdapter extends RecyclerView.Adapter<WordSearchResultYearMonthListViewHolder> {
        private List<Map<String, Object>> _diaryListYearMonth = new ArrayList<>();
        private CustomSimpleCallback simpleCallback;
        private List<CustomSimpleCallback> simpleCallbacks = new ArrayList<>();

        public WordSearchResultYearMonthListAdapter(){
        }

        public WordSearchResultYearMonthListAdapter(List<Map<String, Object>> diaryListYearMonth){
            _diaryListYearMonth = diaryListYearMonth;
        }

        //日記リスト(年月)のホルダーと日記リスト(年月)のアイテムレイアウトを紐づける。
        @Override
        public WordSearchResultYearMonthListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("onCreateViewHolder確認", "起動");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_list_year_month, parent, false);
            WordSearchResultYearMonthListViewHolder holder = new WordSearchResultYearMonthListViewHolder(view);

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

            return holder;
        }

        //日記リスト(年月)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(WordSearchResultYearMonthListViewHolder holder, int position) {
            Log.d("リスト表示確認","onBindViewHolder呼び出し");

            // 対象行の情報を取得
            Map<String, Object> item = _diaryListYearMonth.get(position);
            String diaryYear = (String) item.get("Year");
            String diaryMonth = (String) item.get("Month");
            WordSearchResultDayListAdapter diaryDayListAdapter =
                    (WordSearchResultDayListAdapter) item.get("Adapter");

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


    private void updateListHeaderSectionBarDate() {
        Log.d("アクションバー日付更新確認", "更新");
        //日記リスト(年月)の画面表示中先頭アイテムのビューホルダーを取得
        RecyclerView recyclerWordSearchResults = binding.recyclerWordSearchResults;
        LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerWordSearchResults.getLayoutManager();
        int DiaryListFirstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder diaryListFirstViewHolder =
                recyclerWordSearchResults.findViewHolderForAdapterPosition(DiaryListFirstPosition);
        WordSearchResultYearMonthListViewHolder diaryListFirstYearMonthViewHolder =
                (WordSearchResultYearMonthListViewHolder) diaryListFirstViewHolder;

        // 日記リスト先頭ビューホルダーの年月情報を取得・リストヘッダーセクションバーへ表示
        String showedDate = diaryListFirstYearMonthViewHolder.textSectionBar.getText().toString();
        TextView textHeaderSectionBar = binding.textHeaderSectionBar;
        textHeaderSectionBar.setText(showedDate);


    }


    public void backFragment() {
        // TODO:前フラグメントへの処理有無後回し

        // HACK:キーボード表示状態で戻るボタンを押下すると、キーボドが表示したまま戻ってしまう。
        //      SearchView用キーボードだから？
        Keyboard.hide(getView()); // とりあえずフラグメントのレイアウトルートビューを代入

        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        NavController navController = navHostFragment.getNavController();
        navController.popBackStack();

    }

    public void updateList() {
        WordSearchFragment.this.wordSearchViewModel
                .loadWordSearchResultList(WordSearchViewModel.LoadType.UPDATE);
    }

    private SpannableString createSpannableString(String string, String searchWord) {
        SpannableString spannableString = new SpannableString(string);
        BackgroundColorSpan backgroundColorSpan =
                new BackgroundColorSpan(
                        getResources().getColor(R.color.gray)
                );
        int fromIndex = 0;
        while (string.indexOf(searchWord, fromIndex) != -1) {
            int start = string.indexOf(searchWord, fromIndex);
            int end = start + searchWord.length();
            spannableString.setSpan(
                    backgroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            fromIndex = end;
        }
        return spannableString;
    }


}
