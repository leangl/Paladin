package mobi.tattu.utils.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.trello.navi.Event;
import com.trello.navi.Listener;
import com.trello.navi.NaviComponent;
import com.trello.navi.internal.BaseNaviComponent;

import mobi.tattu.utils.R;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.billing.IabHelper;
import mobi.tattu.utils.fragments.BaseFragment;
import roboguice.RoboGuice;
import roboguice.activity.RoboActionBarActivity;

public class BaseActivity extends RoboActionBarActivity implements NaviComponent {

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    private OnBackListener mOnBackListener;
    private ProgressDialog progressDialog;
    private final BaseNaviComponent base = BaseNaviComponent.createActivityComponent();


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        base.onCreate(savedInstanceState);
    }

    // TODO https://github.com/square/otto/issues/139
    /*@Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        base.onCreate(savedInstanceState, persistentState);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        base.onStart();
        Tattu.bus().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        base.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        base.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        base.onStop();
        Tattu.bus().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        base.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        base.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        base.onSaveInstanceState(outState);
    }

    // TODO https://github.com/square/otto/issues/139
    /*@Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        base.onSaveInstanceState(outState, outPersistentState);
    }*/

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        base.onRestoreInstanceState(savedInstanceState);
    }

    // TODO https://github.com/square/otto/issues/139
    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        base.onRestoreInstanceState(savedInstanceState, persistentState);
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        base.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (mOnBackListener == null || !mOnBackListener.onBackPressed()) {
            super.onBackPressed();
            base.onBackPressed();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        base.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        base.onDetachedFromWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        base.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getIabHelper() != null && !getIabHelper().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            base.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            base.onActivityResult(requestCode, resultCode, data);
        }
    }

    // FIXME Android M
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        base.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

    public void start(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment);
        if (addToBackStack) {
            tx.addToBackStack(fragment.getClass().getSimpleName());
        }
        tx.commit();
    }

    public <T extends BaseFragment> void start(T fragment) {
        start(fragment, true);
    }

    public boolean popBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    public void stopLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showLoading(boolean cancelable) {
        showLoading(cancelable, R.string.loading);
    }

    public void showLoading(boolean cancelable, int message) {
        showLoading(cancelable, getString(message));
    }

    public void showLoading(boolean cancelable, String message) {
        if (progressDialog == null) {
            runOnUiThread(() -> {
                progressDialog = ProgressDialog.show(this, null, message, true, cancelable);
                progressDialog.setOnCancelListener(dialog -> progressDialog = null);
                progressDialog.setOnDismissListener(dialog -> progressDialog = null);
            });
        } else {
            Log.d("baseActivity", "Already showing progress dialog.");
        }
    }

    public void setOnBackListener(OnBackListener listener) {
        mOnBackListener = listener;
    }

    public void removeOnBackListener(OnBackListener listener) {
        if (mOnBackListener == listener) {
            mOnBackListener = null;
        }
    }

    public interface OnBackListener {
        boolean onBackPressed();
    }

    public void changeTitleActionBar(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!popBackStack()) {
                    try {
                        NavUtils.navigateUpFromSameTask(this);
                    } catch (Exception e) {
                        finish();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void snackbar(int resId) {
        snackbar(getString(resId));
    }

    public void snackbar(String message) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG).show();
    }

    private Toast mToast;

    public void toast(int resId) {
        toast(getString(resId));
    }

    public void toast(String message) {
        Tattu.runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            Spannable centeredText = new SpannableString(message);
            centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    0, message.length() - 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            mToast = Toast.makeText(this, centeredText, Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.BOTTOM, 0, 150);
            mToast.show();
        });
    }

    /**
     * Se tiene que sobreescribir si se va a usar la opcion de comprar desde la aplicacion
     *
     * @return
     */
    public IabHelper getIabHelper() {
        return null;
    }

}
