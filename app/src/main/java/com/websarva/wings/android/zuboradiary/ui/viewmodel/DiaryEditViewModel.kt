package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.action.Action
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.action.DiaryEditFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.state.DiaryEditState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal class DiaryEditViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val weatherApiRepository: WeatherApiRepository,
    private val locationRepository: LocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val uriRepository: UriRepository
) : BaseViewModel() {

    companion object {
        private const val SAVED_HAS_PREPARED_DIARY_STATE_KEY = "hasPreparedDiary"
        private const val SAVED_PREVIOUS_DATE_STATE_KEY = "previousDate"
        private const val SAVED_LOADED_DATE_STATE_KEY = "loadedDate"
        private const val SAVED_LOADED_PICTURE_PATH_STATE_KEY = "loadedPicturePath"
        private const val SAVED_SHOULD_INITIALIZE_ON_FRAGMENT_DESTROY_STATE_KEY =
            "shouldInitializeOnFragmentDestroy"
    }

    private val logTag = createLogTag()

    private val initialDiaryEditState = DiaryEditState.Idle
    private val _diaryEditState = MutableStateFlow<DiaryEditState>(initialDiaryEditState)

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
        combine(_diaryEditState, numVisibleItems) { state, numVisibleItems ->
            return@combine when (state) {
                DiaryEditState.Idle -> {
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
        combine(_diaryEditState, picturePath) { state, picturePath ->
            return@combine when (state) {
                DiaryEditState.Idle -> {
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
        _diaryEditState.map { state ->
            return@map when (state) {
                DiaryEditState.Idle,
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

    // Fragment処理
    private val _fragmentAction= MutableSharedFlow<Action<FragmentAction>>(replay = 1)
    val fragmentAction
        get() = _fragmentAction.asSharedFlow()

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

    fun onBackPressed() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    // ViewClicked処理
    fun onDiarySaveMenuClicked() {
        _diaryEditState.value = DiaryEditState.Saving
        viewModelScope.launch {
            saveDiary()
            _diaryEditState.value = DiaryEditState.Idle
        }
    }

    fun onDiaryDeleteMenuClicked() {
        _diaryEditState.value = DiaryEditState.Deleting
        val date = this.date.requireValue()
        viewModelScope.launch {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryDeleteDialog(date)
            )
            _diaryEditState.value = DiaryEditState.Idle
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
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDatePickerDialog(date)
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
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryItemTitleEditFragment(itemNumber, itemTitle)
            )
        }
    }

    fun onItemAdditionButtonClicked() {
        _diaryEditState.value = DiaryEditState.ItemAdding
        incrementVisibleItemsCount()
    }

    fun onItemDeleteButtonClicked(itemNumber: ItemNumber) {
        viewModelScope.launch {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryItemDeleteDialog(itemNumber)
            )
        }
    }

    fun onAttachedPictureDeleteButtonClicked() {
        viewModelScope.launch {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryPictureDeleteDialog
            )
        }
    }

    // DialogButtonClicked処理
    fun onDiaryLoadingDialogPositiveButtonClicked() {
        val date = this.date.requireValue()
        viewModelScope.launch {
            _diaryEditState.value = DiaryEditState.Loading
            prepareDiary(
                date,
                true
            )
        }
    }

    fun onDiaryLoadingDialogNegativeButtonClicked() {
        viewModelScope.launch {
            _diaryEditState.value = DiaryEditState.WeatherFetching
            val date = date.requireValue()
            if (!shouldLoadWeatherInfo(date)) return@launch

            loadWeatherInfo(date)
        }
    }

    fun onDiaryUpdateDialogPositiveButtonClicked() {
        _diaryEditState.value = DiaryEditState.Saving
        viewModelScope.launch {
            saveDiary(true)
            _diaryEditState.value = DiaryEditState.Idle
        }
    }

    fun onDiaryDeleteDialogPositiveButtonClicked() {
        _diaryEditState.value = DiaryEditState.Deleting
        viewModelScope.launch {
            deleteDiary()
            _diaryEditState.value = DiaryEditState.Idle
        }
    }

    fun onDatePickerDialogPositiveButtonClicked(date: LocalDate) {
        _diaryEditState.value = DiaryEditState.WeatherFetching
        viewModelScope.launch {
            prepareDiaryDate(date)
        }
    }

    fun onDiaryLoadingFailureDialogPositiveButtonClicked() {
        viewModelScope.launch {
            navigatePreviousFragment()
        }
    }

    fun onWeatherInfoFetchDialogPositiveButtonClicked() {
        _diaryEditState.value = DiaryEditState.WeatherFetching
        viewModelScope.launch {
            val date = diaryStateFlow.date.requireValue()
            if (!shouldLoadWeatherInfo(date)) return@launch

            loadWeatherInfo(date, true)
        }
    }

    fun onDiaryItemDeleteDialogPositiveButtonClicked(itemNumber: ItemNumber) {
        _diaryEditState.value = DiaryEditState.ItemDeleting
        val numVisibleItems = numVisibleItems.requireValue()

        if (itemNumber.value == 1 && numVisibleItems == itemNumber.value) {
            deleteItem(itemNumber)
            _diaryEditState.value = DiaryEditState.Idle
        } else {
            viewModelScope.launch {
                updateFragmentAction(
                    DiaryEditFragmentAction.TransitionDiaryItemHidedState(itemNumber)
                )
            }
        }
    }

    // Fragment状態処理
    fun onDiaryDataSetUp(
        date: LocalDate,
        shouldLoadDiary: Boolean
    ) {
        _diaryEditState.value = DiaryEditState.Loading
        viewModelScope.launch {
            prepareDiary(date, shouldLoadDiary)
        }
    }

    fun onDiaryPictureDeleteDialogPositiveButtonClicked() {
        _diaryEditState.value = DiaryEditState.PictureDeleting
        deletePicturePath()
        _diaryEditState.value = DiaryEditState.Idle
    }

    fun onAttachedPictureClicked() {
        _diaryEditState.value = DiaryEditState.PictureSelecting
    }

    // 他Fragmentからの受取処理
    fun onDataReceivedFromItemTitleEditFragment(itemNumber: ItemNumber, itemTitle: String) {
        updateItemTitle(itemNumber, itemTitle)
    }

    fun onPicturePathReceivedFromOpenDocument(uri: Uri?) {
        // MEMO:未選択時null
        if (uri != null) diaryStateFlow.picturePath.value = uri

        _diaryEditState.value = DiaryEditState.Idle
    }

    // StateFlow値変更時処理
    fun onWeather1Changed() {
        updateWeather2AdapterList()
    }

    // MotionLayout変更時処理
    fun onDiaryItemHidedStateTransitionCompleted(itemNumber: ItemNumber) {
        deleteItem(itemNumber)
        _diaryEditState.value = DiaryEditState.Idle
    }

    fun onDiaryItemShowedStateTransitionCompleted() {
        _diaryEditState.value = DiaryEditState.Idle
    }

    // 権限確認後処理
    fun onAccessLocationPermissionChecked(isGranted: Boolean, date: LocalDate) {
        viewModelScope.launch {
            if (isGranted) {
                val geoCoordinates = fetchCurrentLocation()
                if (geoCoordinates == null) {
                    addAppMessage(DiaryEditAppMessage.WeatherInfoLoadingFailure)
                    _diaryEditState.value = DiaryEditState.Idle
                    return@launch
                }
                fetchWeatherInfo(date, geoCoordinates)
            } else {
                addAppMessage(DiaryEditAppMessage.AccessLocationPermissionRequest)
            }
            _diaryEditState.value = DiaryEditState.Idle
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
                    addAppMessage(DiaryEditAppMessage.DiaryLoadingFailure)
                } else {
                    updateFragmentAction(
                        DiaryEditFragmentAction.NavigateDiaryLoadingFailureDialog(date)
                    )
                }
                return
            } finally {
                _diaryEditState.value = DiaryEditState.Idle
            }
        } else {
            _diaryEditState.value = DiaryEditState.WeatherFetching
            prepareDiaryDate(date)
        }
        hasPreparedDiary = true

        Log.i(logTag, "${logMsg}_完了")
    }
    
    // TODO:onDateChangedメソッドに変更して、Fragmentの監視から呼び出す？
    private suspend fun prepareDiaryDate(date: LocalDate) {
        updateDate(date)
        
        if (shouldShowDiaryLoadingDialog(date)) {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryLoadingDialog(date)
            )
            _diaryEditState.value = DiaryEditState.Idle
            return
        }

        if (!shouldLoadWeatherInfo(date)) {
            _diaryEditState.value = DiaryEditState.Idle
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
            addAppMessage(DiaryEditAppMessage.WeatherInfoLoadingFailure)
            return null
        }
    }

    private suspend fun saveDiary(shouldIgnoreConfirmationDialog: Boolean = false) {
        if (!shouldIgnoreConfirmationDialog) {
            val shouldShowDialog =
                shouldShowUpdateConfirmationDialog() ?: return
            if (shouldShowDialog) {
                updateFragmentAction(
                    DiaryEditFragmentAction.NavigateDiaryUpdateDialog(date.requireValue())
                )
                return
            }
        }

        val isSuccessful = saveDiaryToDatabase()
        if (!isSuccessful) return

        updatePictureUriPermissionOnDiarySaved()
        updateFragmentAction(
            DiaryEditFragmentAction
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
            uriRepository.releasePersistablePermission(loadedPictureUri)
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
            addAppMessage(DiaryEditAppMessage.DiarySavingFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
    }

    private suspend fun deleteDiary() {
        val loadedDate = loadedDate.value
        val loadedPictureUri = loadedPicturePath

        val isSuccessful = deleteDiaryFromDatabase()
        if (!isSuccessful) {
            return
        }

        if (loadedPictureUri != null) uriRepository.releasePersistablePermission(loadedPictureUri)
        updateFragmentAction(
            DiaryEditFragmentAction
                .NavigatePreviousFragmentOnDiaryDelete(loadedDate)
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
            addAppMessage(DiaryEditAppMessage.DiaryDeleteFailure)
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
        if (!isWeatherInfoAcquisitionPreferenceChecked()) return false
        if (!isNewDiary && previousDate == null) return false
        return previousDate != date
    }

    private suspend fun isWeatherInfoAcquisitionPreferenceChecked(): Boolean {
        return withContext(Dispatchers.IO) {
            userPreferencesRepository
                .loadAllPreferences()
                .map { value: AllPreferences ->
                    value.weatherInfoAcquisitionPreference.isChecked
                }.first()
        }
    }

    private suspend fun loadWeatherInfo(
        date: LocalDate,
        shouldIgnoreConfirmationDialog: Boolean = false
    ) {
        if (!weatherApiRepository.canFetchWeatherInfo(date)) {
            _diaryEditState.value = DiaryEditState.Idle
            return
        }
        if (!shouldIgnoreConfirmationDialog && shouldShowWeatherInfoFetchingDialog()) {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateWeatherInfoFetchingDialog(date)
            )
            _diaryEditState.value = DiaryEditState.Idle
            return
        }

        updateFragmentAction(DiaryEditFragmentAction.CheckAccessLocationPermission(date))
    }

    private fun shouldShowWeatherInfoFetchingDialog(): Boolean {
        return previousDate != null
    }

    private suspend fun fetchWeatherInfo(date: LocalDate, geoCoordinates: GeoCoordinates?) {
        if (geoCoordinates == null) {
            addAppMessage(DiaryEditAppMessage.WeatherInfoLoadingFailure)
            return
        }

        if (!weatherApiRepository.canFetchWeatherInfo(date)) return

        val logMsg = "天気情報取得"
        Log.i(logTag, "${logMsg}_開始")

        val currentDate = LocalDate.now()
        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)

        val response =
            if (betweenDays == 0L) {
                weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates)
            } else {
                weatherApiRepository.fetchPastDayWeatherInfo(
                    geoCoordinates,
                    betweenDays.toInt()
                )
            }
        Log.d(logTag, "fetchWeatherInformation()_code = " + response.code())
        Log.d(logTag, "fetchWeatherInformation()_message = :" + response.message())

        if (response.isSuccessful) {
            Log.d(logTag, "fetchWeatherInformation()_body = " + response.body())
            val result =
                response.body()?.toWeatherInfo() ?: throw IllegalStateException()
            updateWeather1(result)
            updateWeather2(Weather.UNKNOWN)
            Log.i(logTag, "${logMsg}_完了")
        } else {
            response.errorBody().use { errorBody ->
                val errorBodyString = errorBody?.string() ?: "null"
                Log.d(
                    logTag,
                    "fetchWeatherInformation()_errorBody = $errorBodyString"
                )
            }
            addAppMessage(DiaryEditAppMessage.WeatherInfoLoadingFailure)
            Log.e(logTag, "${logMsg}_失敗")
        }
    }

    private suspend fun fetchCurrentLocation(): GeoCoordinates? {
        return locationRepository.fetchLocation()
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

    // 表示保留中Dialog追加
    // MEMO:引数の型をサブクラスに制限
    fun addPendingDialogList(pendingDialog: DiaryEditPendingDialog) {
        super.addPendingDialogList(pendingDialog)
    }
    
    // FragmentAction関係
    private suspend fun updateFragmentAction(action: FragmentAction) {
        _fragmentAction.emit(
            Action(action)
        )
    }

    private suspend fun navigatePreviousFragment() {
        val loadedDate = loadedDate.value
        updateFragmentAction(DiaryEditFragmentAction.NavigatePreviousFragment(loadedDate))
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
