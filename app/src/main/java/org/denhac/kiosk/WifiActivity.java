package org.denhac.kiosk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class WifiActivity extends AppCompatActivity {

    private ImageView qrCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        qrCodeView = findViewById(R.id.qr_code);

        int smallerDimension = Math.min(qrCodeView.getMinimumWidth(), qrCodeView.getMinimumHeight());
        Log.i("DIM", String.valueOf(smallerDimension));

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
