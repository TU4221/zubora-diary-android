package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString

class WeatherInfoFetchDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + WeatherInfoFetchDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_weather_info_fetch_title)
    }

    override fun createMessage(): String {
        val date =
            WeatherInfoFetchDialogFragmentArgs.fromBundle(requireArguments()).parameters.date
        val dateString = date.toJapaneseDateString(requireContext())
        return dateString + getString(R.string.dialog_weather_info_fetch_message)
    }

    override fun handleOnPositiveButtonClick() {
        val parameters =
            WeatherInfoFetchDialogFragmentArgs.fromBundle(requireArguments()).parameters
        setResult(KEY_RESULT, DialogResult.Positive(parameters))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
