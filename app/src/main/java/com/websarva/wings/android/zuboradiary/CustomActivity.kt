package com.websarva.wings.android.zuboradiary

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class CustomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        Log.d(javaClass.simpleName, "onRestart()")
        super.onRestart()
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

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "onDestroy()")
        super.onDestroy()
    }
}
