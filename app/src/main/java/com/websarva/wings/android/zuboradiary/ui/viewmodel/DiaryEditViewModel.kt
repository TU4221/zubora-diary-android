package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.data.usecase.diary.error.FetchWeatherInfoError
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.LoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldLoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldRequestDiaryLoadingConfirmationUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.error.DeleteDiaryError
import com.websarva.wings.android.zuboradiary.data.usecase.settings.IsWeatherInfoAcquisitionEnabledUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditEvent
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadingParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoAcquisitionParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.ItemTitleEditResult
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryEditState
import com.websarva.wings.android.zuboradiary.ui.model.state.ViewModelState
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
    private val handle: SavedStateHandle,
    private val shouldRequestDiaryLoadingConfirmationUseCase: ShouldRequestDiaryLoadingConfirmationUseCase,
    private val shouldRequestDiaryUpdateConfirmationUseCase: ShouldRequestDiaryUpdateConfirmationUseCase,
    private val shouldRequestWeatherInfoConfirmationUseCase: ShouldRequestWeatherInfoConfirmationUseCase,
    private val loadDiaryUseCase: LoadDiaryUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val isWeatherInfoAcquisitionEnabledUseCase: IsWeatherInfoAcquisitionEnabledUseCase,
    private val loadWeatherInfoUseCase: LoadWeatherInfoUseCase,
    private val shouldLoadWeatherInfoUseCase: ShouldLoadWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) : BaseViewModel<DiaryEditEvent, DiaryEditAppMessage, DiaryEditState>() {

    companion object {
        private const val SAVED_HAS_PREPARED_DIARY_STATE_KEY = "hasPreparedDiary"
        private const val SAVED_PREVIOUS_DATE_STATE_KEY = "previousDate"
        private const val SAVED_LOADED_DATE_STATE_KEY = "loadedDate"
        private const val SAVED_LOADED_PICTURE_PATH_STATE_KEY = "loadedPicturePath"
        private const val SAVED_SHOULD_INITIALIZE_ON_FRAGMENT_DESTROY_STATE_KEY =
            "shouldInitializeOnFragmentDestroy"
    }

    private val logTag = createLogTag()

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

    private val initialLoadedDate: LocalDate? = null
    private val _loadedDate =
        MutableStateFlow( handle[SAVED_LOADED_DATE_STATE_KEY] ?: initialLoadedDate)
    val loadedDate
        get() = _loadedDate.asStateFlow()

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
                ViewModelState.Idle -> {
                    numVisibleItems < 5
                }
                else -> false
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
                ViewModelState.Idle -> {
                    picturePath != null
                }
                else -> false
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val initialLoadedPicturePath: Uri? = null
    private var loadedPicturePath =
        handle[SAVED_LOADED_PICTURE_PATH_STATE_KEY] ?: initialLoadedPicturePath
        private set(value) {
            handle[SAVED_LOADED_PICTURE_PATH_STATE_KEY] = value
            field = value
        }

    // ProgressIndicator表示
    val isVisibleUpdateProgressBar: StateFlow<Boolean> =
        viewModelState.map { state ->
            return@map when (state) {
                ViewModelState.Idle,
                DiaryEditState.ItemAdding,
                DiaryEditState.ItemDeleting -> false
                else -> true
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
        _loadedDate.onEach {
            handle[SAVED_LOADED_DATE_STATE_KEY] = it
        }.launchIn(viewModelScope)
    }

    override fun initialize() {
        super.initialize()
        hasPreparedDiary = initialHasPreparedDiary
        previousDate = initialPreviousDate
        _loadedDate.value = initialLoadedDate
        diaryStateFlow.initialize()
        _weather1AdapterList.value = initialWeatherAdapterList
        _weather2AdapterList.value = initialWeatherAdapterList
        _conditionAdapterList.value = initialConditionAdapterList
        loadedPicturePath = initialLoadedPicturePath
        shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    // ViewClicked処理
    fun onDiarySaveMenuClicked() {

        val diaryEntity = diaryStateFlow.createDiaryEntity()
        val diaryItemTitleSelectionHistoryItemEntityList =
            diaryStateFlow.createDiaryItemTitleSelectionHistoryItemEntityList()
        val date = date.requireValue()
        val loadedDate = _loadedDate.value
        val loadedPicturePath = loadedPicturePath

        viewModelScope.launch {
            requestDiaryUpdateConfirmation(
                date,
                diaryEntity,
                diaryItemTitleSelectionHistoryItemEntityList,
                loadedDate,
                loadedPicturePath
            ) {
                saveDiary(
                    diaryEntity,
                    diaryItemTitleSelectionHistoryItemEntityList,
                    loadedDate,
                    loadedPicturePath
                )
            }
        }
    }

    fun onDiaryDeleteMenuClicked() {
        val parameters =
            DiaryDeleteParameters(
                loadedDate.requireValue(),
                loadedPicturePath
            )
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryDeleteDialog(parameters)
            )
        }

    }

    fun onNavigationClicked() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    fun onDateInputFieldClicked() {
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
        val itemTitle = getItemTitle(itemNumber).requireValue()
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryItemTitleEditFragment(itemNumber, itemTitle)
            )
        }
    }

    fun onItemAdditionButtonClicked() {
        viewModelScope.launch {
            addDiaryItem()
        }
    }

    fun onItemDeleteButtonClicked(itemNumber: ItemNumber) {
        viewModelScope.launch {
            val parameters = DiaryItemDeleteParameters(itemNumber)
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(parameters)
            )
        }
    }

    fun onAttachedPictureDeleteButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryPictureDeleteDialog
            )
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
            processWeatherInfoAcquisition(date, previousDate)
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
        val diaryEntity = parameters.diaryEntity
        val diaryItemTitleSelectionHistoryItemEntityList =
            parameters.diaryItemTitleSelectionHistoryItemEntityList
        val loadedDate = parameters.loadedDate
        val loadedPicturePath = parameters.loadedPicturePath
        viewModelScope.launch {
            saveDiary(
                diaryEntity,
                diaryItemTitleSelectionHistoryItemEntityList,
                loadedDate,
                loadedPicturePath
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
        val loadedDate = _loadedDate.value
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

    fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<WeatherInfoAcquisitionParameters>) {
        when (result) {
            is DialogResult.Positive<WeatherInfoAcquisitionParameters> -> {
                onWeatherInfoFetchDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                updateViewModelIdleState()
            }
        }
    }

    private fun onWeatherInfoFetchDialogPositiveResultReceived(
        parameters: WeatherInfoAcquisitionParameters
    ) {
        viewModelScope.launch {
            checkPermissionBeforeWeatherInfoAcquisition(parameters)
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
    fun onPicturePathReceivedFromOpenDocument(uri: Uri?) {
        updatePicturePath(uri)
    }

    // Fragment状態処理
    fun onDiaryDataSetUp(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {

        val loadedDate = _loadedDate.value

        viewModelScope.launch {
            prepareDiary(date, loadedDate, shouldLoadDiary)
        }
    }

    fun onAttachedPictureClicked() {
        updateViewModelState(DiaryEditState.PictureSelecting)
    }

    // StateFlow値変更時処理
    fun onWeather1Changed() {
        updateWeather2AdapterList()
    }

    // MotionLayout変更時処理
    fun onDiaryItemHidedStateTransitionCompleted(itemNumber: ItemNumber) {
        deleteItem(itemNumber)
    }

    fun onDiaryItemShowedStateTransitionCompleted() {
        updateViewModelIdleState()
    }

    // 権限確認後処理
    fun onAccessLocationPermissionChecked(
        isGranted: Boolean,
        parameters: WeatherInfoAcquisitionParameters
    ) {

        val date = parameters.date

        viewModelScope.launch {
            loadWeatherInfo(isGranted, date)
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
            processWeatherInfoAcquisition(date, previousDate)
        }
    }

    private suspend fun loadDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateViewModelState(DiaryEditState.Loading)
        when (val result = loadDiaryUseCase(date)) {
            is UseCaseResult.Success -> {
                val diaryEntity = result.value

                // HACK:下記はDiaryStateFlow#update()処理よりも前に処理すること。
                //      (後で処理するとDiaryStateFlowのDateのObserverがloadedDateの更新よりも先に処理される為)
                updateLoadedDate(date)

                diaryStateFlow.update(diaryEntity)
                loadedPicturePath = diaryStateFlow.picturePath.value
            }
            is UseCaseResult.Error -> {
                Log.e(logTag, "${logMsg}_失敗", result.error)
                if (hasPreparedDiary) {
                    emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadingFailure)
                } else {
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingFailureDialog(date)
                    )
                }
            }
        }
        updateViewModelIdleState()
    }

    private suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>,
        loadedDate: LocalDate?,
        loadedPicturePath: Uri?
    ) {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        updateViewModelState(DiaryEditState.Saving)
        val result =
            saveDiaryUseCase(
                diaryEntity,
                diaryItemTitleSelectionHistoryItemEntityList,
                loadedDate,
                loadedPicturePath
            )
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                emitViewModelEvent(
                    DiaryEditEvent
                        .NavigateDiaryShowFragment(
                            LocalDate.parse(diaryEntity.date)
                        )
                )
            }
            is UseCaseResult.Error -> {
                Log.e(logTag, "${logMsg}失敗")
                emitAppMessageEvent(DiaryEditAppMessage.DiarySavingFailure)
            }
        }
        updateViewModelIdleState()
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
                emitViewModelEvent(
                    DiaryEditEvent
                        .NavigatePreviousFragmentOnDiaryDelete(
                            FragmentResult.Some(loadedDate)
                        )
                )
            }
            is UseCaseResult.Error -> {
                when (result.error) {
                    is DeleteDiaryError.DeleteDiary -> {
                        Log.e(logTag, "${logMsg}失敗")
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
                    }
                    is DeleteDiaryError.ReleaseUriPermission -> {
                        Log.i(logTag, "${logMsg}完了(Uri開放失敗)")
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
        updateViewModelIdleState()
    }

    private suspend fun requestDiaryLoadingConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        loadedDate: LocalDate?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.Loading)
        val result = shouldRequestDiaryLoadingConfirmationUseCase(date, previousDate, loadedDate)
        when (result) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    val parameters = DiaryLoadingParameters(date)
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingDialog(
                            parameters
                        )
                    )
                    updateViewModelIdleState()
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Error -> {
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
                updateViewModelIdleState()
            }
        }
    }

    private suspend fun requestDiaryUpdateConfirmation(
        date: LocalDate,
        diaryEntity: DiaryEntity,
        diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>,
        loadedDate: LocalDate?,
        loadedPicturePath: Uri?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.Saving)
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, loadedDate)) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    val parameters = DiaryUpdateParameters(
                        diaryEntity,
                        diaryItemTitleSelectionHistoryItemEntityList,
                        loadedDate,
                        loadedPicturePath
                    )
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryUpdateDialog(parameters)
                    )
                    updateViewModelIdleState()
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Error -> {
                emitAppMessageEvent(
                    DiaryEditAppMessage.DiarySavingFailure
                )
                updateViewModelIdleState()
            }
        }
    }

    // 天気情報取得関係
    private suspend fun processWeatherInfoAcquisition(date: LocalDate, previousDate: LocalDate?) {
        checkWeatherInfoAcquisitionEnabled { isEnabled ->
            if (!isEnabled) {
                updateViewModelIdleState()
                return@checkWeatherInfoAcquisitionEnabled
            }

            requestWeatherInfoConfirmation(
                date,
                previousDate
            ) {

                checkShouldLoadWeatherInfo(
                    date,
                    previousDate
                ) { shouldLoad ->
                    if (!shouldLoad) {
                        updateViewModelIdleState()
                        return@checkShouldLoadWeatherInfo
                    }

                    val parameters = WeatherInfoAcquisitionParameters(date)
                    checkPermissionBeforeWeatherInfoAcquisition(parameters)
                }
            }
        }
    }

    private suspend fun checkWeatherInfoAcquisitionEnabled(
        onResult: suspend (Boolean) -> Unit
    ) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        when (val result = isWeatherInfoAcquisitionEnabledUseCase()) {
            is UseCaseResult.Success -> {
                onResult(result.value)
            }
            is UseCaseResult.Error -> {
                emitAppMessageEvent(DiaryEditAppMessage.SettingLoadingFailure)
                updateViewModelIdleState()
            }
        }
    }

    private suspend fun requestWeatherInfoConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        val result =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate)
        when (result) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    val parameters = WeatherInfoAcquisitionParameters(date)
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateWeatherInfoFetchingDialog(parameters)
                    )
                    updateViewModelIdleState()
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Error -> {
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
                updateViewModelIdleState()
            }
        }
    }

    private suspend fun checkShouldLoadWeatherInfo(
        date: LocalDate,
        previousDate: LocalDate?,
        onResult: suspend (Boolean) -> Unit
    ) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        when (val result = shouldLoadWeatherInfoUseCase(date, previousDate)) {
            is UseCaseResult.Success -> {
                onResult(result.value)
            }
            is UseCaseResult.Error -> {
                // Errorにならないため処理不要
            }
        }

    }

    private suspend fun checkPermissionBeforeWeatherInfoAcquisition(
        parameters: WeatherInfoAcquisitionParameters
    ) {
        emitViewModelEvent(
            DiaryEditEvent.CheckAccessLocationPermissionBeforeWeatherInfoAcquisition(parameters)
        )
    }

    private suspend fun loadWeatherInfo(
        isGranted: Boolean,
        date: LocalDate
    ) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        when (val result = loadWeatherInfoUseCase(isGranted, date)) {
            is UseCaseResult.Success -> {
                updateWeather1(result.value)
                updateWeather2(Weather.UNKNOWN)
            }
            is UseCaseResult.Error -> {
                when (result.error) {
                    is FetchWeatherInfoError.LocationPermissionNotGranted -> {
                        emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
                    }
                    is FetchWeatherInfoError.AccessLocation -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoLoadingFailure)
                    }
                    is FetchWeatherInfoError.WeatherInfoDateOutOfRange -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is FetchWeatherInfoError.LoadWeatherInfo -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoLoadingFailure)
                    }
                }
            }
        }
        updateViewModelIdleState()
    }

    // 日付関係
    private fun updateDate(date: LocalDate) {
        // HACK:下記はDiaryStateFlowのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        this@DiaryEditViewModel.previousDate = diaryStateFlow.date.value
        diaryStateFlow.date.value = date
    }

    private fun updateLoadedDate(date: LocalDate) {
        _loadedDate.value = date
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
        updateViewModelState(DiaryEditState.ItemDeleting)
        diaryStateFlow.deleteItem(itemNumber)
        updateViewModelIdleState()
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
    private fun updatePicturePath(uri: Uri?) {
        diaryStateFlow.picturePath.value = uri
        updateViewModelIdleState()
    }

    private fun deletePicturePath() {
        updateViewModelState(DiaryEditState.PictureDeleting)
        diaryStateFlow.picturePath.value = null
        updateViewModelIdleState()
    }


    private suspend fun navigatePreviousFragment() {
        val loadedDate = loadedDate.value
        val result =
            if (loadedDate == null) {
                FragmentResult.None
            } else {
                FragmentResult.Some(loadedDate)
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
                        is UseCaseResult.Error -> {
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


                    val diaryEntity = diaryStateFlow.createDiaryEntity()
                    val diaryItemTitleSelectionHistoryItemEntityList =
                        diaryStateFlow.createDiaryItemTitleSelectionHistoryItemEntityList()
                    val result =
                        saveDiaryUseCase(
                            diaryEntity,
                            diaryItemTitleSelectionHistoryItemEntityList,
                            _loadedDate.value,
                            loadedPicturePath
                        )
                    when (result) {
                        is UseCaseResult.Success -> {
                            // 処理なし
                        }
                        is UseCaseResult.Error -> {
                            isTesting = false
                            return@launch
                        }
                    }
                }
            }
            navigatePreviousFragment()
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
