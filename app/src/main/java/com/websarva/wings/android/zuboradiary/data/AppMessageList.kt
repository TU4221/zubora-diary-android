package com.websarva.wings.android.zuboradiary.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppMessageList {

    private final List<AppMessage> appMessageList;

    public AppMessageList(List<AppMessage> appMessageList) {
        Objects.requireNonNull(appMessageList);
        appMessageList.stream().forEach(Objects::requireNonNull);

        this.appMessageList = Collections.unmodifiableList(appMessageList);
    }

    public AppMessageList() {
        this.appMessageList = new ArrayList<>();
    }

    public boolean isEmpty() {
        return appMessageList.isEmpty();
    }

    @NonNull
    public AppMessageList add(AppMessage appMessage) {
        Objects.requireNonNull(appMessage);

        List<AppMessage> resultList = new ArrayList<>(appMessageList);
        resultList.add(appMessage);
        return new AppMessageList(resultList);
    }

    @NonNull
    public AppMessageList removeFirstItem() {
        List<AppMessage> resultList = new ArrayList<>(appMessageList);
        if (!resultList.isEmpty()) resultList.remove(0);
        return new AppMessageList(resultList);
    }

    public boolean equalLastItem(AppMessage appMessage) {
        Objects.requireNonNull(appMessage);
        if (appMessageList.isEmpty()) return false;

        int lastPosition = appMessageList.size() - 1;
        AppMessage lastAppMessage = appMessageList.get(lastPosition);
        return lastAppMessage.equals(appMessage);
    }

    @Nullable
    public AppMessage findFirstItem() {
        if (appMessageList.isEmpty()) return null;
        return appMessageList.get(0);
    }
}
