package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.cern.cvmfs.R;

public class SplashFragment extends CVMFSFragment {

    private SplashListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (SplashListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException(getParentFragment() + " does not implement SplashListener");
        }
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mCallback.splashLoaded();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    @Override
    protected void onPrepareInterface() {
        // nothing to do here
    }


    public interface SplashListener {
        void splashLoaded();
    }


}
