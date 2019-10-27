import javax.swing.*;
import java.awt.*;

public class SearchWindow extends JFrame {

    private JList<String> list;
    private DefaultListModel<String> model;

    public SearchWindow() throws HeadlessException {
        super("Search results");
        model = new DefaultListModel<>();
        list = new JList<>( model );

        JPanel panel = new JPanel();
        panel.add(list);

        add(panel);
    }

    public void addFile(String file) {
        //list.add(new JLabel(file));
        model.addElement(file);
    }
}
