package mobi.tattu.utils.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import mobi.tattu.utils.R;

/**
 * Created by Leandro on 8/8/2015.
 */
public abstract class BaseDrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        FragmentManager fm = getSupportFragmentManager();

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        ((ListView) mNavigationView.getChildAt(0)).setSelector(new ColorDrawable(Color.TRANSPARENT));

        fm.addOnBackStackChangedListener(() -> {
            if (fm.getBackStackEntryCount() == 0) {
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        toolbar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        start(getFragmentForDrawerItem(mNavigationView.getMenu().getItem(0).getItemId()), false);
    }

    /**
     * @param id el id del item del menu: menuItem.getItemId()
     * @return fragment correspondiente al item seleccionado. Si es null entonces no se abre nada.
     */
    protected abstract Fragment getFragmentForDrawerItem(int id);

    public void startDrawerFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0 || !fragment.getClass().getSimpleName().equals(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName())) {
            for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                fm.popBackStackImmediate();
            }
            start(fragment, true);
        }
        // TODO marker item del drawer como seleccionado
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        if (mNavigationView.getMenu().getItem(0).getItemId() == menuItem.getItemId()) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
            menuItem.setChecked(true);
        } else {
            Fragment fragment = getFragmentForDrawerItem(menuItem.getItemId()); // FIXME es poco eficiente, se pide una instancia sin saber realmente si va a mostrarse
            if (fragment != null) {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() == 0 || !fragment.getClass().getSimpleName().equals(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName())) {
                    fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    start(fragment, true);
                }
                menuItem.setChecked(true);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
