package com.printzone.presscost;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AndroidBridge {
    private final Activity activity;

    public AndroidBridge(Activity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void share(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, text);
                activity.startActivity(Intent.createChooser(i, "Share"));
            }
        });
    }

    @JavascriptInterface
    public String getClipboard() {
        final String[] out = new String[]{""};
        final CountDownLatch latch = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null && cm.hasPrimaryClip()) {
                        ClipData d = cm.getPrimaryClip();
                        if (d != null && d.getItemCount() > 0) {
                            CharSequence t = d.getItemAt(0).coerceToText(activity);
                            if (t != null) out[0] = t.toString();
                        }
                    }
                } catch (Exception e) {
                    out[0] = "";
                }
                latch.countDown();
            }
        });
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        return out[0];
    }
}
