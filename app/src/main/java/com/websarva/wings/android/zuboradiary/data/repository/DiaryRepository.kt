package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DataBaseAccessException
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.DiaryError
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.Throws


internal class DiaryRepository (
    private val diaryDataSource: DiaryDataSource
) {

    private val logTag = createLogTag()

    @Throws(DiaryError.CountDiaries::class)
    suspend fun countDiaries(): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.CountDiaries(e)
            }
        }

    }

    @Throws(DiaryError.CountDiaries::class)
    suspend fun countDiaries(date: LocalDate): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries(date)
            } catch (e: DataBaseAccessException) {
                throw DiaryError.CountDiaries(e)
            }
        }
    }

    @Throws(DiaryError.CheckDiaryExistence::class)
    suspend fun existsDiary(date: LocalDate): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsDiary(date)
            } catch (e: DataBaseAccessException) {
                throw DiaryError.CheckDiaryExistence(e)
            }
        }
    }

    @Throws(DiaryError.CheckPicturePathUsage::class)
    suspend fun existsPicturePath(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsPicturePath(uri)
            } catch (e: DataBaseAccessException) {
                throw DiaryError.CheckPicturePathUsage(e)
            }
        }
    }

    @Throws(DiaryError.LoadDiary::class)
    suspend fun loadDiary(date: LocalDate): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectDiary(date)?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.LoadDiary(e)
            }
        }
    }

    @Throws(DiaryError.LoadDiary::class)
    suspend fun loadNewestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectNewestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.LoadDiary(e)
            }
        }
    }

    @Throws(DiaryError.LoadDiary::class)
    suspend fun loadOldestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectOldestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.LoadDiary(e)
            }
        }
    }

    @Throws(DiaryError.LoadDiaryList::class)
    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItem> {
        Log.d(logTag, "loadDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource
                    .selectDiaryListOrderByDateDesc(num, offset, date)
                    .map { it.toDomainModel() }
            } catch (e: DataBaseAccessException) {
                throw DiaryError.LoadDiaryList(e)
            }
        }
    }

    @Throws(DiaryError.CountDiaries::class)
    suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countWordSearchResults(searchWord)
            } catch (e: DataBaseAccessException) {
                throw DiaryError.CountDiaries(e)
            }
        }
    }

    @Throws(DiaryError.LoadDiaryList::class)
    suspend fun loadWordSearchResultDiaryList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<WordSearchResultListItem> {
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource
                    .selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
                    .map { it.toDomainModel() }
            } catch (e: DataBaseAccessException) {
                throw DiaryError.LoadDiaryList(e)
            }
        }
    }

    @Throws(DiaryError.SaveDiary::class)
    suspend fun saveDiary(
        diary: Diary,
        historyItemList: List<DiaryItemTitleSelectionHistoryItem>
    ) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.saveDiary(
                    diary.toDataModel(),
                    historyItemList.map { it.toDataModel() }
                )
            } catch (e: DataBaseAccessException) {
                throw DiaryError.SaveDiary(e)
            }
        }
    }

    @Throws(DiaryError.DeleteAndSaveDiary::class)
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        newDiary: Diary,
        historyItemList: List<DiaryItemTitleSelectionHistoryItem>
    ) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAndSaveDiary(
                    deleteDiaryDate,
                    newDiary.toDataModel(),
                    historyItemList.map { it.toDataModel() }
                )
            } catch (e: DataBaseAccessException) {
                throw DiaryError.DeleteAndSaveDiary(e)
            }
        }
    }

    @Throws(DiaryError.DeleteDiary::class)
    suspend fun deleteDiary(date: LocalDate) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteDiary(date)
            } catch (e: DataBaseAccessException) {
                throw DiaryError.DeleteDiary(e)
            }
        }
    }

    @Throws(DiaryError.DeleteAllDiaries::class)
    suspend fun deleteAllDiaries() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllDiaries()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.DeleteAllDiaries(e)
            }
        }
    }

    @Throws(DiaryError.DeleteAllData::class)
    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllData()
            } catch (e: DataBaseAccessException) {
                throw DiaryError.DeleteAllData(e)
            }
        }
    }
}
