package org.denhac.kiosk.popup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.denhac.kiosk.meetup.MeetupRepository;

public class AttendeeListView extends RecyclerView {
    private AttendeeAdapter attendeeAdapter;

    public AttendeeListView(@NonNull Context context) {
        super(context);
        init();
    }

    public AttendeeListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AttendeeListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setMeetupRepository(MeetupRepository meetupRepository) {
        attendeeAdapter = new AttendeeAdapter(meetupRepository);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 5);

        setAdapter(attendeeAdapter);
        setLayoutManager(layoutManager);
    }

    public AttendeeAdapter getAttendeeAdapter() {
        return attendeeAdapter;
    }
}
