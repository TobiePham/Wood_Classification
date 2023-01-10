package com.example.woody.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    Gson gson =new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    ApiService apiService= new Retrofit.Builder()
            .baseUrl("https://b4d3-34-86-138-115.ngrok.io/").addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);


    @POST("detectWoodImage")
    Call<String> checkWoodImage(@Query("image") String image);
}
