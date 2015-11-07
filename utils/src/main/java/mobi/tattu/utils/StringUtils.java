package mobi.tattu.utils;

import android.widget.TextView;

/**
 * Created by Leandro on 21/7/2015.
 */
public class StringUtils {

    public static boolean isBlank(TextView text) {
        return text == null || isBlank(text.getText());
    }

    public static boolean isBlank(CharSequence text) {
        return text == null || text.toString().trim().isEmpty();
    }

    public static boolean isNotBlank(TextView text) {
        return text != null && isNotBlank(text.getText());
    }

    public static boolean isNotBlank(CharSequence text) {
        return !isBlank(text);
    }

    public static boolean isEmpty(TextView text) {
        return text == null || isEmpty(text.getText());
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static boolean isNotEmpty(TextView text) {
        return text != null && isNotEmpty(text.getText());
    }

    public static boolean isNotEmpty(CharSequence text) {
        return !isEmpty(text);
    }

    public static boolean equals(CharSequence s1, CharSequence s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 != null) {
            return s1.equals(s2);
        }
        if (s2 != null) {
            return s2.equals(s1);
        }
        return false;
    }

}
