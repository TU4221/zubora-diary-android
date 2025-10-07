package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
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
        val uuidString = UUIDString(id)
        val date = handle.get<LocalDate>(DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        viewModelScope.launch {
            loadSavedDiary(uuidString, date)
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
        val id = diaryStateFlow.id.requireValue()
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(id.value, date)
            )
        }
    }

    fun onDiaryDeleteMenuClick() {
        if (uiState.value != DiaryShowState.LoadSuccess) return

        val id = diaryStateFlow.id.requireValue()
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            updatePendingDiaryDeleteParameters(id, date)
            emitUiEvent(
                DiaryShowEvent.NavigateDiaryDeleteDialog(date)
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
        viewModelScope.launch {
            parameters?.let {
                deleteDiary(it.id, it.date)
            } ?: throw IllegalStateException()
        }
    }

    // データ処理
    private suspend fun loadSavedDiary(id: UUIDString, date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Loading)
        when (val result = loadDiaryByIdUseCase(id)) {
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

    private suspend fun deleteDiary(id: UUIDString, date: LocalDate) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateUiState(DiaryShowState.Deleting)
        when (val result = deleteDiaryUseCase(id)) {
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
                val app =
                    when (result.exception) {
                        is DiaryDeleteException.DiaryDataDeleteFailure,
                        is DiaryDeleteException.Unknown -> DiaryShowAppMessage.DiaryDeleteFailure
                        is DiaryDeleteException.ImageFileDeleteFailure -> DiaryShowAppMessage.DiaryImageDeleteFailure
                    }
                emitAppMessageEvent(app)
            }
        }
    }

    // FragmentAction関係
    private suspend fun navigatePreviousFragment(diaryDate: LocalDate) {
        emitNavigatePreviousFragmentEvent(
            FragmentResult.Some(diaryDate)
        )
    }

    private fun updatePendingDiaryDeleteParameters(id: UUIDString, date: LocalDate) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, date)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private data class DiaryDeleteParameters(
        val id: UUIDString,
        val date: LocalDate
    )
}
