package org.denhac.kiosk.meetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventAttendee {
    @Expose
    @SerializedName("member")
    private Member member;

    @Expose
    @SerializedName("rsvp")
    private RSVP rsvp;

    public String getName() {
        return member.name;
    }

    public boolean isAttending() {
        return rsvp != null && rsvp.response.equals("yes");
    }

    public int getGuestsCount() {
        return rsvp.guestsCount;
    }

    public boolean isHost()
    {
        if(member.eventContext != null) {
            return member.eventContext.host;
        }

        return false;
    }

    public String getPhotoLink() {
        if(member.photo != null) {
            return member.photo.photoLink;
        }

        return null;
    }
}

class Member {
    @Expose
    @SerializedName("name")
    String name;

    @Expose
    @SerializedName("event_context")
    EventContext eventContext;

    @Expose
    @SerializedName("photo")
    Photo photo;
}

class EventContext {
    @Expose
    @SerializedName("host")
    boolean host;
}

class Photo {
    @Expose
    @SerializedName("photo_link")
    String photoLink;
}

class RSVP {
    @Expose
    @SerializedName("response")
    String response;

    @Expose
    @SerializedName("guests")
    int guestsCount;
}