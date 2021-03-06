package ch.cern.cvmfs.activities;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.fragments.CVMFSFragment;
import ch.cern.cvmfs.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextWrapper cw = new ContextWrapper(this);
        File mainStorageDirectory = new File(cw.getFilesDir() + File.separator + "CernVM_FS");
        if (!mainStorageDirectory.exists()) {
            boolean result = mainStorageDirectory.mkdirs();
            if (!result) {
                Log.e("FileStorage", "Couldn't create the directory \'" +
                        mainStorageDirectory.getAbsolutePath() + "\'");
            }
        }
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            loadMainFragment();
        }
    }

	public String getCvmfsCachePath() {
		ContextWrapper cw = new ContextWrapper(this);
		return new File(cw.getFilesDir() + File
				.separator + "CernVM_FS").getAbsolutePath();
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
