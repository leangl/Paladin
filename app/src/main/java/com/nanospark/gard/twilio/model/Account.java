package com.nanospark.gard.twilio.model;

import java.io.Serializable;

import mobi.tattu.utils.StringUtils;

/**
 * Created by Leandro on 10/8/2015.
 */
public class Account implements Serializable {

    private String phone;
    private String sid;
    private String token;

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getSid() {
        return sid;
    }
    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(phone)
                && StringUtils.isNotBlank(sid)
                && StringUtils.isNotBlank(token);
    }

}
