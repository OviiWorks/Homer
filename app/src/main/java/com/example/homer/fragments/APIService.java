package com.example.homer.fragments;

import com.example.homer.Notifications.MyResponse;
import com.example.homer.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAUvm8fDI:APA91bE8ggfyXYPxePlBiOkC7eRD2dI_1-GfrqJPT4SsHqC7SGaeaztWsg--1ic1sjdtDSfZdBb-MYkBH227-0w2NiTBasbgxdh91I2Ebx7fUIB6xexAQAB88yosk8ad_i_8F8RxLVX9"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
