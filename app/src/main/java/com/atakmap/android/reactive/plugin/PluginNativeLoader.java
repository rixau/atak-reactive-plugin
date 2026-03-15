package com.atakmap.android.reactive.plugin;

import android.content.Context;

import java.io.File;

public class PluginNativeLoader {

    private static String ndl = null;

    synchronized static void init(final Context context) {
        if (ndl == null) {
            final String nativeDir = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 0)
                    .nativeLibraryDir;
            ndl = nativeDir;
        }
    }

    public static String getNativeLibraryDir() {
        return ndl;
    }

    public static void loadLibrary(final String name) {
        if (ndl != null) {
            final File lib = new File(ndl, "lib" + name + ".so");
            if (lib.exists()) {
                System.load(lib.getAbsolutePath());
                return;
            }
        }
        System.loadLibrary(name);
    }
}
