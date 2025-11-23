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

/**
 * このアプリケーションで使用しているオープンソースソフトウェアのライセンス情報を表示するための全画面ダイアログ。
 *
 * 以下の責務を持つ:
 * - `AboutLibraries`ライブラリを利用して、ライセンスの一覧をComposeで表示する
 * - ツールバーのナビゲーションアイコンでダイアログを閉じる
 */
class OpenSourceSoftwareLicensesDialogFragment
    : BaseSimpleFullScreenDialogFragment<DialogOpenSourceSoftwareLicensesBinding>() {

    /** 追加処理として、ツールバーとライセンス表示の初期設定を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolBar()
        setupAboutLibraries()
    }

    override fun createViewBinding(
        themeColorInflater: LayoutInflater,
        container: ViewGroup?
    ): DialogOpenSourceSoftwareLicensesBinding {
        return DialogOpenSourceSoftwareLicensesBinding.inflate(themeColorInflater, container, false)
    }

    /** ツールバーのナビゲーションアイコンクリック時の処理を設定する。 */
    private fun setupToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                dismissWithNavigateUp()
            }
    }

    /** `AboutLibraries`ライブラリを使用して、ライセンス一覧を表示するComposeViewを設定する。 */
    private fun setupAboutLibraries() {
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
