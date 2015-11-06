package ch.cern.cvmfs.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.cern.cvmfs.cvmfs.R;


public class MainFragment extends CVMFSFragment implements SplashFragment.SplashListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadSplash(false);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadSplash(boolean recreateActivity) {
        if (recreateActivity)
            getActivity().recreate();
        replaceFragment(new SplashFragment(), R.id.main_container_frame);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void splashLoaded() {
        replaceFragment(new RepositorySelectionFragment(), R.id.main_container_frame);
    }
}
