package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * 日記関連のUI Stateから、派生的なUI Stateを生成（Produce）する責務を持つヘルパークラス。
 */
internal class DiaryUiStateHelper @Inject constructor(
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) {

    /**
     * diaryLoadStateFlowから、isWeather2Visibleの状態を示すFlowを生成する。
     */
    fun createIsWeather2VisibleFlow(diaryLoadStateFlow: Flow<LoadState<DiaryUi>>): Flow<Boolean> {
        return diaryLoadStateFlow
            .distinctUntilChanged()
            .map { loadState ->
                if (loadState is LoadState.Success) {
                    val diary = loadState.data
                    diary.weather1 != WeatherUi.UNKNOWN && diary.weather2 != WeatherUi.UNKNOWN
                } else {
                    false
                }
            }
    }

    /**
     * diaryLoadStateFlowから、NumVisibleDiaryItemsの状態を示すFlowを生成する。
     *
     * このメソッドが返すFlowは、内部で[buildDiaryImageFilePathUseCase]を呼び出しているため、
     */
    fun createNumVisibleDiaryItemsFlowFromLoadState(
        diaryLoadStateFlow: Flow<LoadState<DiaryUi>>
    ): Flow<Int> {
        return diaryLoadStateFlow
            .distinctUntilChanged()
            .mapNotNull {
                (it as? LoadState.Success)?.data
            }.let { createNumVisibleDiaryItemsFlow(it) }
    }

    fun createNumVisibleDiaryItemsFlow(diaryFlow: Flow<DiaryUi>): Flow<Int> {
        return diaryFlow
            .distinctUntilChanged()
            .map{ diary ->
                listOf(
                    diary.item1Title,
                    diary.item2Title,
                    diary.item3Title,
                    diary.item4Title,
                    diary.item5Title
                ).indexOfLast { it != null }.let { if (it == -1) 0 else it + 1 }
            }
    }

    fun createNumVisibleDiaryItemsFlowFromMap(diaryFlow: Flow<DiaryUi>): Flow<Int> {
        return diaryFlow
            .distinctUntilChanged()
            .map{ diary ->
                diary.itemTitles
                    .toSortedMap().values.toList().also { Log.d("202510222", it.toString()) }
                    .indexOfLast { it != null }.let { if (it == -1) 0 else it + 1 }
            }
    }

    /**
     * diaryLoadStateFlowから、DiaryImageFilePathの状態を示すFlowを生成する。
     *
     * このメソッドが返すFlowは、内部で[buildDiaryImageFilePathUseCase]を呼び出しているため、
     * 予期せぬ例外をスローする可能性があります。
     */
    fun createDiaryImageFilePathFlowFromLoadState(
        diaryLoadStateFlow: Flow<LoadState<DiaryUi>>
    ): Flow<FilePathUi?> {
        return diaryLoadStateFlow
            .distinctUntilChanged()
            .mapNotNull {
                (it as? LoadState.Success)?.data
            }.let { createDiaryImageFilePathFlow(it) }
    }

    fun createDiaryImageFilePathFlow(diaryFlow: Flow<DiaryUi>): Flow<FilePathUi?> {
        return diaryFlow
            .distinctUntilChanged()
            .map { diary ->
                val fileName = diary.imageFileName ?: return@map null
                val result =
                    buildDiaryImageFilePathUseCase(
                        DiaryImageFileName(fileName)
                    )
                when (result) {
                    is UseCaseResult.Success -> FilePathUi.Available(result.value)
                    is UseCaseResult.Failure -> FilePathUi.Unavailable
                }
            }
    }
}
