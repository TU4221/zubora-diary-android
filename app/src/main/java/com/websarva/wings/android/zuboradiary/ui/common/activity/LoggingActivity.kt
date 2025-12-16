package com.websarva.wings.android.zuboradiary.ui.common.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.websarva.wings.android.zuboradiary.core.utils.logTag

/**
 * [androidx.appcompat.app.AppCompatActivity]のライフサイクルイベントをLogcatに出力する機能を持つ抽象基底クラス。
 *
 * デバッグ目的でActivityのライフサイクルの遷移を追跡したい場合に、
 * このクラスを継承して使用する。
 * 各ライフサイクルメソッド（[onCreate], [onStart]など）が呼ばれるたびに、
 * そのメソッド名をログに出力する。
 */
abstract class LoggingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        application
        Log.d(logTag, "ActivityLifeCycle_onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        Log.d(logTag, "ActivityLifeCycle_onRestart()")
        super.onRestart()
    }

    override fun onStart() {
        Log.d(logTag, "ActivityLifeCycle_onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(logTag, "ActivityLifeCycle_onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(logTag, "ActivityLifeCycle_onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(logTag, "ActivityLifeCycle_onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(logTag, "ActivityLifeCycle_onDestroy()")
        super.onDestroy()
    }
}
