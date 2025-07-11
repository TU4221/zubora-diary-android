package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class UpdateThemeColorSettingFailedException(
    themeColor: ThemeColor,
    cause: Throwable
) : DomainException("テーマカラー設定 '$themeColor' の更新に失敗しました。", cause)
