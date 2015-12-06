package com.nanospark.gard.model.user;

import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.model.door.Door;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Pattern;

import mobi.tattu.utils.ResourceUtils;
import mobi.tattu.utils.StringUtils;

/**
 * Created by Leandro on 1/10/2015.
 */
public class User implements Serializable {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    private String id;
    private String name;
    private String phone;
    private String password;
    private Notify notify;
    private ControlSchedule schedule;
    private Long createDate;

    public User() {
        this.id = UUID.randomUUID().toString();
        createDate = System.currentTimeMillis();

    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public void setSchedule(ControlSchedule schedule) {
        this.schedule = schedule;
    }
    public Long getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isPasswordRequired() {
        return StringUtils.isNotBlank(password);
    }

    public boolean isNotificationEnabled() {
        return notify != null && !notify.equals(Notify.NONE);
    }

    public boolean isAllowed() {
        if (schedule == null) {
            return true;
        } else {
            return schedule.isAllowed();
        }
    }

    public enum Notify {
        OPEN(true, false), CLOSE(false, true), ALL(true, true), NONE(false, false);

        private boolean open;
        private boolean close;

        Notify(boolean open, boolean close) {
            this.open = open;
            this.close = close;
        }

        public boolean notify(Door.State state) {
            switch (state) {
                case OPEN:
                    return open;
                case CLOSED:
                    return close;
                case UNKNOWN:
                default:
                    return false;
            }
        }

        @Override
        public String toString() {
            return ResourceUtils.stringByName("notify." + name().toLowerCase());
        }
    }

    public static boolean isUsernameValid(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        if (username.length() > 20) {
            return false;
        }
        if (WHITESPACE_PATTERN.matcher(username).find()) {
            return false;
        }
        if (Character.isDigit(username.charAt(0))) {
            return false;
        }

        return true;
    }

    public boolean isPasswordCorrect(String password) {
        return password != null && password.equalsIgnoreCase(this.password);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean hasSchedules() {
        return getSchedule() != null;
    }

    public String getHourRangeString() {
        if (getSchedule() == null) return GarD.instance.getString(R.string.not_available);
        return getSchedule().getHourRangeString();
    }

    public String getDayLimitString() {
        if (getSchedule() == null) return "";
        return getSchedule().getDayLimitString();
    }
}
