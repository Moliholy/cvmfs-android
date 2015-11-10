package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ch.cern.cvmfs.R;

public class DrawerFragment extends CVMFSFragment {

	private View mView;
	private LinearLayout rootLayout;
	private DrawerListener mCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_drawer, container, false);
		return mView;
	}

	@Override
	public void onAttach(Activity activity) {
		try {
			mCallback = (DrawerListener) getParentFragment();
		} catch (ClassCastException e) {
			throw new RuntimeException(getParentFragment() + " must implement DrawerListener!");
		}
		super.onAttach(activity);
	}

	protected void onPrepareInterface() {
		rootLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_root_layout);
		rootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.drawerHomeSelected();
			}
		});
	}

	public interface DrawerListener {
		void drawerHomeSelected();

	}
}
