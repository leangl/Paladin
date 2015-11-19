package mobi.tattu.utils.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.navi.Event;
import com.trello.navi.Listener;
import com.trello.navi.NaviComponent;
import com.trello.navi.internal.BaseNaviComponent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import mobi.tattu.utils.R;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.activities.BaseActivity;
import mobi.tattu.utils.annotations.Nullable;
import mobi.tattu.utils.annotations.SaveState;
import mobi.tattu.utils.log.Logger;
import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment implements BaseActivity.OnBackListener, NaviComponent {

    private final BaseNaviComponent base = BaseNaviComponent.createFragmentComponent();

    @Override
    public <T> boolean hasEvent(Event<T> event) {
        return base.hasEvent(event);
    }

    @Override
    public <T> void addListener(Event<T> event, Listener<T> listener) {
        base.addListener(event, listener);
    }

    @Override
    public <T> void removeListener(Event<T> event, Listener<T> listener) {
        base.removeListener(event, listener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        base.onAttach(activity);
    }

    // FIXME Android M
    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        base.onAttach(context);
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        base.onCreate(savedInstanceState);

        // Automatically restores Fragment state annotated with {@link mobi.tattu.utils.annotations.SaveState} annotation.
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        base.onCreateView(savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        base.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        base.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        base.onStart();
        Tattu.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        base.onResume();
        getBaseActivity().setOnBackListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        base.onPause();
        getBaseActivity().removeOnBackListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        base.onStop();
        Tattu.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        base.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        base.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        base.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        base.onSaveInstanceState(outState);

        // Automatically saves Fragment state annotated with {@link SaveState} annotation.
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        base.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        base.onActivityResult(requestCode, resultCode, data);
    }

    // FIXME Android M
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        base.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

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

    protected void showLoading(boolean cancelable, int resId) {
        if (getBaseActivity() != null) getBaseActivity().showLoading(cancelable, resId);
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public boolean onBackPressed() {
        return false;
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

    public void toast(Throwable error) {
        toast(error != null ? error.getMessage() : getString(R.string.unknown_error));
    }

    public <T extends View> T inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return (T) LayoutInflater.from(getActivity()).inflate(resource, root, attachToRoot);
    }

    public <T extends View> T findViewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

}
