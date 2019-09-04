package org.denhac.kiosk.events;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import org.denhac.kiosk.R;
import org.denhac.kiosk.events.meetup.MeetupRepository;
import org.denhac.kiosk.events.popup.PopupView;
import org.denhac.kiosk.events.popup.PopupWindow;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EventsActivity extends AppCompatActivity implements MeetupRepository.NetworkStatus, PopupWindow.Callback {

    private TextView monthText;
    private Calendar currentDay;
    private Calendar currentViewedMonth;
    private CalendarAdapter calendarAdapter;
    private MeetupRepository meetupRepository;
    private ImageView previousMonth;
    private ImageView nextMonth;
    private CalendarView calendarView;
    private TextView offlineTextView;

    private Disposable intervalDisposable;
    private PopupView popupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        currentDay = Calendar.getInstance();
        currentViewedMonth = (Calendar) currentDay.clone();
        currentViewedMonth.set(Calendar.DAY_OF_MONTH, currentViewedMonth.getActualMinimum(Calendar.DAY_OF_MONTH));

        monthText = findViewById(R.id.month_title_bar);
        calendarView = findViewById(R.id.calendar_recycler_view);

        meetupRepository = new MeetupRepository(this);

        popupView = findViewById(R.id.popup_window);
        popupView.setup(meetupRepository, this);

        calendarAdapter = new CalendarAdapter(meetupRepository, popupView, (Calendar) currentViewedMonth.clone());
        calendarView.setAdapter(calendarAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarView.setLayoutManager(layoutManager);

        previousMonth = findViewById(R.id.previous_month);
        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousMonth();
            }
        });

        nextMonth = findViewById(R.id.next_month);
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextMonth();
            }
        });

        offlineTextView = findViewById(R.id.offline_text);

        updateView();

        intervalDisposable = getUpdateInterval();
    }

    private Disposable getUpdateInterval() {
        return Observable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long unused) {
                        if (!DateUtils.isToday(currentDay.getTimeInMillis())) {
                            Calendar newDay = Calendar.getInstance();
                            boolean currentDayIsViewable = currentDay.get(Calendar.YEAR) == currentViewedMonth.get(Calendar.YEAR) &&
                                    currentDay.get(Calendar.MONTH) == currentViewedMonth.get(Calendar.MONTH);
                            boolean newDayIsViewable = newDay.get(Calendar.YEAR) == currentViewedMonth.get(Calendar.YEAR) &&
                                    newDay.get(Calendar.MONTH) == currentViewedMonth.get(Calendar.MONTH);
                            if (currentDayIsViewable || newDayIsViewable) {
                                currentDay = newDay;
                                currentViewedMonth = (Calendar) currentDay.clone();
                                currentViewedMonth.set(Calendar.DAY_OF_MONTH, currentViewedMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
                                updateView();
                                calendarAdapter.notifyDataSetChanged();
                            }
                        }

                        meetupRepository.fetchEventsForYear(currentDay.get(Calendar.YEAR));

                        popupView.update();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        intervalDisposable.dispose();
    }

    private void goToNextMonth() {
        currentViewedMonth.add(Calendar.MONTH, 1);
        updateView();
    }

    private void goToPreviousMonth() {
        currentViewedMonth.add(Calendar.MONTH, -1);
        updateView();
    }

    private void updateView() {
        calendarAdapter.setCurrentlyViewedMonth((Calendar) currentViewedMonth.clone());
        if (monthText != null) {
            String monthName = currentViewedMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            int year = currentViewedMonth.get(Calendar.YEAR);
            monthText.setText(monthName + " " + year);
        }
    }

    @Override
    public void notifyStatus(final boolean online) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (online) {
                    offlineTextView.setVisibility(View.INVISIBLE);
                } else {
                    offlineTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onOpen() {
        previousMonth.setClickable(false);
        nextMonth.setClickable(false);
        calendarView.setTouchEnabled(false);
    }

    @Override
    public void onClose() {
        previousMonth.setClickable(true);
        nextMonth.setClickable(true);
        calendarView.setTouchEnabled(true);
    }
}
