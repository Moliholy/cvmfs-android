package ch.cern.cvmfs.fragments;

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

import ch.cern.cvmfs.R;


public class MainFragment extends CVMFSFragment implements SplashFragment.SplashListener {

    private Toolbar toolbar;
	private View mView;
	private TextView menuTitleTextView;
	private ImageView drawerSwitcherImg;
	private DrawerLayout drawerLayout;
	private ImageView menuBackImg;
	private ImageView menuLogoImageView;
	private View mRightDrawerView;

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

    @Override
    public void splashLoaded() {
        loadRepositorySelection();
    }
}
