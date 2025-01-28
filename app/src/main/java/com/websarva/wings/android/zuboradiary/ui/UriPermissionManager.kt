package com.websarva.wings.android.zuboradiary.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public abstract class UriPermissionManager {

    private final ContentResolver resolver;

    public UriPermissionManager(Context context) {
        Objects.requireNonNull(context);

        resolver = context.getContentResolver();
    }

    public void takePersistablePermission(Uri uri){
        Objects.requireNonNull(uri);

        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
        // 対処できないがアプリを落としたくない為、catchのみの処理とする。
        }
    }

    /**
     * 対象Uriが他で使用されていないかを確認するコードを記述すること。権限解放時、このメソッドが処理される。
     * */
    public abstract boolean checkUsedUriDoesNotExist(@NonNull Uri uri);

    // MEMO:Uri先のファイルを削除すると、登録されていたUriPermissionも同時に削除される。
    public void releasePersistablePermission(Uri uri) {
        Objects.requireNonNull(uri);

        List<UriPermission> permissionList = resolver.getPersistedUriPermissions();
        for (UriPermission uriPermission: permissionList) {
            Uri permissionedUri = uriPermission.getUri();
            Objects.requireNonNull(permissionedUri);
            String permissionedUriString = permissionedUri.toString();
            String targetUriString = uri.toString();

            if (permissionedUriString.equals(targetUriString)) {
                if (!checkUsedUriDoesNotExist(uri)) return;

                resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return;
            }
        }
    }

    public void releaseAllPersistablePermission() {
        List<UriPermission> permissionList = resolver.getPersistedUriPermissions();
        for (UriPermission uriPermission: permissionList) {
            Uri uri = uriPermission.getUri();
            Objects.requireNonNull(uri);
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
}
