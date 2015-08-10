package com.nanospark.gard.scheluded;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TimePicker;

import com.nanospark.gard.MainActivity;
import com.nanospark.gard.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.persistance.datastore.DataStore;

/**
 * Created by cristian on 09/08/15.
 */
public class BuilderWizardScheluded implements DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnClickListener, DialogUtils.DialogListener, TimePickerFragment.TimePickerFragmentListener {

    public static String ACTION_OPEN_DOOR = "action_open_door";
    public static String ACTION_CLOSE_DOOR = "action_close_door";

    private Scheluded mScheluded;
    private Context mContext;
    private boolean launcherClock = true;
    private boolean launcherDays;
    private BuilderWizardScheludedListener mListener;
    private String mId;

    /**
     *
     * @param context
     * @param id identifica de donde es lanzado
     */
    public BuilderWizardScheluded(Context context,String id) {
        this.mScheluded = new Scheluded();
        this.mContext = context;
        this.mScheluded.days = new ArrayList<>();
        this.mScheluded.dayNameSelecteds = new ArrayList<>();
        this.mId = id;
    }

    @Override
    public void positiveButton(DialogInterface dialog) {
        dialog.dismiss();
        if(launcherClock){
            TimePickerFragment newFragment = new TimePickerFragment();
            newFragment.setTimePickerFragmentListener(this);
            newFragment.show(((MainActivity) mContext).getSupportFragmentManager(), "timePicker");
            launcherClock = false;
        }else{
            DataStore.getInstance().putObject(mScheluded.action, mScheluded);
            initializeAlarm(mContext,mScheluded);
            this.mListener.onSuccess(mId, mScheluded);
        }

    }

    public static void initializeAlarm(Context context,Scheluded scheluded) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.KEY_EXTRA_ACTION,scheluded.action);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, scheluded.hourOfDay);
        calendar.set(Calendar.MINUTE, scheluded.minute);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                TimeUnit.HOURS.toMillis(24), alarmIntent);
        context.getSharedPreferences("alarm",Context.MODE_PRIVATE).edit().putString(scheluded.action,scheluded.action).commit();
    }

    @Override
    public void negativeButton(DialogInterface dialog) {
        dialog.dismiss();
    }

    /**
     * Es para los radiobutton
     *
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        String action = this.mContext.getResources().getStringArray(R.array.desiredActions)[which];
        mScheluded.action = action.contains("Open") ? ACTION_OPEN_DOOR : ACTION_CLOSE_DOOR;

    }

    /**
     * Es para los checkbox
     *
     * @param dialog
     * @param which
     * @param isChecked
     */
    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked) {
            mScheluded.dayNameSelecteds.add(mContext.getResources().getStringArray(R.array.days)[which]);
            mScheluded.days.add(which +1);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mScheluded.hourOfDay = hourOfDay;
        mScheluded.minute = minute;
        DialogUtils.builderSelectDays(mContext,this);
    }

    public void setListener(BuilderWizardScheludedListener listener){
        this.mListener = listener;
    }
    public interface BuilderWizardScheludedListener {
        /**
         * Este metodo se invoca cuando se termina de seleccionar los dias
         * @param id
         * @param scheluded
         */
        void onSuccess(String id, Scheluded scheluded);
    }
}
