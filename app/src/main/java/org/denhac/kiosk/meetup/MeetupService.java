package org.denhac.kiosk.meetup;


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

    @GET("{group}/events/{id}/attendance")
    Single<List<EventAttendee>> eventAttendees(@Path("group") String groupName, @Path("id") String eventId);
}
