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
import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.WordSearchEvent
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

        setUpWordSearchView()
        setUpWordSearchResultList()
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

    private fun setUpWordSearchView() {
        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.searchWord
                .collectLatest { value: String ->
                    mainViewModel.onSearchWordChanged(value)
                }
        }
    }

    private fun setUpWordSearchResultList() {
        setUpListAdapter()

        binding.floatingActionButtonTopScroll.hide() // MEMO:初回起動用

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.wordSearchResultList
                .collectLatest { value: DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult> ->
                    val listAdapter =
                        binding.recyclerWordSearchResultList.adapter
                                as WordSearchResultYearMonthListAdapter
                    listAdapter.submitList(value.itemList) {
                        mainViewModel.onWordSearchResultListUpdateCompleted()
                    }
                }
        }
    }

    private fun setUpListAdapter() {
        val wordSearchResultListAdapter =
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
        binding.floatingActionButtonTopScroll.setOnClickListener {
            resultListScrollToFirstPosition()
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

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private fun resultListScrollToFirstPosition() {
        val adapter = binding.recyclerWordSearchResultList.adapter
        val listAdapter = adapter as WordSearchResultYearMonthListAdapter
        listAdapter.scrollToTop()
    }

    private fun navigateDiaryShowFragment(id: DiaryIdUi, date: LocalDate) {
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
