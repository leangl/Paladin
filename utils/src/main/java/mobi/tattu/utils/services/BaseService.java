package mobi.tattu.utils.services;

import android.content.Intent;
import android.os.IBinder;

import mobi.tattu.utils.Tattu;
import roboguice.RoboGuice;
import roboguice.service.RoboService;

/**
 * Created by Leandro on 21/7/2015.
 */
public class BaseService extends RoboService {

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Tattu.bus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Tattu.bus().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Binding sucks!");
    }

}
