package org.denhac.kiosk;

import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import org.denhac.kiosk.meetup.Event;
import org.denhac.kiosk.meetup.EventAttendee;
import org.denhac.kiosk.meetup.MeetupRepository;
import org.denhac.kiosk.popup.AttendeeListView;
import org.denhac.kiosk.popup.PopupWindowManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EventsActivity extends AppCompatActivity implements MeetupRepository.NetworkStatus {

    private TextView monthText;
    private Calendar currentDay;
    private Calendar currentViewedMonth;
    private CalendarAdapter calendarAdapter;
    private MeetupRepository meetupRepository;
    private ImageView previousMonth;
    private ImageView nextMonth;
    private CalendarView calendarView;
    private TextView offlineTextView;

    private PopupWindowManager popupWindowManager;
    private ConstraintLayout popupWindow;
    private AttendeeListView hostListView;
    private AttendeeListView attendeeListView;
    private ImageView popupExitButton;
    private TextView popupEventTitle;
    private TextView popupEventDescription;

    private Disposable intervalDisposable;
    private Disposable fetchAttendeesDisposable;

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

        offlineTextView = findViewById(R.id.offline_text);

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

        hostListView = findViewById(R.id.hosts_list_view);
        attendeeListView = findViewById(R.id.attendees_list_view);

        hostListView.setMeetupRepository(meetupRepository);
        attendeeListView.setMeetupRepository(meetupRepository);

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
        fetchAttendeesDisposable.dispose();
    }

    private PopupWindowManager getPopupWindowManager() {
        return new PopupWindowManager() {
            @Override
            public void open(final Event event) {
                popupEventTitle.setText(event.getName());
                String description = Html.fromHtml(event.getDescription()).toString();
                popupEventDescription.setText(description);

                previousMonth.setClickable(false);
                nextMonth.setClickable(false);
                calendarView.setTouchEnabled(false);

                if (fetchAttendeesDisposable != null) {
                    fetchAttendeesDisposable.dispose();
                }

                hostListView.getAttendeeAdapter().clear();
                attendeeListView.getAttendeeAdapter().clear();

                fetchAttendeesDisposable = meetupRepository.fetchAttendees(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<EventAttendee>>() {
                            @Override
                            public void accept(List<EventAttendee> allAttendees) {
                                List<EventAttendee> hosts = new ArrayList<>();
                                List<EventAttendee> attendees = new ArrayList<>();
                                for (EventAttendee eventAttendee : allAttendees) {
                                    if (eventAttendee.isHost() && eventAttendee.isAttending()) {
                                        hosts.add(eventAttendee);
                                    }

                                    if (! eventAttendee.isHost() && eventAttendee.isAttending()) {
                                        attendees.add(eventAttendee);
                                    }
                                }

                                hostListView.getAttendeeAdapter().setAttendees(hosts);
                                attendeeListView.getAttendeeAdapter().setAttendees(attendees);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("EXCEPTION", "some exception", throwable);
                            }
                        });

                popupWindow.setVisibility(View.VISIBLE);
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
}
