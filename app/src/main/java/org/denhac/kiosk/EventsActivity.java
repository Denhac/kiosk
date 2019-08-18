package org.denhac.kiosk;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EventsActivity extends AppCompatActivity {

    private TextView monthText;
    private Calendar currentTimestamp;
    private CalendarAdapter calendarAdapter;
    private MeetupRepository meetupRepository;
    private Disposable intervalDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Get the current day/time but set it to the first day of the month to avoid issues when switching months
        currentTimestamp = Calendar.getInstance();
        currentTimestamp.set(Calendar.DAY_OF_MONTH, currentTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
        // TODO just for debug
//        currentTimestamp.set(Calendar.MONTH, 2);

        monthText = findViewById(R.id.month_title_bar);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler_view);

        meetupRepository = new MeetupRepository();

        calendarAdapter = new CalendarAdapter(meetupRepository, (Calendar) currentTimestamp.clone());
        recyclerView.setAdapter(calendarAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        recyclerView.setLayoutManager(layoutManager);

        ImageView previousMonth = findViewById(R.id.previous_month);
        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousMonth();
            }
        });

        ImageView nextMonth = findViewById(R.id.next_month);
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextMonth();
            }
        });

        updateView();

        intervalDisposable = Observable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long unused) {
                        // TODO handle day change
                        meetupRepository.fetchEventsForYear(currentTimestamp.get(Calendar.YEAR));
                        updateView();
                    }
                });
    }

    private void goToNextMonth() {
        currentTimestamp.add(Calendar.MONTH, 1);
        updateView();
    }

    private void goToPreviousMonth() {
        currentTimestamp.add(Calendar.MONTH, -1);
        updateView();
    }

    private void updateView() {
        calendarAdapter.setTimestamp((Calendar) currentTimestamp.clone());
        if(monthText != null) {
            String monthName = currentTimestamp.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            int year = currentTimestamp.get(Calendar.YEAR);
            monthText.setText(monthName + " " + year);
        }
    }
}
