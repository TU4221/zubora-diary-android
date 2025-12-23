package com.websarva.wings.android.zuboradiary.ui.common.fragment.dialog.alert

import android.os.Parcelable
import androidx.annotation.StringRes
import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.Parcelize

/**
 * 汎用的な確認ダイアログへの遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property titleText ダイアログのタイトルとして表示する文字列。`titleRes`よりも優先される。
 * @property titleRes ダイアログのタイトルとして表示する文字列リソースのID。
 * @property messageText ダイアログのメッセージとして表示する文字列。`messageRes`よりも優先される。
 * @property messageRes ダイアログのメッセージとして表示する文字列リソースのID。
 * @property positiveButtonText Positiveボタンの文字列。指定がある場合 `positiveButtonRes` より優先される。
 * @property positiveButtonRes Positiveボタンの文字列リソースID。デフォルトは「はい」。
 * @property negativeButtonText Negativeボタンの文字列。指定がある場合 `negativeButtonRes` より優先される。
 * @property negativeButtonRes Negativeボタンの文字列リソースID。デフォルトは「いいえ」。nullを指定するとボタンを非表示にする。
 */
@Parcelize
data class ConfirmationDialogParams(
    val resultKey: String,
    val titleText: String? = null,
    @param:StringRes val titleRes: Int? = null,
    val messageText: String? = null,
    @param:StringRes val messageRes: Int? = null,
    val positiveButtonText: String? = null,
    @param:StringRes val positiveButtonRes: Int = R.string.dialog_alert_positive,
    val negativeButtonText: String? = null,
    @param:StringRes val negativeButtonRes: Int? = R.string.dialog_alert_negative
) : Parcelable {
    init {
        // タイトルとメッセージのどちらかは必ず指定されていることを保証する
        require(titleText != null || titleRes != null) { "タイトルは必ず指定してください。" }
        require(messageText != null || messageRes != null) { "メッセージは必ず指定してください。" }
    }
}
