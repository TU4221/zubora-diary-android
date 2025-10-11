package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryShowState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseDiaryShowViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseDiaryShowViewModel<DiaryShowEvent, DiaryShowAppMessage, DiaryShowState>(
    DiaryShowState.Idle,
    buildDiaryImageFilePathUseCase
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val ID_ARGUMENT_KEY = "load_diary_id"
        private const val DATE_ARGUMENT_KEY = "load_diary_date"
    }

    private val logTag = createLogTag()

    override val isProgressIndicatorVisible =
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

    // キャッシュパラメータ
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null

    init {
        initializeDiaryData(handle)
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        val id = handle.get<String>(ID_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        val date = handle.get<LocalDate>(DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            loadSavedDiary(id, date)
        }
    }

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
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

    override fun createUnexpectedAppMessage(e: Exception): DiaryShowAppMessage {
        return DiaryShowAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        val date = diaryStateFlow.date.requireValue()
        launchWithUnexpectedErrorHandler {
            navigatePreviousFragment(date)
        }
    }

    // Viewクリック処理
    fun onDiaryEditMenuClick() {
        val id = diaryStateFlow.id.requireValue()
        val date = diaryStateFlow.date.requireValue()
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(id, date)
            )
        }
    }

    fun onDiaryDeleteMenuClick() {
        if (uiState.value != DiaryShowState.LoadSuccess) return

        val id = diaryStateFlow.id.requireValue()
        val date = diaryStateFlow.date.requireValue()
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(id, date)
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onNavigationIconClick() {
        val date = diaryStateFlow.date.requireValue()
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
                check(uiState.value == DiaryShowState.LoadSuccess)
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
    private suspend fun loadSavedDiary(id: String, date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Loading)
        when (val result = loadDiaryByIdUseCase(DiaryId(id))) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateUiState(DiaryShowState.LoadSuccess)
                val diary = result.value
                updateDiary(diary.toUiModel())
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadError)
                when (result.exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        emitUiEvent(DiaryShowEvent.NavigateDiaryLoadFailureDialog(date))
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun deleteDiary(id: String, date: LocalDate) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Deleting)
        when (val result = deleteDiaryUseCase(DiaryId(id))) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                emitUiEvent(
                    DiaryShowEvent.NavigatePreviousFragmentOnDiaryDeleted(
                        FragmentResult.Some(date)
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadSuccess)
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

    private fun updatePendingDiaryDeleteParameters(id: String, date: LocalDate) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, date)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private data class DiaryDeleteParameters(
        val id: String,
        val date: LocalDate
    )
}
