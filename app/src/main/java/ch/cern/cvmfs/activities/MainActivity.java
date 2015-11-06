package ch.cern.cvmfs.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.fragments.CVMFSFragment;
import ch.cern.cvmfs.fragments.MainFragment;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            loadMainFragment();
        }
    }

    @Override
    public void onBackPressed() {
        CVMFSFragment mainFragment = getCurrentFragment(R.id.container);
        if (mainFragment != null && mainFragment.backPressed()) {
            return;
        }
        super.onBackPressed();
    }

    public void loadMainFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new MainFragment())
                .commit();
    }

    protected CVMFSFragment getCurrentFragment(int idContainer) {
        return (CVMFSFragment) getSupportFragmentManager().findFragmentById(idContainer);
    }
}
