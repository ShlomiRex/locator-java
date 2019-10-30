import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private SearchParams searchParams;
    private Queue<File> dirQueue;
    private JButton stopButton;
    private boolean isRunning = false;

    //Files above this size are not read.
    private long fileMaxSize;

    public SearchProducerThread(JButton stopButton, SearchParams searchParams) {
        super("SearchThread");
        this.searchParams = searchParams;
        this.stopButton = stopButton;
        this.fileMaxSize = searchParams.isFileSizeSkip_size;

        dirQueue = new ConcurrentLinkedQueue<>();

        //Create shared producer-consumer queue
        this.bq = new LinkedBlockingQueue<String>();

        searchWindow = new SearchWindow(searchParams);
        searchConsumerGUIThread = new SearchConsumerGUIThread(searchWindow, bq);
    }

    @Override
    public void run() {
        super.run();
        isRunning = true;
        System.out.println("Searching string: " + searchParams.searchString);

        searchWindow.setVisible(true);
        searchConsumerGUIThread.start();

        //Search the current directory, linearSearch adds sub-directories into a Queue which will later use for linearSearch them.
        //This way, we don't go deep into sub-directories but instead search from top to bottom in the file hierarchy.Ba

        dirQueue.add(new File(searchParams.path));

        if(searchParams.isRecursive == false) {
            linearSearch(dirQueue.poll());
        }
        else {
            while (isRunning && dirQueue.isEmpty() == false) {
                File f = dirQueue.poll();
                linearSearch(f);
            }
        }

        //Finished searching, update others

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

    //TODO: FOR DEBUG ONLY
    //private ArrayList<File> filesEnqueued = new ArrayList<>();

    private void linearSearch(File dir) {
        File[] directoryListing = dir.listFiles();

        if(directoryListing == null || directoryListing.length == 0) {
            return;
        }

        ArrayList<File> files = new ArrayList<File>();
        files.addAll(Arrays.asList(directoryListing));

        for(File f : files) {
            if(isRunning == false)
                break;
            if(f.isDirectory()) {
                dirQueue.add(f);
                continue;
            }

            final String filepath = f.getAbsolutePath();

            //Is symbolic link?
            if(searchParams.isFollowSymbolicLinks && Files.isSymbolicLink(f.toPath())) {
                try {
                    Path link = Files.readSymbolicLink(f.toPath());
                    files.add(link.toFile());
                    System.out.println("Symbolic link:" + f.toPath() + " -> " + link);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Does not read long/big files
            if(searchParams.isFileSizeSkip && f.length() > fileMaxSize) {
                //System.out.println("Skips file: " + f.getAbsolutePath() + "\n\tReason: File size (" + f.length() + " Bytes) exceeds " + fileMaxSize + " Bytes");
                continue;
            }

            final Pattern pattern = Pattern.compile(searchParams.searchString);
            Matcher m;

            //Does the name has the string?
            try {
                if (searchParams.isIncludeFilename) {
                    if(searchParams.isRegex) {
                        m = pattern.matcher(f.getName());
                        if(m.find()) {
                            System.out.println("Regex match: " + f.getName());
                            bq.put(filepath);
                            continue;
                        }
                    } else {
                        if (searchParams.isCaseSensitive) {
                            if (f.getName().contains(searchParams.searchString)) {
                                bq.put(filepath);
                                continue;
                            }
                        } else {
                            if (f.getName().toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                                bq.put(filepath);
                                continue;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Does the file contains the string?
            try {
                String line;
                final FileReader fReader = new FileReader(f);
                final BufferedReader fileBuff = new BufferedReader(fReader);

                while ((line = fileBuff.readLine()) != null) {
                    if(searchParams.isRegex) {
                        if(searchParams.isRegex) {
                            m = pattern.matcher(line);
                            if(m.find()) {
                                System.out.println("Regex match: " + line);
                                bq.put(filepath);
                                break;
                            }
                        }
                    } else {
                        if (searchParams.isCaseSensitive) {
                            if (line.contains(searchParams.searchString)) {
                                bq.put(filepath);
                                break;
                            }
                        } else {
                            if (line.toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                                bq.put(filepath);
                                break;
                            }
                        }
                    }

                }
                fileBuff.close();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isRunning = false;
    }
}
