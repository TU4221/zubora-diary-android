package com.websarva.wings.android.zuboradiary.data.diary;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ItemNumber implements Serializable, Comparable<ItemNumber> {

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

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ItemNumber)) return false;
        ItemNumber target = (ItemNumber) obj;
        return this.getValue() == target.getValue();
    }

    @Override
    public int compareTo(ItemNumber itemNumber) {
        Objects.requireNonNull(itemNumber);
        return Integer.compare(this.value, itemNumber.value);
    }
}
