package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.FetchWeatherInfoUseCaseException
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryFetchConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySavingConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteDiaryUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.IsWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadingParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.ItemTitleEditResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal class DiaryEditViewModel @Inject constructor(
    private val handle: SavedStateHandle, // MEMO:システムの初期化によるプロセスの終了からの復元用
    private val shouldRequestExitWithoutDiarySavingConfirmationUseCase: ShouldRequestExitWithoutDiarySavingConfirmationUseCase,
    private val shouldRequestDiaryFetchConfirmationUseCase: ShouldRequestDiaryFetchConfirmationUseCase,
    private val shouldRequestDiaryUpdateConfirmationUseCase: ShouldRequestDiaryUpdateConfirmationUseCase,
    private val shouldRequestWeatherInfoConfirmationUseCase: ShouldRequestWeatherInfoConfirmationUseCase,
    private val fetchDiaryUseCase: FetchDiaryUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val isWeatherInfoFetchEnabledUseCase: IsWeatherInfoFetchEnabledUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase,
    private val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
) : BaseViewModel<DiaryEditEvent, DiaryEditAppMessage, DiaryEditState>(
    DiaryEditState.Idle
) {

    companion object {
        private const val SAVED_HAS_PREPARED_DIARY_STATE_KEY = "hasPreparedDiary"
        private const val SAVED_PREVIOUS_DATE_STATE_KEY = "previousDate"
        private const val SAVED_LOADED_DIARY_KEY = "loadedDiary"
        private const val SAVED_SHOULD_INITIALIZE_ON_FRAGMENT_DESTROY_STATE_KEY =
            "shouldInitializeOnFragmentDestroy"
    }

    private val logTag = createLogTag()

    override val isProcessingState =
        viewModelState
            .map { state ->
                when (state) {
                    DiaryEditState.CheckingDiaryInfo,
                    DiaryEditState.Loading,
                    DiaryEditState.Saving,
                    DiaryEditState.Deleting,
                    DiaryEditState.ItemAdding,
                    DiaryEditState.ItemDeleting,
                    DiaryEditState.PictureSelecting,
                    DiaryEditState.CheckingWeatherAvailability,
                    DiaryEditState.FetchingWeatherInfo -> true

                    DiaryEditState.Idle,
                    DiaryEditState.Editing -> false
                }
            }.stateInDefault(
                viewModelScope,
                false
            )


    // 日記データ関係
    private val initialHasPreparedDiary = false
    private var hasPreparedDiary = handle[SAVED_HAS_PREPARED_DIARY_STATE_KEY] ?: initialHasPreparedDiary
        private set(value) {
            handle[SAVED_HAS_PREPARED_DIARY_STATE_KEY] = value
            field = value
        }

    private val initialPreviousDate: LocalDate? = null
    private var previousDate = handle[SAVED_PREVIOUS_DATE_STATE_KEY] ?: initialPreviousDate
        private set(value) {
            handle[SAVED_PREVIOUS_DATE_STATE_KEY] = value
            field = value
        }

    private val initialLoadedDiary: Diary? = null
    private val _loadedDiary = MutableStateFlow(handle[SAVED_LOADED_DIARY_KEY] ?: initialLoadedDiary)
    val loadedDiary = _loadedDiary.asStateFlow()

    private val initialDiaryDateString = ""
    private val _editingDiaryDateString = MutableStateFlow(initialDiaryDateString)
    val editingDiaryDateString = _editingDiaryDateString.asStateFlow()

    private val diaryStateFlow = DiaryStateFlow(viewModelScope, handle)

    val date
        get() = diaryStateFlow.date.asStateFlow()

    /**
     * LayoutDataBinding用
     * */
    val titleMutableStateFlow
        get() = diaryStateFlow.title

    val weather1
        get() = diaryStateFlow.weather1.asStateFlow()

    private val initialWeatherAdapterList = WeatherAdapterList()
    private val _weather1AdapterList = MutableStateFlow(initialWeatherAdapterList)
    val weather1AdapterList
        get() = _weather1AdapterList.asStateFlow()

    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()

    private val _weather2AdapterList = MutableStateFlow(initialWeatherAdapterList)
    val weather2AdapterList
        get() = _weather2AdapterList.asStateFlow()

    private val isEqualWeathers: Boolean
        get() {
            val weather1 = diaryStateFlow.weather1.value
            val weather2 = diaryStateFlow.weather2.value

            return weather1 == weather2
        }

    val condition
        get() = diaryStateFlow.condition.asStateFlow()

    private val initialConditionAdapterList = ConditionAdapterList()
    private val _conditionAdapterList = MutableStateFlow(initialConditionAdapterList)
    val conditionAdapterList
        get() = _conditionAdapterList.asStateFlow()

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
        combine(viewModelState, numVisibleItems) { state, numVisibleItems ->
            return@combine when (state) {
                DiaryEditState.Editing -> {
                    numVisibleItems < 5
                }

                DiaryEditState.Idle,
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.ItemAdding,
                DiaryEditState.ItemDeleting,
                DiaryEditState.PictureSelecting -> false
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val picturePath
        get() = diaryStateFlow.picturePath.asStateFlow()

    val isPicturePathDeleteButtonClickable =
        combine(viewModelState, picturePath) { state, picturePath ->
            return@combine when (state) {
                DiaryEditState.Editing -> {
                    picturePath != null
                }

                DiaryEditState.Idle,
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.ItemAdding,
                DiaryEditState.ItemDeleting,
                DiaryEditState.PictureSelecting -> false
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ProgressIndicator表示
    val isVisibleUpdateProgressBar: StateFlow<Boolean> =
        viewModelState.map { state ->
            return@map when (state) {
                DiaryEditState.CheckingDiaryInfo,
                DiaryEditState.Loading,
                DiaryEditState.Saving,
                DiaryEditState.Deleting,
                DiaryEditState.CheckingWeatherAvailability,
                DiaryEditState.FetchingWeatherInfo,
                DiaryEditState.PictureSelecting -> true

                DiaryEditState.Idle,
                DiaryEditState.Editing,
                DiaryEditState.ItemAdding,
                DiaryEditState.ItemDeleting -> false
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ViewModel初期化関係
    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroy = false
    var shouldInitializeOnFragmentDestroy =
        handle[SAVED_SHOULD_INITIALIZE_ON_FRAGMENT_DESTROY_STATE_KEY]
            ?: initialShouldInitializeOnFragmentDestroy
        set(value) {
            handle[SAVED_SHOULD_INITIALIZE_ON_FRAGMENT_DESTROY_STATE_KEY] = value
            field = value
        }

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    init {
        _loadedDiary.onEach {
            handle[SAVED_LOADED_DIARY_KEY] = it
        }.launchIn(viewModelScope)
    }

    override fun initialize() {
        super.initialize()
        hasPreparedDiary = initialHasPreparedDiary
        previousDate = initialPreviousDate
        _loadedDiary.value = initialLoadedDiary
        _editingDiaryDateString.value = initialDiaryDateString
        diaryStateFlow.initialize()
        _weather1AdapterList.value = initialWeatherAdapterList
        _weather2AdapterList.value = initialWeatherAdapterList
        _conditionAdapterList.value = initialConditionAdapterList
        shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        if (isProcessing) return

        val diary = diaryStateFlow.createDiary()
        val loadedDiary = _loadedDiary.value
        viewModelScope.launch {
            handleBackNavigation(diary, loadedDiary)
        }
    }

    // ViewClicked処理
    fun onDiarySaveMenuClicked() {
        if (isProcessing) return

        val diary = diaryStateFlow.createDiary()
        val diaryItemTitleSelectionHistoryList =
            diaryStateFlow.createDiaryItemTitleSelectionHistoryList()
        val loadedDiary = this._loadedDiary.value

        viewModelScope.launch {
            requestDiaryUpdateConfirmation(
                diary,
                diaryItemTitleSelectionHistoryList,
                loadedDiary
            ) {
                saveDiary(
                    diary,
                    diaryItemTitleSelectionHistoryList,
                    loadedDiary
                )
            }
        }
    }

    fun onDiaryDeleteMenuClicked() {
        if (isProcessing) return

        val loadedDiary = _loadedDiary.value ?: return
        val loadedDate = loadedDiary.date
        val loadedPicturePath = loadedDiary.picturePath
        val parameters = DiaryDeleteParameters(loadedDate, loadedPicturePath)

        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryDeleteDialog(parameters)
            )
        }

    }

    fun onNavigationClicked() {
        if (isProcessing) return

        val diary = diaryStateFlow.createDiary()
        val loadedDiary = _loadedDiary.value
        viewModelScope.launch {
            handleBackNavigation(diary, loadedDiary)
        }
    }

    fun onDateInputFieldClicked() {
        if (isProcessing) return

        val date = this.date.requireValue()

        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDatePickerDialog(date)
            )
        }
    }

    fun onWeather1InputFieldItemClicked(weather: Weather) {
        updateWeather1(weather)
    }

    fun onWeather2InputFieldItemClicked(weather: Weather) {
        updateWeather2(weather)
    }

    fun onConditionInputFieldItemClicked(condition: Condition) {
        updateCondition(condition)
    }

    fun onItemTitleInputFieldClicked(itemNumber: ItemNumber) {
        if (isProcessing) return

        val itemTitle = getItemTitle(itemNumber).requireValue()

        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryItemTitleEditFragment(itemNumber, itemTitle)
            )
        }
    }

    fun onItemAdditionButtonClicked() {
        if (isProcessing) return

        viewModelScope.launch {
            addDiaryItem()
        }
    }

    fun onItemDeleteButtonClicked(itemNumber: ItemNumber) {
        if (isProcessing) return

        viewModelScope.launch {
            val parameters = DiaryItemDeleteParameters(itemNumber)
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(parameters)
            )
        }
    }

    fun onAttachedPictureDeleteButtonClicked() {
        if (isProcessing) return

        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryPictureDeleteDialog
            )
        }
    }

    fun onAttachedPictureClicked() {
        if (isProcessing) return

        viewModelScope.launch {
            selectPicture()
        }
    }

    // Fragmentからの結果受取処理
    fun onDiaryLoadingDialogResultReceived(result: DialogResult<DiaryLoadingParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryLoadingParameters> -> {
                onDiaryLoadingDialogPositiveResultReceived(result.data)
            }
            is DialogResult.Negative,
            is DialogResult.Cancel -> {
                onDiaryLoadingDialogNegativeResultReceived()
            }
        }
    }

    private fun onDiaryLoadingDialogPositiveResultReceived(parameters: DiaryLoadingParameters) {
        val date = parameters.date
        viewModelScope.launch {
            loadDiary(date)
        }
    }

    private fun onDiaryLoadingDialogNegativeResultReceived() {

        val date = date.requireValue()
        val previousDate = previousDate

        viewModelScope.launch {
            processWeatherInfoFetch(date, previousDate)
        }
    }

    fun onDiaryUpdateDialogResultReceived(result: DialogResult<DiaryUpdateParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryUpdateParameters> -> {
                onDiaryUpdateDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryUpdateDialogPositiveResultReceived(parameters: DiaryUpdateParameters) {
        val diary = parameters.diary
        val diaryItemTitleSelectionHistoryList =
            parameters.diaryItemTitleSelectionHistoryItemList
        val loadedDiary = parameters.loadedDiary
        viewModelScope.launch {
            saveDiary(
                diary,
                diaryItemTitleSelectionHistoryList,
                loadedDiary
            )
        }
    }

    fun onDiaryDeleteDialogResultReceived(result: DialogResult<DiaryDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryDeleteParameters> -> {
                onDiaryDeleteDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryDeleteDialogPositiveResultReceived(parameters: DiaryDeleteParameters) {
        val loadedDate = parameters.loadedDate
        val loadedPicturePath = parameters.loadedPicturePath
        viewModelScope.launch {
            deleteDiary(loadedDate, loadedPicturePath)
        }
    }

    fun onDatePickerDialogResultReceived(result: DialogResult<LocalDate>) {
        when (result) {
            is DialogResult.Positive<LocalDate> -> {
                onDatePickerDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDatePickerDialogPositiveResultReceived(date: LocalDate) {
        val loadedDate = _loadedDiary.value?.date
        viewModelScope.launch {
            prepareDiaryDate(date, loadedDate)
        }
    }

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

    fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<WeatherInfoFetchParameters>) {
        when (result) {
            is DialogResult.Positive<WeatherInfoFetchParameters> -> {
                onWeatherInfoFetchDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onWeatherInfoFetchDialogPositiveResultReceived(
        parameters: WeatherInfoFetchParameters
    ) {
        viewModelScope.launch {
            checkPermissionBeforeWeatherInfoFetch(parameters)
        }
    }

    fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<DiaryItemDeleteParameters>) {
        when (result) {
            is DialogResult.Positive<DiaryItemDeleteParameters> -> {
                onDiaryItemDeleteDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryItemDeleteDialogPositiveResultReceived(parameters: DiaryItemDeleteParameters) {
        val itemNumber = parameters.itemNumber
        viewModelScope.launch {
            requestDiaryItemDeleteTransition(itemNumber)
        }
    }

    fun onDiaryPictureDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onDiaryPictureDeleteDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryPictureDeleteDialogPositiveResultReceived() {
        deletePicturePath()
    }

    fun onExitWithoutDiarySavingDialogResultReceived(
        result: DialogResult<NavigatePreviousParameters>
    ) {
        when (result) {
            is DialogResult.Positive<NavigatePreviousParameters> -> {
                val loadedDiary = result.data.loadedDiary
                viewModelScope.launch {
                    navigatePreviousFragment(loadedDiary)
                }
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    fun onItemTitleEditFragmentResultReceived(result: FragmentResult<ItemTitleEditResult>) {
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
    fun onOpenDocumentResultPicturePathReceived(uri: Uri?) {
        updatePicturePath(uri)
    }

    // Fragment状態処理
    fun onDiaryDataSetUp(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {
        val loadedDate = _loadedDiary.value?.date
        viewModelScope.launch {
            prepareDiary(date, loadedDate, shouldLoadDiary)
        }
    }

    // StateFlow値変更時処理
    fun onWeather1Changed() {
        updateWeather2AdapterList()
    }

    fun onLoadedDiaryChangedUpdateEditingDiaryDateString(dateString: String?) {
        _editingDiaryDateString.value = dateString ?: initialDiaryDateString
    }

    // MotionLayout変更時処理
    fun onDiaryItemHidedStateTransitionCompleted(itemNumber: ItemNumber) {
        deleteItem(itemNumber)
    }

    fun onDiaryItemShowedStateTransitionCompleted() {
        updateViewModelState(DiaryEditState.Editing)
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

    // 日記データ処理関係
    private suspend fun prepareDiary(
        date: LocalDate,
        loadedDate: LocalDate?,
        shouldLoadDiary: Boolean
    ) {
        if (hasPreparedDiary) return

        if (shouldLoadDiary) {
            loadDiary(date)
        } else {
            prepareDiaryDate(date, loadedDate)
        }

        hasPreparedDiary = true
    }

    private suspend fun prepareDiaryDate(
        date: LocalDate,
        loadedDate: LocalDate?,
    ) {
        updateDate(date)
        val previousDate = previousDate

        // MEMO:下記処理をdate(StateFlow)変数のCollectorから呼び出すと、
        //      画面回転時にも不要に呼び出してしまう為、下記にて処理。
        requestDiaryLoadingConfirmation(date, previousDate, loadedDate) {
            processWeatherInfoFetch(date, previousDate)
        }
    }

    private suspend fun loadDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateViewModelState(DiaryEditState.Loading)
        when (val result = fetchDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                updateViewModelState(DiaryEditState.Editing)
                val diary = result.value

                // HACK:下記はDiaryStateFlow#update()処理よりも前に処理すること。
                //      (後で処理するとDiaryStateFlowのDateのObserverがloadedDateの更新よりも先に処理される為)
                updateLoadedDiary(diary)

                diaryStateFlow.update(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                if (hasPreparedDiary) {
                    updateViewModelState(DiaryEditState.Editing)
                    emitAppMessageEvent(DiaryEditAppMessage.DiaryFetchFailure)
                } else {
                    updateViewModelState(DiaryEditState.Editing)
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingFailureDialog(date)
                    )
                }
            }
        }
    }

    private fun updateLoadedDiary(diary: Diary) {
        _loadedDiary.value = diary
    }

    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        loadedDiary: Diary?
    ) {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        updateViewModelState(DiaryEditState.Saving)
        val result =
            saveDiaryUseCase(
                diary,
                diaryItemTitleSelectionHistoryItemList,
                loadedDiary
            )
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateViewModelState(DiaryEditState.Idle)
                emitViewModelEvent(
                    DiaryEditEvent
                        .NavigateDiaryShowFragment(diary.date)
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateViewModelState(DiaryEditState.Editing)
                emitAppMessageEvent(DiaryEditAppMessage.DiarySavingFailure)
            }
        }
    }

    private suspend fun deleteDiary(
        loadedDate: LocalDate,
        loadedPicturePath: Uri?
    ) {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        updateViewModelState(DiaryEditState.Deleting)
        when (val result = deleteDiaryUseCase(loadedDate, loadedPicturePath)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateViewModelState(DiaryEditState.Idle)
                emitViewModelEvent(
                    DiaryEditEvent
                        .NavigatePreviousFragmentOnDiaryDelete(
                            FragmentResult.Some(loadedDate)
                        )
                )
            }
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is DeleteDiaryUseCaseException.DeleteDiaryFailed -> {
                        Log.e(logTag, "${logMsg}失敗")
                        updateViewModelState(DiaryEditState.Editing)
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
                    }
                    is DeleteDiaryUseCaseException.RevokePersistentAccessUriFailed -> {
                        Log.i(logTag, "${logMsg}完了(Uri開放失敗)")
                        updateViewModelState(DiaryEditState.Idle)
                        emitViewModelEvent(
                            DiaryEditEvent
                                .NavigatePreviousFragmentOnDiaryDelete(
                                    FragmentResult.Some(loadedDate)
                                )
                        )
                    }
                }
            }
        }
    }

    private suspend fun requestDiaryLoadingConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        loadedDate: LocalDate?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.CheckingDiaryInfo)
        val result = shouldRequestDiaryFetchConfirmationUseCase(date, previousDate, loadedDate)
        when (result) {
            is UseCaseResult.Success -> {
                updateViewModelState(DiaryEditState.Editing)
                if (result.value) {
                    val parameters = DiaryLoadingParameters(date)
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingDialog(
                            parameters
                        )
                    )
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Failure -> {
                updateViewModelState(DiaryEditState.Editing)
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
            }
        }
    }

    private suspend fun requestDiaryUpdateConfirmation(
        diary: Diary,
        diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
        loadedDiary: Diary?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.CheckingDiaryInfo)
        val date = diary.date
        val loadedDate = loadedDiary?.date
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, loadedDate)) {
            is UseCaseResult.Success -> {
                updateViewModelState(DiaryEditState.Editing)
                if (result.value) {
                    val parameters = DiaryUpdateParameters(
                        diary,
                        diaryItemTitleSelectionHistoryItemList,
                        loadedDiary
                    )
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryUpdateDialog(parameters)
                    )
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Failure -> {
                updateViewModelState(DiaryEditState.Editing)
                emitAppMessageEvent(
                    DiaryEditAppMessage.DiarySavingFailure
                )
            }
        }
    }

    // 天気情報取得関係
    // TODO:コールバック構成の代替案を検討する。(他処理メソッドも同様に)
    // TODO:State更新タイミングの代替案を検討する。(他処理メソッドも同様に)
    private suspend fun processWeatherInfoFetch(date: LocalDate, previousDate: LocalDate?) {
        updateViewModelState(DiaryEditState.CheckingWeatherAvailability)
        val isEnabled = isWeatherInfoFetchEnabledUseCase().value
        updateViewModelState(DiaryEditState.Editing)
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
            emitViewModelEvent(
                DiaryEditEvent.NavigateWeatherInfoFetchDialog(parameters)
            )
        } else {
            onConfirmationNotNeeded()
        }
    }

    private suspend fun checkPermissionBeforeWeatherInfoFetch(
        parameters: WeatherInfoFetchParameters
    ) {
        emitViewModelEvent(
            DiaryEditEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch(parameters)
        )
    }

    private suspend fun fetchWeatherInfo(
        isGranted: Boolean,
        date: LocalDate
    ) {
        updateViewModelState(DiaryEditState.FetchingWeatherInfo)
        when (val result = fetchWeatherInfoUseCase(isGranted, date)) {
            is UseCaseResult.Success -> {
                updateViewModelState(DiaryEditState.Editing)
                updateWeather1(result.value)
                updateWeather2(Weather.UNKNOWN)
            }
            is UseCaseResult.Failure -> {
                updateViewModelState(DiaryEditState.Editing)
                when (result.exception) {
                    is FetchWeatherInfoUseCaseException.LocationPermissionNotGranted -> {
                        emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
                    }
                    is FetchWeatherInfoUseCaseException.AccessLocationFailed -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                    is FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is FetchWeatherInfoUseCaseException.FetchWeatherInfoFailed -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                }
            }
        }
    }

    // 日付関係
    private fun updateDate(date: LocalDate) {
        // HACK:下記はDiaryStateFlowのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        this@DiaryEditViewModel.previousDate = diaryStateFlow.date.value
        diaryStateFlow.date.value = date
    }

    // 天気、体調関係
    private fun updateWeather1(weather: Weather) {
        diaryStateFlow.weather1.value = weather
        if (weather == Weather.UNKNOWN || isEqualWeathers) updateWeather2(Weather.UNKNOWN)
    }

    private fun updateWeather2(weather: Weather) {
        diaryStateFlow.weather2.value = weather
    }

    private fun updateWeather2AdapterList() {
        val weather1 = diaryStateFlow.weather1.requireValue()
        _weather2AdapterList.value =
            if (weather1 == Weather.UNKNOWN) {
                initialWeatherAdapterList
            } else {
                WeatherAdapterList(weather1)
            }
    }

    private fun updateCondition(condition: Condition) {
        diaryStateFlow.condition.value = condition
    }

    // 項目関係
    private fun getItemTitle(itemNumber: ItemNumber): StateFlow<String> {
        return diaryStateFlow.getItemStateFlow(itemNumber).title
    }

    private suspend fun addDiaryItem() {
        updateViewModelState(DiaryEditState.ItemAdding)
        emitViewModelEvent(DiaryEditEvent.ItemAddition)
        diaryStateFlow.incrementVisibleItemsCount()
    }

    private fun deleteItem(itemNumber: ItemNumber) {
        diaryStateFlow.deleteItem(itemNumber)
        updateViewModelState(DiaryEditState.Editing)
    }

    private fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        diaryStateFlow.updateItemTitle(itemNumber, title)
    }

    private suspend fun requestDiaryItemDeleteTransition(itemNumber: ItemNumber) {
        val numVisibleItems = numVisibleItems.requireValue()

        updateViewModelState(DiaryEditState.ItemDeleting)
        if (itemNumber.value == 1 && numVisibleItems == itemNumber.value) {
            deleteItem(itemNumber)
        } else {
            emitViewModelEvent(
                DiaryEditEvent.TransitionDiaryItemHidedState(itemNumber)
            )
        }
        //updateViewModelIdleState() MEMO:deleteItem(itemNumber)でIdleStateに更新する為、不要。
    }

    // 添付写真関係
    private suspend fun selectPicture() {
        updateViewModelState(DiaryEditState.PictureSelecting)
        emitViewModelEvent(DiaryEditEvent.SelectPicture)
    }

    private fun updatePicturePath(uri: Uri?) {
        diaryStateFlow.picturePath.value = uri
        updateViewModelState(DiaryEditState.Editing)
    }

    private fun deletePicturePath() {
        diaryStateFlow.picturePath.value = null
    }

    private suspend fun handleBackNavigation(
        diary: Diary,
        loadedDiary: Diary?
    ) {
        val shouldRequest =
            shouldRequestExitWithoutDiarySavingConfirmationUseCase(diary, loadedDiary).value
        if (shouldRequest) {
            val parameters = NavigatePreviousParameters(loadedDiary)
            emitViewModelEvent(
                DiaryEditEvent
                    .NavigateExitWithoutDiarySavingConfirmationDialog(parameters)
            )
        } else {
            navigatePreviousFragment(loadedDiary)
        }
    }

    private suspend fun navigatePreviousFragment(loadedDiary: Diary? = null) {
        updateViewModelState(DiaryEditState.Idle)
        val result =
            if (loadedDiary == null) {
                FragmentResult.None
            } else {
                FragmentResult.Some(loadedDiary.date)
            }
        emitViewModelEvent(DiaryEditEvent.NavigatePreviousFragment(result))
    }

    // TODO:テスト用の為、最終的に削除
    fun test() {
        viewModelScope.launch {
            isTesting = true
            val startDate = date.value
            if (startDate != null) {
                for (i in 0 until 10) {
                    val savingDate = startDate.minusDays(i.toLong())

                    when (val result = doesDiaryExistUseCase(savingDate)) {
                        is UseCaseResult.Success -> {
                            if (result.value) continue
                        }
                        is UseCaseResult.Failure -> {
                            emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
                            isTesting = false
                            return@launch
                        }
                    }
                    diaryStateFlow.initialize()
                    updateDate(savingDate)
                    val weather1Int = Random.nextInt(1, Weather.entries.size)
                    updateWeather1(Weather.of(weather1Int))
                    val weather2Int = Random.nextInt(1, Weather.entries.size)
                    updateWeather2(Weather.of(weather2Int))
                    val conditionInt = Random.nextInt(1, Condition.entries.size)
                    updateCondition(Condition.of(conditionInt))
                    val title = generateRandomAlphanumericString(15)
                    diaryStateFlow.title.value = title
                    val numItems = Random.nextInt(ItemNumber.MIN_NUMBER, ItemNumber.MAX_NUMBER + 1)
                    diaryStateFlow.numVisibleItems.value = numItems
                    for (j in 1..numItems) {
                        val itemTitle = generateRandomAlphanumericString(15)
                        val itemComment = generateRandomAlphanumericString(50)
                        diaryStateFlow.getItemStateFlow(ItemNumber(j)).title.value = itemTitle
                        diaryStateFlow.getItemStateFlow(ItemNumber(j)).comment.value = itemComment
                    }


                    val diary = diaryStateFlow.createDiary()
                    val diaryItemTitleSelectionHistoryList =
                        diaryStateFlow.createDiaryItemTitleSelectionHistoryList()
                    val loadedDiary = _loadedDiary.value
                    val result =
                        saveDiaryUseCase(
                            diary,
                            diaryItemTitleSelectionHistoryList,
                            loadedDiary
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
            val loadedDiary = _loadedDiary.value
            navigatePreviousFragment(loadedDiary)
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
