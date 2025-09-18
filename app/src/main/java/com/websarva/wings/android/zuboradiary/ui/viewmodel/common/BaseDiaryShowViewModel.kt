package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.file.BuildImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

internal abstract class BaseDiaryShowViewModel<E : UiEvent, M : AppMessage, S : UiState> (
    initialViewUiState: S,
    protected val buildImageFilePathUseCase: BuildImageFilePathUseCase
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
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).title.asStateFlow()
    val item2Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).title.asStateFlow()
    val item3Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).title.asStateFlow()
    val item4Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).title.asStateFlow()
    val item5Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).title.asStateFlow()
    val item1Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).comment.asStateFlow()
    val item2Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).comment.asStateFlow()
    val item3Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).comment.asStateFlow()
    val item4Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).comment.asStateFlow()
    val item5Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).comment.asStateFlow()
    val imageFileName
        get() = diaryStateFlow.imageFileName.asStateFlow()
    val imageFilePath
        get() = diaryStateFlow.imageFilePath.asStateFlow()
    val log
        get() = diaryStateFlow.log.asStateFlow()

    fun onDiaryImageFileNameChanged(fileName: ImageFileName?) {
        if (fileName == null) {
            updateImageFilePath(ImageFilePathUi.NoImage)
            return
        }

        viewModelScope.launch {
            val imageFilePathUi =
                when (val result = buildImageFilePathUseCase(fileName)) {
                    is UseCaseResult.Success -> {
                        ImageFilePathUi.Valid(result.value)
                    }
                    is UseCaseResult.Failure -> {
                        ImageFilePathUi.Invalid
                    }
                }
            updateImageFilePath(imageFilePathUi)
        }
    }

    protected fun initializeDiary() {
        diaryStateFlow.initialize()
    }

    protected abstract suspend fun loadSavedDiary(date: LocalDate)

    protected fun updateDiary(diary: Diary) {
        diaryStateFlow.update(diary)
    }

    private fun updateImageFilePath(imageFilePath: ImageFilePathUi) {
        diaryStateFlow.imageFilePath.value = imageFilePath
    }
}
