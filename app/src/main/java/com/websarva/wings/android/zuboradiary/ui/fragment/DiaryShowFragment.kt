package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.DiaryLoadFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryShowViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

/**
 * 日記の詳細情報を表示するフラグメント。
 *
 * 以下の責務を持つ:
 * - 遷移元から渡されたIDに基づいて日記をデータベースから読み込む
 * - 読み込んだ日記の内容を画面に表示する
 * - ツールバーのメニュー（編集、削除）に応じた処理を実行する
 * - 日記の読み込みに失敗した場合のエラー処理(一つ前の画面へ戻る)を行う
 */
@AndroidEntryPoint
class DiaryShowFragment : BaseFragment<FragmentDiaryShowBinding, DiaryShowUiEvent>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryShowViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_show_fragment
    //endregion

    //region Fragment Lifecycle
    /** 追加処理として、ツールバーの初期設定を行う。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryShowBinding {
        return FragmentDiaryShowBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    /** 追加処理として、ツールバーのリスナー解放を行う。 */
    override fun clearViewBindings() {
        binding.materialToolbarTopAppBar.setOnMenuItemClickListener(null)

        super.clearViewBindings()
    }
    //endregion

    //region Fragment Result Observation Setup
    override fun setupFragmentResultObservers() {
        observeDiaryLoadFailureDialogResult()
        observeDiaryDeleteDialogResult()
    }

    /** 日記読み込み失敗ダイアログからの結果を監視する。 */
    private fun observeDiaryLoadFailureDialogResult() {
        observeDialogResult(
            DiaryLoadFailureDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryLoadFailureDialogResultReceived(result)
        }
    }

    /** 日記削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult(
            DiaryDeleteDialogFragment.RESULT_KEY
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryShowUiEvent) {
        when (event) {
            is DiaryShowUiEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.id, event.date)
            }
            is DiaryShowUiEvent.NavigateDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
            }
            is DiaryShowUiEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
            is DiaryShowUiEvent.NavigatePreviousFragmentOnDiaryDeleted -> {
                navigatePreviousFragmentWithRetry(RESULT_KEY, event.result)
            }
            is DiaryShowUiEvent.NavigatePreviousFragmentOnDiaryLoadFailed -> {
                navigatePreviousFragmentWithRetry(RESULT_KEY, event.result)
            }
        }
    }
    //endregion

    //region CommonUiEventHandler Overrides
    override fun navigatePreviousFragment(result: FragmentResult<*>) {
        navigatePreviousFragmentOnce(RESULT_KEY, result)
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion

    //region View Setup
    /** ツールバーのメニューアイテムクリックリスナーを設定する。 */
    private fun setupToolbar() {
        binding.materialToolbarTopAppBar.setOnMenuItemClickListener { item: MenuItem ->
            // 日記編集フラグメント起動
            if (item.itemId == R.id.diaryShowToolbarOptionEditDiary) {
                mainViewModel.onDiaryEditMenuClick()
                return@setOnMenuItemClickListener true
            } else if (item.itemId == R.id.diaryShowToolbarOptionDeleteDiary) {
                mainViewModel.onDiaryDeleteMenuClick()
                return@setOnMenuItemClickListener true
            }
            false
        }
    }
    //endregion

    //region Navigation Helpers
    /**
     * 日記編集画面([DiaryEditFragment])へ遷移する。
     * @param id 編集対象の日記ID
     * @param date 編集対象の日記の日付
     */
    private fun navigateDiaryEditFragment(id: String, date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                id,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日記読み込み失敗ダイアログ([DiaryLoadFailureDialogFragment])へ遷移する。
     * @param date 読み込みに失敗した日記の日付
     */
    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    /**
     * 日記削除確認ダイアログ([DiaryDeleteDialogFragment])へ遷移する。
     * @param date 削除対象の日記の日付
     */
    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }
    //endregion

    internal companion object {
        /** このフラグメントから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryShowFragment::class.java.name
    }
}
