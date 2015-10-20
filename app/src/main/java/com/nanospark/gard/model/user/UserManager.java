package com.nanospark.gard.model.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import mobi.tattu.utils.F;
import mobi.tattu.utils.persistance.datastore.DataStore;

/**
 * Created by Leandro on 1/10/2015.
 */
@Singleton
public class UserManager {

    @Inject
    private DataStore mDataStore;

    public List<User> getAll() {
        return new ArrayList<>(mDataStore.getAll(User.class));
    }

    public void add(User user) {
        mDataStore.putObject(user.getId(),user);
    }

    /**
     * Valida si existe el nombre de usuario
     */
    public boolean exists(String username) {
        User user = new User();
        user.setName(username);
        return mDataStore.contains(user);
    }

    public User find(F.Predicate<User> p) {
        for (User user : getAll()) {
            if (p.test(user)) {
                return user;
            }
        }
        return null;
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

}
