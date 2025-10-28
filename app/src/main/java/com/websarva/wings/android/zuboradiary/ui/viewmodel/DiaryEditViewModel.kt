package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.Condition
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
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ErrorType
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryEditUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.collections.map
import kotlin.random.Random

@HiltViewModel
internal class DiaryEditViewModel @Inject constructor(
    handle: SavedStateHandle,
    diaryUiStateHelper: DiaryUiStateHelper,
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
) : BaseViewModel<DiaryEditEvent, DiaryEditAppMessage, DiaryEditUiState>(
    handle.get<DiaryEditUiState>(SAVED_UI_STATE_KEY)?.copy(
        isProcessing = false,
        isInputDisabled = false
    ) ?: DiaryEditUiState(editingDiary = Diary.generate().toUiModel())
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val DIARY_ID_ARGUMENT_KEY = "load_diary_id"
        private const val DIARY_DATE_ARGUMENT_KEY = "load_diary_date"

        // ViewModel状態保存キー
        // MEMO:システムの初期化によるプロセスの終了から(アプリ設定変更からのアプリ再起動時)の復元用
        private const val SAVED_UI_STATE_KEY = "uiState"
    }

    override val isProgressIndicatorVisible =
        uiState.map { state ->
            state.isProcessing
        }.stateInWhileSubscribed(
            false
        )

    private val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && currentUiState.originalDiaryLoadState is LoadState.Success

    // TODO:BaseViewModelに用意
    private val currentUiState
        get() = uiState.value

    private val originalDiary
        get() = (currentUiState.originalDiaryLoadState as LoadState.Success).data

    private val editingDiaryFlow = uiState.map { it.editingDiary }

    // キャッシュパラメータ
    private var pendingDiaryLoadParameters: DiaryLoadParameters? = null
    private var pendingDiaryUpdateParameters: DiaryUpdateParameters? = null
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    private var pendingDiaryDateUpdateParameters: DiaryDateUpdateParameters? = null
    private var pendingDiaryItemDeleteParameters: DiaryItemDeleteParameters? = null
    private var pendingDiaryImageUpdateParameters: DiaryImageUpdateParameters? = null
    private var pendingWeatherInfoFetchParameters: WeatherInfoFetchParameters? = null
    private var pendingPreviousNavigationParameters: PreviousNavigationParameters? = null

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    init {
        initializeDiaryData(handle)
        observeDerivedUiStateChanges(handle, diaryUiStateHelper)
        observeUiStateChanges()
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        // MEMO:下記条件はアプリ設定変更時のアプリ再起動時の不要初期化対策
        if (handle.contains(SAVED_UI_STATE_KEY)) return
        val id = handle.get<String>(DIARY_ID_ARGUMENT_KEY)?.let { DiaryId(it) }
        val date =
            handle.get<LocalDate>(DIARY_DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            prepareDiaryEntry(
                id,
                date
            )
        }
    }

    private fun observeDerivedUiStateChanges(
        handle: SavedStateHandle,
        diaryUiStateHelper: DiaryUiStateHelper
    ) {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_UI_STATE_KEY] = it
        }.launchIn(viewModelScope)

        uiState.map { state ->
            WeatherUi.entries.filter { weather ->
                weather != state.editingDiary.weather1
            }
        }.distinctUntilChanged().onEach { selections ->
            updateUiState {
                it.copy(
                    weather2Options = selections
                )
            }
        }.launchIn(viewModelScope)

        editingDiaryFlow.map { editingDiary ->
            when (editingDiary.weather1) {
                WeatherUi.UNKNOWN -> false
                else -> {
                    editingDiary.weather1 != editingDiary.weather2
                }
            }
        }.distinctUntilChanged().onEach { isEnabled ->
            if (isEnabled) {
                updateUiState {
                    it.copy(
                        isWeather2Enabled = isEnabled
                    )
                }
            } else {
                updateUiState {
                    it.copy(
                        editingDiary = it.editingDiary.copy(weather2 = WeatherUi.UNKNOWN),
                        isWeather2Enabled = isEnabled
                    )
                }
            }
        }.launchIn(viewModelScope)

        editingDiaryFlow.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateUiState {
                it.copy(
                    numVisibleDiaryItems = numVisibleDiaryItems
                )
            }
        }.launchIn(viewModelScope)

        uiState.map {
            !it.isInputDisabled && it.numVisibleDiaryItems < DiaryItemNumber.MAX_NUMBER
        }.distinctUntilChanged().onEach { isEnabled ->
            updateUiState {
                it.copy(
                    isDiaryItemAdditionEnabled = isEnabled
                )
            }
        }.launchIn(viewModelScope)

        editingDiaryFlow.map {
            diaryUiStateHelper.buildImageFilePath(it)
        }.distinctUntilChanged().onEach { path ->
            updateUiState {
                it.copy(
                    diaryImageFilePath = path
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeUiStateChanges() {
        viewModelScope.launch {
            uiState.map { it.numVisibleDiaryItems }
                .distinctUntilChanged()
                .collectLatest { value: Int ->
                    Log.d("20251022", "DiaryEditUiState.numVisibleDiaryItems: $value")
                    emitUiEvent(DiaryEditEvent.UpdateDiaryItemLayout(value))
                }
        }
    }

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): DiaryEditEvent {
        return DiaryEditEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: DiaryEditAppMessage): DiaryEditEvent {
        return DiaryEditEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): DiaryEditAppMessage {
        return DiaryEditAppMessage.Unexpected(e)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    // Viewクリック処理
    fun onDiarySaveMenuClick() {
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

    fun onDiaryDeleteMenuClick() {
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
                DiaryEditEvent.NavigateDiaryDeleteDialog(originalDiaryDate)
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
                DiaryEditEvent.NavigateDatePickerDialog(date)
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
                DiaryEditEvent.NavigateDiaryItemTitleEditFragment(
                    DiaryItemTitleSelectionUi(
                        itemNumberInt,
                        itemTitleId,
                        itemTitle
                    )
                )
            )
        }
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
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(itemNumberInt)
            )
        }
    }

    fun onAttachedImageDeleteButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryImageDeleteDialog
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

    // Fragmentからの結果受取処理
    fun onDiaryLoadDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onDiaryUpdateDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onDatePickerDialogResultReceived(result: DialogResult<LocalDate>) {
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

    fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                launchWithUnexpectedErrorHandler {
                    emitUiEvent(
                        DiaryEditEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed()
                    )
                }
            }
        }
    }

    fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onDiaryImageDeleteDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onExitWithoutDiarySaveDialogResultReceived(result: DialogResult<Unit>) {
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

    fun onItemTitleEditFragmentResultReceived(result: FragmentResult<DiaryItemTitleSelectionUi>) {
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
    fun onOpenDocumentResultImageUriReceived(uri: Uri?) {
        val parameters = pendingDiaryImageUpdateParameters
        clearPendingDiaryImageUpdateParameters()
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                cacheDiaryImage(uri, parameters.id)
            } ?: throw IllegalStateException()
        }
    }

    // MotionLayout変更時処理
    fun onDiaryItemInvisibleStateTransitionCompleted(itemNumberInt: Int) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        deleteItem(itemNumber)
    }

    fun onDiaryItemVisibleStateTransitionCompleted() {
        updateToIdleState()
    }

    // 権限確認後処理
    fun onAccessLocationPermissionChecked(
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

    // データ処理
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
                    // TODO:ErrorTypeは仮。全体で必要になったら検討。
                    updateToDiaryLoadErrorState(ErrorType.Failure(result.exception))

                    // MEMO:連続するUIイベント（エラー表示と画面遷移）は、監視開始前に発行されると
                    //      取りこぼされる可能性がある。これを防ぐため、間に確認ダイアログを挟み、
                    //      ユーザーの応答を待ってから画面遷移を実行する。
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryLoadFailureDialog(date)
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
                    DiaryEditEvent
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
                    DiaryEditEvent
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
                        DiaryEditEvent.NavigateDiaryLoadDialog(date)
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
                        DiaryEditEvent.NavigateDiaryUpdateDialog(diary.date)
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
                DiaryEditEvent.NavigateWeatherInfoFetchDialog(date)
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
            DiaryEditEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch
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
        emitUiEvent(DiaryEditEvent.ItemAddition)
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
                DiaryEditEvent.TransitionDiaryItemToInvisibleState(itemNumber.value)
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
        emitUiEvent(DiaryEditEvent.SelectImage)
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
                DiaryEditEvent.NavigateExitWithoutDiarySaveConfirmationDialog
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
        updateUiState {
            it.copy(
                numVisibleDiaryItems = num
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

    private fun updateToDiaryLoadErrorState(errorType: ErrorType) {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Error(errorType),
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updatePendingDiaryLoadParameters(date: LocalDate, previousDate: LocalDate?) {
        pendingDiaryLoadParameters = DiaryLoadParameters(date, previousDate)
    }

    private fun clearPendingDiaryLoadParameters() {
        pendingDiaryLoadParameters = null
    }

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

    private fun updatePendingDiaryDeleteParameters(id: DiaryId, date: LocalDate) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, date)
    }

    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    private fun updatePendingDiaryDateUpdateParameters(
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        pendingDiaryDateUpdateParameters = DiaryDateUpdateParameters(originalDate, isNewDiary)
    }

    private fun clearPendingDiaryDateUpdateParameters() {
        pendingDiaryDateUpdateParameters = null
    }

    private fun updatePendingPreviousNavigationParameter(originalDiaryDate: LocalDate) {
        pendingPreviousNavigationParameters = PreviousNavigationParameters(originalDiaryDate)
    }

    private fun clearPendingPreviousNavigationParameters() {
        pendingPreviousNavigationParameters = null
    }

    private fun updatePendingWeatherInfoFetchParameters(date: LocalDate) {
        pendingWeatherInfoFetchParameters = WeatherInfoFetchParameters(date)
    }

    private fun clearPendingWeatherInfoFetchParameters() {
        pendingWeatherInfoFetchParameters = null
    }

    private fun updatePendingDiaryItemDeleteParameters(itemNumber: DiaryItemNumber) {
        pendingDiaryItemDeleteParameters = DiaryItemDeleteParameters(itemNumber)
    }

    private fun clearPendingDiaryItemDeleteParameters() {
        pendingDiaryItemDeleteParameters = null
    }

    private fun updatePendingDiaryImageUpdateParameters(diaryId: DiaryId) {
        pendingDiaryImageUpdateParameters = DiaryImageUpdateParameters(diaryId)
    }

    private fun clearPendingDiaryImageUpdateParameters() {
        pendingDiaryImageUpdateParameters = null
    }

    private data class DiaryLoadParameters(
        val date: LocalDate,
        val previousDate: LocalDate?
    )

    private data class DiaryUpdateParameters(
        val diary: Diary,
        val diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        val originalDiary: Diary,
        val isNewDiary: Boolean
    )

    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val date: LocalDate
    )

    private data class DiaryDateUpdateParameters(
        val originalDate: LocalDate,
        val isNewDiary: Boolean
    )

    private data class DiaryItemDeleteParameters(
        val itemNumber: DiaryItemNumber
    )

    private data class DiaryImageUpdateParameters(val id: DiaryId)

    private data class WeatherInfoFetchParameters(
        val date: LocalDate
    )

    private data class PreviousNavigationParameters(
        val originalDiaryDate: LocalDate
    )

    // TODO:テスト用の為、最終的に削除
    // TODO:上手く保存されない。保存ユースケースの条件を変えたため？
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
                updateDate(saveDate)
                val weather1Int = Random.nextInt(1, WeatherUi.entries.size)
                updateWeather1(Weather.of(weather1Int).toUiModel())
                val weather2Int = Random.nextInt(1, WeatherUi.entries.size)
                updateWeather2(Weather.of(weather2Int).toUiModel())
                val conditionInt = Random.nextInt(1, ConditionUi.entries.size)
                updateCondition(Condition.of(conditionInt).toUiModel())
                val title = generateRandomAlphanumericString(15)
                updateTitle(title)
                val numItems = Random.nextInt(DiaryItemNumber.MIN_NUMBER, DiaryItemNumber.MAX_NUMBER + 1)
                updateNumVisibleItems(numItems)
                for (j in 1..numItems) {
                    val diaryItemNumber = j
                    val itemTitle = generateRandomAlphanumericString(15)
                    val itemComment = generateRandomAlphanumericString(50)
                    val selection = DiaryItemTitleSelectionUi(
                        diaryItemNumber,
                        DiaryItemTitleSelectionHistoryId.generate().value,
                        itemTitle
                    )
                    updateItemTitle(selection)
                    updateItemComment(diaryItemNumber, itemComment)
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

    // TODO:テスト用の為、最終的に削除
    private fun generateRandomAlphanumericString(length: Int): String {
        require(length >= 0) { "Length must be non-negative" }

        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
