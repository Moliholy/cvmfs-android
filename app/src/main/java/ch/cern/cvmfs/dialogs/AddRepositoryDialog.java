package ch.cern.cvmfs.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.model.RepositoryDescription;
import ch.cern.cvmfs.model.RepositoryManager;


public class AddRepositoryDialog extends AlertDialog {

	private EditText repoName;
	private EditText repoFQRN;
	private EditText repoURL;
	private Button repoOKButton;

	public AddRepositoryDialog(Context context) {
		super(context);
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

	private void addNewRepository() {
		String name = repoName.getText().toString();
		String fqrn = repoFQRN.getText().toString();
		String url = repoURL.getText().toString();
		RepositoryManager.saveNewRepository(getContext(),
				new RepositoryDescription(name, fqrn, url));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_add_repository);
		repoName = (EditText) findViewById(R.id.dialog_new_repo_name);
		repoFQRN = (EditText) findViewById(R.id.dialog_new_repo_fqrn);
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
					addNewRepository();
					Toast.makeText(getContext(), getContext().getString(R
							.string.new_repo_successfully_added), Toast
							.LENGTH_LONG)
							.show();
					dismiss();
				} else {
					Toast.makeText(getContext(), getContext().getString(R
							.string.new_repo_bad_input), Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}
}
