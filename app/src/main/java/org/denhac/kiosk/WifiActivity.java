package org.denhac.kiosk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class WifiActivity extends KioskActivity {

    private ImageView qrCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        ImageView homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        qrCodeView = findViewById(R.id.qr_code);

        int smallerDimension = Math.min(qrCodeView.getMinimumWidth(), qrCodeView.getMinimumHeight());

        String qrText = "WIFI:T:WPA;S:denhac;P:denhac rules;;";

        QRGEncoder qrgEncoder = new QRGEncoder(qrText, null, QRGContents.Type.TEXT, smallerDimension);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrCodeView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("FAILED_QR", e.toString());
        }
    }
}
