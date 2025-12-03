package com.websarva.wings.android.zuboradiary.ui.model.navigation

import android.os.Parcelable
import androidx.annotation.StringRes
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.ConfirmationDialogFragment
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な確認ダイアログ（[ConfirmationDialogFragment]）に渡すための引数モデル。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property titleText ダイアログのタイトルとして表示する文字列。`titleRes`よりも優先される。
 * @property titleRes ダイアログのタイトルとして表示する文字列リソースのID。
 * @property messageText ダイアログのメッセージとして表示する文字列。`messageRes`よりも優先される。
 * @property messageRes ダイアログのメッセージとして表示する文字列リソースのID。
 */
@Parcelize
data class ConfirmationDialogArgs(
    val resultKey: String,
    val titleText: String? = null,
    @param:StringRes val titleRes: Int? = null,
    val messageText: String? = null,
    @param:StringRes val messageRes: Int? = null
) : Parcelable {
    init {
        // タイトルとメッセージのどちらかは必ず指定されていることを保証する
        require(titleText != null || titleRes != null) { "タイトルは必ず指定してください。" }
        require(messageText != null || messageRes != null) { "メッセージは必ず指定してください。" }
    }
}
