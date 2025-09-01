package com.websarva.wings.android.zuboradiary.domain.model

import java.util.Arrays

/**
 * アプリケーションのテーマカラーを表す enum クラス。
 *
 * 各 enum 定数は、整数値と対応付けて定義する。
 *
 * @property number テーマカラーを表す整数値。
 */
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class ThemeColor @Suppress("unused") constructor(
    private val number: Int
) {

    /** 白色テーマ。 */
    WHITE(0),

    /** 黒色テーマ。 */
    BLACK(1),

    /** 赤色テーマ。 */
    RED(2),

    /** 青色テーマ。 */
    BLUE(3),

    /** 緑色テーマ。 */
    GREEN(4),

    /** 黄色テーマ。 */
    YELLOW(5);

    companion object {
        /**
         * 指定された整数値に対応する [ThemeColor] を返す。
         *
         * @param number 検索する[ThemeColor]の要素に対応付けされた整数値。
         * @return 対応する [ThemeColor]。
         * @throws NoSuchElementException 指定された整数値に対応する [ThemeColor] が見つからない場合。
         */
        @JvmStatic
        fun of(number: Int): ThemeColor {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: ThemeColor -> x.toNumber() == number }.findFirst().get()
        }
    }

    /**
     * このテーマカラーを表す整数値を返す。
     *
     * @return テーマカラーの整数値。
     */
    fun toNumber(): Int {
        return number
    }
}
