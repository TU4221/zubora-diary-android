package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItemDiary;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.time.LocalDate;
import java.time.Year;
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

    private DiaryRepository diaryRepository;
    private MutableLiveData<String> searchWord = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleSearchWordClearButton = new MutableLiveData<>();
    private Future<?> LoadingWordSearchResultListFuture;
    private MutableLiveData<List<WordSearchResultYearMonthListItem>> wordSearchResultList =
            new MutableLiveData<>();
    private boolean isLoading;
    private MutableLiveData<Boolean> isVisibleNumWordSearchResults = new MutableLiveData<>();
    private MutableLiveData<Integer> numWordSearchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleResultList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleNoResultMessage = new MutableLiveData<>();
    private final int LOAD_ITEM_NUM = 10; // TODO:仮数値の為、最後に設定
    private ExecutorService executorService;


    @Inject
    public WordSearchViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    public void initialize() {
        this.searchWord.setValue("");
        this.isVisibleSearchWordClearButton.setValue(false);
        this.wordSearchResultList.setValue(new ArrayList<>());
        this.isVisibleNumWordSearchResults.setValue(false);
        this.numWordSearchResults.setValue(0);
        this.isVisibleResultList.setValue(false);
        this.isVisibleUpdateProgressBar.setValue(false);
        this.isVisibleNoResultMessage.setValue(false);
        this.isLoading = false;
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadWordSearchResultListAsync(LoadType loadType, String word, Runnable exceptionHandling){
        if (this.LoadingWordSearchResultListFuture != null && !this.LoadingWordSearchResultListFuture.isDone()) {
            LoadingWordSearchResultListFuture.cancel(true);
        }
        if (this.searchWord.getValue().isEmpty()) {
            setupVisibilityBeforeWordSearch();
            this.wordSearchResultList.setValue(new ArrayList<>());
            return;
        }
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
        List<WordSearchResultYearMonthListItem> currentList = this.wordSearchResultList.getValue();
        Runnable loadWordSearchResultList =
                new loadWordSearchResultList(loadType, word, handler, exceptionHandling);
        this.LoadingWordSearchResultListFuture = this.executorService.submit(loadWordSearchResultList);
    }

    private class loadWordSearchResultList implements Runnable {
        LoadType loadType;
        String searchWord;
        Handler handler;
        Runnable exceptionHandling;
        public loadWordSearchResultList(
                LoadType loadType, String searchWord, Handler handler, Runnable exceptionHandling) {
            this.loadType = loadType;
            this.searchWord = searchWord;
            this.handler = handler;
            this.exceptionHandling = exceptionHandling;
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
                    loadingOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    numLoadingItems = LOAD_ITEM_NUM;
                    if (currentResultList == null || currentResultList.isEmpty()) {
                        // TODO:assert
                        return;
                    } else {
                        loadingOffset = countDiaryListDayItem(currentResultList);
                    }
                } else {
                    // LoadType.NEW
                    numLoadingItems = LOAD_ITEM_NUM;
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
                Thread.sleep(2000);

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
                    //
                    // loadType == LoadType.ADD
                    numWordSearchResults = WordSearchViewModel.this.numWordSearchResults.getValue();
                }
                ListenableFuture<List<WordSearchResultListItemDiary>> listenableFutureResults =
                        diaryRepository.selectWordSearchResultList(
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
                List<WordSearchResultListItemDiary> loadingData = listenableFutureResults.get();
                if (!loadingData.isEmpty()) {
                    convertedLoadingData = toWordSearchResultYearMonthListFormat(loadingData, searchWord);
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
                                == additionalResultListFirstItemYearMonth) {
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
                handler.post(exceptionHandling);
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
            List<WordSearchResultListItemDiary> beforeList, String searchWord) {
        List<WordSearchResultDayListItem> dayList = new ArrayList<>();
        for (WordSearchResultListItemDiary item: beforeList) {
            String strDate = item.getDate();
            LocalDate date = LocalDate.parse(strDate);
            SpannableString title = createSpannableString(item.getTitle(), searchWord);

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
                    itemTitle = createSpannableString(itemTitles[i], searchWord);
                    itemComment = createSpannableString(itemComments[i], searchWord);
                    break;
                }
                if (i == (itemTitles.length - 1)) {
                    itemNumber = 1;
                    itemTitle = createSpannableString(itemTitles[0], searchWord);
                    itemComment = createSpannableString(itemComments[0], searchWord);
                }
            }

            WordSearchResultDayListItem dayListItem =
                    new WordSearchResultDayListItem(date, title, itemNumber, itemTitle, itemComment);
            dayList.add(dayListItem);
        }

        // 日記リストを月別に振り分ける
        final int VIEW_TYPE_DIARY = DiaryYearMonthListAdapter.VIEW_TYPE_DIARY;
        List<WordSearchResultDayListItem> sortingList= new ArrayList<>();
        List<WordSearchResultYearMonthListItem> wordSearchResultYearMonthList = new ArrayList<>();
        WordSearchResultYearMonthListItem  wordSearchResultMonthListItem;
        YearMonth sortingYearMonth = null;

        for (WordSearchResultDayListItem day: dayList) {
            LocalDate date = day.getDate();
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
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

    // 対象ワードをマーキング
    private SpannableString createSpannableString(String string, String targetWord) {
        SpannableString spannableString = new SpannableString(string);
        BackgroundColorSpan backgroundColorSpan =
                new BackgroundColorSpan(
                        context.getResources().getColor(R.color.gray)
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

    private void setupVisibilityBeforeWordSearch() {
        this.isVisibleNumWordSearchResults.setValue(false);
        this.isVisibleResultList.setValue(false);
        this.isVisibleUpdateProgressBar.setValue(false);
        this.isVisibleNoResultMessage.setValue(false);
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
        this.searchWord.setValue("");
    }

    public void setIsVisibleSearchWordClearButton(boolean bool) {
        this.isVisibleSearchWordClearButton.setValue(bool);
    }

    public LiveData<List<WordSearchResultYearMonthListItem>> getLiveDataWordSearchResultList() {
        return this.wordSearchResultList;
    }

    public boolean getIsLoading() {
        return this.isLoading;
    }

    public void setIsLoading(boolean bool) {
        this.isLoading = bool;
    }

    // 単一・双方向データバインディング用メソッド
    // MEMO:単一の場合、ゲッターの戻り値はLiveData<>にすること。
    //      双方向の場合、ゲッターの戻り値はMutableLiveData<>にすること。
    public MutableLiveData<String> getSearchWord() {
        return this.searchWord;
    }

    public LiveData<Boolean> getIsVisibleSearchWordClearButton() {
        return this.isVisibleSearchWordClearButton;
    }

    public LiveData<Boolean> getIsVisibleNumWordSearchResults() {
        return this.isVisibleNumWordSearchResults;
    }

    public LiveData<Boolean> getIsVisibleResultList() {
        return this.isVisibleResultList;
    }

    public LiveData<Boolean> getIsVisibleUpdateProgressBar() {
        return this.isVisibleUpdateProgressBar;
    }
    public LiveData<Boolean> getIsVisibleNoResultMessage() {
        return this.isVisibleNoResultMessage;
    }

    public LiveData<Integer> getNumWordSearchResults() {
        return this.numWordSearchResults;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.executorService.shutdown();
    }

}
