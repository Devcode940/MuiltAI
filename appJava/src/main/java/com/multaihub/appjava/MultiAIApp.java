package com.multaihub.appjava;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

/**
 * Application entry point for the Java-only variant of MultiAI.
 *
 * Configures WebView-level security features that must be applied before any
 * WebView instance is created in the process.
 */
public final class MultiAIApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebView.enableSlowWholeDocumentDraw();
        }
    }
}
