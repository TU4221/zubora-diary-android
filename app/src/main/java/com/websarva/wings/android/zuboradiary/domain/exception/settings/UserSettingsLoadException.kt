package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * ユーザー設定の読み込み処理中にエラーが発生した場合にスローされる例外基底クラス。。
 *
 * @param message エラーメッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class UserSettingsLoadException (
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {

    /**
     * ユーザー設定へのアクセスに失敗し、読み込みができなかった場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
     */
    class AccessFailure(
        cause: Throwable? = null
    ) : UserSettingsLoadException("ユーザー設定へのアクセスに失敗しました。", cause)

    /**
     * 要求されたユーザー設定データが見つからず、読み込みができなかった場合にスローされる例外。
     *
     * 例えば、特定の設定キーに対応する値が存在しない場合などが考えられる。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
     */
    class DataNotFound(
        cause: Throwable? = null
    ) : UserSettingsLoadException("指定されたユーザー設定のデータが存在しません。", cause)
}
