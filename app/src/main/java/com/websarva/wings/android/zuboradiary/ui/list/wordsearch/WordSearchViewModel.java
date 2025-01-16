package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final MutableLiveData<WordSearchResultYearMonthList> wordSearchResultList = new MutableLiveData<>();
    private final MutableLiveData<Integer> numWordSearchResults = new MutableLiveData<>();

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    private static final int NUM_LOADING_ITEMS = 10; //リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    private final ExecutorService executorService;

    private final boolean isValidityDelay = true;// TODO:調整用

    @Inject
    WordSearchViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    @Override
    public void initialize() {
        initializeAppMessageList();
        searchWord.setValue("");
        wordSearchResultList.setValue(new WordSearchResultYearMonthList());
        numWordSearchResults.setValue(0);
        isVisibleUpdateProgressBar.setValue(false);
        cancelPreviousLoading();
        wordSearchResultListLoadingFuture = null;
    }

    boolean canLoadWordSearchResultList() {
        Log.d("OnScrollDiaryList", "isLoadingDiaryList()");
        if (wordSearchResultListLoadingFuture == null) {
            Log.d("OnScrollDiaryList", "wordSearchResultListLoadingFuture == null");
            return true;
        }
        return wordSearchResultListLoadingFuture.isDone();
    }

    void loadNewWordSearchResultList(int spannableStringColor, int spannableStringBackgroundColor) {
        loadWordSearchResultDiaryList(
                new NewWordSearchResultListCreator(), spannableStringColor, spannableStringBackgroundColor);
    }

    void loadAdditionWordSearchResultList(int spannableStringColor, int spannableStringBackgroundColor) {
        loadWordSearchResultDiaryList(
                new AddedWordSearchResultListCreator(), spannableStringColor, spannableStringBackgroundColor);
    }

    void updateWordSearchResultList(int spannableStringColor, int spannableStringBackgroundColor) {
        loadWordSearchResultDiaryList(
                new UpdateWordSearchResultListCreator(), spannableStringColor, spannableStringBackgroundColor);
    }

    private void loadWordSearchResultDiaryList(
            WordSearchResultListCreator creator, int spannableStringColor, int spannableStringBackgroundColor){
        Objects.requireNonNull(creator);

        cancelPreviousLoading();
        Runnable loadWordSearchResultList =
                new WordSearchResultListLoadingRunnable(
                        creator, spannableStringColor, spannableStringBackgroundColor);
        wordSearchResultListLoadingFuture = executorService.submit(loadWordSearchResultList);
    }

    private void cancelPreviousLoading() {
        if (!canLoadWordSearchResultList()) {
            Log.d("WordSearchLoading","Cancel");
            wordSearchResultListLoadingFuture.cancel(true);
        }
    }

    private interface WordSearchResultListCreator {

        @NonNull
        WordSearchResultYearMonthList create(int spannableStringColor, int spannableStringBackGroundColor)
                throws CancellationException, ExecutionException, InterruptedException;
    }

    private class WordSearchResultListLoadingRunnable implements Runnable {

        private final WordSearchResultListCreator resultListCreator;
        private final int spannableStringColor;
        private final int spannableStringBackGroundColor;

        private WordSearchResultListLoadingRunnable(
                WordSearchResultListCreator resultListCreator,
                int spannableStringColor, int spannableStringBackGroundColor) {
            Objects.requireNonNull(resultListCreator);

            this.resultListCreator = resultListCreator;
            this.spannableStringColor = spannableStringColor;
            this.spannableStringBackGroundColor = spannableStringBackGroundColor;
        }
        @Override
        public void run() {
            Log.d("WordSearchLoading", "run()_start");
            WordSearchResultYearMonthList previousResultList = wordSearchResultList.getValue();
            Objects.requireNonNull(previousResultList);

            try {
                WordSearchResultYearMonthList updateResultList =
                        resultListCreator.create(spannableStringColor, spannableStringBackGroundColor);
                wordSearchResultList.postValue(updateResultList);
            } catch (CancellationException e) {
                Log.d("WordSearchLoading","Exception");
                e.printStackTrace();
                // 例外処理なし

            } catch (ExecutionException e) {
                e.printStackTrace();
                wordSearchResultList.postValue(previousResultList);
                addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (!isValidityDelay) {
                    wordSearchResultList.postValue(previousResultList);
                    addAppMessage(AppMessage.DIARY_LOADING_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                wordSearchResultList.postValue(previousResultList);
                addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            }
        }
    }

    private class NewWordSearchResultListCreator implements WordSearchResultListCreator {

        @Override
        @NonNull
        public WordSearchResultYearMonthList create(int spannableStringColor, int spannableStringBackGroundColor)
                throws CancellationException, ExecutionException, InterruptedException {
            showWordSearchResultListFirstItemProgressIndicator();
            if (isValidityDelay) Thread.sleep(1000);
            return loadWordSearchResultDiaryList(
                    NUM_LOADING_ITEMS, 0, spannableStringColor, spannableStringBackGroundColor);
        }

        private void showWordSearchResultListFirstItemProgressIndicator() {
            WordSearchResultYearMonthList list =
                    new WordSearchResultYearMonthList(false);
            wordSearchResultList.postValue(list);
            numWordSearchResults.postValue(0);
        }
    }

    private class AddedWordSearchResultListCreator implements WordSearchResultListCreator {

        @Override
        @NonNull
        public WordSearchResultYearMonthList create(int spannableStringColor, int spannableStringBackGroundColor)
                throws CancellationException, ExecutionException, InterruptedException {
            WordSearchResultYearMonthList currentResultList = wordSearchResultList.getValue();
            Objects.requireNonNull(currentResultList);
            if (currentResultList.getWordSearchResultYearMonthListItemList().isEmpty()) throw new IllegalStateException();

            if (isValidityDelay) Thread.sleep(1000);
            int loadingOffset = currentResultList.countDiaries();
            WordSearchResultYearMonthList loadedResultList =
                    loadWordSearchResultDiaryList(
                            NUM_LOADING_ITEMS, loadingOffset, spannableStringColor, spannableStringBackGroundColor);
            int numLoadedDiaries = currentResultList.countDiaries() + loadedResultList.countDiaries();
            boolean existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries);
            return currentResultList.combineDiaryLists(loadedResultList, !existsUnloadedDiaries);
        }
    }

    private class UpdateWordSearchResultListCreator implements WordSearchResultListCreator {

        @Override
        @NonNull
        public WordSearchResultYearMonthList create(int spannableStringColor, int spannableStringBackGroundColor)
                throws CancellationException, ExecutionException, InterruptedException {
            WordSearchResultYearMonthList currentResultList = wordSearchResultList.getValue();
            Objects.requireNonNull(currentResultList);
            if (currentResultList.getWordSearchResultYearMonthListItemList().isEmpty()) throw new IllegalStateException();

            isVisibleUpdateProgressBar.postValue(true);
            try {
                if (isValidityDelay) Thread.sleep(3000);
                int numLoadingItems = currentResultList.countDiaries();
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < NUM_LOADING_ITEMS) {
                    numLoadingItems = NUM_LOADING_ITEMS;
                }
                return loadWordSearchResultDiaryList(
                        numLoadingItems, 0, spannableStringColor, spannableStringBackGroundColor);
            } finally {
                isVisibleUpdateProgressBar.postValue(false);
            }
        }
    }

    @NonNull
    private WordSearchResultYearMonthList loadWordSearchResultDiaryList(
            int numLoadingItems, int loadingOffset, int spannableStringColor, int spannableStringBackGroundColor)
            throws CancellationException, ExecutionException, InterruptedException {
        if (numLoadingItems <= 0) throw new IllegalArgumentException();
        if (loadingOffset < 0) throw new IllegalArgumentException();

        String searchWord = this.searchWord.getValue();
        Objects.requireNonNull(searchWord);

        ListenableFuture<List<WordSearchResultListItem>> listenableFutureResults =
                diaryRepository.loadWordSearchResultDiaryList(
                        numLoadingItems, loadingOffset, searchWord
                );

        List<WordSearchResultListItem> loadedResultList = listenableFutureResults.get();

        if (loadedResultList.isEmpty()) return new WordSearchResultYearMonthList();
        List<WordSearchResultDayListItem> resultDayListItemList = new ArrayList<>();
        loadedResultList.stream().forEach(x -> {
            resultDayListItemList.add(
                    new WordSearchResultDayListItem(
                            x, searchWord, spannableStringColor, spannableStringBackGroundColor)
            );
        });
        WordSearchResultDayList resultDayList = new WordSearchResultDayList(resultDayListItemList);
        boolean existsUnloadedDiaries = existsUnloadedDiaries(resultDayList.countDiaries());
        return new WordSearchResultYearMonthList(resultDayList, !existsUnloadedDiaries);
    }

    private boolean existsUnloadedDiaries(int numLoadedDiaries)
            throws CancellationException, ExecutionException, InterruptedException {

        String searchWord = this.searchWord.getValue();
        Objects.requireNonNull(searchWord);

        Integer numExistingDiaries = diaryRepository.countWordSearchResultDiaries(searchWord).get();
        Objects.requireNonNull(numExistingDiaries);
        this.numWordSearchResults.postValue(numExistingDiaries);
        if (numExistingDiaries <= 0) return false;

        return numLoadedDiaries < numExistingDiaries;
    }

    // LiveDataGetter
    // MEMO:単一データバインディングの場合、ゲッターの戻り値はLiveData<>にすること。
    //      双方向データバインディングの場合、ゲッターの戻り値はMutableLiveData<>にすること。
    @NonNull
    LiveData<String> getSearchWordLiveData() {
        return searchWord;
    }

    @NonNull
    public MutableLiveData<String> getSearchWordMutableLiveData() {
        return searchWord;
    }

    @NonNull
    LiveData<WordSearchResultYearMonthList> getWordSearchResultListLiveData() {
        return wordSearchResultList;
    }

    @NonNull
    public LiveData<Integer> getNumWordSearchResultsLiveData() {
        return numWordSearchResults;
    }

    @NonNull
    public LiveData<Boolean> getIsVisibleUpdateProgressBarLiveData() {
        return isVisibleUpdateProgressBar;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

}
