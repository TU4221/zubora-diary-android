package com.websarva.wings.android.zuboradiary.domain.model

/**
 * 日記の添付画像を表すデータクラス。
 *
 * このクラスは、日記で表示する添付画像(フルサイズ)と日記リストで表示する画像(サムネイルサイズ)のファイルパスを保持する。
 *
 * @property fullSizePath 日記で表示する添付画像(フルサイズ)のファイルパス。
 *   ファイルベース名の末尾が [SUFFIX_FULL] であること。
 * @property thumbnailSizePath 日記リストで表示する画像(サムネイルサイズ)のファイルパス。
 *   ファイルベース名の末尾が [SUFFIX_THUMB] であること。
 * @throws IllegalArgumentException ファイルベース名の末尾に対象の接尾辞が含まれていない場合。
 */
internal data class DiaryImageFilePath(
    val fullSizePath: String,
    val thumbnailSizePath: String,
) {

    companion object {
        // TODO:
        const val SUFFIX_FULL = "_full"
        const val SUFFIX_THUMB = "_thumbnail"
    }

    init {
        require(hasFullSuffixInBasename(fullSizePath, SUFFIX_FULL))
        require(hasFullSuffixInBasename(thumbnailSizePath, SUFFIX_THUMB))
    }

    private fun hasFullSuffixInBasename(filePath: String, targetSuffix: String): Boolean {
        if (filePath.isEmpty()) {
            return false
        }

        // 1. ファイル名部分を取得 (パス区切り文字の最後の要素)
        //    ('/' はUnix/Android想定)
        val fileName = filePath.substringAfterLast('/', filePath)
        // 2. 拡張子を除いたベース名を取得
        val baseName = fileName.substringBeforeLast('.', fileName)
        // 3. ベース名の末尾が対象の接尾辞かどうかを確認
        return baseName.endsWith(targetSuffix)
    }
}
