package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

// TODO: [画像表示の永続化改善] 現在は画像URIを直接参照しているが、以下の課題があるため、
//       画像選択時にサムネイル（または適切なプレビューサイズのBitmap）を生成し、
//       アプリの内部ストレージにファイルとして保存する方式への変更を検討する。
//
//       課題:
//       1. URIパーミッションの失効: アプリの再インストールや機種変更、
//          またはOSによる権限クリアにより、保存したURIへのアクセス権限が失われ、
//          ユーザーによる再選択が必要になる。
//       2. 元ファイルの変更/削除への追従不可: ユーザーがファイルマネージャー等で
//          元の画像ファイルを変更・削除しても、保存されたURIではそれを検知できない
//          （または検知が難しい）。
//
//       変更による期待効果:
//       - URIパーミッション失効後も、アプリ内に保存したサムネイルで画像を表示可能にする。
//       - 機種変更時のデータ移行（自動バックアップ経由）で画像表示を引き継ぎやすくする。
//
//       検討事項・作業項目:
//       - サムネイル生成処理の実装（適切なサイズ、品質の決定）。
//       - アプリ内部ストレージへのファイル保存処理（ファイル名命名規則、保存場所の決定）。
//       - データベースに保存する情報を、元のURIから内部ストレージのファイルパス/URIに変更。
//       - 既存データのマイグレーション方法（もしあれば）。
//       - サムネイルのキャッシュ管理（不要になったサムネイルの削除ロジックなど）。
//       - 元画像へのフルアクセスが必要な場合のフォールバック（例: 高解像度表示時）。
internal class TakePersistableUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(uriString: String): DefaultUseCaseResult<Unit> {
        val logMsg = "URIの永続的権限取得_"
        Log.i(logTag, "${logMsg}開始_$uriString")

        return try {
            uriRepository.takePersistableUriPermission(uriString)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: PersistableUriPermissionTakeFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            UseCaseResult.Failure(e)
        }
    }
}
