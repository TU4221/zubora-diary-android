package com.websarva.wings.android.zuboradiary.data.diary;

// TODO:保留。(使い勝手良いような悪いような・・・)
public class ItemNumber {

    private final int value;
    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 5;

    public ItemNumber(int value) {
        if (value < MIN_NUMBER || value > MAX_NUMBER) throw new IllegalArgumentException();

        this.value = value;
    }

    public ItemNumber incrementNumber() {
        return new ItemNumber(value + 1);
    }

    public ItemNumber decrementNumber() {
        return new ItemNumber(value - 1);
    }

    public int getValue() {
        return value;
    }
}
