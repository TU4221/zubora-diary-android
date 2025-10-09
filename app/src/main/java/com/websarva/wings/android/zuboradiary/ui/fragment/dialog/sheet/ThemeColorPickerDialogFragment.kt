package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.mapper.asString
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class ThemeColorPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + ThemeColorPickerDialogFragment::class.java.name
    }

    override fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    ) {
        setResultSelectedThemeColor(firstPickerValue)
    }

    private fun setResultSelectedThemeColor(
        pickerValue: Int,
    ) {
        val selectedThemeColor = ThemeColorUi.entries[pickerValue]

        setResult(KEY_RESULT, DialogResult.Positive(selectedThemeColor))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }

    override fun setUpNumberPickers() {
        val maxNumThemeColors = ThemeColorUi.entries.size
        binding.numberPickerFirst.maxValue = maxNumThemeColors - 1
        binding.numberPickerFirst.minValue = 0
        setUpInitialValue()
        setUpDisplayedValues()
        binding.numberPickerFirst.wrapSelectorWheel = false
        binding.numberPickerSecond.visibility = View.GONE
        binding.numberPickerThird.visibility = View.GONE
    }

    private fun setUpInitialValue() {
        binding.numberPickerFirst.value =
            themeColor.ordinal // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
    }

    private fun setUpDisplayedValues() {
        val maxNumThemeColors = ThemeColorUi.entries.size
        val themeColorList = arrayOfNulls<String>(maxNumThemeColors)
        for (item in ThemeColorUi.entries) {
            val ordinal = item.ordinal
            themeColorList[ordinal] = item.asString(requireContext())
        }
        binding.numberPickerFirst.displayedValues = themeColorList
    }
}
