package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.listeners.RepositoryStatusListener;
import ch.cern.cvmfs.model.RepositoryDescription;

public class DrawerFragment extends CVMFSFragment implements RepositoryStatusListener {

	private View mView;
	private LinearLayout rootFolderLayout;
	private LinearLayout addRepositoryLayout;
	private LinearLayout tagsLayout;
	private LinearLayout selectDateLayout;
	private DrawerListener mCallback;
	private TextView drawerFQRNTextview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return mView = inflater.inflate(R.layout.fragment_drawer, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		try {
			mCallback = (DrawerListener) getParentFragment();
		} catch (ClassCastException e) {
			throw new RuntimeException(getParentFragment() + " must implement " +
					DrawerListener.class.getName());
		}
		super.onAttach(activity);
	}

	protected void onPrepareInterface() {
		rootFolderLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_root_layout);
		rootFolderLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.drawerHomeSelected();
			}
		});
		addRepositoryLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_add_repository);
		addRepositoryLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCallback.addRepositorySelected();
			}
		});
		tagsLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_list_tags);
		tagsLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCallback.tagsSelected();
			}
		});
		selectDateLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_select_date);
		selectDateLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCallback.dateSelected();
			}
		});
		drawerFQRNTextview = (TextView) mView.findViewById(R.id.drawer_fqrn_textview);
	}

	@Override
	public void repositoryChanged(RepositoryDescription repo) {
		drawerFQRNTextview.setText(repo.getFqrn());
	}

	public interface DrawerListener {
		void drawerHomeSelected();

		void addRepositorySelected();

		void tagsSelected();

		void dateSelected();
	}
}
