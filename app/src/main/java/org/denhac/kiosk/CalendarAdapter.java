package org.denhac.kiosk;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DAY = 1;

    private final MeetupRepository meetupRepository;
    private Calendar currentTimestamp;

    public CalendarAdapter(MeetupRepository meetupRepository, Calendar currentTimestamp) {
        this.meetupRepository = meetupRepository;
        setTimestamp(currentTimestamp);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView;
        if (viewType == VIEW_TYPE_DAY) {
            contactView = inflater.inflate(R.layout.calendar_item, parent, false);
        } else {
            contactView = inflater.inflate(R.layout.calendar_header, parent, false);
        }

        // Return a new holder instance
        return new ViewHolder(contactView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 7) {
            String[] weekdays = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            holder.setVisibility(View.VISIBLE);
            holder.setText(weekdays[position]);
            return;
        }

        int startPosition = currentTimestamp.get(Calendar.DAY_OF_WEEK) - 1 + 7;
        int stopPosition = startPosition + currentTimestamp.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;

        if (position >= startPosition && position <= stopPosition) {
            int dayNumber = position - startPosition + 1;
            Calendar currentDay = (Calendar) currentTimestamp.clone();
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

    public void setTimestamp(Calendar currentTimestamp) {
        currentTimestamp.set(Calendar.DAY_OF_MONTH, currentTimestamp.getActualMinimum(Calendar.DAY_OF_MONTH));
        this.currentTimestamp = currentTimestamp;

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ListView listView;
        private ConstraintLayout calendarItem;
        private int viewType;
        private TextView titleView;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            this.viewType = viewType;
            titleView = itemView.findViewById(R.id.title);
            calendarItem = itemView.findViewById(R.id.calendar_item);
            if (viewType == VIEW_TYPE_DAY) {
                listView = itemView.findViewById(R.id.list_view);
                if (listView.getAdapter() == null) {
                    ListItemAdapter adapter = new ListItemAdapter();
                    listView.setAdapter(adapter);
                }
            }
        }

        public void setDate(Calendar currentDate) {
            ((ListItemAdapter) listView.getAdapter()).update(currentDate);
            if(DateUtils.isToday(currentDate.getTimeInMillis())) {
                titleView.setBackgroundResource(R.drawable.calendar_item_title_filled);
                titleView.setTextColor(ContextCompat.getColor(titleView.getContext(), R.color.white));
            }
        }

        public void setText(String text) {
            titleView.setText(text);
        }

        public void setVisibility(int visible) {
            calendarItem.setVisibility(visible);
        }
    }

    public class ListItemAdapter extends BaseAdapter {
        private CompositeDisposable disposable;
        private List<Event> eventList = Collections.emptyList();

        public void update(Calendar currentDay) {
            if (disposable != null) {
                disposable.dispose();
            }

            eventList = Collections.emptyList();
            disposable = new CompositeDisposable();
            disposable.add(
                    meetupRepository.fetchEvents(currentDay)
                            .subscribe(new Consumer<List<Event>>() {
                                @Override
                                public void accept(List<Event> events) {
                                    eventList = events;
                                    notifyDataSetChanged();
                                }
                            })
            );
        }

        @Override
        public int getCount() {
            return eventList.size();
        }

        @Override
        public Object getItem(int position) {
            return "";
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.calendar_list_item, viewGroup, false);
            }

            if (eventList.size() <= position) {
                return view;
            }

            final Event event = eventList.get(position);
            ((TextView) view.findViewById(R.id.meeting_time)).setText(event.getLocalTime());
            ((TextView) view.findViewById(R.id.meeting_name)).setText(event.getName());
            String yesRSVPCount = event.getYesRSVPCount();
            ((TextView) view.findViewById(R.id.meeting_yes_rsvp)).setText(yesRSVPCount + " attendee" + (yesRSVPCount == "1" ? "" : "s"));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), event.getDescription(), Toast.LENGTH_LONG).show();;
                }
            });

            return view;
        }
    }
}
