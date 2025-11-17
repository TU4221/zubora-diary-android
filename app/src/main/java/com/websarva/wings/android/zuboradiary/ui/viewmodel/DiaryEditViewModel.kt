package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ClearDiaryImageCacheFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CacheDiaryImageUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheFileClearException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryUpdateConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.CheckWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryEditUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class DiaryEditViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val shouldRequestExitWithoutDiarySaveConfirmationUseCase: ShouldRequestExitWithoutDiarySaveConfirmationUseCase,
    private val shouldRequestDiaryLoadConfirmationUseCase: ShouldRequestDiaryLoadConfirmationUseCase,
    private val shouldRequestDiaryUpdateConfirmationUseCase: ShouldRequestDiaryUpdateConfirmationUseCase,
    private val shouldRequestWeatherInfoConfirmationUseCase: ShouldRequestWeatherInfoConfirmationUseCase,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val checkWeatherInfoFetchEnabledUseCase: CheckWeatherInfoFetchEnabledUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase,
    private val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val cacheDiaryImageUseCase: CacheDiaryImageUseCase,
    private val clearDiaryImageCacheFileUseCase: ClearDiaryImageCacheFileUseCase
) : BaseFragmentViewModel<DiaryEditUiState, DiaryEditUiEvent, DiaryEditAppMessage>(
    handle.get<DiaryEditUiState>(SAVED_STATE_UI_KEY)?.let {
        DiaryEditUiState.fromSavedState(it)
    } ?: DiaryEditUiState(editingDiary = Diary.generate().toUiModel())
) {


    //region Properties
    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && currentUiState.originalDiaryLoadState is LoadState.Success

    private val originalDiary
        get() = (currentUiState.originalDiaryLoadState as LoadState.Success).data

    private val editingDiaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.editingDiary == new.editingDiary
        }.map { it.editingDiary }

    // キャッシュパラメータ
    private var pendingDiaryLoadParameters: DiaryLoadParameters? = null
    private var pendingDiaryUpdateParameters: DiaryUpdateParameters? = null
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    private var pendingDiaryDateUpdateParameters: DiaryDateUpdateParameters? = null
    private var pendingDiaryItemDeleteParameters: DiaryItemDeleteParameters? = null
    private var pendingDiaryImageUpdateParameters: DiaryImageUpdateParameters? = null
    private var pendingWeatherInfoFetchParameters: WeatherInfoFetchParameters? = null
    private var pendingPreviousNavigationParameters: PreviousNavigationParameters? = null
    //endregion

    //region Initialization
    init {
        initializeDiaryData()
        collectUiStates()
    }

    private fun initializeDiaryData() {
        // MEMO:下記条件はアプリ設定変更時のアプリ再起動時の不要初期化対策
        if (handle.contains(SAVED_STATE_UI_KEY)) return
        val id = handle.get<String>(ARGUMENT_DIARY_ID_KEY)?.let { DiaryId(it) }
        val date =
            handle.get<LocalDate>(ARGUMENT_DIARY_DATE_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            prepareDiaryEntry(
                id,
                date
            )
        }
    }

    private fun collectUiStates() {
        collectUiState()
        collectWeather2Options()
        collectWeather2Enabled()
        collectNumVisibleDiaryItems()
        collectDiaryItemAdditionEnabled()
        collectImageFilePath()
    }

    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    private fun collectWeather2Options() {
        uiState.distinctUntilChanged { old, new ->
            old.editingDiary.weather1 == new.editingDiary.weather1
        }.map { state ->
            WeatherUi.entries.filter { weather ->
                weather != state.editingDiary.weather1
            }
        }.distinctUntilChanged().onEach { options ->
            updateWeather2Options(options)
        }.launchIn(viewModelScope)
    }

    private fun collectWeather2Enabled() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
            old.weather1 == new.weather1 && old.weather2 == new.weather2
        }.map { editingDiary ->
            when (editingDiary.weather1) {
                WeatherUi.UNKNOWN -> false
                else -> {
                    editingDiary.weather1 != editingDiary.weather2
                }
            }
        }.distinctUntilChanged().onEach { isEnabled ->
            if (isEnabled) {
                updateToWeather2EnabledState()
            } else {
                updateToWeather2DisabledState()
            }
        }.launchIn(viewModelScope)
    }

    private fun collectNumVisibleDiaryItems() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
            old.itemTitles == new.itemTitles
        }.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it.itemTitles)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateNumVisibleItems(numVisibleDiaryItems)
            emitUiEvent(
                DiaryEditUiEvent.UpdateDiaryItemLayout(numVisibleDiaryItems)
            )
        }.launchIn(viewModelScope)
    }

    private fun collectDiaryItemAdditionEnabled() {
        uiState.distinctUntilChanged { old, new ->
            old.isInputDisabled == new.isInputDisabled
                    && old.numVisibleDiaryItems == new.numVisibleDiaryItems
        }.map {
            !it.isInputDisabled && it.numVisibleDiaryItems < DiaryItemNumber.MAX_NUMBER
        }.distinctUntilChanged().onEach { isEnabled ->
            updateIsDiaryItemAdditionEnabled(isEnabled)
        }.launchIn(viewModelScope)
    }

    private fun collectImageFilePath() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
            old.imageFileName == new.imageFileName
        }.map {
            diaryUiStateHelper.buildImageFilePath(it.imageFileName)
        }.catchUnexpectedError(
            FilePathUi.Unavailable
        ).distinctUntilChanged().onEach { path ->
            updateDiaryImageFilePath(path)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region UI Event Handlers - Action
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    // Viewクリック処理
    internal fun onDiarySaveMenuClick() {
        if (!isReadyForOperation) return

        updateLog(LocalDateTime.now())
        val diary = currentUiState.editingDiary.toDomainModel()
        val diaryItemTitleSelectionHistoryList =
            currentUiState.diaryItemTitleSelectionHistories
                .values.filterNotNull().map { it.toDomainModel() }
        val originalDiary = originalDiary.toDomainModel()
        val isNewDiary = currentUiState.isNewDiary
        launchWithUnexpectedErrorHandler {
            requestDiaryUpdateConfirmation(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        }
    }

    internal fun onDiaryDeleteMenuClick() {
        if (!isReadyForOperation) return
        if (currentUiState.isNewDiary) return

        val originalDiaryId = DiaryId(originalDiary.id)
        val originalDiaryDate = originalDiary.date
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                originalDiaryId,
                originalDiaryDate
            )
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryDeleteDialog(originalDiaryDate)
            )
        }

    }

    fun onNavigationClick() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    fun onDateInputFieldClick() {
        if (!isReadyForOperation) return

        val date = currentUiState.editingDiary.date
        val originalDate = originalDiary.date
        val isNewDiary = currentUiState.isNewDiary
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDateUpdateParameters(originalDate, isNewDiary)
            emitUiEvent(
                DiaryEditUiEvent.NavigateDatePickerDialog(date)
            )
        }
    }

    fun onWeather1InputFieldItemClick(position: Int) {
        val selectWeather = currentUiState.weather1Options[position]
        updateWeather1(selectWeather)
    }

    fun onWeather2InputFieldItemClick(position: Int) {
        val selectWeather = currentUiState.weather2Options[position]
        updateWeather2(selectWeather)
    }

    fun onConditionInputFieldItemClick(position: Int) {
        val selectCondition = currentUiState.conditionOptions[position]
        updateCondition(selectCondition)
    }

    fun onTitleTextChanged(text: CharSequence) {
        updateTitle(text.toString())
    }

    fun onItemTitleInputFieldClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemTitleId = null // MEMO:日記項目タイトルIDは受取用でここでは不要の為、nullとする。
        val itemTitle =
            currentUiState.editingDiary.itemTitles[itemNumberInt] ?: throw IllegalStateException()

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryItemTitleEditFragment(
                    DiaryItemTitleSelectionUi(
                        itemNumberInt,
                        itemTitleId,
                        itemTitle
                    )
                )
            )
        }
    }

    fun onItemTitleTextChanged(itemNumberInt: Int, text: CharSequence) {
        updateItemTitle(
            itemNumberInt,
            text.toString()
        )
    }

    fun onItemAdditionButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            addDiaryItem()
        }
    }

    fun onItemCommentTextChanged(itemNumberInt: Int, text: CharSequence) {
        updateItemComment(
            itemNumberInt,
            text.toString()
        )
    }

    fun onItemDeleteButtonClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryItemDeleteParameters(itemNumber)
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryItemDeleteDialog(itemNumberInt)
            )
        }
    }

    fun onAttachedImageDeleteButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryImageDeleteDialog
            )
        }
    }

    fun onAttachedImageClick() {
        if (!isReadyForOperation) return

        val diaryId = currentUiState.editingDiary.id.let { DiaryId(it) }
        launchWithUnexpectedErrorHandler {
            selectImage(diaryId)
        }
    }

    internal fun onDiaryItemInvisibleStateTransitionCompleted(itemNumberInt: Int) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        deleteItem(itemNumber)
    }

    internal fun onDiaryItemVisibleStateTransitionCompleted() {
        updateToIdleState()
    }
    //endregion

    //region UI Event Handlers - Results
    internal fun onDiaryLoadDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryLoadDialogPositiveResult(pendingDiaryLoadParameters)
            }
            is DialogResult.Negative,
            is DialogResult.Cancel -> {
                handleDiaryLoadDialogNegativeResult(pendingDiaryLoadParameters)
            }
        }
        clearPendingDiaryLoadParameters()
    }

    private fun handleDiaryLoadDialogPositiveResult(parameters: DiaryLoadParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                loadDiaryByDate(it.date)
            } ?: throw IllegalStateException()
        }
    }

    private fun handleDiaryLoadDialogNegativeResult(parameters: DiaryLoadParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                fetchWeatherInfo(it.date, it.previousDate)
            } ?: throw IllegalStateException()
        }
    }

    internal fun onDiaryUpdateDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryUpdateDialogPositiveResult(pendingDiaryUpdateParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryUpdateParameters()
    }

    private fun handleDiaryUpdateDialogPositiveResult(parameters: DiaryUpdateParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                saveDiary(
                    it.diary,
                    it.diaryItemTitleSelectionHistoryList,
                    it.originalDiary,
                    it.isNewDiary
                )
            } ?: throw IllegalStateException()
        }
    }

    internal fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
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

    internal fun onDatePickerDialogResultReceived(result: DialogResult<LocalDate>) {
        when (result) {
            is DialogResult.Positive<LocalDate> -> {
                handleDatePickerDialogPositiveResult(
                    result.data,
                    pendingDiaryDateUpdateParameters
                )
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryDateUpdateParameters()
    }

    private fun handleDatePickerDialogPositiveResult(
        date: LocalDate,
        parameters: DiaryDateUpdateParameters?
    ) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                processChangedDiaryDate(date, it.originalDate, it.isNewDiary)
            } ?: throw IllegalStateException()
        }
    }

    internal fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                launchWithUnexpectedErrorHandler {
                    emitUiEvent(
                        DiaryEditUiEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed()
                    )
                }
            }
        }
    }

    internal fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleWeatherInfoFetchDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                clearPendingWeatherInfoFetchParameters()
            }
        }
    }

    private fun handleWeatherInfoFetchDialogPositiveResult() {
        launchWithUnexpectedErrorHandler {
            checkPermissionBeforeWeatherInfoFetch()
        }
    }

    internal fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryItemDeleteDialogPositiveResult(pendingDiaryItemDeleteParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryItemDeleteParameters()
    }

    private fun handleDiaryItemDeleteDialogPositiveResult(parameters: DiaryItemDeleteParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                requestDiaryItemDeleteTransition(it.itemNumber)
            } ?: throw IllegalStateException()
        }
    }

    internal fun onDiaryImageDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleDiaryImageDeleteDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDiaryImageDeleteDialogPositiveResult() {
        launchWithUnexpectedErrorHandler {
            deleteImage()
        }
    }

    internal fun onExitWithoutDiarySaveDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleExitWithoutDiarySaveDialogPositiveResult(pendingPreviousNavigationParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingPreviousNavigationParameters()
    }

    private fun handleExitWithoutDiarySaveDialogPositiveResult(
        parameters: PreviousNavigationParameters?
    ) {
        launchWithUnexpectedErrorHandler {
            clearDiaryImageCacheFile()
            parameters?.let {
                navigatePreviousFragment(it.originalDiaryDate)
            } ?: throw IllegalStateException()
        }
    }

    internal fun onItemTitleEditFragmentResultReceived(result: FragmentResult<DiaryItemTitleSelectionUi>) {
        when (result) {
            is FragmentResult.Some -> {
                updateItemTitle(result.data)
            }
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    // MEMO:未選択時null
    internal fun onOpenDocumentResultImageUriReceived(uri: Uri?) {
        val parameters = pendingDiaryImageUpdateParameters
        clearPendingDiaryImageUpdateParameters()
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                cacheDiaryImage(uri, parameters.id)
            } ?: throw IllegalStateException()
        }
    }
    //endregion

    //region UI Event Handlers - Permissions
    internal fun onAccessLocationPermissionChecked(
        isGranted: Boolean
    ) {
        val parameters = pendingWeatherInfoFetchParameters
        clearPendingWeatherInfoFetchParameters()
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                executeFetchWeatherInfo(isGranted, it.date)
            } ?: throw IllegalStateException()
        }
    }
    //endregion

    //region Business Logic
    private suspend fun prepareDiaryEntry(
        id: DiaryId?,
        date: LocalDate
    ) {
        if (id == null) {
            prepareNewDiaryEntry(date)
        } else {
            loadDiaryById(id, date)
        }
    }

    private suspend fun prepareNewDiaryEntry(date: LocalDate) {
        updateToNewDiaryState(date)
        val previousDate = currentUiState.previousSelectedDate
        val originalDate = originalDiary.date
        val isNewDiary = currentUiState.isNewDiary
        requestDiaryLoadConfirmation(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    private suspend fun loadDiaryById(id: DiaryId, date: LocalDate) {
        executeDiaryLoad(
            id,
            date,
            { id, _ ->
                id ?: throw IllegalArgumentException()
                loadDiaryByIdUseCase(id)
            },
            { exception ->
                when (exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun loadDiaryByDate(date: LocalDate) {
        executeDiaryLoad(
            date = date,
            executeLoadDiary = { _, date ->
                loadDiaryByDateUseCase(date)
            },
            emitAppMessageOnFailure = { exception ->
                when (exception) {
                    is DiaryLoadByDateException.LoadFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByDateException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    private suspend fun <E : UseCaseException> executeDiaryLoad(
        id: DiaryId? = null,
        date: LocalDate,
        executeLoadDiary: suspend (DiaryId?, LocalDate) -> UseCaseResult<Diary, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        val previousState = currentUiState
        Log.i(logTag, "${logMsg}_previousState: $previousState")
        updateToDiaryLoadingState()
        when (val result = executeLoadDiary(id, date)) {
            is UseCaseResult.Success -> {
                updateToDiaryLoadSuccessState(result.value)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                if (previousState.originalDiaryLoadState == LoadState.Idle) {
                    updateToDiaryLoadErrorState()

                    // MEMO:連続するUIイベント（エラー表示と画面遷移）は、監視開始前に発行されると
                    //      取りこぼされる可能性がある。これを防ぐため、間に確認ダイアログを挟み、
                    //      ユーザーの応答を待ってから画面遷移を実行する。
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryLoadFailureDialog(date)
                    )
                } else {
                    updateUiState { previousState }
                    emitAppMessageOnFailure(result.exception)
                }
            }
        }
    }

    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        updateToProcessingState()
        val result =
            saveDiaryUseCase(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateToIdleState()
                clearDiaryImageCacheFile()
                emitUiEvent(
                    DiaryEditUiEvent
                        .NavigateDiaryShowFragment(diary.id.value, diary.date)
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiarySaveException.SaveFailure -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiarySaveException.InsufficientStorage -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveInsufficientStorageFailure
                        )
                    }
                    is DiarySaveException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    private suspend fun deleteDiary(
        id: DiaryId,
        date: LocalDate
    ) {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        updateToProcessingState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateToIdleState()
                clearDiaryImageCacheFile()
                emitUiEvent(
                    DiaryEditUiEvent
                        .NavigatePreviousFragmentOnDiaryDelete(
                            FragmentResult.Some(date)
                        )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    private suspend fun requestDiaryLoadConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        updateToProcessingState()
        val result =
            shouldRequestDiaryLoadConfirmationUseCase(date, previousDate, originalDate, isNewDiary)
        when (result) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                if (result.value) {
                    updatePendingDiaryLoadParameters(date, previousDate)
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryLoadDialog(date)
                    )
                } else {
                    fetchWeatherInfo(date, previousDate)
                }
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is DiaryLoadConfirmationCheckException.CheckFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryLoadConfirmationCheckException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun requestDiaryUpdateConfirmation(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        updateToProcessingState()
        val date = diary.date
        val originalDate = originalDiary.date
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, originalDate, isNewDiary)) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                if (result.value) {
                    updatePendingDiaryUpdateParameters(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryUpdateDialog(diary.date)
                    )
                } else {
                    saveDiary(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                }
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is DiaryUpdateConfirmationCheckException.CheckFailure -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiaryUpdateConfirmationCheckException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    // 天気情報取得関係
    private suspend fun fetchWeatherInfo(date: LocalDate, previousDate: LocalDate?) {
        val isEnabled = checkWeatherInfoFetchEnabledUseCase().value
        if (!isEnabled) return

        requestWeatherInfoConfirmation(date, previousDate)
    }

    private suspend fun requestWeatherInfoConfirmation(
        date: LocalDate,
        previousDate: LocalDate?
    ) {
        val shouldRequest =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate).value
        if (shouldRequest) {
            updatePendingWeatherInfoFetchParameters(date)
            emitUiEvent(
                DiaryEditUiEvent.NavigateWeatherInfoFetchDialog(date)
            )
        } else {
            val shouldLoad = shouldFetchWeatherInfoUseCase(date, previousDate).value
            if (!shouldLoad) return

            updatePendingWeatherInfoFetchParameters(date)
            checkPermissionBeforeWeatherInfoFetch()
        }
    }

    private suspend fun checkPermissionBeforeWeatherInfoFetch() {
        emitUiEvent(
            DiaryEditUiEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch
        )
    }

    private suspend fun executeFetchWeatherInfo(
        isGranted: Boolean,
        date: LocalDate
    ) {
        if (!isGranted) {
            updateToIdleState()
            emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
        }

        updateToProcessingState()
        when (val result = fetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                updateWeather1(result.value.toUiModel())
                updateWeather2(Weather.UNKNOWN.toUiModel())
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is WeatherInfoFetchException.LocationPermissionNotGranted -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.AccessLocationPermissionRequest
                        )
                    }
                    is WeatherInfoFetchException.DateOutOfRange -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is WeatherInfoFetchException.LocationAccessFailure,
                    is WeatherInfoFetchException.FetchFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                    is WeatherInfoFetchException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    // 日付関係
    private suspend fun processChangedDiaryDate(
        date: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        updateDate(date)
        val previousDate = currentUiState.previousSelectedDate
        // MEMO:下記処理をdate(StateFlow)変数のCollectorから呼び出すと、
        //      画面回転時にも不要に呼び出してしまう為、下記にて処理。
        requestDiaryLoadConfirmation(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    // 項目関係
    // MEMO:日記項目追加処理完了時のUi更新(編集中)は日記項目追加完了イベントメソッドにて処理
    private suspend fun addDiaryItem() {
        updateToInputDisabledState()
        emitUiEvent(DiaryEditUiEvent.ItemAddition)
        val numVisibleItems = currentUiState.numVisibleDiaryItems
        val additionItemNumber = numVisibleItems + 1
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemTitles = it.editingDiary.itemTitles + (additionItemNumber to ""),
                    itemComments = it.editingDiary.itemComments + (additionItemNumber to "")
                )
            )
        }
    }

    // MEMO:日記項目削除処理完了時のUi更新(編集中)は日記項目削除メソッドにて処理
    private suspend fun requestDiaryItemDeleteTransition(itemNumber: DiaryItemNumber) {
        val numVisibleItems = currentUiState.numVisibleDiaryItems

        updateToInputDisabledState()
        if (itemNumber.isMinNumber && itemNumber.value == numVisibleItems) {
            deleteItem(itemNumber)
        } else {
            emitUiEvent(
                DiaryEditUiEvent.TransitionDiaryItemToInvisibleState(itemNumber.value)
            )
        }
    }

    // MEMO:日記項目削除処理開始時のUi更新(項目削除中)は日記項目削除トランジション要求メソッドにて処理
    private fun deleteItem(itemNumber: DiaryItemNumber) {
        val currentEditingDiary = currentUiState.editingDiary
        val updateItemTitles = currentEditingDiary.itemTitles.toMutableMap()
        val updateItemComments = currentEditingDiary.itemComments.toMutableMap()
        val updateHistories = currentUiState.diaryItemTitleSelectionHistories.toMutableMap()

        if (itemNumber.isMinNumber) {
            updateItemTitles[itemNumber.value] = ""
            updateItemComments[itemNumber.value] = ""
        } else {
            updateItemTitles[itemNumber.value] = null
            updateItemComments[itemNumber.value] = null
        }
        updateHistories[itemNumber.value] = null

        val currentNumVisibleItems = currentUiState.numVisibleDiaryItems
        if (itemNumber.value < currentNumVisibleItems) {
            for (i in itemNumber.value until currentNumVisibleItems) {
                val targetItemNumber = i
                val nextItemNumber = targetItemNumber.inc()

                updateItemTitles[targetItemNumber] = updateItemTitles[nextItemNumber]
                updateItemTitles[nextItemNumber] = null
                updateItemComments[targetItemNumber] = updateItemComments[nextItemNumber]
                updateItemComments[nextItemNumber] = null
                updateHistories[targetItemNumber] = updateHistories[nextItemNumber]
                updateHistories[nextItemNumber] = null
            }
        }

        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemTitles = updateItemTitles,
                    itemComments = updateItemComments
                ),
                diaryItemTitleSelectionHistories = updateHistories
            )
        }
        updateToIdleState()
    }

    // 添付画像関係
    // MEMO:画像選択完了時のUi更新(編集中)は画像選択完了イベントメソッドにて処理
    private suspend fun selectImage(diaryId: DiaryId) {
        updateToProcessingState()
        updatePendingDiaryImageUpdateParameters(diaryId)
        emitUiEvent(DiaryEditUiEvent.SelectImage)
    }

    private suspend fun deleteImage() {
        updateImageFileName(null)
        clearDiaryImageCacheFile()
    }

    private suspend fun cacheDiaryImage(uri: Uri?, diaryId: DiaryId) {
        if (uri != null) {
            val result =
                cacheDiaryImageUseCase(uri.toString(), diaryId)
            when (result) {
                is UseCaseResult.Success -> {
                    updateImageFileName(result.value.fullName)
                }
                is UseCaseResult.Failure -> {
                    when (result.exception) {
                        is DiaryImageCacheException.CacheFailure -> {
                            emitAppMessageEvent(
                                DiaryEditAppMessage.ImageLoadFailure
                            )
                        }
                        is DiaryImageCacheException.InsufficientStorage -> {
                            emitAppMessageEvent(
                                DiaryEditAppMessage.ImageLoadInsufficientStorageFailure
                            )
                        }
                        is DiaryImageCacheException.Unknown -> {
                            emitUnexpectedAppMessage(result.exception)
                        }
                    }
                }
            }
        }
        updateToIdleState()
    }

    private suspend fun clearDiaryImageCacheFile() {
        updateToProcessingState()
        when (val result = clearDiaryImageCacheFileUseCase()) {
            is UseCaseResult.Success -> {
                updateToIdleState()
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "画像キャッシュファイルクリア失敗", result.exception)
                updateToIdleState()
                when (result.exception) {
                    is DiaryImageCacheFileClearException.ClearFailure -> {
                        // ユーザーには直接関わらない処理の為、通知不要
                    }
                    is DiaryImageCacheFileClearException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun handleBackNavigation(
        diary: Diary,
        originalDiary: Diary
    ) {
        val shouldRequest = shouldRequestExitWithoutDiarySaveConfirmationUseCase(
            diary,
            originalDiary
        ).value
        if (shouldRequest) {
            updatePendingPreviousNavigationParameter(originalDiary.date)
            emitUiEvent(
                DiaryEditUiEvent.NavigateExitWithoutDiarySaveConfirmationDialog
            )
        } else {
            clearDiaryImageCacheFile()
            navigatePreviousFragment(originalDiary.date)
        }
    }

    private suspend fun navigatePreviousFragment(originalDiaryDate: LocalDate) {
        emitNavigatePreviousFragmentEvent(
            FragmentResult.Some(originalDiaryDate)
        )
    }
    //endregion

    //region UI State Update - Property
    private fun updateDate(date: LocalDate) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(date = date),
                previousSelectedDate = it.editingDiary.date
            )
        }
    }

    private fun updateTitle(title: String) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(title = title)
            )
        }
    }

    private fun updateWeather1(weather: WeatherUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather1 = weather)
            )
        }
    }

    private fun updateWeather2(weather: WeatherUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather2 = weather)
            )
        }
    }

    private fun updateCondition(condition: ConditionUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(condition = condition)
            )
        }
    }

    private fun updateNumVisibleItems(num: Int) {
        updateUiState { it.copy(numVisibleDiaryItems = num) }
    }

    private fun updateItemTitle(itemNumberInt: Int, title: String) {
        updateUiState {
            it.copy(
                editingDiary =
                    it.editingDiary.copy(
                        itemTitles = it.editingDiary.itemTitles + (itemNumberInt to title)
                    )
            )
        }
    }

    private fun updateItemTitle(selection: DiaryItemTitleSelectionUi) {
        val itemNumberInt = selection.itemNumber
        val title = selection.title
        val updateHistory = selection.let {
            DiaryItemTitleSelectionHistoryUi(
                it.id ?: throw IllegalStateException(),
                it.title,
                LocalDateTime.now()
            )
        }
        updateUiState {
            it.copy(
                editingDiary =
                    it.editingDiary.copy(
                        itemTitles = it.editingDiary.itemTitles + (itemNumberInt to title)
                    ),
                diaryItemTitleSelectionHistories =
                    it.diaryItemTitleSelectionHistories + (itemNumberInt to updateHistory)
            )
        }
    }

    private fun updateItemComment(
        itemNumberInt: Int,
        comment: String
    ) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemComments = it.editingDiary.itemComments + (itemNumberInt to comment)
                )
            )
        }
    }

    private fun updateImageFileName(imageFileName: String?) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(imageFileName = imageFileName)
            )
        }
    }

    private fun updateLog(log: LocalDateTime) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(log = log)
            )
        }
    }

    private fun updateWeather2Options(options: List<WeatherUi>) {
        updateUiState { it.copy(weather2Options = options) }
    }

    private fun updateIsDiaryItemAdditionEnabled(isEnabled: Boolean) {
        updateUiState { it.copy(isDiaryItemAdditionEnabled = isEnabled) }
    }

    private fun updateDiaryImageFilePath(path: FilePathUi?) {
        updateUiState { it.copy(diaryImageFilePath = path) }
    }
    //endregion

    //region UI State Update - State
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToInputDisabledState() {
        updateUiState {
            it.copy(
                isInputDisabled = true
            )
        }
    }

    private fun updateToNewDiaryState(date: LocalDate) {
        val newDiary = Diary.generate().toUiModel().copy(date = date)
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Success(newDiary),
                editingDiary = newDiary,
                isNewDiary = true
            )
        }
    }private fun updateToDiaryLoadingState() {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToDiaryLoadSuccessState(diary: Diary) {
        val diaryUi = diary.toUiModel()
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Success(diaryUi),
                editingDiary = diaryUi,
                isNewDiary = false,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToDiaryLoadErrorState() {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToWeather2EnabledState() {
        updateUiState { it.copy(isWeather2Enabled = true) }
    }

    private fun updateToWeather2DisabledState() {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather2 = WeatherUi.UNKNOWN),
                isWeather2Enabled = false
            )
        }
    }
    //endregion

    //region Pending Diary Load Parameters
    private fun updatePendingDiaryLoadParameters(date: LocalDate, previousDate: LocalDate?) {
        pendingDiaryLoadParameters = DiaryLoadParameters(date, previousDate)
    }

    private fun clearPendingDiaryLoadParameters() {
        pendingDiaryLoadParameters = null
    }

    private data class DiaryLoadParameters(
        val date: LocalDate,
        val previousDate: LocalDate?
    )
    //endregion

    //region Pending Diary Update Parameters
    private fun updatePendingDiaryUpdateParameters(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        pendingDiaryUpdateParameters =
            DiaryUpdateParameters(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
    }

    private fun clearPendingDiaryUpdateParameters() {
        pendingDiaryUpdateParameters = null
    }

    private data class DiaryUpdateParameters(
        val diary: Diary,
        val diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        val originalDiary: Diary,
        val isNewDiary: Boolean
    )
    //endregion

    //region Pending Diary Delete Parameters
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
    //endregion

    //region Pending Diary Date Update Parameters
    private fun updatePendingDiaryDateUpdateParameters(
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        pendingDiaryDateUpdateParameters = DiaryDateUpdateParameters(originalDate, isNewDiary)
    }

    private fun clearPendingDiaryDateUpdateParameters() {
        pendingDiaryDateUpdateParameters = null
    }

    private data class DiaryDateUpdateParameters(
        val originalDate: LocalDate,
        val isNewDiary: Boolean
    )
    //endregion

    //region Pending Previous Navigation Parameters
    private fun updatePendingPreviousNavigationParameter(originalDiaryDate: LocalDate) {
        pendingPreviousNavigationParameters = PreviousNavigationParameters(originalDiaryDate)
    }

    private fun clearPendingPreviousNavigationParameters() {
        pendingPreviousNavigationParameters = null
    }

    private data class DiaryItemDeleteParameters(
        val itemNumber: DiaryItemNumber
    )
    //endregion

    //region Pending Weather Info Fetch Parameters
    private fun updatePendingWeatherInfoFetchParameters(date: LocalDate) {
        pendingWeatherInfoFetchParameters = WeatherInfoFetchParameters(date)
    }

    private fun clearPendingWeatherInfoFetchParameters() {
        pendingWeatherInfoFetchParameters = null
    }

    private data class DiaryImageUpdateParameters(val id: DiaryId)
    //endregion

    //region Pending Diary Item Delete Parameters
    private fun updatePendingDiaryItemDeleteParameters(itemNumber: DiaryItemNumber) {
        pendingDiaryItemDeleteParameters = DiaryItemDeleteParameters(itemNumber)
    }

    private fun clearPendingDiaryItemDeleteParameters() {
        pendingDiaryItemDeleteParameters = null
    }

    private data class WeatherInfoFetchParameters(
        val date: LocalDate
    )
    //endregion

    //region Pending Diary Image Update Parameters
    private fun updatePendingDiaryImageUpdateParameters(diaryId: DiaryId) {
        pendingDiaryImageUpdateParameters = DiaryImageUpdateParameters(diaryId)
    }

    private fun clearPendingDiaryImageUpdateParameters() {
        pendingDiaryImageUpdateParameters = null
    }

    private data class PreviousNavigationParameters(
        val originalDiaryDate: LocalDate
    )
    //endregion

    private companion object {
        const val ARGUMENT_DIARY_ID_KEY = "diary_id"
        const val ARGUMENT_DIARY_DATE_KEY = "diary_date"

        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }

    //region For Test
    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    fun test() {
        launchWithUnexpectedErrorHandler {
            isTesting = true
            val startDate = currentUiState.editingDiary.date
            for (i in 0 until 10) {
                val saveDate = startDate.minusDays(i.toLong())

                when (val result = doesDiaryExistUseCase(saveDate)) {
                    is UseCaseResult.Success -> {
                        if (result.value) continue
                    }
                    is UseCaseResult.Failure -> {
                        when (result.exception) {
                            is DiaryExistenceCheckException.CheckFailure -> {
                                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
                            }
                            is DiaryExistenceCheckException.Unknown -> {
                                emitUnexpectedAppMessage(result.exception)
                            }
                        }
                        isTesting = false
                        return@launchWithUnexpectedErrorHandler
                    }
                }
                updateUiState {
                    DiaryEditUiState(editingDiary = Diary.generate().toUiModel())
                }
                updateToNewDiaryState(saveDate)

                // ランダムな天気と体調を設定
                val weather1Options = uiState.value.weather1Options
                updateWeather1(weather1Options.random())
                val weather2Options = uiState.value.weather2Options
                updateWeather2(weather2Options.random())
                val conditionOptions = uiState.value.conditionOptions
                updateCondition(conditionOptions.random())

                // パターン化された日記データを取得
                val testData = getTestDiaryDataPattern(i)

                // 日記タイトルを設定
                updateTitle(testData.diaryTitle)

                // 日記項目数と内容を設定
                updateNumVisibleItems(testData.items.size)
                testData.items.forEachIndexed { index, item ->
                    val diaryItemNumber = index + 1
                    val selection = DiaryItemTitleSelectionUi(
                        diaryItemNumber,
                        DiaryItemTitleSelectionHistoryId.generate().value,
                        item.itemTitle
                    )
                    updateItemTitle(selection)
                    updateItemComment(diaryItemNumber, item.itemComment)
                }


                val diary = currentUiState.editingDiary.toDomainModel()
                val diaryItemTitleSelectionHistoryList =
                    currentUiState.diaryItemTitleSelectionHistories
                        .values.filterNotNull().map { it.toDomainModel() }
                val originalDiary = originalDiary.toDomainModel()
                val isNewDiary = currentUiState.isNewDiary

                val result =
                    saveDiaryUseCase(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                when (result) {
                    is UseCaseResult.Success -> {
                        // 処理なし
                    }
                    is UseCaseResult.Failure -> {
                        isTesting = false
                        updateToIdleState()
                        when (result.exception) {
                            is DiarySaveException.SaveFailure -> {
                                emitAppMessageEvent(
                                    DiaryEditAppMessage.DiarySaveFailure
                                )
                            }
                            is DiarySaveException.InsufficientStorage -> {
                                emitAppMessageEvent(
                                    DiaryEditAppMessage.DiarySaveInsufficientStorageFailure
                                )
                            }
                            is DiarySaveException.Unknown -> emitUnexpectedAppMessage(result.exception)
                        }
                        return@launchWithUnexpectedErrorHandler
                    }
                }
            }
            clearDiaryImageCacheFile()
            navigatePreviousFragment(originalDiary.date)
            isTesting = false
        }
    }

    private fun generateRandomAlphanumericString(length: Int): String {
        require(length >= 0) { "Length must be non-negative" }

        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * テスト用の日記データ一式を保持するデータクラス。
     */
    data class TestDiaryData(
        val diaryTitle: String,
        val items: List<TestDiaryItem>
    )

    /**
     * テスト用の日記項目データ一式を保持するデータクラス。
     */
    data class TestDiaryItem(
        val itemTitle: String,
        val itemComment: String
    )

    /**
     * 関連性のある日記データ一式のセットを、指定されたパターン番号で取得する。
     * @param patternNumber 0から29の間のパターン番号。
     * @return 指定されたパターンの日記データセット。
     */
    private fun getTestDiaryDataPattern(patternNumber: Int): TestDiaryData {
        // 30パターンの日記データテンプレート
        val diaryTemplates = listOf(
            // パターン0: カフェ巡り (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("新しいカフェを発見", "駅裏の路地を入ったところに、雰囲気の良いカフェを見つけた。コーヒーが絶品。"),
                    TestDiaryItem("読書タイム", "買ったばかりの小説を読み進めた。物語の世界に没頭できた。"),
                    TestDiaryItem("今日のケーキ", "チーズケーキを注文。濃厚でクリーミー、最高の味だった。")
                )
                TestDiaryData("お気に入りのカフェ巡り", items)
            },
            // パターン1: 映画鑑賞 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("話題のSF大作", "映像美がとにかく凄かった。特に宇宙船のシーンは圧巻。"),
                    TestDiaryItem("ストーリーの感想", "少し難解な部分もあったけど、伏線が回収されていくのが見事。"),
                    TestDiaryItem("心に残ったセリフ", "「未来は決まっていない、君が作るんだ」という言葉が胸に響いた。"),
                    TestDiaryItem("映画館のポップコーン", "塩バター味のポップコーンが映画の最高のお供だった。")
                )
                TestDiaryData("映画館で過ごす休日", items)
            },
            // パターン2: 筋トレ (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("ベンチプレス", "自己ベストを更新！60kgを3回持ち上げられた。"),
                    TestDiaryItem("スクワット", "フォームを意識して丁寧に。下半身にしっかり効いた。"),
                    TestDiaryItem("プロテイン摂取", "トレーニング後のゴールデンタイムにプロテインを補給。チョコ味が美味しい。"),
                    TestDiaryItem("今日の体調", "少し疲労感はあるけど、達成感がすごい。筋肉痛が楽しみだ。"),
                    TestDiaryItem("ジムの混雑具合", "平日の昼間は空いていて、マシンが使い放題で快適だった。")
                )
                TestDiaryData("今日のジムトレーニング記録", items)
            },
            // パターン3: 料理 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("パスタに挑戦", "レシピ動画を見ながらカルボナーラを作った。"),
                    TestDiaryItem("出来栄えは？", "卵が少し固まってしまったけど、味はなかなか。家族にも好評だった。")
                )
                TestDiaryData("手作りカルボナーラ", items)
            },
            // パターン4: 散歩 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("近所の公園", "天気が良かったので、近くの公園まで散歩。金木犀の香りがした。"),
                    TestDiaryItem("見かけた猫", "ベンチで日向ぼっこしている猫がいて癒された。"),
                    TestDiaryItem("今日の歩数", "アプリで見たら8000歩も歩いていた。良い運動になった。")
                )
                TestDiaryData("秋の散歩日和", items)
            },
            // パターン5: 新しい挑戦 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("プログラミング学習", "Kotlinの新しいライブラリを触ってみた。覚えることが多くて大変だが、面白い。")
                )
                TestDiaryData("新しいスキル習得への道", items)
            },
            // パターン6:読書 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("ミステリー小説", "東野圭吾の新作を一気読み。最後のどんでん返しに鳥肌が立った。"),
                    TestDiaryItem("心に残った一文", "「絶望の淵でこそ、人は最も強く輝く」という言葉が印象的だった。"),
                    TestDiaryItem("次の本", "次は自己啓発本を読んでみようか検討中。"),
                    TestDiaryItem("読書環境", "静かな部屋で、温かいコーヒーを飲みながら読むのが至福の時間。")
                )
                TestDiaryData("読書に没頭した一日", items)
            },
            // パターン7: 買い物 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("秋服を探しに", "デパートをぶらぶら。チェック柄のシャツと新しいジャケットを購入。"),
                    TestDiaryItem("衝動買い", "買う予定はなかったけど、デザインが気に入ってスニーカーも買ってしまった。")
                )
                TestDiaryData("ショッピングで気分転換", items)
            },
            // パターン8: 勉強・学習 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("資格試験の勉強", "模擬試験を解いてみた。合格ラインにはまだ少し届かない。"),
                    TestDiaryItem("苦手分野", "計算問題で時間を使いすぎているのが課題。重点的に復習が必要だ。"),
                    TestDiaryItem("集中力", "2時間集中して勉強できた。ポモドーロテクニックが効果的かもしれない。")
                )
                TestDiaryData("資格取得に向けた勉強記録", items)
            },
            // パターン9: ガーデニング (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("ベランダ菜園", "育てているミニトマトが赤くなってきた。収穫が楽しみ。"),
                    TestDiaryItem("水やり", "朝晩の涼しい時間にたっぷり水をあげた。植物が元気だと嬉しい。"),
                    TestDiaryItem("新しい仲間", "ハーブの苗を新しく購入。バジルとローズマリーを植えた。"),
                    TestDiaryItem("虫対策", "アブラムシが少し付いていたので、専用のスプレーで駆除した。"),
                    TestDiaryItem("成長の記録", "毎日の変化を写真に撮って記録するのも楽しい。")
                )
                TestDiaryData("ベランダでのガーデニング日和", items)
            },
            // パターン10: 友人との時間 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("久しぶりの再会", "高校時代の友人とランチ。何年経っても変わらない関係が嬉しい。"),
                    TestDiaryItem("思い出話", "昔の笑える失敗談で盛り上がった。時間が経つのがあっという間。"),
                    TestDiaryItem("近況報告", "お互いの仕事やプライベートについて語り合った。良い刺激をもらえた。")
                )
                TestDiaryData("親友との楽しいひととき", items)
            },
            // パターン11: ペットとのふれあい (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("犬の散歩", "夕方に長めの散歩へ。他の犬と楽しそうに挨拶していた。"),
                    TestDiaryItem("新しいおもちゃ", "新しいボールを買ってあげたら、夢中になって遊んでいる。")
                )
                TestDiaryData("愛犬とののんびりした一日", items)
            },
            // パターン12: 健康・体調 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("少し風邪気味", "朝から少し喉が痛い。早めに薬を飲んで対策。"),
                    TestDiaryItem("食事", "消化に良いものを食べようと思い、夜はお粥にした。"),
                    TestDiaryItem("休息", "今日は無理せず、早めにベッドに入ってゆっくり休むことにした。"),
                    TestDiaryItem("ビタミン補給", "フルーツを多めに食べてビタミンを補給した。")
                )
                TestDiaryData("体調管理を意識した日", items)
            },
            // パターン13: 整理整頓 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("クローゼットの整理", "思い切って断捨離。もう着ない服を処分したら、かなりスッキリした。")
                )
                TestDiaryData("断捨離で心もスッキリ", items)
            },
            // パターン14: 美容・セルフケア (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("美容院へ", "髪を10cmほどカットして、カラーも秋色にチェンジ。気分が変わる。"),
                    TestDiaryItem("スキンケア", "いつもより時間をかけて丁寧にスキンケア。パックで肌がもちもちになった。"),
                    TestDiaryItem("半身浴", "好きな香りの入浴剤を入れて、ゆっくり半身浴。最高のリラックスタイム。"),
                    TestDiaryItem("ネイルケア", "爪やすりで形を整えて、新しいネイルカラーを塗った。"),
                    TestDiaryItem("マッサージ", "セルフマッサージで足のむくみを取った。体が軽くなった気がする。")
                )
                TestDiaryData("自分を労わるセルフケアDAY", items)
            },
            // パターン15: 目標設定 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("今月の目標を再確認", "月の後半に向けて、目標の進捗状況を見直した。"),
                    TestDiaryItem("計画の修正", "少し遅れ気味なので、週末に集中して作業する時間を確保する計画を立てた。"),
                    TestDiaryItem("モチベーション", "目標を達成した時のことを想像したら、やる気が湧いてきた。")
                )
                TestDiaryData("目標達成に向けた計画見直し", items)
            },
            // パターン16: ゲーム (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("新作RPGをプレイ", "発売日にダウンロードして早速プレイ開始。世界観が最高。"),
                    TestDiaryItem("最初のボス戦", "ギリギリの戦いだったけど、なんとか勝利。レベル上げが必要そうだ。")
                )
                TestDiaryData("ゲームの世界に没入", items)
            },
            // パターン17: 家族との時間 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("週末の家族ディナー", "みんなで食卓を囲んで食事。他愛もない話で笑い合えるのが幸せ。"),
                    TestDiaryItem("子供の成長", "子供が新しい言葉を覚えた。日々の成長を見るのが楽しみ。"),
                    TestDiaryItem("昔のアルバム", "古いアルバムを引っ張り出してきて、思い出話に花が咲いた。"),
                    TestDiaryItem("共同作業", "一緒に夕食の準備をした。共同作業も楽しいものだ。")
                )
                TestDiaryData("家族と過ごす温かい時間", items)
            },
            // パターン18: ネットサーフィン (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("面白い記事を発見", "ネットニュースで興味深い科学記事を読んだ。知らない世界が広がって面白い。")
                )
                TestDiaryData("ネットの海を漂う一日", items)
            },
            // パターン19: 過去の思い出 (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("学生時代を思い出す", "ふとした瞬間に、高校時代の文化祭の準備で忙しかった日々を思い出した。"),
                    TestDiaryItem("昔の写真", "スマートフォンの写真フォルダを遡っていたら、旅行の写真が出てきて懐かしくなった。"),
                    TestDiaryItem("思い出の曲", "ラジオから流れてきた曲が、昔よく聴いていた曲で、一気に当時にタイムスリップした気分。"),
                    TestDiaryItem("卒業文集", "本棚の奥から卒業文集を発見。自分の書いた文章が若すぎて恥ずかしい。"),
                    TestDiaryItem("旧友への連絡", "懐かしくなって、何年も連絡を取っていなかった友人にメッセージを送ってみた。")
                )
                TestDiaryData("懐かしい思い出に浸る", items)
            },
            // パターン20: スキルアップ (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("オンライン講座を受講", "マーケティングに関するオンライン講座で新しい知識をインプット。"),
                    TestDiaryItem("学んだことの実践", "講座で学んだフレームワークを、早速仕事の資料作成に応用してみた。"),
                    TestDiaryItem("今後の課題", "知識を定着させるために、継続的なアウトプットが必要だと感じた。")
                )
                TestDiaryData("自己投資とスキルアップ", items)
            },
            // パターン21: ドライブ (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("海沿いをドライブ", "天気が良かったので、窓を全開にして海沿いの道を走った。潮風が気持ちいい。"),
                    TestDiaryItem("好きな音楽と共に", "お気に入りのプレイリストを大音量でかけながらの運転は最高だ。")
                )
                TestDiaryData("気ままなドライブ旅", items)
            },
            // パターン22: 家でのんびり (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("何もしない贅沢", "今日は予定を何も入れず、家でひたすらゴロゴロして過ごした。"),
                    TestDiaryItem("溜まっていたドラマを消化", "録画していた連続ドラマを1話から一気見。続きが気になる！"),
                    TestDiaryItem("お昼寝", "ソファでうとうとしていたら、いつの間にか2時間も寝てしまっていた。最高の休日。"),
                    TestDiaryItem("デリバリー", "夕食はデリバリーを頼んで、料理もサボることに決めた。")
                )
                TestDiaryData("最高の休日、おうち時間", items)
            },
            // パターン23: 新しい場所の開拓 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("一駅歩いてみた", "いつもは電車に乗る区間を、今日は歩いてみることに。"),
                    TestDiaryItem("知らないお店", "歩いている途中で、趣のある古本屋やおしゃれな雑貨屋を見つけた。"),
                    TestDiaryItem("新しい発見", "普段通らない道を歩くだけで、新しい発見がたくさんあって楽しかった。")
                )
                TestDiaryData("近所の新たな魅力探し", items)
            },
            // パターン24: ボランティア活動 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("地域の清掃活動に参加", "朝早くから、公園のゴミ拾いボランティアに参加した。"),
                    TestDiaryItem("活動後の達成感", "汗をかいた後の達成感は格別。街がきれいになって気持ちがいい。")
                )
                TestDiaryData("社会貢献でリフレッシュ", items)
            },
            // パターン25: 失敗談 (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("寝坊して大慌て", "目覚ましをかけ忘れて、いつもより1時間も寝坊。朝からバタバタだった。"),
                    TestDiaryItem("大事なものを忘れる", "家を出る直前に、今日の会議で使う大事な資料を忘れたことに気づいた。"),
                    TestDiaryItem("今日の教訓", "前日の夜に、次の日の準備をしっかりしておくことの大切さを痛感した。"),
                    TestDiaryItem("電車を乗り間違える", "ぼーっとしていて、反対方向の電車に乗ってしまった。"),
                    TestDiaryItem("笑い話", "失敗続きの一日だったけど、後から考えれば笑い話になりそうだ。")
                )
                TestDiaryData("ちょっとツイてない一日", items)
            },
            // パターン26: 夢の話 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("不思議な夢を見た", "空を飛ぶ夢を見た。自由に飛び回れて、とても気持ちが良かった。"),
                    TestDiaryItem("夢の登場人物", "なぜか昔の同級生が出てきた。何か意味があるのだろうか。"),
                    TestDiaryItem("夢の記憶", "目が覚めた直後は覚えていたのに、段々と内容が薄れていくのがもどかしい。")
                )
                TestDiaryData("今日の夢日記", items)
            },
            // パターン27: アート鑑賞 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("美術館へ", "話題の現代アート展に行ってきた。作品の意図を考えるのが面白い。")
                )
                TestDiaryData("美術館でアートに触れる", items)
            },
            // パターン28: 目標達成 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("ついに目標達成！", "3ヶ月間続けてきた「毎日1万歩歩く」という目標をついに達成できた。"),
                    TestDiaryItem("達成感", "継続は力なり、という言葉を実感。自分に自信がついた。"),
                    TestDiaryItem("次の目標", "次は「週末に30分ジョギングする」という新しい目標を立ててみようと思う。"),
                    TestDiaryItem("ご褒美", "目標達成のご褒美に、少しリッチなディナーを予約した。")
                )
                TestDiaryData("継続の果てに掴んだ成功", items)
            },
            // パターン29: 音楽鑑賞 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("新しいアルバム", "好きなバンドの新しいアルバムを一日中リピートしていた。"),
                    TestDiaryItem("お気に入りの曲", "3曲目のメロディが特に好き。歌詞も心に沁みる。"),
                    TestDiaryItem("ライブに行きたい", "このアルバムの曲を生で聴いたら最高だろうな。次のツアーが待ち遠しい。")
                )
                TestDiaryData("最高の音楽に浸る一日", items)
            }
        )

        // 引数で渡されたパターン番号のラムダを実行してデータを返す
        // パターン番号が範囲外の場合は0番目を返す
        val index = if (patternNumber in diaryTemplates.indices) patternNumber else 0
        return diaryTemplates[index]()
    }
    //endregion
}
