package com.websarva.wings.android.zuboradiary.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.websarva.wings.android.zuboradiary.MainActivity;
import com.websarva.wings.android.zuboradiary.data.AppError;

import java.util.List;


public abstract class BaseFragment extends CustomFragment {

    protected NavController navController;
    protected int destinationId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViewModel();

        navController = NavHostFragment.findNavController(this);
        destinationId = getCurrentDestinationId();
    }

    protected abstract void initializeViewModel();

    private int getCurrentDestinationId() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination == null) {
            throw new NullPointerException();
        }
        return navDestination.getId();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setUpFragmentTransitionEffect();
        return initializeDataBinding(inflater, container);
    }

    protected abstract View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container);

    private void setUpFragmentTransitionEffect() {
        // FROM:遷移元 TO:遷移先
        // FROM - TO の TO として現れるアニメーション

        // HACK:ボトムナビゲーションタブでFragment切替時はEnterTransitionで設定されるエフェクトを変更する。
        //      NavigationStartFragment(DiaryListFragment)はReenterTransitionで設定されたエフェクトが処理される。
        //      遷移元FragmentのエフェクトはMainActivityクラスにて設定。
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getTabWasSelected()) {
            setEnterTransition(new MaterialFadeThrough());
        } else {
            setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        }

        // FROM - TO の FROM として消えるアニメーション
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));

        // TO - FROM の FROM として現れるアニメーション
        if (mainActivity.getTabWasSelected()) {
            setReenterTransition(new MaterialFadeThrough());
        } else {
            setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        }

        // TO - FROM の TO として消えるアニメーション
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

        mainActivity.resetTabWasSelected();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpPreviousFragmentResultReceiver();
        setUpDialogResultReceiver();
        setUpErrorMessageDialog();
    }

    @Nullable
    private SavedStateHandle getNavBackStackEntrySavedStateHandle() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry == null) {
            return null;
        }
        return navBackStackEntry.getSavedStateHandle();
    }

    private void setUpPreviousFragmentResultReceiver() {
        SavedStateHandle savedStateHandle = getNavBackStackEntrySavedStateHandle();
        if (savedStateHandle == null) {
            return;
        }
        handleOnReceivingResultFromPreviousFragment(savedStateHandle);
    }

    protected abstract void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle);

    private void setUpDialogResultReceiver() {
        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
        if (navBackStackEntry == null) {
            return;
        }

        LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                // MEMO:Dialog表示中:Lifecycle.Event.ON_PAUSE
                //      Dialog非表示中:Lifecycle.Event.ON_RESUME
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    handleOnReceivingDialogResult(savedStateHandle);
                    retryErrorDialogShow();
                    removeDialogResult(savedStateHandle);
                }
            }
        };

        navBackStackEntry.getLifecycle().addObserver(lifecycleEventObserver);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(
                    @NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                    // MEMO:removeで削除しないとこのFragmentを閉じてもResult内容が残ってしまう。
                    //      その為、このFragmentを再表示した時にObserverがResultの内容で処理してしまう。
                    SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
                    removeDialogResult(savedStateHandle);
                    // TODO:下記コード意味あるか検証。コメントアウトしてFragment切替後の状態を確認したがObserverが重複することはなかった。
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    protected abstract void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle);

    protected abstract void removeDialogResult(@NonNull SavedStateHandle savedStateHandle);

    @Nullable
    public <T> T receiveResulFromDialog(String key) {
        SavedStateHandle savedStateHandle = getNavBackStackEntrySavedStateHandle();
        if (savedStateHandle == null) {
            return null;
        }
        boolean containsDialogResult = savedStateHandle.contains(key);
        if (!containsDialogResult) {
            return null;
        }
        return savedStateHandle.get(key);
    }

    /**
     * BaseViewModelのAppErrorBufferListのObserverを設定する。
     * */
    protected abstract void setUpErrorMessageDialog();

    protected class AppErrorBufferListObserver implements Observer<List<AppError>> {

        BaseViewModel baseViewModel;

        public AppErrorBufferListObserver(BaseViewModel baseViewModel) {
            if (baseViewModel == null) {
                throw new NullPointerException();
            }
            this.baseViewModel = baseViewModel;
        }

        @Override
        public void onChanged(List<AppError> appErrors) {
            if (appErrors == null) {
                throw new NullPointerException();
            }
            if (appErrors.isEmpty()) {
                return;
            }

            AppError appError = appErrors.get(0);
            showErrorMessageDialog(appError);
            baseViewModel.removeAppErrorBufferListFirstItem();
        }
    }

    private void showErrorMessageDialog(AppError appError) {
        if (appError == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        String dialogTitle = appError.getDialogTitle(requireContext());
        String dialogMessage = appError.getDialogMessage(requireContext());
        showMessageDialog(dialogTitle, dialogMessage);

    }

    protected boolean canShowOtherFragment() {
        return destinationId == getCurrentDestinationId();
    }

    protected abstract void showMessageDialog(@NonNull String title,@NonNull  String message);

    protected abstract void retryErrorDialogShow();

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
