package com.websarva.wings.android.zuboradiary.ui;

import android.util.Log;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.websarva.wings.android.zuboradiary.R;

public class ChangeFragment {
    public static void addFragment(
            @NonNull FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            @IdRes int containerViewId,
            @NonNull Class<? extends androidx.fragment.app.Fragment> fragmentClass,
            @Nullable android.os.Bundle args
    ) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setReorderingAllowed(true);
        if (needsAdditionTOBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.add(containerViewId, fragmentClass, args);
        transaction.commit();
    }

    public static void replaceFragment(
            @NonNull FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            @IdRes int containerViewId,
            @NonNull Class<? extends androidx.fragment.app.Fragment> fragmentClass,
            @Nullable android.os.Bundle args
    ) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setReorderingAllowed(true);
        if (needsAdditionTOBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(containerViewId, fragmentClass, args);
        transaction.commit();
    }

    public static void removeFragment(
            @NonNull FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            @NonNull Fragment fragment
    ) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setReorderingAllowed(true);
        if (needsAdditionTOBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.remove(fragment);
        transaction.commit();
    }

    // FrontFragmentContainer用popBackStackメソッド
    // HACK:NavHostFragmentのレイアウト設定でapp:defaultNavHost="true"と設定することにより、
    //      popBackStackメソッド等で対象となるバックスタックが優先的にNavHostFragmentのバックスタックとなる。
    //      これにより、NavHostFragmentのタブ切替後、同階層のFragmentContainerViewにフラグメントを追加し、
    //      FragmentManager#popBackStackを行うとFragmentContainerViewに追加したフラグメントが
    //      削除されるのではなく、NavHostFragmentのみがタブ切替前の状態に戻ってしまう。
    //      これの対策として、下記プログラムを記述した。
    //      app:defaultNavHost="false"にするだけで解決するかと思ったが、
    //      その場合、NavHostFragmentのバックスタック対象のpopBackStackが機能しなかった(しっかり検証してない)ので、
    //      暫定として下記対応をとった。
    public static void popBackStackOnFrontFragment(@NonNull FragmentManager manager, Boolean popAllBackStack) {
        FragmentTransaction firstTransaction = manager.beginTransaction();
        firstTransaction.setPrimaryNavigationFragment(
                manager.findFragmentById(R.id.front_fragmentContainerView_activity_main)
        );
        firstTransaction.commit();
        manager.executePendingTransactions();

        // MEMO:全てのバックスタックをポップする場合は、
        //      それまでのフラグメント処理(add,replace,remove)をバックスタックに追加する必要あり。
        if (popAllBackStack) {
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            manager.popBackStack();
        }

        FragmentTransaction secondTransaction = manager.beginTransaction();
        secondTransaction.setPrimaryNavigationFragment(
                manager.findFragmentById(R.id.nav_host_fragment_activity_main)
        );
        secondTransaction.commit();
    }

    public static void popAllBackStackOnFrontFragment(@NonNull FragmentManager manager) {
        FragmentTransaction firstTransaction = manager.beginTransaction();
        firstTransaction.setPrimaryNavigationFragment(
                manager.findFragmentById(R.id.front_fragmentContainerView_activity_main)
        );
        firstTransaction.commit();
        manager.executePendingTransactions();

        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction secondTransaction = manager.beginTransaction();

        secondTransaction.setPrimaryNavigationFragment(
                manager.findFragmentById(R.id.nav_host_fragment_activity_main)
        );
        secondTransaction.commit();
    }

}
