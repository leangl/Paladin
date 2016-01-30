package com.nanospark.gard.sms.twilio;

import com.google.gson.JsonElement;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
public interface TwilioApi {

    // Wed, 08 Jul 2015 23:54:32 +0000
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    @GET("/2010-04-01/Accounts/{AccountSid}/Messages.json")
    Observable<JsonElement> getMessages(@Path("AccountSid") String accountSid, @Query("To") String to);

    @FormUrlEncoded
    @POST("/2010-04-01/Accounts/{AccountSid}/Messages.json")
    Observable<JsonElement> sendMessage(@Path("AccountSid") String accountSid, @Field("Body") String body,
                                        @Field("From") String from, @Field("To") String to);
}
