package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import static com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.*;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.EditTextSetup;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;
import com.websarva.wings.android.zuboradiary.ui.KeyboardInitializer;
import com.websarva.wings.android.zuboradiary.databinding.FragmentWordSearchBinding;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

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
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        wordSearchViewModel = provider.get(WordSearchViewModel.class);
        wordSearchViewModel.initialize();
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
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
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        resultWordColor = themeColor.getOnTertiaryContainerColor(getResources());
        resultWordBackgroundColor = themeColor.getTertiaryContainerColor(getResources());
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        retryErrorDialogShow();
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        // LifecycleEventObserverにダイアログからの結果受取処理コードを記述したら、ここに削除処理を記述する。
    }

    @Override
    protected void setUpErrorMessageDialog() {
        wordSearchViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(wordSearchViewModel));
    }

    private void setUpToolBar() {
        binding.materialToolbarTopAppBar
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Objects.requireNonNull(v);

                        navController.navigateUp();
                    }
                });
    }

    private void setUpWordSearchView() {
        String searchWord = wordSearchViewModel.getSearchWordMutableLiveData().getValue();
        Objects.requireNonNull(searchWord);
        if (searchWord.isEmpty()) {
            binding.editTextKeyWordSearch.requestFocus();
            KeyboardInitializer keyboardInitializer = new KeyboardInitializer(requireActivity());
            keyboardInitializer.show(binding.editTextKeyWordSearch);
        }

        wordSearchViewModel.getSearchWordMutableLiveData()
                .observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        Objects.requireNonNull(s);
                        // HACK:キーワードの入力時と確定時に検索Observerが起動してしまい
                        //      同じキーワードで二重に検索してしまう。防止策として下記条件追加。
                        if (s.equals(previousText)) return;

                        // 検索結果表示Viewは別Observerにて表示
                        if (s.isEmpty()) {
                            binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                            binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
                            wordSearchViewModel.initialize();

                        } else  {
                            wordSearchViewModel
                                    .loadNewWordSearchResultList(resultWordColor, resultWordBackgroundColor);
                        }
                        previousText = s;
                    }
                });

        EditTextSetup editTextSetup = new EditTextSetup(requireActivity());
        editTextSetup.setUpFocusClearOnClickBackground(binding.viewFullScreenBackground, binding.editTextKeyWordSearch);
        editTextSetup.setUpKeyboardCloseOnEnter(binding.editTextKeyWordSearch);
        editTextSetup.setUpClearButton(binding.editTextKeyWordSearch, binding.imageButtonKeyWordClear);
    }

    private void setUpWordSearchResultList() {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        WordSearchResultListAdapter wordSearchResultListAdapter =
                new WordSearchResultListAdapter(
                        requireContext(),
                        binding.recyclerWordSearchResultList,
                        themeColor,
                        false
                );
        wordSearchResultListAdapter.build();
        wordSearchResultListAdapter.setOnClickChildItemListener(new OnClickChildItemListener() {
            @Override
            public void onClick(LocalDate date) {
                Objects.requireNonNull(date);

                showShowDiaryFragment(date);
            }
        });

        wordSearchViewModel.getWordSearchResultListLiveData()
                .observe(getViewLifecycleOwner(), new WordSearchResultListObserver());

        wordSearchViewModel.getNumWordSearchResultsLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        Objects.requireNonNull(integer);

                        int visibility;
                        if (integer > 0) {
                            visibility = View.VISIBLE;
                        } else {
                            visibility = View.INVISIBLE;
                        }
                        binding.textWordSearchResults.setVisibility(visibility);
                    }
                });

        binding.includeProgressIndicator.viewBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Objects.requireNonNull(v);
                Objects.requireNonNull(event);

                v.performClick();
                return true;
            }
        });

        updateWordSearchResultList();
    }

    private class WordSearchResultListAdapter extends DiaryYearMonthListAdapter {

        private WordSearchResultListAdapter(
                Context context, RecyclerView recyclerView, ThemeColor themeColor, boolean canSwipeItem) {
            super(context, recyclerView, themeColor, canSwipeItem);
        }

        @Override
        public void loadListOnScrollEnd() {
            ThemeColor themeColor = settingsViewModel.getThemeColorSettingValueLiveData().getValue();
            Objects.requireNonNull(themeColor);

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

            DiaryYearMonthListAdapter wordSearchResultYearMonthListAdapter =
                    (DiaryYearMonthListAdapter)
                            binding.recyclerWordSearchResultList.getAdapter();
            Objects.requireNonNull(wordSearchResultYearMonthListAdapter);

            String searchWord = wordSearchViewModel.getSearchWordLiveData().getValue();
            Objects.requireNonNull(searchWord);
            if (searchWord.isEmpty()) {
                binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
            } else if (wordSearchResultYearMonthList.getWordSearchResultYearMonthListItemList().isEmpty()) {
                binding.textWordSearchNoResults.setVisibility(View.VISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.INVISIBLE);
            } else {
                binding.textWordSearchNoResults.setVisibility(View.INVISIBLE);
                binding.linerLayoutWordSearchResults.setVisibility(View.VISIBLE);
            }

            List<DiaryYearMonthListItemBase> convertedList =
                    new ArrayList<>(wordSearchResultYearMonthList.getWordSearchResultYearMonthListItemList());
            Log.d("20241017", "convertedList.size():" + convertedList.size());
            wordSearchResultYearMonthListAdapter.submitList(convertedList);
        }
    }

    private void updateWordSearchResultList() {
        WordSearchResultYearMonthList list =
                wordSearchViewModel.getWordSearchResultListLiveData().getValue();
        Objects.requireNonNull(list);

        if (!list.getWordSearchResultYearMonthListItemList().isEmpty()) {
            wordSearchViewModel
                    .updateWordSearchResultList(resultWordColor, resultWordBackgroundColor);
        }
    }

    private void setUpFloatingActionButton() {
        binding.fabTopScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                resultListScrollToFirstPosition();
            }
        });
    }

    //日記リスト(年月)を自動でトップへスクロールさせるメソッド。
    private void resultListScrollToFirstPosition() {
        RecyclerView.Adapter<?> adapter = binding.recyclerWordSearchResultList.getAdapter();
        Objects.requireNonNull(adapter);

        DiaryYearMonthListAdapter diaryYearMonthListAdapter = (DiaryYearMonthListAdapter) adapter;
        diaryYearMonthListAdapter.scrollToFirstPosition();
    }

    private void showShowDiaryFragment(LocalDate date) {
        Objects.requireNonNull(date);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                WordSearchFragmentDirections
                        .actionNavigationWordSearchFragmentToDiaryShowFragment(date);
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                WordSearchFragmentDirections
                        .actionWordSearchFragmentToMessageDialog(
                                title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        wordSearchViewModel.triggerAppErrorBufferListObserver();
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
