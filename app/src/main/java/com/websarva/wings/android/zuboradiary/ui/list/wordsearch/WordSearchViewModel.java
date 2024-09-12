package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WordSearchViewModel extends BaseViewModel {

    private final DiaryRepository diaryRepository;
    private final MutableLiveData<String> searchWord = new MutableLiveData<>();
    private Future<?> wordSearchResultListLoadingFuture; // キャンセル用
    private final MutableLiveData<List<WordSearchResultYearMonthListItem>> wordSearchResultList =
            new MutableLiveData<>();
    private final MutableLiveData<Integer> numWordSearchResults = new MutableLiveData<>();
    // TODO:Visible変数を削除してFragment上で制御できるか検討(UpdateはViewModelの方が簡潔に制御できる？)
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private static final int NUM_LOADING_ITEMS = 10; //リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    private final ExecutorService executorService;


    @Inject
    public WordSearchViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        searchWord.setValue("");
        wordSearchResultList.setValue(new ArrayList<>());
        numWordSearchResults.setValue(0);
        isVisibleUpdateProgressBar.setValue(false);
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public boolean canLoadWordSearchResultList() {
        Log.d("OnScrollDiaryList", "isLoadingDiaryList()");
        if (wordSearchResultListLoadingFuture == null) {
            Log.d("OnScrollDiaryList", "wordSearchResultListLoadingFuture == null");
            return true;
        }
        return wordSearchResultListLoadingFuture.isDone();
    }

    public void loadWordSearchResultList(
            LoadType loadType, int spannableStringColor, int spannableStringBackGroundColor){
        if (!canLoadWordSearchResultList()) {
            Log.d("WordSearchLoading","Cancel");
            wordSearchResultListLoadingFuture.cancel(true);
        }
        String searchWord = this.searchWord.getValue();
        if (searchWord == null || searchWord.isEmpty()) {
            isVisibleUpdateProgressBar.setValue(false);
            wordSearchResultList.setValue(new ArrayList<>());
            return;
        }
        Runnable loadWordSearchResultList =
                new loadWordSearchResultList(
                        loadType, searchWord, spannableStringColor, spannableStringBackGroundColor);
        wordSearchResultListLoadingFuture = executorService.submit(loadWordSearchResultList);
    }

    private class loadWordSearchResultList implements Runnable {
        LoadType loadType;
        String searchWord;
        int spannableStringColor;
        int spannableStringBackGroundColor;

        public loadWordSearchResultList(
                LoadType loadType, String searchWord, int spannableStringColor, int spannableStringBackGroundColor) {
            this.loadType = loadType;
            this.searchWord = searchWord;
            this.spannableStringColor = spannableStringColor;
            this.spannableStringBackGroundColor = spannableStringBackGroundColor;
        }
        @Override
        public void run() {
            boolean isValidityDelay = true;// TODO:調整用
            Log.d("WordSearchLoading", "run()_start");
            List<WordSearchResultYearMonthListItem> previousResultList = new ArrayList<>();
            try {
                // 日記リスト読込準備
                List<WordSearchResultYearMonthListItem> currentResultList =
                        wordSearchResultList.getValue();
                int numLoadingItems;
                int loadingOffset;
                if (loadType == LoadType.UPDATE) {
                    isVisibleUpdateProgressBar.postValue(true);
                    if (currentResultList == null || currentResultList.isEmpty()) {
                        return;
                    }
                    numLoadingItems = countDiaryListDayItem(currentResultList);
                    if (numLoadingItems < NUM_LOADING_ITEMS) {
                        numLoadingItems = NUM_LOADING_ITEMS;
                    }
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    isVisibleUpdateProgressBar.postValue(false);
                    numLoadingItems = NUM_LOADING_ITEMS;
                    if (currentResultList == null || currentResultList.isEmpty()) {
                        // TODO:assert
                        return;
                    } else {
                        loadingOffset = countDiaryListDayItem(currentResultList);
                    }
                } else {
                    // LoadType.NEW
                    isVisibleUpdateProgressBar.postValue(false);
                    numLoadingItems = NUM_LOADING_ITEMS;
                    loadingOffset = 0;
                    numWordSearchResults.postValue(0);
                }

                // 現時点のDiaryListをCloneで生成
                if (loadType != LoadType.NEW) {
                    if (currentResultList != null && !currentResultList.isEmpty()) {
                        for (WordSearchResultYearMonthListItem item : currentResultList) {
                            WordSearchResultYearMonthListItem cloneItem = item.clone();
                            previousResultList.add(cloneItem);
                        }
                    }
                }

                // ProgressBar表示
                List<WordSearchResultYearMonthListItem> resultListContainingProgressBar = new ArrayList<>();
                if (loadType == LoadType.NEW) {
                    WordSearchResultYearMonthListItem progressBar =
                            new WordSearchResultYearMonthListItem(
                                    DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR
                            );
                    resultListContainingProgressBar.add(progressBar);
                    wordSearchResultList.postValue(resultListContainingProgressBar);
                }

                if (isValidityDelay) {
                    // TODO:ProgressBarを表示させる為に仮で記述
                    Thread.sleep(1000);
                }

                // 日記リスト読込
                Integer numWordSearchResults;
                List<WordSearchResultYearMonthListItem> convertedLoadingData = new ArrayList<>();
                // TODO:下記if条件を廃止して、DiaryListと同様に毎回カウントするか検討
                if (loadType == LoadType.NEW || loadType == LoadType.UPDATE) {
                    ListenableFuture<Integer> listenableFutureResult =
                            diaryRepository.countWordSearchResults(searchWord);
                    numWordSearchResults = listenableFutureResult.get();
                } else {
                    // loadType == LoadType.ADD
                    numWordSearchResults = WordSearchViewModel.this.numWordSearchResults.getValue();
                    if (numWordSearchResults == null) {
                        ListenableFuture<Integer> listenableFutureResult =
                                diaryRepository.countWordSearchResults(searchWord);
                        numWordSearchResults = listenableFutureResult.get();
                    }
                }
                ListenableFuture<List<WordSearchResultListItem>> listenableFutureResults =
                        diaryRepository.selectWordSearchResultListOrderByDateDesc(
                                numLoadingItems,
                                loadingOffset,
                                searchWord
                        );

                List<WordSearchResultListItem> loadingData = listenableFutureResults.get();
                if (!loadingData.isEmpty()) {
                    convertedLoadingData =
                            toWordSearchResultYearMonthListFormat(
                                    loadingData, searchWord, spannableStringColor, spannableStringBackGroundColor);
                }


                // 更新用日記リスト準備
                List<WordSearchResultYearMonthListItem> updateResultList = new ArrayList<>();
                if (loadType == LoadType.ADD) {
                    for (WordSearchResultYearMonthListItem item : previousResultList) {
                        WordSearchResultYearMonthListItem cloneItem = item.clone();
                        updateResultList.add(cloneItem);
                    }
                    int updateResultListLastItemPosition = updateResultList.size() - 1;
                    WordSearchResultYearMonthListItem diaryYearMonthListItem =
                            updateResultList.get(updateResultListLastItemPosition);
                    if (diaryYearMonthListItem.getViewType() != DiaryYearMonthListAdapter.VIEW_TYPE_DIARY) {
                        updateResultList.remove(updateResultListLastItemPosition);
                    }
                }

                // 読込データを更新用日記リストへ追加
                if (!convertedLoadingData.isEmpty()) {
                    if (loadType == LoadType.ADD) {
                        // 前回の読込リストの最終アイテムの年月取得
                        int previousResultListProgressBar = previousResultList.size() - 1;
                        int previousResultListLastItemPosition = previousResultListProgressBar - 1;
                        WordSearchResultYearMonthListItem previousResultYearMonthListLastItem =
                                previousResultList.get(previousResultListLastItemPosition);
                        YearMonth previousResultYearMonthListLastItemYearMonth =
                                previousResultYearMonthListLastItem.getYearMonth();

                        // 今回の読込リストの先頭アイテムの年月取得
                        WordSearchResultYearMonthListItem additionalResultListFirstItem =
                                convertedLoadingData.get(0);
                        YearMonth additionalResultListFirstItemYearMonth =
                                additionalResultListFirstItem.getYearMonth();

                        // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                        // そこにDiaryDayListItemを足し込む
                        if (previousResultYearMonthListLastItemYearMonth
                                .equals(additionalResultListFirstItemYearMonth)) {
                            List<WordSearchResultDayListItem> additionalDiaryDayListItemList =
                                    additionalResultListFirstItem.getWordSearchResultDayList();
                            updateResultList.get(previousResultListLastItemPosition)
                                    .getWordSearchResultDayList().addAll(additionalDiaryDayListItemList);
                            convertedLoadingData.remove(0);
                        }
                    }
                    updateResultList.addAll(convertedLoadingData);
                }

                // 次回読み込む日記あり確認
                if (!updateResultList.isEmpty()) {
                    boolean existsUnloadedResults =
                            countDiaryListDayItem(updateResultList) < numWordSearchResults;
                    if (numWordSearchResults > 0 && !existsUnloadedResults) {
                        WordSearchResultYearMonthListItem noDiaryMessage =
                                new WordSearchResultYearMonthListItem(
                                        DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE
                                );
                        updateResultList.add(noDiaryMessage);
                    } else {
                        WordSearchResultYearMonthListItem noDiaryMessage =
                                new WordSearchResultYearMonthListItem(
                                        DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR
                                );
                        updateResultList.add(noDiaryMessage);
                    }
                }


                // 日記リスト読込完了処理
                WordSearchViewModel.this.numWordSearchResults.postValue(numWordSearchResults);
                wordSearchResultList.postValue(updateResultList);
            } catch (CancellationException e) {
                Log.d("WordSearchLoading","Exception");
                e.printStackTrace();
                // 例外処理なし

            } catch (ExecutionException e) {
                e.printStackTrace();
                wordSearchResultList.postValue(previousResultList);
                addAppError(AppError.DIARY_LOADING);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (!isValidityDelay) {
                    // TODO:ProgressBarを表示させる為に仮で記述
                    wordSearchResultList.postValue(previousResultList);
                    addAppError(AppError.DIARY_LOADING);
                }
            } finally {
                isVisibleUpdateProgressBar.postValue(false);
                Log.d("WordSearchLoading","run()_end");
                Log.d("WordSearchLoading","LoadType:" + loadType);
                Log.d("WordSearchLoading","SearchWord:" + searchWord);
                Log.d("WordSearchLoading","SpannableStringBackGroundColor:" + spannableStringBackGroundColor);
            }

        }
    }

    private int countDiaryListDayItem(List<WordSearchResultYearMonthListItem> diaryList) {
        int count = 0;
        for (WordSearchResultYearMonthListItem item: diaryList) {
            count += item.getWordSearchResultDayList().size();
        }
        return count;
    }

    private List<WordSearchResultYearMonthListItem> toWordSearchResultYearMonthListFormat(
            List<WordSearchResultListItem> beforeList,
            String searchWord, int spannableStringColor, int spannableStringBackGroundColor) {
        List<WordSearchResultDayListItem> wordSearchResultDayList =
                toWordSearchResultDayList(beforeList, searchWord,spannableStringColor, spannableStringBackGroundColor);
        return toWordSearchResultYearMonthList(wordSearchResultDayList);
    }

    private List<WordSearchResultDayListItem> toWordSearchResultDayList(
            List<WordSearchResultListItem> beforeList,
            String searchWord, int spannableStringColor, int spannableStringBackGroundColor) {
        List<WordSearchResultDayListItem> dayList = new ArrayList<>();
        for (WordSearchResultListItem item: beforeList) {
            String strDate = item.getDate();
            LocalDate date = LocalDate.parse(strDate);
            SpannableString title =
                    toSpannableString(
                            item.getTitle(), searchWord, spannableStringColor, spannableStringBackGroundColor);

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
            int itemNumber = 0;
            SpannableString itemTitle = new SpannableString("");
            SpannableString itemComment = new SpannableString("");
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
                    itemNumber = i + 1;
                    itemTitle = toSpannableString(
                            itemTitles[i], searchWord, spannableStringColor, spannableStringBackGroundColor);
                    itemComment = toSpannableString(
                            itemComments[i], searchWord, spannableStringColor, spannableStringBackGroundColor);
                    break;
                }
                if (i == (itemTitles.length - 1)) {
                    itemNumber = 1;
                    itemTitle = toSpannableString(
                            itemTitles[0], searchWord, spannableStringColor, spannableStringBackGroundColor);
                    itemComment = toSpannableString(
                            itemComments[0], searchWord, spannableStringColor, spannableStringBackGroundColor);
                }
            }

            WordSearchResultDayListItem dayListItem =
                    new WordSearchResultDayListItem(date, title, itemNumber, itemTitle, itemComment);
            dayList.add(dayListItem);
        }
        return dayList;
    }

    // 対象ワードをマーキング
    private SpannableString toSpannableString(String string, String targetWord, int textColor, int backgroundColor) {
        SpannableString spannableString = new SpannableString(string);
        int fromIndex = 0;
        while (string.indexOf(targetWord, fromIndex) != -1) {
            BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(backgroundColor);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(textColor);
            int start = string.indexOf(targetWord, fromIndex);
            int end = start + targetWord.length();
            spannableString.setSpan(
                    backgroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            spannableString.setSpan(
                    foregroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            fromIndex = end;
        }
        return spannableString;
    }

    private List<WordSearchResultYearMonthListItem> toWordSearchResultYearMonthList(
            List<WordSearchResultDayListItem> beforeList) {
        // 日記リストを月別に振り分ける
        final int VIEW_TYPE_DIARY = DiaryYearMonthListAdapter.VIEW_TYPE_DIARY;
        List<WordSearchResultDayListItem> sortingList= new ArrayList<>();
        List<WordSearchResultYearMonthListItem> wordSearchResultYearMonthList = new ArrayList<>();
        WordSearchResultYearMonthListItem  wordSearchResultMonthListItem;
        YearMonth sortingYearMonth = null;

        for (WordSearchResultDayListItem day: beforeList) {
            LocalDate date = day.getDate();
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());

            if (sortingYearMonth != null &&  !yearMonth.equals(sortingYearMonth)) {
                wordSearchResultMonthListItem =
                        new WordSearchResultYearMonthListItem(sortingYearMonth, sortingList, VIEW_TYPE_DIARY);
                wordSearchResultYearMonthList.add( wordSearchResultMonthListItem);
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYearMonth = yearMonth;
        }

        if (sortingYearMonth != null) {
            wordSearchResultMonthListItem =
                    new WordSearchResultYearMonthListItem(sortingYearMonth, sortingList, VIEW_TYPE_DIARY);
            wordSearchResultYearMonthList.add( wordSearchResultMonthListItem);
        } else {
            // TODO:assert
        }

        return wordSearchResultYearMonthList;
    }

    public void clearSearchWord() {
        searchWord.setValue("");
    }

    // LiveDataGetter
    // MEMO:単一データバインディングの場合、ゲッターの戻り値はLiveData<>にすること。
    //      双方向データバインディングの場合、ゲッターの戻り値はMutableLiveData<>にすること。
    public LiveData<String> getSearchWordLiveData() {
        return searchWord;
    }

    public MutableLiveData<String> getSearchWordMutableLiveData() {
        return searchWord;
    }

    public LiveData<List<WordSearchResultYearMonthListItem>> getWordSearchResultListLiveData() {
        return wordSearchResultList;
    }

    public LiveData<Integer> getNumWordSearchResults() {
        return numWordSearchResults;
    }

    public LiveData<Boolean> getIsVisibleUpdateProgressBarLiveData() {
        return isVisibleUpdateProgressBar;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

}
