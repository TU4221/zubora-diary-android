package com.websarva.wings.android.zuboradiary.ui;

import android.os.Bundle;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.AppMessageList;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

import java.util.Objects;

import dagger.internal.Preconditions;


public abstract class BaseFragment extends CustomFragment {

    protected SettingsViewModel settingsViewModel;
    protected NavController navController;
    protected int destinationId;

    @NonNull
    protected final MainActivity requireMainActivity() {
        return Objects.requireNonNull((MainActivity) requireActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViewModel();

        settingsViewModel = createSettingsViewModel();
        navController = NavHostFragment.findNavController(this);
        destinationId = getCurrentDestinationId();
    }

    /**
     * BaseFragment#onCreate()で呼び出される。
     * */
    protected abstract void initializeViewModel();

    @NonNull
    private SettingsViewModel createSettingsViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        SettingsViewModel settingsViewModel = provider.get(SettingsViewModel.class);
        return Objects.requireNonNull(settingsViewModel);
    }

    private int getCurrentDestinationId() {
        NavDestination navDestination = navController.getCurrentDestination();
        Objects.requireNonNull(navDestination);

        return navDestination.getId();
    }

    /**
     * 戻るボタン押下時の処理。
     * */
    protected final void addOnBackPressedCallback(OnBackPressedCallback callback) {
        Objects.requireNonNull(callback);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(container);

        super.onCreateView(inflater, container, savedInstanceState);

        setUpFragmentTransitionEffect();

        LayoutInflater themeColorInflater = createThemeColorInflater(inflater);
        ViewDataBinding dataBinding = initializeDataBinding(themeColorInflater, container);
        Objects.requireNonNull(dataBinding);
        return dataBinding.getRoot();
    }

    /**
     * BaseFragment#onCreateView()で呼び出される。
     * */
    protected abstract ViewDataBinding initializeDataBinding(
            @NonNull LayoutInflater themeColorInflater, @NonNull ViewGroup container);

    // ThemeColorに合わせたインフレーター作成
    @NonNull
    private LayoutInflater createThemeColorInflater(LayoutInflater inflater) {
        Preconditions.checkNotNull(inflater);

        ThemeColorInflaterCreator creator =
                new ThemeColorInflaterCreator(requireContext(), inflater, requireThemeColor());
        LayoutInflater themeColorInflater = creator.create();
        return Objects.requireNonNull(themeColorInflater);
    }

    @NonNull
    protected final ThemeColor requireThemeColor() {
        return settingsViewModel.loadThemeColorSettingValue();
    }

