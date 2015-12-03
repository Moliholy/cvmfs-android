package ch.cern.cvmfs.model;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.molina.cvmfs.repository.Repository;
import com.molina.cvmfs.repository.exception.CacheDirectoryNotFound;
import com.molina.cvmfs.repository.exception.FailedToLoadSourceException;
import com.molina.cvmfs.repository.exception.RepositoryNotFoundException;
import com.molina.cvmfs.rootfile.exception.RootFileException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ch.cern.cvmfs.R;

public class RepositoryManager extends Thread {

	private static final String SAVED_REPOSITORIES_TAG = "saved_repositories";

    private static final Object LOCK = new Object();
    private static final Object LOCK_INSTANCE = new Object();
    private static RepositoryManager INSTANCE;
    private Repository currentRepository;
    private Queue<Runnable> tasks;
    private boolean closed;

    private RepositoryManager() {
        closed = false;
        tasks = new ArrayDeque<>();
    }

    public synchronized static RepositoryManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RepositoryManager();
            INSTANCE.start();
        }
        return INSTANCE;
    }

	public static void saveNewRepository(Context context,
	                                     RepositoryDescription newRepository) {
		Set<String> oldSet = retrieveSavedRepositories(context);
		oldSet.add(newRepository.toStoredFormat());
		context.getSharedPreferences("", Context.MODE_PRIVATE)
				.edit()
				.putStringSet(SAVED_REPOSITORIES_TAG, oldSet)
				.commit();
	}

	public static Set<String> retrieveSavedRepositories(Context context) {
		return context.getSharedPreferences("", Context.MODE_PRIVATE)
				.getStringSet(SAVED_REPOSITORIES_TAG, new HashSet<String>());
	}

    public static RepositoryDescription[] getRepositoryList(Activity activity) {
	    Set<String> savedRepos = retrieveSavedRepositories(activity);
        List<RepositoryDescription> repos = new ArrayList<>();
	    for (String repo : savedRepos) {
		    String[] parts = repo.split(";");
		    repos.add(new RepositoryDescription(parts[0], parts[1], parts[2]));
	    }
	    String[] unformattedList = activity.getResources()
			    .getStringArray(R.array.repo_list);
        for (String repo : unformattedList) {
            String[] parts = repo.split(";");
            repos.add(new RepositoryDescription(parts[0], parts[1],
			        parts[2]));
        }
        return repos.toArray(new RepositoryDescription[repos.size()]);
    }

    @Override
    public void run() {
        while (!closed) {
            synchronized (LOCK_INSTANCE) {
                while (tasks.isEmpty()) {
                    try {
                        Log.d("LOCK_INSTANCE", "Prepared to sleep until new tasks are added");
                        LOCK_INSTANCE.wait();
                        Log.d("LOCK_INSTANCE", "Prepared to grab a task");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Runnable task = tasks.poll();
            if (task != null) {
                Log.d(RepositoryManager.class.getName(), "Grabbing a task");
                task.run();
            }
            synchronized (LOCK) {
                LOCK.notifyAll();
                Log.d("LOCK", "Task finished. Lock notified");
            }
        }
    }

    public void removeRepositoryInstance() {
        tasks.clear();
        currentRepository = null;
    }

    public Repository setRepositoryInstanceAsync(final String url, final String cacheDirectory) {
        tasks.clear();
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    currentRepository = new Repository(url, cacheDirectory);
                } catch (FailedToLoadSourceException | IOException | RootFileException | CacheDirectoryNotFound | RepositoryNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        return currentRepository;
    }

	public Repository setRepositoryInstance(final String url, final String cacheDirectory) {
		tasks.clear();
		try {
			currentRepository = new Repository(url, cacheDirectory);
		} catch (RepositoryNotFoundException | FailedToLoadSourceException | IOException | CacheDirectoryNotFound | RootFileException e) {
			e.printStackTrace();
			currentRepository = null;
		}
		return currentRepository;
	}

	public Repository setRepositoryInstance(Repository repository) {
		tasks.clear();
		return currentRepository = repository;
	}

    public synchronized Repository getRepositoryInstance() {
        return currentRepository;
    }

    public void addTask(Runnable newTask) {
        synchronized (LOCK_INSTANCE) {
            tasks.add(newTask);
            LOCK_INSTANCE.notifyAll();
            Log.d("LOCK_INSTANCE", "Notifying new task added");
        }
        synchronized (LOCK) {
            try {
                Log.d("LOCK", "Waiting until the task finishes");
                LOCK.wait();
                Log.d("LOCK", "Just woke up after wait()");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
