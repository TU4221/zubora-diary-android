package com.websarva.wings.android.zuboradiary.data.repository

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException
import com.websarva.wings.android.zuboradiary.data.mapper.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate

internal class DiaryRepositoryImpl (
    private val diaryDataSource: DiaryDataSource
) : DiaryRepository {

    private val logTag = createLogTag()

    //region Diary
    override suspend fun countDiaries(date: LocalDate?): Int {
        return try {
            if (date == null) {
                diaryDataSource.countDiaries()
            } else {
                diaryDataSource.countDiaries(date)
            }
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun existsDiary(date: LocalDate): Boolean {
        return try {
            diaryDataSource.existsDiary(date)
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun loadDiary(date: LocalDate): Diary {
        return try {
            diaryDataSource.selectDiary(date)?.toDomainModel()
                ?: throw ResourceNotFoundException()
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun loadNewestDiary(): Diary {
        return try {
            diaryDataSource.selectNewestDiary()?.toDomainModel()
                ?: throw ResourceNotFoundException()
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun loadOldestDiary(): Diary {
        return try {
            diaryDataSource.selectOldestDiary()?.toDomainModel()
                ?: throw ResourceNotFoundException()
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryDayListItem.Standard> {
        Log.d(logTag, "loadDiaryList(num = $num, offset = $offset, date = $date)")
        require(num >= 1)
        require(offset >= 0)

        return try {
            diaryDataSource
                .selectDiaryListOrderByDateDesc(num, offset, date)
                .map { it.toDomainModel() }
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun saveDiary(
        diary: Diary
    ) {
        try {
            diaryDataSource.saveDiary(
                diary.toDataModel()
            )
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun deleteAndSaveDiary(
        diary: Diary
    ) {
        try {
            diaryDataSource.deleteAndSaveDiary(
                diary.toDataModel()
            )
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun deleteDiary(date: LocalDate) {
        try {
            diaryDataSource.deleteDiary(date)
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun deleteAllDiaries() {
        try {
            diaryDataSource.deleteAllDiaries()
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }
    //endregion

    //region WordSearchResult
    override suspend fun countWordSearchResults(searchWord: String): Int {
        return try {
            diaryDataSource.countWordSearchResults(searchWord)
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun loadWordSearchResultList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<RawWordSearchResultListItem> {
        require(num >= 1)
        require(offset >= 0)

        return try {
            diaryDataSource
                .selectWordSearchResultListOrderByDateDesc(num, offset, searchWord)
                .map { it.toDomainModel() }
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }
    //endregion

    //region DiaryItemTitleSelectionHistory
    override fun loadDiaryItemTitleSelectionHistoryList(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryListItem>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryDataSource
            .selectHistoryListOrderByLogDesc(num, offset)
            .catch {
                // Flowストリーム内で発生した例外をDataStorageExceptionにラップ
                if (it is DatabaseException) {
                    throw DataStorageException(cause = it)
                } else {
                    throw it // その他の予期せぬ例外はそのままスロー
                }
            }
            .map { list ->
                list.map { it.toDomainModel() }
            }
    }

    override suspend fun updateDiaryItemTitleSelectionHistory(
        historyItemList: List<DiaryItemTitleSelectionHistory>
    ) {
        try {
            diaryDataSource.updateDiaryItemTitleSelectionHistory(
                historyItemList.map { it.toDataModel() }
            )
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun deleteDiaryItemTitleSelectionHistory(title: String) {
        try {
            diaryDataSource.deleteHistoryItem(title)
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }
    //endregion

    //region Options
    override suspend fun deleteAllData() {
        try {
            diaryDataSource.initializeAllData()
        } catch (e: DatabaseException) {
            throw DataStorageException(cause = e)
        }
    }
    //endregion
}
