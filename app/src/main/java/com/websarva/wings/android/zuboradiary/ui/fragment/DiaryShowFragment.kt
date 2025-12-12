package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.ConfirmationDialogArgs
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DiaryShowNavDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString
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
class DiaryShowFragment : BaseFragment<
        FragmentDiaryShowBinding,
        DiaryShowUiEvent,
        DiaryShowNavDestination,
        DummyNavBackDestination
>() {

    //region Properties
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryShowViewModel by viewModels()

    override val destinationId = R.id.navigation_diary_show_fragment

    override val resultKey: String get() = RESULT_KEY
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
        observeDialogResult<Unit>(
            RESULT_KEY_DIARY_LOAD_FAILURE
        ) { result ->
            when (result) {
                is DialogResult.Positive,
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryLoadFailureDialogResultReceived()
                }
            }
        }
    }

    /** 日記削除確認ダイアログからの結果を監視する。 */
    private fun observeDiaryDeleteDialogResult() {
        observeDialogResult<Unit>(
            RESULT_KEY_DIARY_DELETE_CONFIRMATION
        ) { result ->
            when (result) {
                is DialogResult.Positive -> {
                    mainViewModel.onDiaryDeleteDialogPositiveResultReceived()
                }
                DialogResult.Negative,
                DialogResult.Cancel -> {
                    mainViewModel.onDiaryDeleteDialogNegativeResultReceived()
                }
            }
        }
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: DiaryShowUiEvent) {
       // 処理なし
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
    override fun toNavDirections(destination: DiaryShowNavDestination): NavDirections {
        return when (destination) {
            is DiaryShowNavDestination.AppMessageDialog -> {
                navigationEventHelper.createAppMessageDialogNavDirections(destination.message)
            }
            is DiaryShowNavDestination.DiaryEditScreen -> {
                createDiaryEditFragmentNavDirections(destination.id, destination.date)
            }
            is DiaryShowNavDestination.DiaryLoadFailureDialog -> {
                createDiaryLoadFailureDialogNavDirections(destination.date)
            }
            is DiaryShowNavDestination.DiaryDeleteDialog -> {
                createDiaryDeleteDialogNavDirections(destination.date)
            }
        }
    }

    override fun toNavDestinationId(destination: DummyNavBackDestination): Int {
        // 処理なし
        throw IllegalStateException("NavDestinationIdへの変換は不要の為、未対応。")
    }

    /**
     * 日記編集画面へ遷移する為の [NavDirections] オブジェクトを生成する。
     * 
     * @param id 編集対象の日記ID
     * @param date 編集対象の日記の日付
     */
    private fun createDiaryEditFragmentNavDirections(id: String, date: LocalDate): NavDirections {
        return DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                id,
                date
            )
    }

    /**
     * 日記読み込み失敗ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。
     * 
     * @param date 読み込みに失敗した日記の日付
     */
    private fun createDiaryLoadFailureDialogNavDirections(date: LocalDate): NavDirections {
        val args = ConfirmationDialogArgs(
            resultKey = RESULT_KEY_DIARY_LOAD_FAILURE,
            titleRes = R.string.dialog_diary_load_failure_title,
            messageText = getString(
                R.string.dialog_diary_load_failure_message,
                date.formatDateString(requireContext())
            )
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(args)
    }

    /**
     * 日記削除確認ダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。
     * 
     * @param date 削除対象の日記の日付
     */
    private fun createDiaryDeleteDialogNavDirections(date: LocalDate): NavDirections {
        val args = ConfirmationDialogArgs(
            resultKey = RESULT_KEY_DIARY_DELETE_CONFIRMATION,
            titleRes = R.string.dialog_diary_delete_title,
            messageText = getString(
                R.string.dialog_diary_delete_message,
                date.formatDateString(requireContext())
            )
        )
        return MobileNavigationDirections.actionGlobalToConfirmationDialog(args)
    }
    //endregion

    internal companion object {
        /** このフラグメントから遷移元へ結果を返すためのキー。 */
        val RESULT_KEY = RESULT_KEY_PREFIX + DiaryShowFragment::class.java.name

        /** 日記読込失敗ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_DIARY_LOAD_FAILURE = "diary_load_failure_result"

        /** 日記削除の確認ダイアログの結果を受け取るためのキー。 */
        private const val RESULT_KEY_DIARY_DELETE_CONFIRMATION = "diary_delete_confirmation_result"
    }
}
