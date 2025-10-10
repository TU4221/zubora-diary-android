package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

//region StringConverter
/**
 * ThemeColorUi enumに対応する文字列リソースIDを取得する拡張プロパティ。
 */
internal val ThemeColorUi.stringResId: Int
    get() = when (this) {
        ThemeColorUi.WHITE -> R.string.enum_theme_color_white
        ThemeColorUi.BLACK -> R.string.enum_theme_color_black
        ThemeColorUi.RED -> R.string.enum_theme_color_red
        ThemeColorUi.BLUE -> R.string.enum_theme_color_blue
        ThemeColorUi.GREEN -> R.string.enum_theme_color_green
        ThemeColorUi.YELLOW -> R.string.enum_theme_color_yellow
    }

/**
 * ThemeColorUiをContextを使ってローカライズされた文字列に変換する拡張関数。
 */
internal fun ThemeColorUi.asString(context: Context): String {
    return context.getString(this.stringResId)
}
//endregion

//region ResourceProperties
/**
 * ステータスバーのアイコン等を明るい色で表示するべきか (背景が暗い色か) を示す。
 */
internal val ThemeColorUi.isAppearanceLightStatusBars: Boolean
    get() = when (this) {
        ThemeColorUi.BLACK -> false
        ThemeColorUi.WHITE, ThemeColorUi.RED, ThemeColorUi.BLUE, ThemeColorUi.GREEN, ThemeColorUi.YELLOW -> true
    }

/**
 * DatePickerDialogに適用するテーマのリソースIDを取得する。
 */
internal val ThemeColorUi.datePickerDialogThemeResId: Int
    get() {
        return when (this) {
            ThemeColorUi.WHITE -> R.style.MaterialCalendarThemeColorWhite
            ThemeColorUi.BLACK -> R.style.MaterialCalendarThemeColorBlack
            ThemeColorUi.RED -> R.style.MaterialCalendarThemeColorRed
            ThemeColorUi.BLUE -> R.style.MaterialCalendarThemeColorBlue
            ThemeColorUi.GREEN -> R.style.MaterialCalendarThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.MaterialCalendarThemeColorYellow
        }
    }

/**
 * TimePickerDialogに適用するテーマのリソースIDを取得する。
 */
internal val ThemeColorUi.timePickerDialogThemeResId: Int
    get() {
        return when (this) {
            ThemeColorUi.WHITE -> R.style.MaterialTimePickerThemeColorWhite
            ThemeColorUi.BLACK -> R.style.MaterialTimePickerThemeColorBlack
            ThemeColorUi.RED -> R.style.MaterialTimePickerThemeColorRed
            ThemeColorUi.BLUE -> R.style.MaterialTimePickerThemeColorBlue
            ThemeColorUi.GREEN -> R.style.MaterialTimePickerThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.MaterialTimePickerThemeColorYellow
        }
    }

/**
 * アプリケーション全体に適用するテーマのリソースIDを取得する。
 */
internal val ThemeColorUi.themeResId: Int
    get() {
        return when (this) {
            ThemeColorUi.WHITE -> R.style.AppThemeColorWhite
            ThemeColorUi.BLACK -> R.style.AppThemeColorBlack
            ThemeColorUi.RED -> R.style.AppThemeColorRed
            ThemeColorUi.BLUE -> R.style.AppThemeColorBlue
            ThemeColorUi.GREEN -> R.style.AppThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.AppThemeColorYellow
        }
    }

/**
 * NumberPickerを含むBottomSheetDialogのViewをInflateする時のThemeResIdを取得する。
 */
internal val ThemeColorUi.numberPickerBottomSheetDialogThemeResId: Int
    get() {
        // HACK:下記理由から、ApiLevel29未満かつThemeColorBlackの時はNumberPickerBottomSheetDialogのThemeColorをWhiteにする。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
        return when (this) {
            ThemeColorUi.WHITE -> R.style.AppThemeColorWhite
            ThemeColorUi.BLACK -> if (Build.VERSION.SDK_INT >= 29) {
                R.style.AppThemeColorBlack
            } else {
                R.style.AppThemeColorWhite
            }
            ThemeColorUi.RED -> R.style.AppThemeColorRed
            ThemeColorUi.BLUE -> R.style.AppThemeColorBlue
            ThemeColorUi.GREEN -> R.style.AppThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.AppThemeColorYellow
        }
    }

/**
 * AlertDialogに適用するテーマのリソースIDを取得する。
 */
internal val ThemeColorUi.alertDialogThemeResId: Int
    get() {
        return when (this) {
            ThemeColorUi.WHITE -> R.style.MaterialAlertDialogThemeColorWhite
            ThemeColorUi.BLACK -> R.style.MaterialAlertDialogThemeColorBlack
            ThemeColorUi.RED -> R.style.MaterialAlertDialogThemeColorRed
            ThemeColorUi.BLUE -> R.style.MaterialAlertDialogThemeColorBlue
            ThemeColorUi.GREEN -> R.style.MaterialAlertDialogThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.MaterialAlertDialogThemeColorYellow
        }
    }

