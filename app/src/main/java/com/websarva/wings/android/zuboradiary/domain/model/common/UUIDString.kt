package com.websarva.wings.android.zuboradiary.domain.model.common

import java.util.UUID

/**
 * UUIDの文字列を表す抽象クラス。
 *
 * このクラスは、UUIDの文字列が有効なUUID形式であるかを検証する機能を有する。
 *
 * **【重要】** このクラスを継承するサブクラスは、自身の`init`ブロック内で必ず [validate] を呼び出すこと。
 */
internal abstract class UUIDString {

    /**
     * UUIDの文字列。
     */
    abstract val value: String

    /**
     * [value] プロパティが有効なUUID形式であるかを検証。
     * このメソッドは、必ずサブクラスの`init`ブロックから呼び出すこと。
     *
     * @throws IllegalArgumentException `value`が有効なUUID形式でない場合。
     */
    protected fun validate() {
        UUID.fromString(value)
    }
}
