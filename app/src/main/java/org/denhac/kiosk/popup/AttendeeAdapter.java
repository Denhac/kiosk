package org.denhac.kiosk.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.denhac.kiosk.R;
import org.denhac.kiosk.meetup.EventAttendee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private List<EventAttendee> attendees;

    AttendeeAdapter() {
        attendees = Collections.emptyList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View attendeeItemView = inflater.inflate(R.layout.event_attendee_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(attendeeItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(attendees.get(position));
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    public void setAttendees(List<EventAttendee> attendees) {
        this.attendees = attendees;
        notifyDataSetChanged();
    }

    public void clear() {
        setAttendees(new ArrayList<EventAttendee>());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView attendeeNameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            attendeeNameTextView = itemView.findViewById(R.id.attendee_name);
        }

        void bind(EventAttendee eventAttendee) {
            attendeeNameTextView.setText(eventAttendee.getName());
        }
    }
}
