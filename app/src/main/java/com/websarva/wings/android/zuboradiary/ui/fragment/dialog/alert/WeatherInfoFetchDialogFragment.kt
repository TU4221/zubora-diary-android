package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString

class WeatherInfoFetchDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val RESULT_KEY = RESULT_KEY_PREFIX + WeatherInfoFetchDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_weather_info_fetch_title)
    }

    override fun createMessage(): String {
        val date =
            WeatherInfoFetchDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateString = date.formatDateString(requireContext())
        return dateString + getString(R.string.dialog_weather_info_fetch_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }
}
