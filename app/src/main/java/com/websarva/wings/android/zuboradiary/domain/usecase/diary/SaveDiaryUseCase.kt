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

            val savedImageUriString = diary.imageUriString
            val loadedImageUriString = loadedDiary?.imageUriString
            manageImageUriPermission(
                savedImageUriString,
                loadedImageUriString
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
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


    private suspend fun manageImageUriPermission(
        savedImageUriString: String?,
        loadedImageUriString: String?
    ) {
        val logMsg = "画像Uri権限管理_"
        Log.i(logTag, "${logMsg}開始")

        if (loadedImageUriString != null) {
            when (val result = releaseUriPermissionUseCase(loadedImageUriString)) {
                is UseCaseResult.Success -> {
                    // 処理なし
                }
                is UseCaseResult.Failure -> {
                    throw result.exception
                }
            }
        }

        if (savedImageUriString != null) {
            when (val result = takeUriPermissionUseCase(savedImageUriString)) {
                is UseCaseResult.Success -> {
                    // 処理なし
                }
                is UseCaseResult.Failure -> {
                    throw result.exception
                }
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
