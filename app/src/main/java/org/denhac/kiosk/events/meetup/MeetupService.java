package org.denhac.kiosk.events.meetup;


import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeetupService {
    @GET("{group}/events?status=past,upcoming&page=1000")
    Single<List<Event>> events(@Path("group") String groupName,
                               @Query("no_earlier_than") String noEarlierThan,
                               @Query("no_later_than") String noLaterThan);

    @GET("{group}/events/{id}/rsvps")
    Single<List<EventRSVP>> eventAttendees(@Path("group") String groupName, @Path("id") String eventId);

    @GET("{group}/events/{id}")
    Single<Event> event(@Path("group") String groupName, @Path("id") String eventId);
}
