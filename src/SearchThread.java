import java.util.concurrent.BlockingQueue;

/**
 * Search a filename that contains a string. Outputs to BlockingQueue.
 * Producer model.
 * @author Shlomi
 *
 */
public class SearchThread extends Thread {
    public SearchThread(BlockingQueue<String> bq) {
        super("SearchThread");
    }
}
