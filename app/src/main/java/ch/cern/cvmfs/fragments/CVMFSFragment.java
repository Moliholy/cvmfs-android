package ch.cern.cvmfs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;


public class CVMFSFragment extends Fragment {

    public boolean onBackPressed() {
        return false;
    }

    protected void replaceFragment(CVMFSFragment fragment, int containerId) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onPrepareInterface();
    }

    protected CVMFSFragment getCurrentFragment(int containerId) {
        try {
            return (CVMFSFragment) getChildFragmentManager().findFragmentById(containerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPrepareInterface() {

    }
}
