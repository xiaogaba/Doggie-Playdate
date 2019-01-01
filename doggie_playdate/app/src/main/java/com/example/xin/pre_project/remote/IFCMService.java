package com.example.xin.pre_project.remote;

import com.example.xin.pre_project.Model.FCMResponse;
import com.example.xin.pre_project.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAPNlByag:APA91bGCmsU7_WrUnUhsarZ_8TTo4tJbry568nat8a4tP88NaGd-71zojt2SXiA1oS9fcBWJvoyMDzJsI_N8UdMdQRHoZxY6G2Qofz22QTGK4I7wDMpl50XQ9BEnBu6h2FsSUzVIwwB0"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}