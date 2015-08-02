package mobi.tattu.utils.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HintAdapter<T> extends ArrayAdapter<T> {

    private int hiddenItemIndex;
    private LayoutInflater mInflater;
    private int mResource;

    public HintAdapter(Context context, List<T> objects, int layoutResId) {
        this(context, objects, 0, layoutResId);
    }

    public HintAdapter(Context context, List<T> objects, int hiddenItemIndex, int layoutResId) {
        super(context, layoutResId, android.R.id.text1, objects);
        mInflater = LayoutInflater.from(context);
        mResource = layoutResId;
        this.hiddenItemIndex = hiddenItemIndex;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        if (position == hiddenItemIndex) {
            ((TextView) v.findViewById(android.R.id.text1)).setText("");
        }
        return v;
    }
}