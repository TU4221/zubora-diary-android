package com.websarva.wings.android.zuboradiary.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.databinding.ViewDataBinding
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding
import com.websarva.wings.android.zuboradiary.ui.base.BaseFragment

class OpenSourceLicensesFragment: BaseFragment() {

    // View関係
    private var _binding: FragmentOpenSourceLicensesBinding? = null
    private val binding get() = checkNotNull(_binding)

    // ViewModel
    // MEMO:ViewModel不要の為、nullを代入。
    @Suppress("unused")
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
                navController.navigateUp()
            }
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun receiveDialogResults() {
        // 処理なし
    }

    override fun removeDialogResults() {
        // 処理なし
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        // 処理なし
    }

    override fun destroyBinding() {
        _binding = null
    }

}
