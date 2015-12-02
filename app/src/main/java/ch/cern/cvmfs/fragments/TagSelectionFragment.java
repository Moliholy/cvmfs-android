package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.molina.cvmfs.history.History;
import com.molina.cvmfs.history.RevisionTag;
import com.molina.cvmfs.history.exception.HistoryNotFoundException;
import com.molina.cvmfs.repository.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.model.RepositoryManager;


public class TagSelectionFragment extends CVMFSFragment {

    private View mView;
    private ListView tagListView;
    private List<RevisionTag> tags;
    private TagSelectionListener mCallback;
    private DatePicker datepicker;
    private ImageButton calendarButton;

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (TagSelectionListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException(getParentFragment() + " must implement " +
                    TagSelectionListener.class.getName());
        }
        super.onAttach(activity);
    }

    @Override
    public boolean onBackPressed() {
        mCallback.tagSelectionBack();
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_tag_selection, container, false);
    }

    @Override
    protected void onPrepareInterface() {
        tagListView = (ListView) mView.findViewById(R.id.tag_selection_listview);
        datepicker = (DatePicker) mView.findViewById((R.id.tag_selection_datepicker));
        Calendar calendar = Calendar.getInstance();
        datepicker.init(calendar.get(Calendar.YEAR) - 1900,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        long newTimestamp = c.getTime().getTime() / 1000L;
                        filterTags(newTimestamp);
                    }
                });
        calendarButton = (ImageButton) mView.findViewById(R.id.tag_selection_calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCalendarStatus();
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new TagLoader().execute();
    }

    private void switchCalendarStatus() {
        if (datepicker.getVisibility() == View.VISIBLE) {
            // hide the datepicker
            calendarButton.setImageResource(R.drawable.ic_calendar_disabled);
            datepicker.setVisibility(View.GONE);
        } else {
            // show the datepicker
            calendarButton.setImageResource(R.drawable.ic_calendar_enabled);
            datepicker.setVisibility(View.VISIBLE);
        }
    }

    private void elementPressed(RevisionTag element) {
        mCallback.tagSelected(element);
    }

    private void filterTags(long timestamp) {
        int limit = 0;
        for (int i = 0; i < tags.size(); i++) {
            if (timestamp > tags.get(i).getTimestamp()) {
                limit = Math.min(i + 1, tags.size());
                break;
            }
        }
        List<RevisionTag> filteredList = new ArrayList<>(tags.subList(0, limit));
        Collections.reverse(filteredList);
        TagAdapter adapter = (TagAdapter) tagListView.getAdapter();
        adapter.setTagList(filteredList);
        adapter.notifyDataSetChanged();
    }

    public interface TagSelectionListener {
        void tagSelectionBack();

        void tagSelected(RevisionTag revisionTag);
    }

    private class TagAdapter extends BaseAdapter {

        private List<RevisionTag> tagList;

        public TagAdapter(List<RevisionTag> tagList) {
            this.tagList = tagList;
        }

        public void setTagList(List<RevisionTag> tagList) {
            this.tagList = tagList;
        }

        @Override
        public int getCount() {
            return tagList.size();
        }

        @Override
        public Object getItem(int i) {
            return tags.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View finalView = view;
            if (finalView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                finalView = inflater.inflate(R.layout.tag_layout, parent, false);
            }
            finalView.setBackgroundResource(R.color.ui_white);
            final RevisionTag model = tagList.get(position);

            TextView itemName = (TextView) finalView.findViewById(R.id.tag_name_textview);
            itemName.setText(model.getName());
            ImageView itemImage = (ImageView) finalView.findViewById(R.id.tag_imageview);
            itemImage.setImageResource(R.drawable.ic_tag);

            long timestamp = model.getTimestamp();
            Date date = new Date(timestamp * 1000L);
            TextView itemDate = (TextView) finalView.findViewById(R.id.tag_date_textview);
            itemDate.setText(date.toString());

            final int accessedPosition = position;
            finalView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementPressed(tagList.get(accessedPosition));
                }
            });
            return finalView;
        }
    }

    private class TagLoader extends AsyncTask<Void, Void, List<RevisionTag>> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    getResources().getString(R.string.dialog_loading),
                    getResources().getString(R.string.dialog_downloading_tags),
                    true);
        }

        @Override
        protected List<RevisionTag> doInBackground(Void... voids) {
            final Repository repository = RepositoryManager.getInstance().getRepositoryInstance();
            final List<RevisionTag> tags = new ArrayList<>();
            RepositoryManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        History history = repository.retrieveHistory();
                        tags.addAll(history.listTags());
                    } catch (HistoryNotFoundException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return tags;
        }

        @Override
        protected void onPostExecute(List<RevisionTag> revisionTags) {
            progressDialog.dismiss();
            if (revisionTags.isEmpty()) {
                Toast.makeText(getActivity(),
                        R.string.toast_no_tags_found, Toast.LENGTH_SHORT).show();
                mCallback.tagSelectionBack();
                return;
            }
            tags = revisionTags;
            tagListView.setAdapter(new TagAdapter(tags));
        }
    }
}
