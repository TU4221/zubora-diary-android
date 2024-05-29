package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class WordSearchViewModel extends AndroidViewModel {

    private WordSearchRepository wordSearchRepository;
    private MutableLiveData<String> searchWord = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleSearchWordClearButton = new MutableLiveData<>();
    private MutableLiveData<List<WordSearchResultListItemDiary>> loadedWordSearchResultList
            = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleNoResultMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVisibleResults = new MutableLiveData<>();
    private MutableLiveData<Integer> resultNum = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final int LOAD_ITEM_NUM = 15; // TODO:仮数値の為、最後に設定
    private int loadItemOffset;


    public WordSearchViewModel(@NonNull Application application) {
        super(application);
        wordSearchRepository = new WordSearchRepository(getApplication());
        initialize();
    }

    public void initialize() {
        this.searchWord.setValue("");
        this.isVisibleSearchWordClearButton.setValue(false);
        this.loadedWordSearchResultList.setValue(new ArrayList<>());
        this.isVisibleNoResultMessage.setValue(false);
        this.isVisibleResults.setValue(false);
        this.resultNum.setValue(0);
        this.isLoading.setValue(false);;
        this.loadItemOffset = 0;
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }
    public void loadWordSearchResultList(LoadType loadType) {
        Log.d("リスト読込確認", "起動");
        List<WordSearchResultListItemDiary> loadedList = new ArrayList<>();
        int loadItemNum;
        if (loadType == LoadType.NEW) {
            loadItemNum = this.LOAD_ITEM_NUM;
            this.loadItemOffset = 0;
            this.resultNum.setValue(
                    this.wordSearchRepository.countWordSearchResults(this.searchWord.getValue())
            );
        } else if(loadType == LoadType.UPDATE) {
            loadItemNum = this.loadItemOffset;
            this.loadItemOffset = 0;
            this.resultNum.setValue(
                    this.wordSearchRepository.countWordSearchResults(this.searchWord.getValue())
            );
        } else {
            loadItemNum = this.LOAD_ITEM_NUM;
            loadedList = this.loadedWordSearchResultList.getValue();
        }

        if (this.searchWord.getValue().isEmpty()) {
            loadedList.addAll(
                    this.wordSearchRepository.selectWordSearchResultList(
                            loadItemNum,
                            this.loadItemOffset,
                            null
                    )
            );
        } else {
            loadedList.addAll(
                    this.wordSearchRepository.selectWordSearchResultList(
                            loadItemNum,
                            this.loadItemOffset,
                            this.searchWord.getValue()
                    )
            );
        }

        this.loadItemOffset += loadItemNum;
        this.loadedWordSearchResultList.setValue(loadedList);
    }

    public void clearSearchWord() {
        this.searchWord.setValue("");
    }

    public void setIsVisibleSearchWordClearButton(boolean bool) {
        this.isVisibleSearchWordClearButton.setValue(bool);
    }

    public LiveData<List<WordSearchResultListItemDiary>> getLoadedWordSearchResultList() {
        return this.loadedWordSearchResultList;
    }

    public void setIsLoading(boolean bool) {
        this.isLoading.setValue(bool);
        Log.d("progressbar確認", String.valueOf(this.isLoading.getValue()));
    }

    public void prepareBeforeWordSearchShowing() {
        this.isVisibleNoResultMessage.setValue(false);
        this.isVisibleResults.setValue(false);
    }

    public void prepareWordSearchNoResultShowing() {
        this.isVisibleNoResultMessage.setValue(true);
        this.isVisibleResults.setValue(false);
    }

    public void prepareWordSearchResultShowing() {
        this.isVisibleNoResultMessage.setValue(false);
        this.isVisibleResults.setValue(true);
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

    public LiveData<Boolean> getIsVisibleNoResultMessage() {
        return this.isVisibleNoResultMessage;
    }

    public LiveData<Boolean> getIsVisibleResults() {
        return this.isVisibleResults;
    }

    public LiveData<Integer> getResultNum() {
        return this.resultNum;
    }

    public LiveData<Boolean> getIsLoading() {
        return this.isLoading;
    }

}
