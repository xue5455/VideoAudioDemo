package com.xue.douyin.permission;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class PermissionLifeCycleImpl implements Application.ActivityLifecycleCallbacks {

    private static final String FRAGMENT_TAG = "permission:fragment";

    private PermissionFragment mFragment;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        FragmentActivity fActivity = (FragmentActivity) activity;
        Fragment fragment = fActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            addFragment(fActivity);
        } else {
            mFragment = (PermissionFragment) fragment;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        FragmentActivity fActivity = (FragmentActivity) activity;
        Fragment fragment = fActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            addFragment(fActivity);
        } else {
            mFragment = (PermissionFragment) fragment;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private void addFragment(FragmentActivity activity) {
        mFragment = new PermissionFragment();
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(mFragment,FRAGMENT_TAG);
        ft.commitAllowingStateLoss();
    }

    public PermissionFragment getFragment(){
        return mFragment;
    }
}
