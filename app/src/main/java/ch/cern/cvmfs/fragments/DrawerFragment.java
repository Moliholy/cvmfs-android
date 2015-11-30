package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
    private LinearLayout exitLayout;
	private DrawerListener mCallback;
	private TextView drawerTopTextview;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            String topText = savedInstanceState.getString("top_text");
            drawerTopTextview.setText(topText);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String topText = drawerTopTextview.getText().toString();
        outState.putString("top_text", topText);
        super.onSaveInstanceState(outState);
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
				mCallback.drawerAddRepositorySelected();
			}
		});
        drawerTopTextview = (TextView) mView.findViewById(R.id.drawer_fqrn_textview);
		tagsLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_list_tags);
		tagsLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCallback.drawerTagsSelected();
			}
		});
        exitLayout = (LinearLayout) mView.findViewById(R.id.drawer_option_exit_layout);
        exitLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCallback.drawerExitSelected();
                drawerTopTextview.setText("");
            }
        });
	}

	@Override
	public void repositoryChanged(RepositoryDescription repo) {
        Log.d(DrawerFragment.class.getName(), "Changing to FQRN to " + repo.getFqrn());
        drawerTopTextview.setText(repo.getFqrn());
	}

	public interface DrawerListener {
		void drawerHomeSelected();

		void drawerAddRepositorySelected();

		void drawerTagsSelected();

        void drawerExitSelected();
	}
}
