import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow extends JFrame {


    private SearchProducerThread searchProducerThread = null; //Initialized once for every seach.
    private Thread mainThread;
    public MainWindow() {
        super("Search");
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
        JTextField textField_Path = new JTextField(35); //Needed for on folder select
        JLabel label_FolderSelect = new JLabel("Select a folder, or type path");
        JButton btn_SelectFolder = new JButton("Select");

        JPanel panel_FolderSelect = new JPanel();
        panel_FolderSelect.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel_FolderSelect.add(label_FolderSelect);
        panel_FolderSelect.add(btn_SelectFolder);


        panel_FolderSelect.setAlignmentX(Component.LEFT_ALIGNMENT);


        // Path panel
        JPanel panel_PathPanel = new JPanel();
        panel_PathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label_Path = new JLabel("Path:");
        panel_PathPanel.add(label_Path);
        panel_PathPanel.add(textField_Path);
        panel_PathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //Checkboxes
        JCheckBox checkBox_RecursiveSearch = new JCheckBox("Recursive search", true);
        checkBox_RecursiveSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox checkBox_SymbolicLinks = new JCheckBox("Follow symbolic links", true);
        checkBox_SymbolicLinks.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox checkBox_IncludeFilenames = new JCheckBox("Include filenames", true);
        checkBox_IncludeFilenames.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox checkBox_CaseSensitive = new JCheckBox("Case sensitive");
        JCheckBox checkBox_FileSizeSkip = new JCheckBox("Skip files");
        checkBox_FileSizeSkip.setSelected(true);

        // File size panel
        JPanel panel_FileSizePanel = new JPanel();
        panel_FileSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel_FileSizePanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel label_FileSkipSize = new JLabel("Skips file with size over:");
        panel_FileSizePanel.add(label_FileSkipSize);
        JSpinner spinner_FileSize = new JSpinner();
        ((JSpinner.DefaultEditor) spinner_FileSize.getEditor()).getTextField().setColumns(4);
        spinner_FileSize.setValue(new Long(5));
        JComboBox comboBox_SizeType = new JComboBox();
        comboBox_SizeType.addItem("B"); //0
        comboBox_SizeType.addItem("KB"); //1
        comboBox_SizeType.addItem("MB"); //2
        comboBox_SizeType.addItem("GB"); //3
        comboBox_SizeType.setSelectedIndex(2);

        panel_FileSizePanel.add(spinner_FileSize);
        panel_FileSizePanel.add(comboBox_SizeType);

        panel_FileSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        //Add to JFrame
        add(panel_SearchPanel);
        add(panel_FolderSelect);
        add(panel_PathPanel);
        add(checkBox_RecursiveSearch);
        add(checkBox_SymbolicLinks);
        add(checkBox_IncludeFilenames);
        add(checkBox_CaseSensitive);
        add(checkBox_FileSizeSkip);
        add(panel_FileSizePanel);


        // Finalize (listeners)
        btn_SelectFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String path = textField_Path.getText();
                if(path != null && path != "") {
                    try {
                        File previousDir = new File(path);
                        fileChooser.setCurrentDirectory(previousDir);
                    } catch (Exception e2) {
                        //skip
                    }
                }
                int ret_val = fileChooser.showOpenDialog(getContentPane());
                if(ret_val == JFileChooser.APPROVE_OPTION) {
                    File folder = fileChooser.getSelectedFile();
                    System.out.println("Selected folder: " + folder.getPath());
                    textField_Path.setText(folder.getPath());
                }
            }
        });

        btn_Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                SearchParams searchParams = new SearchParams(textField_Search.getText(), textField_Path.getText());
                searchParams.isFollowSymbolicLinks = checkBox_SymbolicLinks.isSelected();
                searchParams.isRecursive = checkBox_RecursiveSearch.isSelected();
                searchParams.isIncludeFilename = checkBox_IncludeFilenames.isSelected();
                searchParams.isCaseSensitive = checkBox_CaseSensitive.isSelected();
                searchParams.isFileSizeSkip = checkBox_FileSizeSkip.isSelected();

                long bytes = ((Number) spinner_FileSize.getValue()).longValue();
                int indexSelected = comboBox_SizeType.getSelectedIndex();
                bytes *= Math.pow(1024, indexSelected);
                searchParams.isFileSizeSkip_size = bytes;
                System.out.println(bytes);

                searchProducerThread = new SearchProducerThread(btn_StopSearching, searchParams);
                searchProducerThread.start();

                btn_StopSearching.setEnabled(true);
            }
        });

        btn_StopSearching.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProducerThread.interrupt();
            }
        });

        checkBox_FileSizeSkip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = checkBox_FileSizeSkip.isSelected();
                spinner_FileSize.setEnabled(enable);
                comboBox_SizeType.setEnabled(enable);
                label_FileSkipSize.setEnabled(enable);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }


}
