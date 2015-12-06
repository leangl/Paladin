package com.nanospark.gard.services;

import android.app.Notification;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.google.inject.Inject;
import com.nanospark.gard.BuildConfig;
import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.sms.SmsManager;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.services.BaseService;
import roboguice.util.Ln;

/**
 * Created by Leandro on 21/7/2015.
 */
public class GarDService extends BaseService implements IOIOLooperProvider {

    public static final String RESTART_IOIO = "restart_ioio";

    // service started flag
    private boolean started;

    @Inject
    private Door.One mDoorOne;
    @Inject
    private Door.Two mDoorTwo;
    @Inject
    private SmsManager mClient;

    private IOIOAndroidApplicationHelper ioioHelper;

    private Notification mNotification;

    private boolean mBoardConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopIOIO();
        mClient.stopChecking();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!started) {
            started = true;

            mNotification = new NotificationCompat.Builder(GarDService.this)
                    .setContentTitle("Paladin is running")
                    .setSmallIcon(R.drawable.ic_stat_paladin)
                    .build();
            startForeground(123, mNotification);

            mClient.startChecking();

            startIOIO();
        }

        if (intent != null && intent.getAction() != null && RESTART_IOIO.equals(intent.getAction())) {
            startIOIO();
        }

        return START_STICKY;
    }

    @Subscribe
    public void on(BoardConnected e) {
        mBoardConnected = true;
        Ln.d("BoardConnected");
    }

    @Subscribe
    public void on(BoardDisconnected e) {
        mBoardConnected = false;
        Ln.d("BoardDisconnected");
    }

    @Produce
    public BoardConnected produceConnected() {
        Ln.d("Produce BoardConnected? " + mBoardConnected);
        return mBoardConnected ? new BoardConnected() : null;
    }

    @Produce
    public BoardDisconnected produceDisconnected() {
        Ln.d("Produce BoardDisconnected? " + !mBoardConnected);
        return !mBoardConnected ? new BoardDisconnected() : null;
    }

    private void startIOIO() {
        stopIOIO();
        Tattu.runOnUiThread(() -> {
            // Disable IOIO on emulator since it starves all resources
            if (!BuildConfig.DEBUG || !Utils.isVM()) {
                ioioHelper = new IOIOAndroidApplicationHelper(this, this);
                ioioHelper.create();
                ioioHelper.start();
            }
        }, 3000);
    }

    private void stopIOIO() {
        if (ioioHelper != null) {
            ioioHelper.stop();
            ioioHelper.destroy();
            ioioHelper = null;
        }
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new Looper();
    }

    private class Looper extends BaseIOIOLooper {

        @Override
        protected void setup() throws ConnectionLostException {
            mDoorOne.setup(ioio_);
            mDoorTwo.setup(ioio_);
            Tattu.post(new BoardConnected());
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            mDoorOne.loop();
            mDoorTwo.loop();
        }

        @Override
        public void disconnected() {
            Tattu.post(new BoardDisconnected());
        }

        @Override
        public void incompatible() {
            GarD.instance.toast("Incompatible firmware version!");
        }
    }

    public static void start() {
        Intent i = new Intent(GarD.instance, GarDService.class);
        GarD.instance.startService(i);
    }

    public static void restart() {
        Intent i = new Intent(GarD.instance, GarDService.class);
        i.setAction(RESTART_IOIO);
        GarD.instance.startService(i);
    }

    public static void stop() {
        GarD.instance.stopService(new Intent(GarD.instance, GarDService.class));
    }

}
