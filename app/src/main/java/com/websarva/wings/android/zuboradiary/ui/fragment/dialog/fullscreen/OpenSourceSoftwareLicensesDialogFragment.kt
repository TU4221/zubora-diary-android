package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.chipColors
import com.mikepenz.aboutlibraries.ui.compose.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.websarva.wings.android.zuboradiary.databinding.DialogOpenSourceSoftwareLicensesBinding
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asPrimaryColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSurfaceColorInt

class OpenSourceSoftwareLicensesDialogFragment
    : BaseSimpleFullScreenDialogFragment<DialogOpenSourceSoftwareLicensesBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        setUpAboutLibraries()
    }

    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): DialogOpenSourceSoftwareLicensesBinding {
        return DialogOpenSourceSoftwareLicensesBinding.inflate(themeColorInflater, container, false)
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navigatePreviousFragment()
            }
    }

    private fun setUpAboutLibraries() {
        with (binding.composeViewAboutLibraries) {
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

                    // 色の設定
                    colors = LibraryDefaults.libraryColors(
                        // ダイアログ背景色
                        backgroundColor = Color(themeColor.asSurfaceColorInt(resources)),

                        // リストアイテム、ダイアログ文字
                        contentColor = Color(themeColor.asOnSurfaceColorInt(resources)),

                        // バージョンチップ
                        versionChipColors = LibraryDefaults.chipColors(
                            containerColor = Color(themeColor.asSecondaryContainerColorInt(resources)),
                            contentColor = Color(themeColor.asOnSecondaryContainerColorInt(resources))
                        ),

                        // ライセンスチップ
                        licenseChipColors = LibraryDefaults.chipColors(
                            containerColor = Color(themeColor.asSecondaryContainerColorInt(resources)),
                            contentColor = Color(themeColor.asOnSecondaryContainerColorInt(resources))
                        ),

                        // ダイアログの確認ボタン
                        dialogConfirmButtonColor = Color(themeColor.asPrimaryColorInt(resources))
                    )
                )
            }
        }
    }
}
