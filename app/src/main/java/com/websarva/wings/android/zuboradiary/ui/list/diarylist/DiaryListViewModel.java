package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity;
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
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
public class DiaryListViewModel extends BaseViewModel {

    private final DiaryRepository diaryRepository;
    private Future<?> diaryListLoadingFuture; // キャンセル用
    private final MutableLiveData<DiaryYearMonthList> diaryList = new MutableLiveData<>();

    /**
     * データベース読込からRecyclerViewへの反映までを true とする。
     */
    private final MutableLiveData<Boolean> isVisibleUpdateProgressBar = new MutableLiveData<>();
    public static final int NUM_LOADING_ITEMS = 10; //初期読込時の対象リストが画面全体に表示される値にすること。 // TODO:仮数値の為、最後に設定
    private LocalDate sortConditionDate;
    private final ExecutorService executorService;

    private final boolean isValidityDelay = true;// TODO:調整用

    @Inject
    DiaryListViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        executorService = Executors.newSingleThreadExecutor();
        initialize();
    }

    @Override
    protected void initialize() {
        initializeAppMessageList();
        diaryList.setValue(new DiaryYearMonthList());
        isVisibleUpdateProgressBar.setValue(false);
        sortConditionDate = null;
    }

    boolean canLoadDiaryList() {
        Log.d("OnScrollDiaryList", "isLoadingDiaryList()");
        if (diaryListLoadingFuture == null) {
            Log.d("OnScrollDiaryList", "diaryListLoadingFuture == null");
            return true;
        }
        return diaryListLoadingFuture.isDone();
    }

    void loadNewDiaryList() {
        loadSavedDiaryList(new NewDiaryListCreator());
    }

    void loadAdditionDiaryList() {
        loadSavedDiaryList(new AddedDiaryListCreator());
    }

    void updateDiaryList() {
        loadSavedDiaryList(new UpdateDiaryListCreator());
    }

    private void loadSavedDiaryList(DiaryListCreator creator) {
        Log.d("DiaryListLoading", "loadDiaryList()");
        cancelPreviousLoading();
        DiaryListLoadingRunnable runnable = new DiaryListLoadingRunnable(creator);
        diaryListLoadingFuture = executorService.submit(runnable);
    }

    private void cancelPreviousLoading() {
        if (!canLoadDiaryList()) {
            diaryListLoadingFuture.cancel(true);
        }
    }

    private interface DiaryListCreator {

        @NonNull
        DiaryYearMonthList create() throws CancellationException, ExecutionException, InterruptedException;
    }

    private class DiaryListLoadingRunnable implements Runnable {

        DiaryListCreator diaryListCreator;

        DiaryListLoadingRunnable(DiaryListCreator diaryListCreator) {
            this.diaryListCreator = diaryListCreator;
        }

        @Override
        public void run() {
            Log.d("DiaryListLoading", "DiaryListLoadingRunnable.run()");
            DiaryYearMonthList previousDiaryList = diaryList.getValue();
            Objects.requireNonNull(previousDiaryList);
            try {
                DiaryYearMonthList updateDiaryList = diaryListCreator.create();
                Log.d("DiaryListLoading", "diaryList.postValue()");
                diaryList.postValue(updateDiaryList);
            } catch (CancellationException e) {
                e.printStackTrace();
                // 例外処理なし
            } catch (ExecutionException e) {
                e.printStackTrace();
                diaryList.postValue(previousDiaryList);
                addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (!isValidityDelay) {
                    diaryList.postValue(previousDiaryList);
                    addAppMessage(AppMessage.DIARY_LOADING_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                diaryList.postValue(previousDiaryList);
                addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            }
        }
    }

    private class NewDiaryListCreator implements DiaryListCreator {

        @Override
        @NonNull
        public DiaryYearMonthList create()
                throws CancellationException, ExecutionException, InterruptedException {
            showDiaryListFirstItemProgressIndicator();
            if (isValidityDelay) Thread.sleep(1000);
            return loadSavedDiaryList(NUM_LOADING_ITEMS, 0);
        }

        private void showDiaryListFirstItemProgressIndicator() {
            DiaryYearMonthList list = new DiaryYearMonthList(false);
            diaryList.postValue(list);
        }
    }

    private class AddedDiaryListCreator implements DiaryListCreator {

        @Override
        @NonNull
        public DiaryYearMonthList create()
                throws CancellationException, ExecutionException, InterruptedException {
            DiaryYearMonthList currentDiaryList = diaryList.getValue();
            Objects.requireNonNull(currentDiaryList);
            if (currentDiaryList.getDiaryYearMonthListItemList().isEmpty()) throw new IllegalStateException();

            if (isValidityDelay) Thread.sleep(1000);
            int loadingOffset = currentDiaryList.countDiaries();
            DiaryYearMonthList loadedDiaryList = loadSavedDiaryList(NUM_LOADING_ITEMS, loadingOffset);
            int numLoadedDiaries = currentDiaryList.countDiaries() + loadedDiaryList.countDiaries();
            boolean existsUnloadedDiaries = existsUnloadedDiaries(numLoadedDiaries);
            return currentDiaryList.combineDiaryLists(loadedDiaryList, !existsUnloadedDiaries);
        }
    }

    private class UpdateDiaryListCreator implements DiaryListCreator {

        @Override
        @NonNull
        public DiaryYearMonthList create()
                throws CancellationException, ExecutionException, InterruptedException {
            DiaryYearMonthList currentDiaryList = diaryList.getValue();
            Objects.requireNonNull(currentDiaryList);
            if (currentDiaryList.getDiaryYearMonthListItemList().isEmpty()) throw new IllegalStateException();

            isVisibleUpdateProgressBar.postValue(true);
            try {
                if (isValidityDelay) Thread.sleep(3000);
                int numLoadingItems = currentDiaryList.countDiaries();
                // HACK:画面全体にリストアイテムが存在しない状態で日記を追加した後にリスト画面に戻ると、
                //      日記追加前のアイテム数しか表示されない状態となる。また、スクロール更新もできない。
                //      対策として下記コードを記述。
                if (numLoadingItems < NUM_LOADING_ITEMS) {
                    numLoadingItems = NUM_LOADING_ITEMS;
                }
                return loadSavedDiaryList(numLoadingItems, 0);
            } finally {
                isVisibleUpdateProgressBar.postValue(false);
            }
        }
    }

    @NonNull
    private DiaryYearMonthList loadSavedDiaryList(int numLoadingItems, int loadingOffset)
            throws CancellationException, ExecutionException, InterruptedException {
        if (numLoadingItems <= 0) throw new IllegalArgumentException();
        if (loadingOffset < 0) throw new IllegalArgumentException();


        ListenableFuture<List<DiaryListItem>> listListenableFuture =
                diaryRepository.loadDiaryList(
                        numLoadingItems,
                        loadingOffset,
                        sortConditionDate
                );

        List<DiaryListItem> loadedDiaryList = listListenableFuture.get();
        if (loadedDiaryList.isEmpty()) return new DiaryYearMonthList();
        List<DiaryDayListItem> diaryDayListItemList = new ArrayList<>();
        loadedDiaryList.stream().forEach(x -> diaryDayListItemList.add(new DiaryDayListItem(x)));
        DiaryDayList diaryDayList = new DiaryDayList(diaryDayListItemList);
        boolean existsUnloadedDiaries = existsUnloadedDiaries(diaryDayList.countDiaries());
        return new DiaryYearMonthList(diaryDayList, !existsUnloadedDiaries);
    }

    private boolean existsUnloadedDiaries(int numLoadedDiaries)
            throws CancellationException, ExecutionException, InterruptedException {

        Integer numExistingDiaries;
        if (sortConditionDate == null) {
            numExistingDiaries = diaryRepository.countDiaries().get();
        } else {
            numExistingDiaries = diaryRepository.countDiaries(sortConditionDate).get();
        }
        Objects.requireNonNull(numExistingDiaries);
        if (numExistingDiaries <= 0) return false;

        return numLoadedDiaries < numExistingDiaries;
    }

    void updateSortConditionDate(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth);

        sortConditionDate= yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    boolean deleteDiary(LocalDate date) {
        Objects.requireNonNull(date);

        Integer result;
        try {
            result = diaryRepository.deleteDiary(date).get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
            return false;
        }
        Objects.requireNonNull(result);

        // 削除件数 = 1が正常
        if (result != 1) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
            return false;
        }

        updateDiaryList();
        return true;
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    boolean checkSavedPicturePathDoesNotExist(Uri uri) {
        Objects.requireNonNull(uri);

        try {
            return !diaryRepository.existsPicturePath(uri).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            return false;
        }
    }

    @Nullable
    Integer countSavedDiaries() {
        try {
            return diaryRepository.countDiaries().get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR);
            return null;
        }
    }


    @Nullable
    LocalDate loadNewestSavedDiary() {
        try {
            DiaryEntity diaryEntity = diaryRepository.loadNewestDiary().get();
            String strDate = diaryEntity.getDate();
            return LocalDate.parse(strDate);
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR);
            return null;
        }
    }

    @Nullable
    LocalDate loadOldestSavedDiary() {
        try {
            DiaryEntity diaryEntity = diaryRepository.loadOldestDiary().get();
            String strDate = diaryEntity.getDate();
            return LocalDate.parse(strDate);
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR);
            return null;
        }
    }

    // LiveDataGetter
    @NonNull
    LiveData<DiaryYearMonthList> getDiaryListLiveData() {
        return diaryList;
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
