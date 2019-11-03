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

        ArrayList<File> files = new ArrayList<File>();
        if(directoryListing != null)
            files.addAll(Arrays.asList(directoryListing));

        ArrayList<File> symbolicFiles = new ArrayList<>();

        for(File f : files) {
            if(isRunning == false)
                break;
            if(f.isDirectory()) {
                dirQueue.add(f);
                continue;
            }
            if(f.isFile() == false) {
                //Skip special files (unix)
                continue;
            }

            //Is symbolic link?
            if(searchParams.isFollowSymbolicLinks && Files.isSymbolicLink(f.toPath())) {
                try {
                    Path link = Files.readSymbolicLink(f.toPath());
                    //files.add(link.toFile()); //Can't modify while iterating on it
                    symbolicFiles.add(link.toFile());
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

            //Does the name has the string?
            if(searchParams.isIncludeFilename) {
                if(searchParams.isCaseSensitive) {
                    if( f.getName().contains(searchParams.searchString)) {
                        try {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if( f.getName().toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                        try {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }

            //Does the file contains the string?
            if(f.canRead()) {
                try {
                    String line;
                    FileReader fReader = new FileReader(f);
                    BufferedReader fileBuff = new BufferedReader(fReader, 1000000);
                    while ((line = fileBuff.readLine()) != null) {
                        if(searchParams.isCaseSensitive) {
                            if(line.contains(searchParams.searchString)) {
                                String fpath = f.getAbsolutePath();
                                bq.put(fpath);
                                break;
                            }
                        } else {
                            if(line.toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                                String fpath = f.getAbsolutePath();
                                bq.put(fpath);
                                break;
                            }
                        }
                    }
                    fileBuff.close();
                } catch (SecurityException | InterruptedException | IOException e) {
                    e.printStackTrace();
                    System.err.println("Error reading: " + f.getAbsolutePath());
                }
            }
        }

        for(File f : symbolicFiles) {
            if(isRunning == false)
                break;

            if(f.isFile() == false) {
                //Skip special files (unix)
                continue;
            }

            //Does not read long/big files
            if(searchParams.isFileSizeSkip && f.length() > fileMaxSize) {
                //System.out.println("Skips file: " + f.getAbsolutePath() + "\n\tReason: File size (" + f.length() + " Bytes) exceeds " + fileMaxSize + " Bytes");
                continue;
            }

            //Does the name has the string?
            if(searchParams.isIncludeFilename) {
                if(searchParams.isCaseSensitive) {
                    if( f.getName().contains(searchParams.searchString)) {
                        try {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if( f.getName().toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                        try {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }

            //Does the file contains the string?
            try {
                String line;
                FileReader fReader = new FileReader(f);
                BufferedReader fileBuff = new BufferedReader(fReader);
                while ((line = fileBuff.readLine()) != null) {
                    if(searchParams.isCaseSensitive) {
                        if(line.contains(searchParams.searchString)) {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            break;
                        }
                    } else {
                        if(line.toLowerCase().contains(searchParams.searchString.toLowerCase())) {
                            String fpath = f.getAbsolutePath();
                            bq.put(fpath);
                            break;
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
