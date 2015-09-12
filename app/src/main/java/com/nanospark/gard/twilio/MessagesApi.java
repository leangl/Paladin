package com.nanospark.gard.twilio;

import com.google.gson.JsonElement;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Leandro on 26/7/2015.
 */
public interface MessagesApi {

    @GET("/2010-04-01/Accounts/{AccountSid}/Messages.json")
    Observable<JsonElement> getMessages(@Path("AccountSid") String accountSid, @Query("To") String to);

    @FormUrlEncoded
    @POST("/2010-04-01/Accounts/{AccountSid}/Messages.json")
    Observable<JsonElement> sendMessage(@Path("AccountSid") String accountSid, @Field("Body") String body,
                                        @Field("From") String from, @Field("To") String to);
}
