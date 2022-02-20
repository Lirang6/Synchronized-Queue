/**
 * Liran Goldstein - 204812689
 */
import java.io.File;

public class DiskSearcher {

    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int LIMIT_COPY = 3;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length != 6) {
            System.out.println("illegal number of arguments");
            return;
        }

        boolean milestoneQueueFlag = Boolean.parseBoolean(args[0]);
        String fileExtension = args[1];
        File rootDirectory = new File(args[2]), destinationDirectory = new File(args[3]);
        int searchers = Integer.parseInt(args[4]), copiers = Integer.parseInt(args[5]), id = 0;

        if (searchers < 0) {
            System.out.println("illegal number of searchers");
            throw new NumberFormatException();
        }

        if (copiers < 0) {
            System.out.println("illegal number of copiers");
            throw new NumberFormatException();
        }

        SynchronizedQueue<File> resultQueue = new SynchronizedQueue<File>(RESULTS_QUEUE_CAPACITY);
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<File>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestonesQueue = null;
        if(milestoneQueueFlag){
            milestonesQueue = new SynchronizedQueue<>(500);
            milestonesQueue.registerProducer();
            milestonesQueue.enqueue("General, program has started the search");
            milestonesQueue.unregisterProducer();
        }

        Scouter scouter = new Scouter(id++, directoryQueue, rootDirectory, milestonesQueue, milestoneQueueFlag);
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();
        Thread[] searcherList = new Thread[searchers];
        Thread[] copierList = new Thread[copiers];

        for (int i = 0; i < searchers; i++) {
            Searcher searcher = new Searcher(id++, fileExtension, directoryQueue, resultQueue, milestonesQueue,
                    milestoneQueueFlag);
            Thread searcherThread = new Thread(searcher);
            searcherList[i] = searcherThread;
            searcherThread.start();
        }
        
        for (int i = 0; i < copiers; i++) {
            Copier copier = new Copier(id++, destinationDirectory, resultQueue, milestonesQueue, milestoneQueueFlag);
            Thread copierThread = new Thread(copier);
            copierList[i] = copierThread;
            copierThread.start();
        }
        try{
            scouterThread.join();

            for (int i = 0; i < searchers; i++) {
                searcherList[i].join();
            }

            for (int i = 0; i < copiers; i++) {
                copierList[i].join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        long run = end - start;
        System.out.println("Running time in milliseconds:" + run);
    }
}
