package com.websarva.wings.android.zuboradiary.ui

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding

abstract class BaseNumberPickersBottomSheetDialogFragment : BaseBottomSheetDialogFragment() {

    // View関係
    private var _binding: DialogFragmentNumberPickersBinding? = null
    protected val binding get() = checkNotNull(_binding)

    override fun createDialogView(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        Log.d(javaClass.simpleName, "createDialogView()")

        _binding = createBinding(inflater, container)
        return binding.apply {
            buttonDecision.setOnClickListener(PositiveButtonClickListener())
            buttonCancel.setOnClickListener(NegativeButtonClickListener())
            setUpNumberPickers(this)
        }.root
    }

    private fun createBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFragmentNumberPickersBinding {
        // HACK:下記理由から、ThemeColor#getNumberPickerBottomSheetDialogThemeResId()から
        //      ThemeResIdを取得してInflaterを再作成。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。

        val themeResId = themeColor.numberPickerBottomSheetDialogThemeResId
        val contextWithTheme: Context = ContextThemeWrapper(requireActivity(), themeResId)
        val cloneInflater = inflater.cloneInContext(contextWithTheme)

        val binding =
            DialogFragmentNumberPickersBinding.inflate(cloneInflater, container, false)

        setUpNumberPickerTextColor(binding)

        return binding
    }

    private fun setUpNumberPickerTextColor(binding: DialogFragmentNumberPickersBinding) {
        if (Build.VERSION.SDK_INT >= 29) {
            val onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources)
            binding.apply {
                numberPickerFirst.textColor = onSurfaceVariantColor
                numberPickerSecond.textColor = onSurfaceVariantColor
                numberPickerThird.textColor = onSurfaceVariantColor
            }
        }
    }

    /**
     * BaseNumberPickersBottomSheetDialogFragment#createDialogView()で呼び出される。
     */
    protected abstract fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding)

    override fun onDestroyView() {
        Log.d(javaClass.simpleName, "onDestroyView()")
        super.onDestroyView()
        _binding = null
    }
}
