package com.websarva.wings.android.zuboradiary.data.mapper.location

import android.location.Location
import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation

internal fun Location.toDomainModel(): SimpleLocation {
    return SimpleLocation(latitude, longitude)
}
