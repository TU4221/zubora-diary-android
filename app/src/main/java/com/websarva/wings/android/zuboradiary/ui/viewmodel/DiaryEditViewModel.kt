package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.adapter.WeatherAdapterList
import com.websarva.wings.android.zuboradiary.ui.model.action.DiaryEditFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.adapter.ConditionAdapterList
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionAction
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal class DiaryEditViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val weatherApiRepository: WeatherApiRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    // 日記データ関係
    private val initialHasPreparedDiary = false
    var hasPreparedDiary = initialHasPreparedDiary
        private set

    private val initialPreviousDate: LocalDate? = null
    private var previousDate = initialPreviousDate

    private val initialLoadedDate: LocalDate? = null
    private val _loadedDate = MutableStateFlow(initialLoadedDate)
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

    private val diaryStateFlow = DiaryStateFlow()

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

    val picturePath
        get() = diaryStateFlow.picturePath.asStateFlow()

    private val initialLoadedPicturePath: Uri? = null
    private var loadedPicturePath = initialLoadedPicturePath

    // ProgressIndicator表示
    private val initialIsVisibleProgressIndicator = false
    private val _isVisibleProgressIndicator = MutableStateFlow(initialIsVisibleProgressIndicator)
    val isVisibleUpdateProgressBar
        get() = _isVisibleProgressIndicator.asStateFlow()

    // ViewModel初期化関係
    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroy = false
    var shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy

    // Fragment処理
    private val initialFragmentAction = FragmentAction.None
    private val _fragmentAction: MutableStateFlow<FragmentAction> =
        MutableStateFlow(initialFragmentAction)
    val fragmentAction: StateFlow<FragmentAction>
        get() = _fragmentAction

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

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
        _isVisibleProgressIndicator.value = initialIsVisibleProgressIndicator
        shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy
        _fragmentAction.value = initialFragmentAction
    }

    fun onBackPressed() {
        navigatePreviousFragment()
    }

    // ViewClicked処理
    fun onDiarySaveMenuClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            saveDiary()
        }
    }

    fun onDiaryDeleteMenuClicked() {
        val date = this.date.requireValue()
        updateFragmentAction(
            DiaryEditFragmentAction.NavigateDiaryDeleteDialog(date)
        )
    }

    fun onNavigationClicked() {
        navigatePreviousFragment()
    }

    fun onDateInputFieldClicked() {
        val date = this.date.requireValue()
        updateFragmentAction(
            DiaryEditFragmentAction.NavigateDatePickerDialog(date)
        )
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
        updateFragmentAction(
            DiaryEditFragmentAction.NavigateDiaryItemTitleEditFragment(itemNumber, itemTitle)
        )
    }

    fun onItemAdditionButtonClicked() {
        incrementVisibleItemsCount()
    }

    fun onItemDeleteButtonClicked(itemNumber: ItemNumber) {
        updateFragmentAction(
            DiaryEditFragmentAction.NavigateDiaryItemDeleteDialog(itemNumber)
        )
    }

    fun onAttachedPictureDeleteButtonClicked() {
        updateFragmentAction(
            DiaryEditFragmentAction.NavigateDiaryPictureDeleteDialog
        )
    }

    // DialogButtonClicked処理
    fun onDiaryLoadingDialogPositiveButtonClicked(
        requestFetchWeatherInfo: Boolean,
        geoCoordinates: GeoCoordinates?
    ) {
        val date = this.date.requireValue()
        prepareDiary(
            date,
            true,
            requestFetchWeatherInfo,
            geoCoordinates
        )
    }

    fun onDiaryLoadingDialogNegativeButtonClicked(
        requestFetchWeatherInfo: Boolean,
        geoCoordinates: GeoCoordinates?
    ) {
        if (!requestFetchWeatherInfo) return

        viewModelScope.launch(Dispatchers.IO) {
            val date = date.requireValue()
            loadWeatherInfo(date, geoCoordinates)
        }
    }

    fun onDiaryUpdateDialogPositiveButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            saveDiary(true)
        }
    }

    fun onDiaryDeleteDialogPositiveButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteDiary()
        }
    }

    fun onDatePickerDialogPositiveButtonClicked(
        date: LocalDate,
        requestsFetchWeatherInfo: Boolean = false,
        geoCoordinates: GeoCoordinates? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            prepareDiaryDate(date, requestsFetchWeatherInfo, geoCoordinates)
        }
    }

    fun onDiaryLoadingFailureDialogPositiveButtonClicked() {
        navigatePreviousFragment()
    }

    fun onWeatherInfoFetchDialogPositiveButtonClicked(geoCoordinates: GeoCoordinates?) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = diaryStateFlow.date.requireValue()
            loadWeatherInfo(date, geoCoordinates, true)
        }
    }

    fun onDiaryItemDeleteDialogPositiveButtonClicked(itemNumber: ItemNumber) {
        val numVisibleItems = numVisibleItems.requireValue()

        if (itemNumber.value == 1 && numVisibleItems == itemNumber.value) {
            deleteItem(itemNumber)
        } else {
            updateFragmentAction(DiaryEditFragmentAction.TransitionDiaryItemHidedState(itemNumber))
        }
    }

    fun onDiaryPictureDeleteDialogPositiveButtonClicked() {
        deletePicturePath()
    }

    // 他Fragmentからの受取処理
    fun onDataReceivedFromItemTitleEditFragment(itemNumber: ItemNumber, itemTitle: String) {
        updateItemTitle(itemNumber, itemTitle)
    }

    // StateFlow値変更時処理
    fun onWeather1Changed() {
        updateWeather2AdapterList()
    }

    // MotionLayout変更時処理
    fun onDiaryItemHidedStateTransitionCompleted(itemNumber: ItemNumber) {
        deleteItem(itemNumber)
    }

    // データ処理
    fun prepareDiary(
        date: LocalDate,
        shouldLoadDiary: Boolean,
        requestFetchWeatherInfo: Boolean,
        geoCoordinates: GeoCoordinates?
    ) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")
        updateProgressIndicatorVisibility(true)
        viewModelScope.launch(Dispatchers.IO) {
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
                    updateProgressIndicatorVisibility(false)
                    return@launch
                }
            } else {
                prepareDiaryDate(date, requestFetchWeatherInfo, geoCoordinates)
            }
            hasPreparedDiary = true
            updateProgressIndicatorVisibility(false)

            Log.i(logTag, "${logMsg}_完了")
        }
    }
    
    // TODO:onDateChangedメソッドに変更して、Fragmentの監視から呼び出す？
    private suspend fun prepareDiaryDate(
        date: LocalDate,
        requestsFetchWeatherInfo: Boolean = false,
        geoCoordinates: GeoCoordinates? = null
    ) {
        updateDate(date)
        
        if (shouldShowDiaryLoadingDialog(date)) {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateDiaryLoadingDialog(date)
            )
            return
        }

        if (!requestsFetchWeatherInfo) return
        loadWeatherInfo(date, geoCoordinates)
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
        updateProgressIndicatorVisibility(true)
        if (!shouldIgnoreConfirmationDialog) {
            val shouldShowDialog =
                shouldShowUpdateConfirmationDialog() ?: return
            if (shouldShowDialog) {
                updateProgressIndicatorVisibility(false)
                updateFragmentAction(
                    DiaryEditFragmentAction.NavigateDiaryUpdateDialog(date.requireValue())
                )
                return
            }
        }

        val isSuccessful = saveDiaryToDatabase()
        if (!isSuccessful) return
        val permissionAction = selectPictureUriPermissionAction()
        updateFragmentAction(
            DiaryEditFragmentAction
                .NavigateDiaryShowFragment(
                    date.requireValue(),
                    permissionAction
                )
        )
        updateProgressIndicatorVisibility(false)
    }

    private suspend fun shouldShowUpdateConfirmationDialog(): Boolean? {
        if (isLoadedDateEqualToInputDate) return false
        val inputDate = diaryStateFlow.date.requireValue()
        return existsSavedDiary(inputDate)
    }

    private fun selectPictureUriPermissionAction(): UriPermissionAction {
        val latestPictureUri = picturePath.value
        val loadedPictureUri = loadedPicturePath

        if (latestPictureUri == null && loadedPictureUri == null) {
            return UriPermissionAction.None
        }
        if (latestPictureUri != null && loadedPictureUri == null) {
            return UriPermissionAction.Take(latestPictureUri)
        }
        if (latestPictureUri == null) {
            return UriPermissionAction.Release(checkNotNull(loadedPictureUri))
        }
        if (latestPictureUri == loadedPictureUri) {
            return UriPermissionAction.None
        }
        return UriPermissionAction.ReleaseAndTake(checkNotNull(loadedPictureUri), latestPictureUri)
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
        updateProgressIndicatorVisibility(true)
        val loadedDate = loadedDate.value
        val loadedPictureUri = loadedPicturePath

        val isSuccessful = deleteDiaryFromDatabase()
        if (!isSuccessful) {
            updateProgressIndicatorVisibility(false)
            return
        }

        updateFragmentAction(
            DiaryEditFragmentAction
                .NavigatePreviousFragmentOnDiaryDelete(loadedDate, loadedPictureUri)
        )
        updateProgressIndicatorVisibility(false)
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
    private suspend fun loadWeatherInfo(
        date: LocalDate,
        geoCoordinates: GeoCoordinates?,
        shouldIgnoreConfirmationDialog: Boolean = false
    ) {
        updateProgressIndicatorVisibility(true)
        if (!shouldFetchWeatherInfo(date)) {
            updateProgressIndicatorVisibility(false)
            return
        }
        if (!shouldIgnoreConfirmationDialog && shouldShowWeatherInfoFetchingDialog()) {
            updateFragmentAction(
                DiaryEditFragmentAction.NavigateWeatherInfoFetchingDialog(date)
            )
            updateProgressIndicatorVisibility(false)
            return
        }
        fetchWeatherInfo(date, geoCoordinates)
        updateProgressIndicatorVisibility(false)
    }

    private fun shouldFetchWeatherInfo(date: LocalDate): Boolean {
        if (!weatherApiRepository.canFetchWeatherInfo(date)) return false
        if (!isNewDiary && previousDate == null) return false
        return previousDate != date
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

    fun updatePicturePath(uri: Uri) {
        diaryStateFlow.picturePath.value = uri
    }

    private fun deletePicturePath() {
        diaryStateFlow.picturePath.value = null
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    suspend fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean? {
        try {
            return !diaryRepository.existsPicturePath(uri)
        } catch (e: Exception) {
            Log.e(logTag, "端末写真URI使用状況確認_失敗", e)
            addAppMessage(DiaryEditAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }
    
    // ProgressIndicator関係
    private fun updateProgressIndicatorVisibility(isVisible: Boolean) {
        _isVisibleProgressIndicator.value = isVisible
    } 

    // 表示保留中Dialog追加
    // MEMO:引数の型をサブクラスに制限
    fun addPendingDialogList(pendingDialog: DiaryEditPendingDialog) {
        super.addPendingDialogList(pendingDialog)
    }
    
    // FragmentAction関係
    private fun updateFragmentAction(action: FragmentAction) {
        _fragmentAction.value = action
    }

    fun clearFragmentAction() {
        _fragmentAction.value = initialFragmentAction
    }

    private fun navigatePreviousFragment() {
        val loadedDate = loadedDate.value
        updateFragmentAction(DiaryEditFragmentAction.NavigatePreviousFragment(loadedDate))
    }

    // TODO:テスト用の為、最終的に削除
    fun test() {
        viewModelScope.launch(Dispatchers.IO) {
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
