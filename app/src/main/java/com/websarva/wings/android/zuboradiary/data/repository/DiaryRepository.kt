package com.websarva.wings.android.zuboradiary.data.repository

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DataBaseAccessException
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteAllDiariesFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CountDiariesFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CheckDiaryExistenceFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryListFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CheckDiaryImageUriUsedFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.SaveDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CountWordSearchResultFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteAllDataFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteDiaryItemTitleSelectionHistoryItemFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryItemTitleSelectionHistoryFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchWordSearchResultListFailedException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.Throws


internal class DiaryRepository (
    private val diaryDataSource: DiaryDataSource
) {

    private val logTag = createLogTag()

    @Throws(CountDiariesFailedException::class)
    suspend fun countDiaries(): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries()
            } catch (e: DataBaseAccessException) {
                throw CountDiariesFailedException(e)
            }
        }

    }

    @Throws(CountDiariesFailedException::class)
    suspend fun countDiaries(date: LocalDate): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries(date)
            } catch (e: DataBaseAccessException) {
                throw CountDiariesFailedException(e)
            }
        }
    }

    @Throws(CheckDiaryExistenceFailedException::class)
    suspend fun existsDiary(date: LocalDate): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsDiary(date)
            } catch (e: DataBaseAccessException) {
                throw CheckDiaryExistenceFailedException(date, e)
            }
        }
    }

    @Throws(CheckDiaryImageUriUsedFailedException::class)
    suspend fun existsImageUri(uriString: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsImageUri(uriString)
            } catch (e: DataBaseAccessException) {
                throw CheckDiaryImageUriUsedFailedException(uriString, e)
            }
        }
    }

    @Throws(FetchDiaryFailedException::class)
    suspend fun fetchDiary(date: LocalDate): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectDiary(date)?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw FetchDiaryFailedException("日付 '$date' の日記の取得に失敗しました。", e)
            }
        }
    }

    @Throws(FetchDiaryFailedException::class)
    suspend fun fetchNewestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectNewestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw FetchDiaryFailedException("最新の日記の取得に失敗しました。", e)
            }
        }
    }

    @Throws(FetchDiaryFailedException::class)
    suspend fun fetchOldestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectOldestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessException) {
                throw FetchDiaryFailedException("最古の日記の取得に失敗しました。", e)
            }
        }
    }

    @Throws(FetchDiaryListFailedException::class)
    suspend fun fetchDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryListItem> {
        Log.d(logTag, "fetchDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource
                    .selectDiaryListOrderByDateDesc(num, offset, date)
                    .map { it.toDomainModel() }
            } catch (e: DataBaseAccessException) {
                throw FetchDiaryListFailedException(e)
            }
        }
    }

    @Throws(CountWordSearchResultFailedException::class)
    suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countWordSearchResults(searchWord)
            } catch (e: DataBaseAccessException) {
                throw CountWordSearchResultFailedException(searchWord, e)
            }
        }
    }

    @Throws(FetchWordSearchResultListFailedException::class)
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
                throw FetchWordSearchResultListFailedException(searchWord, e)
            }
        }
    }

    @Throws(SaveDiaryFailedException::class)
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
                throw SaveDiaryFailedException(diary.date, e)
            }
        }
    }

    @Throws(SaveDiaryFailedException::class)
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
                throw SaveDiaryFailedException(newDiary.date, e)
            }
        }
    }

    @Throws(DeleteDiaryFailedException::class)
    suspend fun deleteDiary(date: LocalDate) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteDiary(date)
            } catch (e: DataBaseAccessException) {
                throw DeleteDiaryFailedException(date, e)
            }
        }
    }

    @Throws(DeleteAllDiariesFailedException::class)
    suspend fun deleteAllDiaries() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllDiaries()
            } catch (e: DataBaseAccessException) {
                throw DeleteAllDiariesFailedException(e)
            }
        }
    }

    @Throws(DeleteAllDataFailedException::class)
    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllData()
            } catch (e: DataBaseAccessException) {
                throw DeleteAllDataFailedException(e)
            }
        }
    }

    @Throws(FetchDiaryItemTitleSelectionHistoryFailedException::class)
    fun fetchDiaryItemTitleSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItem>> {
        require(num >= 1)
        require(offset >= 0)

        return try {
            diaryDataSource
                .selectHistoryListOrderByLogDesc(num, offset)
                .map { list ->
                    list.map { it.toDomainModel() }
                }
        } catch (e: DataBaseAccessException) {
            throw FetchDiaryItemTitleSelectionHistoryFailedException(e)
        }
    }

    @Throws(DeleteDiaryItemTitleSelectionHistoryItemFailedException::class)
    suspend fun deleteDiaryItemTitleSelectionHistoryItem(title: String) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteHistoryItem(title)
            } catch (e: DataBaseAccessException) {
                throw DeleteDiaryItemTitleSelectionHistoryItemFailedException(title, e)
            }
        }
    }
}
