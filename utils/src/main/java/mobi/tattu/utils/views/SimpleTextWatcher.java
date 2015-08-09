package mobi.tattu.utils.views;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Simple class that extends and implements every TextWatcher method.
 * This allows shorter code when implementing TextWatcher interface inline.
 * <p>
 * Created by Leandro on 28/7/2015.
 */
public class SimpleTextWatcher implements TextWatcher {

    private BeforeTextChanged before;
    private TextChanged changed;
    private AfterTextChanged after;

    public SimpleTextWatcher() {
    }

    public SimpleTextWatcher(AfterTextChanged after, BeforeTextChanged before, TextChanged changed) {
        this.after = after;
        this.before = before;
        this.changed = changed;
    }

    public SimpleTextWatcher(TextChanged changed) {
        this.changed = changed;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (before != null) before.beforeTextChanged(start, count, after, s);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (changed != null) changed.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (after != null) after.afterTextChanged(s);
    }

    public interface BeforeTextChanged {
        void beforeTextChanged(int start, int count, int after, CharSequence s);
    }

    public interface TextChanged {
        void onTextChanged(CharSequence s, int start, int before, int count);
    }

    public interface AfterTextChanged {
        void afterTextChanged(Editable s);
    }
}
