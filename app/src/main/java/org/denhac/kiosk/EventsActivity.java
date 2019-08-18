package org.denhac.kiosk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventsActivity extends AppCompatActivity {

    private TextView monthText;
    private Calendar currentTimestamp;
    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        currentTimestamp = Calendar.getInstance();
        monthText = findViewById(R.id.month_title_bar);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler_view);

        calendarAdapter = new CalendarAdapter(currentTimestamp);
        recyclerView.setAdapter(calendarAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        recyclerView.setLayoutManager(layoutManager);

        ImageView previousMonth = findViewById(R.id.previous_month);
        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTimestamp.add(Calendar.MONTH, -1);
                updateView();
            }
        });

        ImageView nextMonth = findViewById(R.id.next_month);
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTimestamp.add(Calendar.MONTH, 1);
                updateView();
            }
        });

        updateView();
    }

    private void updateView() {
        calendarAdapter.setTimestamp(currentTimestamp);
        if(monthText != null) {
            String monthName = currentTimestamp.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            int year = currentTimestamp.get(Calendar.YEAR);
            monthText.setText(monthName + " " + year);
        }
    }
}
