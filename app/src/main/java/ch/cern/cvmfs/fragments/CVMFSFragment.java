package ch.cern.cvmfs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;


public abstract class CVMFSFragment extends Fragment {

    public boolean onBackPressed() {
        return false;
    }

    protected void replaceFragment(CVMFSFragment fragment, int containerId, String name) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment, name).commit();
        getActivity().overridePendingTransition(0, 0);
    }

    protected void replaceFragment(CVMFSFragment fragment, int containerId) {
        replaceFragment(fragment, containerId, null);
    }

    protected void addFragment(CVMFSFragment fragment, int containerId, String name) {
        getChildFragmentManager().beginTransaction().add(containerId, fragment)
                .addToBackStack(name).commit();
        getActivity().overridePendingTransition(0, 0);
    }

    protected void popFragment(int containerId, String name) {
        removeFragment(containerId);
        getChildFragmentManager().popBackStackImmediate(name, 0);
        getActivity().overridePendingTransition(0, 0);
    }

    protected void removeFragment(int containerId) {
        CVMFSFragment currentFragment = getCurrentFragment(containerId);
        getChildFragmentManager().beginTransaction().remove(currentFragment).commit();
        getActivity().overridePendingTransition(0, 0);
    }

    protected void removeFragments(String name) {
        getChildFragmentManager().popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    /**
     * Called after onViewCreated
     */
    protected abstract void onPrepareInterface();
}
