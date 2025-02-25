package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.requireValue
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val weatherApiRepository: WeatherApiRepository
) : BaseViewModel() {
    // Getter
    // 日記データ関係
    private val initialPreviousDate = null
    private val _previousDate = MutableStateFlow<LocalDate?>(initialPreviousDate)
    val previousDate
        get() = _previousDate.asStateFlow()

    private val initialLoadedDate = null
    private val _loadedDate = MutableStateFlow<LocalDate?>(initialLoadedDate)
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

    private val initialLoadedPicturePath = null
    private val _loadedPicturePath = MutableStateFlow<Uri?>(initialLoadedPicturePath)
    val loadedPicturePath
        get() = _loadedPicturePath.asStateFlow()

    private val initialIsVisibleUpdateProgressBar = false
    private val _isVisibleUpdateProgressBar = MutableStateFlow(initialIsVisibleUpdateProgressBar)
    val isVisibleUpdateProgressBar
        get() = _isVisibleUpdateProgressBar.asStateFlow()

    private val initialHasPreparedDiary = false
    var hasPreparedDiary = initialHasPreparedDiary
        private set

    val isNewDiaryDefaultStatus
        get() = hasPreparedDiary && _previousDate.value == null && _loadedDate.value == null

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

    // Fragment切替記憶
    private val initialIsShowingItemTitleEditFragment = false
    var isShowingItemTitleEditFragment = initialIsShowingItemTitleEditFragment
        private set

    init {
        initialize()
    }

    public override fun initialize() {
        initializeAppMessageList()
        hasPreparedDiary = initialHasPreparedDiary
        _previousDate.value = initialPreviousDate
        _loadedDate.value = initialLoadedDate
        _loadedPicturePath.value = initialLoadedPicturePath
        diaryStateFlow.initialize()
        isShowingItemTitleEditFragment = initialIsShowingItemTitleEditFragment
    }

    suspend fun prepareDiary(date: LocalDate, shouldLoadDiary: Boolean): Boolean {
        _isVisibleUpdateProgressBar.value = true
        if (shouldLoadDiary) {
            try {
                loadSavedDiary(date)
            } catch (e: NoSuchElementException) {
                updateDate(date)
            } catch (e: Exception) {
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                _isVisibleUpdateProgressBar.value = false
                return false
            }
        } else {
            updateDate(date)
        }
        hasPreparedDiary = true
        _isVisibleUpdateProgressBar.value = false
        return true
    }

    @Throws(Exception::class)
    private suspend fun loadSavedDiary(date: LocalDate): Boolean {
        val diaryEntity: DiaryEntity
        try {
            diaryEntity = diaryRepository.loadDiary(date)

            // HACK:下記はDiaryStateFlow#update()処理よりも前に処理すること。
            //      (後で処理するとDiaryStateFlowのDateのObserverがloadedDateの更新よりも先に処理される為)
            _loadedDate.value = date

            diaryStateFlow.update(diaryEntity)
            _loadedPicturePath.value = diaryStateFlow.picturePath.value
        } catch (e: Exception) {
            Log.d("Exception", "loadSavedDiary()" , e)
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        }
        return true
    }

    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return null
        }
    }

    // TODO:TestDiariesSaverクラス削除後、public削除。
    suspend fun saveDiary(): Boolean {
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
            addAppMessage(AppMessage.DIARY_SAVING_ERROR)
            return false
        }
        return true
    }

    suspend fun deleteDiary(): Boolean {
        val deleteDate = _loadedDate.requireValue()

        try {
            diaryRepository.deleteDiary(deleteDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }
        return true
    }

    // 日付関係
    // TODO:TestDiariesSaverクラス削除後、public削除。
    fun updateDate(date: LocalDate) {
        val previousDate = diaryStateFlow.date.value

        // HACK:下記はDiaryStateFlowのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        _previousDate.value = previousDate

        diaryStateFlow.date.value = date
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

    fun canFetchWeatherInformation(date: LocalDate): Boolean {
        return weatherApiRepository.canFetchWeatherInfo(date)
    }

    // 天気情報関係
    suspend fun fetchWeatherInformation(date: LocalDate, geoCoordinates: GeoCoordinates) {
        if (!canFetchWeatherInformation(date)) return

        val currentDate = LocalDate.now()
        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)

        _isVisibleUpdateProgressBar.value = true
        val response =
            if (betweenDays == 0L) {
                weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates)
            } else {
                weatherApiRepository.fetchPastDayWeatherInfo(
                    geoCoordinates,
                    betweenDays.toInt()
                )
            }
        Log.d("WeatherApi", "response.code():" + response.code())
        Log.d("WeatherApi", "response.message():" + response.message())

        if (response.isSuccessful) {
            Log.d("WeatherApi", "response.body():" + response.body())
            val result =
                response.body()?.toWeatherInfo() ?: throw IllegalStateException()
            diaryStateFlow.weather1.value = result
        } else {
            response.errorBody().use { errorBody ->
                Log.d("WeatherApi", "response.errorBody():" + errorBody!!.string())
            }
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
        }
        _isVisibleUpdateProgressBar.value = false
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
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return null
        }
    }

    // Fragment切替記憶
    fun updateIsShowingItemTitleEditFragment(isShowing: Boolean) {
        isShowingItemTitleEditFragment = isShowing
    }

    fun addWeatherInfoFetchErrorMessage() {
        addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
    }

    suspend fun shouldShowUpdateConfirmationDialog(): Boolean? {
        if (isLoadedDateEqualToInputDate) return false
        val inputDate = diaryStateFlow.date.requireValue()
        return existsSavedDiary(inputDate)
    }
}
