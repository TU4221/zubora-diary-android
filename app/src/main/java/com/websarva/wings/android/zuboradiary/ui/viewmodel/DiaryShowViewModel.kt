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
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ErrorType
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryShowUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val diaryUiStateHelper: DiaryUiStateHelper
) : BaseViewModel<DiaryShowEvent, DiaryShowAppMessage, DiaryShowUiState>(
    DiaryShowUiState()
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val ID_ARGUMENT_KEY = "load_diary_id"
        private const val DATE_ARGUMENT_KEY = "load_diary_date"
    }

    override val isProgressIndicatorVisible =
        uiState
            .map { state ->
                state.isProcessing
            }
            .stateInWhileSubscribed(
                false
            )

    private val diaryFlow =
        uiState.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    // キャッシュパラメータ
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null

    init {
        observeDerivedUiStateChanges()
        initializeDiaryData(handle)
    }

    private fun observeDerivedUiStateChanges() {
        diaryFlow.map {
            diaryUiStateHelper.isWeather2Visible(it)
        }.onEach { isWeather2Visible ->
            updateUiState {
                it.copy(
                    isWeather2Visible = isWeather2Visible
                )
            }
        }.launchIn(viewModelScope)

        diaryFlow.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it)
        }.onEach { numVisibleDiaryItems ->
            updateUiState {
                it.copy(
                    numVisibleDiaryItems = numVisibleDiaryItems
                )
            }
        }.launchIn(viewModelScope)

        diaryFlow.map {
            diaryUiStateHelper.buildImageFilePath(it)
        }.catchUnexpectedError(null).onEach { path ->
            updateUiState {
                it.copy(
                    diaryImageFilePath = path
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        val id = handle.get<String>(ID_ARGUMENT_KEY)?.let { DiaryId(it) } ?: throw IllegalArgumentException()
        val date = handle.get<LocalDate>(DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            loadSavedDiary(id, date)
        }
    }

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): DiaryShowEvent {
        return DiaryShowEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: DiaryShowAppMessage): DiaryShowEvent {
        return DiaryShowEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): DiaryShowAppMessage {
        return DiaryShowAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        val currentUiState = uiState.value
        if (currentUiState.isInputDisabled) return
        val diaryLoadState = currentUiState.diaryLoadState
        if (diaryLoadState !is LoadState.Success) return

        val diary = diaryLoadState.data
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigatePreviousFragment(date)
        }
    }

    // Viewクリック処理
    fun onDiaryEditMenuClick() {
        val currentUiState = uiState.value
        if (currentUiState.isInputDisabled) return
        val diaryLoadState = currentUiState.diaryLoadState
        if (diaryLoadState !is LoadState.Success) return

        val diary = diaryLoadState.data
        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(id, date)
            )
        }
    }

    fun onDiaryDeleteMenuClick() {
        val currentUiState = uiState.value
        if (currentUiState.isInputDisabled) return
        val diaryLoadState = currentUiState.diaryLoadState
        if (diaryLoadState !is LoadState.Success) return

        val diary = diaryLoadState.data
        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(DiaryId(id), date)
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onNavigationIconClick() {
        val currentUiState = uiState.value
        if (currentUiState.isInputDisabled) return
        val diaryLoadState = currentUiState.diaryLoadState
        if (diaryLoadState !is LoadState.Success) return

        val diary = diaryLoadState.data
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
                        DiaryShowEvent.NavigatePreviousFragmentOnDiaryLoadFailed()
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
                        updateToDiaryLoadErrorState(ErrorType.Failure(result.exception))
                        emitUiEvent(DiaryShowEvent.NavigateDiaryLoadFailureDialog(date))
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        updateToDiaryLoadErrorState(ErrorType.Unexpected(result.exception))
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
                    DiaryShowEvent.NavigatePreviousFragmentOnDiaryDeleted(
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

    private fun updateToDiaryLoadErrorState(errorType: ErrorType) {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Error(errorType),
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
