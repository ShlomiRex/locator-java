import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * Consumes the filenames of SearchThread.
 * Consumer model.
 * @author Shlomi
 *
 */
public class SearchConsumerGUIThread extends Thread {

    private BlockingQueue<String> bq;
    private SearchWindow searchWindow;
    private boolean running;

    /**
     *
     * @param searchWindow For changing the GUI.
     * @param bq For reading the producer's output.
     */
    public SearchConsumerGUIThread(SearchWindow searchWindow, BlockingQueue<String> bq) {
        super("SearchGUIThread");
        this.bq = bq;
        this.searchWindow = searchWindow;
    }

    @Override
    public void run() {
        super.run();
        running = true;

        while(running) {
            try {
                String file = bq.take();
                //System.out.println("Taking: " + file);

                //Get abs path
                File f = new File(file);
                searchWindow.addFile(f.getAbsolutePath());
                f = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("Interrupted");
        running = false;
    }
}
