package com.websarva.wings.android.zuboradiary.domain.model

import java.util.Arrays

// CAUTION:要素の追加、順序変更を行った時はThemeColorPickerDialogFragment、string.xmlを修正すること。
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class ThemeColor @Suppress("unused") constructor(
    private val number: Int
) {

    WHITE(0),
    BLACK(1),
    RED(2),
    BLUE(3),
    GREEN(4),
    YELLOW(5);

    companion object {
        @JvmStatic
        fun of(number: Int): ThemeColor {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: ThemeColor -> x.toNumber() == number }.findFirst().get()
        }
    }

    fun toNumber(): Int {
        return number
    }
}
