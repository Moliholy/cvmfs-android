package ch.cern.cvmfs.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.molina.cvmfs.repository.Repository;

import java.net.MalformedURLException;
import java.net.URL;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.activities.MainActivity;
import ch.cern.cvmfs.model.RepositoryDescription;
import ch.cern.cvmfs.model.RepositoryManager;


public class AddRepositoryDialog extends AlertDialog {

	private EditText repoName;
	private EditText repoURL;
	private Button repoOKButton;
	private MainActivity mActivity;

	public AddRepositoryDialog(Activity activity) {
		super(activity);
		mActivity = (MainActivity) activity;
	}

	protected AddRepositoryDialog(Context context, int theme) {
		super(context, theme);
	}

	protected AddRepositoryDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	private boolean localValidation() {
		String urlString = repoURL.getText().toString();
		boolean fieldsCorrect = !repoName.getText().toString().isEmpty() &&
				!repoName.getText().toString().isEmpty() &&
				urlString.startsWith("http://");

		boolean urlCorrect = true;
		try {
			new URL(urlString);
		} catch (MalformedURLException e) {
			urlCorrect = false;
		}
		return fieldsCorrect && urlCorrect;
	}

	private void addNewRepository(String fqrn) {
		String name = repoName.getText().toString();
		String url = repoURL.getText().toString();
		RepositoryManager.saveNewRepository(getContext(),
				new RepositoryDescription(name, fqrn, url));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_add_repository);
		repoName = (EditText) findViewById(R.id.dialog_new_repo_name);
		repoURL = (EditText) findViewById(R.id.dialog_new_repo_url);
		repoOKButton = (Button) findViewById(R.id.dialog_new_repo_ok_button);

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		getWindow().setSoftInputMode(WindowManager
				.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		setTitle(R.string.dialog_add_title);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		repoOKButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (localValidation()) {
					new TestRepository(repoURL.getText().toString()).execute();
				} else {
					Toast.makeText(getContext(), getContext().getString(R
							.string.new_repo_bad_input), Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	private class TestRepository extends AsyncTask<Void, Void, Repository> {

		private String url;

		public TestRepository(String url) {
			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			repoOKButton.setEnabled(false);
			setCancelable(false);
		}

		@Override
		protected Repository doInBackground(Void... params) {
			String cachePath = mActivity.getCvmfsCachePath();
			RepositoryManager rm = RepositoryManager.getInstance();
			Repository current = rm.getRepositoryInstance();
			rm.setRepositoryInstanceAsync(url, cachePath);
			return current;
		}

		@Override
		protected void onPostExecute(Repository oldRepo) {
			repoOKButton.setEnabled(true);
			setCancelable(true);

			RepositoryManager rm = RepositoryManager.getInstance();
			Repository newRepo = rm.getRepositoryInstance();
			if (newRepo != null && newRepo.getFqrn() != null) {
				Toast.makeText(getContext(), getContext().getString
								(R.string.new_repo_successfully_added),
						Toast.LENGTH_LONG)
						.show();
				addNewRepository(newRepo.getFqrn());
				dismiss();
			} else {
				Toast.makeText(getContext(), getContext().getString(R
						.string.new_repo_not_reachable), Toast.LENGTH_LONG)
						.show();
			}
			rm.setRepositoryInstance(oldRepo);
		}

	}

}
