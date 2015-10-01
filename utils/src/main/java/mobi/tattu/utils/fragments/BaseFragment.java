package mobi.tattu.utils.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.tattu.utils.activities.BaseActivity;
import mobi.tattu.utils.annotations.SaveState;
import mobi.tattu.utils.log.Logger;
import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment implements BaseActivity.OnBackListener {

    /**
     * Automatically restores Fragment state annotated with {@link mobi.tattu.utils.annotations.SaveState} annotation.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(this, "Restoring fragment state");
        if (savedInstanceState != null) {
            try {
                for (Field field : getFields(getClass())) {
                    if (field.isAnnotationPresent(SaveState.class)) {
                        Logger.d(this, "Restoring fragment state for field: " + field.getName());
                        field.setAccessible(true);
                        Serializable value = savedInstanceState.getSerializable(field.getName());
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                Logger.e(this, "Error restoring fragment state.", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Automatically saves Fragment state annotated with {@link SaveState} annotation.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Logger.d(this, "Saving fragment state");
        try {
            for (Field field : getFields(getClass())) {
                if (field.isAnnotationPresent(SaveState.class)) {

                    field.setAccessible(true);
                    if (field.get(this) != null && !Serializable.class.isAssignableFrom(field.get(this).getClass())) {
                        throw new Exception("Field " + field.getName() + " annotated with SaveState but not serializable.");
                    }

                    outState.putSerializable(field.getName(), (Serializable) field.get(this));
                }
            }
        } catch (Exception e) {
            Logger.e(this, "Error saving fragment state", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Return all public or private fields in the given class.
     *
     * @param startClass
     * @return
     */
    private static Iterable<Field> getFields(Class<?> startClass) {

        List<Field> currentClassFields = new ArrayList<Field>(Arrays.asList(startClass.getDeclaredFields()));

        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(BaseFragment.class))) {
            List<Field> parentClassFields = (List<Field>) getFields(parentClass);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public void start(Fragment fragment) {
        start(fragment, true);
    }

    public void start(Fragment fragment, boolean addToBackStack) {
        ((BaseActivity) getActivity()).start(fragment, addToBackStack);
    }

    public boolean finish() {
        return getBaseActivity().popBackStack();
    }

    protected void stopLoading() {
        if (getBaseActivity() != null) getBaseActivity().stopLoading();
    }

    protected void showLoading() {
        showLoading(false);
    }

    protected void showLoading(boolean cancelable) {
        if (getBaseActivity() != null) getBaseActivity().showLoading(cancelable);
    }

    protected  void showLoading(boolean cancelable, int resId) {
        if (getBaseActivity() != null) getBaseActivity().showLoading(cancelable, resId);
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        getBaseActivity().removeOnBackListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getBaseActivity().setOnBackListener(this);
    }

    public void changeTitleActionBar(String title) {
        getBaseActivity().changeTitleActionBar(title);
    }


    public void snackbar(int resId) {
        getBaseActivity().snackbar(resId);
    }

    public void snackbar(String message) {
        getBaseActivity().snackbar(message);
    }

    public void toast(int resId) {
        getBaseActivity().toast(resId);
    }

    public void toast(String message) {
        getBaseActivity().toast(message);
    }

}
