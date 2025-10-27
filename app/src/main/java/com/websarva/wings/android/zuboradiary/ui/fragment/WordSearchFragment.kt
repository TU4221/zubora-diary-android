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
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@AndroidEntryPoint
class WordSearchFragment : BaseFragment<FragmentWordSearchBinding, WordSearchEvent>() {

    override val destinationId = R.id.navigation_word_search_fragment

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by viewModels()

    private lateinit var wordSearchResultListAdapter: WordSearchResultYearMonthListAdapter

    override fun createViewBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): FragmentWordSearchBinding {
        return FragmentWordSearchBinding.inflate(themeColorInflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = mainViewModel
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()
        setUpWordSearchResultListAdapter()
        setUpFloatingActionButton()

        mainViewModel.onUiReady()
    }

    override fun initializeFragmentResultReceiver() {
        // 処理なし
    }

    override fun onMainUiEventReceived(event: WordSearchEvent) {
        when (event) {
            is WordSearchEvent.NavigateDiaryShowFragment -> {
                navigateDiaryShowFragment(event.id, event.date)
            }
            WordSearchEvent.ShowKeyboard -> {
                showKeyboard()
            }
            is WordSearchEvent.CommonEvent -> {
                when(event.wrappedEvent) {
                    is CommonUiEvent.NavigatePreviousFragment<*> -> {
                        navigatePreviousFragmentOnce()
                    }
                    is CommonUiEvent.NavigateAppMessage -> {
                        navigateAppMessageDialog(event.wrappedEvent.message)
                    }
                }
            }
        }
    }

    private fun observeUiState() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.uiState
                .map { it.wordSearchResultList }.distinctUntilChanged().collect {
                    updateWordSearchResultList(it)
                }
        }
    }

    private fun updateWordSearchResultList(
        list: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>
    ) {
        wordSearchResultListAdapter.submitList(list.itemList) {
            mainViewModel.onWordSearchResultListUpdateCompleted()
        }
    }

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

    private fun showKeyboard() {
        binding.textInputEditTextSearchWord.requestFocus()
        KeyboardManager().showKeyboard(binding.textInputEditTextSearchWord)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.onUiGone()
    }
}
