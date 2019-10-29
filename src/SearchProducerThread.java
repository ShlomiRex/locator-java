import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
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
    private long fileMaxSize = 10;

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
        isRunning = true;
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
        files.addAll(Arrays.asList(directoryListing));

        for(File f : files) {
            if(f.isDirectory()) {
                dirQueue.add(f);
                continue;
            }

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

            //Does not read long files
            long file_size = f.length();
            long kb = file_size / 1024;
            long mb = kb / 1024;

            if(mb > fileMaxSize) {
                System.out.println("Skips file: " + f.getAbsolutePath() + "\n\tReason: File size (" + mb + "MB) exceeds " + fileMaxSize + "MB");
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
