package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult.WordSearchResultYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@AndroidEntryPoint
class WordSearchFragment : BaseFragment<FragmentWordSearchBinding, WordSearchUiEvent>() {

    //region Properties
    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by viewModels()

    override val destinationId = R.id.navigation_word_search_fragment

    private lateinit var wordSearchResultListAdapter: WordSearchResultYearMonthListAdapter
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpWordSearchResultListAdapter()
        setUpFloatingActionButton()

        mainViewModel.onUiReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.onUiGone()
    }
    //endregion

    //region View Binding Setup
    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentWordSearchBinding {
        return FragmentWordSearchBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }
    //endregion

    //region Fragment Result Receiver Setup
    override fun setUpFragmentResultReceivers() {
        // 処理なし
    }
    //endregion

    //region UI Observation Setup
    override fun onMainUiEventReceived(event: WordSearchUiEvent) {
        when (event) {
            is WordSearchUiEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            WordSearchUiEvent.ShowKeyboard -> {
                showKeyboard()
            }
        }
    }

    override fun onNavigatePreviousFragmentEventReceived(result: FragmentResult<*>) {
        navigatePreviousFragmentOnce()
    }

    override fun onNavigateAppMessageEventReceived(appMessage: AppMessage) {
        navigateAppMessageDialog(appMessage)
    }

    override fun setUpUiStateObservers() {
        super.setUpUiStateObservers()

        observeWordSearchResultListItem()
    }

    private fun observeWordSearchResultListItem() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState.distinctUntilChanged { old, new ->
                old.wordSearchResultList == new.wordSearchResultList
            }.map {
                it.wordSearchResultList
            }.collect {
                updateWordSearchResultList(it)
            }
        }
    }
    //endregion

    //region View Setup
    private fun setUpWordSearchResultListAdapter() {
        wordSearchResultListAdapter =
            object : WordSearchResultYearMonthListAdapter(
                binding.recyclerWordSearchResultList,
                themeColor
            ) {
                override fun loadListOnScrollEnd() {
                    mainViewModel.onWordSearchResultListEndScrolled()
                }
            }

        wordSearchResultListAdapter.apply {
            build()
            registerOnChildItemClickListener { item: DiaryDayListItemUi.WordSearchResult ->
                mainViewModel.onWordSearchResultListItemClick(item)
            }
        }
    }

    private fun setUpFloatingActionButton() {
        binding.floatingActionButtonTopScroll.apply {
            hide() // MEMO:初回起動用
            setOnClickListener {
                wordSearchResultListAdapter.scrollToTop()
            }
        }
        binding.recyclerWordSearchResultList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (recyclerView.canScrollVertically(-1)) {
                    binding.floatingActionButtonTopScroll.show()
                } else {
                    binding.floatingActionButtonTopScroll.hide()
                }
            }
        })
    }
    //endregion

    //region View Manipulation
    private fun updateWordSearchResultList(
        list: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>
    ) {
        wordSearchResultListAdapter.submitList(list.itemList) {
            mainViewModel.onWordSearchResultListUpdateCompleted()
        }
    }

    private fun showKeyboard() {
        binding.textInputEditTextSearchWord.requestFocus()
        KeyboardManager().showKeyboard(binding.textInputEditTextSearchWord)
    }
    //endregion

    //region Navigation Helpers
    private fun navigateDiaryShowFragment(id: String, date: LocalDate) {
        val directions =
            WordSearchFragmentDirections
                .actionNavigationWordSearchFragmentToDiaryShowFragment(id, date)
        navigateFragmentOnce(NavigationCommand.To(directions))
    }

    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections.actionWordSearchFragmentToAppMessageDialog(appMessage)
        navigateFragmentWithRetry(NavigationCommand.To(directions))
    }
    //endregion
}
