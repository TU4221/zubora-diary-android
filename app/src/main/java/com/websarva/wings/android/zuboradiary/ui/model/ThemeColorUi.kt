package com.websarva.wings.android.zuboradiary.ui.model

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

// CAUTION:要素の追加、順序変更を行った時はThemeColorPickerDialogFragment、string.xmlを修正すること。
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class ThemeColorUi @Suppress("unused") constructor(
    val number: Int,
    private val stringResId: Int
) {

    WHITE(0, R.string.enum_theme_color_white),
    BLACK(1, R.string.enum_theme_color_black),
    RED(2, R.string.enum_theme_color_red),
    BLUE(3, R.string.enum_theme_color_blue),
    GREEN(4, R.string.enum_theme_color_green),
    YELLOW(5, R.string.enum_theme_color_yellow);

    companion object {
        @JvmStatic
        fun of(number: Int): ThemeColorUi {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: ThemeColorUi -> x.number == number }.findFirst().get()
        }
    }

    fun toSting(context: Context): String {
        return context.getString(stringResId)
    }

    val isAppearanceLightStatusBars
        // MEMO:下記以降はViewに関するカラー、リソース等を取得するメソッド。
        get() = when (this) {
            BLACK -> false
            WHITE, RED, BLUE, GREEN, YELLOW -> true
        }

    fun getPrimaryColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_primary
            BLACK -> R.color.md_theme_color_black_primary
            RED -> R.color.md_theme_color_red_primary
            BLUE -> R.color.md_theme_color_blue_primary
            GREEN -> R.color.md_theme_color_green_primary
            YELLOW -> R.color.md_theme_color_yellow_primary
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnPrimaryColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onPrimary
            BLACK -> R.color.md_theme_color_black_onPrimary
            RED -> R.color.md_theme_color_red_onPrimary
            BLUE -> R.color.md_theme_color_blue_onPrimary
            GREEN -> R.color.md_theme_color_green_onPrimary
            YELLOW -> R.color.md_theme_color_yellow_onPrimary
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnPrimaryContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onPrimaryContainer
            BLACK -> R.color.md_theme_color_black_onPrimaryContainer
            RED -> R.color.md_theme_color_red_onPrimaryContainer
            BLUE -> R.color.md_theme_color_blue_onPrimaryContainer
            GREEN -> R.color.md_theme_color_green_onPrimaryContainer
            YELLOW -> R.color.md_theme_color_yellow_onPrimaryContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getSecondaryColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_secondary
            BLACK -> R.color.md_theme_color_black_secondary
            RED -> R.color.md_theme_color_red_secondary
            BLUE -> R.color.md_theme_color_blue_secondary
            GREEN -> R.color.md_theme_color_green_secondary
            YELLOW -> R.color.md_theme_color_yellow_secondary
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnSecondaryColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onSecondary
            BLACK -> R.color.md_theme_color_black_onSecondary
            RED -> R.color.md_theme_color_red_onSecondary
            BLUE -> R.color.md_theme_color_blue_onSecondary
            GREEN -> R.color.md_theme_color_green_onSecondary
            YELLOW -> R.color.md_theme_color_yellow_onSecondary
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getSecondaryContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_secondaryContainer
            BLACK -> R.color.md_theme_color_black_secondaryContainer
            RED -> R.color.md_theme_color_red_secondaryContainer
            BLUE -> R.color.md_theme_color_blue_secondaryContainer
            GREEN -> R.color.md_theme_color_green_secondaryContainer
            YELLOW -> R.color.md_theme_color_yellow_secondaryContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnSecondaryContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onSecondaryContainer
            BLACK -> R.color.md_theme_color_black_onSecondaryContainer
            RED -> R.color.md_theme_color_red_onSecondaryContainer
            BLUE -> R.color.md_theme_color_blue_onSecondaryContainer
            GREEN -> R.color.md_theme_color_green_onSecondaryContainer
            YELLOW -> R.color.md_theme_color_yellow_onSecondaryContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getTertiaryContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_tertiaryContainer
            BLACK -> R.color.md_theme_color_black_tertiaryContainer
            RED -> R.color.md_theme_color_red_tertiaryContainer
            BLUE -> R.color.md_theme_color_blue_tertiaryContainer
            GREEN -> R.color.md_theme_color_green_tertiaryContainer
            YELLOW -> R.color.md_theme_color_yellow_tertiaryContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnTertiaryContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onTertiaryContainer
            BLACK -> R.color.md_theme_color_black_onTertiaryContainer
            RED -> R.color.md_theme_color_red_onTertiaryContainer
            BLUE -> R.color.md_theme_color_blue_onTertiaryContainer
            GREEN -> R.color.md_theme_color_green_onTertiaryContainer
            YELLOW -> R.color.md_theme_color_yellow_onTertiaryContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getSurfaceColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_surface
            BLACK -> R.color.md_theme_color_black_surface
            RED -> R.color.md_theme_color_red_surface
            BLUE -> R.color.md_theme_color_blue_surface
            GREEN -> R.color.md_theme_color_green_surface
            YELLOW -> R.color.md_theme_color_yellow_surface
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getSurfaceContainerColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_surfaceContainer
            BLACK -> R.color.md_theme_color_black_surfaceContainer
            RED -> R.color.md_theme_color_red_surfaceContainer
            BLUE -> R.color.md_theme_color_blue_surfaceContainer
            GREEN -> R.color.md_theme_color_green_surfaceContainer
            YELLOW -> R.color.md_theme_color_yellow_surfaceContainer
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getSurfaceContainerHighestColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_surfaceContainerHighest
            BLACK -> R.color.md_theme_color_black_surfaceContainerHighest
            RED -> R.color.md_theme_color_red_surfaceContainerHighest
            BLUE -> R.color.md_theme_color_blue_surfaceContainerHighest
            GREEN -> R.color.md_theme_color_green_surfaceContainerHighest
            YELLOW -> R.color.md_theme_color_yellow_surfaceContainerHighest
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnSurfaceColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onSurface
            BLACK -> R.color.md_theme_color_black_onSurface
            RED -> R.color.md_theme_color_red_onSurface
            BLUE -> R.color.md_theme_color_blue_onSurface
            GREEN -> R.color.md_theme_color_green_onSurface
            YELLOW -> R.color.md_theme_color_yellow_onSurface
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOnSurfaceVariantColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_onSurfaceVariant
            BLACK -> R.color.md_theme_color_black_onSurfaceVariant
            RED -> R.color.md_theme_color_red_onSurfaceVariant
            BLUE -> R.color.md_theme_color_blue_onSurfaceVariant
            GREEN -> R.color.md_theme_color_green_onSurfaceVariant
            YELLOW -> R.color.md_theme_color_yellow_onSurfaceVariant
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOutlineColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_outline
            BLACK -> R.color.md_theme_color_black_outline
            RED -> R.color.md_theme_color_red_outline
            BLUE -> R.color.md_theme_color_blue_outline
            GREEN -> R.color.md_theme_color_green_outline
            YELLOW -> R.color.md_theme_color_yellow_outline
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getOutlineVariantColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_outlineVariant
            BLACK -> R.color.md_theme_color_black_outlineVariant
            RED -> R.color.md_theme_color_red_outlineVariant
            BLUE -> R.color.md_theme_color_blue_outlineVariant
            GREEN -> R.color.md_theme_color_green_outlineVariant
            YELLOW -> R.color.md_theme_color_yellow_outlineVariant
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getErrorColor(resources: Resources): Int {
        val colorResId = when (this) {
            WHITE -> R.color.md_theme_color_white_error
            BLACK -> R.color.md_theme_color_black_error
            RED -> R.color.md_theme_color_red_error
            BLUE -> R.color.md_theme_color_blue_error
            GREEN -> R.color.md_theme_color_green_error
            YELLOW -> R.color.md_theme_color_yellow_error
        }
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    val datePickerDialogThemeResId: Int
        get() {
            return when (this) {
                WHITE -> R.style.MaterialCalendarThemeColorWhite
                BLACK -> R.style.MaterialCalendarThemeColorBlack
                RED -> R.style.MaterialCalendarThemeColorRed
                BLUE -> R.style.MaterialCalendarThemeColorBlue
                GREEN -> R.style.MaterialCalendarThemeColorGreen
                YELLOW -> R.style.MaterialCalendarThemeColorYellow
            }
        }

    val timePickerDialogThemeResId: Int
        get() {
            return when (this) {
                WHITE -> R.style.MaterialTimePickerThemeColorWhite
                BLACK -> R.style.MaterialTimePickerThemeColorBlack
                RED -> R.style.MaterialTimePickerThemeColorRed
                BLUE -> R.style.MaterialTimePickerThemeColorBlue
                GREEN -> R.style.MaterialTimePickerThemeColorGreen
                YELLOW -> R.style.MaterialTimePickerThemeColorYellow
            }
        }

    val themeResId: Int
        get() {
            return when (this) {
                WHITE -> R.style.AppThemeColorWhite
                BLACK -> R.style.AppThemeColorBlack
                RED -> R.style.AppThemeColorRed
                BLUE -> R.style.AppThemeColorBlue
                GREEN -> R.style.AppThemeColorGreen
                YELLOW -> R.style.AppThemeColorYellow
            }
        }

    val numberPickerBottomSheetDialogThemeResId: Int
        /**
         * NumberPickerを含むBottomSheetDialogのViewをInflateする時のThemeResIdを取得する。
         */
        get() {
            // HACK:下記理由から、ApiLevel29未満かつThemeColorBlackの時はNumberPickerBottomSheetDialogのThemeColorをWhiteにする。
            //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
            //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
            return when (this) {
                WHITE -> R.style.AppThemeColorWhite
                BLACK -> if (Build.VERSION.SDK_INT >= 29) {
                    R.style.AppThemeColorBlack
                } else {
                    R.style.AppThemeColorWhite
                }

                RED -> R.style.AppThemeColorRed
                BLUE -> R.style.AppThemeColorBlue
                GREEN -> R.style.AppThemeColorGreen
                YELLOW -> R.style.AppThemeColorYellow
            }
        }

    val alertDialogThemeResId: Int
        get() {
            return when (this) {
                WHITE -> R.style.MaterialAlertDialogThemeColorWhite
                BLACK -> R.style.MaterialAlertDialogThemeColorBlack
                RED -> R.style.MaterialAlertDialogThemeColorRed
                BLUE -> R.style.MaterialAlertDialogThemeColorBlue
                GREEN -> R.style.MaterialAlertDialogThemeColorGreen
                YELLOW -> R.style.MaterialAlertDialogThemeColorYellow
            }
        }

    val bottomSheetDialogThemeResId: Int
        get() {
            return when (this) {
                WHITE -> R.style.AppBottomSheetDialogThemeColorWhite
                BLACK -> R.style.AppBottomSheetDialogThemeColorBlack
                RED -> R.style.AppBottomSheetDialogThemeColorRed
                BLUE -> R.style.AppBottomSheetDialogThemeColorBlue
                GREEN -> R.style.AppBottomSheetDialogThemeColorGreen
                YELLOW -> R.style.AppBottomSheetDialogThemeColorYellow
            }
        }
}
