package org.denhac.kiosk;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.denhac.kiosk.events.EventsActivity;

public class MainActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Turn wifi on
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi

        ConstraintLayout eventsGroup = findViewById(R.id.events_group);
        eventsGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                startActivity(intent);
            }
        });

        ConstraintLayout newMemberGroup = findViewById(R.id.new_member_group);
        newMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = WebViewActivity.newIntent(getApplicationContext(),
                        "https://denhac.org/membership");
                startActivity(intent);
            }
        });

        ConstraintLayout waiverGroup = findViewById(R.id.waiver_group);
        waiverGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = WebViewActivity.newIntent(getApplicationContext(),
                        "https://denhac.org/release");
                startActivity(intent);
            }
        });

        ConstraintLayout helpGroup = findViewById(R.id.help_group);
        helpGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = WebViewActivity.newIntent(getApplicationContext(),
                        "https://denhac.org/howto");
                startActivity(intent);
            }
        });

        ConstraintLayout wifiGroup = findViewById(R.id.wifi_group);
        wifiGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WifiActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "kiosk mode", Toast.LENGTH_LONG).show();
    }

    @Override
    protected long getNoInteractionTimeout() {
        // Really, we never want this activity to exit. Hopefully this is long enough but a bunch of
        // engineers were wrong before about how long their code would last and then we got Y2K.
        // Note: shouldAllowActivityExit being false should make this not matter, but I still like
        // the comment so it stays
        return Long.MAX_VALUE - 1;
    }

    @Override
    protected boolean shouldAllowActivityExit() {
        return false;
    }
}
