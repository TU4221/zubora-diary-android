package com.websarva.wings.android.zuboradiary.data.database

import android.database.sqlite.SQLiteException
import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

internal class DiaryDataSource(
    private val diaryDatabase: DiaryDatabase,
    private val diaryDAO: DiaryDAO,
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    private val logTag = createLogTag()

    @Throws(DataBaseAccessException::class)
    private suspend fun <R> executeSuspendDbOperation(
        operation: suspend () -> R
    ): R {
        return try {
            operation()
        } catch (e: SQLiteException) {
            throw DataBaseAccessException(e)
        } catch (e: IllegalStateException) {
            throw DataBaseAccessException(e)
        }
    }

    @Throws(DataBaseAccessException::class)
    private fun <R> executeDbOperation(
        operation: () -> R
    ): R {
        return try {
            operation()
        } catch (e: SQLiteException) {
            throw DataBaseAccessException(e)
        } catch (e: IllegalStateException) {
            throw DataBaseAccessException(e)
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun countDiaries(): Int {
        return executeSuspendDbOperation {
            diaryDAO.countDiaries()
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun countDiaries(date: LocalDate): Int {
        return executeSuspendDbOperation {
            diaryDAO.countDiaries(date.toString())
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun existsDiary(date: LocalDate): Boolean {
        return executeSuspendDbOperation {
            diaryDAO.existsDiary(date.toString())
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun existsPicturePath(uri: Uri): Boolean {
        return executeSuspendDbOperation {
            diaryDAO.existsPicturePath(uri.toString())
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun selectDiary(date: LocalDate): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDAO.selectDiary(date.toString())
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun selectNewestDiary(): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDAO.selectNewestDiary()
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun selectOldestDiary(): DiaryEntity? {
        return executeSuspendDbOperation {
            diaryDAO.selectOldestDiary()
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItemData> {
        Log.d(logTag, "selectDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return executeSuspendDbOperation {
            if (date == null) {
                diaryDAO.selectDiaryListOrderByDateDesc(num, offset)
            } else {
                diaryDAO.selectDiaryListOrderByDateDesc(num, offset, date.toString())
            }
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun countWordSearchResults(searchWord: String): Int {
        return executeSuspendDbOperation {
            diaryDAO.countWordSearchResults(searchWord)
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItemData> {
        require(num >= 1)
        require(offset >= 0)


        return executeSuspendDbOperation {
            diaryDAO.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun saveDiary(
        diary: DiaryEntity,
        historyItemList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        executeSuspendDbOperation {
            diaryDatabase.saveDiary(
                diary,
                historyItemList
            )
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        newDiary: DiaryEntity,
        historyItemList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        executeSuspendDbOperation {
            diaryDatabase.deleteAndSaveDiary(
                deleteDiaryDate,
                newDiary,
                historyItemList
            )
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun deleteDiary(date: LocalDate) {
        executeSuspendDbOperation {
            diaryDAO.deleteDiary(date.toString())
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun deleteAllDiaries() {
        executeSuspendDbOperation {
            diaryDAO.deleteAllDiaries()
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun deleteAllData() {
        executeSuspendDbOperation {
            diaryDatabase.deleteAllData()
        }
    }

    @Throws(DataBaseAccessException::class)
    fun selectHistoryListOrderByLogDesc(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItemEntity>> {
        require(num >= 1)
        require(offset >= 0)

        return executeDbOperation {
            diaryItemTitleSelectionHistoryDAO
                .selectHistoryListOrderByLogDesc(num, offset)
        }
    }

    @Throws(DataBaseAccessException::class)
    suspend fun deleteHistoryItem(title: String) {
        return executeSuspendDbOperation {
            diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title)
        }
    }
}
