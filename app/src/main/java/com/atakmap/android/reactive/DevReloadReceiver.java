package com.atakmap.android.reactive;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.atak.plugins.impl.AtakPluginRegistry;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

/**
 * Debug-only broadcast receiver that triggers plugin unload/reload.
 *
 * Usage: adb shell am broadcast -a com.atakmap.android.reactive.DEV_RELOAD
 *
 * Uses a delayed runnable so the receiver's onReceive() returns before
 * the classloader is torn down. This avoids the self-destruct problem
 * where unloading destroys the classloader that's currently executing.
 */
public class DevReloadReceiver extends DropDownReceiver {

    public static final String ACTION_DEV_RELOAD =
            "com.atakmap.android.reactive.DEV_RELOAD";

    private static final String TAG = "DevReloadReceiver";
    private static final String PLUGIN_PACKAGE =
            "com.atakmap.android.reactive.plugin";
    private static final long RELOAD_DELAY_MS = 500;

    public DevReloadReceiver(MapView mapView) {
        super(mapView);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || !action.equals(ACTION_DEV_RELOAD)) return;

        Log.d(TAG, "Reload requested, scheduling unload/reload in "
                + RELOAD_DELAY_MS + "ms");

        // Post with delay so onReceive() returns before classloader teardown
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                AtakPluginRegistry registry = AtakPluginRegistry.get();
                if (registry == null) {
                    Log.e(TAG, "Plugin registry not available");
                    return;
                }

                Log.d(TAG, "Unloading plugin: " + PLUGIN_PACKAGE);
                boolean unloaded = registry.unloadPlugin(PLUGIN_PACKAGE);
                Log.d(TAG, "Unload result: " + unloaded);

                Log.d(TAG, "Loading plugin: " + PLUGIN_PACKAGE);
                boolean loaded = registry.loadPlugin(PLUGIN_PACKAGE);
                Log.d(TAG, "Load result: " + loaded);

            } catch (Exception e) {
                Log.e(TAG, "Error during plugin reload", e);
            }
        }, RELOAD_DELAY_MS);
    }

    @Override
    public void disposeImpl() {
    }
}
