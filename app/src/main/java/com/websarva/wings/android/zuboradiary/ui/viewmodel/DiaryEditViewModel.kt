package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.exception.LocationAccessFailedException
import com.websarva.wings.android.zuboradiary.data.exception.LocationPermissionException
import com.websarva.wings.android.zuboradiary.data.exception.WeatherInfoDateOutOfRangeException
import com.websarva.wings.android.zuboradiary.data.exception.WeatherInfoFetchFailedException
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.uri.ReleaseUriPermissionUseCase
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
    private val diaryRepository: DiaryRepository,
    private val uriRepository: UriRepository,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
    private val isWeatherInfoAcquisitionEnabledUseCase: IsWeatherInfoAcquisitionEnabledUseCase,
    private val canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase
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

    private val isNewDiaryDefaultStatus
        get() = hasPreparedDiary && previousDate == null && _loadedDate.value == null

    private val isNewDiary
        get() = _loadedDate.value == null

    private val shouldDeleteLoadedDateDiary: Boolean
        get() {
            if (isNewDiary) return false
            return !isLoadedDateEqualToInputDate
        }

    private val isLoadedDateEqualToInputDate: Boolean
        get() {
            val loadedDate = _loadedDate.value ?: return false
            val inputDate = diaryStateFlow.date.value
            return loadedDate == inputDate
        }

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
        updateViewModelState(DiaryEditState.Saving)
        viewModelScope.launch {
            saveDiary()
            updateViewModelIdleState()
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

    // TODO:クリック時2回処理される。OneTouchClickTextInputEditTextが原因と思われるので修正する。
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
            updateViewModelState(DiaryEditState.WeatherFetching)
            val date = date.requireValue()
            if (!shouldLoadWeatherInfo(date)) {
                updateViewModelIdleState()
                return@launch
            }

            loadWeatherInfo(date)
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
            saveDiary(true)
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
            deleteDiary()
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
                // 処理なし
            }
        }
    }

    private fun onWeatherInfoFetchDialogPositiveResultReceived() {
        updateViewModelState(DiaryEditState.WeatherFetching)
        viewModelScope.launch {
            val date = diaryStateFlow.date.requireValue()
            if (!shouldLoadWeatherInfo(date)) return@launch

            loadWeatherInfo(date, true)
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

    fun onPicturePathReceivedFromOpenDocument(uri: Uri?) {
        // MEMO:未選択時null
        if (uri != null) diaryStateFlow.picturePath.value = uri

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
    fun onAccessLocationPermissionChecked(isGranted: Boolean, date: LocalDate) {
        viewModelScope.launch {
            when (val result = fetchWeatherInfoUseCase(isGranted, date)) {
                is UseCaseResult.Success -> {
                    updateWeather1(result.value)
                    updateWeather2(Weather.UNKNOWN)
                }
                is UseCaseResult.Error -> {
                    when (result.exception) {
                        is LocationPermissionException -> {
                            emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
                        }
                        is LocationAccessFailedException -> {
                            emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoLoadingFailure)
                        }
                        is WeatherInfoDateOutOfRangeException -> {
                            emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                        }
                        is WeatherInfoFetchFailedException -> {
                            emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoLoadingFailure)
                        }
                    }
                }
            }
            updateViewModelIdleState()
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
            try {
                val isSuccessful = loadSavedDiary(date)
                if (!isSuccessful) throw Exception()
            } catch (e: Exception) {
                Log.e(logTag, "${logMsg}_失敗", e)
                if (hasPreparedDiary) {
                    emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadingFailure)
                } else {
                    emitViewModelEvent(
                        DiaryEditEvent.NavigateDiaryLoadingFailureDialog(date)
                    )
                }
                return
            } finally {
                updateViewModelIdleState()
            }
        } else {
            updateViewModelState(DiaryEditState.WeatherFetching)
            prepareDiaryDate(date)
        }
        hasPreparedDiary = true

        Log.i(logTag, "${logMsg}_完了")
    }
    
    // TODO:onDateChangedメソッドに変更して、Fragmentの監視から呼び出す？
    private suspend fun prepareDiaryDate(date: LocalDate) {
        updateDate(date)
        
        if (shouldShowDiaryLoadingDialog(date)) {
            emitViewModelEvent(
                DiaryEditEvent.NavigateDiaryLoadingDialog(date)
            )
            updateViewModelIdleState()
            return
        }

        if (!shouldLoadWeatherInfo(date)) {
            updateViewModelIdleState()
            return
        }

        loadWeatherInfo(date)
    }

    private suspend fun shouldShowDiaryLoadingDialog(changedDate: LocalDate): Boolean {
        if (isNewDiaryDefaultStatus) {
            return existsSavedDiary(changedDate) ?: false
        }

        val previousDate = previousDate
        val loadedDate = loadedDate.value

        if (changedDate == previousDate) return false
        if (changedDate == loadedDate) return false
        return existsSavedDiary(changedDate) ?: false
    }

    @Throws(Exception::class)
    private suspend fun loadSavedDiary(date: LocalDate): Boolean {
        val diaryEntity = diaryRepository.loadDiary(date) ?: return false

        // HACK:下記はDiaryStateFlow#update()処理よりも前に処理すること。
        //      (後で処理するとDiaryStateFlowのDateのObserverがloadedDateの更新よりも先に処理される為)
        updateLoadedDate(date)

        diaryStateFlow.update(diaryEntity)
        loadedPicturePath = diaryStateFlow.picturePath.value
        return true
    }

    private suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            Log.e(logTag, "日記既存確認_失敗", e)
            emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoLoadingFailure)
            return null
        }
    }

    private suspend fun saveDiary(shouldIgnoreConfirmationDialog: Boolean = false) {
        if (!shouldIgnoreConfirmationDialog) {
            val shouldShowDialog =
                shouldShowUpdateConfirmationDialog() ?: return
            if (shouldShowDialog) {
                emitViewModelEvent(
                    DiaryEditEvent.NavigateDiaryUpdateDialog(date.requireValue())
                )
                return
            }
        }

        val isSuccessful = saveDiaryToDatabase()
        if (!isSuccessful) return

        updatePictureUriPermissionOnDiarySaved()
        emitViewModelEvent(
            DiaryEditEvent
                .NavigateDiaryShowFragment(
                    date.requireValue()
                )
        )
    }

    private suspend fun shouldShowUpdateConfirmationDialog(): Boolean? {
        if (isLoadedDateEqualToInputDate) return false
        val inputDate = diaryStateFlow.date.requireValue()
        return existsSavedDiary(inputDate)
    }

    private suspend fun updatePictureUriPermissionOnDiarySaved() {
        val latestPictureUri = picturePath.value
        val loadedPictureUri = loadedPicturePath

        if (latestPictureUri == loadedPictureUri) return

        if (loadedPictureUri != null) {
            releaseUriPermissionUseCase(loadedPictureUri)
        }
        if (latestPictureUri != null) {
            return uriRepository.takePersistablePermission(latestPictureUri)
        }
    }

    private suspend fun saveDiaryToDatabase(): Boolean {
        val logMsg = "日記保存"
        Log.i(logTag, "${logMsg}_開始")

        val diaryEntity = diaryStateFlow.createDiaryEntity()
        val diaryItemTitleSelectionHistoryItemEntityList =
            diaryStateFlow.createDiaryItemTitleSelectionHistoryItemEntityList()
        try {
            if (shouldDeleteLoadedDateDiary) {
                diaryRepository
                    .deleteAndSaveDiary(
                        _loadedDate.requireValue(),
                        diaryEntity,
                        diaryItemTitleSelectionHistoryItemEntityList
                    )
            } else {
                diaryRepository
                    .saveDiary(diaryEntity, diaryItemTitleSelectionHistoryItemEntityList)
            }
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            emitAppMessageEvent(DiaryEditAppMessage.DiarySavingFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
    }

    private suspend fun deleteDiary() {
        val loadedDate = loadedDate.requireValue()
        val loadedPictureUri = loadedPicturePath

        val isSuccessful = deleteDiaryFromDatabase()
        if (!isSuccessful) {
            return
        }

        releaseUriPermissionUseCase(loadedPictureUri)
        emitViewModelEvent(
            DiaryEditEvent
                .NavigatePreviousFragmentOnDiaryDelete(
                    FragmentResult.Some(loadedDate)
                )
        )
    }

    private suspend fun deleteDiaryFromDatabase(): Boolean {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")
        val deleteDate = _loadedDate.requireValue()

        try {
            diaryRepository.deleteDiary(deleteDate)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
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
    private suspend fun shouldLoadWeatherInfo(date: LocalDate): Boolean {
        when (val result = isWeatherInfoAcquisitionEnabledUseCase()) {
            is UseCaseResult.Success -> if (!result.value) return false
            is UseCaseResult.Error -> return false
        }
        if (!isNewDiary && previousDate == null) return false
        return previousDate != date
    }

    private suspend fun loadWeatherInfo(
        date: LocalDate,
        shouldIgnoreConfirmationDialog: Boolean = false
    ) {
        when (val result = canFetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                if (!result.value) {
                    updateViewModelIdleState()
                    return
                }
            }
            is UseCaseResult.Error -> {
                updateViewModelIdleState()
                return
            }
        }

        if (!shouldIgnoreConfirmationDialog && shouldShowWeatherInfoFetchingDialog()) {
            emitViewModelEvent(
                DiaryEditEvent.NavigateWeatherInfoFetchingDialog(date)
            )
            updateViewModelIdleState()
            return
        }

        emitViewModelEvent(DiaryEditEvent.CheckAccessLocationPermission(date))
    }

    private fun shouldShowWeatherInfoFetchingDialog(): Boolean {
        return previousDate != null
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
                    val isPass = existsSavedDiary(savingDate)
                    if (isPass == null) {
                        isTesting = false
                        return@launch
                    }
                    if (isPass) {
                        continue
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
                    val isSuccessful = saveDiaryToDatabase()
                    if (!isSuccessful) {
                        isTesting = false
                        return@launch
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
