package mobi.tattu.utils.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import mobi.tattu.utils.R;
import mobi.tattu.utils.Tattu;
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

}
