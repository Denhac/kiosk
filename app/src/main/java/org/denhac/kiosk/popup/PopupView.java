package org.denhac.kiosk.popup;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.denhac.kiosk.R;
import org.denhac.kiosk.meetup.Event;
import org.denhac.kiosk.meetup.EventRSVP;
import org.denhac.kiosk.meetup.MeetupRepository;

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

        String description = Html.fromHtml(event.getDescription()).toString();
        popupEventDescription.setText(description);

        hostListView.getAttendeeAdapter().clear();
        attendeeListView.getAttendeeAdapter().clear();

        update();

        popupView.setVisibility(View.VISIBLE);

        this.callback.onOpen();
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
                        Log.e("EXCEPTION", "some exception", throwable);
                        Log.i("MESSAGE", ((HttpException) throwable).response().raw().request().url().toString());
                    }
                });
    }
}
