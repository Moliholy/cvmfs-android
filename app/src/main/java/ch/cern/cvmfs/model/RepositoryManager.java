package ch.cern.cvmfs.model;


import android.content.Context;
import android.util.Log;

import com.molina.cvmfs.repository.Repository;
import com.molina.cvmfs.repository.exception.CacheDirectoryNotFound;
import com.molina.cvmfs.repository.exception.FailedToLoadSourceException;
import com.molina.cvmfs.repository.exception.RepositoryNotFoundException;
import com.molina.cvmfs.rootfile.exception.RootFileException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import ch.cern.cvmfs.R;

public class RepositoryManager extends Thread {

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

    public Repository setRepositoryInstance(final String url, final String cacheDirectory) {
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
                Log.d("LOCK", "Waiting!");
                LOCK.wait();
                Log.d("LOCK", "Just woke up after wait()");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
