package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
    var hasPreparedDiary = handle[SAVED_HAS_PREPARED_DIARY_STATE_KEY] ?: initialHasPreparedDiary
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
        viewModelScope.launch {
            requestDiaryUpdateConfirmation {
                processDiarySave()
            }
        }
    }

    fun onDiaryDeleteMenuClicked() {
        updateViewModelState(DiaryEditState.Deleting)
        val date = this.date.requireValue()
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryDeleteDialog(date)
            )
            updateViewModelIdleState()
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
        updateViewModelState(DiaryEditState.ItemAdding)
        viewModelScope.launch {
            emitViewModelEvent(DiaryEditEvent.ItemAddition)
            incrementVisibleItemsCount()
        }
    }

    fun onItemDeleteButtonClicked(itemNumber: ItemNumber) {
        viewModelScope.launch {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryItemDeleteDialog(itemNumber)
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
    fun onDiaryLoadingDialogResultReceived(result: DialogResult<String>) {
        when (result) {
            is DialogResult.Positive<String> -> {
                onDiaryLoadingDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                onDiaryLoadingDialogNegativeResultReceived()
            }
        }
    }

    private fun onDiaryLoadingDialogPositiveResultReceived() {
        val date = this.date.requireValue()
        viewModelScope.launch {
            updateViewModelState(DiaryEditState.Loading)
            prepareDiary(
                date,
                true
            )
        }
    }

    private fun onDiaryLoadingDialogNegativeResultReceived() {
        viewModelScope.launch {
            checkWeatherInfoAcquisitionEnabled { isEnabled ->
                if (!isEnabled) return@checkWeatherInfoAcquisitionEnabled

                requestWeatherInfoConfirmation {
                    // 処理なし
                }
            }
        }
    }

    fun onDiaryUpdateDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onDiaryUpdateDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryUpdateDialogPositiveResultReceived() {
        updateViewModelState(DiaryEditState.Saving)
        viewModelScope.launch {
            processDiarySave()
            updateViewModelIdleState()
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
        updateViewModelState(DiaryEditState.Deleting)
        viewModelScope.launch {
            processDiaryDelete()
            updateViewModelIdleState()
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
        updateViewModelState(DiaryEditState.WeatherFetching)
        viewModelScope.launch {
            prepareDiaryDate(date)
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

    fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onWeatherInfoFetchDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                updateViewModelIdleState()
            }
        }
    }

    private fun onWeatherInfoFetchDialogPositiveResultReceived() {
        updateViewModelState(DiaryEditState.WeatherFetching)
        viewModelScope.launch {
            checkPermissionBeforeWeatherInfoLoading()
        }
    }

    fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<ItemNumber>) {
        when (result) {
            is DialogResult.Positive<ItemNumber> -> {
                onDiaryItemDeleteDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onDiaryItemDeleteDialogPositiveResultReceived(itemNumber: ItemNumber) {
        updateViewModelState(DiaryEditState.ItemDeleting)
        val numVisibleItems = numVisibleItems.requireValue()

        if (itemNumber.value == 1 && numVisibleItems == itemNumber.value) {
            deleteItem(itemNumber)
            updateViewModelIdleState()
        } else {
            viewModelScope.launch {
                emitViewModelEvent(
                    DiaryEditEvent.TransitionDiaryItemHidedState(itemNumber)
                )
            }
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
        updateViewModelState(DiaryEditState.PictureDeleting)
        deletePicturePath()
        updateViewModelIdleState()
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
        updateViewModelIdleState()
    }

    // Fragment状態処理
    fun onDiaryDataSetUp(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {
        updateViewModelState(DiaryEditState.Loading)
        viewModelScope.launch {
            prepareDiary(date, shouldLoadDiary)
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
        updateViewModelIdleState()
    }

    fun onDiaryItemShowedStateTransitionCompleted() {
        updateViewModelIdleState()
    }

    // 権限確認後処理
    fun onAccessLocationPermissionChecked(isGranted: Boolean) {
        viewModelScope.launch {
            loadWeatherInfo(isGranted)
        }
    }

    // データ処理
    private suspend fun prepareDiary(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")
        if (shouldLoadDiary) {

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
        } else {
            prepareDiaryDate(date)
        }

        hasPreparedDiary = true

        Log.i(logTag, "${logMsg}_完了")
    }
    
    // TODO:onDateChangedメソッドに変更して、Fragmentの監視から呼び出す？
    private suspend fun prepareDiaryDate(date: LocalDate) {
        updateDate(date)

        updateViewModelState(DiaryEditState.Loading)
        requestDiaryLoadingConfirmation(date) {

            checkWeatherInfoAcquisitionEnabled { isEnabled ->
                if (!isEnabled) return@checkWeatherInfoAcquisitionEnabled

                requestWeatherInfoConfirmation {

                    checkShouldLoadWeatherInfo { shouldLoad ->
                        if (!shouldLoad) return@checkShouldLoadWeatherInfo

                        checkPermissionBeforeWeatherInfoLoading()
                    }
                }
            }
        }
    }

    private suspend fun processDiarySave() {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

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
                Log.i(logTag, "${logMsg}完了")
                emitViewModelEvent(
                    DiaryEditEvent
                        .NavigateDiaryShowFragment(
                            date.requireValue()
                        )
                )
            }
            is UseCaseResult.Error -> {
                Log.e(logTag, "${logMsg}失敗")
                emitAppMessageEvent(DiaryEditAppMessage.DiarySavingFailure)
            }
        }
    }

    private suspend fun processDiaryDelete() {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        val loadedDate = loadedDate.requireValue()
        val loadedPictureUri = loadedPicturePath

        when (val result = deleteDiaryUseCase(loadedDate, loadedPictureUri)) {
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
    }

    private suspend fun requestDiaryLoadingConfirmation(
        date: LocalDate,
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        val previousDate = previousDate
        val loadedDate = loadedDate.value
        val result = shouldRequestDiaryLoadingConfirmationUseCase(date, previousDate, loadedDate)
        when (result) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingDialog(date)
                    )
                } else {
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Error -> {
                updateViewModelIdleState()
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
            }
        }
    }

    private suspend fun requestDiaryUpdateConfirmation(
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.Saving)
        val date = date.requireValue()
        val loadedDate = loadedDate.value
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, loadedDate)) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryUpdateDialog(date)
                    )
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

    private suspend fun requestWeatherInfoConfirmation(
        onConfirmationNotNeeded: suspend () -> Unit
    ) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        val date = date.requireValue()
        val previousDate = previousDate
        val result =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate)
        when (result) {
            is UseCaseResult.Success -> {
                if (result.value) {
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateWeatherInfoFetchingDialog(date)
                    )
                } else {
                    updateViewModelIdleState()
                    onConfirmationNotNeeded()
                }
            }
            is UseCaseResult.Error -> {
                updateViewModelIdleState()
                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadingFailure)
            }
        }
    }

    private suspend fun checkShouldLoadWeatherInfo(
        onResult: suspend (Boolean) -> Unit
    ) {
        val date = date.requireValue()
        val previousDate = previousDate
        when (val result = shouldLoadWeatherInfoUseCase(date, previousDate)) {
            is UseCaseResult.Success -> {
                onResult(result.value)
            }
            is UseCaseResult.Error -> {
                // Errorにならないため処理不要
            }
        }
    }

    private suspend fun checkWeatherInfoAcquisitionEnabled(
        onResult: suspend (Boolean) -> Unit
    ) {
        when (val result = isWeatherInfoAcquisitionEnabledUseCase()) {
            is UseCaseResult.Success -> {
                onResult(result.value)
            }
            is UseCaseResult.Error -> {
                emitAppMessageEvent(DiaryEditAppMessage.SettingLoadingFailure)
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

    private fun updateLoadedDate(date: LocalDate) {
        _loadedDate.value = date
    }

    // 天気情報関係
    private suspend fun checkPermissionBeforeWeatherInfoLoading() {
        emitViewModelEvent(DiaryEditEvent.CheckAccessLocationPermission)
    }

    private suspend fun loadWeatherInfo(isGranted: Boolean) {
        updateViewModelState(DiaryEditState.WeatherFetching)
        val date = date.requireValue()
        val result = loadWeatherInfoUseCase(isGranted, date)
        updateViewModelIdleState()
        when (result) {
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
    private fun incrementVisibleItemsCount() {
        diaryStateFlow.incrementVisibleItemsCount()
    }

    private fun getItemTitle(itemNumber: ItemNumber): StateFlow<String> {
        return diaryStateFlow.getItemStateFlow(itemNumber).title
    }

    private fun deleteItem(itemNumber: ItemNumber) {
        diaryStateFlow.deleteItem(itemNumber)
    }

    private fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        diaryStateFlow.updateItemTitle(itemNumber, title)
    }

    // 添付写真関係
    private fun updatePicturePath(uri: Uri?) {
        diaryStateFlow.picturePath.value = uri
    }

    private fun deletePicturePath() {
        diaryStateFlow.picturePath.value = null
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
