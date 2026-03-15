package com.atakmap.android.reactive;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.reactive.plugin.R;
import com.atakmap.android.reactive.plugin.BuildConfig;
import com.atakmap.coremap.log.Log;

public class ReactiveMapComponent extends DropDownMapComponent {

    private static final String TAG = "ReactiveMapComponent";

    private ReactiveWebViewReceiver webViewReceiver;
    private DevReloadReceiver devReloadReceiver;

    @Override
    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);

        webViewReceiver = new ReactiveWebViewReceiver(view, context);

        DocumentedIntentFilter filter = new DocumentedIntentFilter();
        filter.addAction(ReactiveWebViewReceiver.SHOW_REACTIVE,
                "Show the Reactive plugin WebView");
        registerDropDownReceiver(webViewReceiver, filter);

        if (BuildConfig.DEV_MODE) {
            devReloadReceiver = new DevReloadReceiver(view);
            DocumentedIntentFilter reloadFilter = new DocumentedIntentFilter();
            reloadFilter.addAction(DevReloadReceiver.ACTION_DEV_RELOAD,
                    "Trigger plugin reload (debug only)");
            registerDropDownReceiver(devReloadReceiver, reloadFilter);
            Log.d(TAG, "Dev reload receiver registered");
        }

        Log.d(TAG, "Reactive plugin initialized");
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        if (webViewReceiver != null) {
            webViewReceiver.dispose();
        }
        super.onDestroyImpl(context, view);
    }
}
