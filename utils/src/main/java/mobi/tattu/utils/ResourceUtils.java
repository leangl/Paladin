package mobi.tattu.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

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

    public static String toString(Enum<?> e) {
        return ResourceUtils.stringByName(e.getDeclaringClass().getSimpleName().toLowerCase() + "." + e.name().toLowerCase(), e.name().toLowerCase());
    }

    /**
     * Devuelve un ImageView buscando en el los resources
     *
     * @param context
     * @param id      Imagen
     * @return Drawable
     */
    public static Drawable getDrawableResources(Context context, int id) {
        Drawable image = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image = context.getResources().getDrawable(id, null);
        } else {
            image = context.getResources().getDrawable(id);
        }
        return image;
    }

}
