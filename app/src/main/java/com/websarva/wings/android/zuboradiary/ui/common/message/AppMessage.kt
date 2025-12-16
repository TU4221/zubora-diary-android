package com.websarva.wings.android.zuboradiary.ui.common.message

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes

/**
 * アプリケーション内でユーザーに表示するメッセージ（情報、警告、エラーなど）を表現するためのインターフェース。
 *
 * このインターフェースを実装するクラスは、ダイアログのタイトルとメッセージの内容を定義する。
 * メッセージには、[String.format]スタイルのプレースホルダを含めることができ、[dialogMessageArgs]で引数を指定する。
 */
abstract class AppMessage : Parcelable {

    /** ダイアログのタイトルとして表示される文字列リソースID。 */
    @get:StringRes
    abstract val dialogTitleStringResId: Int

    /** ダイアログのメッセージとして表示される文字列リソースID。 */
    @get:StringRes
    abstract val dialogMessageStringResId: Int

    /** ダイアログメッセージ内のプレースホルダに挿入する引数の配列。 */
    open val dialogMessageArgs: Array<Any>
        get() = emptyArray()

    /**
     * コンテキストを使用して、ダイアログのタイトル文字列を取得する。
     * @param context 文字列リソースにアクセスするためのコンテキスト。
     * @return フォーマットされたダイアログタイトル文字列。
     */
    fun getDialogTitle(context: Context): String {
        return context.getString(dialogTitleStringResId)
    }

    /**
     * コンテキストを使用して、ダイアログのメッセージ文字列を取得する。
     * [dialogMessageArgs]に引数が指定されている場合、メッセージはフォーマットされる。
     * @param context 文字列リソースにアクセスするためのコンテキスト。
     * @return フォーマットされたダイアログメッセージ文字列。
     */
    fun getDialogMessage(context: Context): String {
        return context.getString(dialogMessageStringResId, *dialogMessageArgs)
    }
}
