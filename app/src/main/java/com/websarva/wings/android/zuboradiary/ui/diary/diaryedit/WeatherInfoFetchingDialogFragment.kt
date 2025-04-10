package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.DialogInterface
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter
import com.websarva.wings.android.zuboradiary.ui.base.BaseAlertDialogFragment

class WeatherInfoFetchingDialogFragment : BaseAlertDialogFragment() {

    companion object {
        private val fromClassName = "From" + WeatherInfoFetchingDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_BUTTON: String = "SelectedButton$fromClassName"
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_weather_info_fetching_title)
    }

    override fun createMessage(): String {
        val loadingDiaryDate =
            WeatherInfoFetchingDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateTimeStringConverter = DateTimeStringConverter()
        val dateString = dateTimeStringConverter.toYearMonthDayWeek(loadingDiaryDate)
        return dateString + getString(R.string.dialog_weather_info_fetching_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_POSITIVE)
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }

    override fun handleOnCancel() {
        setResult(KEY_SELECTED_BUTTON, DialogInterface.BUTTON_NEGATIVE)
    }
}
