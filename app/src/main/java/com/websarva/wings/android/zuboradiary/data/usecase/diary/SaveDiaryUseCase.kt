package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult2
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.uri.TakeUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val takeUriPermissionUseCase: TakeUriPermissionUseCase,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
) {

    private val logTag = createLogTag()
    
    sealed class SaveDiaryUseCaseException(
        message: String,
        cause: Throwable? = null
    ) : UseCaseException(message, cause) {
        
        class SaveDiaryFailedException(
            cause: Throwable?
        ) : SaveDiaryUseCaseException(
            "日記の保存に失敗しました。",
            cause
        )

        class UpdateUriPermissionFailedException(
            cause: Throwable?
        ) : SaveDiaryUseCaseException(
            "日記添付画像のUri権限取得に失敗しました。",
            cause
        )
    }

    suspend operator fun invoke(
        diaryEntity: DiaryEntity,
        diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>,
        /*loadedDiaryEntity: DiaryEntity?*/
        loadedDate: LocalDate?,
        loadedPicturePath: Uri?
    ): UseCaseResult2<Unit, SaveDiaryUseCaseException> {

        try {
            saveDiaryToDatabase(
                diaryEntity,
                diaryItemTitleSelectionHistoryItemEntityList,
                /*loadedDiaryEntity*/loadedDate
            )

            val savedPicturePath = Uri.parse(diaryEntity.picturePath) 
            /*val loadedDate = Uri.parse(loadedDiaryEntity.picturePath)*/
            updatePictureUriPermission(
                savedPicturePath,
                loadedPicturePath
            )
        } catch (e: SaveDiaryUseCaseException) {
            return UseCaseResult2.Error(e)
        }

        Result
        return UseCaseResult2.Success(Unit)
    }

    private suspend fun saveDiaryToDatabase(
        diaryEntity: DiaryEntity,
        diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>,
        /*loadedDiaryEntity: DiaryEntity?*/
        loadedDate: LocalDate?
    ) {
        val logMsg = "日記保存"
        Log.i(logTag, "${logMsg}_開始")

        try {
            val saveDate = LocalDate.parse(diaryEntity.date)
            /*val loadedDate = LocalDate.parse(loadedDiaryEntity.date)*/
            if (shouldDeleteLoadedDateDiary(saveDate, loadedDate)) {
                diaryRepository
                    .deleteAndSaveDiary(
                        requireNotNull(loadedDate),
                        diaryEntity,
                        diaryItemTitleSelectionHistoryItemEntityList
                    )
            } else {
                diaryRepository
                    .saveDiary(diaryEntity, diaryItemTitleSelectionHistoryItemEntityList)
            }
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            throw SaveDiaryUseCaseException.SaveDiaryFailedException(e)
        }

        Log.i(logTag, "${logMsg}_完了")
    }

    private fun shouldDeleteLoadedDateDiary(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): Boolean {
        loadedDate ?: return false

        return inputDate != loadedDate
    }

    private suspend fun updatePictureUriPermission(
        savedPicturePath: Uri,
        loadedPicturePath: Uri?
    ) {
        if (loadedPicturePath != null) {
            try {
                releaseUriPermissionUseCase(loadedPicturePath)
            } catch (e: Exception) {
                throw SaveDiaryUseCaseException.UpdateUriPermissionFailedException(e)
            }
        }

        try {
            takeUriPermissionUseCase(savedPicturePath)
        } catch (e: Exception) {
            throw SaveDiaryUseCaseException.UpdateUriPermissionFailedException(e)
        }

    }
}
