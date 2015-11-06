package ch.cern.cvmfs.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.cern.cvmfs.R;


public class MainFragment extends CVMFSFragment implements SplashFragment.SplashListener {

    private Toolbar toolbar;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadSplash(false);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void OnPrepareInterface() {
        toolbar = (Toolbar) mView.findViewById(R.id.main_toolbar);
    }

    public void loadSplash(boolean recreateActivity) {
        if (recreateActivity)
            getActivity().recreate();
        replaceFragment(new SplashFragment(), R.id.main_container_frame);
    }

    private void loadRepositorySelection() {
        toolbar.setVisibility(View.GONE);
        replaceFragment(new RepositorySelectionFragment(), R.id.main_container_frame);
    }

    @Override
    public void splashLoaded() {
        loadRepositorySelection();
    }
}
