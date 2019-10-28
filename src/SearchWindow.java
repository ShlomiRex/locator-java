import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SearchWindow extends JFrame {

    private String search_str;
    private JList<String> list;
    private DefaultListModel<String> model;

    public SearchWindow(String search_str) throws HeadlessException {
        super("Search results");
        this.search_str = search_str;

        model = new DefaultListModel<>();
        list = new JList<>( model);

        //Text area
        JTextArea textArea = new JTextArea(10, 50);
        textArea.setEditable(false);
        JScrollPane scrollPane_TextArea = new JScrollPane(textArea);
        JScrollPane scrollPane_List = new JScrollPane(list);


        setLayout( new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


        //Add components to frame
        add(scrollPane_List);
        add(scrollPane_TextArea);

        //Finilize
        pack();
        setLocationRelativeTo(null);

        list.addMouseListener( new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                boolean isRightClick = SwingUtilities.isRightMouseButton(e);
                if(isRightClick) {
                    /*
                    JPopupMenu popupMenu = new JPopupMenu("Test");
                    JMenuItem copy = new JMenuItem("Copy");
                    JMenuItem openFile = new JMenuItem("Open");
                    JMenuItem openInExplorer = new JMenuItem("Open in file explorer");

                    popupMenu.add(copy);
                    popupMenu.add(openFile);
                    popupMenu.add(openInExplorer);

                    copy.addActionListener(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Copy");
                        }
                    });

                    popupMenu.setVisible(true);

                    list.setSelectedIndex(list.locationToIndex(e.getPoint()));
                    list.getModel().get
                     */
                }
            }
        });

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting() == false) {
                    //System.out.println("Selected: " + list.getSelectedValue());

                    try {
                        String file_path = list.getSelectedValue();
                        FileReader fileReader = new FileReader(new File(file_path));
                        textArea.read(fileReader, file_path);

                        //Highlight the matches results within the text
                        Highlighter highlighter = textArea.getHighlighter();
                        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
                        //p0 = Starting index of a matching search
                        //p1 = End index of a matching search
                        final int txtLen = textArea.getText().length();
                        int p0 = 0, p1, left_offset = 0;
                        String textLeft = textArea.getText();
                        while(p0 < txtLen && p0 != -1) {
                            p0 = left_offset + textLeft.indexOf(search_str);
                            p1 = p0 + search_str.length();
                            highlighter.addHighlight(p0, p1, painter );
                            left_offset = p1;
                            textLeft = textLeft.substring(p1 - left_offset);
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
    }

    public void addFile(String file) {
        model.addElement(file);
    }
}
