package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiarySaveFailureException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val takePersistableUriPermissionUseCase: TakePersistableUriPermissionUseCase,
    private val releaseDiaryImageUriPermissionUseCase: ReleaseDiaryImageUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                originalDiary.date,
                isNewDiary
            )
        } catch (e: DiarySaveFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        try {
            releaseImageUriPermission(originalDiary.imageUriString)
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

    @Throws(DiarySaveFailureException::class)
    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistory>,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        val logMsg = "日記データ保存_"
        Log.i(logTag, "${logMsg}開始")

        val saveDate = diary.date
        try {
            if (shouldDeleteOriginalDateDiary(saveDate, originalDate, isNewDiary)) {
                diaryRepository
                    .deleteAndSaveDiary(
                        originalDate,
                        diary,
                        diaryItemTitleSelectionHistoryItemList
                    )
            } else {
                diaryRepository
                    .saveDiary(diary, diaryItemTitleSelectionHistoryItemList)
            }
        } catch (e: DiarySaveFailureException) {
            throw e
        }

        Log.i(logTag, "${logMsg}完了")
    }

    private fun shouldDeleteOriginalDateDiary(
        inputDate: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): Boolean {
        if (isNewDiary) return false

        return inputDate != originalDate
    }

    @Throws(DomainException::class)
    private suspend fun releaseImageUriPermission(
        uriString: String?
    ) {
        val logMsg = "画像URIの永続的権限解放_"

        if (uriString == null) {
            Log.i(logTag, "${logMsg}不要")
            return
        }

        Log.i(logTag, "${logMsg}開始")
        when (val result = releaseDiaryImageUriPermissionUseCase(uriString)) {
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
        val logMsg = "画像URIの永続的権限取得_"

        if (uriString == null) {
            Log.i(logTag, "${logMsg}不要")
            return
        }

        Log.i(logTag, "${logMsg}開始")
        when (val result = takePersistableUriPermissionUseCase(uriString)) {
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
