package com.websarva.wings.android.zuboradiary.core.utils

import kotlinx.coroutines.CancellationException

/**
 * 例外がコルーチンのキャンセル（CancellationException）に起因するものであれば再スローする。
 * それ以外の例外であれば何もしない。
 *
 * ExceptionやIllegalStateExceptionなど、広範囲の例外をキャッチするブロック内で、
 * コルーチンのキャンセルを誤って握りつぶさないためのガード節として使用する。
 *
 * **【使用するタイミング】**
 * `try` ブロック内で **`suspend` 関数** を呼び出している場合に記述する。
 * これらの中断関数は、待機中にキャンセルされると `CancellationException` をスローするためである。
 *
 * ※ `suspend` 関数を含まない（通常のJavaメソッド呼び出し等の）処理ブロックでは、
 * 実行中にキャンセル例外が発生しないため、この判定は不要である。
 */
fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
}
