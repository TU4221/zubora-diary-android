package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WordSearchViewModel extends ViewModel {

    // TODO:Visible変数を削除してFragment上で制御できるか検討
    private final DiaryRepository diaryRepository;
    private final MutableLiveData<String> searchWord = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleSearchWordClearButton = new MutableLiveData<>();
    private Future<?> LoadingWordSearchResultListFuture; // キャンセル用
    private final MutableLiveData<List<WordSearchResultYearMonthListItem>> wordSearchResultList =
            new MutableLiveData<>();
    private boolean isLoading;
    private final MutableLiveData<Boolean> isVisibleNumWordSearchResults = new MutableLiveData<>();
    private final MutableLiveData<Integer> numWordSearchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleResultList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVisibleNoResultMessage = new MutableLiveData<>();
    private static final int NUM_LOADING_ITEMS = 10; //リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    private final ExecutorService executorService;

    // エラー関係
    private final MutableLiveData<Boolean> isDiaryListLoadingError = new MutableLiveData<>();


    @Inject
    public WordSearchViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    public void initialize() {
        searchWord.setValue("");
        isVisibleSearchWordClearButton.setValue(false);
        wordSearchResultList.setValue(new ArrayList<>());
        isVisibleNumWordSearchResults.setValue(false);
        numWordSearchResults.setValue(0);
        isVisibleResultList.setValue(false);
        isVisibleUpdateProgressBar.setValue(false);
        isVisibleNoResultMessage.setValue(false);
        isLoading = false;
        isDiaryListLoadingError.setValue(false);
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadWordSearchResultList(
            LoadType loadType, int spannableStringBackGroundColor){
        if (LoadingWordSearchResultListFuture != null && !LoadingWordSearchResultListFuture.isDone()) {
            LoadingWordSearchResultListFuture.cancel(true);
        }
        String searchWord = this.searchWord.getValue();
        if (searchWord == null || searchWord.isEmpty()) {
            setupVisibilityBeforeWordSearch();
            wordSearchResultList.setValue(new ArrayList<>());
            return;
        }
        Runnable loadWordSearchResultList =
                new loadWordSearchResultList(loadType, searchWord, spannableStringBackGroundColor);
        LoadingWordSearchResultListFuture = executorService.submit(loadWordSearchResultList);
    }

    private class loadWordSearchResultList implements Runnable {
        LoadType loadType;
        String searchWord;
        int spannableStringBackGroundColor;

        public loadWordSearchResultList(
                LoadType loadType, String searchWord, int spannableStringBackGroundColor) {
            // TODO:非同期処理中に値が変わらないようにしたい
            this.loadType = loadType;
            this.searchWord = searchWord;
            this.spannableStringBackGroundColor = spannableStringBackGroundColor;
        }
        @Override
        public void run() {
            List<WordSearchResultYearMonthListItem> previousResultList = new ArrayList<>();
            try {
                // 日記リスト読込準備
                List<WordSearchResultYearMonthListItem> currentResultList =
                                                                wordSearchResultList.getValue();
                isLoading = true;
                setupVisibilityBeforeLoadingSearchResultListAsync(loadType);
                int numLoadingItems;
                int loadingOffset;
                if (loadType == LoadType.UPDATE) {
                    if (currentResultList == null || currentResultList.isEmpty()) {
                        return;
                    }
                    numLoadingItems = countDiaryListDayItem(currentResultList);
                    if (numLoadingItems < NUM_LOADING_ITEMS) {
                        numLoadingItems = NUM_LOADING_ITEMS;
                    }
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    numLoadingItems = NUM_LOADING_ITEMS;
                    if (currentResultList == null || currentResultList.isEmpty()) {
                        // TODO:assert
                        return;
                    } else {
                        loadingOffset = countDiaryListDayItem(currentResultList);
                    }
                } else {
                    // LoadType.NEW
                    numLoadingItems = NUM_LOADING_ITEMS;
                    loadingOffset = 0;
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
                if (loadType != LoadType.NEW) {
                    resultListContainingProgressBar.addAll(previousResultList);
                }
                if (loadType != LoadType.UPDATE) {
                    WordSearchResultYearMonthListItem progressBar =
                            new WordSearchResultYearMonthListItem(
                                    DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR
                            );
                    resultListContainingProgressBar.add(progressBar);
                }
                wordSearchResultList.postValue(resultListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                Thread.sleep(1000);

                // 日記リスト読込
                Integer numWordSearchResults;
                List<WordSearchResultYearMonthListItem> convertedLoadingData = new ArrayList<>();
                // TODO:下記if条件を廃止して、DiaryListと同様に毎回カウントするか検討
                if (loadType == LoadType.NEW || loadType == LoadType.UPDATE) {
                    ListenableFuture<Integer> listenableFutureResult =
                            diaryRepository.countWordSearchResults(searchWord);
                    // 検索文字が変更された時、カウントキャンセル
                    // TODO:下記while意味ある？
                    while (!listenableFutureResult.isDone()) {
                        if (Thread.currentThread().isInterrupted()) {
                            listenableFutureResult.cancel(true);
                            throw new InterruptedException();
                        }
                    }
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

                // 検索文字が変更された時、リスト読込キャンセル
                // TODO:下記while意味ある？
                while (!listenableFutureResults.isDone()) {
                    if (Thread.currentThread().isInterrupted()) {
                        listenableFutureResults.cancel(true);
                        throw new InterruptedException();
                    }
                }
                List<WordSearchResultListItem> loadingData = listenableFutureResults.get();
                if (!loadingData.isEmpty()) {
                    convertedLoadingData = toWordSearchResultYearMonthListFormat(loadingData, searchWord, spannableStringBackGroundColor);
                }


                // 更新用日記リスト準備
                List<WordSearchResultYearMonthListItem> updateResultList = new ArrayList<>();
                if (loadType == LoadType.ADD) {
                    for (WordSearchResultYearMonthListItem item : previousResultList) {
                        WordSearchResultYearMonthListItem cloneItem = item.clone();
                        updateResultList.add(cloneItem);
                    }
                }

                // 読込データを更新用日記リストへ追加
                if (!convertedLoadingData.isEmpty()) {
                    if (loadType == LoadType.ADD) {
                        // 前回の読込リストの最終アイテムの年月取得
                        int previousResultListLastItemPosition = previousResultList.size() - 1;
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
                boolean existsUnloadedResults =
                        countDiaryListDayItem(updateResultList) < numWordSearchResults;
                if (numWordSearchResults > 0 && !existsUnloadedResults) {
                    WordSearchResultYearMonthListItem noDiaryMessage =
                            new WordSearchResultYearMonthListItem(
                                    DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE
                            );
                    updateResultList.add(noDiaryMessage);
                }

                // 日記リスト読込完了処理
                setupVisibilityAfterLoadingSearchResultListAsync(updateResultList.isEmpty());
                WordSearchViewModel.this.numWordSearchResults.postValue(numWordSearchResults);
                wordSearchResultList.postValue(updateResultList);
            } catch (Exception e) {
                e.printStackTrace();
                wordSearchResultList.postValue(previousResultList);
                isDiaryListLoadingError.postValue(true);
            } finally {
                isVisibleUpdateProgressBar.postValue(false);
                isLoading = false;
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
            String searchWord, int spannableStringBackGroundColor) {
        List<WordSearchResultDayListItem> wordSearchResultDayList =
                toWordSearchResultDayList(beforeList, searchWord, spannableStringBackGroundColor);
        return toWordSearchResultYearMonthList(wordSearchResultDayList);
    }

    private List<WordSearchResultDayListItem> toWordSearchResultDayList(
            List<WordSearchResultListItem> beforeList,
            String searchWord, int spannableStringBackGroundColor) {
        List<WordSearchResultDayListItem> dayList = new ArrayList<>();
        for (WordSearchResultListItem item: beforeList) {
            String strDate = item.getDate();
            LocalDate date = LocalDate.parse(strDate);
            SpannableString title =
                    toSpannableString(item.getTitle(), searchWord, spannableStringBackGroundColor);

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
                    itemTitle = toSpannableString(itemTitles[i], searchWord, spannableStringBackGroundColor);
                    itemComment = toSpannableString(itemComments[i], searchWord, spannableStringBackGroundColor);
                    break;
                }
                if (i == (itemTitles.length - 1)) {
                    itemNumber = 1;
                    itemTitle = toSpannableString(itemTitles[0], searchWord, spannableStringBackGroundColor);
                    itemComment = toSpannableString(itemComments[0], searchWord, spannableStringBackGroundColor);
                }
            }

            WordSearchResultDayListItem dayListItem =
                    new WordSearchResultDayListItem(date, title, itemNumber, itemTitle, itemComment);
            dayList.add(dayListItem);
        }
        return dayList;
    }

    // 対象ワードをマーキング
    private SpannableString toSpannableString(String string, String targetWord, int backgroundColor) {
        SpannableString spannableString = new SpannableString(string);
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(backgroundColor);
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

    private void setupVisibilityBeforeWordSearch() {
        isVisibleNumWordSearchResults.setValue(false);
        isVisibleResultList.setValue(false);
        isVisibleUpdateProgressBar.setValue(false);
        isVisibleNoResultMessage.setValue(false);
    }

    private void setupVisibilityBeforeLoadingSearchResultListAsync(LoadType loadType) {
        if (loadType == LoadType.UPDATE) {
            isVisibleNumWordSearchResults.postValue(true);
            isVisibleResultList.postValue(true);
            isVisibleUpdateProgressBar.postValue(true);
            isVisibleNoResultMessage.postValue(false);
        } else if (loadType == LoadType.ADD) {
            isVisibleNumWordSearchResults.postValue(true);
            isVisibleResultList.postValue(true);
            isVisibleUpdateProgressBar.postValue(false);
            isVisibleNoResultMessage.postValue(false);
        } else {
            // LoadType.NEW
            isVisibleNumWordSearchResults.postValue(false);
            isVisibleResultList.postValue(true);
            isVisibleUpdateProgressBar.postValue(false);
            isVisibleNoResultMessage.postValue(false);
        }
    }

    private void setupVisibilityAfterLoadingSearchResultListAsync(Boolean resultListIsEmpty) {
        if (resultListIsEmpty) {
            isVisibleNumWordSearchResults.postValue(false);
            isVisibleResultList.postValue(false);
            isVisibleUpdateProgressBar.postValue(false);
            isVisibleNoResultMessage.postValue(true);
        } else {
            isVisibleNumWordSearchResults.postValue(true);
            isVisibleResultList.postValue(true);
            isVisibleUpdateProgressBar.postValue(false);
            isVisibleNoResultMessage.postValue(false);
        }
    }

    public void clearSearchWord() {
        searchWord.setValue("");
    }

    public void setIsVisibleSearchWordClearButton(boolean bool) {
        isVisibleSearchWordClearButton.setValue(bool);
    }

    // エラー関係
    public void clearIsDiaryListLoadingError() {
        isDiaryListLoadingError.setValue(false);
    }

    // Getter
    public boolean getIsLoading() {
        return isLoading;
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

    public LiveData<Boolean> getIsVisibleSearchWordClearButtonLiveData() {
        return isVisibleSearchWordClearButton;
    }

    public LiveData<List<WordSearchResultYearMonthListItem>> getWordSearchResultListLiveData() {
        return wordSearchResultList;
    }

    public LiveData<Boolean> getIsVisibleNumWordSearchResultsLiveData() {
        return isVisibleNumWordSearchResults;
    }

    public LiveData<Integer> getNumWordSearchResults() {
        return numWordSearchResults;
    }

    public LiveData<Boolean> getIsVisibleResultListLiveData() {
        return isVisibleResultList;
    }

    public LiveData<Boolean> getIsVisibleUpdateProgressBarLiveData() {
        return isVisibleUpdateProgressBar;
    }
    public LiveData<Boolean> getIsVisibleNoResultMessageLiveData() {
        return isVisibleNoResultMessage;
    }

    public LiveData<Boolean> getIsDiaryListLoadingErrorLiveData() {
        return isDiaryListLoadingError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

}
