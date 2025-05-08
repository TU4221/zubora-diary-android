package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter.OnClickChildItemListener
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthList
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult.WordSearchResultYearMonthListAdapter
import com.websarva.wings.android.zuboradiary.ui.viewmodel.WordSearchViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

class WordSearchFragment : BaseFragment() {
    // View関係
    private var _binding: FragmentWordSearchBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var resultWordColor = -1 // 検索結果ワード色
    private var resultWordBackgroundColor = -1 // 検索結果ワードマーカー色

    // ViewModel
    // MEMO:委譲プロパティの委譲先(viewModels())の遅延初期化により"Field is never assigned."と警告が表示される。
    //      委譲プロパティによるViewModel生成は公式が推奨する方法の為、警告を無視する。その為、@Suppressを付与する。
    //      この警告に対応するSuppressネームはなく、"unused"のみでは不要Suppressとなる為、"RedundantSuppression"も追記する。
    @Suppress("unused", "RedundantSuppression")
    override val mainViewModel: WordSearchViewModel by activityViewModels()

    // RecyclerView関係
    // HACK:RecyclerViewのAdapterにセットするListを全て変更した時、
    //      変更前のListの内容で初期スクロール位置が定まらない不具合が発生。
    //      対策としてListを全て変更するタイミングでAdapterを新規でセットする。
    //      (親子関係でRecyclerViewを使用、又はListAdapterの機能による弊害？)
    private var shouldInitializeListAdapter = false

