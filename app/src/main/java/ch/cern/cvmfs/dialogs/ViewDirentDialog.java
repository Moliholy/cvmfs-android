package ch.cern.cvmfs.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.molina.cvmfs.catalog.Catalog;
import com.molina.cvmfs.catalog.exception.CounterNotFound;
import com.molina.cvmfs.directoryentry.DirectoryEntry;

import java.util.Date;

import ch.cern.cvmfs.R;
import ch.cern.cvmfs.model.RepositoryManager;


public class ViewDirentDialog extends AlertDialog {

	private TextView field1TextView;
	private TextView value1TextView;
	private TextView field2TextView;
	private TextView value2TextView;
	private TextView field3TextView;
	private TextView value3TextView;
	private TextView field4TextView;
	private TextView value4TextView;
	private ProgressBar mProgressbar;
	private View mLayout;

	private int revision;
	private DirectoryEntry dirent;
	private String path;

	public ViewDirentDialog(Context context, DirectoryEntry dirent, int
			revision, String path) {
		super(context);
		this.dirent = dirent;
		this.revision = revision;
		this.path = path;
	}

	protected ViewDirentDialog(Context context, int theme) {
		super(context, theme);
	}

	protected ViewDirentDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.dialog_show_dirent);

		mProgressbar = (ProgressBar) findViewById(R.id.dirent_data_progressbar);
		mLayout = findViewById(R.id.dirent_data_main_layout);

		field1TextView = (TextView) findViewById(R.id
				.dialog_dirent_field_1_textview);
		field2TextView = (TextView) findViewById(R.id
				.dialog_dirent_field_2_textview);
		field3TextView = (TextView) findViewById(R.id
				.dialog_dirent_field_3_textview);
		field4TextView = (TextView) findViewById(R.id
				.dialog_dirent_field_4_textview);
		value1TextView = (TextView) findViewById(R.id
				.dialog_dirent_value_1_textview);
		value2TextView = (TextView) findViewById(R.id
				.dialog_dirent_value_2_textview);
		value3TextView = (TextView) findViewById(R.id
				.dialog_dirent_value_3_textview);
		value4TextView = (TextView) findViewById(R.id
				.dialog_dirent_value_4_textview);

		field1TextView.setText(getContext().getText(R.string.dirent_name));
		value1TextView.setText(dirent.getName());

		field2TextView.setText(getContext().getText(R.string.dirent_modified));
		value2TextView.setText(new Date(dirent.getMtime()).toString());

		if (dirent.isSymplink()) {
			field3TextView.setText(getContext().getText(R.string
					.dirent_symlink));
			value3TextView.setText(dirent.getSymlink());
			field4TextView.setText(R.string.dirent_size);
			value4TextView.setText(Formatter
					.formatShortFileSize(getContext(), dirent.getSize()));
		} else if (dirent.isFile()) {
			field3TextView.setText(getContext().getText(R.string.dirent_hash));
			value3TextView.setText(dirent.getContentHash());
			field4TextView.setText(R.string.dirent_size);
			value4TextView.setText(Formatter
					.formatShortFileSize(getContext(), dirent.getSize()));
		} else if (dirent.isNestedCatalogMountpoint()) {
			field3TextView.setText(R.string.dirent_hash);
			field4TextView.setText(R.string.dirent_catalog_entries);
			value3TextView.setText(R.string._unknown);
			value4TextView.setText(R.string._unknown);
			new LoadCatalog().execute();
		}
	}


	private class LoadCatalog extends AsyncTask<Void, Void, String[]> {

		@Override
		protected void onPreExecute() {
			mProgressbar.setVisibility(View.VISIBLE);
			mLayout.setVisibility(View.INVISIBLE);
		}

		@Override
		protected String[] doInBackground(Void... params) {
			final Catalog[] c = {null};
			final int[] numEntries = {0};
			RepositoryManager.getInstance().addTask(new Runnable() {
				@Override
				public void run() {
					c[0] = RepositoryManager.getInstance()
							.getRepositoryInstance()
							.getRevision(revision).retrieveCatalogForPath(path);
					try {
						numEntries[0] = c[0].getStatistics().numEntries();
					} catch (CounterNotFound counterNotFound) {
						counterNotFound.printStackTrace();
					}
				}
			});
			if (c[0] != null && numEntries[0] != 0)
				return new String[]{c[0].getHash(), String.valueOf
						(numEntries[0])};
			return null;
		}

		@Override
		protected void onPostExecute(String[] data) {
			mProgressbar.setVisibility(View.INVISIBLE);
			mLayout.setVisibility(View.VISIBLE);
			if (data != null) {
				value3TextView.setText(data[0]);
				value4TextView.setText(data[1]);
			}
		}
	}
}
