package com.nanospark.gard.model.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.events.CommandProcessed;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.tattu.utils.F;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 1/10/2015.
 */
@Singleton
public class UserManager {

    @Inject
    private DataStore mDataStore;

    public UserManager() {
        Tattu.register(this);
    }

    public static UserManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(UserManager.class);
    }

    public List<User> getAll() {
        List<User> all = new ArrayList<>(mDataStore.getAll(User.class));
        Collections.sort(all, (u1, u2) -> u1.getCreateDate().compareTo(u2.getCreateDate()));
        return all;
    }

    public void add(User user) {
        mDataStore.put(user);
    }

    public void update(User user) {
        add(user);
    }

    /**
     * Valida si existe el nombre de usuario
     */
    public boolean exists(String username) {
        return findByName(username) != null;
    }

    public User find(F.Predicate<User> p) {
        for (User user : getAll()) {
            if (p.test(user)) {
                return user;
            }
        }
        return null;
    }

    public User getUser(String id) {
        return mDataStore.get(id, User.class).get();
    }

    public User findByName(String username) {
        return find((user) -> user.getName().trim().equalsIgnoreCase(username.trim()));
    }

    public User findByPhone(String phone) {
        F.Function<String, String> filter = (number) -> number.trim().replace("+1", "");
        return find((user) -> filter.apply(user.getPhone()).equalsIgnoreCase(filter.apply(phone)));
    }

    public void delete(User user) {
        mDataStore.delete(User.class, user);
    }

    @Subscribe
    public void on(CommandProcessed event) {
        if (event.command != null && event.command.user != null && event.command.user.getSchedule() != null
                && Limit.EVENTS.equals(event.command.user.getSchedule().getLimit())) {
            ToastManager.show("Command processed");
            event.command.user.getSchedule().incrementEvents();
            update(event.command.user);
        }
    }
}
