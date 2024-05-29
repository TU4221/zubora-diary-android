package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.Keyboard;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSearchFragment extends Fragment {

    // View関係
    private FragmentWordSearchBinding binding;

    // Navigation関係
    private NavController navController;

    // ViewModel
    private WordSearchViewModel wordSearchViewModel;
    private DiaryViewModel diaryViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        this.wordSearchViewModel = provider.get(WordSearchViewModel.class);
        this.diaryViewModel = provider.get(DiaryViewModel.class);

        // Navigation設定
        this.navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        // データバインディング設定
        this.binding =
                FragmentWordSearchBinding.inflate(inflater, container, false);

        // 双方向データバインディング設定
        this.binding.setLifecycleOwner(this);
        this.binding.setWordSearchViewModel(this.wordSearchViewModel);

        return this.binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ツールバー設定
        this.binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WordSearchFragment.this.navController.navigateUp();
                    }
                });


        // キーワード検索欄設定
        this.binding.editTextKeyWordSearch.requestFocus();
        Keyboard.show(this.binding.editTextKeyWordSearch);
        this.wordSearchViewModel.getSearchWord()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s.toString().isEmpty()) {
                            WordSearchFragment.this.wordSearchViewModel
                                    .setIsVisibleSearchWordClearButton(false);
                            WordSearchFragment.this.wordSearchViewModel
                                    .prepareBeforeWordSearchShowing();
                        } else {
                            WordSearchFragment.this.wordSearchViewModel
                                    .setIsVisibleSearchWordClearButton(true);
                            WordSearchFragment.this.wordSearchViewModel
                                    .loadWordSearchResultList(
                                            WordSearchViewModel.LoadType.NEW
                                    );
                        }
                    }
                });
        this.binding.editTextKeyWordSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View viewForHidingKeyboard =
                        WordSearchFragment.this.binding.viewForHidingKeyboard;
                if (hasFocus) {
                    viewForHidingKeyboard.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Keyboard.hide(v);
                            WordSearchFragment.this.binding.editTextKeyWordSearch.clearFocus();
                            return false;
                        }
                    });
                } else {
                    viewForHidingKeyboard.setOnTouchListener(null);
                }
            }
        });
        this.wordSearchViewModel.setIsVisibleSearchWordClearButton(false);
        this.binding.imageButtonKeyWordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordSearchFragment.this.wordSearchViewModel.clearSearchWord();
            }
        });


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
                                WordSearchFragment.this.wordSearchViewModel
                                        .getSearchWord().getValue();
                        if (searchWord.isEmpty()) {
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
                            String year;
                            String month;
                            String dayOfMonth;
                            String dayOfWeek;
                            SpannableString title;
                            String itemNumber;
                            SpannableString itemTitle;
                            SpannableString itemComment;
                            final String KEY_DATE = WordSearchResultDayListAdapter.KEY_DATE;
                            final String KEY_YEAR = WordSearchResultYearMonthListAdapter.KEY_YEAR;
                            final String KEY_MONTH = WordSearchResultYearMonthListAdapter.KEY_MONTH;
                            final String KEY_DAY_OF_MONTH =
                                    WordSearchResultDayListAdapter.KEY_DAY_OF_MONTH;
                            final String KEY_DAY_OF_WEEK =
                                    WordSearchResultDayListAdapter.KEY_DAY_OF_WEEK;
                            final String KEY_TITLE = WordSearchResultDayListAdapter.KEY_TITLE;
                            final String KEY_ITEM_NUMBER =
                                    WordSearchResultDayListAdapter.KEY_ITEM_NUMBER;
                            final String KEY_ITEM_TITLE =
                                    WordSearchResultDayListAdapter.KEY_ITEM_TITLE;
                            final String KEY_ITEM_COMMENT =
                                    WordSearchResultDayListAdapter.KEY_ITEM_COMMENT;
                            final String KEY_ADAPTER =
                                    WordSearchResultYearMonthListAdapter.KEY_ADAPTER;
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
                                map.put(KEY_DATE, date);
                                map.put(KEY_YEAR, year);
                                map.put(KEY_MONTH, month);
                                map.put(KEY_DAY_OF_MONTH, dayOfMonth);
                                map.put(KEY_DAY_OF_WEEK, dayOfWeek);
                                map.put(KEY_TITLE, title);
                                map.put(KEY_ITEM_NUMBER, itemNumber);
                                map.put(KEY_ITEM_TITLE, itemTitle);
                                map.put(KEY_ITEM_COMMENT, itemComment);
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

                                if (!(((String) day.get(KEY_YEAR)).equals(sortingYear))
                                        || !(((String) day.get(KEY_MONTH)).equals(sortingMonth))) {

                                    if (!(sortingYear.equals("")) && !(sortingMonth.equals(""))) {
                                        dayListAdapter =
                                                new WordSearchResultDayListAdapter(sortingList);
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

                            dayListAdapter = new WordSearchResultDayListAdapter(sortingList);
                            monthListItem
                                    .put(KEY_YEAR, sortingYear);
                            monthListItem
                                    .put(KEY_MONTH, sortingMonth);
                            monthListItem
                                    .put(KEY_ADAPTER, dayListAdapter);
                            monthList.add(monthListItem);

                            RecyclerView recyclerWordSearchResults =
                                    WordSearchFragment.this.binding.recyclerWordSearchResults;

                            WordSearchResultYearMonthListAdapter
                                    wordSearchResultYearMonthListAdapter =
                                    (WordSearchResultYearMonthListAdapter)
                                            recyclerWordSearchResults.getAdapter();
                            wordSearchResultYearMonthListAdapter.changeItem(monthList);

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

                // 画面に表示されている日記リスト(年月)の2番目のアイテムのセクションバーが
                // ヘッダーセクションバーを押し出す動作
                View secondViewHolder =
                        WordSearchFragment.this.binding.recyclerWordSearchResults
                                .getLayoutManager().getChildAt(1);
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
                    WordSearchFragment.this.wordSearchViewModel.setIsLoading(true);
                    WordSearchFragment.this.wordSearchViewModel
                            .loadWordSearchResultList(WordSearchViewModel.LoadType.ADD);
                    WordSearchFragment.this.wordSearchViewModel.setIsLoading(false);
                }
            }
        });

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
        private List<Map<String, Object>> DiaryDayList;
        public static final String KEY_DAY_OF_WEEK = "DayOfWeek";
        public static final String KEY_DAY_OF_MONTH = "DayOfMonth";
        public static final String KEY_TITLE = "Title";
        public static final String KEY_ITEM_NUMBER = "ItemNumber";
        public static final String KEY_ITEM_TITLE = "ItemTitle";
        public static final String KEY_ITEM_COMMENT = "ItemComment";
        public static final String KEY_DATE = "Date";


        public WordSearchResultDayListAdapter(List<Map<String, Object>> DiaryDayList){
            this.DiaryDayList = DiaryDayList;
        }

        //日記リスト(日)のホルダーと日記リスト(日)のアイテムレイアウトを紐づける。
        @NonNull
        @Override
        public WordSearchResultDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_word_search_result, parent, false);
            return new WordSearchResultDayViewHolder(view);
        }

        //日記リスト(日)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(WordSearchResultDayViewHolder holder, int position) {
            Map<String, Object> item = DiaryDayList.get(position);
            String dayOfWeek = (String) item.get(KEY_DAY_OF_WEEK);
            String dayOfMonth = (String) item.get(KEY_DAY_OF_MONTH);
            SpannableString title = (SpannableString) item.get(KEY_TITLE);
            String itemNumber = (String) item.get(KEY_ITEM_NUMBER);
            SpannableString itemTitle = (SpannableString) item.get(KEY_ITEM_TITLE);
            SpannableString itemComment = (SpannableString) item.get(KEY_ITEM_COMMENT);
            holder.date = (String) item.get(KEY_DATE); // ホルダー毎に日記の日付情報一式付与
            holder.textDayOfMonth.setText(dayOfWeek);
            holder.textDayOfMonth.setText(dayOfMonth);
            holder.textWordSearchResultTitle.setText(title);
            holder.textWordSearchResultItemNumber.setText(itemNumber);
            holder.textWordSearchResultItemTitle.setText(itemTitle);
            holder.textWordSearchResultItemComment.setText(itemComment);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ViewModel へデータセット
                    WordSearchFragment.this.diaryViewModel.clear();
                    WordSearchFragment.this.diaryViewModel
                            .setLiveLoadingDate(
                                    (String) item.get(KEY_DATE)
                            );

                    //日記表示フラグメント起動。
                    NavDirections action =
                            WordSearchFragmentDirections
                                    .actionNavigationWordSearchFragmentToShowDiaryFragment();
                    WordSearchFragment.this.navController.navigate(action);
                }
            });


        }

        //日記リスト(日)のアイテム数を戻すメソッド
        @Override
        public int getItemCount() {
            return this.DiaryDayList.size();
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
    public class WordSearchResultYearMonthListAdapter extends RecyclerView.Adapter<WordSearchResultYearMonthListViewHolder> {
        private List<Map<String, Object>> diaryListYearMonth = new ArrayList<>();
        public static final String KEY_YEAR = "Year";
        public static final String KEY_MONTH = "Month";
        public static final String KEY_ADAPTER = "Adapter";

        public WordSearchResultYearMonthListAdapter(){
        }

        //日記リスト(年月)のホルダーと日記リスト(年月)のアイテムレイアウトを紐づける。
        @Override
        public WordSearchResultYearMonthListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("onCreateViewHolder確認", "起動");
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.row_diary_year_month_list, parent, false);
            WordSearchResultYearMonthListViewHolder holder = new WordSearchResultYearMonthListViewHolder(view);

            //ホルダー内の日記リスト(日)のアイテム装飾の設定。
            //(onBindViewHolder で設定すると、設定内容が重複してアイテムが小さくなる為、onCreateViewHolder で設定)
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
            Map<String, Object> item = diaryListYearMonth.get(position);
            String diaryYear = (String) item.get(KEY_YEAR);
            String diaryMonth = (String) item.get(KEY_MONTH);
            WordSearchResultDayListAdapter diaryDayListAdapter =
                    (WordSearchResultDayListAdapter) item.get(KEY_ADAPTER);

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
            return this.diaryListYearMonth.size();
        }

        // 日記リスト更新
        public void changeItem(List<Map<String, Object>> list) {
            this.diaryListYearMonth =list;

            // TODO:下記ページにエラー回避方法が記載されてる。後日修正。
            // https://qiita.com/toastkidjp/items/f6fffc44acbf4d3690fd
            notifyDataSetChanged();
        }
    }

    // 日記リストヘッダーセクションバーのテキスト更新
    private void updateListHeaderSectionBarDate() {
        Log.d("アクションバー日付更新確認", "更新");
        //日記リスト(年月)の画面表示中先頭アイテムのビューホルダーを取得
        LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) this.binding.recyclerWordSearchResults.getLayoutManager();
        int DiaryListFirstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        RecyclerView.ViewHolder diaryListFirstViewHolder =
                this.binding.recyclerWordSearchResults
                        .findViewHolderForAdapterPosition(DiaryListFirstPosition);
        if (diaryListFirstViewHolder != null) {
            WordSearchResultYearMonthListViewHolder diaryListFirstYearMonthViewHolder =
                    (WordSearchResultYearMonthListViewHolder) diaryListFirstViewHolder;

            // 日記リスト先頭ビューホルダーの年月情報を取得・リストヘッダーセクションバーへ表示
            String showedDate = diaryListFirstYearMonthViewHolder.textSectionBar.getText().toString();
            this.binding.textHeaderSectionBar.setText(showedDate);
        }
    }


    // 対象ワードをマーキング
    private SpannableString createSpannableString(String string, String targetWord) {
        SpannableString spannableString = new SpannableString(string);
        BackgroundColorSpan backgroundColorSpan =
                new BackgroundColorSpan(
                        getResources().getColor(R.color.gray)
                );
        int fromIndex = 0;
        while (string.indexOf(targetWord, fromIndex) != -1) {
            int start = string.indexOf(targetWord, fromIndex);
            int end = start + targetWord.length();
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
