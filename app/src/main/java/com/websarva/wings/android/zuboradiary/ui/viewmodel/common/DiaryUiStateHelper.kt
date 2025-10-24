package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import javax.inject.Inject

/**
 * 日記関連のUI Stateから、派生的なUI Stateを生成（Produce）する責務を持つヘルパークラス。
 */
internal class DiaryUiStateHelper @Inject constructor(
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) {

    fun isWeather2Visible(diary: DiaryUi): Boolean {
        return diary.weather1 != WeatherUi.UNKNOWN && diary.weather2 != WeatherUi.UNKNOWN
    }

    fun calculateNumVisibleDiaryItems(diary: DiaryUi): Int {
        return diary.itemTitles
            .toSortedMap().values.toList()
            .indexOfLast { it != null }.let { if (it == -1) 0 else it + 1 }
    }

    suspend fun buildImageFilePath(diary: DiaryUi): FilePathUi? {
        val fileName = diary.imageFileName ?: return null
        val result =
            buildDiaryImageFilePathUseCase(
                DiaryImageFileName(fileName)
            )
        return when (result) {
            is UseCaseResult.Success -> FilePathUi.Available(result.value)
            is UseCaseResult.Failure -> FilePathUi.Unavailable
        }
    }
}
