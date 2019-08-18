package org.denhac.kiosk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {
    private List<Event> events = Collections.emptyList();

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listView = inflater.inflate(R.layout.calendar_list_item, parent, false);

        // Return a new holder instance
        return new EventListAdapter.ViewHolder(listView);
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


        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            meetingTimeView = itemView.findViewById(R.id.meeting_time);
            meetingNameView = itemView.findViewById(R.id.meeting_name);
            meetingYesRSVP = itemView.findViewById(R.id.meeting_yes_rsvp);
        }

        public void bind(final Event event) {
            meetingTimeView.setText(event.getLocalTime());
            meetingNameView.setText(event.getName());
            String yesRSVPCount = event.getYesRSVPCount();
            meetingYesRSVP.setText(yesRSVPCount + " attendee" + (yesRSVPCount == "1" ? "" : "s"));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), event.getDescription(), Toast.LENGTH_LONG).show();;
                }
            });
        }
    }
}
