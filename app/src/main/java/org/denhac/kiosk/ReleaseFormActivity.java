package org.denhac.kiosk;

import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class ReleaseFormActivity extends AppCompatActivity implements WebAppInterface.Callback {

    private WebView webView;
    private TextView pleaseWait;

    private ConstraintLayout signForm;
    private Signature signBox;
    private TextView buttonClear;
    private TextView buttonConfirm;

    private CompositeDisposable compositeDisposable;

    private boolean webViewTouchesEnabled = true;

    private Map<String, Path> signatures;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_form);

        signatures = new HashMap<>();

        compositeDisposable = new CompositeDisposable();

        webView = findViewById(R.id.web_view);
        pleaseWait = findViewById(R.id.please_wait_text);

        signForm = findViewById(R.id.sign_form);
        signBox = findViewById(R.id.sign_box);

        buttonClear = findViewById(R.id.sign_clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signBox.clear();
            }
        });

        buttonConfirm = findViewById(R.id.sign_confirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signForm.setVisibility(View.INVISIBLE);
                webViewTouchesEnabled = true;
                final String fieldName = signBox.getFieldName();
                signatures.put(signBox.getFieldName(), signBox.getPath());

                Log.i("HERE", "I got here");

                Disposable disposable = signBox.getBase64()
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                String javascript = "javascript:(function() {" +
                                        "document.querySelector('[name=\"" + fieldName + "\"')" +
                                        ".value = 'data:image/png;base64," + s + "';" +
                                        "})()";
                                Log.i("LENGTH", String.valueOf(s.length()));
                                webView.loadUrl(javascript);
                            }
                        });
                compositeDisposable.add(disposable);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(this, this), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                compositeDisposable.add(Observable
                        .defer(new Callable<ObservableSource<String>>() {
                            @Override
                            public ObservableSource<String> call() {
                                return new JavaScriptObservableSource(getAssets(), "js/release.js");
                            }
                        })
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String js) {
                                webView.loadUrl(js);
                            }
                        }));
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !webViewTouchesEnabled;
            }
        });
    }

    @Override
    protected void onResume() {
        webView.loadUrl("https://denhac.org/release");

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onSetupFinished() {
        runOnUiThread(new Runnable() {

            public void run() {
                pleaseWait.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showSigningForm(final String fieldName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                signBox.setFieldName(fieldName);

                if (signatures.containsKey(fieldName)) {
                    signBox.setPath(signatures.get(fieldName));
                } else {
                    signBox.setPath(new Path());
                }

                signForm.setVisibility(View.VISIBLE);
                webViewTouchesEnabled = false;
            }
        });
        // Update the signature text field with whatever base64 image gets created with the signature
        // Change the DOM to show a smaller version of the signature next to the button
    }
}
