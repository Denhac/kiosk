package org.denhac.kiosk;

import android.os.Bundle;
import android.text.format.DateUtils;
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
    private Calendar currentDay;
    private CalendarAdapter calendarAdapter;
    private MeetupRepository meetupRepository;
    private Disposable intervalDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        currentDay = Calendar.getInstance();

        monthText = findViewById(R.id.month_title_bar);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler_view);

        meetupRepository = new MeetupRepository();

        calendarAdapter = new CalendarAdapter(meetupRepository, (Calendar) currentDay.clone());
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

        updateView(currentDay);

        intervalDisposable = Observable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long unused) {
                        currentDay = Calendar.getInstance();
                        if(!DateUtils.isToday(currentDay.getTimeInMillis())) {
                            updateView(currentDay);
                            calendarAdapter.notifyDataSetChanged();
                        }
                        meetupRepository.fetchEventsForYear(currentDay.get(Calendar.YEAR));
                        updateView(currentDay);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        intervalDisposable.dispose();
    }

    private void goToNextMonth() {
        Calendar monthTimestamp = (Calendar) currentDay.clone();
        monthTimestamp.set(Calendar.DAY_OF_MONTH, monthTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
        monthTimestamp.add(Calendar.MONTH, 1);
        updateView(monthTimestamp);
    }

    private void goToPreviousMonth() {
        Calendar monthTimestamp = (Calendar) currentDay.clone();
        monthTimestamp.set(Calendar.DAY_OF_MONTH, monthTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
        monthTimestamp.add(Calendar.MONTH, 1);
        updateView(monthTimestamp);
    }

    private void updateView(Calendar monthTimestamp) {
        monthTimestamp = (Calendar) monthTimestamp.clone();
        monthTimestamp.set(Calendar.DAY_OF_MONTH, monthTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));

        calendarAdapter.setTimestamp(monthTimestamp);
        if (monthText != null) {
            String monthName = monthTimestamp.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            int year = monthTimestamp.get(Calendar.YEAR);
            monthText.setText(monthName + " " + year);
        }
    }
}
