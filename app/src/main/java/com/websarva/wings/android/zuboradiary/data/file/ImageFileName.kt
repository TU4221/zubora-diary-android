package com.websarva.wings.android.zuboradiary.data.file

/**
 * 画像ファイル名を表すバリュークラス。
 *
 * このクラスは、ファイル名が空ではなく、一般的ファイル形式 (basename.extension) であり、
 * かつ拡張子がJPEG形式 (jpg, jpeg) であることを保障する。
 *
 * @property fullName 画像のファイル名。拡張子を含む。
 * @throws IllegalArgumentException ファイル名が指定の要件を満たさない場合。
 */
@JvmInline
value class ImageFileName(
    val fullName: String
) {

    init {
        // 1. 空でないこと
        require(fullName.isNotBlank()) { "ファイル名が空です。" }

        // 2. ベース名と拡張子の間の「.」が含まれていること
        val dotIndex = fullName.lastIndexOf('.')
        require(dotIndex != -1) { "ファイル名に拡張子セパレータ `.` が含まれていません。" }

        // 3. ベース名と拡張子がふくまれていること(「.」の前後に文字が1文字以上含まれていること)
        require(dotIndex > 0) { "ファイル名にベース名が含まれていません。" }
        require(dotIndex < fullName.length - 1) { "ファイル名に拡張子が含まれていません。" }

        // 4. 拡張子がJPEG形式 (jpg, jpeg) であること
        val extension = fullName.substring(dotIndex + 1)
        require(
            extension.equals("jpg", ignoreCase = true) || extension.equals("jpeg", ignoreCase = true)
        ) {
            "ファイル拡張子がJPEG形式 (jpg, jpeg) ではありません。現在の拡張子: $extension"
        }
    }
}
