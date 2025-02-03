package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.EditTextSetup;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordSearchFragment extends BaseFragment {

    // View関係
    private FragmentWordSearchBinding binding;
    private String previousText = ""; // 二重検索防止用

    private int resultWordColor = -1; // 検索結果ワード色
    private int resultWordBackgroundColor = -1; // 検索結果ワードマーカー色

    // ViewModel
    private WordSearchViewModel wordSearchViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        wordSearchViewModel = provider.get(WordSearchViewModel.class);
        wordSearchViewModel.initialize();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container) {
        binding = FragmentWordSearchBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setWordSearchViewModel(wordSearchViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpThemeColor();
        setUpToolBar();
        setUpWordSearchView();
        setUpWordSearchResultList();
        setUpFloatingActionButton();
    }

    private void setUpThemeColor() {
        ThemeColor themeColor = requireThemeColor();
        resultWordColor = themeColor.getOnTertiaryContainerColor(getResources());
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(getResources());
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        retryOtherAppMessageDialogShow();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    @Override
    protected void setUpOtherAppMessageDialog() {
        wordSearchViewModel.getAppMessageBufferList()
                .observe(getViewLifecycleOwner(), new AppMessageBufferListObserver(wordSearchViewModel));
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(v -> {
                    Objects.requireNonNull(v);

                    navController.navigateUp();
                });
    }

    private void setUpWordSearchView() {
        String searchWord = wordSearchViewModel.getSearchWord().getValue();
        Objects.requireNonNull(searchWord);
        if (searchWord.isEmpty()) {
            binding.editTextSearchWord.requestFocus();
            KeyboardInitializer keyboardInitializer = new KeyboardInitializer(requireActivity());
            keyboardInitializer.show(binding.editTextSearchWord);
        }

        wordSearchViewModel.getSearchWord()
                .observe(getViewLifecycleOwner(), s -> {
                    Objects.requireNonNull(s);
                    // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                    //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                    if (s.equals(previousText)) return;

                    // 検索結果表示Viewは別Observerにて表示
                    if (s.isEmpty()) {
                        binding.textNoWordSearchResultsMessage.setVisibility(View.INVISIBLE);
                        binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
                        wordSearchViewModel.initialize();

                    } else  {
                        wordSearchViewModel
                                .loadNewWordSearchResultList(resultWordColor, resultWordBackgroundColor);
                    }
                    previousText = s;
                });

        EditTextSetup editTextSetup = new EditTextSetup(requireActivity());
        editTextSetup.setUpFocusClearOnClickBackground(binding.viewFullScreenBackground, binding.editTextSearchWord);
        editTextSetup.setUpKeyboardCloseOnEnter(binding.editTextSearchWord);
        editTextSetup.setUpClearButton(binding.editTextSearchWord, binding.imageButtonSearchWordClear);
    }

    private void setUpWordSearchResultList() {
        WordSearchResultListAdapter wordSearchResultListAdapter =
                new WordSearchResultListAdapter(
                        requireContext(),
                        binding.recyclerWordSearchResultList,
                        requireThemeColor()
                );
        wordSearchResultListAdapter.build();
        wordSearchResultListAdapter.setOnClickChildItemListener(item -> {
            Objects.requireNonNull(item);

            showShowDiaryFragment(item.getDate());
        });

        wordSearchViewModel.getWordSearchResultList()
                .observe(getViewLifecycleOwner(), new WordSearchResultListObserver());

        wordSearchViewModel.getNumWordSearchResults()
                .observe(getViewLifecycleOwner(), integer -> {
                    Objects.requireNonNull(integer);

                    int visibility;
                    if (integer > 0) {
                        visibility = View.VISIBLE;
                    } else {
                        visibility = View.INVISIBLE;
                    }
                    binding.textNumWordSearchResults.setVisibility(visibility);
                });

        binding.includeProgressIndicator.viewBackground.setOnTouchListener((v, event) -> {
            Objects.requireNonNull(v);
            Objects.requireNonNull(event);

            v.performClick();
            return true;
        });

        updateWordSearchResultList();
    }

    private class WordSearchResultListAdapter extends WordSearchResultYearMonthListAdapter {

        private WordSearchResultListAdapter(
                Context context, RecyclerView recyclerView, ThemeColor themeColor) {
            super(context, recyclerView, themeColor);
        }

        @Override
        public void loadListOnScrollEnd() {
            wordSearchViewModel
                    .loadAdditionWordSearchResultList(resultWordColor, resultWordBackgroundColor);
        }

        @Override
        public boolean canLoadList() {
            return wordSearchViewModel.canLoadWordSearchResultList();
        }
    }

    private class WordSearchResultListObserver implements Observer<WordSearchResultYearMonthList> {

        @Override
        public void onChanged(WordSearchResultYearMonthList wordSearchResultYearMonthList) {
            Objects.requireNonNull(wordSearchResultYearMonthList);

            WordSearchResultYearMonthListAdapter listAdapter =
                    (WordSearchResultYearMonthListAdapter)
                            binding.recyclerWordSearchResultList.getAdapter();
            Objects.requireNonNull(listAdapter);

            String searchWord = wordSearchViewModel.getSearchWord().getValue();
            Objects.requireNonNull(searchWord);
            if (searchWord.isEmpty()) {
                binding.floatingActionButtonTopScroll.hide(); // MEMO:初回起動用
                binding.textNoWordSearchResultsMessage.setVisibility(View.INVISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
            } else if (wordSearchResultYearMonthList.getWordSearchResultYearMonthListItemList().isEmpty()) {
                binding.textNoWordSearchResultsMessage.setVisibility(View.VISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
            } else {
                binding.textNoWordSearchResultsMessage.setVisibility(View.INVISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.VISIBLE);
            }

            List<DiaryYearMonthListBaseItem> convertedList =
                    new ArrayList<>(wordSearchResultYearMonthList.getWordSearchResultYearMonthListItemList());
            listAdapter.submitList(convertedList);
        }
    }

    private void updateWordSearchResultList() {
        WordSearchResultYearMonthList list =
                wordSearchViewModel.getWordSearchResultList().getValue();
        Objects.requireNonNull(list);

        if (list.getWordSearchResultYearMonthListItemList().isEmpty()) return;
        wordSearchViewModel
                .updateWordSearchResultList(resultWordColor, resultWordBackgroundColor);
    }

    private void setUpFloatingActionButton() {
        binding.floatingActionButtonTopScroll.setOnClickListener(v -> {
            Objects.requireNonNull(v);

            resultListScrollToFirstPosition();
        });
        binding.recyclerWordSearchResultList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (recyclerView.canScrollVertically(-1)) {
                    binding.floatingActionButtonTopScroll.show();
                } else {
                    binding.floatingActionButtonTopScroll.hide();
                }
            }
        });
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private void resultListScrollToFirstPosition() {
        RecyclerView.Adapter<?> adapter = binding.recyclerWordSearchResultList.getAdapter();
        Objects.requireNonNull(adapter);

        WordSearchResultYearMonthListAdapter listAdapter =
                (WordSearchResultYearMonthListAdapter) adapter;
        listAdapter.scrollToFirstPosition();
    }

    private void showShowDiaryFragment(LocalDate date) {
        Objects.requireNonNull(date);
        if (isDialogShowing()) return;

        NavDirections action =
                WordSearchFragmentDirections
                        .actionNavigationWordSearchFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    @Override
    protected void navigateAppMessageDialog(@NonNull AppMessage appMessage) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionWordSearchFragmentToAppMessageDialog(appMessage);
        navController.navigate(action);
    }

    @Override
    protected void retryOtherAppMessageDialogShow() {
        wordSearchViewModel.triggerAppMessageBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
