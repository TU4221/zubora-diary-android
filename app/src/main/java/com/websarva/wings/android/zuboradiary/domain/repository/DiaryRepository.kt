package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDiariesDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryCountFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryExistenceCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryImageUriUsageCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiarySaveFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.WordSearchResultCountFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDataDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryItemDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryItemTitleSelectionHistoryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.WordSearchResultListLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.Throws


internal interface DiaryRepository {

    //region Diary
    @Throws(DiaryCountFailureException::class)
    suspend fun countDiaries(): Int

    @Throws(DiaryCountFailureException::class)
    suspend fun countDiaries(date: LocalDate): Int

    @Throws(DiaryExistenceCheckFailureException::class)
    suspend fun existsDiary(date: LocalDate): Boolean

    @Throws(DiaryImageUriUsageCheckFailureException::class)
    suspend fun existsImageUri(uriString: String): Boolean

    @Throws(DiaryLoadException.AccessFailure::class)
    suspend fun loadDiary(date: LocalDate): Diary?

    @Throws(DiaryLoadException.AccessFailure::class)
    suspend fun loadNewestDiary(): Diary?

    @Throws(DiaryLoadException.AccessFailure::class)
    suspend fun loadOldestDiary(): Diary?

    @Throws(DiaryListLoadFailureException::class)
    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryDayListItem.Standard>

    @Throws(DiarySaveFailureException::class)
    suspend fun saveDiary(
        diary: Diary,
        historyItemList: List<DiaryItemTitleSelectionHistory>
    )

    @Throws(DiarySaveFailureException::class)
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        newDiary: Diary,
        historyItemList: List<DiaryItemTitleSelectionHistory>
    )

    @Throws(DiaryDeleteFailureException::class)
    suspend fun deleteDiary(date: LocalDate)

    @Throws(AllDiariesDeleteFailureException::class)
    suspend fun deleteAllDiaries()
    //endregion

    //region WordSearchResult
    @Throws(WordSearchResultCountFailureException::class)
    suspend fun countWordSearchResults(searchWord: String): Int

    @Throws(WordSearchResultListLoadFailureException::class)
    suspend fun loadWordSearchResultList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<RawWordSearchResultListItem>
    //endregion

    //region DiaryItemTitleSelectionHistory
    /**
     * @throws DiaryItemTitleSelectionHistoryLoadFailureException
     */
    fun loadDiaryItemTitleSelectionHistoryList(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryListItem>>

    @Throws(DiaryItemTitleSelectionHistoryItemDeleteFailureException::class)
    suspend fun deleteDiaryItemTitleSelectionHistory(title: String)
    //endregion

    //region Options
    @Throws(AllDataDeleteFailureException::class)
    suspend fun deleteAllData()
    //endregion
}
