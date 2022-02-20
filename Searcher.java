/**
 * Liran Goldstein - 204812689
 */

import java.io.File;

/**
 * extends java.lang.Object
 * implements java.lang.Runnable
 *
 * A searcher thread. Searches for files containing a given pattern and that end with a specific extension in all
 * directories listed in a directory queue.
 */
public class Searcher implements Runnable {
    int id;
    public static int fileCount = 0;
    String extension;
    SynchronizedQueue<File> directoryQueue;
    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;

    /**
     * Constructor. Initializes the searcher thread.
     * @param id Unique id of the thread running the instance
     * @param extension Wanted extension
     * @param directoryQueue A queue with directories to search in (as listed by the scouter)
     * @param resultsQueue A queue for files found (to be copied by a copier)
     * @param milestonesQueue A synchronizedQueue to write milestones to
     * @param isMilestones indicating whether or not the running thread should write to the milestonesQueue
     */
    public Searcher(int id, String extension, SynchronizedQueue<File> directoryQueue,
             SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue,
             boolean isMilestones) {
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;

    }

    /**
     * Runs the searcher thread. Thread will fetch a directory to search in from the directory queue, then search all
     * files inside it (but will not recursively search subdirectories!). Files that a contain the pattern and have
     * the wanted extension are enqueued to the results queue. This method begins by registering to the results queue
     * as a producer and when finishes, it unregisters from it. If the isMilestones was set in the constructor
     * (and therefore the milestonesQueue was sent to it as well, it should write every "important" action to this queue.
     */

    public void run() {
        resultsQueue.registerProducer();
        try{
            File files = directoryQueue.dequeue();
                for (File file : files.listFiles(File::isFile)) {
                    if (fileCount < DiskSearcher.LIMIT_COPY && file.getName().endsWith(extension)) {
                        resultsQueue.enqueue(file);
                        fileCount++;
                        if (isMilestones){
                            milestonesQueue.registerProducer();
                            milestonesQueue.enqueue("Searcher on thread id " + id + ": file named" +
                                    file.toString() +" was found");
                            milestonesQueue.unregisterProducer();
                        }
                    }
                }
        } catch (NullPointerException e){
            e.getStackTrace();
        }

        resultsQueue.unregisterProducer();
    }
}
