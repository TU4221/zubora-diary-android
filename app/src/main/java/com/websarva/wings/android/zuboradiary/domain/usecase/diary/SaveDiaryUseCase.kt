package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.SaveDiaryError
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakeUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

// TODO:コメントアウト最終的に削除
internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val takeUriPermissionUseCase: TakeUriPermissionUseCase,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        /*loadedDiaryEntity: DiaryEntity?*/
        loadedDate: LocalDate?,
        loadedPicturePath: Uri?
    ): UseCaseResult<Unit, SaveDiaryError> {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                /*loadedDiaryEntity*/loadedDate
            )

            val savedPicturePath = diary.picturePath
            /*val loadedDate = Uri.parse(loadedDiaryEntity.picturePath)*/
            managePictureUriPermission(
                savedPicturePath,
                loadedPicturePath
            )
        } catch (e: SaveDiaryError) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Error(e)
        }

        Log.e(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        /*loadedDiaryEntity: DiaryEntity?*/
        loadedDate: LocalDate?
    ) {
        val logMsg = "日記データ保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val saveDate = diary.date
            /*val loadedDate = LocalDate.parse(loadedDiaryEntity.date)*/
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
        } catch (e: Exception) {
            throw SaveDiaryError.SaveDiary(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    private fun shouldDeleteLoadedDateDiary(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): Boolean {
        loadedDate ?: return false

        return inputDate != loadedDate
    }

    private suspend fun managePictureUriPermission(
        savedPicturePath: Uri?,
        loadedPicturePath: Uri?
    ) {
        val logMsg = "画像Uri権限管理_"
        Log.i(logTag, "${logMsg}開始")

        if (loadedPicturePath != null) {
            when (val result = releaseUriPermissionUseCase(loadedPicturePath)) {
                is UseCaseResult.Success -> {
                    // 処理なし
                }
                is UseCaseResult.Error -> {
                    throw SaveDiaryError.ManageUriPermission(result.error)
                }
            }
        }

        if (savedPicturePath != null) {
            when (val result = takeUriPermissionUseCase(savedPicturePath)) {
                is UseCaseResult.Success -> {
                    // 処理なし
                }
                is UseCaseResult.Error -> {
                    throw SaveDiaryError.ManageUriPermission(result.error)
                }
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
