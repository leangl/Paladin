package com.nanospark.gard.sms.twilio;

import com.crashlytics.android.Crashlytics;

import java.io.Serializable;

import mobi.tattu.utils.Base64;
import mobi.tattu.utils.Base64DecoderException;
import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.log.Logger;

/**
 * Created by Leandro on 10/8/2015.
 */
public class TwilioAccount implements Serializable {

    private String phone;
    private String sid;
    private String token;

    public TwilioAccount() {}

    public TwilioAccount(String phone, String sid, String token) {
        this.phone = phone;
        this.sid = sid;
        this.token = token;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getSid() {
        try {
            return new String(Base64.decode(sid.getBytes()));
        } catch (Exception e) {
            Crashlytics.logException(e);
            return "";
        }
    }
    public void setSid(String sid) {
        this.sid = Base64.encode(sid.getBytes());
    }
    public String getToken() {
        try {
            return new String(Base64.decode(token.getBytes()));
        } catch (Exception e) {
            Crashlytics.logException(e);
            return "";
        }
    }
    public void setToken(String token) {
        this.token = Base64.encode(token.getBytes());
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(phone)
                && StringUtils.isNotBlank(sid)
                && StringUtils.isNotBlank(token);
    }

}
