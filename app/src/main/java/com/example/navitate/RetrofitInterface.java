package com.example.navitate;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @POST("/navitation")
    Call<Void> postNavitation(@Body HashMap<String, String> map);

    @POST("/user")
    Call<Void> postUser(@Body HashMap<String, String> map);

    @GET("/navitations")
    Call<List<String>> getNavitations();

    @GET("/navitation/{title}")
    Call<List<GenericMapAnnotation>> getNavitation(@Path("title") String title);

    /*@GET("/score/{latitude}/{longitude}")
    Call<ScoreMessage> getSafetyScore(@Path("latitude") String lat, @Path("longitude") String lon);*/


}
