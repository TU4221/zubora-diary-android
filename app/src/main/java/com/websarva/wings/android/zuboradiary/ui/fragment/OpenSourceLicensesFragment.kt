package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.databinding.ViewDataBinding
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding

class OpenSourceLicensesFragment: BaseFragment() {

    // View関係
    private var _binding: FragmentOpenSourceLicensesBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:ViewModel不要の為、nullを代入。
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        binding.composeViewAboutLibraries
            .apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    LibrariesContainer(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        colors = LibraryDefaults.libraryColors(
                            backgroundColor = Color(themeColor.getSecondaryContainerColor(resources)),
                            badgeBackgroundColor = Color(themeColor.getPrimaryColor(resources)),
                            dialogConfirmButtonColor = Color(themeColor.getPrimaryColor(resources))
                        ),
                        itemContentPadding = LibraryDefaults.ContentPadding,
                        itemSpacing = 8.dp
                    )
                }
            }

        return view
    }

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentOpenSourceLicensesBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@OpenSourceLicensesFragment.viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navigatePreviousFragment()
            }
    }

    override fun initializeFragmentResultReceiver() {
        // 処理なし
    }

    override fun onNavigateAppMessageDialog(appMessage: AppMessage) {
        // 処理なし
    }

    override fun destroyBinding() {
        _binding = null
    }

}
