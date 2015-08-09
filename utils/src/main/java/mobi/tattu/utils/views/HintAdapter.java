package mobi.tattu.utils.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter custom para permitir tener un item en el Spinner que al seleccionarlo muestre un
 * hint.
 *
 * @param <T>
 */
public class HintAdapter<T> extends ArrayAdapter<HintAdapter.Option<T>> {

    private int hintItemIndex;
    private String hint;

    public HintAdapter(Context context, List<T> objects, int layoutResId) {
        this(context, objects, 0, layoutResId);
    }

    /**
     * El hint se toma de la lista de objetos. El objeto usado como hint es el que se encuentre
     * en hintItemIndex
     */
    public HintAdapter(Context context, List<T> objects, int hintItemIndex, int layoutResId) {
        super(context, layoutResId, android.R.id.text1, getWrappers(objects, null));
        this.hintItemIndex = hintItemIndex;
    }

    /**
     * El hint se toma del valor del parámetro hint
     */
    public HintAdapter(Context context, List<T> objects, String hint, int layoutResId) {
        super(context, layoutResId, android.R.id.text1, getWrappers(objects, hint));
        this.hintItemIndex = 0;
        this.hint = hint;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        if (position == hintItemIndex) {
            ((TextView) v.findViewById(android.R.id.text1)).setText("");
        }
        return v;
    }

    private static <T> List<Option<T>> getWrappers(List<T> objects, String hint) {
        List<Option<T>> wrappers = new ArrayList<>(objects.size() + 1);
        if (hint != null) wrappers.add(new Option(hint));
        for (T t : objects) wrappers.add(new Option(t));
        return wrappers;
    }

    public static class Option<T> {
        private T wrapped;
        private String hint;

        private Option(String hint) {
            this.hint = hint;
        }

        private Option(T wrapped) {
            this.wrapped = wrapped;
        }

        public T get() {
            return wrapped;
        }

        @Override
        public String toString() {
            if (wrapped != null) {
                return wrapped.toString();
            } else {
                return hint;
            }
        }

    }
}