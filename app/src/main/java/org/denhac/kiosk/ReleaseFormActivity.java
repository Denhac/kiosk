package org.denhac.kiosk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class ReleaseFormActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_form);

        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        WebChromeClient client = new WebChromeClient();
        webView.setWebChromeClient(client);
    }

    @Override
    protected void onResume() {
        webView.loadUrl("https://denhac.org/release");

        super.onResume();
    }
}
