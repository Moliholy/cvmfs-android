package ch.cern.cvmfs.model;


import android.content.Context;
import android.os.Environment;

import com.molina.cvmfs.repository.Repository;
import com.molina.cvmfs.repository.exception.CacheDirectoryNotFound;
import com.molina.cvmfs.repository.exception.FailedToLoadSourceException;
import com.molina.cvmfs.rootfile.exception.RootFileException;

import java.io.IOException;

import ch.cern.cvmfs.R;

public class RepositoryManager {

	public static final String CACHE_PATH =
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/cvmfs_cache";

	private Repository currentRepository;

	public synchronized void setRepositoryInstance(String url) throws RootFileException, CacheDirectoryNotFound, FailedToLoadSourceException, IOException {
		currentRepository = new Repository(url, CACHE_PATH);
	}

	public synchronized Repository getRepositoryInstance() {
		return currentRepository;
	}


	public static RepositoryDescription[] getRepositoryList(Context context) {
		String[] unformattedList = context.getResources().getStringArray(R.array.repo_list);
		RepositoryDescription[] repos = new RepositoryDescription[unformattedList.length];
		int i = 0;
		for (String repo : unformattedList) {
			String[] parts = repo.split(";");
			repos[i++] = new RepositoryDescription(parts[0], parts[1], parts[2]);
		}
		return repos;
	}

	private RepositoryManager() {

	}
}
