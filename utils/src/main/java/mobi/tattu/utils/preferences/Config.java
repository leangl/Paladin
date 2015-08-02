package mobi.tattu.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.Map;

import mobi.tattu.utils.Tattu;

public class Config implements OnSharedPreferenceChangeListener {

    private static final String APP_PREFERENCES = "app_preferences";

    private static Config instance;
    private SharedPreferences mDefaultSharedPrefs;
    private SharedPreferences mAppPreferences;

    private Config() {
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(Tattu.context);
        mAppPreferences = Tattu.context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        mDefaultSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    public static synchronized Config get() {
        if (instance == null) {
            Config.instance = new Config();
        }
        return Config.instance;
    }

    public void setDefaultPreferences(boolean reset, DefaultPreferenceCallback callback) {
        // Si se hizo un reset de las preferencias o nunca se setearon inicialmente, entonces se setean aquellas preferencias cuyos valores por defecto
        // dependen del dispositivo y por lo tanto no pueden ser parametrizados en el XML.
        if (!mDefaultSharedPrefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false) || reset) {
            Editor editor = mDefaultSharedPrefs.edit();

            if (callback != null) {
                callback.onReset(editor);
            }

            editor.putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true);
            editor.clear();
            editor.commit();
        }
    }

    public interface DefaultPreferenceCallback {
        void onReset(Editor editor);
    }

    /**
     * Retorna las preferencias del usuario.
     *
     * @return
     */
    public SharedPreferences getUserPreferences() {
        return mDefaultSharedPrefs;
    }

    /**
     * Retorna las preferencias internas de la aplicación, aquellas que el usuario no tiene control, por ejemplo flags de primer acceso,
     * etc..
     *
     * @return
     */
    public SharedPreferences getApplicationPreferences() {
        return mAppPreferences;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Handle app wide configuration changes here
    }

    public String dump() {
        SharedPreferences prefs = this.getUserPreferences();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                builder.append(String.format("%s = <null>%n", entry.getKey()));
            } else {
                builder.append(String.format("%s = %s (%s)%n", entry.getKey(), String.valueOf(val), val.getClass().getSimpleName()));
            }
        }
        return builder.toString();
    }

}
