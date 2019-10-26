import java.util.concurrent.BlockingQueue;

/**
 * Consumes the filenames of SearchThread.
 * Consumer model.
 * @author Shlomi
 *
 */
public class SearchConsumerGUIThread extends Thread {

    public SearchConsumerGUIThread(BlockingQueue<String> bq) {
        super("SearchGUIThread");
    }

}
