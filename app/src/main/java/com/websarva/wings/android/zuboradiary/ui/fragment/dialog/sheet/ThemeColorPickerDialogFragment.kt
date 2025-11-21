package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.view.View
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.utils.asString
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

/**
 * アプリケーションのテーマカラーを選択するためのボトムシートダイアログ。
 */
class ThemeColorPickerDialogFragment : BaseNumberPickersBottomSheetDialogFragment() {

    override fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    ) {
        setResultSelectedThemeColor(firstPickerValue)
    }

    /**
     * NumberPickerで選択された値を[ThemeColorUi]に変換し、結果として設定する。
     * @param pickerValue NumberPickerから取得した値
     */
    private fun setResultSelectedThemeColor(
        pickerValue: Int,
    ) {
        val selectedThemeColor = ThemeColorUi.entries[pickerValue]

        setResult(RESULT_KEY, DialogResult.Positive(selectedThemeColor))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    override fun setupNumberPickers() {
        val maxNumThemeColors = ThemeColorUi.entries.size
        binding.numberPickerFirst.maxValue = maxNumThemeColors - 1
        binding.numberPickerFirst.minValue = 0
        setupInitialValue()
        setupDisplayedValues()
        binding.numberPickerFirst.wrapSelectorWheel = false
        binding.numberPickerSecond.visibility = View.GONE
        binding.numberPickerThird.visibility = View.GONE
    }

    /** NumberPickerの初期値を設定する。 */
    private fun setupInitialValue() {
        binding.numberPickerFirst.value =
            themeColor.ordinal // MEMO:最大最小値を設定してから設定すること。(0の位置が表示される)
    }

    /** NumberPickerに表示するテーマカラーの文字列を設定する。 */
    private fun setupDisplayedValues() {
        val maxNumThemeColors = ThemeColorUi.entries.size
        val themeColorList = arrayOfNulls<String>(maxNumThemeColors)
        for (item in ThemeColorUi.entries) {
            val ordinal = item.ordinal
            themeColorList[ordinal] = item.asString(requireContext())
        }
        binding.numberPickerFirst.displayedValues = themeColorList
    }

    internal companion object {
        /** このダイアログから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + ThemeColorPickerDialogFragment::class.java.name
    }
}
