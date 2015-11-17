package ch.cern.cvmfs.listeners;

import ch.cern.cvmfs.model.RepositoryDescription;

public interface RepositoryStatusListener {
	void repositoryChanged(RepositoryDescription repo);
}