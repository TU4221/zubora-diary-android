package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryShowState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val uriRepository: UriRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    private val initialDiaryShowState = DiaryShowState.Idle
    private val _diaryShowState = MutableStateFlow<DiaryShowState>(initialDiaryShowState)

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
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
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
    val picturePath
        get() = diaryStateFlow.picturePath.asStateFlow()
    val log
        get() = diaryStateFlow.log.asStateFlow()

    override fun initialize() {
        super.initialize()
        diaryStateFlow.initialize()
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    // ViewClicked処理
    fun onDiaryEditMenuClicked() {
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryShowEvent.NavigateDiaryEditFragment(date)
            )
        }
    }

    fun onDiaryDeleteMenuClicked() {
        val date = diaryStateFlow.date.requireValue()
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryShowEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    fun onNavigationClicked() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadingFailureDialogResultReceived(result: DialogResult<Unit>) {
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
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onDiaryDeleteDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryDeleteDialogPositiveResultReceived() {
        _diaryShowState.value = DiaryShowState.Deleting
        viewModelScope.launch {
            deleteDiary()
            _diaryShowState.value = DiaryShowState.Idle
        }
    }

    // View状態処理
    fun onCalendarDaySelected(date: LocalDate) {
        _diaryShowState.value = DiaryShowState.Loading
        viewModelScope.launch {
            prepareDiaryForCalendarFragment(date)
            _diaryShowState.value = DiaryShowState.Idle
        }
    }

    // Fragment状態処理
    fun onFragmentViewCreated(date: LocalDate) {
        _diaryShowState.value = DiaryShowState.Loading
        viewModelScope.launch {
            prepareDiaryForDiaryShowFragment(date)
            _diaryShowState.value = DiaryShowState.Idle
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

        try {
            val diaryEntity = diaryRepository.loadDiary(date) ?: throw IllegalArgumentException()
            diaryStateFlow.update(diaryEntity)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            if (ignoreAppMessage) {
                emitViewModelEvent(
                    DiaryShowEvent.NavigateDiaryLoadingFailureDialog(date)
                )
            } else {
                emitAppMessageEvent(DiaryShowAppMessage.DiaryLoadingFailure)
            }
        }

        Log.i(logTag, "${logMsg}_完了")
    }

    private suspend fun deleteDiary() {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        val date = diaryStateFlow.date.requireValue()
        val picturePath  = diaryStateFlow.picturePath.value
        try {
            diaryRepository.deleteDiary(date)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            emitAppMessageEvent(DiaryShowAppMessage.DiaryDeleteFailure)
            return
        }

        if (picturePath != null) uriRepository.releasePersistablePermission(picturePath)
        emitViewModelEvent(
            DiaryShowEvent
                .NavigatePreviousFragmentOnDiaryDelete(
                    FragmentResult.Some(date)
                )
        )
        Log.i(logTag, "${logMsg}_完了")
    }

    // FragmentAction関係
    private suspend fun navigatePreviousFragment() {
        val date = date.requireValue()
        emitViewModelEvent(
            DiaryShowEvent
                .NavigatePreviousFragment(
                    FragmentResult.Some(date)
                )
        )
    }
}
