package org.denhac.kiosk;

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
}
