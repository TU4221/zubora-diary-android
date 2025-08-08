package com.websarva.wings.android.zuboradiary.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryConditionTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryImageUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryItemsVisibilityUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryLogTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.common.DiaryWeatherTextUpdater
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryShowViewModel
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import java.time.LocalDate
import java.time.LocalDateTime

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
                baseDiaryShowViewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolBar()
        setUpWeatherLayout()
        setUpConditionLayout()
        setUpItemLayout()
        setUpImage()
        setUpLogLayout()
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
                navigateDiaryEditFragment(event.date)
            }
            is DiaryShowEvent.NavigateDiaryLoadFailureDialog -> {
                navigateDiaryLoadFailureDialog(event.date)
            }
            is DiaryShowEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.parameters)
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

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.date.filterNotNull()
                .collectLatest { value: LocalDate ->
                    val dateString = value.toJapaneseDateString(requireContext())
                    binding.materialToolbarTopAppBar.title = dateString
                }
        }
    }

    // 天気表示欄設定
    private fun setUpWeatherLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather1
                .collectLatest { value: Weather ->
                    DiaryWeatherTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textWeather1Selected,
                            value
                        )
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: Weather ->
                    DiaryWeatherTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textWeather2Selected,
                            value
                        )
                }
        }
    }

    private fun setUpConditionLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.condition
                .collectLatest { value: Condition ->
                    DiaryConditionTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textConditionSelected,
                            value
                        )
                }
        }
    }



    private fun setUpItemLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            val itemLayouts =
                binding.includeDiaryShow.run {
                    arrayOf(
                        includeItem1.linerLayoutDiaryShowItem,
                        includeItem2.linerLayoutDiaryShowItem,
                        includeItem3.linerLayoutDiaryShowItem,
                        includeItem4.linerLayoutDiaryShowItem,
                        includeItem5.linerLayoutDiaryShowItem
                    )
                }

            mainViewModel.numVisibleItems
                .collectLatest { value: Int ->
                    DiaryItemsVisibilityUpdater()
                        .update(
                            itemLayouts,
                            value
                        )
                }
        }
    }

    private fun setUpImage() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.imageUri
                .collectLatest { value: Uri? ->
                    // MEMO:添付画像がないときはnullとなり、デフォルト画像をセットする。
                    //      nullの時ImageView自体は非表示となるためデフォルト画像をセットする意味はないが、
                    //      クリアという意味合いでデフォルト画像をセットする。
                    DiaryImageUpdater()
                        .update(
                            themeColor,
                            binding.includeDiaryShow.imageAttachedImage,
                            value
                        )
                }
        }
    }



    private fun setUpLogLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.log.filterNotNull()
                .collectLatest { value: LocalDateTime ->
                    DiaryLogTextUpdater()
                        .update(
                            requireContext(),
                            binding.includeDiaryShow.textLogValue,
                            value
                        )
                }
        }
    }

    private fun navigateDiaryEditFragment(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                true,
                date
            )
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadFailureDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryLoadFailureDialog(date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(parameters: DiaryDeleteParameters) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(parameters)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
}
