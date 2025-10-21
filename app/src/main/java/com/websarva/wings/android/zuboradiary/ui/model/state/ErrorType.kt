package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO:仮で作成。全FragmentのUiState実装後必要有無判断
/**
* UI層で扱う、エラーの種類を表現するシールドクラス。
*/
@Parcelize
internal sealed class ErrorType : Parcelable {
    abstract val cause: Exception
    
    /**
     * 要求されたリソース（日記、ユーザーなど）が存在しなかったことを示す。
     *
     * @param cause エラーの根本的な原因となった [Exception]。
     */
    data class NotFound(override val cause: Exception) : ErrorType()

    /**
     * ネットワーク接続の失敗、サーバーからのエラー応答(4xx)、DBアクセス失敗など、処理が正常に完了しなかったことを示す。
     * 
     * @param cause エラーの根本的な原因となった [Exception]。
     */
    data class Failure(override val cause: Exception) : ErrorType()

    /**
     * 予期せぬエラーが発生して、処理が正常に完了しなかったことを示す。
     * 
     * @param cause エラーの根本的な原因となった [Exception]。
     */
    data class Unexpected(override val cause: Exception) : ErrorType()
}
