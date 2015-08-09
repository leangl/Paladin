package mobi.tattu.utils.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import mobi.tattu.utils.R;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.billing.IabHelper;
import mobi.tattu.utils.fragments.BaseFragment;
import roboguice.RoboGuice;
import roboguice.activity.RoboActionBarActivity;

public class BaseActivity extends RoboActionBarActivity {

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    private OnBackListener mOnBackListener;
    private ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        Tattu.bus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Tattu.bus().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
            progressDialog = ProgressDialog.show(this, null, message, true, cancelable);
            progressDialog.setOnCancelListener(dialog -> progressDialog = null);
            progressDialog.setOnDismissListener(dialog -> progressDialog = null);
        } else {
            Log.d("baseActivity", "Already showing progress dialog.");
        }
    }

    @Override
    public void onBackPressed() {
        if (mOnBackListener != null) {
            if (!mOnBackListener.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
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
                    NavUtils.navigateUpFromSameTask(this);
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
            mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getIabHelper() != null && !getIabHelper().handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
