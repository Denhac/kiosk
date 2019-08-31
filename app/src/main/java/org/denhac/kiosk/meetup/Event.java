package org.denhac.kiosk.meetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("local_date")
    private String localDate;

    @Expose
    @SerializedName("local_time")
    private String localTime;

    @Expose
    @SerializedName("yes_rsvp_count")
    private String yesRSVPCount;

    @Expose
    @SerializedName("description")
    private String description;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocalTime() {
        return localTime;
    }

    public String getLocalDate() {
        return localDate;
    }

    public String getYesRSVPCount() {
        return yesRSVPCount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (id != null ? !id.equals(event.id) : event.id != null) return false;
        if (name != null ? !name.equals(event.name) : event.name != null) return false;
        if (localDate != null ? !localDate.equals(event.localDate) : event.localDate != null)
            return false;
        if (localTime != null ? !localTime.equals(event.localTime) : event.localTime != null)
            return false;
        if (yesRSVPCount != null ? !yesRSVPCount.equals(event.yesRSVPCount) : event.yesRSVPCount != null)
            return false;
        return description != null ? description.equals(event.description) : event.description == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (localDate != null ? localDate.hashCode() : 0);
        result = 31 * result + (localTime != null ? localTime.hashCode() : 0);
        result = 31 * result + (yesRSVPCount != null ? yesRSVPCount.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
