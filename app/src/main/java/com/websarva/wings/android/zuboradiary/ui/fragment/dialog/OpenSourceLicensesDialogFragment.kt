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
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding

class OpenSourceLicensesDialogFragment: BaseSimpleFullScreenDialogFragment<FragmentOpenSourceLicensesBinding>() {

    override fun createViewBinding(
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
