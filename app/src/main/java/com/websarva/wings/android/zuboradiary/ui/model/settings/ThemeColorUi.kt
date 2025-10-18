package com.websarva.wings.android.zuboradiary.ui.model.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Arrays

@Parcelize
// CAUTION:要素の追加、順序変更を行った時はThemeColorPickerDialogFragment、string.xmlを修正すること。
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class ThemeColorUi @Suppress("unused") constructor(
    val number: Int
) : Parcelable {

    WHITE(0),
    BLACK(1),
    RED(2),
    BLUE(3),
    GREEN(4),
    YELLOW(5);

    companion object {
        @JvmStatic
        fun of(number: Int): ThemeColorUi {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: ThemeColorUi -> x.number == number }.findFirst().get()
        }
    }
}
