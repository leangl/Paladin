package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
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

import java.util.ArrayList;

/**
 * Created by cristian on 10/10/15.
 */
public class LogFragment extends BaseFragment {

    @Inject
    private LogManager mLogManager;
    private ArrayList<Log> mLogArrayList;

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
        View view = inflater.inflate(R.layout.fragment_log,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        view.findViewById(R.id.fab_save).setOnClickListener(v -> {
            saveLogCsv();
            exportCsv();
        });

        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(getBaseActivity());
        LogAdapter logAdapter = new LogAdapter(mLogArrayList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(logAdapter);
        return view;
    }

    private void saveLogCsv(){}

    private void exportCsv(){}

    @Override
    public boolean showHomeIcon() {
        return true;
    }
}
