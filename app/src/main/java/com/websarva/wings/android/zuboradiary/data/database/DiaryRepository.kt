package com.websarva.wings.android.zuboradiary.data.database

import android.net.Uri
import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import java.time.LocalDate
import java.util.concurrent.Callable
import java.util.concurrent.Future
import javax.inject.Inject


class DiaryRepository @Inject constructor(
    private val diaryDatabase: DiaryDatabase,
    private val diaryDAO: DiaryDAO,
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    fun countDiaries(): ListenableFuture<Int> {
        return diaryDAO.countDiaries()
    }

    fun countDiaries(date: LocalDate): ListenableFuture<Int> {
        return diaryDAO.countDiaries(date.toString())
    }

    fun existsDiary(date: LocalDate): ListenableFuture<Boolean> {
        return diaryDAO.existsDiary(date.toString())
    }

    fun existsPicturePath(uri: Uri): ListenableFuture<Boolean> {
        return diaryDAO.existsPicturePath(uri.toString())
    }

    fun loadDiary(date: LocalDate): ListenableFuture<DiaryEntity> {
        return diaryDAO.selectDiary(date.toString())
    }

    fun loadNewestDiary(): ListenableFuture<DiaryEntity> {
        return diaryDAO.selectNewestDiary()
    }

    fun loadOldestDiary(): ListenableFuture<DiaryEntity> {
        return diaryDAO.selectOldestDiary()
    }

    fun loadDiaryList(
        num: Int, offset: Int, date: LocalDate?
    ): ListenableFuture<List<DiaryListItem>> {
        require(num >= 1)
        require(offset >= 0)

        Log.d(
            "DiaryRepository",
            "loadDiaryList(num = $num, offset = $offset, date = $date)"
        )
        return if (date == null) {
            diaryDAO.selectDiaryListOrderByDateDesc(num, offset)
        } else {
            diaryDAO.selectDiaryListOrderByDateDesc(num, offset, date.toString())
        }
    }

    fun countWordSearchResultDiaries(searchWord: String): ListenableFuture<Int> {
        return diaryDAO.countWordSearchResults(searchWord)
    }

    fun loadWordSearchResultDiaryList(
        num: Int, offset: Int, searchWord: String
    ): ListenableFuture<List<WordSearchResultListItem>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryDAO.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
    }

    fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ): Future<Void> {
        return DiaryDatabase.EXECUTOR_SERVICE.submit(
            Callable {
                diaryDatabase.runInTransaction(
                    Callable<Void> {
                        diaryDAO.insertDiary(diaryEntity)
                        diaryItemTitleSelectionHistoryDAO.insertHistoryItem(updateTitleList)
                        diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem()
                        null
                    }
                )
            }
        )
    }

    fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate, createDiaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ): Future<Void> {
        return DiaryDatabase.EXECUTOR_SERVICE.submit(
            Callable {
                diaryDatabase.runInTransaction(
                    Callable<Void> {
                        diaryDAO.deleteDiary(deleteDiaryDate.toString())
                        diaryDAO.insertDiary(createDiaryEntity)
                        diaryItemTitleSelectionHistoryDAO.insertHistoryItem(updateTitleList)
                        diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem()
                        null
                    }
                )
            }
        )
    }

    fun deleteDiary(date: LocalDate): ListenableFuture<Int> {
        return diaryDAO.deleteDiary(date.toString())
    }

    fun deleteAllDiaries(): ListenableFuture<Int> {
        return diaryDAO.deleteAllDiaries()
    }

    fun deleteAllData(): Future<Void> {
        return DiaryDatabase.EXECUTOR_SERVICE.submit(Callable {
            diaryDatabase.runInTransaction(
                Callable<Void> {
                    diaryDAO.deleteAllDiaries()
                    diaryItemTitleSelectionHistoryDAO.deleteAllItem()
                    null
                }
            )
        })
    }
}
