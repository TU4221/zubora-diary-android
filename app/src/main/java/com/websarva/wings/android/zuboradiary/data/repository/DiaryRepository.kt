package com.websarva.wings.android.zuboradiary.data.repository

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DataBaseAccessFailureException
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDiariesDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryCountFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryExistenceCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryImageUriUsageCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiarySaveFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.WordSearchResultCountFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDataDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryItemDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.WordSearchResultListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.selectionhistory.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.Throws


internal class DiaryRepository (
    private val diaryDataSource: DiaryDataSource
) {

    private val logTag = createLogTag()

    @Throws(DiaryCountFailureException::class)
    suspend fun countDiaries(): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries()
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryCountFailureException(e)
            }
        }

    }

    @Throws(DiaryCountFailureException::class)
    suspend fun countDiaries(date: LocalDate): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countDiaries(date)
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryCountFailureException(e)
            }
        }
    }

    @Throws(DiaryExistenceCheckFailureException::class)
    suspend fun existsDiary(date: LocalDate): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsDiary(date)
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryExistenceCheckFailureException(date, e)
            }
        }
    }

    @Throws(DiaryImageUriUsageCheckFailureException::class)
    suspend fun existsImageUri(uriString: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.existsImageUri(uriString)
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryImageUriUsageCheckFailureException(uriString, e)
            }
        }
    }

    @Throws(DiaryLoadFailureException::class)
    suspend fun loadDiary(date: LocalDate): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectDiary(date)?.toDomainModel()
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryLoadFailureException("日付 '$date' の日記の読込に失敗しました。", e)
            }
        }
    }

    @Throws(DiaryLoadFailureException::class)
    suspend fun loadNewestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectNewestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryLoadFailureException("最新の日記の読込に失敗しました。", e)
            }
        }
    }

    @Throws(DiaryLoadFailureException::class)
    suspend fun loadOldestDiary(): Diary? {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.selectOldestDiary()?.toDomainModel()
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryLoadFailureException("最古の日記の読込に失敗しました。", e)
            }
        }
    }

    @Throws(DiaryListLoadFailureException::class)
    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryDayListItem.Standard> {
        Log.d(logTag, "loadDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource
                    .selectDiaryListOrderByDateDesc(num, offset, date)
                    .map { it.toDomainModel() }
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryListLoadFailureException(e)
            }
        }
    }

    @Throws(WordSearchResultCountFailureException::class)
    suspend fun countWordSearchResultDiaries(searchWord: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                diaryDataSource.countWordSearchResults(searchWord)
            } catch (e: DataBaseAccessFailureException) {
                throw WordSearchResultCountFailureException(searchWord, e)
            }
        }
    }

    @Throws(WordSearchResultListLoadFailureException::class)
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
            } catch (e: DataBaseAccessFailureException) {
                throw WordSearchResultListLoadFailureException(searchWord, e)
            }
        }
    }

    @Throws(DiarySaveFailureException::class)
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
            } catch (e: DataBaseAccessFailureException) {
                throw DiarySaveFailureException(diary.date, e)
            }
        }
    }

    @Throws(DiarySaveFailureException::class)
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
            } catch (e: DataBaseAccessFailureException) {
                throw DiarySaveFailureException(newDiary.date, e)
            }
        }
    }

    @Throws(DiaryDeleteFailureException::class)
    suspend fun deleteDiary(date: LocalDate) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteDiary(date)
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryDeleteFailureException(date, e)
            }
        }
    }

    @Throws(AllDiariesDeleteFailureException::class)
    suspend fun deleteAllDiaries() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllDiaries()
            } catch (e: DataBaseAccessFailureException) {
                throw AllDiariesDeleteFailureException(e)
            }
        }
    }

    @Throws(AllDataDeleteFailureException::class)
    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteAllData()
            } catch (e: DataBaseAccessFailureException) {
                throw AllDataDeleteFailureException(e)
            }
        }
    }

    /**
     * @throws DiaryItemTitleSelectionHistoryLoadFailureException
     */
    fun loadDiaryItemTitleSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<SelectionHistoryListItem>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryDataSource
            .selectHistoryListOrderByLogDesc(num, offset)
            .catch {
                throw DiaryItemTitleSelectionHistoryLoadFailureException(it)
            }
            .map { list ->
                list.map { it.toDomainModel() }
            }
    }

    @Throws(DiaryItemTitleSelectionHistoryItemDeleteFailureException::class)
    suspend fun deleteDiaryItemTitleSelectionHistoryItem(title: String) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteHistoryItem(title)
            } catch (e: DataBaseAccessFailureException) {
                throw DiaryItemTitleSelectionHistoryItemDeleteFailureException(title, e)
            }
        }
    }
}
