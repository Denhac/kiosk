package org.denhac.kiosk.events.popup;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.denhac.kiosk.R;
import org.denhac.kiosk.events.meetup.Event;
import org.denhac.kiosk.events.meetup.EventRSVP;
import org.denhac.kiosk.events.meetup.MeetupRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

public class PopupView extends ConstraintLayout implements PopupWindow {

    private View popupView;
    private TextView popupEventTitle;
    private TextView popupEventDescription;
    private AttendeeListView hostListView;
    private AttendeeListView attendeeListView;

    private MeetupRepository meetupRepository;
    private Callback callback;

    private Disposable fetchAttendeesDisposable;
    private Disposable fetchEventInfoDisposable;
    private Event currentEvent;

    public PopupView(Context context) {
        super(context);
        init();
    }

    public PopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        popupView = inflate(getContext(), R.layout.popup_event, this);
        popupEventTitle = popupView.findViewById(R.id.popup_event_title);
        popupEventDescription = popupView.findViewById(R.id.popup_event_description);

        hostListView = findViewById(R.id.hosts_list_view);
        attendeeListView = findViewById(R.id.attendees_list_view);

        ImageView popupExitButton = findViewById(R.id.popup_window_exit_button);
        popupExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupView.setVisibility(View.GONE);
                PopupView.this.callback.onClose();
                currentEvent = null;
            }
        });
    }

    public void setup(MeetupRepository meetupRepository, Callback callback) {
        this.meetupRepository = meetupRepository;
        this.callback = callback;

        hostListView.setMeetupRepository(meetupRepository);
        attendeeListView.setMeetupRepository(meetupRepository);
    }

    @Override
    public void open(Event event) {
        currentEvent = event;
        popupEventTitle.setText(event.getName());

        updateDescription(event.getDescription());

        hostListView.getAttendeeAdapter().clear();
        attendeeListView.getAttendeeAdapter().clear();

        update();

        popupView.setVisibility(View.VISIBLE);

        this.callback.onOpen();
    }

    private void updateDescription(String newDescription) {
        String htmlRemovedDescription = Html.fromHtml(newDescription).toString();
        if(popupEventDescription.getText() != htmlRemovedDescription) {
            popupEventDescription.setText(htmlRemovedDescription);
        }
    }

    public void update() {
        if(currentEvent == null) {
            return;
        }

        if (fetchAttendeesDisposable != null) {
            fetchAttendeesDisposable.dispose();
        }

        fetchAttendeesDisposable = meetupRepository.fetchAttendees(currentEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EventRSVP>>() {
                    @Override
                    public void accept(List<EventRSVP> allAttendees) {
                        List<EventRSVP> hosts = new ArrayList<>();
                        List<EventRSVP> attendees = new ArrayList<>();
                        for (EventRSVP eventRSVP : allAttendees) {
                            if (eventRSVP.isHost() && eventRSVP.isAttending()) {
                                hosts.add(eventRSVP);
                            }

                            if (! eventRSVP.isHost() && eventRSVP.isAttending()) {
                                attendees.add(eventRSVP);
                            }
                        }

                        hostListView.getAttendeeAdapter().setAttendees(hosts);
                        attendeeListView.getAttendeeAdapter().setAttendees(attendees);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e("EXCEPTION", "Exception while fetching attendees", throwable);
                        Log.i("EXCEPTION_MESSAGE", ((HttpException) throwable).response().raw().request().url().toString());
                    }
                });

        if (fetchEventInfoDisposable != null) {
            fetchEventInfoDisposable.dispose();
        }

        fetchEventInfoDisposable = meetupRepository.fetchEvent(currentEvent.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Event>() {
                    @Override
                    public void accept(Event event) {
                        if (popupEventTitle.getText() != event.getName()) {
                            popupEventTitle.setText(event.getName());
                        }

                        updateDescription(event.getDescription());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e("EXCEPTION", "Exception while fetching event info", throwable);
                        Log.i("EXCEPTION_MESSAGE", ((HttpException) throwable).response().raw().request().url().toString());
                    }
                });
    }
}
