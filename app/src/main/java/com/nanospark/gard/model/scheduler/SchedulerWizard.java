package com.nanospark.gard.model.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.persistance.datastore.DataStore;

/**
 * Created by cristian on 09/08/15.
 */
@Deprecated
public class SchedulerWizard implements TimePickerFragment.TimePickerFragmentListener {

    private ScheduleOld mSchedule;
    private Context mContext;
    private boolean launcherClock = true;
    private BuilderWizardScheludedListener mListener;

    public SchedulerWizard(Context context, String name) {
        this.mSchedule = new ScheduleOld();
        this.mContext = context;
        this.mSchedule.days = new ArrayList<>();
        this.mSchedule.dayNameSelecteds = new ArrayList<>();
        this.mSchedule.name = name;
    }

    public void positiveButton(DialogInterface dialog) {
        dialog.dismiss();
        if (launcherClock) {
            TimePickerFragment newFragment = new TimePickerFragment();
            newFragment.setTimePickerFragmentListener(this);
            newFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "timePicker");
            launcherClock = false;
        } else {
            initializeAlarm(mContext, mSchedule);
            this.mListener.onSuccess(mSchedule.name, mSchedule);
        }

    }

    public static void initializeAlarm(Context context, ScheduleOld schedule) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, schedule.hourOfDay);
        calendar.set(Calendar.MINUTE, schedule.minute);
        calendar.set(Calendar.SECOND, 0);

        long timeInMillis = calendar.getTimeInMillis();
        schedule.timeStamp = timeInMillis;

        DataStore.getInstance().putObject(schedule.name, schedule);
        PendingIntent alarmIntent = getPendingIntent(context, schedule);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, TimeUnit.HOURS.toMillis(24), alarmIntent);
    }

    private static PendingIntent getPendingIntent(Context context, ScheduleOld schedule) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        //intent.setAction(AlarmReceiver.ACTION);
        intent.putExtra(AlarmReceiver.KEY_EXTRA_NAME, schedule.name);
        //intent.putExtra(AlarmReceiver.KEY_EXTRA_TIMESTAMP, schedule.timeStamp);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void negativeButton(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mSchedule.hourOfDay = hourOfDay;
        mSchedule.minute = minute;
        DialogBuilder.buildSelectedDays(mContext, this);
    }

    public void setListener(BuilderWizardScheludedListener listener) {
        this.mListener = listener;
    }

    public interface BuilderWizardScheludedListener {
        /**
         * Este metodo se invoca cuando se termina de seleccionar los dias
         *
         * @param id
         * @param schedule
         */
        void onSuccess(String id, ScheduleOld schedule);
    }

    public ScheduleOld getSchedule() {
        return mSchedule;
    }

}
