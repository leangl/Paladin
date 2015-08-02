package mobi.tattu.utils.views;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import mobi.tattu.utils.log.Logger;

public class Fonts {

    private static Map<String, Typeface> fonts = new HashMap<String, Typeface>();

    public static Typeface getFont(Context context, String fontName) {
        Typeface typeface = fonts.get(fontName);
        if (typeface != null) {
            return typeface;
        } else {
            try {
                Typeface newFont = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
                fonts.put(fontName, newFont);
                return newFont;
            } catch (Exception e) {
                Logger.w(Fonts.class, "Font not found: " + fontName);
                return null;
            }
        }
    }

}

