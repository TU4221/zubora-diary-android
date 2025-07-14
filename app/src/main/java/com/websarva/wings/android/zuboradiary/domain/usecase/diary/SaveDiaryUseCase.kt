package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.SaveDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakeUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val takeUriPermissionUseCase: TakeUriPermissionUseCase,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        loadedDiary: Diary?
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val loadedDate = loadedDiary?.date
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                loadedDate
            )
        } catch (e: SaveDiaryFailedException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        try {
            releaseLoadedImageUriPermission(loadedDiary?.imageUriString)
        } catch (e: DomainException) {
            // MEMO:Uri権限の取り消しに失敗しても日記保存がメインの為、成功とみなす。
            Log.e(logTag, "${logMsg}Uri権限取消失敗", e)
        }

        try {
            takeSavedImageUriPermission(diary.imageUriString)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}Uri権限確保失敗", e)
            // MEMO:Uri権限の確保に失敗しても日記保存がメインの為、成功とみなす。
        }

        Log.e(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(SaveDiaryFailedException::class)
    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        /*loadedDiaryEntity: DiaryEntity?*/
        loadedDate: LocalDate?
    ) {
        val logMsg = "日記データ保存_"
        Log.i(logTag, "${logMsg}開始")

        val saveDate = diary.date
        try {
            if (shouldDeleteLoadedDateDiary(saveDate, loadedDate)) {
                diaryRepository
                    .deleteAndSaveDiary(
                        requireNotNull(loadedDate),
                        diary,
                        diaryItemTitleSelectionHistoryItemList
                    )
            } else {
                diaryRepository
                    .saveDiary(diary, diaryItemTitleSelectionHistoryItemList)
            }
        } catch (e: SaveDiaryFailedException) {
            throw e
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DomainException::class)
    private fun shouldDeleteLoadedDateDiary(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): Boolean {
        loadedDate ?: return false

        return inputDate != loadedDate
    }


    @Throws(DomainException::class)
    private suspend fun releaseLoadedImageUriPermission(
        uriString: String?
    ) {
        val logMsg = "画像Uri権限取消_"
        Log.i(logTag, "${logMsg}開始")

        if (uriString == null) {
            Log.i(logTag, "${logMsg}不要")
            return
        }

        when (val result = releaseUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DomainException::class)
    private fun takeSavedImageUriPermission(
        uriString: String?
    ) {
        val logMsg = "画像Uri権限確保_"
        Log.i(logTag, "${logMsg}開始")

        if (uriString == null) {
            Log.i(logTag, "${logMsg}不要")
            return
        }

        when (val result = takeUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
