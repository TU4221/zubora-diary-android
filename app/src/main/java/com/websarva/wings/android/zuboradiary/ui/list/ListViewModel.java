package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.Diary;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListViewModel extends AndroidViewModel {

    private DiaryRepository diaryRepository;
    private MutableLiveData<List<DiaryYearMonthListItem>> diaryList =
            new MutableLiveData<>(new ArrayList<>());
    private final int LOAD_ITEM_NUM = 10; // TODO:仮数値の為、最後に設定
    private int loadItemOffset = 0;
    private String sortConditionDate = "";


    public ListViewModel(@NonNull Application application) {
        super(application);
        diaryRepository = new DiaryRepository(getApplication());
    }

    public enum LoadType {
        NEW, UPDATE, ADD
    }

    public void loadList(LoadType loadType) {
        if (loadType == LoadType.NEW) {
            ListViewModel.this.diaryList.setValue(new ArrayList<>());
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.d("リスト読込確認", "起動");
                int loadItemNum;
                if (loadType == LoadType.UPDATE) {
                    loadItemNum = ListViewModel.this.loadItemOffset;
                    ListViewModel.this.loadItemOffset = 0;
                } else if (loadType == LoadType.ADD) {
                    loadItemNum = ListViewModel.this.LOAD_ITEM_NUM;
                } else {
                    // LoadType.NEW
                    loadItemNum = ListViewModel.this.LOAD_ITEM_NUM;
                    ListViewModel.this.loadItemOffset = 0;
                    ListViewModel.this.diaryList.postValue(new ArrayList<>());
                }

                // 現時点のDiaryListを保持
                List<DiaryYearMonthListItem> previousDiaryList =
                        ListViewModel.this.diaryList.getValue();

                // ProgressBar追加
                List<DiaryYearMonthListItem> diaryListContainingProgressBar = new ArrayList<>();
                DiaryYearMonthListItem progressBar = new DiaryYearMonthListItem();
                progressBar.setViewType(
                        DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_PROGRESS_BAR);
                diaryListContainingProgressBar.addAll(previousDiaryList);
                diaryListContainingProgressBar.add(progressBar);
                ListViewModel.this.diaryList.postValue(diaryListContainingProgressBar);

                // TODO:ProgressBarを表示させる為に仮で記述
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // データ読込
                List<DiaryListItem> loadedData = new ArrayList<>();
                if (ListViewModel.this.sortConditionDate.equals("")) {
                    loadedData =
                            ListViewModel.this.diaryRepository.selectDiaryList(
                                    loadItemNum,
                                    ListViewModel.this.loadItemOffset,
                                    null
                            );
                } else {
                    loadedData =
                            ListViewModel.this.diaryRepository.selectDiaryList(
                                    loadItemNum,
                                    ListViewModel.this.loadItemOffset,
                                    ListViewModel.this.sortConditionDate
                            );
                }

                // 更新用リスト準備
                List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();

                // 読込データ空確認
                if (loadedData.isEmpty()) {
                    updateDiaryList.addAll(previousDiaryList);
                    DiaryYearMonthListItem noDiaryMessage = new DiaryYearMonthListItem();
                    noDiaryMessage.setViewType(
                            DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE);
                    updateDiaryList.add(noDiaryMessage);
                    ListViewModel.this.diaryList.postValue(updateDiaryList);
                    return;
                }

                // 読込データを日記リストへ追加
                ListViewModel.this.loadItemOffset += loadItemNum;
                List<DiaryYearMonthListItem> convertedList = toDiaryYearMonthListFormat(loadedData);
                if (loadType == LoadType.ADD) {
                    // 前回の読込リストの最終アイテムの年月取得
                    int previousDiaryListLastItemPosition = previousDiaryList.size() - 1;
                    DiaryYearMonthListItem previousDiaryYearMonthListLastItem =
                            previousDiaryList.get(previousDiaryListLastItemPosition);
                    int previousDiaryYearMonthListLastItemYear =
                            previousDiaryYearMonthListLastItem.getYear();
                    int previousDiaryYearMonthListLastItemMonth =
                            previousDiaryYearMonthListLastItem.getMonth();

                    // 今回の読込リストの先頭アイテムの年月取得
                    DiaryYearMonthListItem additionalDiaryListFirstItem =
                            convertedList.get(0);
                    int additionalDiaryListFirstItemYear =
                            additionalDiaryListFirstItem.getYear();
                    int additionalDiaryListFirstItemMonth =
                            additionalDiaryListFirstItem.getMonth();

                    // 前回の読込リストに今回の読込リストの年月が含まれていたら,
                    // そこにDiaryDayListItemを足し込む
                    if (previousDiaryYearMonthListLastItemYear == additionalDiaryListFirstItemYear
                            && previousDiaryYearMonthListLastItemMonth == additionalDiaryListFirstItemMonth) {
                        List<DiaryDayListItem> previousDiaryDayListItemList =
                                previousDiaryYearMonthListLastItem.getDiaryDayListItemList();
                        List<DiaryDayListItem> additionalDiaryDayListItemList =
                                additionalDiaryListFirstItem.getDiaryDayListItemList();
                        previousDiaryDayListItemList.addAll(additionalDiaryDayListItemList);
                        convertedList.remove(0);
                    }

                    updateDiaryList.addAll(previousDiaryList);
                    updateDiaryList.addAll(convertedList);
                    ListViewModel.this.diaryList.postValue(updateDiaryList);
                } else {
                    updateDiaryList.addAll(convertedList);
                    ListViewModel.this.diaryList.postValue(updateDiaryList);
                }

                // TODO:DiaryRepositoryをまとめてから残日記を確認するメソッドを下記条件にあてはめる。
                if (false) {
                    DiaryYearMonthListItem noDiaryMessage = new DiaryYearMonthListItem();
                    noDiaryMessage.setViewType(
                            DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_NO_DIARY_MESSAGE);
                    updateDiaryList.add(noDiaryMessage);
                }

                ListViewModel.this.diaryList.postValue(updateDiaryList);
            }
        });

    }

    private List<DiaryYearMonthListItem> toDiaryYearMonthListFormat(List<DiaryListItem> beforeList) {
        // 型変換:List<DiaryListItem> -> List<Map<String, Object>>
        List<DiaryDayListItem> diaryDayListItemList = new ArrayList<>();
        DiaryDayListItem diaryDayListItem;
        String date;
        String title;
        String picturePath;
        String year;
        String month;
        String dayOfMonth;
        String dayOfWeek;
        final int VIEW_TYPE_DIARY = DiaryListFragment.DiaryYearMonthListAdapter.VIEW_TYPE_DIARY;
        int startIndex;
        int endIndex;
        for (DiaryListItem diaryListItem : beforeList) {
            date = diaryListItem.getDate();
            title = diaryListItem.getTitle();
            picturePath = diaryListItem.getPicturePath();
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
            diaryDayListItem = new DiaryDayListItem();
            diaryDayListItem.setYear(Integer.parseInt(year));
            diaryDayListItem.setMonth(Integer.parseInt(month));
            diaryDayListItem.setDayOfMonth(Integer.parseInt(dayOfMonth));
            diaryDayListItem.setDayOfWeek(dayOfWeek);
            diaryDayListItem.setTitle(title);
            diaryDayListItem.setPicturePath(picturePath);
            diaryDayListItemList.add(diaryDayListItem);
        }

        // 日記リストを月別に振り分ける
        List<DiaryDayListItem> sortingList= new ArrayList<>();
        List<DiaryYearMonthListItem> diaryYearMonthListItemList = new ArrayList<>();
        int sortingYear = 0;
        int sortingMonth = 0;

        for (DiaryDayListItem day: diaryDayListItemList) {
            int _year = day.getYear();
            int _Month = day.getMonth();

            if (sortingYear != 0 && sortingMonth != 0
                    && (_year != sortingYear || _Month != sortingMonth)) {
                addDiaryYearMonthListItem(
                        diaryYearMonthListItemList,
                        sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY);
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYear = _year;
            sortingMonth = _Month;
        }
        addDiaryYearMonthListItem(
                diaryYearMonthListItemList, sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY);

        return diaryYearMonthListItemList;
    }

    private void addDiaryYearMonthListItem (List<DiaryYearMonthListItem> diaryYearMonthListItemList,
                                            int year, int Month, List<DiaryDayListItem> sortingList, int VIEW_TYPE_DIARY) {
        DiaryYearMonthListItem diaryYearMonthListItem = new DiaryYearMonthListItem();
        diaryYearMonthListItem.setYear(year);
        diaryYearMonthListItem.setMonth(Month);
        diaryYearMonthListItem.setDiaryDayListItemList(sortingList);
        diaryYearMonthListItem.setViewType(VIEW_TYPE_DIARY);
        diaryYearMonthListItemList.add(diaryYearMonthListItem);
    }

    public void updateSortConditionDate(int year, int month) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        // https://nainaistar.hatenablog.com/entry/2021/05/13/120000
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate lastDayOfMonthDate = LocalDate
                    .of(year, month, 1)
                    .with(TemporalAdjusters.lastDayOfMonth());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            this.sortConditionDate= lastDayOfMonthDate.format(formatter);
        }
    }

    public void deleteDiary(String date) {
        diaryRepository.deleteDiary(date);
        this.loadItemOffset--;

        LocalDate deleteDiaryDate = DateConverter.toLocalDate(date);

        List<DiaryYearMonthListItem> updateDiaryList = new ArrayList<>();
        List<DiaryYearMonthListItem> currentDiaryList = ListViewModel.this.diaryList.getValue();

        DiaryYearMonthListItem targetYearMonthListItem = new DiaryYearMonthListItem();
        List<DiaryDayListItem> targetDayList = new ArrayList<>();
        for (DiaryYearMonthListItem item: currentDiaryList) {
            if (item.getYear() == deleteDiaryDate.getYear()
                    && item.getMonth() == deleteDiaryDate.getMonthValue()) {
                targetYearMonthListItem = item;
                targetDayList = item.getDiaryDayListItemList();
                break;
            }
        }

        for (DiaryDayListItem item: targetDayList) {
            if (item.getDayOfMonth() == deleteDiaryDate.getDayOfMonth()) {
                targetDayList.remove(item);
                if (targetDayList.isEmpty()) {
                    currentDiaryList.remove(targetYearMonthListItem);
                }
                break;
            }
        }

        updateDiaryList.addAll(currentDiaryList);
        ListViewModel.this.diaryList.postValue(updateDiaryList);

    }

    public int countDiaries() {
        return this.diaryRepository.countDiaries();
    }


    public Diary loadNewestDiary() {
        return this.diaryRepository.selectNewestDiary();
    }

    public Diary loadOldestDiary() {
        return this.diaryRepository.selectOldestDiary();
    }


    // Getter/Setter
    public LiveData<List<DiaryYearMonthListItem>> getLiveDataDiaryList() {
        return this.diaryList;
    }
    public void setLiveDataDiaryList(List<DiaryYearMonthListItem> list) {
        this.diaryList.setValue(list);
    }
}
