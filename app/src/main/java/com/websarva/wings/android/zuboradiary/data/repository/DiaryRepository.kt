package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate


internal class DiaryRepository (
    private val diaryDatabase: DiaryDatabase,
    private val diaryDAO: DiaryDAO,
) {

    private val logTag = createLogTag()

    suspend fun countDiaries(): Int {
        return withContext(Dispatchers.IO) {
            diaryDAO.countDiaries()
        }

    }

    suspend fun countDiaries(date: LocalDate): Int {
        return withContext(Dispatchers.IO) {
            diaryDAO.countDiaries(date.toString())
        }
    }

    suspend fun existsDiary(date: LocalDate): Boolean {
        return withContext(Dispatchers.IO) {
            diaryDAO.existsDiary(date.toString())
        }
    }

    suspend fun existsPicturePath(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            diaryDAO.existsPicturePath(uri.toString())
        }
    }

    suspend fun loadDiary(date: LocalDate): DiaryEntity? {
        return withContext(Dispatchers.IO) {
            diaryDAO.selectDiary(date.toString())
        }
    }

    suspend fun loadNewestDiary(): DiaryEntity? {
        return withContext(Dispatchers.IO) {
            diaryDAO.selectNewestDiary()
        }
    }

    suspend fun loadOldestDiary(): DiaryEntity? {
        return withContext(Dispatchers.IO) {
            diaryDAO.selectOldestDiary()
        }
    }

    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItem> {
        Log.d(logTag, "loadDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            if (date == null) {
                diaryDAO.selectDiaryListOrderByDateDesc(num, offset)
            } else {
                diaryDAO.selectDiaryListOrderByDateDesc(num, offset, date.toString())
            }
        }
    }

    suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        return withContext(Dispatchers.IO) {
            diaryDAO.countWordSearchResults(searchWord)
        }
    }

    suspend fun loadWordSearchResultDiaryList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItem> {
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            diaryDAO.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
        }
    }

    suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        withContext(Dispatchers.IO) {
            diaryDatabase.saveDiary(diaryEntity, updateTitleList)
        }
    }

    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate, createDiaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        withContext(Dispatchers.IO) {
            diaryDatabase.deleteAndSaveDiary(deleteDiaryDate, createDiaryEntity, updateTitleList)
        }
    }

    suspend fun deleteDiary(date: LocalDate) {
        withContext(Dispatchers.IO) {
            diaryDAO.deleteDiary(date.toString())
        }
    }

    suspend fun deleteAllDiaries() {
        withContext(Dispatchers.IO) {
            diaryDAO.deleteAllDiaries()
        }
    }

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            diaryDatabase.deleteAllData()
        }
    }
}
