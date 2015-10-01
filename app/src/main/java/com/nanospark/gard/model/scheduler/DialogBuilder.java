package com.nanospark.gard.model.scheduler;

import android.content.Context;
import android.widget.Toast;

import com.nanospark.gard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.tattu.utils.DialogUtils;

/**
 * Created by cristian on 09/08/15.
 */
public class DialogBuilder {

    public static void buildDesiredActions(Context context, final SchedulerWizard wizard) {
        String[] items = context.getResources().getStringArray(R.array.desiredActions);
        DialogUtils.singleChoiceConfirmation(context,
                items,
                items[0],
                null,
                (action, dialog) -> {
                    if (action != null) {
                        wizard.getSchedule().action = action.contains("Open") ? Schedule.ACTION_OPEN_DOOR : Schedule.ACTION_CLOSE_DOOR;
                        wizard.positiveButton(dialog);
                    } else {
                        wizard.negativeButton(dialog);
                    }
                    return false;
                });

    }

    public static void buildSelectedDays(Context context, final SchedulerWizard wizard) {
        List<String> items = Arrays.asList(context.getResources().getStringArray(R.array.days));
        DialogUtils.multiChoice(context,
                items,
                null, // nothing preselected
                null, // nothing disabled
                null, // no title
                (selectedItems, dialog) -> {
                    if (selectedItems != null) {
                        if (!selectedItems.isEmpty()) {
                            wizard.getSchedule().dayNameSelecteds = selectedItems;
                            wizard.getSchedule().days = new ArrayList<>();
                            for (String day : selectedItems) {
                                wizard.getSchedule().days.add(items.indexOf(day) + 1);
                            }
                            wizard.positiveButton(dialog);
                        } else {
                            Toast.makeText(context, "Select at least one day.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        wizard.negativeButton(dialog);
                    }
                    return false; // let the wizard close the dialog
                });
    }
    
}
