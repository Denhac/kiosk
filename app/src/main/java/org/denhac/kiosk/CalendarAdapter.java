package org.denhac.kiosk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DAY = 1;

    private Calendar currentTimestamp;

    public CalendarAdapter(Calendar currentTimestamp) {
        setTimestamp(currentTimestamp);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView;
        if(viewType == VIEW_TYPE_DAY) {
            contactView = inflater.inflate(R.layout.calendar_item, parent, false);
        } else {
            contactView = inflater.inflate(R.layout.calendar_header, parent, false);
        }

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position < 7) {
            String[] weekdays = new String[]{ "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            holder.bind(true, weekdays[position]);
            return;
        }

        int startPosition = currentTimestamp.get(Calendar.DAY_OF_WEEK) - 1 + 7;
        int stopPosition = startPosition + currentTimestamp.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
        if(position >= startPosition && position <= stopPosition) {
            int dayNumber = position - startPosition + 1;
            holder.bind(true, String.valueOf(dayNumber));
        } else {
            holder.bind(false, "");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 7)
            return VIEW_TYPE_HEADER;

        return VIEW_TYPE_DAY;
    }

    @Override
    public int getItemCount() {
        return 44;
    }

    public void setTimestamp(Calendar currentTimestamp) {
        currentTimestamp.set(Calendar.DAY_OF_MONTH, currentTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
        this.currentTimestamp = currentTimestamp;

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout calendarItem;
        private TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.title);
            calendarItem = itemView.findViewById(R.id.calendar_item);
        }

        public void bind(boolean visible, String text) {
            if(!visible) {
                calendarItem.setVisibility(View.INVISIBLE);
            } else {
                calendarItem.setVisibility(View.VISIBLE);
            }

            titleView.setText(text);
        }
    }
}
