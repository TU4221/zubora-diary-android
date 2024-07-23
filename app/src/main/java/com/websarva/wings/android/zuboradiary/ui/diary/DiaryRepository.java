package com.websarva.wings.android.zuboradiary.ui.diary;

import static com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchFragment.WordSearchResultYearMonthListAdapter.VIEW_TYPE_DIARY;

import android.app.Application;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedDiaryItemTitle;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedItemTitlesHistoryDAO;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListFragment;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultListItemDiary;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultYearMonthListItem;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class DiaryRepository {
    Context context;
    private DiaryDatabase diaryDatabase;
    DiaryDAO diaryDAO;
    SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO;

    public DiaryRepository(Context context) {
        this.context = context;
        diaryDatabase = DiaryDatabase.getDatabase(context);
        diaryDAO = diaryDatabase.createDiaryDAO();
        selectedItemTitlesHistoryDAO = diaryDatabase.createSelectedItemTitlesHistoryDAO();
    }

    public int countDiaries(@Nullable String date) throws Exception {
        ListenableFuture<Integer> listenableFutureResults;
        if (date != null && !date.isEmpty()) {
            listenableFutureResults = this.diaryDAO.countDiariesAsync(date);
        } else {
            listenableFutureResults = this.diaryDAO.countDiariesAsync();
        }
        // 日付が変更された時、カウントキャンセル
        while (!listenableFutureResults.isDone()) {
            if (Thread.currentThread().isInterrupted()) {
                listenableFutureResults.cancel(true);
                throw new InterruptedException();
            }
        }
        return listenableFutureResults.get();
    }

    public boolean hasDiary(int year, int month, int dayOfMonth) throws Exception {
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        ListenableFuture<Boolean> existDiaryListenableFuture = diaryDAO.hasDiaryAsync(stringDate);
        return existDiaryListenableFuture.get();
    }

    public Diary selectDiary(String date) throws Exception {
        ListenableFuture<Diary> diaryListenableFuture = diaryDAO.selectDiaryAsync(date);
        return  diaryListenableFuture.get();
    }

    public Diary selectNewestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectNewestDiaryAsync();
        return listenableFutureResult.get();
    }

    public Diary selectOldestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectOldestDiaryAsync();
        return listenableFutureResult.get();
    }

    public List<DiaryYearMonthListItem> loadDiaryList(int num, int offset, @Nullable String date) throws Exception {
        ListenableFuture<List<DiaryListItem>> listenableFutureResults;
        if (!(date == null)) {
            listenableFutureResults = diaryDAO.selectDiaryListAsync(num, offset, date);
        } else {
            listenableFutureResults = diaryDAO.selectDiaryListAsync(num, offset);
        }
        // 日付が変更された時、リスト読込キャンセル
        while (!listenableFutureResults.isDone()) {
            if (Thread.currentThread().isInterrupted()) {
                listenableFutureResults.cancel(true);
                throw new InterruptedException();
            }
        }
        List<DiaryListItem> loadedData = listenableFutureResults.get();
        List<DiaryYearMonthListItem> convertedList = new ArrayList<>();
        if (!loadedData.isEmpty()) {
            convertedList = toDiaryYearMonthListFormat(loadedData);
        }
        return convertedList;
    }

    private List<DiaryYearMonthListItem> toDiaryYearMonthListFormat(List<DiaryListItem> beforeList) {
        List<DiaryDayListItem> diaryDayList = new ArrayList<>();
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
            diaryDayListItem =
                    new DiaryDayListItem(
                            Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(dayOfMonth),
                            dayOfWeek,
                            title,
                            picturePath
                    );
            diaryDayList.add(diaryDayListItem);
        }

        // 日記リストを月別に振り分ける
        List<DiaryDayListItem> sortingList= new ArrayList<>();
        List<DiaryYearMonthListItem> diaryYearMonthList = new ArrayList<>();
        int sortingYear = 0;
        int sortingMonth = 0;

        for (DiaryDayListItem day: diaryDayList) {
            int _year = day.getYear();
            int _Month = day.getMonth();

            if (sortingYear != 0 && sortingMonth != 0
                    && (_year != sortingYear || _Month != sortingMonth)) {
                diaryYearMonthList.add(
                        new DiaryYearMonthListItem(sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY)
                );
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYear = _year;
            sortingMonth = _Month;
        }
        diaryYearMonthList.add(
                new DiaryYearMonthListItem(sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY)
        );

        return diaryYearMonthList;
    }

    public int countWordSearchResults(String searchWord) throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                diaryDAO.countWordSearchResultsAsync(searchWord);
        // 検索文字が変更された時、カウントキャンセル
        while (!listenableFutureResult.isDone()) {
            if (Thread.currentThread().isInterrupted()) {
                listenableFutureResult.cancel(true);
                throw new InterruptedException();
            }
        }
        return listenableFutureResult.get();
    }

    public List<WordSearchResultYearMonthListItem> selectWordSearchResultList(
            int num, int offset, String searchWord) throws Exception {
        ListenableFuture<List<WordSearchResultListItemDiary>> listenableFutureResults =
                diaryDAO.selectWordSearchResultListAsync(num, offset, searchWord);
        // 検索文字が変更された時、リスト読込キャンセル
        while (!listenableFutureResults.isDone()) {
            if (Thread.currentThread().isInterrupted()) {
                listenableFutureResults.cancel(true);
                throw new InterruptedException();
            }
        }
        List<WordSearchResultListItemDiary> loadedData = listenableFutureResults.get();
        List<WordSearchResultYearMonthListItem> convertedList = new ArrayList<>();
        if (!loadedData.isEmpty()) {
            convertedList = toWordSearchResultYearMonthListFormat(loadedData, searchWord);
        }
        return convertedList;
    }

    private List<WordSearchResultYearMonthListItem> toWordSearchResultYearMonthListFormat(
            List<WordSearchResultListItemDiary> beforeList, String searchWord) {
        List<WordSearchResultDayListItem> dayList = new ArrayList<>();
        WordSearchResultDayListItem dayListItem;
        String date;
        String year;
        String month;
        String dayOfMonth;
        String dayOfWeek;
        SpannableString title;
        int itemNumber;
        SpannableString itemTitle;
        SpannableString itemComment;

        int startIndex;
        int endIndex;
        for (WordSearchResultListItemDiary item: beforeList) {
            date = item.getDate();
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

            title = createSpannableString(item.getTitle(), searchWord);

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
            itemNumber = 0;
            itemTitle = new SpannableString("");
            itemComment = new SpannableString("");
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
                    itemTitle =
                            createSpannableString(itemTitles[i], searchWord);
                    itemComment =
                            createSpannableString(itemComments[i], searchWord);
                    break;
                }
                if (i == (itemTitles.length - 1)) {
                    itemNumber = 1;
                    itemTitle =
                            createSpannableString(itemTitles[0], searchWord);
                    itemComment =
                            createSpannableString(itemComments[0], searchWord);
                }
            }

            dayListItem =
                    new WordSearchResultDayListItem(
                            Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(dayOfMonth),
                            dayOfWeek,
                            title,
                            itemNumber,
                            itemTitle,
                            itemComment
                    );
            dayList.add(dayListItem);
        }

        // 日記リストを月別に振り分ける
        List<WordSearchResultDayListItem> sortingList= new ArrayList<>();
        WordSearchResultYearMonthListItem monthListItem =
                new WordSearchResultYearMonthListItem();
        List<WordSearchResultYearMonthListItem> monthList = new ArrayList<>();
        int sortingYear = 0;
        int sortingMonth = 0;

        for (WordSearchResultDayListItem day: dayList) {
            int _year = day.getYear();
            int _Month = day.getMonth();

            if (sortingYear != 0 && sortingMonth != 0
                    && (_year != sortingYear || _Month != sortingMonth)) {
                monthList.add(
                        new WordSearchResultYearMonthListItem(sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY));
                sortingList= new ArrayList<>();
            }
            sortingList.add(day);
            sortingYear = _year;
            sortingMonth = _Month;
        }

        monthList.add(
                new WordSearchResultYearMonthListItem(sortingYear, sortingMonth, sortingList, VIEW_TYPE_DIARY));

        return monthList;
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

    public List<Integer> selectDiaryDateList(int year, int month) throws Exception {
        String stringDateYearMonth = DateConverter.toStringLocalDateYearMonth(year, month);
        ListenableFuture<List<String>> listenableFutureResults =
                diaryDAO.selectDiaryDateListAsync(stringDateYearMonth);
        List<String> beforeList = listenableFutureResults.get();
        List<Integer> afterList = new ArrayList<>();
        beforeList.stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                int startIndex = s.indexOf("月") + 1;
                int endIndex = s.indexOf("日");
                String dayOfMonth = s.substring(startIndex, endIndex);
                afterList.add(Integer.parseInt(dayOfMonth));
            }
        });
        return afterList;
    }

    public void insertDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception{
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception{
                        diaryDAO.insertDiary(diary);
                        selectedItemTitlesHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void deleteAndInsertDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList)
            throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        diaryDAO.deleteDiary(deleteDiaryDate);
                        diaryDAO.insertDiary(createDiary);
                        selectedItemTitlesHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void deleteDiary(String date) throws Exception {
        ListenableFuture<Integer> diaryListenableFuture = diaryDAO.deleteDiaryAsync(date);
        diaryListenableFuture.get();
    }
}
