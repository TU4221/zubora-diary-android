package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorChanger
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel

abstract class BaseFullScreenDialogFragment<T: ViewDataBinding>: DialogFragment() {

    // View関係
    private var _binding: T? = null
    internal val binding get() = checkNotNull(_binding)

    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    internal val settingsViewModel: SettingsViewModel by activityViewModels()

    internal val themeColor
        get() = settingsViewModel.themeColor.requireValue()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MaterialFullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val themeColorInflater = createThemeColorInflater(inflater, themeColor)
        _binding = createViewDataBinding(themeColorInflater, container)
        return binding.root
    }

    // ThemeColorに合わせたインフレーター作成
    private fun createThemeColorInflater(
        inflater: LayoutInflater,
        themeColor: ThemeColor
    ): LayoutInflater {
        return ThemeColorInflaterCreator().create(inflater, themeColor)
    }

    internal abstract fun createViewDataBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpEdgeToEdge()
        setUpStatusBarAndNavigationBarIconColor()
    }

    private fun setUpEdgeToEdge() {
        dialog?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isNavigationBarContrastEnforced = false
            }
        }
    }

    private fun setUpStatusBarAndNavigationBarIconColor() {
        dialog?.window?.let {
            val changer = ThemeColorChanger()
            changer.applyStatusBarColor(it, themeColor)
            changer.applyNavigationBarColor(it, themeColor)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    internal fun navigatePreviousFragment() {
        findNavController().navigateUp()
    }
}
