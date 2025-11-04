package com.websarva.wings.android.zuboradiary.ui

/**
 * フラグメント間で結果をやり取りする際に使用するリクエストキーの共通プレフィックス。
 *
 * 結果を設定する側の Fragment (または DialogFragment) は、自身の `companion object` 内で、
 * このプレフィックスの末尾に自身のクラス名を連結し、一意なリクエストキーを定義してください。
 * キーは `@JvmField val`として定義します。
 *
 * --- 使用例 ---
 *
 * // 結果を設定する Fragment (例: MyFragment.kt)
 * class MyFragment : Fragment() {
 *     companion object {
 *         @JvmField
 *         val RESULT_KEY = RESULT_KEY_PREFIX + MyFragment::class.java.name
 *     }
 *}
 */
const val RESULT_KEY_PREFIX = "result_from_"
