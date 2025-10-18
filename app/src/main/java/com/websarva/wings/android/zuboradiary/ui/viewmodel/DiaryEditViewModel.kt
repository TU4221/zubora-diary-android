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
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
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
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CacheDiaryImageUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheFileClearException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageFilePathBuildingException
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
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryEditStateFlow
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val checkWeatherInfoFetchEnabledUseCase: CheckWeatherInfoFetchEnabledUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase,
    private val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val cacheDiaryImageUseCase: CacheDiaryImageUseCase,
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase,
    private val clearDiaryImageCacheFileUseCase: ClearDiaryImageCacheFileUseCase
) : BaseViewModel<DiaryEditEvent, DiaryEditAppMessage, DiaryEditState>(
    handle[SAVED_VIEW_MODEL_STATE_KEY] ?: DiaryEditState.Idle
) {

    companion object {
        // 呼び出し元のFragmentから受け取る引数のキー
        private const val DIARY_ID_ARGUMENT_KEY = "load_diary_id"
        private const val DIARY_DATE_ARGUMENT_KEY = "load_diary_date"

        // ViewModel状態保存キー
        // MEMO:システムの初期化によるプロセスの終了から(アプリ設定変更からのアプリ再起動時)の復元用
        private const val SAVED_VIEW_MODEL_STATE_KEY = "uiState"
        private const val SAVED_PREVIOUS_DATE_STATE_KEY = "previousDate"
        private const val SAVED_ORIGINAL_DIARY_KEY = "originalDiary"
        private const val SAVED_IS_NEW_DIARY_KEY = "isNewDiary"
    }

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
        set(value) {
            handle[SAVED_PREVIOUS_DATE_STATE_KEY] = value
            field = value
        }

    private val _isNewDiary = MutableStateFlow(handle[SAVED_IS_NEW_DIARY_KEY] ?: false)
    val isNewDiary = _isNewDiary.asStateFlow()

    private val _originalDiary = MutableStateFlow<Diary?>(null)
    val originalDiaryDate = _originalDiary
        .map { it?.date }
        .stateInWhileSubscribed(_originalDiary.value?.date)

    private val _editingDiaryDateString = MutableStateFlow<String?>(null)
    val editingDiaryDateString = _editingDiaryDateString.asStateFlow()

    private val diaryStateFlow = DiaryEditStateFlow(viewModelScope, handle)

    val date
        get() = diaryStateFlow.date.asStateFlow()

    val titleForBinding
        get() = diaryStateFlow.title

    val weather1
        get() = diaryStateFlow.weather1
            .map { it.toUiModel() }
            .stateInWhileSubscribed(diaryStateFlow.weather1.value.toUiModel())

    val weather2
        get() = diaryStateFlow.weather2
            .map { it.toUiModel() }
            .stateInWhileSubscribed(diaryStateFlow.weather2.value.toUiModel())

    private val isEqualWeathers: Boolean
        get() {
            val weather1 = diaryStateFlow.weather1.value
            val weather2 = diaryStateFlow.weather2.value

            return weather1 == weather2
        }

    val condition
        get() = diaryStateFlow.condition
            .map { it.toUiModel() }
            .stateInWhileSubscribed(diaryStateFlow.condition.value.toUiModel())

    val numVisibleItems
        get() = diaryStateFlow.numVisibleItems.asStateFlow()

    val item1TitleForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(1)).title

    val item1CommentForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(1)).comment

    val item2TitleForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(2)).title

    val item2CommentForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(2)).comment

    val item3TitleForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(3)).title

    val item3CommentMutable
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(3)).comment

    val item4TitleForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(4)).title

    val item4CommentForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(4)).comment

    val item5TitleForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(5)).title

    val item5CommentForBinding
        get() = diaryStateFlow.getItemStateFlow(DiaryItemNumber(5)).comment

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

    val imageFileName
        get() = diaryStateFlow.imageFileName
            .map { it?.fullName }
            .stateInWhileSubscribed(diaryStateFlow.imageFileName.value?.fullName)

    val imageFilePath
        get() = diaryStateFlow.imageFilePath.asStateFlow()

    val isImageDeleteButtonClickable =
        combine(uiState, imageFileName) { state, imageFileName ->
            return@combine when (state) {
                DiaryEditState.Editing -> {
                    imageFileName != null
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

    // キャッシュパラメータ
    private var pendingDiaryLoadParameters: DiaryLoadParameters? = null
    private var pendingDiaryUpdateParameters: DiaryUpdateParameters? = null
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    private var pendingDiaryItemDeleteParameters: DiaryItemDeleteParameters? = null
    private var pendingWeatherInfoFetchParameters: WeatherInfoFetchParameters? = null
    private var pendingPreviousNavigationParameters: PreviousNavigationParameters? = null

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    init {
        initializeDiaryData(handle)
        setUpStateSaveObservers()
    }

    private fun initializeDiaryData(handle: SavedStateHandle) {
        // MEMO:下記条件はアプリ設定変更時のアプリ再起動時の不要初期化対策
        if (uiState.value != DiaryEditState.Idle) return

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

    private fun setUpStateSaveObservers() {
        _isNewDiary.onEach {
            handle[SAVED_IS_NEW_DIARY_KEY] = it
        }.launchIn(viewModelScope)

        _originalDiary.onEach {
            handle[SAVED_ORIGINAL_DIARY_KEY] = it
        }.launchIn(viewModelScope)
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
        val diary = diaryStateFlow.createDiary()
        val originalDiary = _originalDiary.requireValue()
        launchWithUnexpectedErrorHandler {
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
        if (!canExecuteOperationWithUiUpdate) return
        val isNewDiary = _isNewDiary.value
        if (isNewDiary) return

        val originalDiary = _originalDiary.requireValue()
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                originalDiary.id,
                originalDiary.date
            )
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryDeleteDialog(originalDiary.date)
            )
        }

    }

    fun onNavigationClick() {
        val diary = diaryStateFlow.createDiary()
        val originalDiary = _originalDiary.requireValue()

        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    fun onDateInputFieldClick() {
        val date = this.date.requireValue()

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditEvent.NavigateDatePickerDialog(date)
            )
        }
    }

    fun onWeather1InputFieldItemClick(weather: WeatherUi) {
        updateWeather1(weather.toDomainModel())
    }

    fun onWeather2InputFieldItemClick(weather: WeatherUi) {
        updateWeather2(weather.toDomainModel())
    }

    fun onConditionInputFieldItemClick(condition: ConditionUi) {
        updateCondition(condition.toDomainModel())
    }

    fun onItemTitleInputFieldClick(itemNumberInt: Int) {
        if (uiState.value == DiaryEditState.AddingItem) return
        if (uiState.value == DiaryEditState.DeletingItem) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        val itemTitleId = getItemTitleId(itemNumber).value
        val itemTitle = getItemTitle(itemNumber).requireValue()

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryItemTitleEditFragment(
                    DiaryItemTitleSelectionUi(
                        itemNumberInt,
                        itemTitleId?.value,
                        itemTitle
                    )
                )
            )
        }
    }

    fun onItemAdditionButtonClick() {
        if (!canExecuteOperationWithUiUpdate) return

        launchWithUnexpectedErrorHandler {
            addDiaryItem()
        }
    }

    fun onItemDeleteButtonClick(itemNumberInt: Int) {
        if (!canExecuteOperationWithUiUpdate) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryItemDeleteParameters(itemNumber)
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(itemNumberInt)
            )
        }
    }

    fun onAttachedImageDeleteButtonClick() {
        if (!canExecuteOperationWithUiUpdate) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditEvent.NavigateDiaryImageDeleteDialog
            )
        }
    }

    fun onAttachedImageClick() {
        if (!canExecuteOperationWithUiUpdate) return

        launchWithUnexpectedErrorHandler {
            selectImage()
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
                handleDiaryLoadDialogNegativeResult()
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

    private fun handleDiaryLoadDialogNegativeResult() {
        val date = date.requireValue()
        val previousDate = previousDate

        launchWithUnexpectedErrorHandler {
            fetchWeatherInfo(date, previousDate)
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
        launchWithUnexpectedErrorHandler {
            processChangedDiaryDate(date, originalDate, isNewDiary)
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
            deleteImageUri()
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
                val titleId = result.data.id ?: throw IllegalArgumentException()
                updateItemTitle(
                    DiaryItemNumber(result.data.itemNumber),
                    DiaryItemTitleSelectionHistoryId(titleId),
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
        val diaryId = diaryStateFlow.id.value
        launchWithUnexpectedErrorHandler {
            diaryId?.let {
                cacheDiaryImage(uri, diaryId)
            } ?: throw IllegalStateException()
        }
    }

    // StateFlow値変更時処理
    fun onOriginalDiaryDateChanged(dateString: String?) {
        updateEditingDiaryDateString(dateString)
    }

    fun onDiaryImageFileNameChanged(fileName: String?) {
        launchWithUnexpectedErrorHandler {
            buildImageFilePath(fileName?.let { DiaryImageFileName(it) })
        }
    }

    // MotionLayout変更時処理
    fun onDiaryItemInvisibleStateTransitionCompleted(itemNumberInt: Int) {
        check(uiState.value == DiaryEditState.DeletingItem)

        val itemNumber = DiaryItemNumber(itemNumberInt)
        deleteItem(itemNumber)
    }

    fun onDiaryItemVisibleStateTransitionCompleted() {
        check(uiState.value == DiaryEditState.AddingItem)

        updateUiState(DiaryEditState.Editing)
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
        // TODO:handleからの復元処理見直し
        if (id == null) {
            prepareNewDiaryEntry(date)
        } else {
            loadDiaryById(id, date)
        }
    }

    private suspend fun prepareNewDiaryEntry(date: LocalDate) {
        updateIsNewDiary(true)
        updateId(DiaryId.generate())
        updateDate(date)
        updateOriginalDiary(
            handle[SAVED_ORIGINAL_DIARY_KEY] ?: diaryStateFlow.createDiary()
        )
        val previousDate = previousDate
        val originalDate = _originalDiary.requireValue().date
        val isNewDiary = isNewDiary.value
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

        val previousState = uiState.value
        Log.i(logTag, "${logMsg}_previousState: $previousState")
        updateUiState(DiaryEditState.Loading)
        when (val result = executeLoadDiary(id, date)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                diaryStateFlow.update(result.value)
                updateIsNewDiary(false)
                updateOriginalDiary(diaryStateFlow.createDiary())
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                if (previousState == DiaryEditState.Idle) {
                    updateUiState(DiaryEditState.LoadError)

                    // MEMO:連続するUIイベント（エラー表示と画面遷移）は、監視開始前に発行されると
                    //      取りこぼされる可能性がある。これを防ぐため、間に確認ダイアログを挟み、
                    //      ユーザーの応答を待ってから画面遷移を実行する。
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryLoadFailureDialog(date)
                    )
                } else {
                    updateUiState(DiaryEditState.Editing)
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

        updateUiState(DiaryEditState.Saving)
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
                updateUiState(DiaryEditState.Editing)
                clearDiaryImageCacheFile()
                emitUiEvent(
                    DiaryEditEvent
                        .NavigateDiaryShowFragment(diary.id.value, diary.date)
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateUiState(DiaryEditState.Editing)
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

        updateUiState(DiaryEditState.Deleting)
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                clearDiaryImageCacheFile()
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
        updateUiState(DiaryEditState.CheckingDiaryInfo)
        val result =
            shouldRequestDiaryLoadConfirmationUseCase(date, previousDate, originalDate, isNewDiary)
        when (result) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                if (result.value) {
                    updatePendingDiaryLoadParameters(date)
                    emitUiEvent(
                        DiaryEditEvent.NavigateDiaryLoadDialog(date)
                    )
                } else {
                    fetchWeatherInfo(date, previousDate)
                }
            }
            is UseCaseResult.Failure -> {
                updateUiState(DiaryEditState.Editing)
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
        updateUiState(DiaryEditState.CheckingDiaryInfo)
        val date = diary.date
        val originalDate = originalDiary.date
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, originalDate, isNewDiary)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
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
                updateUiState(DiaryEditState.Editing)
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
        updateUiState(DiaryEditState.CheckingWeatherAvailability)
        val isEnabled = checkWeatherInfoFetchEnabledUseCase().value
        updateUiState(DiaryEditState.Editing)
        if (!isEnabled) {
            return
        }

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
            updateUiState(DiaryEditState.Editing)
            emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
        }

        updateUiState(DiaryEditState.FetchingWeatherInfo)
        when (val result = fetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                updateUiState(DiaryEditState.Editing)
                updateWeather1(result.value)
                updateWeather2(Weather.UNKNOWN)
            }
            is UseCaseResult.Failure -> {
                updateUiState(DiaryEditState.Editing)
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
        val previousDate = previousDate
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
    private fun getItemTitleId(itemNumber: DiaryItemNumber): StateFlow<DiaryItemTitleSelectionHistoryId?> {
        return diaryStateFlow.getItemStateFlow(itemNumber).titleId
    }

    private fun getItemTitle(itemNumber: DiaryItemNumber): StateFlow<String?> {
        return diaryStateFlow.getItemStateFlow(itemNumber).title
    }

    // MEMO:日記項目追加処理完了時のUi更新(編集中)は日記項目追加完了イベントメソッドにて処理
    private suspend fun addDiaryItem() {
        updateUiState(DiaryEditState.AddingItem)
        emitUiEvent(DiaryEditEvent.ItemAddition)
        diaryStateFlow.incrementVisibleItemsCount()
    }

    // MEMO:日記項目削除処理完了時のUi更新(編集中)は日記項目削除メソッドにて処理
    private suspend fun requestDiaryItemDeleteTransition(itemNumber: DiaryItemNumber) {
        val numVisibleItems = numVisibleItems.requireValue()

        updateUiState(DiaryEditState.DeletingItem)
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
        diaryStateFlow.deleteItem(itemNumber)
        updateUiState(DiaryEditState.Editing)
    }

    // 添付画像関係
    // MEMO:画像選択完了時のUi更新(編集中)は画像選択完了イベントメソッドにて処理
    private suspend fun selectImage() {
        updateUiState(DiaryEditState.SelectingImage)
        emitUiEvent(DiaryEditEvent.SelectImage)
    }

    private suspend fun deleteImageUri() {
        updateImageFileName(null)
        clearDiaryImageCacheFile()
    }

    private suspend fun cacheDiaryImage(uri: Uri?, diaryId: DiaryId) {
        if (uri != null) {
            val result =
                cacheDiaryImageUseCase(uri.toString(), diaryId)
            when (result) {
                is UseCaseResult.Success -> {
                    updateImageFileName(result.value)
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
        updateUiState(DiaryEditState.Editing)
    }

    private suspend fun clearDiaryImageCacheFile() {
        when (val result = clearDiaryImageCacheFileUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "画像キャッシュファイルクリア失敗", result.exception)
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

    private suspend fun buildImageFilePath(fileName: DiaryImageFileName?) {
        val imageFilePathUi =
            if (fileName == null) {
                null
            } else {
                val result = buildDiaryImageFilePathUseCase(fileName)
                when (result) {
                    is UseCaseResult.Success -> {
                        FilePathUi.Available(result.value)
                    }
                    is UseCaseResult.Failure -> {
                        when (result.exception) {
                            is DiaryImageFilePathBuildingException.BuildingFailure -> {
                                emitAppMessageEvent(DiaryEditAppMessage.ImageLoadFailure)
                            }
                            is DiaryImageFilePathBuildingException.Unknown -> {
                                emitUnexpectedAppMessage(result.exception)
                            }
                        }
                        FilePathUi.Unavailable
                    }
                }
            }
        updateImageFilePath(imageFilePathUi)
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

    private fun updateId(id: DiaryId) {
        diaryStateFlow.id.value = id
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

    private fun updateItemTitle(
        itemNumber: DiaryItemNumber,
        titleId: DiaryItemTitleSelectionHistoryId,
        title: String
    ) {
        diaryStateFlow.updateItemTitle(itemNumber, titleId, title)
    }

    private fun updateItemComment(itemNumber: DiaryItemNumber, comment: String) {
        diaryStateFlow.getItemStateFlow(itemNumber).comment.value = comment
    }

    private fun updateImageFileName(imageFileName: DiaryImageFileName?) {
        diaryStateFlow.imageFileName.value = imageFileName
    }

    private fun updateImageFilePath(imageFilePath: FilePathUi?) {
        diaryStateFlow.imageFilePath.value = imageFilePath
    }

    private fun updateIsNewDiary(isNew: Boolean) {
        _isNewDiary.value = isNew
    }

    private fun updateOriginalDiary(diary: Diary) {
        _originalDiary.value = diary
    }

    private fun updatePreviousDate(date: LocalDate?) {
        previousDate = date
    }

    private fun updateEditingDiaryDateString(dateString: String?) {
        _editingDiaryDateString.value = dateString
    }

    private fun updatePendingDiaryLoadParameters(date: LocalDate) {
        pendingDiaryLoadParameters = DiaryLoadParameters(date)
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

    private data class DiaryLoadParameters(
        val date: LocalDate
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

    private data class DiaryItemDeleteParameters(
        val itemNumber: DiaryItemNumber
    )

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
            val startDate = date.value
            if (startDate != null) {
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
                    diaryStateFlow.initialize()
                    updateDate(saveDate)
                    val weather1Int = Random.nextInt(1, WeatherUi.entries.size)
                    updateWeather1(Weather.of(weather1Int))
                    val weather2Int = Random.nextInt(1, WeatherUi.entries.size)
                    updateWeather2(Weather.of(weather2Int))
                    val conditionInt = Random.nextInt(1, ConditionUi.entries.size)
                    updateCondition(Condition.of(conditionInt))
                    val title = generateRandomAlphanumericString(15)
                    updateTitle(title)
                    val numItems = Random.nextInt(DiaryItemNumber.MIN_NUMBER, DiaryItemNumber.MAX_NUMBER + 1)
                    updateNumVisibleItems(numItems)
                    for (j in 1..numItems) {
                        val itemTitle = generateRandomAlphanumericString(15)
                        val itemComment = generateRandomAlphanumericString(50)
                        updateItemTitle(
                            DiaryItemNumber(j),
                            DiaryItemTitleSelectionHistoryId.generate(),
                            itemTitle
                        )
                        val diaryItemNumber = DiaryItemNumber(j)
                        updateItemComment(diaryItemNumber, itemComment)
                        diaryStateFlow.getItemStateFlow(diaryItemNumber).title.value = itemTitle
                        diaryStateFlow.getItemStateFlow(diaryItemNumber).comment.value = itemComment
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
                            updateUiState(DiaryEditState.Editing)
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
            }
            clearDiaryImageCacheFile()
            navigatePreviousFragment(_originalDiary.requireValue().date)
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
