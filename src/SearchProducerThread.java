import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Search a filename that contains a string. Outputs to BlockingQueue.
 * Producer model.
 * @author Shlomi
 *
 */
public class SearchProducerThread extends Thread {

    private final String search_str;
    private final SearchWindow searchWindow;
    private final SearchConsumerGUIThread searchConsumerGUIThread;

    private BlockingQueue<String> bq;

    //Search parameters
    private String search_cmd;
    private String path;

    public SearchProducerThread(String search_str, String path) {
        super("SearchThread");
        this.search_str = search_str;
        this.path = path;

        //Create shared producer-consumer queue
        this.bq = new LinkedBlockingQueue<String>();

        searchWindow = new SearchWindow(search_str);
        searchConsumerGUIThread = new SearchConsumerGUIThread(searchWindow, bq);
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Searching string: " + search_str);

        searchWindow.setVisible(true);
        searchConsumerGUIThread.start();

        search_cmd = "";
        //search_cmd += "echo \"" + path + "\" | ";
        search_cmd += "findstr /s /M \"" + search_str + "\" *";
        System.out.println("Command: " + search_cmd);
        try {
            Process powerShellProcess = Runtime.getRuntime().exec(search_cmd);

            String line;
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            while ((line = stdout.readLine()) != null) {
                try {
                    bq.put(line);
                    //System.out.println("Putting: " + line);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            stdout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println("Finished searching");
    }
}
