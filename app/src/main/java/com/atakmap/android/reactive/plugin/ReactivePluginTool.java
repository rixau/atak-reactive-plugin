package com.atakmap.android.reactive.plugin;

import android.content.Context;

import com.atak.plugins.impl.AbstractPluginTool;

public class ReactivePluginTool extends AbstractPluginTool {

    public ReactivePluginTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_name),
                context.getResources().getDrawable(R.drawable.ic_launcher, null),
                "com.atakmap.android.reactive.SHOW_REACTIVE");
    }
}
