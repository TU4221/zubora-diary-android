package com.websarva.wings.android.zuboradiary.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class CustomFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("FragmentLifeCycle", "onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("FragmentLifeCycle", "onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentLifeCycle", "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d("FragmentLifeCycle", "onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("FragmentLifeCycle", "onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("FragmentLifeCycle", "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("FragmentLifeCycle", "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d("FragmentLifeCycle", "onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d("FragmentLifeCycle", "onDestroy()");
        super.onDestroy();
    }
}
