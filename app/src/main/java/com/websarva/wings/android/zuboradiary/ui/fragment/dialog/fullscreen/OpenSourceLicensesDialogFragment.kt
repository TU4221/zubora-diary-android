package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults.chipPadding
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.chipColors
import com.mikepenz.aboutlibraries.ui.compose.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asErrorColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asPrimaryColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt

class OpenSourceLicensesDialogFragment: BaseSimpleFullScreenDialogFragment<FragmentOpenSourceLicensesBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        setUpAboutLibraries()
    }

    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOpenSourceLicensesBinding {
        return FragmentOpenSourceLicensesBinding.inflate(themeColorInflater, container, false)
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navigatePreviousFragment()
            }
    }

    // TODO:VerUpによる仮修正
    private fun setUpAboutLibraries() {
        binding.composeViewAboutLibraries
            .apply {
                // Compositionの破棄タイミングをFragmentのViewのライフサイクルと連動
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )

                setContent {
                    // ライブラリ情報を読み込み、インスタンス化
                    val libraries = rememberLibraries()
                    // 新しい LibrariesContainer のシグネチャに合わせて修正
                    LibrariesContainer(
                        libraries = libraries.value, // Libs オブジェクトを渡す
                        modifier = Modifier.fillMaxSize(),


                        // リスト全体の左右の余白
                        contentPadding = PaddingValues(horizontal = 16.dp),

                        // 色の設定
                        colors = LibraryDefaults.libraryColors(
                            backgroundColor = Color(themeColor.asSecondaryContainerColorInt(resources)),
                            contentColor = Color(themeColor.asPrimaryColorInt(resources)),
                            versionChipColors = LibraryDefaults.chipColors(
                                containerColor = Color(
                                    themeColor.asSecondaryContainerColorInt(resources)
                                )
                            ),
                            licenseChipColors = LibraryDefaults.chipColors(),
                            fundingChipColors = LibraryDefaults.chipColors(
                                    containerColor = Color(themeColor.asErrorColorInt(resources)),
                                    contentColor = contentColorFor(
                                        Color(themeColor.asErrorColorInt(resources))
                                    ),
                                ),
                            dialogConfirmButtonColor = Color(themeColor.asPrimaryColorInt(resources))
                        ),

                        // 各ライブラリ項目の「内部」の余白を設定
                        padding = LibraryDefaults.libraryPadding(
                            contentPadding = PaddingValues(16.dp),
                            namePadding = PaddingValues(0.dp),
                            versionPadding = chipPadding(
                                containerPadding = PaddingValues(start = 8.dp)
                            ),
                            licensePadding = chipPadding(),
                            fundingPadding = chipPadding(),
                            verticalPadding = 2.dp,
                            licenseDialogContentPadding = 8.dp
                        ),

                        // 各ライブラリ項目「間」のスペースを設定
                        dimensions = LibraryDefaults.libraryDimensions(
                            itemSpacing = 8.dp, // 各項目間の縦のスペース
                            chipMinHeight = 16.dp
                        )
                    )

                    /*LibrariesContainer(
                        modifier = Modifier.fillMaxSize(), //
                        contentPadding = PaddingValues(16.dp),//
                        colors = LibraryDefaults.libraryColors(
                            backgroundColor = Color(themeColor.getSecondaryContainerColor(resources)),
                            badgeBackgroundColor = Color(themeColor.getPrimaryColor(resources)),
                            dialogConfirmButtonColor = Color(themeColor.getPrimaryColor(resources))
                        ),
                        itemContentPadding = LibraryDefaults.ContentPadding,
                        itemSpacing = 8.dp
                    )*/
                }
            }
    }
}
