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
    private JTextArea textArea;
    private DefaultListModel<String> model;

    public SearchWindow(String search_str) throws HeadlessException {
        super("Search results");
        this.search_str = search_str;

        model = new DefaultListModel<>();
        list = new JList<>( model);

        //Text area
        JTextArea textArea = new JTextArea(10, 50);
        this.textArea = textArea;
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
                        highlight();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void highlight() throws IOException, BadLocationException {
        String file_path = list.getSelectedValue();
        FileReader fileReader = new FileReader(new File(file_path));
        textArea.read(fileReader, file_path);

        //Highlight the matches results within the text
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

        final String text = textArea.getText();
        int p0 = 0, p1 = 0;
        int offset = 0;

        while(true) {
            p0 = text.indexOf(search_str, offset);
            if(p0 == -1)
                break;

            p1 = p0 + search_str.length();
            offset += p1;
            highlighter.addHighlight(p0, p1, painter);
        }
    }

    public void addFile(String file) {
        model.addElement(file);
    }
}
