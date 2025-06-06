package com.websarva.wings.android.zuboradiary.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.Condition
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentDiaryShowBinding
import com.websarva.wings.android.zuboradiary.ui.view.imageview.DiaryPictureConfigurator
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryDeleteDialogFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.DiaryLoadingFailureDialogFragment
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryShowViewModel
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateString
import com.websarva.wings.android.zuboradiary.ui.utils.toJapaneseDateTimeWithSecondsString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
internal class DiaryShowFragment : BaseFragment<FragmentDiaryShowBinding>() {

    internal companion object {
        // Navigation関係
        val KEY_RESULT = RESULT_KEY_PREFIX + DiaryShowFragment::class.java.name
    }

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: DiaryShowViewModel by viewModels()

    override fun initializeDataBinding(
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
        setUpWeatherLayout()
        setUpConditionLayout()
        setUpItemLayout()
        setUpPicture()
        setUpLogLayout()

        val diaryDate = DiaryShowFragmentArgs.fromBundle(requireArguments()).date
        mainViewModel.onFragmentViewCreated(diaryDate)
    }

    override fun initializeFragmentResultReceiver() {
        setUpDiaryLoadingFailureDialogResultReceiver()
        setUpDiaryDeleteDialogResultReceiver()
    }

    // 日記読込失敗確認ダイアログフラグメントからデータ受取
    private fun setUpDiaryLoadingFailureDialogResultReceiver() {
        setUpDialogResultReceiver(
            DiaryLoadingFailureDialogFragment.KEY_RESULT
        ) { result ->
            mainViewModel.onDiaryLoadingFailureDialogResultReceived(result)
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

    override fun onMainViewModelEventReceived(event: ViewModelEvent) {
        when (event) {
            is DiaryShowEvent.NavigateDiaryEditFragment -> {
                navigateDiaryEditFragment(event.date)
            }
            is DiaryShowEvent.NavigateDiaryLoadingFailureDialog -> {
                navigateDiaryLoadingFailureDialog(event.date)
            }
            is DiaryShowEvent.NavigateDiaryDeleteDialog -> {
                navigateDiaryDeleteDialog(event.date)
            }
            is DiaryShowEvent.NavigatePreviousFragment -> {
                navigatePreviousFragment(event.result)
            }
            is DiaryShowEvent.NavigatePreviousFragmentOnDiaryDelete -> {
                navigatePreviousFragment(event.result)
            }
            is ViewModelEvent.NavigatePreviousFragment -> {
                // MEMO:"DiaryShowEvent.NavigatePreviousFragment"を使用する為、
                //      "ViewModelEvent.NavigatePreviousFragment"処理不要。
                throw IllegalArgumentException()
            }
            is ViewModelEvent.NavigateAppMessage -> {
                navigateAppMessageDialog(event.message)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar.apply {
            setOnMenuItemClickListener { item: MenuItem ->
                // 日記編集フラグメント起動
                if (item.itemId == R.id.diaryShowToolbarOptionEditDiary) {
                    mainViewModel.onDiaryEditMenuClicked()
                    return@setOnMenuItemClickListener true
                } else if (item.itemId == R.id.diaryShowToolbarOptionDeleteDiary) {
                    mainViewModel.onDiaryDeleteMenuClicked()
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.date
                .collectLatest { value: LocalDate? ->
                    // MEMO:DiaryViewModelを初期化するとDiaryDateにnullが代入されるため、下記"return"を処理。
                    if (value == null) return@collectLatest

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
                    Weather1Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeather1Selected
                    ).onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.weather2
                .collectLatest { value: Weather ->
                    Weather2Observer(
                        requireContext(),
                        binding.includeDiaryShow.textWeather2Selected
                    ).onChanged(value)
                }
        }
    }

    internal class Weather1Observer(
        private val context: Context,
        private val textWeather: TextView
    ) {
        fun onChanged(value: Weather) {
            textWeather.text = value.toString(context)
        }
    }

    internal class Weather2Observer(
        private val context: Context,
        private val textWeather: TextView
    ) {
        fun onChanged(value: Weather) {
            textWeather.text = value.toString(context)
        }
    }

    private fun setUpConditionLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.condition
                .collectLatest { value: Condition ->
                    ConditionObserver(
                        requireContext(),
                        binding.includeDiaryShow.textConditionSelected
                    ).onChanged(value)
                }
        }
    }

    internal class ConditionObserver(private val context: Context, private val textCondition: TextView) {

        fun onChanged(value: Condition) {
            textCondition.text = value.toString(context)
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
                    NumVisibleItemsObserver(itemLayouts).onChanged(value)
                }
        }
    }

    internal class NumVisibleItemsObserver(private val itemLayouts: Array<LinearLayout>) {

        fun onChanged(value: Int) {
            require(!(value < ItemNumber.MIN_NUMBER || value > ItemNumber.MAX_NUMBER))

            for (i in ItemNumber.MIN_NUMBER..ItemNumber.MAX_NUMBER) {
                val itemArrayNumber = i - 1
                if (i <= value) {
                    itemLayouts[itemArrayNumber].visibility = View.VISIBLE
                } else {
                    itemLayouts[itemArrayNumber].visibility = View.GONE
                }
            }
        }
    }

    private fun setUpPicture() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.picturePath
                .collectLatest { value: Uri? ->
                    PicturePathObserver(
                        themeColor,
                        binding.includeDiaryShow.imageAttachedPicture
                    ).onChanged(value)
                }
        }
    }

    internal class PicturePathObserver(
        private val themeColor: ThemeColor,
        private val imageView: ImageView
    ) {

        fun onChanged(value: Uri?) {
            if (value == null) return

            DiaryPictureConfigurator()
                .setUpPictureOnDiary(
                    imageView,
                    value,
                    themeColor
                )
        }
    }

    private fun setUpLogLayout() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.log
                .collectLatest { value: LocalDateTime? ->
                    LogObserver(requireContext(), binding.includeDiaryShow.textLogValue)
                        .onChanged(value)
                }
        }
    }

    internal class LogObserver(private val context: Context ,private val textLog: TextView) {

        fun onChanged(value: LocalDateTime?) {
            // MEMO:DiaryViewModelを初期化するとDiaryLogにnullが代入されるため、下記"return"を処理。
            if (value == null) return

            val dateString = value.toJapaneseDateTimeWithSecondsString(context)
            textLog.text = dateString
        }
    }

    private fun navigateDiaryEditFragment(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionNavigationDiaryShowFragmentToDiaryEditFragment(
                false,
                true,
                date
            )
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryLoadingFailureDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryLoadingFailureDialog(date)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigateDiaryDeleteDialog(date: LocalDate) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToDiaryDeleteDialog(date)
        navigateFragment(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            DiaryShowFragmentDirections.actionDiaryShowFragmentToAppMessageDialog(appMessage)
        navigateFragment(NavigationCommand.To(directions))
    }

    private fun navigatePreviousFragment(result: FragmentResult.Some<LocalDate>) {
        navigateFragment(NavigationCommand.Up(KEY_RESULT, result))
    }
}
