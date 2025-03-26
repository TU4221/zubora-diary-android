package com.websarva.wings.android.zuboradiary

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class CustomActivity : AppCompatActivity() {

    private val logTag = getLogTag()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        Log.d(logTag, "onRestart()")
        super.onRestart()
    }

    override fun onStart() {
        Log.d(logTag, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(logTag, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(logTag, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(logTag, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(logTag, "onDestroy()")
        super.onDestroy()
    }
}
