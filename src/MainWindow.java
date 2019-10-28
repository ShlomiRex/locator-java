import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow extends JFrame {


    private SearchProducerThread searchProducerThread = null; //Initialized once for every seach.
    private Thread mainThread;
    public MainWindow() {
        super("locator");
        this.mainThread = Thread.currentThread();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(400,200);
        BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        // Search panel

        JTextField textField_Search = new JTextField(25);
        JButton btn_Search = new JButton("Search");

        JButton btn_StopSearching = new JButton("Stop");
        btn_StopSearching.setEnabled(false);

        JPanel panel_SearchPanel = new JPanel();
        panel_SearchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel_SearchPanel.add(textField_Search);
        panel_SearchPanel.add(btn_Search);
        panel_SearchPanel.add(btn_StopSearching);

        panel_SearchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Folder select panel

        JTextField textField_Path = new JTextField(25); //Needed for on folder select
        textField_Path.setText("C:\\Users\\Shlomi\\Desktop\\workspace");

        JLabel label_FolderSelect = new JLabel("Select a folder, or type path");
        JButton btn_SelectFolder = new JButton("Select");
        btn_SelectFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret_val = fileChooser.showOpenDialog(getContentPane());
                if(ret_val == JFileChooser.APPROVE_OPTION) {
                    File folder = fileChooser.getSelectedFile();
                    System.out.println("Selected folder: " + folder.getPath());
                    textField_Path.setText(folder.getPath());
                }
            }
        });

        JPanel panel_FolderSelect = new JPanel();
        panel_FolderSelect.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel_FolderSelect.add(label_FolderSelect);
        panel_FolderSelect.add(btn_SelectFolder);


        panel_FolderSelect.setAlignmentX(Component.LEFT_ALIGNMENT);


        // Path panel

        JLabel label_Path = new JLabel("Path:");


        JPanel panel_PathPanel = new JPanel();
        panel_PathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel_PathPanel.add(label_Path);
        panel_PathPanel.add(textField_Path);

        panel_PathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add panels

        add(panel_SearchPanel);
        add(panel_FolderSelect);
        add(panel_PathPanel);

        // Add components to BoxLayout
        JCheckBox checkBox_RecursiveSearch = new JCheckBox("Recursive search", true);
        checkBox_RecursiveSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox checkBox_SymbolicLinks = new JCheckBox("Follow symbolic links", true);
        checkBox_SymbolicLinks.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(checkBox_RecursiveSearch);
        add(checkBox_SymbolicLinks);

        // Finalize

        btn_Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Search clicked");

                SearchParams searchParams = new SearchParams(textField_Search.getText(), textField_Path.getText());
                searchParams.isFollowSymbolicLinks = checkBox_SymbolicLinks.isSelected();
                searchParams.isReursive = checkBox_RecursiveSearch.isSelected();

                searchProducerThread = new SearchProducerThread(btn_StopSearching, searchParams);
                searchProducerThread.start();

                btn_StopSearching.setEnabled(true);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }


}
