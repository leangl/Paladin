package com.nanospark.gard.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.CreateUserFragment;

/**
 * Created by cristian on 16/10/15.
 */
public class CreateUserActivity extends BaseActivity {

    private CreateUserListener mListener;
    private String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getIntent() != null){
            mId =getIntent().getStringExtra(CreateUserFragment.ARG_ID_USER);
        }
       super.onCreate(savedInstanceState);
                getSupportActionBar().setTitle(R.string.new_user_label);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public boolean containsTab() {
        return false;
    }

    @Override
    public Fragment getFragment() {
        return CreateUserFragment.newInstance(mId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                mListener.save();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
         return super.onOptionsItemSelected(item);
    }

    public interface CreateUserListener{
        void save();
    }

    public void setListener(CreateUserListener listener){
        this.mListener = listener;
    }
}
