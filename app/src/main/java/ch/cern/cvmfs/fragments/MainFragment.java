package ch.cern.cvmfs.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.molina.cvmfs.history.RevisionTag;
import com.molina.cvmfs.repository.Repository;
import com.molina.cvmfs.revision.Revision;

import java.io.File;
import java.util.zip.Inflater;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.dialogs.AddRepositoryDialog;
import ch.cern.cvmfs.listeners.RepositoryStatusListener;
import ch.cern.cvmfs.model.RepositoryDescription;
import ch.cern.cvmfs.model.RepositoryManager;


public class MainFragment extends CVMFSFragment
        implements SplashFragment.SplashListener, DrawerFragment.DrawerListener,
        RepositorySelectionFragment.RepositorySelectionListener,
        BrowserFragment.BrowserFragmentListener, TagSelectionFragment.TagSelectionListener {

    private static final String FRAGMENT_STACK_NAME = "main_fragment_stack";
    private Toolbar toolbar;
    private View mView;
    private TextView menuTitleTextView;
    private ImageView drawerSwitcherImg;
    private DrawerLayout drawerLayout;
    private ImageView menuBackImg;
    private ImageView menuLogoImageView;
    private View mRightDrawerView;
    private ProgressDialog progressDialog;
    private String lastVisitedPath;
    private int lastRevision;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            lastRevision = savedInstanceState.getInt("revision");
            lastVisitedPath = savedInstanceState.getString("path");
        } else {
            lastRevision = -1;
            lastVisitedPath = "";
        }
        return mView = inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            loadSplash();
            replaceFragment(new DrawerFragment(), R.id.loggedin_main_right_frame);
            drawerLayout.setDrawerListener(new ActionBarDrawerToggle(getActivity(),
                    drawerLayout, R.string.drawer_opened, R.string.drawer_closed));
        } else if (lastRevision > 0) {
            menuLogoImageView.setVisibility(View.GONE);
            menuTitleTextView.setVisibility(View.VISIBLE);
            menuTitleTextView.setText(Integer.toString(lastRevision));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("revision", lastRevision);
        outState.putString("path", lastVisitedPath);
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
        if (drawerLayout.isDrawerOpen(mRightDrawerView))
            drawerLayout.closeDrawer(mRightDrawerView);
        removeFragments(FRAGMENT_STACK_NAME);
        toolbar.setVisibility(View.VISIBLE);
        RepositoryManager.getInstance().removeRepositoryInstance();
        menuTitleTextView.setVisibility(View.GONE);
        menuLogoImageView.setVisibility(View.VISIBLE);
        replaceFragment(new RepositorySelectionFragment(), R.id.main_container_frame);
    }

    private void loadTagSelection() {
        if (RepositoryManager.getInstance().getRepositoryInstance() != null) {
            drawerLayout.closeDrawer(mRightDrawerView);
            addFragment(new TagSelectionFragment(), R.id.main_container_frame, FRAGMENT_STACK_NAME);
        } else {
            showRepositoryNotLoadedMessage();
        }
    }

    private CVMFSFragment createNewBrowserFragment() {
        CVMFSFragment newFragment = new BrowserFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", lastVisitedPath);
        bundle.putInt("revision", lastRevision);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    private void replaceBrowserPath(PathNavigation navigation) {
        menuLogoImageView.setVisibility(View.GONE);
        menuTitleTextView.setText(R.string.revision_latest);
        if (lastRevision > 0)
            menuTitleTextView.setText(Integer.toString(lastRevision));

        switch (navigation) {
            case CHILD:
                CVMFSFragment newFragment = createNewBrowserFragment();
                addFragment(newFragment, R.id.main_container_frame, FRAGMENT_STACK_NAME);
                break;
            case PARENT:
                removeFragment(R.id.main_container_frame);
                break;
            case HOME:
                //removeFragments(FRAGMENT_STACK_NAME);
                CVMFSFragment homeFragment = createNewBrowserFragment();
                replaceFragment(homeFragment, R.id.main_container_frame, FRAGMENT_STACK_NAME);
                break;
        }

    }

    private void loadRepositoryView(RepositoryDescription chosen) {
        new LoadNewRepository(chosen).execute();
    }

    private void showRepositoryNotLoadedMessage() {
        Toast.makeText(getActivity(), R.string.toast_repository_not_selected,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void splashLoaded() {
        loadRepositorySelection();
    }

    @Override
    public void drawerHomeSelected() {
        Repository repo = RepositoryManager.getInstance().getRepositoryInstance();
        if (repo == null) {
            showRepositoryNotLoadedMessage();
            return;
        }
        drawerLayout.closeDrawer(mRightDrawerView);
        menuTitleTextView.setVisibility(View.VISIBLE);
        menuTitleTextView.setText("/");
        lastVisitedPath = "";
        replaceBrowserPath(PathNavigation.HOME);
    }

    @Override
    public void drawerAddRepositorySelected() {
		new AddRepositoryDialog(getActivity()).show();
    }

    @Override
    public void drawerTagsSelected() {
        loadTagSelection();
    }

    @Override
    public void drawerExitSelected() {
        loadRepositorySelection();

    }

    @Override
    public void repositoryChosen(RepositoryDescription chosen) {
        loadRepositoryView(chosen);
    }

    @Override
    public void directorySelected(String absolutePath, int revision) {
        String lastPath = absolutePath.equals("") ? "/" : "";
        PathNavigation navigation = absolutePath.contains(lastPath) ?
                PathNavigation.CHILD : PathNavigation.PARENT;
        lastVisitedPath = absolutePath;
        lastRevision = revision;
        replaceBrowserPath(navigation);
    }

    @Override
    public void fileSelected(String path) {
        new DownloadFile(path).execute();
    }

    @Override
    public void browserBack() {
        lastRevision = -1;
        lastVisitedPath = "";
        loadRepositorySelection();
    }

    @Override
    public void parentSelected(String parentPath, int revision) {
        lastVisitedPath = parentPath;
        lastRevision = revision;
        popFragment(R.id.main_container_frame, FRAGMENT_STACK_NAME);
    }

    @Override
    public void tagSelectionBack() {
        replaceBrowserPath(PathNavigation.PARENT);
    }

    @Override
    public void tagSelected(RevisionTag revisionTag) {
        lastRevision = revisionTag.getRevision();
        lastVisitedPath = "";
        replaceBrowserPath(PathNavigation.HOME);
    }

    protected enum PathNavigation {
        PARENT, CHILD, HOME
    }

    private class DownloadFile extends AsyncTask<String, Void, File> {

        private String path;

        public DownloadFile(String path) {
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    getResources().getString(R.string.dialog_loading),
                    getResources().getString(R.string.dialog_downloading_file),
                    true);
        }

        @Override
        protected File doInBackground(String... params) {
            final String path = this.path;
            final File[] object = {null};
            final Repository currentRepo = RepositoryManager.getInstance().getRepositoryInstance();
            RepositoryManager.getInstance().addTask(new Runnable() {

                @Override
                public void run() {
                    Revision rev = currentRepo.getCurrentRevision();
                    object[0] = rev.getFile(path);
                }
            });
            return object[0];
        }

        @Override
        protected void onPostExecute(File file) {
            progressDialog.dismiss();
            if (file != null && file.isFile()) {
                Uri uri = FileProvider.getUriForFile(getActivity(), "ch.cern.cvmfs.fragments.MainFragment", file);
                ContentResolver cr = getActivity().getContentResolver();
                String mimeType = cr.getType(uri);
                if (mimeType == null || mimeType.contains("octet-stream")) {
                    mimeType = "*/*";
                }
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(uri, mimeType);
                newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            } else {
                Toast.makeText(getActivity(), R.string.toast_file_not_retrieved, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class LoadNewRepository extends AsyncTask<Void, Void, Void> {

	    private RepositoryDescription chosen;

        public LoadNewRepository(RepositoryDescription chosen) {
	        this.chosen = chosen;
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
            RepositoryManager.getInstance().setRepositoryInstance(chosen.getUrl(),
		            mainStorageDirectory.getAbsolutePath());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if (RepositoryManager.getInstance().getRepositoryInstance() == null) {
                Toast.makeText(getActivity(), R.string.toast_repository_not_loadable, Toast.LENGTH_SHORT).show();
                return;
            }
	        // find the drawer fragment first
	        RepositoryStatusListener rsl = (RepositoryStatusListener) getCurrentFragment(R.id.loggedin_main_right_frame);
	        rsl.repositoryChanged(chosen);

            menuTitleTextView.setVisibility(View.VISIBLE);
            lastRevision = RepositoryManager.getInstance().getRepositoryInstance().getManifest().getRevision();
            lastVisitedPath = "";
            replaceBrowserPath(PathNavigation.HOME);
        }
    }
}
