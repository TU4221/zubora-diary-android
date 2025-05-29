package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString

class WeatherInfoFetchingDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + WeatherInfoFetchingDialogFragment::class.java.name
    }

    override fun createTitle(): String {
        return getString(R.string.dialog_weather_info_fetching_title)
    }

    override fun createMessage(): String {
        val loadingDiaryDate =
            WeatherInfoFetchingDialogFragmentArgs.fromBundle(requireArguments()).date
        val dateString = loadingDiaryDate.toJapaneseDateString(requireContext())
        return dateString + getString(R.string.dialog_weather_info_fetching_message)
    }

    override fun handleOnPositiveButtonClick() {
        setResult(KEY_RESULT, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
