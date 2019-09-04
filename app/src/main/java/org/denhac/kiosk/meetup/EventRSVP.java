package org.denhac.kiosk.meetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventRSVP {
    @Expose
    @SerializedName("member")
    private Member member;

    @Expose
    @SerializedName("response")
    private String response;

    @Expose
    @SerializedName("guests")
    private Integer guests;

    public String getName() {
        return member.name;
    }

    public boolean isAttending() {
        return response != null && response.equals("yes");
    }

    public int getGuestsCount() {
        return guests;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventRSVP eventRSVP = (EventRSVP) o;

        if (member != null ? !member.equals(eventRSVP.member) : eventRSVP.member != null)
            return false;
        if (response != null ? !response.equals(eventRSVP.response) : eventRSVP.response != null)
            return false;
        return guests != null ? guests.equals(eventRSVP.guests) : eventRSVP.guests == null;
    }

    @Override
    public int hashCode() {
        int result = member != null ? member.hashCode() : 0;
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (guests != null ? guests.hashCode() : 0);
        return result;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        if (name != null ? !name.equals(member.name) : member.name != null) return false;
        if (eventContext != null ? !eventContext.equals(member.eventContext) : member.eventContext != null)
            return false;
        return photo != null ? photo.equals(member.photo) : member.photo == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (eventContext != null ? eventContext.hashCode() : 0);
        result = 31 * result + (photo != null ? photo.hashCode() : 0);
        return result;
    }
}

class EventContext {
    @Expose
    @SerializedName("host")
    boolean host;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventContext that = (EventContext) o;

        return host == that.host;
    }

    @Override
    public int hashCode() {
        return (host ? 1 : 0);
    }
}

class Photo {
    @Expose
    @SerializedName("photo_link")
    String photoLink;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;

        return photoLink != null ? photoLink.equals(photo.photoLink) : photo.photoLink == null;
    }

    @Override
    public int hashCode() {
        return photoLink != null ? photoLink.hashCode() : 0;
    }
}