    override fun initializeDataBinding(
        themeColorInflater: LayoutInflater, container: ViewGroup
    ): ViewDataBinding {
        _binding = FragmentWordSearchBinding.inflate(themeColorInflater, container, false)

        return binding.apply {
            lifecycleOwner = this@WordSearchFragment.viewLifecycleOwner
            wordSearchViewModel = mainViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModelInitialization()
        setUpThemeColor()
        setUpToolBar()
        setUpWordSearchView()
        setUpWordSearchResultList()
        setUpFloatingActionButton()
    }

    private fun setUpThemeColor() {
        resultWordColor = themeColor.getOnTertiaryContainerColor(resources)
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(resources)
    }

    override fun handleOnReceivingResultFromPreviousFragment() {
        // 処理なし
    }

    override fun receiveDialogResults() {
        // 処理なし
    }

    override fun removeDialogResults() {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    private fun setUpViewModelInitialization() {
        navController.addOnDestinationChangedListener(ViewModelInitializationSetupListener())
    }

    private fun setUpToolBar() {
        binding.materialToolbarTopAppBar
            .setNavigationOnClickListener {
                navController.navigateUp()
            }
    }

    private fun setUpWordSearchView() {
        val searchWord = mainViewModel.searchWord.value
        if (searchWord.isEmpty()) {
            binding.editTextSearchWord.requestFocus()
            KeyboardManager().showKeyboard(binding.editTextSearchWord)
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.searchWord
                .collectLatest { value: String ->
                    // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                    //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                    if (!mainViewModel.shouldLoadWordSearchResultList) {
                        if (mainViewModel.shouldUpdateWordSearchResultList) {
                            updateWordSearchResultList()
                        }
                        return@collectLatest
                    }

                    // 検索結果表示Viewは別Observerにて表示
                    if (value.isEmpty()) {
                        binding.textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                        binding.linerLayoutWordSearchResults.visibility = View.INVISIBLE
                        mainViewModel.initialize()
                    } else {
                        shouldInitializeListAdapter = true
                        mainViewModel
                            .loadNewWordSearchResultList(resultWordColor, resultWordBackgroundColor)
                    }
                    mainViewModel.previousSearchWord = value
                }
        }


        val editTextConfigurator = EditTextConfigurator()
        editTextConfigurator.setUpFocusClearOnClickBackground(
            binding.viewFullScreenBackground,
            binding.editTextSearchWord
        )
        editTextConfigurator.setUpKeyboardCloseOnEnter(binding.editTextSearchWord)
        editTextConfigurator.setUpClearButton(
            binding.editTextSearchWord,
            binding.imageButtonSearchWordClear
        )
    }

    private fun setUpWordSearchResultList() {
        setUpListAdapter()

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.wordSearchResultList
                .collectLatest { value: WordSearchResultYearMonthList ->
                    WordSearchResultListObserver().onChanged(value)
                }
        }

        launchAndRepeatOnViewLifeCycleStarted {
            mainViewModel.numWordSearchResults
                .collectLatest { value: Int ->
                    val visibility = if (value > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    binding.textNumWordSearchResults.visibility = visibility
                }
        }

        navController.addOnDestinationChangedListener(WordSearchResultListUpdateSetupListener())
    }

    private fun setUpListAdapter() {
        val wordSearchResultListAdapter =
            WordSearchResultListAdapter(
                binding.recyclerWordSearchResultList,
                themeColor
            )
        wordSearchResultListAdapter.apply {
            build()
            onClickChildItemListener =
                OnClickChildItemListener { item: DiaryDayListBaseItem ->
                    navigateDiaryShowFragment(item.date)
                }
        }
    }

    private inner class WordSearchResultListAdapter(
        recyclerView: RecyclerView,
        themeColor: ThemeColor
    ) :
        WordSearchResultYearMonthListAdapter(recyclerView, themeColor) {
        override fun loadListOnScrollEnd() {
            mainViewModel
                .loadAdditionWordSearchResultList(resultWordColor, resultWordBackgroundColor)
        }

        override fun canLoadList(): Boolean {
            return mainViewModel.canLoadWordSearchResultList
        }
    }

    private inner class WordSearchResultListObserver :
        Observer<WordSearchResultYearMonthList> {
        override fun onChanged(value: WordSearchResultYearMonthList) {
            val searchWord = mainViewModel.searchWord.value
            if (searchWord.isEmpty()) {
                binding.apply {
                    floatingActionButtonTopScroll.hide() // MEMO:初回起動用
                    textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                    linerLayoutWordSearchResults.visibility = View.INVISIBLE
                }
            } else if (value.isEmpty) {
                binding.apply {
                    textNoWordSearchResultsMessage.visibility = View.VISIBLE
                    linerLayoutWordSearchResults.visibility = View.INVISIBLE
                }
            } else {
                binding.apply {
                    textNoWordSearchResultsMessage.visibility = View.INVISIBLE
                    linerLayoutWordSearchResults.visibility = View.VISIBLE
                }
            }

            if (shouldInitializeListAdapter) {
                shouldInitializeListAdapter = false
                setUpListAdapter()
            }

            val listAdapter =
                binding.recyclerWordSearchResultList.adapter
                        as WordSearchResultYearMonthListAdapter
            val convertedList: List<DiaryYearMonthListBaseItem> =
                ArrayList<DiaryYearMonthListBaseItem>(value.itemList)
            listAdapter.submitList(convertedList)
        }
    }

    private fun updateWordSearchResultList() {
        val list = mainViewModel.wordSearchResultList.value
        if (list.isEmpty) return
        mainViewModel.updateWordSearchResultList(resultWordColor, resultWordBackgroundColor)
    }

    private inner class WordSearchResultListUpdateSetupListener
        : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                if (destination.id == R.id.navigation_word_search_fragment) return

                if (destination.id == R.id.navigation_diary_show_fragment) {
                    mainViewModel.shouldUpdateWordSearchResultList = true
                }

                navController.removeOnDestinationChangedListener(this)
            }
    }

    private inner class ViewModelInitializationSetupListener
        : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            if (destination.id == R.id.navigation_word_search_fragment) return

            if (destination.id != R.id.navigation_diary_show_fragment) {
                mainViewModel.shouldInitializeOnFragmentDestroy = true
            }

            navController.removeOnDestinationChangedListener(this)
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
        listAdapter.scrollToFirstPosition()
    }

    @MainThread
    private fun navigateDiaryShowFragment(date: LocalDate) {
        if (!canNavigateFragment) return

        val directions =
            WordSearchFragmentDirections.actionNavigationWordSearchFragmentToDiaryShowFragment(date)
        navController.navigate(directions)
    }

    @MainThread
    override fun navigateAppMessageDialog(appMessage: AppMessage) {
        val directions =
            WordSearchFragmentDirections.actionWordSearchFragmentToAppMessageDialog(appMessage)
        navController.navigate(directions)
    }

    override fun destroyBinding() {
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // MEMO:WordSearchViewModelのスコープ範囲はActivityになるが、
        //      WordSearchFragment、DiaryShowFragment、DiaryEditFragment、
        //      DiaryItemTitleEditFragment表示時のみ ViewModelのプロパティ値を保持できたらよいので、
        //      WordSearchFragmentを破棄するタイミングでViewModelのプロパティ値を初期化する。
        mainViewModel.apply {
            if (shouldInitializeOnFragmentDestroy) initialize()
        }
    }
}
