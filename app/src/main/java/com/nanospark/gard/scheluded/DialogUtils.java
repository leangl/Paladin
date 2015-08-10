package com.nanospark.gard.scheluded;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.nanospark.gard.R;
import com.nanospark.gard.scheluded.BuilderWizardScheluded;

/**
 * Created by cristian on 09/08/15.
 */
public class DialogUtils {

    public interface DialogListener  {
        public void positiveButton(DialogInterface dialog);
        public void negativeButton(DialogInterface dialog);
    }

    public static void builderDesiredAction(Context context,final BuilderWizardScheluded handlerScheluded){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setSingleChoiceItems(R.array.desiredActions, -1, handlerScheluded);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handlerScheluded.positiveButton(dialog);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handlerScheluded.negativeButton(dialog);
            }
        });
        builder.show();
    }

    public static void builderSelectDays(Context context,final BuilderWizardScheluded handlerScheluded){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMultiChoiceItems(R.array.days, null, handlerScheluded);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                handlerScheluded.positiveButton(dialog);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                handlerScheluded.negativeButton(dialog);
            }
        });
        builder.show();
    }


}
