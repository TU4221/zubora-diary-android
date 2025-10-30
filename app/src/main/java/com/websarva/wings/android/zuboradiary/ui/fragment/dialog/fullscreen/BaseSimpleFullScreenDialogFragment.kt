package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.fragment.FragmentHelper
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.utils.enableEdgeToEdge

abstract class BaseSimpleFullScreenDialogFragment<T: ViewBinding>: DialogFragment() {

    // View関係
    private var _binding: T? = null
    internal val binding get() = checkNotNull(_binding)

    internal val fragmentHelper = FragmentHelper()

    internal val themeColor
        get() = (requireActivity() as MainActivity).themeColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MaterialFullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val themeColorInflater = fragmentHelper.createThemeColorInflater(inflater, themeColor)
        _binding = createViewBinding(themeColorInflater, container)
        return binding.root
    }

    internal abstract fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableEdgeToEdge(themeColor)
        val dialogWindow = checkNotNull(dialog?.window)
        ThemeColorChanger().applyStatusBarIconColor(dialogWindow, themeColor)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        clearViewBindings()
    }

    internal open fun clearViewBindings() {
        _binding = null
    }

    internal fun navigatePreviousFragment() {
        findNavController().navigateUp()
    }
}
