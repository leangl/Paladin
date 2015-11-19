package com.nanospark.gard.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.SettingsFragment;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by cristian on 16/10/15.
 */
public class SettingsActivity extends BaseActivity {

    private SettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getToolbar().setTitle(R.string.title_settings);

        TextView textView = (TextView) getViewInToolbar(R.id.textview_menu_overflow);
        textView.setVisibility(View.VISIBLE);
        textView.setText("SAVE");
        textView.setOnClickListener(v -> {
            if (mFragment != null) {
                showLoading(false);
                mFragment.save().observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ok -> {
                            stopLoading();
                            if (ok) {
                                finish();
                            }
                        });
            }
        });

    }

    @Override
    public Fragment getFragment() {
        mFragment = SettingsFragment.newInstance();
        return mFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                mFragment.save();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}
