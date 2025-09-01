package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor

/**
 * テーマカラー設定の更新処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param themeColor 更新しようとしたテーマカラー。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class ThemeColorSettingUpdateFailureException(
    themeColor: ThemeColor,
    cause: Throwable
) : DomainException("テーマカラー設定 '$themeColor' の更新に失敗しました。", cause)
