package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.diary.DiaryRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.diary.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.diary.toDomainModel
import com.websarva.wings.android.zuboradiary.data.mapper.diary.toListItemDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.RawWordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate

internal class DiaryRepositoryImpl (
    private val diaryDataSource: DiaryDataSource,
    private val diaryRepositoryExceptionMapper: DiaryRepositoryExceptionMapper
) : DiaryRepository {

    //region Diary
    override suspend fun countDiaries(date: LocalDate?): Int {
        return try {
            diaryDataSource.countDiaries(date)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsDiary(date: LocalDate): Boolean {
        return try {
            diaryDataSource.existsDiary(date)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadDiaryId(date: LocalDate): DiaryId {
        return try {
            val id = diaryDataSource.selectDiaryId(date)
            DiaryId(id)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadDiary(id: DiaryId): Diary {
        return try {
            diaryDataSource.selectDiary(id.value).toDomainModel()
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadNewestDiary(): Diary {
        return try {
            diaryDataSource.selectNewestDiary().toDomainModel()
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadOldestDiary(): Diary {
        return try {
            diaryDataSource.selectOldestDiary().toDomainModel()
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryDayListItem.Standard> {
        return try {
            diaryDataSource
                .selectDiaryListOrderByDateDesc(num, offset, date)
                .map { it.toDomainModel() }
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun saveDiary(
        diary: Diary
    ) {
        try {
            diaryDataSource.upsertDiary(
                diary.toDataModel()
            )
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteAndSaveDiary(
        deleteDiaryId: DiaryId,
        saveDiary: Diary
    ) {
        try {
            diaryDataSource.deleteAndUpsertDiary(
                deleteDiaryId.value,
                saveDiary.toDataModel()
            )
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteDiary(id: DiaryId) {
        try {
            diaryDataSource.deleteDiary(id.value)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteAllDiaries() {
        try {
            diaryDataSource.deleteAllDiaries()
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }
    //endregion

    //region WordSearchResult
    override suspend fun countWordSearchResults(searchWord: SearchWord): Int {
        return try {
            diaryDataSource.countWordSearchResults(searchWord.value)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun loadWordSearchResultList(
        num: Int,
        offset: Int,
        searchWord: SearchWord
    ): List<RawWordSearchResultListItem> {
        return try {
            diaryDataSource
                .selectWordSearchResultListOrderByDateDesc(num, offset, searchWord.value)
                .map { it.toDomainModel() }
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }
    //endregion

    //region DiaryItemTitleSelectionHistory
    override suspend fun findDiaryItemTitleSelectionHistoriesByTitles(
        titleList: List<DiaryItemTitle>
    ): List<DiaryItemTitleSelectionHistory> {
        return try {
            val stringList = titleList.map { it.value }
            diaryDataSource
                .selectDiaryItemTitleSelectionHistoriesByTitles(stringList).map {
                    it.toDomainModel()
                }
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override fun loadDiaryItemTitleSelectionHistoryList(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryListItem>> {
        return diaryDataSource
            .selectHistoryListOrderByLogDesc(num, offset)
            .catch {
                // Flowストリーム内で発生した例外をDataStorageExceptionにラップ
                throw if (it is Exception) {
                    diaryRepositoryExceptionMapper.toDomainException(it)
                } else {
                    it // その他の予期せぬ例外はそのままスロー
                }
            }
            .map { list ->
                list.map { it.toListItemDomainModel() }
            }
    }

    override suspend fun updateDiaryItemTitleSelectionHistory(
        historyItemList: List<DiaryItemTitleSelectionHistory>
    ) {
        try {
            diaryDataSource.upsertAndPruneDiaryItemTitleSelectionHistory(
                historyItemList.map { it.toDataModel() }
            )
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteDiaryItemTitleSelectionHistory(id: DiaryItemTitleSelectionHistoryId) {
        try {
            diaryDataSource.deleteHistoryItem(id.value)
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }
    //endregion

    //region Options
    override suspend fun deleteAllData() {
        try {
            diaryDataSource.initializeAllData()
        } catch (e: Exception) {
            throw diaryRepositoryExceptionMapper.toDomainException(e)
        }
    }
    //endregion
}
