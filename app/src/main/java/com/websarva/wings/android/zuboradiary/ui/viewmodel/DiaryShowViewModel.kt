package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryShowUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : BaseViewModel<DiaryShowUiEvent, DiaryShowAppMessage, DiaryShowUiState>(
    DiaryShowUiState()
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val ARGUMENT_DIARY_ID_KEY = "diary_id"
        private const val ARGUMENT_DIARY_DATE_KEY = "diary_date"
    }

    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && (currentUiState.diaryLoadState is LoadState.Success)

    private val diary
        get() = (currentUiState.diaryLoadState as LoadState.Success).data

    private val diaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.diaryLoadState == new.diaryLoadState
        }.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    // キャッシュパラメータ
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null

    init {
        initializeDiaryData()
        collectUiStates()
    }

    private fun initializeDiaryData() {
        val id = handle.get<String>(ARGUMENT_DIARY_ID_KEY)?.let { DiaryId(it) } ?: throw IllegalArgumentException()
        val date = handle.get<LocalDate>(ARGUMENT_DIARY_DATE_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            loadSavedDiary(id, date)
        }
    }

    private fun collectUiStates() {
        collectWeather2Visible()
        collectNumVisibleDiaryItems()
        collectImageFilePath()
    }

    private fun collectWeather2Visible() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.weather1 == new.weather1 && old.weather2 == new.weather2
        }.map {
            diaryUiStateHelper.isWeather2Visible(it.weather1, it.weather2)
        }.distinctUntilChanged().onEach { isWeather2Visible ->
            updateIsWeather2Visible(isWeather2Visible)
        }.launchIn(viewModelScope)
    }

    private fun collectNumVisibleDiaryItems() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.itemTitles == new.itemTitles
        }.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it.itemTitles)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateNumVisibleDiaryItems(numVisibleDiaryItems)
        }.launchIn(viewModelScope)
    }

    private fun collectImageFilePath() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.imageFileName == new.imageFileName
        }.map {
            diaryUiStateHelper.buildImageFilePath(it.imageFileName)
        }.catchUnexpectedError(
            FilePathUi.Unavailable
        ).distinctUntilChanged().onEach { path ->
            updateDiaryImageFilePath(path)
        }.launchIn(viewModelScope)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigatePreviousFragment(date)
        }
    }

    // Viewクリック処理
    fun onDiaryEditMenuClick() {
        if (!isReadyForOperation) return

        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryShowUiEvent.NavigateDiaryEditFragment(id, date)
            )
        }
    }

    fun onDiaryDeleteMenuClick() {
        if (!isReadyForOperation) return

        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(DiaryId(id), date)
            emitUiEvent(
                DiaryShowUiEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onNavigationIconClick() {
        if (!isReadyForOperation) return

        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigatePreviousFragment(date)
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                launchWithUnexpectedErrorHandler {
                    emitUiEvent(
                        DiaryShowUiEvent.NavigatePreviousFragmentOnDiaryLoadFailed()
                    )
                }
            }
        }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleDiaryDeleteDialogPositiveResult(pendingDiaryDeleteParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryDeleteParameters()
    }

    private fun handleDiaryDeleteDialogPositiveResult(parameters: DiaryDeleteParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                deleteDiary(it.id, it.date)
            } ?: throw IllegalStateException()
        }
    }

    // データ処理
    private suspend fun loadSavedDiary(id: DiaryId, date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToDiaryLoadingState()
        when (val result = loadDiaryByIdUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                val diary = result.value.toUiModel()
                updateToDiaryLoadSuccessState(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                when (result.exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        updateToDiaryLoadErrorState()
                        emitUiEvent(DiaryShowUiEvent.NavigateDiaryLoadFailureDialog(date))
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        updateToDiaryLoadErrorState()
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun deleteDiary(id: DiaryId, date: LocalDate) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateToProgressVisibleState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateToProgressInvisibleState()
                emitUiEvent(
                    DiaryShowUiEvent.NavigatePreviousFragmentOnDiaryDeleted(
                        FragmentResult.Some(date)
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToProgressInvisibleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        emitAppMessageEvent(DiaryShowAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(DiaryShowAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    // FragmentAction関係
    private suspend fun navigatePreviousFragment(diaryDate: LocalDate) {
        emitNavigatePreviousFragmentEvent(
            FragmentResult.Some(diaryDate)
        )
    }

    private fun updateIsWeather2Visible(isVisible: Boolean) {
        updateUiState { it.copy(isWeather2Visible = isVisible) }
    }

    private fun updateNumVisibleDiaryItems(count: Int) {
        updateUiState { it.copy(numVisibleDiaryItems = count) }
    }

    private fun updateDiaryImageFilePath(path: FilePathUi?) {
        updateUiState { it.copy(diaryImageFilePath = path) }
    }

    private fun updateToDiaryLoadingState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToDiaryLoadSuccessState(diary: DiaryUi) {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Success(diary),
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToDiaryLoadErrorState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = true
            )
        }
    }

    private fun updateToProgressVisibleState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToProgressInvisibleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updatePendingDiaryDeleteParameters(id: DiaryId, date: LocalDate) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, date)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val date: LocalDate
    )
}
