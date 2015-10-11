package com.nanospark.gard.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.model.log.Log;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by cristian on 10/10/15.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {



    private final String SPACE = " ";
    private final String COMMA = ",";
    private Context mContext;
    private ArrayList<Log> mLogArrayList;
    private TextView textView;

    public LogAdapter(ArrayList<Log> logArrayList){
        this.mLogArrayList = logArrayList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_log, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log log = mLogArrayList.get(position);
        String subTitle = mContext.getResources().getString(R.string.opened_label) + " for ";
        if(log.getEvent().equals(Log.EVENT_CLOSE)){
            String uri = "drawable://" + R.drawable.ic_unlock_2;
            ImageLoader.getInstance().displayImage(uri, holder.mLockImageView);
            textView.setText("Abierto por " + position);
            textView.setVisibility(View.VISIBLE);
        }
        StringBuilder builder = getDate(log);
        holder.mTitleTextView.setText(Html.fromHtml(builder.toString()));
        textView = holder.mSubTitleTextView;
        if(position == getItemCount() -1 ){
            textView.setText("Abierto por " + position);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    private StringBuilder getDate(Log log) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(log.getDate());
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT, Locale.US);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        builder.append("<b>");
        builder.append(dayOfWeek);
        builder.append(SPACE);
        builder.append(dayOfMonth);
        builder.append(COMMA);
        builder.append("</b>");
        builder.append(SPACE);
        builder.append(getHour(calendar));
        return builder;
    }

    private String getHour(Calendar calendar){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        StringBuilder builder = new StringBuilder();
        builder.append(hour > 9 ? hour : "0" + hour );
        builder.append(":");
        builder.append(minute > 9 ? minute : "0" + minute);
        return builder.toString();
    }


    @Override
    public int getItemCount() {
        return mLogArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mLockImageView;
        public TextView mTitleTextView;
        public TextView mSubTitleTextView;

        public ViewHolder(View v) {
            super(v);
            mLockImageView = (ImageView) v.findViewById(R.id.imageview_lock);
            mTitleTextView = (TextView) v.findViewById(R.id.textView_title);
            mSubTitleTextView = (TextView) v.findViewById(R.id.textView_subtitle);
        }
    }

}
