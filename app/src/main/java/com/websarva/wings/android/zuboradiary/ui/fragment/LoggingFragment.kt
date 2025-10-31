package com.websarva.wings.android.zuboradiary.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.websarva.wings.android.zuboradiary.core.utils.logTag

abstract class LoggingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "FragmentLifeCycle_onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag, "FragmentLifeCycle_onCreateView()")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(logTag, "FragmentLifeCycle_onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        Log.d(logTag, "FragmentLifeCycle_onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(logTag, "FragmentLifeCycle_onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(logTag, "FragmentLifeCycle_onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(logTag, "FragmentLifeCycle_onStop()")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(logTag, "FragmentLifeCycle_onDestroyView()")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(logTag, "FragmentLifeCycle_onDestroy()")
        super.onDestroy()
    }
}
