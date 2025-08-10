package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.FetchWeatherInfoUseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.IsWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParametersForDiaryEdit
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryEditStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal class DiaryEditViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val shouldRequestExitWithoutDiarySaveConfirmationUseCase: ShouldRequestExitWithoutDiarySaveConfirmationUseCase,
    private val shouldRequestDiaryLoadConfirmationUseCase: ShouldRequestDiaryLoadConfirmationUseCase,
    private val shouldRequestDiaryUpdateConfirmationUseCase: ShouldRequestDiaryUpdateConfirmationUseCase,
    private val shouldRequestWeatherInfoConfirmationUseCase: ShouldRequestWeatherInfoConfirmationUseCase,
    private val loadDiaryUseCase: LoadDiaryUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val isWeatherInfoFetchEnabledUseCase: IsWeatherInfoFetchEnabledUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase,
    private val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
) : BaseViewModel<DiaryEditEvent, DiaryEditAppMessage, DiaryEditState>(
    handle[SAVED_VIEW_MODEL_STATE_KEY] ?: DiaryEditState.Idle
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val DIARY_DATE_ARGUMENT_KEY = "diary_date"
        private const val SHOULD_LOAD_DIARY_ARGUMENT_KEY = "should_load_diary"

        // ViewModel状態保存キー
        // MEMO:システムの初期化によるプロセスの終了から(アプリ設定変更からのアプリ再起動時)の復元用
        private const val SAVED_VIEW_MODEL_STATE_KEY = "uiState"
        private const val SAVED_PREVIOUS_DATE_STATE_KEY = "previousDate"
        private const val SAVED_ORIGINAL_DIARY_KEY = "originalDiary"
        private const val SAVED_IS_NEW_DIARY_KEY = "isNewDiary"
    }

    private val logTag = createLogTag()

    override val isProgressIndicatorVisible =
        uiState.map { state ->
            return@map when (state) {
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.SelectingImage -> true

                // DataSourceにアクセスしない処理
                DiaryEditState.AddingItem,
                DiaryEditState.DeletingItem -> false

                DiaryEditState.Idle,
                DiaryEditState.Editing,
                DiaryEditState.LoadError -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    // MEMO:UiState更新が伴うイベントメソッドの条件として使用
    private val canExecuteOperationWithUiUpdate: Boolean
        get() {
            return when (uiState.value) {
                DiaryEditState.Editing -> true

                DiaryEditState.Idle,
                DiaryEditState.Loading,
                DiaryEditState.LoadError,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.AddingItem,
                DiaryEditState.DeletingItem,
                DiaryEditState.SelectingImage -> false
            }
        }

    // 日記データ関係
    private var previousDate: LocalDate? = handle[SAVED_PREVIOUS_DATE_STATE_KEY]
        private set(value) {
            handle[SAVED_PREVIOUS_DATE_STATE_KEY] = value
            field = value
        }

    private val _isNewDiary = MutableStateFlow(handle[SAVED_IS_NEW_DIARY_KEY] ?: false)
    val isNewDiary = _isNewDiary.asStateFlow()

    private val _originalDiary = MutableStateFlow<Diary?>(handle[SAVED_ORIGINAL_DIARY_KEY])

    val editingDiaryDate =
        combine(_isNewDiary, _originalDiary) { isNewDiary, originalDiary ->
            return@combine if (isNewDiary) null else originalDiary?.date
        }.stateInWhileSubscribed(null)

    private val _editingDiaryDateString = MutableStateFlow<String?>(null)
    val editingDiaryDateString = _editingDiaryDateString.asStateFlow()

    private val diaryStateFlow = DiaryEditStateFlow(viewModelScope, handle)

    val date
        get() = diaryStateFlow.date.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val titleMutableStateFlow
        get() = diaryStateFlow.title

    val weather1
        get() = diaryStateFlow.weather1.asStateFlow()

    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()

    private val isEqualWeathers: Boolean
        get() {
            val weather1 = diaryStateFlow.weather1.value
            val weather2 = diaryStateFlow.weather2.value

            return weather1 == weather2
        }

    val condition
        get() = diaryStateFlow.condition.asStateFlow()

    val numVisibleItems
        get() = diaryStateFlow.numVisibleItems.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val item1TitleMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).title

    /**
     * LayoutDataBinding用
     * */
    val item2TitleMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).title

    /**
     * LayoutDataBinding用
     * */
    val item3TitleMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).title

    /**
     * LayoutDataBinding用
     * */
    val item4TitleMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).title

    /**
     * LayoutDataBinding用
     * */
    val item5TitleMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).title

    /**
     * LayoutDataBinding用
     * */
    val item1CommentMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).comment

    /**
     * LayoutDataBinding用
     * */
    val item2CommentMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).comment

    /**
     * LayoutDataBinding用
     * */
    val item3CommentMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).comment

    /**
     * LayoutDataBinding用
     * */
    val item4CommentMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).comment

    /**
     * LayoutDataBinding用
     * */
    val item5CommentMutable
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).comment

    val isItemAdditionButtonClickable =
        combine(uiState, numVisibleItems) { state, numVisibleItems ->
            return@combine when (state) {
                DiaryEditState.Editing -> {
                    numVisibleItems < 5
                }

                DiaryEditState.Idle,
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.LoadError,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.AddingItem,
                DiaryEditState.DeletingItem,
                DiaryEditState.SelectingImage -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    val imageUri
        get() = diaryStateFlow.imageUri.asStateFlow()

    val isImageDeleteButtonClickable =
        combine(uiState, imageUri) { state, imageUri ->
            return@combine when (state) {
                DiaryEditState.Editing -> {
                    imageUri != null
                }

                DiaryEditState.Idle,
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.LoadError,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.AddingItem,
                DiaryEditState.DeletingItem,
                DiaryEditState.SelectingImage -> false
            }
        }.stateInWhileSubscribed(
            false
        )

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    init {
        initializeDiaryData(handle)
        setUpStateSaveObservers()
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        // MEMO:下記条件はアプリ設定変更時のアプリ再起動時の不要初期化対策
        if (uiState.value != DiaryEditState.Idle) return

        val date =
            handle.get<LocalDate>(DIARY_DATE_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        val shouldLoadDiary =
            handle.get<Boolean>(SHOULD_LOAD_DIARY_ARGUMENT_KEY) ?: throw IllegalArgumentException()
        viewModelScope.launch {
            prepareDiaryEntry(date, shouldLoadDiary)
        }
    }

    private fun setUpStateSaveObservers() {
        _isNewDiary.onEach {
            handle[SAVED_IS_NEW_DIARY_KEY] = it
        }.launchIn(viewModelScope)

        _originalDiary.onEach {
            handle[SAVED_ORIGINAL_DIARY_KEY] = it
        }.launchIn(viewModelScope)
    }

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: DiaryEditAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        val diary = diaryStateFlow.createDiary()
        val originalDiary = _originalDiary.requireValue()
        viewModelScope.launch {
            handleBackNavigation(diary, originalDiary)
        }
    }

    // Viewクリック処理
    fun onDiarySaveMenuClick() {
        if (!canExecuteOperationWithUiUpdate) return

        val diary = diaryStateFlow.createDiary()
        val diaryItemTitleSelectionHistoryList =
            diaryStateFlow.createDiaryItemTitleSelectionHistoryList()
        val originalDiary = _originalDiary.requireValue()
        val isNewDiary = _isNewDiary.value

        viewModelScope.launch {
            requestDiaryUpdateConfirmation(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            ) {
                saveDiary(
                    diary,
                    diaryItemTitleSelectionHistoryList,
                    originalDiary,
                    isNewDiary
                )
            }
        }
    }

    fun onDiaryDeleteMenuClick() {
        if (!canExecuteOperationWithUiUpdate) return
        val isNewDiary = _isNewDiary.value
        if (isNewDiary) return

        val originalDiary = _originalDiary.requireValue()
        val originalDate = originalDiary.date
        val originalImageUri = originalDiary.imageUriString?.let { Uri.parse(it) }

        val parameters = DiaryDeleteParameters(originalDate, originalImageUri)

        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryDeleteDialog(parameters)
            )
        }

    }

    fun onNavigationClick() {
        val diary = diaryStateFlow.createDiary()
        val originalDiary = _originalDiary.requireValue()

        viewModelScope.launch {
            handleBackNavigation(diary, originalDiary)
        }
    }

    fun onDateInputFieldClick() {
        val date = this.date.requireValue()

        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.NavigateDatePickerDialog(date)
            )
        }
    }

    fun onWeather1InputFieldItemClick(weather: Weather) {
        updateWeather1(weather)
    }

    fun onWeather2InputFieldItemClick(weather: Weather) {
        updateWeather2(weather)
    }

    fun onConditionInputFieldItemClick(condition: Condition) {
        updateCondition(condition)
    }

    fun onItemTitleInputFieldClick(itemNumber: ItemNumber) {
        if (uiState.value == DiaryEditState.AddingItem) return
        if (uiState.value == DiaryEditState.DeletingItem) return

        val itemTitle = getItemTitle(itemNumber).requireValue()

        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryItemTitleEditFragment(
                    DiaryItemTitle(itemNumber, itemTitle)
                )
            )
        }
    }

    fun onItemAdditionButtonClick() {
        if (!canExecuteOperationWithUiUpdate) return

        viewModelScope.launch {
            addDiaryItem()
        }
    }

    fun onItemDeleteButtonClick(itemNumber: ItemNumber) {
        if (!canExecuteOperationWithUiUpdate) return

        viewModelScope.launch {
            val parameters = DiaryItemDeleteParameters(itemNumber)
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(parameters)
            )
        }
    }

    fun onAttachedImageDeleteButtonClick() {
        if (!canExecuteOperationWithUiUpdate) return

        viewModelScope.launch {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryImageDeleteDialog
            )
        }
    }

    fun onAttachedImageClick() {
        if (!canExecuteOperationWithUiUpdate) return

        viewModelScope.launch {
            selectImage()
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadDialogResultReceived(result: DialogResult<DiaryLoadParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryLoadParameters> -> {
                handleDiaryLoadDialogPositiveResult(result.data)
            }
            is DialogResult.Negative,
            is DialogResult.Cancel -> {
                handleDiaryLoadDialogNegativeResult()
            }
        }
    }

    private fun handleDiaryLoadDialogPositiveResult(parameters: DiaryLoadParameters) {
        val date = parameters.date
        viewModelScope.launch {
            loadDiary(date)
        }
    }

    private fun handleDiaryLoadDialogNegativeResult() {
        val date = date.requireValue()
        val previousDate = previousDate

        viewModelScope.launch {
            processWeatherInfoFetch(date, previousDate)
        }
    }

    fun onDiaryUpdateDialogResultReceived(result: DialogResult<DiaryUpdateParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryUpdateParameters> -> {
                handleDiaryUpdateDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDiaryUpdateDialogPositiveResult(parameters: DiaryUpdateParameters) {
        val diary = parameters.diary
        val diaryItemTitleSelectionHistoryList =
            parameters.diaryItemTitleSelectionHistoryItemList
        val originalDiary = parameters.originalDiary
        val isNewDiary = parameters.isNewDiary
        viewModelScope.launch {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<DiaryDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryDeleteParameters> -> {
                handleDiaryDeleteDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDiaryDeleteDialogPositiveResult(parameters: DiaryDeleteParameters) {
        val date = parameters.date
        val imageUri = parameters.imageUri
        viewModelScope.launch {
            deleteDiary(date, imageUri)
        }
    }

    fun onDatePickerDialogResultReceived(result: DialogResult<LocalDate>) {
        when (result) {
            is DialogResult.Positive<LocalDate> -> {
                handleDatePickerDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDatePickerDialogPositiveResult(date: LocalDate) {
        val originalDate = _originalDiary.requireValue().date
        val isNewDiary = _isNewDiary.value
        viewModelScope.launch {
            processChangedDiaryDate(date, originalDate, isNewDiary)
        }
    }

    fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                viewModelScope.launch {
                    emitUiEvent(
                        DiaryEditEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed()
                    )
                }
            }
        }
    }

    fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<WeatherInfoFetchParameters>) {
        when (result) {
            is DialogResult.Positive<WeatherInfoFetchParameters> -> {
                handleWeatherInfoFetchDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleWeatherInfoFetchDialogPositiveResult(
        parameters: WeatherInfoFetchParameters
    ) {
        viewModelScope.launch {
            checkPermissionBeforeWeatherInfoFetch(parameters)
        }
    }

    fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<DiaryItemDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryItemDeleteParameters> -> {
                handleDiaryItemDeleteDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleDiaryItemDeleteDialogPositiveResult(parameters: DiaryItemDeleteParameters) {
        val itemNumber = parameters.itemNumber
        viewModelScope.launch {
            requestDiaryItemDeleteTransition(itemNumber)
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
        deleteImageUri()
    }

    fun onExitWithoutDiarySaveDialogResultReceived(
        result: DialogResult<NavigatePreviousParametersForDiaryEdit>
    ) {
        when (result) {
            is DialogResult.Positive<NavigatePreviousParametersForDiaryEdit> -> {
                val originalDiary = result.data.originalDiary
                viewModelScope.launch {
                    navigatePreviousFragment(originalDiary)
                }
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    fun onItemTitleEditFragmentResultReceived(result: FragmentResult<DiaryItemTitle>) {
        when (result) {
            is FragmentResult.Some -> {
                updateItemTitle(
                    result.data.itemNumber,
                    result.data.title
                )
            }
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    // MEMO:未選択時null
    fun onOpenDocumentResultImageUriReceived(uri: Uri?) {
        updateImageUri(uri)
        updateUiState(DiaryEditState.Editing)
    }

    // StateFlow値変更時処理
    fun onOriginalDiaryDateChanged(dateString: String?) {
        updateEditingDiaryDateString(dateString)
    }

    // MotionLayout変更時処理
    fun onDiaryItemInvisibleStateTransitionCompleted(itemNumber: ItemNumber) {
        check(uiState.value == DiaryEditState.DeletingItem)

        deleteItem(itemNumber)
    }

    fun onDiaryItemVisibleStateTransitionCompleted() {
        check(uiState.value == DiaryEditState.AddingItem)

        updateUiState(DiaryEditState.Editing)
    }

    // 権限確認後処理
    fun onAccessLocationPermissionChecked(
        isGranted: Boolean,
        parameters: WeatherInfoFetchParameters
    ) {
        val date = parameters.date

        viewModelScope.launch {
            fetchWeatherInfo(isGranted, date)
        }
    }

    // データ処理
    private suspend fun prepareDiaryEntry(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {
        if (shouldLoadDiary) {
            loadDiary(date)
        } else {
            prepareNewDiaryEntry(date)
        }
    }

    private suspend fun prepareNewDiaryEntry(date: LocalDate) {
        updateIsNewDiary(true)
        updateDate(date)
        updateOriginalDiary(diaryStateFlow.createDiary())
        val previousDate = previousDate
        val originalDate = _originalDiary.requireValue().date
        val isNewDiary = isNewDiary.value
        requestDiaryLoadConfirmationAndFetchWeatherIfNeeded(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    private suspend fun loadDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        val previousState = uiState.value
        updateUiState(DiaryEditState.Loading)
        when (val result = loadDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                val diary = result.value
                diaryStateFlow.update(diary)
                updateIsNewDiary(false)
                updateOriginalDiary(diaryStateFlow.createDiary())
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                if (previousState == DiaryEditState.Idle) {
                    updateUiState(DiaryEditState.LoadError)
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryLoadFailureDialog(date)
                    )
                } else {
                    updateUiState(DiaryEditState.Editing)
                    emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadFailure)
                }
            }
        }
    }

    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        updateUiState(DiaryEditState.Saving)
        val result =
            saveDiaryUseCase(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                originalDiary,
                isNewDiary
            )
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateUiState(DiaryEditState.Editing)
                emitUiEvent(
                    DiaryEditEvent
                        .NavigateDiaryShowFragment(diary.date)
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateUiState(DiaryEditState.Editing)
                emitAppMessageEvent(DiaryEditAppMessage.DiarySaveFailure)
            }
        }
    }

    private suspend fun deleteDiary(
        date: LocalDate,
        imageUri: Uri?
    ) {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        updateUiState(DiaryEditState.Deleting)
        val imageUriString = imageUri?.toString()
        when (deleteDiaryUseCase(date, imageUriString)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateUiState(DiaryEditState.Editing)
                emitUiEvent(
                    DiaryEditEvent
                        .NavigatePreviousFragmentOnDiaryDelete(
                            FragmentResult.Some(date)
                        )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateUiState(DiaryEditState.Editing)
                emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
            }
        }
    }

    private suspend fun requestDiaryLoadConfirmationAndFetchWeatherIfNeeded(
        date: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        requestDiaryLoadConfirmation(date, previousDate, originalDate, isNewDiary) {
            processWeatherInfoFetch(date, previousDate)
        }
    }

    private suspend fun requestDiaryLoadConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateUiState(DiaryEditState.CheckingDiaryInfo)
        val result =
            shouldRequestDiaryLoadConfirmationUseCase(date, previousDate, originalDate, isNewDiary)
        when (result) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                if (result.value) {
                    val parameters = DiaryLoadParameters(date)
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryLoadDialog(
                            parameters
                        )
                    )
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Failure -> {
                updateUiState(DiaryEditState.Editing)
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
            }
        }
    }

    private suspend fun requestDiaryUpdateConfirmation(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        originalDiary: Diary,
        isNewDiary: Boolean,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateUiState(DiaryEditState.CheckingDiaryInfo)
        val date = diary.date
        val originalDate = originalDiary.date
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, originalDate, isNewDiary)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                if (result.value) {
                    val parameters = DiaryUpdateParameters(
                        diary,
                        diaryItemTitleSelectionHistoryItemList,
                        originalDiary,
                        isNewDiary
                    )
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryUpdateDialog(parameters)
                    )
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Failure -> {
                updateUiState(DiaryEditState.Editing)
                emitAppMessageEvent(
                    DiaryEditAppMessage.DiarySaveFailure
                )
            }
        }
    }

    // 天気情報取得関係
    // TODO:コールバック構成の代替案を検討する。(他処理メソッドも同様に)
    // TODO:State更新タイミングの代替案を検討する。(他処理メソッドも同様に)
    private suspend fun processWeatherInfoFetch(date: LocalDate, previousDate: LocalDate?) {
        updateUiState(DiaryEditState.CheckingWeatherAvailability)
        val isEnabled = isWeatherInfoFetchEnabledUseCase().value
        updateUiState(DiaryEditState.Editing)
        if (!isEnabled) {
            return
        }

        requestWeatherInfoConfirmation(
            date,
            previousDate
        ) {

            val shouldLoad = shouldFetchWeatherInfoUseCase(date, previousDate).value
            if (!shouldLoad) {
                return@requestWeatherInfoConfirmation
            }

            val parameters = WeatherInfoFetchParameters(date)
            checkPermissionBeforeWeatherInfoFetch(parameters)
        }
    }

    private suspend fun requestWeatherInfoConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        val result =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate)
        if (result.value) {
            val parameters = WeatherInfoFetchParameters(date)
            emitUiEvent(
                DiaryEditEvent.NavigateWeatherInfoFetchDialog(parameters)
            )
        } else {
            onConfirmationNotNeeded()
        }
    }

    private suspend fun checkPermissionBeforeWeatherInfoFetch(
        parameters: WeatherInfoFetchParameters
    ) {
        emitUiEvent(
            DiaryEditEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch(parameters)
        )
    }

    private suspend fun fetchWeatherInfo(
        isGranted: Boolean,
        date: LocalDate
    ) {
        updateUiState(DiaryEditState.FetchingWeatherInfo)
        when (val result = fetchWeatherInfoUseCase(isGranted, date)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                updateWeather1(result.value)
                updateWeather2(Weather.UNKNOWN)
            }
            is UseCaseResult.Failure -> {
                updateUiState(DiaryEditState.Editing)
                when (result.exception) {
                    is FetchWeatherInfoUseCaseException.LocationPermissionNotGranted -> {
                        emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
                    }
                    is FetchWeatherInfoUseCaseException.LocationAccessFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                    is FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is FetchWeatherInfoUseCaseException.WeatherInfoFetchFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
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
        val previousDate = previousDate
        // MEMO:下記処理をdate(StateFlow)変数のCollectorから呼び出すと、
        //      画面回転時にも不要に呼び出してしまう為、下記にて処理。
        requestDiaryLoadConfirmationAndFetchWeatherIfNeeded(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    // 項目関係
    private fun getItemTitle(itemNumber: ItemNumber): StateFlow<String?> {
        return diaryStateFlow.getItemStateFlow(itemNumber).title
    }

    private suspend fun addDiaryItem() {
        updateUiState(DiaryEditState.AddingItem)
        emitUiEvent(DiaryEditEvent.ItemAddition)
        diaryStateFlow.incrementVisibleItemsCount()
    }

    private fun deleteItem(itemNumber: ItemNumber) {
        diaryStateFlow.deleteItem(itemNumber)
        updateUiState(DiaryEditState.Editing)
    }

    private suspend fun requestDiaryItemDeleteTransition(itemNumber: ItemNumber) {
        val numVisibleItems = numVisibleItems.requireValue()

        updateUiState(DiaryEditState.DeletingItem)
        if (itemNumber.value == 1 && numVisibleItems == itemNumber.value) {
            deleteItem(itemNumber)
        } else {
            emitUiEvent(
                DiaryEditEvent.TransitionDiaryItemToInvisibleState(itemNumber)
            )
        }
        // MEMO:deleteItem(itemNumber)でEditingStateに更新する為、下記コード不要。
        //updateUiState(DiaryEditState.Editing)
    }

    // 添付画像関係
    private suspend fun selectImage() {
        updateUiState(DiaryEditState.SelectingImage)
        emitUiEvent(DiaryEditEvent.SelectImage)
    }

    private fun deleteImageUri() {
        updateImageUri(null)
    }

    private suspend fun handleBackNavigation(
        diary: Diary,
        originalDiary: Diary
    ) {
        val shouldRequest =
            shouldRequestExitWithoutDiarySaveConfirmationUseCase(diary, originalDiary).value
        if (shouldRequest) {
            val parameters = NavigatePreviousParametersForDiaryEdit(originalDiary)
            emitUiEvent(
                DiaryEditEvent
                    .NavigateExitWithoutDiarySaveConfirmationDialog(parameters)
            )
        } else {
            navigatePreviousFragment(originalDiary)
        }
    }

    private suspend fun navigatePreviousFragment(originalDiary: Diary?) {
        val result =
            if (originalDiary == null) {
                FragmentResult.None
            } else {
                FragmentResult.Some(originalDiary.date)
            }
        emitNavigatePreviousFragmentEvent(result)
    }

    // SavedStateHandle対応State更新
    override fun updateUiState(state: DiaryEditState) {
        super.updateUiState(state)

        // MEMO:アプリ再起動後も安全に復元できる安定したUI状態のみをSavedStateHandleに保存する。
        //      LoadingやSavingなど、非同期処理中の一時的な状態を保存すると、
        //      再起動時に処理が中断されたままUIが固まる可能性があるため除外する。
        when (state) {
            DiaryEditState.Idle,
            DiaryEditState.Editing,
            DiaryEditState.LoadError,
            DiaryEditState.SelectingImage -> {
                // MEMO:これらの状態はユーザーインタラクションが可能、または明確な結果を示しているため保存対象とする。
                handle[SAVED_VIEW_MODEL_STATE_KEY] = state
            }

            DiaryEditState.CheckingDiaryInfo,
            DiaryEditState.Loading,
            DiaryEditState.Saving,
            DiaryEditState.Deleting,
            DiaryEditState.CheckingWeatherAvailability,
            DiaryEditState.FetchingWeatherInfo,
            DiaryEditState.AddingItem,
            DiaryEditState.DeletingItem -> {
                // MEMO:これらの一時的な処理中状態は、再起動後に意味をなさなくなるか、
                //      UIを不適切な状態でロックする可能性があるため保存しない。
            }
        }
    }

    private fun updateDate(date: LocalDate) {
        // HACK:下記はDiaryStateFlowのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        updatePreviousDate(diaryStateFlow.date.value)
        diaryStateFlow.date.value = date
    }

    private fun updateTitle(title: String) {
        diaryStateFlow.title.value = title
    }

    private fun updateWeather1(weather: Weather) {
        diaryStateFlow.weather1.value = weather
        if (weather == Weather.UNKNOWN || isEqualWeathers) updateWeather2(Weather.UNKNOWN)
    }

    private fun updateWeather2(weather: Weather) {
        diaryStateFlow.weather2.value = weather
    }

    private fun updateCondition(condition: Condition) {
        diaryStateFlow.condition.value = condition
    }

    private fun updateNumVisibleItems(num: Int) {
        diaryStateFlow.numVisibleItems.value = num
    }

    private fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        diaryStateFlow.updateItemTitle(itemNumber, title)
    }

    private fun updateItemComment(itemNumber: ItemNumber, comment: String) {
        diaryStateFlow.getItemStateFlow(itemNumber).comment.value = comment
    }

    private fun updateImageUri(imageUri: Uri?) {
        diaryStateFlow.imageUri.value = imageUri
    }

    private fun updateIsNewDiary(isNew: Boolean) {
        _isNewDiary.value = isNew
    }

    private fun updateOriginalDiary(diary: Diary?) {
        _originalDiary.value = diary
    }

    private fun updatePreviousDate(date: LocalDate?) {
        previousDate = date
    }

    private fun updateEditingDiaryDateString(dateString: String?) {
        _editingDiaryDateString.value = dateString
    }

    // TODO:テスト用の為、最終的に削除
    fun test() {
        viewModelScope.launch {
            isTesting = true
            val startDate = date.value
            if (startDate != null) {
                for (i in 0 until 10) {
                    val saveDate = startDate.minusDays(i.toLong())

                    when (val result = doesDiaryExistUseCase(saveDate)) {
                        is UseCaseResult.Success -> {
                            if (result.value) continue
                        }
                        is UseCaseResult.Failure -> {
                            emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
                            isTesting = false
                            return@launch
                        }
                    }
                    diaryStateFlow.initialize()
                    updateDate(saveDate)
                    val weather1Int = Random.nextInt(1, Weather.entries.size)
                    updateWeather1(Weather.of(weather1Int))
                    val weather2Int = Random.nextInt(1, Weather.entries.size)
                    updateWeather2(Weather.of(weather2Int))
                    val conditionInt = Random.nextInt(1, Condition.entries.size)
                    updateCondition(Condition.of(conditionInt))
                    val title = generateRandomAlphanumericString(15)
                    updateTitle(title)
                    val numItems = Random.nextInt(ItemNumber.MIN_NUMBER, ItemNumber.MAX_NUMBER + 1)
                    updateNumVisibleItems(numItems)
                    for (j in 1..numItems) {
                        val itemTitle = generateRandomAlphanumericString(15)
                        val itemComment = generateRandomAlphanumericString(50)
                        updateItemTitle(ItemNumber(j), itemTitle)
                        updateItemComment(ItemNumber(j), itemComment)
                        diaryStateFlow.getItemStateFlow(ItemNumber(j)).title.value = itemTitle
                        diaryStateFlow.getItemStateFlow(ItemNumber(j)).comment.value = itemComment
                    }


                    val diary = diaryStateFlow.createDiary()
                    val diaryItemTitleSelectionHistoryList =
                        diaryStateFlow.createDiaryItemTitleSelectionHistoryList()
                    val originalDiary = _originalDiary.requireValue()
                    val isNewDiary = _isNewDiary.value

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
                            return@launch
                        }
                    }
                }
            }
            navigatePreviousFragment(_originalDiary.value)
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
