package org.denhac.kiosk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.denhac.kiosk.meetup.Event;
import org.denhac.kiosk.popup.PopupWindow;

import java.util.Collections;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {
    private List<Event> events = Collections.emptyList();
    private PopupWindow popupWindow;

    public EventListAdapter(PopupWindow popupWindow) {
        this.popupWindow = popupWindow;
    }

    public void setEvents(List<Event> events) {
        List<Event> oldEvents = this.events;
        this.events = events;

        if(oldEvents.size() != events.size()) {
            notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < oldEvents.size(); i++) {
            if(! oldEvents.get(i).equals(events.get(i))) {
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listView = inflater.inflate(R.layout.calendar_list_item, parent, false);

        // Return a new holder instance
        return new EventListAdapter.ViewHolder(listView, popupWindow);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;
        private final TextView meetingTimeView;
        private final TextView meetingNameView;
        private final TextView meetingYesRSVP;
        private PopupWindow popupWindow;


        public ViewHolder(View itemView, PopupWindow popupWindow) {
            super(itemView);

            this.itemView = itemView;
            meetingTimeView = itemView.findViewById(R.id.meeting_time);
            meetingNameView = itemView.findViewById(R.id.meeting_name);
            meetingYesRSVP = itemView.findViewById(R.id.meeting_yes_rsvp);
            this.popupWindow = popupWindow;
        }

        public void bind(final Event event) {
            meetingTimeView.setText(event.getLocalTime());
            meetingNameView.setText(event.getName());
            String yesRSVPCount = event.getYesRSVPCount();
            meetingYesRSVP.setText(yesRSVPCount + " attendee" + (yesRSVPCount.equals("1") ? "" : "s"));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(view.getContext(), event.getDescription(), Toast.LENGTH_LONG).show();
                    popupWindow.open(event);
                }
            });
        }
    }
}
