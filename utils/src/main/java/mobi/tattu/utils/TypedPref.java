package mobi.tattu.utils;

import android.content.SharedPreferences;

import java.io.Serializable;

import mobi.tattu.utils.log.Logger;
import mobi.tattu.utils.preferences.Config;

public class TypedPref<T> implements Serializable {
    private String key;
    private T defaultValue;
    private Class<T> fetchType;
    private Class<?> persistType;

    /**
     * Constructor when no default value available and fetched type == persisted type
     *
     * @param key
     */
    public TypedPref(String key, Class<T> fetchType) {
        this(key, fetchType, fetchType);
    }

    /**
     * Constructor when no default value available
     *
     * @param key
     */
    public TypedPref(String key, Class<T> fetchType, Class<?> persistType) {
        this.key = key;
        this.fetchType = fetchType;
        this.persistType = persistType;
    }

    public TypedPref(String key, T defaultValue) {
        this(key, defaultValue, defaultValue.getClass());
    }

    @SuppressWarnings("unchecked")
    public TypedPref(String key, T defaultValue, Class<?> persistType) {
        this(key, (Class<T>) defaultValue.getClass(), persistType);
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Class<T> getFetchType() {
        return fetchType;
    }

    public Class<?> getPersistType() {
        return persistType;
    }

    /**
     * Returns the persisted value
     *
     * @return
     */
    public T getValue() {
        try {
            if (String.class.isAssignableFrom(persistType)) {
                return convert(getUserPreferences().getString(key, (String) convert(defaultValue, persistType)), fetchType);
            } else if (Boolean.class.isAssignableFrom(persistType)) {
                return convert(Boolean.valueOf(getUserPreferences().getBoolean(key, (Boolean) convert(defaultValue, persistType))), fetchType);
            } else if (Integer.class.isAssignableFrom(persistType)) {
                return convert(Integer.valueOf(getUserPreferences().getInt(key, (Integer) convert(defaultValue, persistType))), fetchType);
            } else if (Long.class.isAssignableFrom(persistType)) {
                return convert(Long.valueOf(getUserPreferences().getLong(key, (Long) convert(defaultValue, persistType))), fetchType);
            } else if (Float.class.isAssignableFrom(persistType)) {
                return convert(Float.valueOf(getUserPreferences().getFloat(key, (Float) convert(defaultValue, persistType))), fetchType);
            }
        } catch (Exception e) {
            Logger.e(this, "Error getting pref value: " + key + "=" + defaultValue);
        }
        return defaultValue;
    }

    public void setValue(T value) {
        SharedPreferences.Editor editor = getUserPreferences().edit();

        Class clazz = value.getClass();
        try {
            if (String.class.isAssignableFrom(clazz)) {
                editor.putString(key, (String) value);
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                editor.putBoolean(key, (Boolean) value);
            } else if (Integer.class.isAssignableFrom(clazz)) {
                editor.putInt(key, (Integer) value);
            } else if (Long.class.isAssignableFrom(clazz)) {
                editor.putLong(key, (Long) value);
            } else if (Float.class.isAssignableFrom(clazz)) {
                editor.putFloat(key, (Float) value);
            } else {
                throw new Exception("Wrong type preference: " + value.getClass() + "(" + value + ")" + "for key: " + key);
            }
            editor.commit();
        } catch (Exception e) {
            Logger.e(this, "Error getting pref value: " + key + "=" + defaultValue, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <I, O> O convert(I input, Class<O> outputClass) throws Exception {
        if (input == null) return null;
        if (input.getClass().equals(outputClass)) return (O) input;
        return outputClass.getConstructor(String.class).newInstance(input.toString());
    }

    private SharedPreferences getUserPreferences() {
        return Config.get().getUserPreferences();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypedPref other = (TypedPref) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getValue().toString();
    }

}