/**
 * BottomSheetDialogに適用するテーマのリソースIDを取得する。
 */
internal val ThemeColorUi.bottomSheetDialogThemeResId: Int
    get() {
        return when (this) {
            ThemeColorUi.WHITE -> R.style.AppBottomSheetDialogThemeColorWhite
            ThemeColorUi.BLACK -> R.style.AppBottomSheetDialogThemeColorBlack
            ThemeColorUi.RED -> R.style.AppBottomSheetDialogThemeColorRed
            ThemeColorUi.BLUE -> R.style.AppBottomSheetDialogThemeColorBlue
            ThemeColorUi.GREEN -> R.style.AppBottomSheetDialogThemeColorGreen
            ThemeColorUi.YELLOW -> R.style.AppBottomSheetDialogThemeColorYellow
        }
    }
//endregion

//region ColorValueConverter
internal fun ThemeColorUi.asPrimaryColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_primary
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_primary
        ThemeColorUi.RED -> R.color.md_theme_color_red_primary
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_primary
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_primary
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_primary
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnPrimaryColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onPrimary
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onPrimary
        ThemeColorUi.RED -> R.color.md_theme_color_red_onPrimary
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onPrimary
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onPrimary
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onPrimary
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnPrimaryContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onPrimaryContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onPrimaryContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_onPrimaryContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onPrimaryContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onPrimaryContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onPrimaryContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asSecondaryColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_secondary
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_secondary
        ThemeColorUi.RED -> R.color.md_theme_color_red_secondary
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_secondary
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_secondary
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_secondary
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnSecondaryColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onSecondary
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onSecondary
        ThemeColorUi.RED -> R.color.md_theme_color_red_onSecondary
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onSecondary
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onSecondary
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onSecondary
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asSecondaryContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_secondaryContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_secondaryContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_secondaryContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_secondaryContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_secondaryContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_secondaryContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnSecondaryContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onSecondaryContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onSecondaryContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_onSecondaryContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onSecondaryContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onSecondaryContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onSecondaryContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asTertiaryContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_tertiaryContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_tertiaryContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_tertiaryContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_tertiaryContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_tertiaryContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_tertiaryContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnTertiaryContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onTertiaryContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onTertiaryContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_onTertiaryContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onTertiaryContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onTertiaryContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onTertiaryContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asSurfaceColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_surface
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_surface
        ThemeColorUi.RED -> R.color.md_theme_color_red_surface
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_surface
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_surface
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_surface
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asSurfaceContainerColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_surfaceContainer
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_surfaceContainer
        ThemeColorUi.RED -> R.color.md_theme_color_red_surfaceContainer
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_surfaceContainer
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_surfaceContainer
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_surfaceContainer
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asSurfaceContainerHighestColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_surfaceContainerHighest
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_surfaceContainerHighest
        ThemeColorUi.RED -> R.color.md_theme_color_red_surfaceContainerHighest
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_surfaceContainerHighest
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_surfaceContainerHighest
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_surfaceContainerHighest
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnSurfaceColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onSurface
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onSurface
        ThemeColorUi.RED -> R.color.md_theme_color_red_onSurface
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onSurface
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onSurface
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onSurface
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOnSurfaceVariantColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_onSurfaceVariant
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_onSurfaceVariant
        ThemeColorUi.RED -> R.color.md_theme_color_red_onSurfaceVariant
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_onSurfaceVariant
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_onSurfaceVariant
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_onSurfaceVariant
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOutlineColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_outline
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_outline
        ThemeColorUi.RED -> R.color.md_theme_color_red_outline
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_outline
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_outline
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_outline
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asOutlineVariantColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_outlineVariant
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_outlineVariant
        ThemeColorUi.RED -> R.color.md_theme_color_red_outlineVariant
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_outlineVariant
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_outlineVariant
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_outlineVariant
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}

internal fun ThemeColorUi.asErrorColorInt(resources: Resources): Int {
    val colorResId = when (this) {
        ThemeColorUi.WHITE -> R.color.md_theme_color_white_error
        ThemeColorUi.BLACK -> R.color.md_theme_color_black_error
        ThemeColorUi.RED -> R.color.md_theme_color_red_error
        ThemeColorUi.BLUE -> R.color.md_theme_color_blue_error
        ThemeColorUi.GREEN -> R.color.md_theme_color_green_error
        ThemeColorUi.YELLOW -> R.color.md_theme_color_yellow_error
    }
    return ResourcesCompat.getColor(resources, colorResId, null)
}
//endregion
