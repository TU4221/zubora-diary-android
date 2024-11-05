package com.websarva.wings.android.zuboradiary.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppErrorList {

    private final List<AppError> appErrorList;

    public AppErrorList(List<AppError> appErrorList) {
        Objects.requireNonNull(appErrorList);
        appErrorList.stream().forEach(Objects::requireNonNull);

        this.appErrorList = Collections.unmodifiableList(appErrorList);
    }

    public AppErrorList() {
        this.appErrorList = new ArrayList<>();
    }

    public boolean isEmpty() {
        return appErrorList.isEmpty();
    }

    @NonNull
    public AppErrorList addAppError(AppError appError) {
        Objects.requireNonNull(appError);

        List<AppError> resultList = new ArrayList<>(appErrorList);
        resultList.add(appError);
        return new AppErrorList(resultList);
    }

    @NonNull
    public AppErrorList removeFirstAppError() {
        List<AppError> resultList = new ArrayList<>(appErrorList);
        if (!resultList.isEmpty()) resultList.remove(0);
        return new AppErrorList(resultList);
    }

    public boolean equalLastAppError(AppError appError) {
        Objects.requireNonNull(appError);
        if (appErrorList.isEmpty()) return false;

        int lastPosition = appErrorList.size() - 1;
        AppError lastAppError = appErrorList.get(lastPosition);
        return lastAppError.equals(appError);
    }

    @Nullable
    public AppError findFirstAppError() {
        if (appErrorList.isEmpty()) return null;
        return appErrorList.get(0);
    }
}
