package com.atakmap.android.reactive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.webkit.WebViewAssetLoader;

import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.reactive.bridge.AtakBridge;
import com.atakmap.android.reactive.bridge.BridgeEventEmitter;
import com.atakmap.android.reactive.plugin.BuildConfig;
import com.atakmap.coremap.log.Log;

public class ReactiveWebViewReceiver extends DropDownReceiver
        implements OnStateListener {

    public static final String SHOW_REACTIVE = "com.atakmap.android.reactive.SHOW_REACTIVE";
    private static final String TAG = "ReactiveWebView";

    private static final String DEV_URL = "http://localhost:5173";
    private static final String PROD_URL = "https://appassets.androidplatform.net/assets/web/index.html";

    private final Context pluginContext;
    private final LinearLayout container;
    private WebView webView;
    private WebViewAssetLoader assetLoader;
    private AtakBridge bridge;
    private BridgeEventEmitter eventEmitter;

    public ReactiveWebViewReceiver(final MapView mapView,
            final Context context) {
        super(mapView);
        this.pluginContext = context;

        container = new LinearLayout(pluginContext);
        container.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // WebView must be created on the UI thread with the application context
        mapView.post(() -> {
            Context appContext = mapView.getContext();

            webView = new WebView(appContext);
            webView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            // Asset loader for production mode
            assetLoader = new WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/",
                            new WebViewAssetLoader.AssetsPathHandler(appContext))
                    .build();

            configureWebSettings();

            // JS Bridge
            eventEmitter = new BridgeEventEmitter(webView);
            bridge = new AtakBridge(mapView, eventEmitter);
            webView.addJavascriptInterface(bridge, "_atak");

            webView.setWebViewClient(new ReactiveWebViewClient());
            webView.setWebChromeClient(new ReactiveWebChromeClient());

            // Load blank first for consistency on subsequent opens
            webView.loadUrl("about:blank");

            container.addView(webView);
        });
    }

    private void configureWebSettings() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);

        if (BuildConfig.DEV_MODE) {
            // Allow loading from localhost dev server
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(SHOW_REACTIVE)) {
            showDropDown(container, HALF_WIDTH, FULL_HEIGHT,
                    FULL_WIDTH, HALF_HEIGHT, false, this);

            if (BuildConfig.DEV_MODE) {
                // Try dev server first; WebViewClient will fall back to
                // bundled assets if localhost is unreachable
                Log.d(TAG, "Dev mode: trying " + DEV_URL
                        + " (fallback: bundled assets)");
                webView.loadUrl(DEV_URL);
            } else {
                webView.loadUrl(PROD_URL);
            }

            if (eventEmitter != null) {
                eventEmitter.startListening();
            }
        }
    }

    @Override
    public void onDropDownVisible(boolean visible) {
        if (webView != null) {
            if (visible) {
                webView.onResume();
            } else {
                webView.onPause();
            }
        }
    }

    @Override
    public void onDropDownClose() {
        if (eventEmitter != null) {
            eventEmitter.stopListening();
        }
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void disposeImpl() {
        if (eventEmitter != null) {
            eventEmitter.stopListening();
        }
        if (bridge != null) {
            bridge.dispose();
        }
        if (webView != null) {
            webView.destroy();
        }
    }

    public void dispose() {
        disposeImpl();
    }

    private class ReactiveWebViewClient extends WebViewClient {
        private boolean devFallbackTriggered = false;

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                WebResourceRequest request) {
            // Serve from bundled assets when not using the dev server
            if (assetLoader != null) {
                WebResourceResponse response = assetLoader
                        .shouldInterceptRequest(request.getUrl());
                if (response != null) {
                    return response;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                WebResourceError error) {
            // In dev mode, if localhost fails, fall back to bundled assets
            if (BuildConfig.DEV_MODE && !devFallbackTriggered
                    && request.isForMainFrame()) {
                devFallbackTriggered = true;
                Log.w(TAG, "Dev server unreachable, falling back to "
                        + "bundled assets");
                view.loadUrl(PROD_URL);
                return;
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "Loaded: " + url);
            // Reset fallback flag so reopening the dropdown retries dev server
            if (url.equals(PROD_URL) || url.startsWith(DEV_URL)) {
                devFallbackTriggered = false;
            }
            super.onPageFinished(view, url);
        }
    }

    private static class ReactiveWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage msg) {
            String level;
            switch (msg.messageLevel()) {
                case ERROR:
                    level = "ERROR";
                    break;
                case WARNING:
                    level = "WARN";
                    break;
                default:
                    level = "LOG";
            }
            Log.d(TAG, "[JS " + level + "] " + msg.message()
                    + " (" + msg.sourceId() + ":" + msg.lineNumber() + ")");
            return true;
        }
    }
}
