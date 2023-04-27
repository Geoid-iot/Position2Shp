package com.example.position2shp.MapLocationListener;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import javax.security.auth.callback.Callback;

public class MapLocationListener implements DefaultLifecycleObserver {
    private boolean enabled = false;
    private Lifecycle lmLifecycle;
    private Context mContext;
    private Activity mActivity;

    public MapLocationListener(Context context, Lifecycle lifecycle, Activity activity, Callback callback) {
        lmLifecycle = lifecycle;
        mContext = context;
        mActivity = activity;
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        if (enabled) {
            // connect
        }
    }

    public void enable() {
        enabled = true;
        if (lmLifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            // connect if not connected
        }
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        // disconnect if connected
    }



}
