package com.websarva.wings.android.zuboradiary.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;


public abstract class BaseFragment extends CustomFragment {

    public NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeNavController();
    }

    private void initializeNavController() {
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpPreviousFragmentResultReceiver();
        setUpDialogResultReceiver();
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
        handleOnReceivedResultFromPreviousFragment(savedStateHandle);
    }

    protected abstract void handleOnReceivedResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle);

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
                    handleOnReceivedResulFromDialog(savedStateHandle);
                    removeResulFromDialog(savedStateHandle);
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
                    removeResulFromDialog(savedStateHandle);
                    // TODO:下記コード意味あるか検証。コメントアウトしてFragment切替後の状態を確認したがObserverが重複することはなかった。
                    navBackStackEntry.getLifecycle().removeObserver(lifecycleEventObserver);
                }
            }
        });
    }

    protected abstract void handleOnReceivedResulFromDialog(@NonNull SavedStateHandle savedStateHandle);

    protected abstract void removeResulFromDialog(@NonNull SavedStateHandle savedStateHandle);

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
}