    private void setUpFragmentTransitionEffect() {
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション

        // HACK:ボトムナビゲーションタブでFragment切替時はEnterTransitionで設定されるエフェクトを変更する。
        //      NavigationStartFragment(DiaryListFragment)はReenterTransitionで設定されたエフェクトが処理される。
        //      遷移元FragmentのエフェクトはMainActivityクラスにて設定。
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getWasSelectedTab()) {
            setEnterTransition(new MaterialFadeThrough());
        } else {
            setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        }

        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));

        // TO - FROM の FROM として現れるアニメーション
        if (mainActivity.getWasSelectedTab()) {
            setReenterTransition(new MaterialFadeThrough());
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }

        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        mainActivity.clearWasSelectedTab();
    }

    protected final void addTransitionListener(Transition.TransitionListener listener) {
        Objects.requireNonNull(listener);

        MaterialSharedAxis enterTransition = (MaterialSharedAxis) getEnterTransition();
        Objects.requireNonNull(enterTransition);
        enterTransition.addListener(listener);

        MaterialSharedAxis exitTransition = (MaterialSharedAxis) getExitTransition();
        Objects.requireNonNull(exitTransition);
        exitTransition.addListener(listener);

        MaterialSharedAxis reenterTransition = (MaterialSharedAxis) getReenterTransition();
        Objects.requireNonNull(reenterTransition);
        reenterTransition.addListener(listener);

        MaterialSharedAxis returnTransition = (MaterialSharedAxis) getReturnTransition();
        Objects.requireNonNull(returnTransition);
        returnTransition.addListener(listener);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpPreviousFragmentResultReceiver();
        setUpDialogResultReceiver();
        setUpSettingsAppMessageDialog();
        setUpOtherAppMessageDialog();
    }

    @NonNull
    private SavedStateHandle getNavBackStackEntrySavedStateHandle() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);

        return navBackStackEntry.getSavedStateHandle();
    }

    private void setUpPreviousFragmentResultReceiver() {
        SavedStateHandle savedStateHandle = getNavBackStackEntrySavedStateHandle();
        Objects.requireNonNull(savedStateHandle);

        handleOnReceivingResultFromPreviousFragment(savedStateHandle);
    }


    /**
     * BaseFragment#setUpPreviousFragmentResultReceiver()で呼び出される。
     * */
    protected abstract void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle);

    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        Objects.requireNonNull(navBackStackEntry);

        LifecycleEventObserver lifecycleEventObserver = (lifecycleOwner, event) -> {
            // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
            //      Dialog非表示中:Lifecycle.Event.ON_RESUME
            if (event.equals(Lifecycle.Event.ON_RESUME)) {
                SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                handleOnReceivingDialogResult(savedStateHandle);
                retrySettingsAppMessageDialogShow();
                retryOtherAppMessageDialogShow();
                removeDialogResultOnDestroy(savedStateHandle);
            }
        };

        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle()
                .addObserver((LifecycleEventObserver) (source, event) -> {
                    if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                        // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                        //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                        removeDialogResultOnDestroy(savedStateHandle);

                        // MEMO:removeで削除しないと再度Fragment(前回表示Fragmentと同インスタンスの場合)を表示した時、Observerが重複する。
                        navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                    }
                });
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     * */
    protected abstract void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle);

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     * */
    protected abstract void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle);

    @Nullable
    public <T> T receiveResulFromDialog(String key) {
        Objects.requireNonNull(key);

        SavedStateHandle savedStateHandle = getNavBackStackEntrySavedStateHandle();
        boolean containsDialogResult = savedStateHandle.contains(key);
        if (!containsDialogResult) return null;

        return savedStateHandle.get(key);
    }

    private void setUpSettingsAppMessageDialog() {
        settingsViewModel.getAppMessageBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppMessageBufferListObserver(settingsViewModel));
    }

    /**
     * BaseFragment#setUpDialogResultReceiver()で呼び出される。
     * BaseViewModelのAppMessageBufferListのObserverを設定する。
     * */
    protected abstract void setUpOtherAppMessageDialog();

    protected final class AppMessageBufferListObserver implements Observer<AppMessageList> {

        private final BaseViewModel baseViewModel;

        public AppMessageBufferListObserver(BaseViewModel baseViewModel) {
            Objects.requireNonNull(baseViewModel);

            this.baseViewModel = baseViewModel;
        }

        @Override
        public void onChanged(AppMessageList appMessageList) {
            Objects.requireNonNull(appMessageList);
            if (appMessageList.isEmpty()) return;

            AppMessage firstAppMessage = appMessageList.findFirstItem();
            showAppMessageDialog(firstAppMessage);
            baseViewModel.removeAppMessageBufferListFirstItem();
        }
    }

    private void showAppMessageDialog(AppMessage appMessage) {
        Objects.requireNonNull(appMessage);
        if (isDialogShowing()) return;

        navigateAppMessageDialog(appMessage);
    }

    protected final boolean isDialogShowing() {
        return destinationId != getCurrentDestinationId();
    }

    /**
     * BaseFragment#showAppMessageDialog()で呼び出される。
     * */
    protected abstract void navigateAppMessageDialog(@NonNull AppMessage appMessage);

    protected abstract void retryOtherAppMessageDialogShow();

    private void retrySettingsAppMessageDialogShow() {
        settingsViewModel.triggerAppMessageBufferListObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        destroyBinding();
    }

    /**
     * Bindingクラス変数のメモリリーク対策として変数にNullを代入すること。
     * */
    protected abstract void destroyBinding();
}
