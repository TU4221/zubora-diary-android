package com.websarva.wings.android.zuboradiary.ui.common.navigation.event

/**
 * 後方遷移先が存在しない場合に使用する代替インターフェース。
 *
 * 通常、型引数が不要な場合は [Nothing] を使用すべきだが、
 * Android Data Binding（XMLレイアウト）の技術的な制約を回避するために用意。
 */
// HACK:ViewModelのジェネリクス型引数に [Nothing] を指定すると、
//      Data Bindingのコード生成時にビルドエラーが発生する問題がある。本インターフェースはその回避策として使用する。
sealed interface DummyNavBackDestination : AppNavBackDestination
