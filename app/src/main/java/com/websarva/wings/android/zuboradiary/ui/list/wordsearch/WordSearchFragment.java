package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.Keyboard;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListSetting;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseViewHolder;
import com.websarva.wings.android.zuboradiary.ui.list.NoDiaryMessageViewHolder;
import com.websarva.wings.android.zuboradiary.ui.list.ProgressBarViewHolder;

import java.util.List;

public class WordSearchFragment extends Fragment {

    // View関係
    private FragmentWordSearchBinding binding;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL = 16;
    private final int DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL = 32;
    private DiaryListSetting<DiaryListFragment.DiaryYearMonthListViewHolder> diaryListSetting;
    private String beforeText = ""; // 二重検索防止用

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

        // 日記リスト設定クラスインスタンス化
        this.diaryListSetting = new DiaryListSetting<>();
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

        // 画面遷移時のアニメーション設定
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setEnterTransition(new MaterialFadeThrough());
            mainActivity.resetTabWasSelected();
        } else {
            setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        }
        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        // TO - FROM の FROM として現れるアニメーション
        /*if (switchesReenterTransition != null && switchesReenterTransition) {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }*/
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

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
        if (this.wordSearchViewModel.getSearchWord().getValue().isEmpty()) {
            this.binding.editTextKeyWordSearch.requestFocus();
            Keyboard.show(this.binding.editTextKeyWordSearch);
        }
        this.wordSearchViewModel.getSearchWord()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                        //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                        if (s.equals(WordSearchFragment.this.beforeText)) {
                            return;
                        }
                        WordSearchFragment.this.wordSearchViewModel
                                .setIsVisibleSearchWordClearButton(!s.isEmpty());
                        WordSearchFragment.this.wordSearchViewModel
                                .loadWordSearchResultListAsync(
                                        WordSearchViewModel.LoadType.NEW,
                                        s,
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                String messageTitle = "通信エラー";
                                                String message = "日記の読込に失敗しました。";
                                                navigateMessageDialog(messageTitle, message);
                                            }
                                        }
                                );
                        WordSearchFragment.this.beforeText = s;
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
                            v.performClick();
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
        // エンターキー押下時の処理
        // HACK:setImeOptions()メソッドを使用しなくても、onEditorAction()のactionIdはIME_ACTION_DONEとなるが、
        //      一応設定しておく。onEditorAction()のeventは常時nullとなっている。(ハードキーボードなら返ってくる？)
        //      https://vividcode.hatenablog.com/entry/android-app/oneditoractionlistener-practice
        this.binding.editTextKeyWordSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.binding.editTextKeyWordSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.KEYCODE_ENTER
                                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    Keyboard.hide(v);
                    v.clearFocus();
                    return true;
                }
                return false;
            }
        });
        this.binding.imageButtonKeyWordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordSearchFragment.this.wordSearchViewModel.clearSearchWord();
                WordSearchFragment.this.binding.editTextKeyWordSearch.requestFocus();
                Keyboard.show(WordSearchFragment.this.binding.editTextKeyWordSearch);
            }
        });


        // データベースから読み込んだ日記リストをリサクラービューに反映
        this.wordSearchViewModel.getLiveDataWordSearchResultList().observe(
                getViewLifecycleOwner(), new Observer<List<WordSearchResultYearMonthListItem>>() {
                    @Override
                    public void onChanged(
                            List<WordSearchResultYearMonthListItem> wordSearchResultYearMonthListItems) {
                        WordSearchResultYearMonthListAdapter wordSearchResultYearMonthListAdapter =
                                (WordSearchResultYearMonthListAdapter)
                                        WordSearchFragment.this.binding.recyclerWordSearchResults.getAdapter();
                        wordSearchResultYearMonthListAdapter.submitList(wordSearchResultYearMonthListItems);
                    }
        });

        RecyclerView recyclerWordSearchResults = this.binding.recyclerWordSearchResults;
        recyclerWordSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        WordSearchResultYearMonthListAdapter wordSearchResultYearMonthListAdapter =
                new WordSearchResultYearMonthListAdapter(
                        new WordSearchResultYearMonthListDiffUtilItemCallback()
                );
        recyclerWordSearchResults.setAdapter(wordSearchResultYearMonthListAdapter);
        // HACK:下記問題が発生する為アイテムアニメーションを無効化
        //      問題1.アイテム追加時もやがかかる。今回の構成(親Recycler:年月、子Recycler:日)上、
        //           既に表示されている年月に日のアイテムを追加すると、年月のアイテムに変更アニメーションが発生してしまう。
        //           これに対して、日のアイテムに追加アニメーションを発生させようとすると、
        //           年月のアイテムのサイズ変更にアニメーションが発生せず全体的に違和感となるアニメーションになってしまう。
        //      問題2.最終アイテムまで到達し、ProgressBarが消えた後にセクションバーがその分ずれる)
        recyclerWordSearchResults.setItemAnimator(null);
        recyclerWordSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 日記リスト先頭アイテムセクションバー位置更新
                WordSearchFragment.this.diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerView,
                                WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
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
                if (!WordSearchFragment.this.wordSearchViewModel.getIsLoading()
                        && (firstVisibleItem + visibleItemCount) >= totalItemCount
                        && dy > 0
                        && lastItemViewType == DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                    WordSearchFragment.this.wordSearchViewModel
                            .loadWordSearchResultListAsync(
                                    WordSearchViewModel.LoadType.ADD,
                                    WordSearchFragment.this.wordSearchViewModel.getSearchWord().getValue(),
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
        recyclerWordSearchResults.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 日記追加読込後RecyclerView更新時、セクションバーが元の位置に戻るので再度位置更新
                WordSearchFragment.this.diaryListSetting
                        .updateFirstVisibleSectionBarPosition(
                                recyclerWordSearchResults,
                                WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL
                        );
            }
        });

        // 検索結果リスト更新
        if (!this.wordSearchViewModel.getLiveDataWordSearchResultList().getValue().isEmpty()) {
            WordSearchFragment.this.wordSearchViewModel
                    .loadWordSearchResultListAsync(
                            WordSearchViewModel.LoadType.UPDATE,
                            WordSearchFragment.this.wordSearchViewModel.getSearchWord().getValue(),
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

        this.binding.viewWordSearchResultListProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
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
            extends ListAdapter<WordSearchResultDayListItem, WordSearchResultDayViewHolder> {

        public WordSearchResultDayListAdapter(
                @NonNull DiffUtil.ItemCallback<WordSearchResultDayListItem> diffCallback){
            super(diffCallback);
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
            WordSearchResultDayListItem item = getItem(position);
            int year = item.getYear();
            int month = item.getMonth();
            int dayOfMonth = item.getDayOfMonth();
            String dayOfWeek = item.getDayOfWeek();
            SpannableString title = item.getTitle();
            int itemNumber = item.getItemNumber();
            SpannableString itemTitle = item.getItemTitle();
            SpannableString itemComment = item.getItemComment();
            holder.date = DateConverter.toStringLocalDate(year, month, dayOfMonth); // ホルダー毎に日記の日付情報一式付与
            holder.textDayOfMonth.setText(dayOfWeek);
            holder.textDayOfMonth.setText(String.valueOf(dayOfMonth));
            holder.textWordSearchResultTitle.setText(title);
            String stringItemNumber = "項目" + String.valueOf(itemNumber);
            holder.textWordSearchResultItemNumber.setText(stringItemNumber);
            holder.textWordSearchResultItemTitle.setText(itemTitle);
            holder.textWordSearchResultItemComment.setText(itemComment);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //日記表示フラグメント起動。
                    NavDirections action =
                            WordSearchFragmentDirections
                                    .actionNavigationWordSearchFragmentToShowDiaryFragment(
                                            year, month, dayOfMonth);
                    WordSearchFragment.this.navController.navigate(action);
                }
            });
        }

    }

    public class WordSearchResultDayListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<WordSearchResultDayListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull WordSearchResultDayListItem oldItem, @NonNull WordSearchResultDayListItem newItem) {
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
            if (oldItem.getItemNumber() != newItem.getItemNumber()) {
                return false;
            }
            if (oldItem.getItemTitle() != null
                    && newItem.getItemTitle() != null
                    && !oldItem.getItemTitle().equals(newItem.getItemTitle())) {
                return false;
            }
            if (oldItem.getItemComment() != null
                    && newItem.getItemComment() != null
                    && !oldItem.getItemComment().equals(newItem.getItemComment())) {
                return false;
            }
            return true;
        }
    }


    //日記リスト(年月)リサイクルビューホルダークラス
    private class WordSearchResultYearMonthListBaseViewHolder extends DiaryYearMonthListBaseViewHolder {
        public RecyclerView recyclerDayList;

        public WordSearchResultYearMonthListBaseViewHolder(View itemView) {
            super(itemView);
            this.textSectionBar = itemView.findViewById(R.id.text_section_bar);
            this.recyclerDayList = itemView.findViewById(R.id.recycler_day_list);
        }
    }

    //日記リスト(年月)リサイクルビューアダプタクラス
    public class WordSearchResultYearMonthListAdapter
            extends ListAdapter<WordSearchResultYearMonthListItem, RecyclerView.ViewHolder> {
        public static final int VIEW_TYPE_DIARY = 0;
        public static final int VIEW_TYPE_PROGRESS_BAR = 1;
        public static final int VIEW_TYPE_NO_DIARY_MESSAGE = 2;

        public WordSearchResultYearMonthListAdapter(
                @NonNull DiffUtil.ItemCallback<WordSearchResultYearMonthListItem> diffCallback){
            super(diffCallback);
        }

        //日記リスト(年月)のホルダーと日記リスト(年月)のアイテムレイアウトを紐づける。
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_DIARY) {
                View view = inflater.inflate(R.layout.row_diary_year_month_list, parent, false);
                WordSearchResultYearMonthListBaseViewHolder holder = new WordSearchResultYearMonthListBaseViewHolder(view);

                //ホルダー内の日記リスト(日)のアイテム装飾の設定。
                //(onBindViewHolder で設定すると、設定内容が重複してアイテムが小さくなる為、onCreateViewHolder で設定)
                holder.recyclerDayList.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(
                            @NonNull Rect outRect,
                            @NonNull View view,
                            @NonNull RecyclerView parent,
                            @NonNull RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);
                        outRect.top = WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                        outRect.left = WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;
                        outRect.right = WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_HORIZONTAL;

                        // TODO:Fragment切り替え方法をNavigationへの置換後、代替メソッド検討
                        Log.d("リスト装飾確認",Integer.toString(parent.findContainingViewHolder(view).getAdapterPosition()));
                        if (parent.findContainingViewHolder(view).getAdapterPosition() == (parent.getAdapter().getItemCount() - 1)) {
                            outRect.bottom = WordSearchFragment.this.DIARY_DAY_LIST_ITEM_MARGIN_VERTICAL;
                        }
                    }
                });
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

        //日記リスト(年月)の各行アイテム(ホルダー)情報を設定。
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof WordSearchResultYearMonthListBaseViewHolder) {
                WordSearchResultYearMonthListBaseViewHolder _holder =
                        (WordSearchResultYearMonthListBaseViewHolder) holder;
                // 対象行の情報を取得
                WordSearchResultYearMonthListItem item = getItem(position);
                int diaryYear = item.getYear();
                int diaryMonth = item.getMonth();
                List<WordSearchResultDayListItem> wordSearchResultDayList =
                        item.getWordSearchResultDayList();

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
                WordSearchResultDayListAdapter wordSearchResultDayListAdapter =
                        new WordSearchResultDayListAdapter(new WordSearchResultDayListDiffUtilItemCallback());
                _holder.recyclerDayList.setAdapter(wordSearchResultDayListAdapter);
                wordSearchResultDayListAdapter.submitList(wordSearchResultDayList);
            }
        }

        @Override
        public int getItemViewType(int position ) {
            WordSearchResultYearMonthListItem item = getItem(position);
            return item.getViewType();
        }
    }

    public class WordSearchResultYearMonthListDiffUtilItemCallback
            extends DiffUtil.ItemCallback<WordSearchResultYearMonthListItem> {
        @Override
        public boolean areItemsTheSame(
                @NonNull WordSearchResultYearMonthListItem oldItem,
                @NonNull WordSearchResultYearMonthListItem newItem) {
            // MEMO:更新時はリストアイテムを再インスタンス化する為、IDが異なり全アイテムfalseとなり、
            //      更新時リストが最上部へスクロールされてしまう。これを防ぐために下記処理を記述。
            // return oldItem.getId().equals(newItem.getId());
            return true;
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull WordSearchResultYearMonthListItem oldItem,
                @NonNull WordSearchResultYearMonthListItem newItem) {
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
            int oldChildListSize = oldItem.getWordSearchResultDayList().size();
            int newChildListSize = newItem.getWordSearchResultDayList().size();
            if (oldChildListSize != newChildListSize) {
                return false;
            }

            int maxSize = Math.max(oldChildListSize, newChildListSize);
            for (int i = 0; i < maxSize; i++) {
                WordSearchResultDayListItem oldChildListItem =
                        oldItem.getWordSearchResultDayList().get(i);
                WordSearchResultDayListItem newChildListItem =
                        newItem.getWordSearchResultDayList().get(i);
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
                if (oldChildListItem.getItemNumber() != newChildListItem.getItemNumber()) {
                    return false;
                }
                if (oldChildListItem.getItemTitle() != null
                        && newChildListItem.getItemTitle() != null
                        && !oldChildListItem.getItemTitle().equals(newChildListItem.getItemTitle())) {
                    return false;
                }
                if (oldChildListItem.getItemComment() != null
                        && newChildListItem.getItemComment() != null
                        && !oldChildListItem.getItemComment().equals(newChildListItem.getItemComment())) {
                    return false;
                }
            }

            return true;
        }
    }

    private void navigateMessageDialog(String title, String message) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionWordSearchFragmentToMessageDialog(
                                title, message);
        WordSearchFragment.this.navController.navigate(action);
    }
}
