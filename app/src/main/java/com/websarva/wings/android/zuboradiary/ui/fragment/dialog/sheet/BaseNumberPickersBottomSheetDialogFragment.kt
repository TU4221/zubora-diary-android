package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.sheet

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.databinding.DialogNumberPickersBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.numberPickerBottomSheetDialogThemeResId
import com.websarva.wings.android.zuboradiary.core.utils.logTag

abstract class BaseNumberPickersBottomSheetDialogFragment
    : BaseBottomSheetDialogFragment<DialogNumberPickersBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            buttonDecision.setOnClickListener {
                Log.d(logTag, "onClick()_PositiveButton")
                handleOnPositiveButtonClick(
                    numberPickerFirst.value,
                    numberPickerSecond.value,
                    numberPickerThird.value
                )
                navigatePreviousFragment()
            }

            buttonCancel.setOnClickListener {
                Log.d(logTag, "onClick()_NegativeButton")
                handleOnNegativeButtonClick()
                navigatePreviousFragment()
            }
        }

        setupNumberPickerTextColor(binding)
        setupNumberPickers()
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogNumberPickersBinding {

        // HACK:下記理由から、ThemeColor#getNumberPickerBottomSheetDialogThemeResId()から
        //      ThemeResIdを取得してInflaterを再作成。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
        val themeResId = themeColor.numberPickerBottomSheetDialogThemeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireActivity(), themeResId)
        val cloneInflater = inflater.cloneInContext(contextWithTheme)

        return DialogNumberPickersBinding
            .inflate(cloneInflater, container, false)
    }

    // HACK:NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
    private fun setupNumberPickerTextColor(binding: DialogNumberPickersBinding) {
        if (Build.VERSION.SDK_INT >= 29) {
            val onSurfaceVariantColor = themeColor.asOnSurfaceVariantColorInt(resources)
            with(binding) {
                numberPickerFirst.textColor = onSurfaceVariantColor
                numberPickerSecond.textColor = onSurfaceVariantColor
                numberPickerThird.textColor = onSurfaceVariantColor
            }
        }
    }

    /**
     * BaseBottomSheetDialogFragment.PositiveButtonClickListener#onClick()で呼び出される。
     */
    protected abstract fun handleOnPositiveButtonClick(
        firstPickerValue: Int,
        secondPickerValue: Int,
        thirdPickerValue: Int
    )

    /**
     * BaseBottomSheetDialogFragment.NegativeButtonClickListener#onClick()で呼び出される。
     */
    protected abstract fun handleOnNegativeButtonClick()

    /**
     * BaseNumberPickersBottomSheetDialogFragment#createDialogView()で呼び出される。
     */
    protected abstract fun setupNumberPickers()
}
