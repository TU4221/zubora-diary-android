package com.websarva.wings.android.zuboradiary.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ChangeFragment {
    public static void addFragment(
            FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            int containerViewId,
            Class<? extends androidx.fragment.app.Fragment> fragmentClass,
            android.os.Bundle args
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
            FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            int containerViewId,
            Class<? extends androidx.fragment.app.Fragment> fragmentClass,
            android.os.Bundle args
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
            FragmentManager manager,
            Boolean needsAdditionTOBackStack,
            Fragment fragment
    ) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setReorderingAllowed(true);
        if (needsAdditionTOBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.remove(fragment);
        transaction.commit();
    }

}
