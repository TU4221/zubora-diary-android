package com.websarva.wings.android.zuboradiary.domain.model.diary

/**
 * 日記の項目番号を表すバリュークラス。
 *
 * このクラスは、1から5までの整数値をラップする。
 *
 * @property value 項目番号を表す整数値。1から5の範囲でなければならない。
 */
@JvmInline
internal value class DiaryItemNumber(val value: Int) : Comparable<DiaryItemNumber> {

    companion object {
        /** 項目番号の最小値。 */
        const val MIN_NUMBER: Int = 1

        /** 項目番号の最大値。 */
        const val MAX_NUMBER: Int = 5
    }

    /**
     * 項目番号が [MIN_NUMBER] かどうかを示す。
     *
     * @return 項目番号が [MIN_NUMBER] の場合は `true`、異なる場合は `false`。
     */
    val isMinNumber get() = value == MIN_NUMBER

    init {
        require(value in MIN_NUMBER..MAX_NUMBER) {
            "項目番号は${MIN_NUMBER}以上、${MAX_NUMBER}以下の値にすること。(value: ${value})"
        }
    }

    /**
     * 現在の項目番号をインクリメントした新しい [DiaryItemNumber] を返す。
     * 結果が [MAX_NUMBER] を超える場合は [IllegalArgumentException] をスローする。
     *
     * @return インクリメントされた [DiaryItemNumber]。
     * @throws IllegalArgumentException インクリメント後の値が [MAX_NUMBER] を超える場合。
     */
    fun inc(): DiaryItemNumber {
        val value = this.value.inc()
        require(value <= MAX_NUMBER)

        return DiaryItemNumber(value)
    }

    /**
     * この [DiaryItemNumber] を別の [DiaryItemNumber] と比較します。
     *
     * @param other 比較対象の [DiaryItemNumber]。
     * @return このオブジェクトが `other` より小さい場合は負の整数、等しい場合はゼロ、大きい場合は正の整数。
     */
    override fun compareTo(other: DiaryItemNumber): Int {
        return this.value.compareTo(other.value)
    }
}
