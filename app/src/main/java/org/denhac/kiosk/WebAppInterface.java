package org.denhac.kiosk;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    private Context context;
    private Callback callback;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @JavascriptInterface
    public void setupFinished() {
        this.callback.onSetupFinished();
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void showSigningForm(String fieldName) {
        this.callback.showSigningForm(fieldName);
    }

    interface Callback {
        void onSetupFinished();

        void showSigningForm(String fieldName);
    }
}
