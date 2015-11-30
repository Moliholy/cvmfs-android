package ch.cern.cvmfs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;


public abstract class CVMFSFragment extends Fragment {

	public boolean onBackPressed() {
		return false;
	}

	protected void replaceFragment(CVMFSFragment fragment, int containerId) {
		getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
		getActivity().overridePendingTransition(0, 0);
	}

    protected void addFragment(CVMFSFragment fragment, int containerId) {
        getChildFragmentManager().beginTransaction().add(containerId, fragment)
                .addToBackStack(null).commit();
        getActivity().overridePendingTransition(0, 0);
    }

    protected void removeFragment(int containerId) {
        CVMFSFragment currentFragment = getCurrentFragment(containerId);
        getChildFragmentManager().beginTransaction().remove(currentFragment).commit();
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
