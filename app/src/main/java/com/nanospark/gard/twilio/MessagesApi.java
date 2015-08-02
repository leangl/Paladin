package com.nanospark.gard.twilio;

import com.google.gson.JsonElement;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by Leandro on 26/7/2015.
 */
public interface MessagesApi {

    @GET("/2010-04-01/Accounts/{AccountSid}/Messages.json")
    Observable<JsonElement> getMessages(@Path("AccountSid") String accountSid);

}
