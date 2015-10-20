package com.nanospark.gard.model.user;

import com.nanospark.gard.model.door.Door;

import mobi.tattu.utils.StringUtils;

/**
 * Created by Leandro on 1/10/2015.
 */
public class User {


    private String name;
    private String phone;
    private String password;
    private Notify notify;
    private ControlSchedule schedule;

    public User() {
    }

    public User(String name, String phone, String password, Notify notify) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.notify = notify;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Notify getNotify() {
        return notify;
    }
    public void setNotify(Notify notify) {
        this.notify = notify;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public ControlSchedule getSchedule() {
        return schedule;
    }
    public void setSchedules(ControlSchedule schedule) {
        this.schedule = schedule;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isPasswordRequired() {
        return StringUtils.isNotBlank(password);
    }

    public boolean isNotificationEnabled() {
        return notify != null && !notify.equals(Notify.NONE);
    }

    public boolean isAllowedTime(Door door) {
        return true; // TODO
    }

    public enum Notify {
        OPEN(true, false), CLOSE(false, true), ALL(true, true), NONE(false, false);

        private boolean open;
        private boolean close;

        Notify(boolean open, boolean close) {
            this.open = open;
            this.close = close;
        }

        public boolean notify(boolean isOpen) {
            if (isOpen) {
                return open;
            } else {
                return close;
            }
        }

    }

    public static boolean isUsernameValid(String username) {
        return StringUtils.isNotBlank(username) && username.length() <= 20;
    }

    public boolean isPasswordCorrect(String password) {
        return password != null && password.equalsIgnoreCase(this.password);
    }

}
