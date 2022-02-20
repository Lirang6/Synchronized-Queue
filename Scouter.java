/**
 * Liran Goldstein - 205812689
 */

import java.io.File;
import java.util.Objects;

/**
 *
 * extends java.lang.Object
 * implements java.lang.Runnable
 *
 * A scouter thread This thread lists all sub-directories from a given root path. Each sub-directory is enqueued
 * to be searched for files by Searcher threads.
 */

public class Scouter implements Runnable {
    int id;
    SynchronizedQueue<File> directoryQueue;
    File root;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;

    /**
     * Constructor. Initializes the scouter with a queue for the directories to be searched and a root directory
     * to start from.
     * Parameters:
     * @param id The id of the thread running the instance
     * @param directoryQueue A queue for directories to be searched
     * @param root Root directory to start from
     * @param milestonesQueue A synchronizedQueue to write milestones to
     * @param isMilestones Indicating whether or not the running thread should write to the milestonesQueue
     */
    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root,
            SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Starts the scouter thread. Lists directories under root directory and adds them to queue, then lists directories
     * in the next level and enqueues them and so on. This method begins by registering to the directory queue as
     * a producer and when finishes, it unregisters from it. If the isMilestones was set in the constructor
     * (and therefore the milestonesQueue was sent to it as well, it should write every "important" action to this queue.
     */
    public void run(){
        directoryQueue.registerProducer();
        if(root.isDirectory()){
            directoryQueue.enqueue(root);
            if (isMilestones){
                milestonesQueue.registerProducer();
                milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named" +
                        root.getName() +" was scouted");
                milestonesQueue.unregisterProducer();
            }
        }
        runInRecursive(root);
        directoryQueue.unregisterProducer();
    }

    private  void runInRecursive(File currentRoot){
        try {
            if (Objects.requireNonNull(currentRoot.listFiles(File::isDirectory)).length == 0) {
                return;
            }

            for (File file : Objects.requireNonNull(currentRoot.listFiles(File::isDirectory))) {
                directoryQueue.enqueue(file);
                if (isMilestones){
                    milestonesQueue.registerProducer();
                    milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named" +
                            file.getName() +" was scouted");
                    milestonesQueue.unregisterProducer();
                }
                runInRecursive(file);
            }
        } catch (NullPointerException e){
            e.getStackTrace();
        }
    }
}
