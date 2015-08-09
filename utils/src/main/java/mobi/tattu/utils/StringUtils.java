package mobi.tattu.utils;

/**
 * Created by Leandro on 21/7/2015.
 */
public class StringUtils {

    public static boolean isBlank(CharSequence text) {
        return text == null || text.toString().trim().isEmpty();
    }

    public static boolean isNotBlank(CharSequence text) {
        return !isBlank(text);
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
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
