import javax.swing.*;
import java.io.*;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    private Queue<File> dirQueue;

    private JButton stopButton;
    public SearchProducerThread(JButton stopButton, SearchParams searchParams) {
        super("SearchThread");
        this.searchParams = searchParams;
        this.stopButton = stopButton;

        dirQueue = new ConcurrentLinkedQueue<>();

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


        /*
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
        */

        //Search the current directory, linearSearch adds sub-directories into a Queue which will later use for linearSearch them.
        //This way, we don't go deep into sub-directories but instead search from top to bottom in the file hierarchy.Ba

        dirQueue.add(new File(searchParams.path));

        while(dirQueue.isEmpty() == false) {
            File f = dirQueue.poll();
            linearSearch(f);
        }

        //Tell consumer that producer stopped
        searchConsumerGUIThread.interrupt();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stopButton.setEnabled(false);
            }
        });

        System.out.println("Finished searching");
    }

    private void linearSearch(File dir) {
        File[] directoryListing = dir.listFiles();

        for(File f : directoryListing) {
            if(f.isDirectory()) {
                dirQueue.add(f);
                continue;
            }
            //Does not read long files
            long file_size = f.length();
            long kb = file_size / 1024;
            long mb = kb / 1024;

            long maxSizeMB = 100;
            if(mb > maxSizeMB) {
                System.out.println("Skips file: " + f.getAbsolutePath()+"\n\tReason: File size (" + mb + "MB) exceeds " + maxSizeMB + "MB");
                continue;
            }

            //Does the name has the string?
            if(searchParams.isIncludeFilename && f.getName().contains(searchParams.searchString)) {
                try {
                    bq.put(f.getAbsolutePath());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Does the file contains the string?
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
