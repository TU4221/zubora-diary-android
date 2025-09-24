package com.websarva.wings.android.zuboradiary.data.file.exception

/**
 * ディレクトリ内のファイル削除処理中に一つ以上のエラーが発生したことを示す例外。
 *
 * @property directoryPath 対象となったディレクトリのパス。
 * @property individualFailures 個々の失敗に関する情報 (ファイルパスと発生した例外のペアのリスト)。
 */
internal class DirectoryDeletionFailedException(
    private val directoryPath: String? = null,
    val individualFailures: List<Pair<String, Exception>>
) : FileOperationException(
    "ディレクトリ '${directoryPath ?: "画像ディレクトリ"} ' 内の一部または全てのファイル/ディレクトリの削除に失敗しました。" +
            "${individualFailures.size}件のエラーが見つかりました。"
) {

    override fun toString(): String {
        // エラー詳細のメッセージを日本語化
        val details =
            individualFailures
                .joinToString("\n  - ", prefix = "\n  - ") { (path, ex) ->
                    "$path: ${ex.javaClass.simpleName}(${ex.localizedMessage ?: ex.message ?: "詳細不明"})"
                }
        return "${super.toString()}\n失敗した処理の詳細:$details"
    }
}
