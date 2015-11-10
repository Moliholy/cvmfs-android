package ch.cern.cvmfs.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.File;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.fragments.CVMFSFragment;
import ch.cern.cvmfs.fragments.MainFragment;
import ch.cern.cvmfs.model.RepositoryManager;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    File mainStorageDirectory = new File(RepositoryManager.CACHE_PATH);
	    if (!mainStorageDirectory.exists()) {
		    boolean result = mainStorageDirectory.mkdirs();
		    if (!result) {
			    Log.e("FileStorage", "Couldn't create the directory \'" + RepositoryManager.CACHE_PATH + "\'");
		    }
	    }
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            loadMainFragment();
        }
    }

    @Override
    public void onBackPressed() {
        CVMFSFragment mainFragment = getCurrentFragment(R.id.container);
        if (mainFragment != null && mainFragment.onBackPressed()) {
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
