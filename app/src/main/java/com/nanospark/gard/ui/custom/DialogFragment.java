package com.nanospark.gard.ui.custom;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.nanospark.gard.R;

/**
 * Created by cristian on 07/10/15.
 */
public class DialogFragment extends android.support.v4.app.DialogFragment {

    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_ALERT_DIALOG = "arg_alert_dialog";

    private String mMessage;
    private String mTitle;
    private boolean mAlertDialog;

    private DialogFragmentListener mListener;

    public static DialogFragment newInstance(String message, String title,boolean alertDialog) {

        Bundle args = new Bundle();
        DialogFragment fragment = new DialogFragment();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_ALERT_DIALOG,alertDialog);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessage = getArguments().getString(ARG_MESSAGE);
        mTitle = getArguments().getString(ARG_TITLE);
        mAlertDialog = getArguments().getBoolean(ARG_ALERT_DIALOG);
     }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setMessage(mMessage);
        builder.setTitle(mTitle);
        if(mAlertDialog){
            builder.setIcon(R.drawable.ic_alert_warning);
            builder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {mListener.onPositiveButton(dialog);});
        builder.setNegativeButton(android.R.string.cancel,(dialog, which) -> {mListener.onNegativeButton(dialog);});
        return builder.create();
    }

    public interface DialogFragmentListener{
        void onPositiveButton(DialogInterface dialog);
        void onNegativeButton(DialogInterface dialog);
    }

    public void setDialogFragmentListener(DialogFragmentListener listener){
        this.mListener = listener;
    }
}
