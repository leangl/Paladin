package mobi.tattu.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.BuildConfig;
import android.util.Patterns;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import mobi.tattu.utils.log.Logger;

/**
 * Created by Leandro on 30/05/2015.
 */
public class Utils {
    public static String MULTIVALUE_PREFERENCE_SEPARATOR = "|";
    private static final SimpleDateFormat FILENAME_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_hhmm");
    private static final char[] FILE_ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPhoneValid(CharSequence phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidFilename(CharSequence filename) {
        String filenameStr = filename.toString();
        for (char illegalChar : FILE_ILLEGAL_CHARACTERS) {
            if (filenameStr.indexOf(illegalChar) != -1) {
                return false;
            }
        }
        return true;
    }

    private static final int MAX_INCREMENT = 3;

    public static String increment(String s) throws Exception {
        if (s == null || s.isEmpty()) return "a";

        char lastChar = s.charAt(s.length() - 1);
        if (lastChar < 'z') {
            return s.substring(0, s.length() - 1) + (char) (lastChar + 1);
        } else {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) < 'z') {
                    char[] arr = s.toCharArray();
                    arr[i] = (char) (arr[i] + 1);
                    for (int j = 0; j < i; j++) arr[j] = 'a';
                    arr[arr.length - 1] = 'a';
                    return new String(arr);
                }
            }
            if (s.length() < MAX_INCREMENT) {
                char arr[] = new char[s.length() + 1];
                for (int i = 0; i < s.length() + 1; i++) arr[i] = 'a';
                return new String(arr);
            }
        }

        throw new Exception("Max filename: " + s);
    }

    public static File resizeBitmapFile(File file) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        int maxHeight = 640;
        int maxWidth = 480;
        float scale = Math.min(((float) maxHeight / bitmap.getWidth()), ((float) maxWidth / bitmap.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //create a file to write bitmap data
        File f = new File(Tattu.context.getCacheDir(), file.getName());
        f.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return f;
    }

    public static Comparator<Size> SIZE_COMPARATOR = new Comparator<Size>() {

        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.height * lhs.width > rhs.height * rhs.width) {
                return -1;
            } else if (lhs.height * lhs.width > rhs.height * rhs.width) {
                return 1;
            }
            return 0;
        }

    };


    /**
     * Get the size in bytes of a bitmap.
     *
     * @param bitmap
     * @return size in bytes
     */
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static final int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Returns the min size from the list.
     *
     * @param sizes
     * @return
     */
    public static Size getMinSize(List<Size> sizes) {
        Size minSize = null;
        for (Size size : sizes) {
            if (minSize == null || size.width * size.height < minSize.width * minSize.height) {
                minSize = size;
            }
        }
        return minSize;
    }

    /**
     * Returns the next size equal or higher than w * h.
     */
    public static Size nextHigherSize(List<Size> sizes, int w, int h) {
        Size closest = null;
        int closestDiff = 0;
        for (Size size : sizes) {
            int sizeDiff = size.width * size.height - w * h;
            if (closest == null || sizeDiff > 0 && sizeDiff < closestDiff) {
                closest = size;
                closestDiff = sizeDiff;
            }
        }
        return closest;
    }

    /**
     * Returns the max size from the list.
     *
     * @param sizes
     * @return
     */
    public static Size getMaxSize(List<Size> sizes) {
        Size maxSize = null;
        for (Size size : sizes) {
            if (maxSize == null || size.width * size.height > maxSize.width * maxSize.height) {
                maxSize = size;
            }
        }
        return maxSize;
    }

    public Size getOptimalSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static String getSizeString(Size size) {
        StringBuilder sizeEntry = new StringBuilder();
        return sizeEntry.append(size.width).append("x").append(size.height).toString();
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    //    /**
//     * Devuelve un ImageView buscando en el los resources
//     * @param context
//     * @param id Imagen
//     * @return Drawable
//     */
//    public static Drawable getDrawableResources(Context context, int id){
//        Drawable image = null;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            image = context.getResources().getDrawable(id,null);
//        }else{
//            image = context.getResources().getDrawable(id);
//        }
//        return image;
//    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }



    public static void init(Application app) {

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Logger.e("ERROR", ex);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }


    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * Returns the max and min Iso values supported by the camera
     *
     * @param params
     * @return [max, min] iso values or null if not supported
     */
    public static String[] findMaxMinIsoValues(Camera.Parameters params) {
        String isoValues = params.get("iso-values");
        if (!StringUtils.isEmpty(isoValues)) {
            Integer minIsoValue = null;
            Integer maxIsoValue = null;
            String minIso = null;
            String maxIso = null;
            String[] isoValuesArr = isoValues.split(",");
            for (int i = 0; i < isoValuesArr.length; i++) {
                try {
                    Integer val = parseIsoValue(isoValuesArr[i]);
                    if (val != null) {
                        if (maxIsoValue == null || val > maxIsoValue) {
                            maxIsoValue = val;
                            maxIso = isoValuesArr[i];
                        }
                        if (minIsoValue == null || val < minIsoValue) {
                            minIsoValue = val;
                            minIso = isoValuesArr[i];
                        }
                    }
                } catch (Exception e) {
                    Logger.i(Utils.class, e);
                }
            }
            if (maxIso != null && minIso != null) {
                return new String[]{maxIso, minIso};
            }
        }
        return null;
    }

    @Deprecated
    public static boolean isEmpty(String text) {
        return StringUtils.isEmpty(text);
    }


    @Deprecated
    public static boolean isBlank(String text) {
        return StringUtils.isBlank(text);
    }

    @Deprecated
    public static boolean isNotBlank(String text) {
        return StringUtils.isNotBlank(text);
    }

    @Deprecated
    public static boolean equals(String s1, String s2) {
        return StringUtils.equals(s1, s2);
    }

    /**
     * Try to parse the ISO value (ie 1600 or ISO1600)
     *
     * @param iso
     * @return Iso Integer value or null if could not be parsed (ie auto)
     */
    public static Integer parseIsoValue(String iso) {
        Scanner in = new Scanner(iso);
        in.useDelimiter("[^0-9]+");
        Integer val = null;
        while (in.hasNextInt()) {
            int nextVal = in.nextInt();
            if (val == null || nextVal > val) {
                val = nextVal;
            }
        }
        in.close();
        return val;
    }

    public static boolean supportsAutoIso(Camera.Parameters params) {
        String isoValues = params.get("iso-values");
        if (!StringUtils.isEmpty(isoValues)) {
            String[] isoValuesArr = isoValues.split(",");
            for (int i = 0; i < isoValuesArr.length; i++) {
                if ("auto".equals(isoValuesArr[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsPreference(String value, TypedPref<String> preference) {
        String multiValuePreference = preference.getValue();
        String[] vals = splitMultiValue(multiValuePreference);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String[] splitMultiValue(String values) {
        return values.split("\\" + MULTIVALUE_PREFERENCE_SEPARATOR);
    }

    /**
     * @return El modelo del telefono
     */
    public static String cellPhoneModel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static String joinMultiValue(List<String> values) {
        StringBuilder sb = new StringBuilder();
        if (values != null) {
            Iterator<String> it = values.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(MULTIVALUE_PREFERENCE_SEPARATOR);
                }
            }
        }
        return sb.toString();
    }
    public static String joinMultiValueByComma(List<String> values) {
        StringBuilder sb = new StringBuilder();
        if (values != null) {
            Iterator<String> it = values.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }
    public static final String getBase64Key() {
        StringBuilder sb = new StringBuilder();
        int evenIdx = 0, oddIdx = 0;
        for (int i = 0; i < Base64.BASE_64_KEYS_ODD.length + Base64.BASE_64_KEYS_EVEN.length; i++) {
            if (i % 2 == 0) { // If even
                sb.append(Base64.BASE_64_KEYS_EVEN[evenIdx++]);
            } else { // if odd
                sb.append(Base64.BASE_64_KEYS_ODD[oddIdx++]);
            }
        }
        return sb.toString();
    }

    /**
     * Saca el valor de la preferencia
     *
     * @param value
     * @param pref
     * @return
     */
    public static String removeValuePreference(String value, TypedPref<String> pref) {
        String multiValuePreference = pref.getValue();
        String[] vals = multiValuePreference.split("\\" + Utils.MULTIVALUE_PREFERENCE_SEPARATOR);
        List<String> newVals = new ArrayList<String>(vals.length);
        for (int i = 0; i < vals.length; i++) {
            if (!vals[i].equals(value)) {
                newVals.add(vals[i]);
            }
        }
        return Utils.joinMultiValue(newVals);
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

    /**
     * Verifica que haya una conexion de internet y este disponible para conectarse.
     *
     * @param context
     * @return true, if successful
     */
    public static boolean checkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return Boolean.FALSE;
        }
        if (!networkInfo.isConnected()) {
            return Boolean.FALSE;
        }
        if (!networkInfo.isAvailable()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

