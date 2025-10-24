package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.FragmentHelper
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.utils.enableEdgeToEdge
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel

abstract class BaseSimpleFullScreenDialogFragment<T: ViewBinding>: DialogFragment() {

    // View関係
    private var _binding: T? = null
    internal val binding get() = checkNotNull(_binding)

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    internal val fragmentHelper = FragmentHelper()

    internal val themeColor
        get() = settingsViewModel.uiState.value.themeColor!!

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
