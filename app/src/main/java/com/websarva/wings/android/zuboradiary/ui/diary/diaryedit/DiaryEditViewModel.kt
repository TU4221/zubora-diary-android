package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiCallable
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val weatherApiRepository: WeatherApiRepository
) :
    BaseViewModel() {
    // Getter
    // 日記データ関係
    private val _previousDate = MutableLiveData<LocalDate?>()
    val previousDate: LiveData<LocalDate?>
        get() = _previousDate

    private val _loadedDate = MutableLiveData<LocalDate?>()
    val loadedDate: LiveData<LocalDate?>
        get() = _loadedDate

    private val diaryLiveData = DiaryLiveData()

    val date: LiveData<LocalDate?>
        get() = diaryLiveData.date

    /**
     * LayoutDataBinding用
     * */
    val titleMutableLiveData: MutableLiveData<String>
        get() = diaryLiveData.title

    val weather1: LiveData<Weather>
        get() = diaryLiveData.weather1

    val weather2: LiveData<Weather>
        get() = diaryLiveData.weather2

    val condition: LiveData<Condition>
        get() = diaryLiveData.condition

    val numVisibleItems: LiveData<Int>
        get() = diaryLiveData.numVisibleItems

    /**
     * LayoutDataBinding用
     * */
    val item1TitleMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(1)).title

    /**
     * LayoutDataBinding用
     * */
    val item2TitleMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(2)).title

    /**
     * LayoutDataBinding用
     * */
    val item3TitleMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(3)).title

    /**
     * LayoutDataBinding用
     * */
    val item4TitleMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(4)).title

    /**
     * LayoutDataBinding用
     * */
    val item5TitleMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(5)).title

    /**
     * LayoutDataBinding用
     * */
    val item1CommentMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(1)).comment

    /**
     * LayoutDataBinding用
     * */
    val item2CommentMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(2)).comment

    /**
     * LayoutDataBinding用
     * */
    val item3CommentMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(3)).comment

    /**
     * LayoutDataBinding用
     * */
    val item4CommentMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(4)).comment

    /**
     * LayoutDataBinding用
     * */
    val item5CommentMutable: MutableLiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(5)).comment

    val picturePath: LiveData<Uri?>
        get() = diaryLiveData.picturePath

    private val _loadedPicturePath = MutableLiveData<Uri?>()
    val loadedPicturePath: LiveData<Uri?>
        get() = _loadedPicturePath

    var hasPreparedDiary = false
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

    val shouldShowUpdateConfirmationDialog: Boolean
        get() {
            if (isLoadedDateEqualToInputDate) return false
            val inputDate = checkNotNull(diaryLiveData.date.value)
            return existsSavedDiary(inputDate)
        }

    private val isLoadedDateEqualToInputDate: Boolean
        get() {
            val loadedDate = _loadedDate.value ?: return false
            val inputDate = diaryLiveData.date.value
            return loadedDate == inputDate
        }

    val isEqualWeathers: Boolean
        get() {
            val weather1 = checkNotNull(diaryLiveData.weather1.value)
            val weather2 = checkNotNull(diaryLiveData.weather2.value)

            return weather1 == weather2
        }

    // Fragment切替記憶
    var isShowingItemTitleEditFragment = false
        private set

    init {
        initialize()
    }

    public override fun initialize() {
        initializeAppMessageList()
        hasPreparedDiary = false
        _previousDate.value = null
        _loadedDate.value = null
        _loadedPicturePath.value = null
        diaryLiveData.initialize()
        isShowingItemTitleEditFragment = false
    }

    fun prepareDiary(date: LocalDate, requiresDiaryLoading: Boolean) {
        if (requiresDiaryLoading) {
            try {
                loadSavedDiary(date)
            } catch (e: NoSuchElementException) {
                updateDate(date)
            } catch (e: CancellationException) {
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                return
            } catch (e: ExecutionException) {
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                return
            } catch (e: InterruptedException) {
                addAppMessage(AppMessage.DIARY_LOADING_ERROR)
                return
            }
        } else {
            updateDate(date)
        }
        hasPreparedDiary = true
    }

    @Throws(
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class,
        NoSuchElementException::class
    )
    private fun loadSavedDiary(date: LocalDate) {
        val diaryEntity = diaryRepository.loadDiary(date).get()
            ?: throw NoSuchElementException()

        // HACK:下記はDiaryLiveData#update()処理よりも前に処理すること。
        //      (後で処理するとDiaryLiveDataのDateのObserverがloadedDateの更新よりも先に処理される為)
        _loadedDate.value = date

        diaryLiveData.update(diaryEntity)
        _loadedPicturePath.value = diaryLiveData.picturePath.value
    }

    fun existsSavedDiary(date: LocalDate): Boolean {
        try {
            return diaryRepository.existsDiary(date).get()
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        }
    }

    // TODO:TestDiariesSaverクラス削除後、public削除。
    fun saveDiary(): Boolean {
        val diaryEntity = diaryLiveData.createDiaryEntity()
        val diaryItemTitleSelectionHistoryItemEntityList =
            diaryLiveData.createDiaryItemTitleSelectionHistoryItemEntityList()
        try {
            if (shouldDeleteLoadedDateDiary) {
                diaryRepository
                    .deleteAndSaveDiary(
                        _loadedDate.checkNotNull(),
                        diaryEntity,
                        diaryItemTitleSelectionHistoryItemEntityList
                    )
                    .get()
            } else {
                diaryRepository
                    .saveDiary(diaryEntity, diaryItemTitleSelectionHistoryItemEntityList).get()
            }
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_SAVING_ERROR)
            return false
        }
        return true
    }

    fun deleteDiary(): Boolean {
        val deleteDate = _loadedDate.checkNotNull()

        val result: Int
        try {
            result = diaryRepository.deleteDiary(deleteDate).get()
        } catch (e: CancellationException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }

        // 削除件数 = 1が正常
        if (result != 1) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }

        return true
    }

    // 日付関係
    // TODO:TestDiariesSaverクラス削除後、public削除。
    fun updateDate(date: LocalDate) {
        val previousDate = diaryLiveData.date.value

        // HACK:下記はDiaryLiveDataのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        this._previousDate.value = previousDate

        diaryLiveData.date.value = date
    }

    // 天気、体調関係
    // MEMO:Weather、Conditionsから文字列に変換するにはContextが必要なため、
    //      Fragment上のLivedDateObserverにて変換した値を受け取る。
    fun updateWeather1(weather: Weather) {
        diaryLiveData.weather1.value = weather
    }

    fun updateWeather2(weather: Weather) {
        diaryLiveData.weather2.value = weather
    }

    fun updateCondition(condition: Condition) {
        diaryLiveData.condition.value = condition
    }

    fun canFetchWeatherInformation(date: LocalDate): Boolean {
        return weatherApiRepository.canFetchWeatherInfo(date)
    }

    // 天気情報関係
    fun fetchWeatherInformation(date: LocalDate, geoCoordinates: GeoCoordinates) {
        if (!canFetchWeatherInformation(date)) return

        val currentDate = LocalDate.now()
        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)
        val weatherApiResponseCall = if (betweenDays == 0L) {
            weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates)
        } else {
            weatherApiRepository.fetchPastDayWeatherInfo(geoCoordinates, betweenDays.toInt())
        }

        val executorService = Executors.newSingleThreadExecutor()
        try {
            executorService.submit(CustomWeatherApiCallable(weatherApiResponseCall)).get()
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
        }
    }

    private inner class CustomWeatherApiCallable(weatherApiResponseCall: Call<WeatherApiResponse>) :
        WeatherApiCallable(weatherApiResponseCall) {
        override fun onResponse(weather: Weather) {
            diaryLiveData.weather1.postValue(weather)
        }

        override fun onFailure() {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
        }

        override fun onException(e: Exception) {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
        }
    }

    // 項目関係
    fun incrementVisibleItemsCount() {
        diaryLiveData.incrementVisibleItemsCount()
    }

    fun getItemTitleLiveData(itemNumber: ItemNumber): LiveData<String> {
        return diaryLiveData.getItemLiveData(itemNumber).title
    }

    fun deleteItem(itemNumber: ItemNumber) {
        diaryLiveData.deleteItem(itemNumber)
    }

    fun updateItemTitle(itemNumber: ItemNumber, title: String) {
        diaryLiveData.updateItemTitle(itemNumber, title)
    }

    fun updatePicturePath(uri: Uri) {
        diaryLiveData.picturePath.value = uri
    }

    fun deletePicturePath() {
        diaryLiveData.picturePath.value = null
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean {
        try {
            return !diaryRepository.existsPicturePath(uri).get()
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        }
    }

    // Fragment切替記憶
    fun updateIsShowingItemTitleEditFragment(isShowing: Boolean) {
        isShowingItemTitleEditFragment = isShowing
    }

    fun addWeatherInfoFetchErrorMessage() {
        addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR)
    }
}
