package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryShowState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val fetchDiaryUseCase: FetchDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : BaseViewModel<DiaryShowEvent, DiaryShowAppMessage, DiaryShowState>(
    DiaryShowState.Idle
) {

    private val logTag = createLogTag()

    override val isProcessingState =
        uiState
            .map { state ->
                when (state) {
                    DiaryShowState.Loading,
                    DiaryShowState.Deleting -> true

                    DiaryShowState.Idle,
                    DiaryShowState.LoadSuccess,
                    DiaryShowState.LoadError -> false
                }
            }
            .stateInWhileSubscribed(
                false
            )

    // 日記データ関係
    private val diaryStateFlow = DiaryStateFlow(viewModelScope, handle)
    val date
        get() = diaryStateFlow.date.asStateFlow()
    val weather1
        get() = diaryStateFlow.weather1.asStateFlow()
    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()
    val isWeather2Visible =
        combine(weather1, weather2) { weather1, weather2 ->
            return@combine weather1 != Weather.UNKNOWN && weather2 != Weather.UNKNOWN
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
    val imageUri
        get() = diaryStateFlow.imageUri.asStateFlow()
    val log
        get() = diaryStateFlow.log.asStateFlow()

    override fun initialize() {
        super.initialize()
        diaryStateFlow.initialize()
    }

    override suspend fun emitNavigatePreviousFragmentEvent() {
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment<Nothing>()
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: DiaryShowAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (isProcessing) return

        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            navigatePreviousFragment(date)
        }
    }

    // ViewClicked処理
    fun onDiaryEditMenuClicked() {
        if (uiState.value != DiaryShowState.LoadSuccess) return

        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(date)
            )
        }
    }

    fun onDiaryDeleteMenuClicked() {
        if (uiState.value != DiaryShowState.LoadSuccess) return

        val date = diaryStateFlow.date.requireValue()
        val imageUri = diaryStateFlow.imageUri.value
        viewModelScope.launch {
            val parameters = DiaryDeleteParameters(date, imageUri)
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryDeleteDialog(parameters)
            )
        }
    }

    fun onNavigationClicked() {
        if (isProcessing) return

        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            navigatePreviousFragment(date)
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadingFailureDialogResultReceived(result: DialogResult<Unit>) {
        check(uiState.value == DiaryShowState.LoadError)

        when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                viewModelScope.launch {
                    navigatePreviousFragment()
                }
            }
        }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
        check(uiState.value == DiaryShowState.LoadSuccess)

        when (result) {
            is DialogResult.Positive<Unit> -> {
                onDiaryDeleteDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                check(uiState.value == DiaryShowState.LoadSuccess)
            }
        }
    }

    private fun onDiaryDeleteDialogPositiveResultReceived() {
        viewModelScope.launch {
            deleteDiary()
        }
    }

    // View状態処理
    fun onCalendarDaySelected(date: LocalDate) {
        viewModelScope.launch {
            prepareDiaryForCalendarFragment(date)
        }
    }

    // Fragment状態処理
    fun onFragmentViewCreated(date: LocalDate) {
        if (uiState.value != DiaryShowState.Idle) return

        viewModelScope.launch {
            prepareDiaryForDiaryShowFragment(date)
        }
    }

    // データ処理
    private suspend fun prepareDiaryForDiaryShowFragment(date: LocalDate) {
        loadSavedDiary(date, true)
    }

    private suspend fun prepareDiaryForCalendarFragment(date: LocalDate) {
        loadSavedDiary(date, false)
    }

    private suspend fun loadSavedDiary(date: LocalDate, ignoreAppMessage: Boolean = false) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Loading)
        when (val result = fetchDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateUiState(DiaryShowState.LoadSuccess)
                val diary = result.value
                diaryStateFlow.update(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadError)
                if (ignoreAppMessage) {
                    emitUiEvent(
                        DiaryShowEvent.NavigateDiaryLoadingFailureDialog(date)
                    )
                } else {
                    emitAppMessageEvent(DiaryShowAppMessage.DiaryLoadingFailure)
                }
            }
        }
    }

    private suspend fun deleteDiary() {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        val date = diaryStateFlow.date.requireValue()
        val imageUriString  = diaryStateFlow.imageUri.value?.toString()

        updateUiState(DiaryShowState.Deleting)
        when (val result = deleteDiaryUseCase(date, imageUriString)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                navigatePreviousFragment(date)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadSuccess)
                emitAppMessageEvent(DiaryShowAppMessage.DiaryDeleteFailure)
            }
        }
    }

    // FragmentAction関係
    private suspend fun navigatePreviousFragment(loadedDiaryDate: LocalDate? = null) {
        updateUiState(DiaryShowState.Idle)
        val result =
            if (loadedDiaryDate == null) {
                FragmentResult.None
            } else {
                FragmentResult.Some(loadedDiaryDate)
            }
        emitUiEvent(
            DiaryShowEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(
                        result
                    )
                )
        )
    }
}
