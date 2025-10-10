package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

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
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.chipColors
import com.mikepenz.aboutlibraries.ui.compose.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.websarva.wings.android.zuboradiary.databinding.FragmentOpenSourceLicensesBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asPrimaryColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt

class OpenSourceLicensesDialogFragment: BaseSimpleFullScreenDialogFragment<FragmentOpenSourceLicensesBinding>() {

    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOpenSourceLicensesBinding {
        return FragmentOpenSourceLicensesBinding.inflate(themeColorInflater, container, false)
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

    // TODO:VerUpによる仮修正
    private fun setUpAboutLibraries() {
        binding.composeViewAboutLibraries
            .apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    // rememberLibraries を使ってライブラリ情報をロード
                    val libraries = rememberLibraries()

                    // 新しい LibrariesContainer のシグネチャに合わせて修正
                    LibrariesContainer(
                        libraries = libraries.value, // Libs オブジェクトを渡す
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        colors = LibraryDefaults.libraryColors(
                            backgroundColor = Color(themeColor.asSecondaryContainerColorInt(resources)),
                            contentColor = Color(themeColor.asPrimaryColorInt(resources)),
                            licenseChipColors = LibraryDefaults.chipColors(),
                            dialogConfirmButtonColor = Color(themeColor.asPrimaryColorInt(resources))
                        ),
                        // itemContentPadding と itemSpacing は padding や dimensions パラメータに統合されたか、
                        // LibraryDefaults 内で設定するようになった可能性があります。
                        // LibraryDefaults.libraryPadding() や LibraryDefaults.libraryDimensions() を確認してください。
                        // もしこれらの引数が直接ない場合は、一旦削除してデフォルトの挙動を確認するか、
                        // ライブラリのドキュメントで新しい指定方法を確認してください。
                        // 例として、LibraryDefaults を使った padding の設定例 (実際のAPIと異なる場合があります)
                        // padding = LibraryDefaults.libraryPadding(content = LibraryDefaults.ContentPadding),
                        // dimensions = LibraryDefaults.libraryDimensions(itemSpacing = 8.dp)
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
