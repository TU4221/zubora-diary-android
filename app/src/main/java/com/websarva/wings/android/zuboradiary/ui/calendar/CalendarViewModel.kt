package com.websarva.wings.android.zuboradiary.ui.calendar

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    private val _selectedDate = MutableLiveData<LocalDate>()
    internal val selectedDate: LiveData<LocalDate> get() = _selectedDate

    private val _previousSelectedDate = MutableLiveData<LocalDate?>()
    internal val previousSelectedDate: LiveData<LocalDate?> get() = _previousSelectedDate

    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        _selectedDate.value = LocalDate.now()
        _previousSelectedDate.value = null
    }

    private class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            handler.post(command)
        }
    }

    fun existsSavedDiary(date: LocalDate, callback: ViewModelCallback<Boolean>) {
        viewModelScope.launch {
            try {
                val existsDiary = diaryRepository.existsDiary(date)
                callback.onSuccess(existsDiary)
            } catch (e: Exception) {
                callback.onFailure(e)

                // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
                //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
                if (equalLastAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)) return@launch
                addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            }
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _previousSelectedDate.value = _selectedDate.value
        _selectedDate.value = date
    }
}
