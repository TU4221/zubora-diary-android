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
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryShowViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DiaryShowFragment : BaseFragment<FragmentDiaryShowBinding, DiaryShowEvent>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryShowFragment::class.java.name
    }

    override val destinationId = R.id.navigation_diary_show_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryShowViewModel by viewModels()

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentDiaryShowBinding {
        return FragmentDiaryShowBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryLoadFailureDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
    }

    // 日記読込失敗確認ダイアログフラグメントからデータ受取
    private fun setUpDiaryLoadFailureDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadFailureDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadFailureDialogResultReceived(result)
        }
    }

    // 日記削除確認ダイアログフラグメントからデータ受取
    private fun setUpDiaryDeleteDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryDeleteDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryDeleteDialogResultReceived(result)
        }
    }

    override fun onMainUiEventReceived(event: DiaryShowEvent) {
        when (event) {
            is DiaryShowEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.id, event.date)
            }
            is DiaryShowEvent.NavigateDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
            }
            is DiaryShowEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
            is DiaryShowEvent.NavigatePreviousFragmentOnDiaryDeleted -> {
                navigatePreviousFragmentWithRetry(KEY_RESULT, event.result)
            }
            is DiaryShowEvent.NavigatePreviousFragmentOnDiaryLoadFailed -> {
                navigatePreviousFragmentWithRetry(KEY_RESULT, event.result)
            }

            is DiaryShowEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        navigatePreviousFragmentOnce(KEY_RESULT, event.wrappedEvent.result)
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            setOnMenuItemClickListener { item: MenuItem ->
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
    }

    private fun navigateDiaryEditFragment(id: String, date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                id,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
}
