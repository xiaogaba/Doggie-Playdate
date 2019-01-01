package com.example.xin.pre_project.Common;

import android.location.Location;

import com.example.xin.pre_project.Model.User;
import com.example.xin.pre_project.remote.FCMClient;
import com.example.xin.pre_project.remote.IFCMService;
import com.example.xin.pre_project.remote.IGoogleAPI;
import com.example.xin.pre_project.remote.RetrofitClient;

public class Common {

    public static final String user_location_tb1 = "Users";  // store user's location
    public static final String user_info_tb1 = "UsersInformation";  // store users's all information including name,phone,email
    public static final String date_request_tb1 = "DateRequest";
    public static final String driver_tb1 = "Drivers";//part13
    public static final String user_driver_tb1 = "DriverInFormation";//part13
    public static final String user_rider_tb1 = "RiderInformation";//part13
    public static final String token_tb1 = "Token";
    public static Location mLastLocation = null;

    public static User currentUser; //part13


    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
