package com.nanospark.gard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by cristian on 10/10/15.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    public static final String SPACE = " ";

    private Context mContext;
    private List<Log> mLogs;

    public LogAdapter(List<Log> logArrayList) {
        mLogs = logArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_log, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    public Log getPreviousOpenLog(int position, int doorId) {
        Log previous = null;
        for (int i = position + 1; i < mLogs.size(); i++) {
            Log log = mLogs.get(i);
            if (log.getDoorId() == doorId && log.getEvent() == Door.State.OPEN) {
                return log;
            }
        }
        return previous;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log log = mLogs.get(position);
        if (log.getEvent() == Door.State.CLOSED) {
            holder.image.setImageResource(R.drawable.ic_lock_2);
            Log previous = getPreviousOpenLog(position, log.getDoorId());
            if (previous != null) {
                holder.subTitle.setText(getOpenedFor(log, previous));
                holder.subTitle.setVisibility(View.VISIBLE);
            } else {
                holder.subTitle.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_unlock_2);
            holder.subTitle.setVisibility(View.INVISIBLE);
        }
        holder.title.setText(log.getDateString(true));
    }

    private String getOpenedFor(Log log, Log previous) {
        long delta = log.getDate().getTime() - previous.getDate().getTime();
        long hours = TimeUnit.MILLISECONDS.toHours(delta);
        delta = delta - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(delta);
        delta = delta - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(delta);

        StringBuilder builder = new StringBuilder();
        if (hours > 0 || minutes > 0 || seconds > 0) {
            builder.append(getString(R.string.opened_for_label));
            builder.append(SPACE);
            appendTime(builder, hours, R.string.hours_label);
            appendTime(builder, minutes, R.string.minutes_label);
            appendTime(builder, seconds, R.string.seconds_label);
        }
        return builder.toString();
    }

    private void appendTime(StringBuilder builder, long value, int text) {
        if (value > 0) {
            builder.append(value);
            builder.append(SPACE);
            builder.append(getString(text));
            builder.append(SPACE);
        }
    }

    private int diff(int valueMax, int valueMin) {
        return (valueMax - valueMin) * -1;
    }

    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView subTitle;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.imageview_lock);
            title = (TextView) v.findViewById(R.id.textView_title);
            subTitle = (TextView) v.findViewById(R.id.textView_subtitle);
        }
    }

    private String getString(int text) {
        return mContext.getString(text);
    }
}
