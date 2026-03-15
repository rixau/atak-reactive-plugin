package com.atakmap.android.reactive.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.reactive.ReactiveMapComponent;
import gov.tak.api.plugin.IServiceController;

public class ReactivePlugin extends AbstractPlugin {

    public ReactivePlugin(IServiceController serviceController) {
        super(serviceController,
                new ReactivePluginTool(
                        serviceController.getService(PluginContextProvider.class)
                                .getPluginContext()),
                new ReactiveMapComponent());
    }
}
