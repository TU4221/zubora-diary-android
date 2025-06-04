package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding

class OpenSourceLicensesDialogFragment: BaseFullScreenDialogFragment<FragmentOpenSourceLicensesBinding>() {

    override fun createViewDataBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOpenSourceLicensesBinding {
        return FragmentOpenSourceLicensesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        setUpAboutLibraries()
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navigatePreviousFragment()
            }
        resizeToolbar()
    }

    // HACK:EdgeToEdgeを有効にした状態で、LayoutファイルのToolBarタグの属性"fitsSystemWindows"を有効にすると、
    //      高さがStatusBar分(？)高くなる。その為下記メソッドでToolbarの高さを修正する。
    // TODO:他のToolbarにも実装する
    private fun resizeToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.materialToolbarTopAppBar
        ) { toolbarView, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbarView.updatePadding(top = systemBars.top)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setUpAboutLibraries() {
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
    }
}
