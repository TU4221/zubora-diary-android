package com.websarva.wings.android.zuboradiary.ui.model.common

import androidx.recyclerview.widget.ListAdapter

/**
 * UIモデルを一意に識別するためのIDを提供させるインターフェース。
 *
 * [ListAdapter]などで、アイテムの差分計算や状態保持を効率的に行うために使用される。
 */
internal interface Identifiable {

    /** アイテムを一意に識別するためのID。 */
    val id: Any
}
