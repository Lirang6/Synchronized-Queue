/**
 * Liran Goldstein - 204812689
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * extends java.lang.Object
 * implements java.lang.Runnable
 *
 * A copier thread. Reads files to copy from a queue and copies them to the given destination.
 */
public class Copier implements Runnable {
    public static final int COPY_BUFFER_SIZE = 1;

    int id;
    File destination;
    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;

    /**
     * Constructor. Initializes the worker with a destination directory and a queue of files to copy.
     * @param id The id of the thread running the specific instance
     * @param destination Destination directory
     * @param resultsQueue Queue of files found, to be copied milestonesQueue
     * @param milestonesQueue A synchronizedQueue to write milestones to
     * @param isMilestones Indicating whether or not the running thread should write to the milestonesQueue
     */
    public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue,
                  SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.destination = destination;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Runs the copier thread. Thread will fetch files from queue and copy them, one after each other, to the
     * destination directory. When the queue has no more files, the thread finishes. If the isMilestones was set in
     * the constructor (and therefore the milstonesQueue was sent to it as well, it should write every "important"
     * action to this queue.
     */
    public void run() {
        File fileToTransfer = resultsQueue.dequeue();
        if (!destination.exists()) {
            if (!destination.mkdir()) {
                return;
            }
        }
        try {
            while (fileToTransfer != null) {
                File dest = new File(destination, fileToTransfer.getName());
                Files.copy(fileToTransfer.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (isMilestones){
                    milestonesQueue.registerProducer();
                    milestonesQueue.enqueue("Copier from thread id " + id + ": file named" +
                            fileToTransfer.toString() +" was copied");
                    milestonesQueue.unregisterProducer();
                }
                fileToTransfer = resultsQueue.dequeue();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
