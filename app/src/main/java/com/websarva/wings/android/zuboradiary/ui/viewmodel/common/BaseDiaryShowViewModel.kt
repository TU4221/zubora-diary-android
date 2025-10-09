package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.model.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal abstract class BaseDiaryShowViewModel<E : UiEvent, M : AppMessage, S : UiState> (
    initialViewUiState: S,
    protected val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseViewModel<E, M, S>(
    initialViewUiState
) {

    protected val diaryStateFlow = DiaryStateFlow()
    val date
        get() = diaryStateFlow.date.asStateFlow()
    val weather1
        get() = diaryStateFlow.weather1.asStateFlow()
    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()
    val isWeather2Visible =
        combine(weather1, weather2) { weather1, weather2 ->
            return@combine weather1 != WeatherUi.UNKNOWN && weather2 != WeatherUi.UNKNOWN
        }.stateInWhileSubscribed(
            false
        )
    val condition
        get() = diaryStateFlow.condition.asStateFlow()
    val title
        get() = diaryStateFlow.title.asStateFlow()
    val numVisibleItems
        get() = diaryStateFlow.numVisibleItems.asStateFlow()
    val item1Title
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(1)).title.asStateFlow()
    val item2Title
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(2)).title.asStateFlow()
    val item3Title
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(3)).title.asStateFlow()
    val item4Title
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(4)).title.asStateFlow()
    val item5Title
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(5)).title.asStateFlow()
    val item1Comment
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(1)).comment.asStateFlow()
    val item2Comment
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(2)).comment.asStateFlow()
    val item3Comment
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(3)).comment.asStateFlow()
    val item4Comment
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(4)).comment.asStateFlow()
    val item5Comment
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(5)).comment.asStateFlow()
    val imageFileName
        get() = diaryStateFlow.imageFileName.asStateFlow()
    val imageFilePath
        get() = diaryStateFlow.imageFilePath.asStateFlow()
    val log
        get() = diaryStateFlow.log.asStateFlow()

    fun onDiaryImageFileNameChanged(fileName: ImageFileNameUi?) {
        viewModelScope.launch {
            buildImageFilePath(fileName)
        }
    }

    protected fun initializeDiary() {
        diaryStateFlow.initialize()
    }

    protected fun updateDiary(diary: DiaryUi) {
        diaryStateFlow.update(diary)
    }

    private suspend fun buildImageFilePath(fileName: ImageFileNameUi?) {
        val imageFilePathUi =
            if (fileName == null) {
                null
            } else {
                when (val result = buildDiaryImageFilePathUseCase(fileName.toDomainModel())) {
                    is UseCaseResult.Success -> {
                        ImageFilePathUi(result.value)
                    }
                    is UseCaseResult.Failure -> {
                        ImageFilePathUi()
                    }
                }
            }
        updateImageFilePath(imageFilePathUi)
    }

    private fun updateImageFilePath(imageFilePath: ImageFilePathUi?) {
        diaryStateFlow.imageFilePath.value = imageFilePath
    }
}
