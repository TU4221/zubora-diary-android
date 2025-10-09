package com.websarva.wings.android.zuboradiary.domain.model.common

/**
 * ファイル名を表す抽象クラス。
 *
 * このクラスは、ファイル名が有効なファイル名であるかを検証する機能を有する。
 *
 * **【重要】** このクラスを継承するサブクラスは、自身の`init`ブロック内で必ず [validate] を呼び出すこと。
 *
 * @property fullName ファイル名。拡張子を含む。
 */
internal abstract class FileName(
    open val fullName: String
) {

    /**
     * [fullName] プロパティが有効なファイル名であるかを検証。
     * - ファイル名が空ではないこと。
     * - 一般的ファイル形式 (basename.extension) であること。
     *
     * このメソッドは、必ずサブクラスの`init`ブロックから呼び出すこと。
     *
     * @throws IllegalArgumentException `value`が有効なUUID形式でない場合。
     */
    protected fun validate() {
        // 1. 空でないこと
        require(fullName.isNotBlank()) { "ファイル名が空です。" }

        // 2. ベース名と拡張子の間の「.」が含まれていること
        val dotIndex = fullName.lastIndexOf('.')
        require(dotIndex != -1) { "ファイル名にベース名と拡張子を分ける `.` が含まれていません。" }

        // 3. ベース名と拡張子がふくまれていること(「.」の前後に文字が1文字以上含まれていること)
        require(dotIndex > 0) { "ベース名が含まれていません。" }
        require(dotIndex < fullName.length - 1) { "拡張子が含まれていません。" }
    }
}
