package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.molina.cvmfs.directoryentry.DirectoryEntry;
import com.molina.cvmfs.repository.Repository;
import com.molina.cvmfs.revision.Revision;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.model.RepositoryManager;


public class BrowserFragment extends CVMFSFragment {

    private View mView;
    private ProgressBar progressBar;
    private ListView mainListView;
    private String path;
    private List<DirectoryEntry> entries;
    private BrowserFragmentListener mCallback;
    private int revision;

    private int topList;
    private int indexList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_browser, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        path = getArguments().getString("path");
        revision = getArguments().getInt("revision");
        if (savedInstanceState != null) {
            topList = savedInstanceState.getInt("top_list");
            indexList = savedInstanceState.getInt("index_list");
        }
        new RepositoryOperation().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int index = mainListView.getFirstVisiblePosition();
        View v = mainListView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mainListView.getPaddingTop());
        outState.putInt("top_list", top);
        outState.putInt("index_list", index);
    }

    @Override
    public boolean onBackPressed() {
        int pos = path.lastIndexOf("/");
        if (pos >= 0) {
            String parentPath = path.substring(0, pos);
            mCallback.parentSelected(parentPath, revision);
            return true;
        }
        mCallback.browserBack();
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (BrowserFragmentListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException(getParentFragment() + " must implement " +
                    BrowserFragmentListener.class.getName());
        }
        super.onAttach(activity);
    }

    @Override
    protected void onPrepareInterface() {
        entries = new ArrayList<>();
        progressBar = (ProgressBar) mView.findViewById(R.id.browser_progressbar);
        mainListView = (ListView) mView.findViewById(R.id.browser_main_listview);
        mainListView.setAdapter(new FSAdapter(entries));
    }

    private void elementPressed(int position) {
        DirectoryEntry selectedDirent = entries.get(position);
        String absolutePath = path + "/" + selectedDirent.getName();
        if (selectedDirent.isDirectory()) {
            mCallback.directorySelected(absolutePath, revision);
        } else if (selectedDirent.isSymplink()) {
            Toast.makeText(getActivity(), R.string.symlinks_not_accepted, Toast.LENGTH_SHORT).show();
        } else {  // it's a file then
            mCallback.fileSelected(absolutePath);
        }
    }

    public interface BrowserFragmentListener {
        void directorySelected(String absolutePath, int revision);

        void fileSelected(String absolutePath);

        void browserBack();

        void parentSelected(String parentPath, int revision);
    }

    private class FSAdapter extends BaseAdapter {

        private List<DirectoryEntry> objects;

        public FSAdapter(List<DirectoryEntry> objects) {
            super();
            this.objects = objects;
        }

        public void setObjects(List<DirectoryEntry> objects) {
            this.objects = objects;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public Object getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View finalView = convertView;
            if (finalView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                finalView = inflater.inflate(R.layout.item_layout, parent, false);
            }
            finalView.setBackgroundResource(R.color.ui_white);
            final DirectoryEntry model = objects.get(position);

            TextView itemName = (TextView) finalView.findViewById(R.id.item_name_textview);
            try {
                itemName.setText(new String(model.getName().getBytes("US-ASCII")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ImageView itemImage = (ImageView) finalView.findViewById(R.id.item_imageview);
            if (model.isDirectory()) {
                if (model.isNestedCatalogMountpoint()) {
                    itemImage.setImageResource(R.drawable.ic_folder_catalog);
                } else {
                    itemImage.setImageResource(R.drawable.ic_folder_normal);
                }
            } else {
                itemImage.setImageResource(R.drawable.ic_file);
            }

            final int accessedPosition = position;
            finalView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementPressed(accessedPosition);
                }
            });
            return finalView;
        }
    }

    private class RepositoryOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            final Repository currentRepo = RepositoryManager.getInstance().getRepositoryInstance();
            RepositoryManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    Revision rev = revision == -1 ?
                            currentRepo.getCurrentRevision() : currentRepo.getRevision(revision);
                    entries = rev.listDirectory(path);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            progressBar.setVisibility(View.GONE);
            mainListView.setVisibility(View.VISIBLE);
            ((FSAdapter) mainListView.getAdapter()).setObjects(entries);
            ((FSAdapter) mainListView.getAdapter()).notifyDataSetChanged();
            mainListView.setSelectionFromTop(indexList, topList);
        }

    }

}
