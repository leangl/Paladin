package com.nanospark.gard.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.adapter.LogAdapter;
import com.nanospark.gard.model.log.Log;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.ui.custom.BaseFragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import mobi.tattu.utils.ToastManager;
import mobi.tattu.utils.image.AsyncTask;

/**
 * Created by cristian on 10/10/15.
 */
public class LogFragment extends BaseFragment {

    public static final String COMMA = ",";
    public static final String NEW_LINE_FILE = "\r\n";

    @Inject
    private LogManager mLogManager;
    private List<Log> mLogArrayList;

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogArrayList = mLogManager.getLogs();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        changeTitleActionBar(getString(R.string.logs));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        view.findViewById(R.id.fab_save).setOnClickListener(v -> {
            saveLogCsv();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseActivity());
        LogAdapter logAdapter = new LogAdapter(mLogArrayList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(logAdapter);
        return view;
    }

    private void saveLogCsv() {
        LogAsyncTask logAsyncTask = new LogAsyncTask();
        logAsyncTask.execute();
    }

    private void exportCsv(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Send To"));
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    public class LogAsyncTask extends AsyncTask<Void, Void, File> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading(true, R.string.save_file_log_msg);
        }

        @Override
        protected File doInBackground(Void... params) {
            File result = null;
            File folder = new File(Environment.getExternalStorageDirectory() + "/Paladin");
            if (!folder.exists()) {
                folder.mkdir();
            }
            String fileName = folder.toString() + "/" + "Log.csv";
            int size = mLogArrayList.size();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(fileName);

                StringBuilder builder = new StringBuilder();
                builder.append(getString(R.string.door_label));
                builder.append(COMMA);
                builder.append(getString(R.string.event_label));
                builder.append(COMMA);
                builder.append(getString(R.string.date_label));
                builder.append(NEW_LINE_FILE);

                fileWriter.write(builder.toString());
                builder = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    Log log = mLogArrayList.get(i);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(log.getDate());

                    builder.append(log.getDoorId());
                    builder.append(COMMA);

                    builder.append(log.getEvent().name());
                    builder.append(log.getDateString(false));
                    builder.append(NEW_LINE_FILE);
                    fileWriter.write(builder.toString());
                }
                fileWriter.close();
                result = new File(fileName);
            } catch (IOException io) {
                android.util.Log.e("Log", io.getMessage(), io);
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(File file) {
            stopLoading();
            if (file == null) {
                ToastManager.get().showToast(R.string.error_save_log_msg);
            } else {
                exportCsv(file);
            }
        }
    }
}
