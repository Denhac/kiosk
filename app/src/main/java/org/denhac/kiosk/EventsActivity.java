package org.denhac.kiosk;

import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    private Calendar currentViewedMonth;
    private CalendarAdapter calendarAdapter;
    private MeetupRepository meetupRepository;
    private Disposable intervalDisposable;
    private ImageView previousMonth;
    private ImageView nextMonth;
    private CalendarView calendarView;

    private PopupWindowManager popupWindowManager;
    private ConstraintLayout popupWindow;
    private ImageView popupExitButton;
    private TextView popupEventTitle;
    private TextView popupEventDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        currentDay = Calendar.getInstance();
        currentViewedMonth = (Calendar) currentDay.clone();
        currentViewedMonth.set(Calendar.DAY_OF_MONTH, currentViewedMonth.getActualMinimum(Calendar.DAY_OF_MONTH));

        monthText = findViewById(R.id.month_title_bar);
        calendarView = findViewById(R.id.calendar_recycler_view);

        meetupRepository = new MeetupRepository();

        popupWindow = findViewById(R.id.popup_window);
        popupWindowManager = getPopupWindowManager();

        calendarAdapter = new CalendarAdapter(meetupRepository, popupWindowManager, (Calendar) currentViewedMonth.clone());
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

        popupExitButton = findViewById(R.id.popup_window_exit_button);
        popupExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.setVisibility(View.GONE);
                previousMonth.setClickable(true);
                nextMonth.setClickable(true);
                calendarView.setTouchEnabled(true);
            }
        });

        popupEventDescription = findViewById(R.id.popup_event_description);

        popupEventTitle = findViewById(R.id.popup_event_title);

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
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        intervalDisposable.dispose();
    }

    private PopupWindowManager getPopupWindowManager() {
        return new PopupWindowManager() {
            @Override
            public void open(Event e) {
                popupEventTitle.setText(e.getName());
                String description = Html.fromHtml(e.getDescription()).toString();
                popupEventDescription.setText(description);

                popupWindow.setVisibility(View.VISIBLE);
                previousMonth.setClickable(false);
                nextMonth.setClickable(false);
                calendarView.setTouchEnabled(false);
            }
        };
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
}
