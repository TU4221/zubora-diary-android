package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

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
            // 日記リスト読込準備
            WordSearchViewModel.this.isLoading = true;
            setupVisibilityBeforeLoadingSearchResultListAsync(loadType);
            int numLoadingItems;
            int loadingOffset;
            if (loadType == LoadType.UPDATE) {
                numLoadingItems =
                        countDiaryListDayItem(WordSearchViewModel.this.wordSearchResultList.getValue());
                loadingOffset = 0;
            } else if (loadType == LoadType.ADD) {
                numLoadingItems = WordSearchViewModel.this.LOAD_ITEM_NUM;
                loadingOffset =
                        countDiaryListDayItem(WordSearchViewModel.this.wordSearchResultList.getValue());
            } else {
                // LoadType.NEW
                numLoadingItems = WordSearchViewModel.this.LOAD_ITEM_NUM;
                loadingOffset = 0;
            }

            // 現時点のDiaryListをCloneで生成
            List<WordSearchResultYearMonthListItem> currentResultList =
                    WordSearchViewModel.this.wordSearchResultList.getValue();
            List<WordSearchResultYearMonthListItem> previousResultList = new ArrayList<>();
            if (loadType != LoadType.NEW) {
                for (WordSearchResultYearMonthListItem item : currentResultList) {
                    WordSearchResultYearMonthListItem cloneItem = item.clone();
                    previousResultList.add(cloneItem);
                }
            }

            // ProgressBar表示
            List<WordSearchResultYearMonthListItem> resultListContainingProgressBar = new ArrayList<>();
            if (loadType != LoadType.NEW) {
                resultListContainingProgressBar.addAll(previousResultList);
            }
            if (loadType != LoadType.UPDATE) {
                WordSearchResultYearMonthListItem progressBar =
                        new WordSearchResultYearMonthListItem();
                progressBar.setViewType(
                        WordSearchFragment.WordSearchResultYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                resultListContainingProgressBar.add(progressBar);
            }
            WordSearchViewModel.this.wordSearchResultList.postValue(resultListContainingProgressBar);

            // TODO:ProgressBarを表示させる為に仮で記述
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                WordSearchViewModel.this.wordSearchResultList.postValue(previousResultList);
                throw new RuntimeException(e);
            }

            // 日記リスト読込
            int numWordSearchResults;
            List<WordSearchResultYearMonthListItem> loadedData;
            try {
                if (loadType == LoadType.NEW || loadType == LoadType.UPDATE) {
                    numWordSearchResults =
                            WordSearchViewModel.this.diaryRepository.countWordSearchResults(searchWord);
                } else {
                    // loadType == LoadType.ADD
                    numWordSearchResults =
                            WordSearchViewModel.this.numWordSearchResults.getValue();
                }
                loadedData =
                        WordSearchViewModel.this.diaryRepository.selectWordSearchResultList(
                                numLoadingItems,
                                loadingOffset,
                                searchWord
                        );
            } catch (InterruptedException e) {
                WordSearchViewModel.this.wordSearchResultList.postValue(previousResultList);
                return;
            } catch (Exception e) {
                WordSearchViewModel.this.wordSearchResultList.postValue(previousResultList);
                WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                WordSearchViewModel.this.isLoading = false;
                handler.post(exceptionHandling);
                return;
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
            if (!loadedData.isEmpty()) {
                if (loadType == LoadType.ADD) {
                    // 前回の読込リストの最終アイテムの年月取得
                    int previousResultListLastItemPosition = previousResultList.size() - 1;
                    WordSearchResultYearMonthListItem previousResultYearMonthListLastItem =
                            previousResultList.get(previousResultListLastItemPosition);
                    int previousResultYearMonthListLastItemYear =
                            previousResultYearMonthListLastItem.getYear();
                    int previousResultYearMonthListLastItemMonth =
                            previousResultYearMonthListLastItem.getMonth();

                    // 今回の読込リストの先頭アイテムの年月取得
                    WordSearchResultYearMonthListItem additionalResultListFirstItem =
                            loadedData.get(0);
                    int additionalResultListFirstItemYear =
                            additionalResultListFirstItem.getYear();
                    int additionalResultListFirstItemMonth =
                            additionalResultListFirstItem.getMonth();

                    // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                    // そこにDiaryDayListItemを足し込む
                    if (previousResultYearMonthListLastItemYear == additionalResultListFirstItemYear
                            && previousResultYearMonthListLastItemMonth == additionalResultListFirstItemMonth) {
                        List<WordSearchResultDayListItem> additionalDiaryDayListItemList =
                                additionalResultListFirstItem.getWordSearchResultDayList();
                        updateResultList.get(previousResultListLastItemPosition)
                                .getWordSearchResultDayList().addAll(additionalDiaryDayListItemList);
                        loadedData.remove(0);
                    }
                }
                updateResultList.addAll(loadedData);
            }

            // 次回読み込む日記あり確認
            boolean existsUnloadedResults =
                    countDiaryListDayItem(updateResultList) < numWordSearchResults;
            if (numWordSearchResults > 0 && !existsUnloadedResults) {
                WordSearchResultYearMonthListItem noDiaryMessage =
                        new WordSearchResultYearMonthListItem();
                noDiaryMessage.setViewType(
                        WordSearchFragment.WordSearchResultYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE);
                updateResultList.add(noDiaryMessage);
            }

            // 日記リスト読込完了処理
            setupVisibilityAfterLoadingSearchResultListAsync(updateResultList.isEmpty());
            WordSearchViewModel.this.numWordSearchResults.postValue(numWordSearchResults);
            WordSearchViewModel.this.wordSearchResultList.postValue(updateResultList);
            WordSearchViewModel.this.isLoading = false;
        }
    }

    private int countDiaryListDayItem(List<WordSearchResultYearMonthListItem> diaryList) {
        int count = 0;
        for (WordSearchResultYearMonthListItem item: diaryList) {
            count += item.getWordSearchResultDayList().size();
        }
        return count;
    }

    private void setupVisibilityBeforeWordSearch() {
        this.isVisibleNumWordSearchResults.setValue(false);
        this.isVisibleResultList.setValue(false);
        this.isVisibleUpdateProgressBar.setValue(false);
        this.isVisibleNoResultMessage.setValue(false);
    }

    private void setupVisibilityBeforeLoadingSearchResultListAsync(LoadType loadType) {
        if (loadType == LoadType.UPDATE) {
            WordSearchViewModel.this.isVisibleNumWordSearchResults.postValue(true);
            WordSearchViewModel.this.isVisibleResultList.postValue(true);
            WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(true);
            WordSearchViewModel.this.isVisibleNoResultMessage.postValue(false);
        } else if (loadType == LoadType.ADD) {
            WordSearchViewModel.this.isVisibleNumWordSearchResults.postValue(true);
            WordSearchViewModel.this.isVisibleResultList.postValue(true);
            WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
            WordSearchViewModel.this.isVisibleNoResultMessage.postValue(false);
        } else {
            // LoadType.NEW
            WordSearchViewModel.this.isVisibleNumWordSearchResults.postValue(false);
            WordSearchViewModel.this.isVisibleResultList.postValue(true);
            WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
            WordSearchViewModel.this.isVisibleNoResultMessage.postValue(false);
        }
    }

    private void setupVisibilityAfterLoadingSearchResultListAsync(Boolean resultListIsEmpty) {
        if (resultListIsEmpty) {
            WordSearchViewModel.this.isVisibleNumWordSearchResults.postValue(false);
            WordSearchViewModel.this.isVisibleResultList.postValue(false);
            WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
            WordSearchViewModel.this.isVisibleNoResultMessage.postValue(true);
        } else {
            WordSearchViewModel.this.isVisibleNumWordSearchResults.postValue(true);
            WordSearchViewModel.this.isVisibleResultList.postValue(true);
            WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
            WordSearchViewModel.this.isVisibleNoResultMessage.postValue(false);
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
