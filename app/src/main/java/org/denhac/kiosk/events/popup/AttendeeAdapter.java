package org.denhac.kiosk.events.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.denhac.kiosk.R;
import org.denhac.kiosk.events.meetup.EventRSVP;
import org.denhac.kiosk.events.meetup.MeetupRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private List<EventRSVP> attendees;
    private MeetupRepository meetupRepository;

    public AttendeeAdapter(MeetupRepository meetupRepository) {
        this.meetupRepository = meetupRepository;
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

    public void setAttendees(List<EventRSVP> attendees) {
        List<EventRSVP> oldAttendees = this.attendees;
        this.attendees = attendees;

        if(oldAttendees.size() != attendees.size()) {
            notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < oldAttendees.size(); i++) {
            if(! oldAttendees.get(i).equals(attendees.get(i))) {
                notifyItemChanged(i);
            }
        }
    }

    public void clear() {
        setAttendees(new ArrayList<EventRSVP>());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView attendeeImageView;
        private final TextView attendeeNameTextView;
        private Disposable imageDisposable;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            attendeeNameTextView = itemView.findViewById(R.id.attendee_name);
            attendeeImageView = itemView.findViewById(R.id.attendee_image);
        }

        void bind(EventRSVP eventRSVP) {
            if (imageDisposable != null) {
                imageDisposable.dispose();
            }

            attendeeNameTextView.setText(eventRSVP.getName());
            attendeeImageView.setImageDrawable(null);

            final String photoLink = eventRSVP.getPhotoLink();
            if (photoLink != null) {
                imageDisposable = meetupRepository
                        .getImageData(photoLink)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, attendeeImageView.getWidth(),
                                        attendeeImageView.getHeight(), false);
                                attendeeImageView.setImageBitmap(scaledBitmap);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                useDefaultImage();
                            }
                        });
            } else {
                useDefaultImage();
            }
        }

        private void useDefaultImage() {
            attendeeImageView.setImageResource(R.drawable.ic_insert_emoticon_black_24dp);
        }
    }
}
