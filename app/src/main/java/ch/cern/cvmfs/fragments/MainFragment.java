package ch.cern.cvmfs.fragments;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.molina.cvmfs.repository.Repository;

import java.io.File;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.listeners.RepositoryStatusListener;
import ch.cern.cvmfs.model.RepositoryDescription;
import ch.cern.cvmfs.model.RepositoryManager;


public class MainFragment extends CVMFSFragment
		implements SplashFragment.SplashListener, DrawerFragment.DrawerListener,
		RepositorySelectionFragment.RepositorySelectionListener, BrowserFragment.BrowserFragmentListener {

	private Toolbar toolbar;
	private View mView;
	private TextView menuTitleTextView;
	private ImageView drawerSwitcherImg;
	private DrawerLayout drawerLayout;
	private ImageView menuBackImg;
	private ImageView menuLogoImageView;
	private View mRightDrawerView;
	private ProgressDialog progressDialog;

	private static final int MAX_PATH_LENGTH = 25;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return mView = inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		prepareFragments();
		if (savedInstanceState == null) {
			loadSplash();
		}
	}

	@Override
	protected void onPrepareInterface() {
		toolbar = (Toolbar) mView.findViewById(R.id.main_toolbar);
		mRightDrawerView = mView.findViewById(R.id.loggedin_main_right_frame);
		drawerLayout = (DrawerLayout) mView.findViewById(R.id.drawer_content_layout);
		drawerSwitcherImg = (ImageView) mView.findViewById(R.id.menu_drawer_btn);
		drawerSwitcherImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (drawerLayout.isDrawerOpen(mRightDrawerView)) {
					drawerLayout.closeDrawer(mRightDrawerView);
				} else {
					drawerLayout.openDrawer(mRightDrawerView);
				}
			}
		});
		menuBackImg = (ImageView) mView.findViewById(R.id.menu_back_btn);
		menuTitleTextView = (TextView) mView.findViewById(R.id.menu_title_textview);
		menuLogoImageView = (ImageView) mView.findViewById(R.id.menu_logo_imageview);
		menuBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void prepareFragments() {
		replaceFragment(new DrawerFragment(), R.id.loggedin_main_right_frame);
		drawerLayout.setDrawerListener(new ActionBarDrawerToggle(getActivity(),
				drawerLayout, R.string.drawer_opened, R.string.drawer_closed));
	}

	@Override
	public boolean onBackPressed() {
		if (drawerLayout.isDrawerOpen(mRightDrawerView)) {
			drawerLayout.closeDrawer(mRightDrawerView);
			return true;
		}
		CVMFSFragment currentFragment = getCurrentFragment(R.id.main_container_frame);
		return currentFragment != null && currentFragment.onBackPressed();
	}

	public void loadSplash() {
		toolbar.setVisibility(View.GONE);
		replaceFragment(new SplashFragment(), R.id.main_container_frame);
	}

	private void loadRepositorySelection() {
		toolbar.setVisibility(View.VISIBLE);
		menuTitleTextView.setVisibility(View.GONE);
		menuLogoImageView.setVisibility(View.VISIBLE);
		replaceFragment(new RepositorySelectionFragment(), R.id.main_container_frame);
	}

	private void replaceBrowserPath(String newPath) {
		menuLogoImageView.setVisibility(View.GONE);
		String displayPath = newPath;
		if (displayPath.length() > MAX_PATH_LENGTH) {
			displayPath = displayPath.substring(0, MAX_PATH_LENGTH) + "...";
		}
		menuTitleTextView.setText(displayPath);
		CVMFSFragment newFragment = new BrowserFragment();
		Bundle bundle = new Bundle();
		bundle.putString("path", newPath);
		newFragment.setArguments(bundle);
		replaceFragment(newFragment, R.id.main_container_frame);
	}

	private void loadRepositoryView(RepositoryDescription chosen) {
		// find the drawer fragment first
		RepositoryStatusListener rsl = (RepositoryStatusListener)getCurrentFragment(R.id.loggedin_main_right_frame);
		rsl.repositoryChanged(chosen);
		new LoadNewRepository(chosen.getUrl()).execute();
	}

	@Override
	public void splashLoaded() {
		loadRepositorySelection();
	}

	@Override
	public void drawerHomeSelected() {
		Repository repo = RepositoryManager.getInstance().getRepositoryInstance();
		if (repo == null) {
			Toast.makeText(getActivity(), R.string.toast_repository_not_selected,
					Toast.LENGTH_SHORT).show();
			return;
		}
		menuTitleTextView.setVisibility(View.VISIBLE);
		menuTitleTextView.setText("/");
		replaceBrowserPath("");
	}

	@Override
	public void repositoryChosen(RepositoryDescription chosen) {
		loadRepositoryView(chosen);
	}

	@Override
	public void directorySelected(String absolutePath) {
		replaceBrowserPath(absolutePath);
	}

	@Override
	public void fileSelected(String absolutePath) {
		// TODO show the detail view
	}

	@Override
	public void browserBack() {
		loadRepositorySelection();
	}

	private class LoadNewRepository extends AsyncTask<Void, Void, Void> {

		String url;

		public LoadNewRepository(String url) {
			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(),
					getResources().getString(R.string.dialog_loading),
					getResources().getString(R.string.dialog_downloading_catalog),
					true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			ContextWrapper cw = new ContextWrapper(getActivity());
			File mainStorageDirectory = new File(cw.getFilesDir() + File.separator + "CernVM_FS");
			RepositoryManager.getInstance().setRepositoryInstance(url, mainStorageDirectory.getAbsolutePath());
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			progressDialog.dismiss();
			if (RepositoryManager.getInstance().getRepositoryInstance() == null) {
				Toast.makeText(getActivity(), R.string.toast_repository_not_loadable, Toast.LENGTH_SHORT).show();
				return;
			}
			menuTitleTextView.setVisibility(View.VISIBLE);
			replaceBrowserPath("");
		}
	}
}
