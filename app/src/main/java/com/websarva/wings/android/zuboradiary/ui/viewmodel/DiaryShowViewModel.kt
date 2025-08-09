package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
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
    private val loadDiaryUseCase: LoadDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : BaseDiaryShowViewModel<DiaryShowEvent, DiaryShowAppMessage, DiaryShowState>(
    DiaryShowState.Idle
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val DATE_ARGUMENT_KEY = "date"
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

    init {
        initializeDiaryData(handle)
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        val date = handle.get<LocalDate>(DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        viewModelScope.launch {
            loadSavedDiary(date)
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

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            navigatePreviousFragment(date)
        }
    }

    // Viewクリック処理
    fun onDiaryEditMenuClick() {
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(date)
            )
        }
    }

    fun onDiaryDeleteMenuClick() {
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

    fun onNavigationIconClick() {
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            navigatePreviousFragment(date)
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                viewModelScope.launch {
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
                handleDiaryDeleteDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                check(uiState.value == DiaryShowState.LoadSuccess)
            }
        }
    }

    private fun handleDiaryDeleteDialogPositiveResult() {
        viewModelScope.launch {
            deleteDiary()
        }
    }

    // データ処理
    override suspend fun loadSavedDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Loading)
        when (val result = loadDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateUiState(DiaryShowState.LoadSuccess)
                val diary = result.value
                updateDiary(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadError)
                emitUiEvent(
                    DiaryShowEvent.NavigateDiaryLoadFailureDialog(date)
                )
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
                emitUiEvent(
                    DiaryShowEvent.NavigatePreviousFragmentOnDiaryDeleted(
                        FragmentResult.Some(date)
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateUiState(DiaryShowState.LoadSuccess)
                emitAppMessageEvent(DiaryShowAppMessage.DiaryDeleteFailure)
            }
        }
    }

    // FragmentAction関係
    private suspend fun navigatePreviousFragment(diaryDate: LocalDate) {
        emitNavigatePreviousFragmentEvent(
            FragmentResult.Some(diaryDate)
        )
    }
}
