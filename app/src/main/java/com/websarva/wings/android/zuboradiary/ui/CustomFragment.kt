package com.websarva.wings.android.zuboradiary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class CustomFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(javaClass.simpleName, "onCreateView()")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        Log.d(javaClass.simpleName, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(javaClass.simpleName, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(javaClass.simpleName, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(javaClass.simpleName, "onStop()")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(javaClass.simpleName, "onDestroyView()")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "onDestroy()")
        super.onDestroy()
    }
}
