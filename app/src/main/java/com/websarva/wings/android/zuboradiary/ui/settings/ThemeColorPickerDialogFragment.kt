package com.websarva.wings.android.zuboradiary.ui.settings

import android.content.DialogInterface
import android.view.View
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.BaseNumberPickersBottomSheetDialogFragment

class ThemeColorPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    companion object {
        private val fromClassName = "From" + ThemeColorPickerDialogFragment::class.java.name
        @JvmField
        val KEY_SELECTED_THEME_COLOR: String = "SelectedThemeColor$fromClassName"
    }

    override val isCancelableOtherThanPressingButton
        get() = true

    override fun handleOnPositiveButtonClick(v: View) {
        setResultSelectedThemeColor()
    }

    private fun setResultSelectedThemeColor() {
        val selectedValue = binding.numberPickerFirst.value
        val selectedThemeColor = ThemeColor.entries[selectedValue]

        setResult(KEY_SELECTED_THEME_COLOR, selectedThemeColor)
    }

    override fun handleOnNegativeButtonClick(v: View) {
        // 処理なし
    }

    override fun handleOnCancel(dialog: DialogInterface) {
        // 処理なし
    }

    override fun handleOnDismiss() {
    }

    override fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding) {
        val maxNumThemeColors = ThemeColor.entries.size
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
        val maxNumThemeColors = ThemeColor.entries.size
        val themeColorList = arrayOfNulls<String>(maxNumThemeColors)
        for (item in ThemeColor.entries) {
            val ordinal = item.ordinal
            themeColorList[ordinal] = item.toSting(requireContext())
        }
        binding.numberPickerFirst.displayedValues = themeColorList
    }
}
