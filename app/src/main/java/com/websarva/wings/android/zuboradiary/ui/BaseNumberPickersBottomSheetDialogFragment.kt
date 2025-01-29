package com.websarva.wings.android.zuboradiary.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.NavHostFragment
import com.websarva.wings.android.zuboradiary.databinding.DialogFragmentNumberPickersBinding

abstract class BaseNumberPickersBottomSheetDialogFragment : BaseBottomSheetDialogFragment() {

    // View関係
    protected lateinit var binding: DialogFragmentNumberPickersBinding

    override fun createDialogView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = createBinding(inflater, container)
        binding.buttonDecision.setOnClickListener(PositiveButtonClickListener())
        binding.buttonCancel.setOnClickListener(NegativeButtonClickListener())
        setUpNumberPickers(binding)
        return binding.root
    }

    private fun createBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFragmentNumberPickersBinding {
        // HACK:下記理由から、ThemeColor#getNumberPickerBottomSheetDialogThemeResId()から
        //      ThemeResIdを取得してInflaterを再作成。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。

        val themeColor = requireThemeColor()
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
            val themeColor = requireThemeColor()
            val onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources)
            binding.numberPickerFirst.textColor = onSurfaceVariantColor
            binding.numberPickerSecond.textColor = onSurfaceVariantColor
            binding.numberPickerThird.textColor = onSurfaceVariantColor
        }
    }

    /**
     * BaseNumberPickersBottomSheetDialogFragment#createDialogView()で呼び出される。
     */
    protected abstract fun setUpNumberPickers(binding: DialogFragmentNumberPickersBinding?)

    protected fun setResult(key: String, value: Any) {
        val navController = NavHostFragment.findNavController(this)
        val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle[key] = value
    }
}
