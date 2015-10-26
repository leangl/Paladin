package com.nanospark.gard.model.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.events.CommandProcessed;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import mobi.tattu.utils.F;
import mobi.tattu.utils.Tattu;
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
        return new ArrayList<>(mDataStore.getAll(User.class));
    }

    public void add(User user) {
        mDataStore.putObject(user);
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
        return mDataStore.getObject(id, User.class).get();
    }

    public User findByName(String username) {
        return find((user) -> user.getName().trim().equalsIgnoreCase(username.trim()));
    }

    public User findByPhone(String phone) {
        return find((user) -> user.getPhone().trim().equalsIgnoreCase(phone.trim()));
    }

    public void delete(User user) {
        mDataStore.delete(User.class, user);
    }

    @Subscribe
    public void on(CommandProcessed event) {
        if (event.command.user != null && event.command.user.getSchedule() != null
                && ControlSchedule.Limit.EVENTS.equals(event.command.user.getSchedule())) {
            event.command.user.getSchedule().incrementEvents();
            update(event.command.user);
        }
    }
}
