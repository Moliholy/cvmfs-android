package ch.cern.cvmfs.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.model.RepositoryDescription;
import ch.cern.cvmfs.model.RepositoryManager;

public class RepositorySelectionFragment extends CVMFSFragment {

    private View mView;
    private Spinner repositorySpinner;
    private RepositoryDescription[] repositoryDescriptions;
    private RepositoryDescription selectedRepository;
    private Button enterButton;
    private RepositorySelectionListener mCallback;
    private int chosenPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return mView = inflater.inflate(R.layout.fragment_repository_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        chosenPosition = retrieveChosenPosition();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        storeSelectedPosition();
        super.onDestroy();
    }

    private void storeSelectedPosition() {
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit().putInt("position", chosenPosition).commit();
    }

    private int retrieveChosenPosition() {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getInt("position", 0);
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (RepositorySelectionListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new RuntimeException(getParentFragment() + " must implement " +
                    RepositorySelectionListener.class.getName());
        }
        super.onAttach(activity);
    }

    @Override
    protected void onPrepareInterface() {
        repositoryDescriptions = RepositoryManager.getRepositoryList(getActivity());
        repositorySpinner = (Spinner) mView.findViewById(R.id.repo_selection_fqrn_spinner);
        enterButton = (Button) mView.findViewById(R.id.repo_selection_enter_button);

        ArrayAdapter<RepositoryDescription> arrayAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.spinner_textview);
        arrayAdapter.addAll(repositoryDescriptions);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repositorySpinner.setAdapter(arrayAdapter);
        repositorySpinner.setSelection(chosenPosition);
        repositorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRepository = repositoryDescriptions[position];
                enterButton.setEnabled(true);
                chosenPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRepository = null;
                enterButton.setEnabled(false);
            }
        });
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.repositoryChosen(selectedRepository);
            }
        });
    }

    public interface RepositorySelectionListener {
        void repositoryChosen(RepositoryDescription chosen);
    }
}
