package org.denhac.kiosk;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;


public class ReleaseFormActivity extends AppCompatActivity {

    private WebView webView;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_form);

        compositeDisposable = new CompositeDisposable();

        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                compositeDisposable.add(Observable
                        .defer(new Callable<ObservableSource<String>>() {
                            @Override
                            public ObservableSource<String> call() throws Exception {
                                return new JavaScriptObservableSource(getAssets(), "js/release.js");
                            }
                        })
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String js) throws Exception {
                                webView.loadUrl(js);
                            }
                        }));
            }
        });
    }

    @Override
    protected void onResume() {
        webView.loadUrl("https://denhac.org/release");

        super.onResume();
    }
}
