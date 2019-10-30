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

    private JList<String> list;
//    private JTextArea textArea;
    private DefaultListModel<String> model;

    private SearchParams searchParams;

    public SearchWindow(SearchParams searchParams) {
        super("Search results");
        this.searchParams = searchParams;

        model = new DefaultListModel<>();
        list = new JList<>( model);

        setIconImage( new ImageIcon("images/icon.png").getImage());

        JScrollPane scrollPane_List = new JScrollPane(list);

        JTextAreaWithLineNumber textAreaWithLineNumber = new JTextAreaWithLineNumber();
        JTextArea textArea = textAreaWithLineNumber.getTextArea();
        textArea.setColumns(50);
        textArea.setRows(10);
        textArea.setEditable(false);

        setLayout( new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(scrollPane_List);
        add(textAreaWithLineNumber);
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
                    System.out.println("Selected: " + list.getSelectedValue());

                    try {
                        String file_path = list.getSelectedValue();
                        textAreaWithLineNumber.populateTextArea(file_path);

                        highlight(textArea);
                    } catch (IOException | BadLocationException exx) {
                        exx.printStackTrace();
                    }
                }
            }
        });


    }

    private void highlight(JTextArea textArea) throws BadLocationException {
        //Highlight the matches results within the text
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

        final String text = textArea.getText().toLowerCase();
        int p0 = 0, p1 = 0;
        int offset = 0;
        final int searchString_length = searchParams.searchString.length();
        final String searchString_lower = searchParams.searchString.toLowerCase();

        while(true) {
            if(searchParams.isCaseSensitive)
                p0 = text.indexOf(searchString_lower, offset);
            else
                p0 = text.indexOf(searchString_lower, offset);

            if(p0 == -1)
                break;

            p1 = p0 + searchString_length;
            offset += p1;
            highlighter.addHighlight(p0, p1, painter);
        }

    }

    public void addFile(String file) {
        model.addElement(file);
    }
}
