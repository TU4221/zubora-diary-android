package com.websarva.wings.android.zuboradiary.data.database

import android.net.Uri
import android.util.Log
import java.time.LocalDate
import javax.inject.Inject


class DiaryRepository @Inject constructor(
    private val diaryDatabase: DiaryDatabase,
    private val diaryDAO: DiaryDAO,
) {

    suspend fun countDiaries(): Int {
        return diaryDAO.countDiaries()
    }

    suspend fun countDiaries(date: LocalDate): Int {
        return diaryDAO.countDiaries(date.toString())
    }

    suspend fun existsDiary(date: LocalDate): Boolean {
        return diaryDAO.existsDiary(date.toString())
    }

    suspend fun existsPicturePath(uri: Uri): Boolean {
        return diaryDAO.existsPicturePath(uri.toString())
    }

    suspend fun loadDiary(date: LocalDate): DiaryEntity {
        return diaryDAO.selectDiary(date.toString())
    }

    suspend fun loadNewestDiary(): DiaryEntity {
        return diaryDAO.selectNewestDiary()
    }

    suspend fun loadOldestDiary(): DiaryEntity {
        return diaryDAO.selectOldestDiary()
    }

    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItem> {
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

    suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        return diaryDAO.countWordSearchResults(searchWord)
    }

    suspend fun loadWordSearchResultDiaryList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItem> {
        require(num >= 1)
        require(offset >= 0)

        return diaryDAO.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
    }

    suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        diaryDatabase.saveDiary(diaryEntity, updateTitleList)
    }

    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate, createDiaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        diaryDatabase.deleteAndSaveDiary(deleteDiaryDate, createDiaryEntity, updateTitleList)
    }

    suspend fun deleteDiary(date: LocalDate) {
        diaryDAO.deleteDiary(date.toString())
    }

    suspend fun deleteAllDiaries() {
        diaryDAO.deleteAllDiaries()
    }

    suspend fun deleteAllData() {
        diaryDatabase.deleteAllData()
    }
}
