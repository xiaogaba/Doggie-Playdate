package com.example.xin.pre_project;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Playdate {
    public PDAttendee user1, user2;
    public Date date;   // yyyy-MM-dd HH:mm
    public float latitude, longitude;

    public Playdate(PDAttendee u1, PDAttendee u2, Date dt, float lat, float lon) {
        this.user1 = u1;
        this.user2 = u2;
        this.date = dt;
        this.latitude = lat;
        this.longitude = lon;
    }



    public String dateToDBString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    public String dToString() {
        SimpleDateFormat format = new SimpleDateFormat("E MMM d, y  HH:mm");
        return format.format(date);
    }
}
