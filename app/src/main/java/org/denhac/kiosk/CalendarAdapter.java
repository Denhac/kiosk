package org.denhac.kiosk;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DAY = 1;

    private final MeetupRepository meetupRepository;
    private Calendar currentlyViewedMonth;

    public CalendarAdapter(MeetupRepository meetupRepository, Calendar currentlyViewedMonth) {
        this.meetupRepository = meetupRepository;
        setCurrentlyViewedMonth(currentlyViewedMonth);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View calendarItemView;
        if (viewType == VIEW_TYPE_DAY) {
            calendarItemView = inflater.inflate(R.layout.calendar_item, parent, false);
        } else {
            calendarItemView = inflater.inflate(R.layout.calendar_header, parent, false);
        }

        // Return a new holder instance
        return new ViewHolder(calendarItemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 7) {
            String[] weekdays = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            holder.setVisibility(View.VISIBLE);
            holder.setText(weekdays[position]);
            return;
        }

        int startPosition = currentlyViewedMonth.get(Calendar.DAY_OF_WEEK) - 1 + 7;
        int stopPosition = startPosition + currentlyViewedMonth.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;

        if (position >= startPosition && position <= stopPosition) {
            int dayNumber = position - startPosition + 1;
            Calendar currentDay = (Calendar) currentlyViewedMonth.clone();
            currentDay.set(Calendar.DAY_OF_MONTH, dayNumber);

            holder.setVisibility(View.VISIBLE);
            holder.setText(String.valueOf(dayNumber));
            holder.setDate(currentDay);
        } else {
            holder.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 7)
            return VIEW_TYPE_HEADER;

        return VIEW_TYPE_DAY;
    }

    @Override
    public int getItemCount() {
        return 44;
    }

    public void setCurrentlyViewedMonth(Calendar newTimestamp) {
        newTimestamp.set(Calendar.DAY_OF_MONTH, newTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
//
//        if (this.currentlyViewedMonth != null &&
//                newTimestamp.get(Calendar.YEAR) == this.currentlyViewedMonth.get(Calendar.YEAR) &&
//                newTimestamp.get(Calendar.MONTH) == this.currentlyViewedMonth.get(Calendar.MONTH)) {
//            return;
//        }
        this.currentlyViewedMonth = newTimestamp;

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView eventsList;
        private ConstraintLayout calendarItem;
        private TextView titleView;
        private Disposable disposable;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            titleView = itemView.findViewById(R.id.title);
            calendarItem = itemView.findViewById(R.id.calendar_item);
            if (viewType == VIEW_TYPE_DAY) {
                eventsList = itemView.findViewById(R.id.events_list);
                eventsList.setAdapter(new EventListAdapter());
                eventsList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
        }

        public void setDate(Calendar currentDate) {
            final EventListAdapter adapter = (EventListAdapter) eventsList.getAdapter();
            if (disposable != null) {
                disposable.dispose();
            }

            if (adapter != null) {
                adapter.setEvents(Collections.<Event>emptyList());
                disposable = meetupRepository.fetchEvents(currentDate)
                        .subscribe(new Consumer<List<Event>>() {
                            @Override
                            public void accept(List<Event> events) {
                                adapter.setEvents(events);
                            }
                        });
            }

            if (DateUtils.isToday(currentDate.getTimeInMillis())) {
                titleView.setBackgroundResource(R.drawable.calendar_item_title_filled);
                titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), R.color.white));
            } else {
                titleView.setBackgroundResource(R.drawable.calendar_item_title);
                titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), R.color.black));
            }
        }

        public void setText(String text) {
            titleView.setText(text);
        }

        public void setVisibility(int visible) {
            calendarItem.setVisibility(visible);
        }
    }
}
