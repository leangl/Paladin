package mobi.tattu.utils;

import android.content.res.Resources;

/**
 * Created by Leandro on 21/7/2015.
 */
public class ResourceUtils {

    public static String stringByName(String name) {
        return getString(name);
    }

    public static String stringByName(String name, String defValue) {
        if (StringUtils.isBlank(name)) return getString(defValue);
        try {
            return getString(name);
        } catch (Resources.NotFoundException e) {
            return getString(defValue);
        }
    }

    private static String getString(String name) throws Resources.NotFoundException {
        return Tattu.context.getResources().getString(
                Tattu.context.getResources().getIdentifier(
                        name,
                        "string",
                        Tattu.context.getPackageName()
                )
        );
    }


}
