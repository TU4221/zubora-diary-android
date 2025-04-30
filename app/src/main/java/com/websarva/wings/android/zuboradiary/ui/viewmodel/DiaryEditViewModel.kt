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
import com.websarva.wings.android.zuboradiary.ui.model.navigation.DiaryEditNavigationAction
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationAction
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
    private val initialPreviousDate: LocalDate? = null
    private var previousDate = initialPreviousDate

    private val initialLoadedDate: LocalDate? = null
    private val _loadedDate = MutableStateFlow(initialLoadedDate)
    val loadedDate
        get() = _loadedDate.asStateFlow()

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

    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()

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

    val picturePath
        get() = diaryStateFlow.picturePath.asStateFlow()

    private val initialLoadedPicturePath: Uri? = null
    private var loadedPicturePath = initialLoadedPicturePath

    private val initialIsVisibleProgressIndicator = false
    private val _isVisibleProgressIndicator = MutableStateFlow(initialIsVisibleProgressIndicator)
    val isVisibleUpdateProgressBar
        get() = _isVisibleProgressIndicator.asStateFlow()

    private val initialHasPreparedDiary = false
    var hasPreparedDiary = initialHasPreparedDiary
        private set

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

    val isEqualWeathers: Boolean
        get() {
            val weather1 = diaryStateFlow.weather1.value
            val weather2 = diaryStateFlow.weather2.value

            return weather1 == weather2
        }

    private val initialShouldJumpItemMotionLayout = false
    var shouldJumpItemMotionLayout = initialShouldJumpItemMotionLayout
        private set


    // MEMO:画面回転時の不要な初期化を防ぐ
    private val initialShouldInitializeOnFragmentDestroy = false
    var shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy


    // Fragment表示
    private val initialNavigationAction = NavigationAction.None
    private val _navigationAction: MutableStateFlow<NavigationAction> =
        MutableStateFlow(initialNavigationAction)
    val navigationAction: StateFlow<NavigationAction>
        get() = _navigationAction

    // UriPermission管理
    private val initialUriPermissionAction = UriPermissionAction.None
    private val _uriPermissionAction =
        MutableStateFlow<UriPermissionAction>(initialUriPermissionAction)
    val uriPermissionAction: StateFlow<UriPermissionAction>
        get() = _uriPermissionAction.asStateFlow()

    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    override fun initialize() {
        super.initialize()
        previousDate = initialPreviousDate
        _loadedDate.value = initialLoadedDate
        diaryStateFlow.initialize()
        loadedPicturePath = initialLoadedPicturePath
        _isVisibleProgressIndicator.value = initialIsVisibleProgressIndicator
        hasPreparedDiary = initialHasPreparedDiary
        shouldJumpItemMotionLayout = initialShouldJumpItemMotionLayout
        shouldInitializeOnFragmentDestroy = initialShouldInitializeOnFragmentDestroy
        _navigationAction.value = initialNavigationAction
        _uriPermissionAction.value = initialUriPermissionAction
    }

    fun prepareDiary(
        date: LocalDate,
        shouldLoadDiary: Boolean,
        requestFetchWeatherInfo: Boolean,
        geoCoordinates: GeoCoordinates?
    ) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")
        _isVisibleProgressIndicator.value = true
        viewModelScope.launch(Dispatchers.IO) {
            shouldJumpItemMotionLayout = true
            val previousNumVisibleItems = diaryStateFlow.numVisibleItems.value
            if (shouldLoadDiary) {
                try {
                    val isSuccessful = loadSavedDiary(date)
                    if (!isSuccessful) throw Exception()
                } catch (e: Exception) {
                    Log.e(logTag, "${logMsg}_失敗", e)
                    if (hasPreparedDiary) {
                        addAppMessage(DiaryEditAppMessage.DiaryLoadingFailure)
                    } else {
                        _navigationAction.value =
                            DiaryEditNavigationAction.NavigateDiaryLoadingFailureDialog(date)
                    }
                    _isVisibleProgressIndicator.value = false
                    shouldJumpItemMotionLayout = false
                    return@launch
                }
            } else {
                updateDate(date, requestFetchWeatherInfo, geoCoordinates)
            }
            hasPreparedDiary = true
            _isVisibleProgressIndicator.value = false

            // MEMO:"numVisibleItems"が読込前と同じ時はStateFlowのCollectが起動せず、MotionLayoutが処理されないので、
            //      ”shouldJumpItemMotionLayout”を下記でクリアする。
            if (previousNumVisibleItems == diaryStateFlow.numVisibleItems.value) {
                shouldJumpItemMotionLayout = false
            }

            Log.i(logTag, "${logMsg}_完了")
        }
    }

    @Throws(Exception::class)
    private suspend fun loadSavedDiary(date: LocalDate): Boolean {
        val diaryEntity = diaryRepository.loadDiary(date) ?: return false

        // HACK:下記はDiaryStateFlow#update()処理よりも前に処理すること。
        //      (後で処理するとDiaryStateFlowのDateのObserverがloadedDateの更新よりも先に処理される為)
        _loadedDate.value = date

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

    fun saveDiary(shouldIgnoreConfirmationDialog: Boolean = false) {
        _isVisibleProgressIndicator.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (!shouldIgnoreConfirmationDialog) {
                val shouldShowDialog =
                    shouldShowUpdateConfirmationDialog() ?: return@launch
                if (shouldShowDialog) {
                    _isVisibleProgressIndicator.value = false
                    _navigationAction.value =
                        DiaryEditNavigationAction.NavigateDiaryUpdateDialog(date.requireValue())
                    return@launch
                }
            }

            val isSuccessful = saveDiaryToDatabase()
            if (!isSuccessful) return@launch
            updatePictureUriPermission()
            _navigationAction.value =
                DiaryEditNavigationAction.DiaryShowFragment(date.requireValue())
            _isVisibleProgressIndicator.value = false
        }
    }

    private fun updatePictureUriPermission() {
        val latestPictureUri = picturePath.value
        val loadedPictureUri = loadedPicturePath

        if (latestPictureUri == null && loadedPictureUri == null) {
            _uriPermissionAction.value = UriPermissionAction.None
            return
        }
        if (latestPictureUri != null && loadedPictureUri == null) {
            _uriPermissionAction.value = UriPermissionAction.Take(latestPictureUri)
            return
        }
        if (latestPictureUri == null) {
            _uriPermissionAction.value = UriPermissionAction.Release(checkNotNull(loadedPictureUri))
            return
        }
        if (latestPictureUri == loadedPictureUri) {
            _uriPermissionAction.value = UriPermissionAction.None
            return
        }
        _uriPermissionAction.value =
            UriPermissionAction.ReleaseAndTake(checkNotNull(loadedPictureUri), latestPictureUri)
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

    fun deleteDiary() {
        _isVisibleProgressIndicator.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val isSuccessful = deleteDiaryFromDatabase()
            if (!isSuccessful) {
                _isVisibleProgressIndicator.value = false
                return@launch
            }

            if (loadedPicturePath != null) {
                _uriPermissionAction.value =
                    UriPermissionAction.Release(loadedPicturePath!!)
            }
            _navigationAction.value =
                DiaryEditNavigationAction.NavigatePreviousFragmentOnDiaryDelete
            _isVisibleProgressIndicator.value = false
        }
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
    fun updateDate(
        date: LocalDate,
        requestsFetchWeatherInfo: Boolean = false,
        geoCoordinates: GeoCoordinates? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // HACK:下記はDiaryStateFlowのDateのsetValue()処理よりも前に処理すること。
            //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
            this@DiaryEditViewModel.previousDate = diaryStateFlow.date.value

            diaryStateFlow.date.value = date


            if (shouldShowDiaryLoadingDialog(date)) {
                _navigationAction.value =
                    DiaryEditNavigationAction.NavigateDiaryLoadingDialog(date)
                return@launch
            }

            if (!requestsFetchWeatherInfo) return@launch
            loadWeatherInfo(date, geoCoordinates)
        }

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

    fun loadWeatherInfo(
        date: LocalDate,
        geoCoordinates: GeoCoordinates?,
        shouldIgnoreConfirmationDialog: Boolean = false
    ) {
        _isVisibleProgressIndicator.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (!shouldFetchWeatherInfo(date)) return@launch
            if (!shouldIgnoreConfirmationDialog && shouldShowWeatherInfoFetchingDialog()) {
                _navigationAction.value =
                    DiaryEditNavigationAction.NavigateWeatherInfoFetchingDialog(date)
                _isVisibleProgressIndicator.value = false
                return@launch
            }
            fetchWeatherInfo(date, geoCoordinates)
            _isVisibleProgressIndicator.value = false
        }
    }

    private fun shouldFetchWeatherInfo(date: LocalDate): Boolean {
        if (!weatherApiRepository.canFetchWeatherInfo(date)) return false
        if (!isNewDiary && previousDate == null) return false
        return previousDate != date
    }

    private fun shouldShowWeatherInfoFetchingDialog(): Boolean {
        return previousDate != null
    }

    // 天気、体調関係
    // MEMO:Weather、Conditionsから文字列に変換するにはContextが必要なため、
    //      Fragment上のLivedDateObserverにて変換した値を受け取る。
    fun updateWeather1(weather: Weather) {
        diaryStateFlow.weather1.value = weather
    }

    fun updateWeather2(weather: Weather) {
        diaryStateFlow.weather2.value = weather
    }

    fun updateCondition(condition: Condition) {
        diaryStateFlow.condition.value = condition
    }

    // 天気情報関係
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
            diaryStateFlow.weather1.value = result
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

    // 項目関係
    fun incrementVisibleItemsCount() {
        diaryStateFlow.incrementVisibleItemsCount()
    }

    fun getItemTitle(itemNumber: ItemNumber): StateFlow<String> {
        return diaryStateFlow.getItemStateFlow(itemNumber).title
    }

    fun deleteItem(itemNumber: ItemNumber) {
        diaryStateFlow.deleteItem(itemNumber)
    }

    fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        diaryStateFlow.updateItemTitle(itemNumber, title)
    }

    fun updatePicturePath(uri: Uri) {
        diaryStateFlow.picturePath.value = uri
    }

    fun deletePicturePath() {
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

    fun clearShouldJumpItemMotionLayout() {
        shouldJumpItemMotionLayout = false
    }

    private suspend fun shouldShowUpdateConfirmationDialog(): Boolean? {
        if (isLoadedDateEqualToInputDate) return false
        val inputDate = diaryStateFlow.date.requireValue()
        return existsSavedDiary(inputDate)
    }

    // 表示保留中Dialog追加
    // MEMO:引数の型をサブクラスに制限
    fun addPendingDialogList(pendingDialog: DiaryEditPendingDialog) {
        super.addPendingDialogList(pendingDialog)
    }

    // Fragment切替変数クリア
    fun clearNavigationAction() {
        _navigationAction.value = initialNavigationAction
    }

    // UriPermission
    fun clearUriPermissionAction() {
        _uriPermissionAction.value = UriPermissionAction.None
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
            _navigationAction.value = NavigationAction.NavigatePreviousFragment
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
