package ch.cern.cvmfs.fragments;

import android.support.v4.app.Fragment;


public class CVMFSFragment extends Fragment {

    public boolean backPressed() {
        return false;
    }

    protected void replaceFragment(CVMFSFragment fragment, int containerId) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
        getActivity().overridePendingTransition(0, 0);
    }
}
