import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Search a filename that contains a string. Outputs to BlockingQueue.
 * Producer model.
 * @author Shlomi
 *
 */
public class SearchProducerThread extends Thread {

    private final SearchWindow searchWindow;
    private final SearchConsumerGUIThread searchConsumerGUIThread;

    private BlockingQueue<String> bq;

    //Search parameters
    private SearchParams searchParams;

    public SearchProducerThread(SearchParams searchParams) {
        super("SearchThread");
        this.searchParams = searchParams;

        //Create shared producer-consumer queue
        this.bq = new LinkedBlockingQueue<String>();

        searchWindow = new SearchWindow(searchParams.searchString);
        searchConsumerGUIThread = new SearchConsumerGUIThread(searchWindow, bq);
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Searching string: " + searchParams.searchString);

        searchWindow.setVisible(true);
        searchConsumerGUIThread.start();

        /*
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
        */



        try {
            Files.walk(Paths.get(searchParams.path))
                    .filter(Files::isRegularFile)
                    .forEach((f)->{
                        String filepath = f.toString();
                        if(searchParams.isIncludeFilename && filepath.contains(searchParams.searchString)) {
                            try {
                                bq.put(filepath);
                                return; //Go next file
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        //If the file contains the searchString, add to bq
                        File file = new File(filepath);
                        String line;
                        try {
                            FileReader fReader = new FileReader(file);
                            BufferedReader fileBuff = new BufferedReader(fReader);
                            while ((line = fileBuff.readLine()) != null) {
                                if(line.contains(searchParams.searchString)) {
                                    bq.put(filepath);
                                    return;
                                }
                            }
                            fileBuff.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Finished searching");
    }

    private void recursiveSearch() {

    }

    private void linearSearch(String path) throws InterruptedException, IOException {
        File dir = new File(path);
        File[] directoryListing = dir.listFiles();
        for(File f : directoryListing) {
            //Does the name has the string?
            if(searchParams.isIncludeFilename && f.getName().contains(searchParams.searchString)) {
                bq.put(f.getAbsolutePath());
            }

            //Does the file contains the string?
            String line;
            FileReader fReader = new FileReader(f);
            BufferedReader fileBuff = new BufferedReader(fReader);
            while ((line = fileBuff.readLine()) != null) {
                if(line.contains(searchParams.searchString)) {
                    bq.put(f.getAbsolutePath());
                    break;
                }
            }
            fileBuff.close();
        }
    }
}
