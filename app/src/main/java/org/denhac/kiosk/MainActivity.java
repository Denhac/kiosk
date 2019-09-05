package org.denhac.kiosk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.denhac.kiosk.events.EventsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    protected void onPause() {
        super.onPause();
    }
}
