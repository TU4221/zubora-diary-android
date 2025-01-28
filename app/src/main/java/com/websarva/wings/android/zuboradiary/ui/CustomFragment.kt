package com.websarva.wings.android.zuboradiary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class CustomFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("FragmentLifeCycle", "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d("FragmentLifeCycle", "onCreateView()")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("FragmentLifeCycle", "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        Log.d("FragmentLifeCycle", "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d("FragmentLifeCycle", "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d("FragmentLifeCycle", "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d("FragmentLifeCycle", "onStop()")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d("FragmentLifeCycle", "onDestroyView()")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("FragmentLifeCycle", "onDestroy()")
        super.onDestroy()
    }
}
