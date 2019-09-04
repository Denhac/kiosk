package org.denhac.kiosk;

import android.content.res.AssetManager;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;

public class JavaScriptObservableSource implements ObservableSource<String> {

    private AssetManager assetManager;
    private final String filename;

    JavaScriptObservableSource(AssetManager assetManager, String filename) {
        this.assetManager = assetManager;
        this.filename = filename;
    }

    @Override
    public void subscribe(Observer<? super String> observer) {
        InputStream input;
        try {
            input = assetManager.open(filename);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);

            String javascript = "javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(script)" +
                    "})()";
            observer.onNext(javascript);

            observer.onComplete();
        } catch (IOException e) {
            observer.onError(e);
        }
    }
}