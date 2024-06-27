package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.ui.diary.DiaryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordSearchViewModel extends AndroidViewModel {

    private DiaryRepository DiaryRepository;
    private MutableLiveData<String> searchWord = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleSearchWordClearButton = new MutableLiveData<>();
    private MutableLiveData<List<WordSearchResultYearMonthListItem>> wordSearchResultList =
            new MutableLiveData<>();
    private boolean isLoading;
    private MutableLiveData<Boolean> isVisibleNumWordSearchResults = new MutableLiveData<>();
    private MutableLiveData<Integer> numWordSearchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleResultList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleNoResultMessage = new MutableLiveData<>();
    private final int LOAD_ITEM_NUM = 15; // TODO:仮数値の為、最後に設定
    private ExecutorService executorService;


    public WordSearchViewModel(@NonNull Application application) {
        super(application);
        DiaryRepository = new DiaryRepository(getApplication());
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

    // TODO:処理内容確認(調整時のコード、コメントが残っている)
    public void loadWordSearchResultList(LoadType loadType, Runnable runnable){
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // 日記リスト読込準備
                WordSearchViewModel.this.isLoading = true;

                // 読込前Visible設定
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


                String searchWord = WordSearchViewModel.this.searchWord.getValue(); // TODO:Runnableクラスの引数として受け取ったほうが良いかも(LoadType,etc...)
                int numLoadingItems;
                int loadingOffset;
                List<WordSearchResultListItemDiary> loadedList = new ArrayList<>();
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
                if (loadType == LoadType.UPDATE) {
                    WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(true);
                } else {
                    WordSearchResultYearMonthListItem progressBar =
                            new WordSearchResultYearMonthListItem();
                    progressBar.setViewType(
                            WordSearchFragment.WordSearchResultYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                    resultListContainingProgressBar.add(progressBar);
                }
                WordSearchViewModel.this.wordSearchResultList.postValue(resultListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


                // 日記リスト読込
                int numWordSearchResults = 0;
                List<WordSearchResultYearMonthListItem> loadedData = new ArrayList<>();
                try {
                    if (loadType == LoadType.NEW || loadType == LoadType.UPDATE) {
                        numWordSearchResults =
                                WordSearchViewModel.this.DiaryRepository.countWordSearchResults(searchWord);
                    }
                    if (searchWord.equals("")) {
                        loadedData =
                                WordSearchViewModel.this.DiaryRepository.selectWordSearchResultList(
                                        numLoadingItems,
                                        loadingOffset,
                                        null
                                );
                    } else {
                        loadedData =
                                WordSearchViewModel.this.DiaryRepository.selectWordSearchResultList(
                                        numLoadingItems,
                                        loadingOffset,
                                        searchWord
                                );
                    }
                } catch (Exception e) {
                    WordSearchViewModel.this.wordSearchResultList.postValue(previousResultList);
                    WordSearchViewModel.this.isVisibleUpdateProgressBar.postValue(false);
                    WordSearchViewModel.this.isLoading = false;
                    handler.post(runnable);
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
                if (updateResultList.isEmpty()) {
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
                WordSearchViewModel.this.numWordSearchResults.postValue(numWordSearchResults);
                WordSearchViewModel.this.wordSearchResultList.postValue(updateResultList);
                WordSearchViewModel.this.isLoading = false;

                //////////////////////////////////////////////////////

                /*ListenableFuture<List<WordSearchResultListItemDiary>> listListenableFuture = null;
                if (this.searchWord.getValue().isEmpty()) {
            *//*loadedList.addAll(
                    this.wordSearchRepository.selectWordSearchResultList(
                            loadItemNum,
                            this.loadItemOffset,
                            null
                    )
            );*//*
                    listListenableFuture =
                            this.DiaryRepository.selectWordSearchResultList(
                                    numLoadingItems,
                                    this.loadItemOffset,
                                    null);
                } else {
            *//*loadedList.addAll(
                    this.wordSearchRepository.selectWordSearchResultList(
                            loadItemNum,
                            this.loadItemOffset,
                            this.searchWord.getValue()
                    )
            );*//*
                    listListenableFuture =
                            this.DiaryRepository.selectWordSearchResultList(
                                    numLoadingItems,
                                    this.loadItemOffset,
                                    this.searchWord.getValue());
                }

                //this.loadItemOffset += loadItemNum;
                //this.loadedWordSearchResultList.setValue(loadedList);
                Futures.addCallback(
                        listListenableFuture,
                        new FutureCallback<List<WordSearchResultListItemDiary>>() {
                            @Override
                            public void onSuccess(List<WordSearchResultListItemDiary> result) {
                                for (int i = 0; i < 1000000000;) {
                                    i++;
                                }
                                WordSearchViewModel.this.loadItemOffset += numLoadingItems;
                                Log.d("20240611", "setValue");
                                WordSearchViewModel.this.loadedWordSearchResultList.setValue(result);
                            }

                            @Override
                            public void onFailure(Throwable t) {

                            }
                        },
                        getApplication().getMainExecutor());*/
            }
        });


    }

    private int countDiaryListDayItem(List<WordSearchResultYearMonthListItem> diaryList) {
        int count = 0;
        for (WordSearchResultYearMonthListItem item: diaryList) {
            count += item.getWordSearchResultDayList().size();
        }
        return count;
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

    public void prepareBeforeWordSearchShowing() {
        this.isVisibleNoResultMessage.setValue(false);
        this.isVisibleResultList.setValue(false);
    }

    public void prepareWordSearchNoResultShowing() {
        this.isVisibleNoResultMessage.setValue(true);
        this.isVisibleResultList.setValue(false);
    }

    public void prepareWordSearchResultShowing() {
        this.isVisibleNoResultMessage.setValue(false);
        this.isVisibleResultList.setValue(true);
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

}
